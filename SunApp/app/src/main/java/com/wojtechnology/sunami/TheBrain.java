package com.wojtechnology.sunami;

import android.app.Notification;
import android.app.PendingIntent;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.AudioManager;
import android.media.MediaMetadata;
import android.media.MediaPlayer;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Binder;
import android.os.Build;
import android.os.IBinder;
import android.os.PowerManager;
import android.support.v4.media.MediaMetadataCompat;
import android.support.v4.media.session.MediaSessionCompat;
import android.support.v4.media.session.PlaybackStateCompat;
import android.util.Log;
import android.widget.RemoteViews;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Calendar;
import java.util.List;

/**
 * Created by wojtekswiderski on 15-04-19.
 */

// Class that manages the smart shuffle
public class TheBrain extends Service {

    public static final String TOGGLE_PLAY = "toggle_play";
    public static final String PLAY_NEXT = "play_next";
    public static final String PLAY_LAST = "play_last";
    public static final String PLAY_STOP = "play_stop";
    private final String WAKE_LOCK_ID = "SunamiWakeLock";

    private final String SAVE_FILE_BASE = "__app_data_";

    private static final int HISTORY_SIZE = 10;

    private MainActivity mContext;
    private boolean mIsInit;
    private boolean mBound;
    private boolean mHasAudioFocus;
    private boolean mLoaded;
    private boolean mPreparing;

    // Determines whether the song will be paused after it is prepared
    private boolean mPauseAfterLoad;

    // Keeps track of how many save request were made
    private int mSaveCount;

    // Contains list of songs
    private SongManager mSongManager;
    private SongHistory mSongHistory;
    private PlayTimer mPlayTimer;
    private ShuffleController mShuffleController;

    // Contains list of genres
    private GenreGraph mGenreGraph;
    public FireMixtape mPlaying;
    public MediaPlayer mMediaPlayer;
    private AudioManager mAudioManager;
    private PowerManager.WakeLock mWakeLock;
    public UpNext mUpNext;
    AudioManager.OnAudioFocusChangeListener mAFChangeListener;

    // Sessions and Notifications
    private MediaSessionCompat mSession;
    private Notification mNotification;
    private RemoteViews mNotificationView;

    private final IBinder mBinder = new LocalBinder();

    // Receiver for plugging out earphones
    private class NoisyAudioStreamReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
            if (AudioManager.ACTION_AUDIO_BECOMING_NOISY.equals(intent.getAction())) {
                pausePlayback();
                setUI(false);
            }
        }
    }

    private NoisyAudioStreamReceiver mNoisyAudioStreamReceiver;

    private MediaPlayer.OnPreparedListener mMPPreparedListener;

    private class LoadAppDataTask extends AsyncTask<Void, Integer, Void> {

        private void readNew(JSONArray ja) {
            mGenreGraph.populateGraphJSON(ja);
            mSongManager.genresFromDB(mGenreGraph.getGenreSet());
        }

        private void readOld(JSONArray ja) throws JSONException {
            mGenreGraph.populateGraphJSON(ja.getJSONArray(1));
            mSongManager.updateGenres(mGenreGraph.getGenreSet(), ja.getJSONArray(2));
        }

        private void attemptReadOld() {
            long startTime = Calendar.getInstance().getTimeInMillis();
            boolean isNew = false;
            int numTries = 0;
            int fileNum = getExistingFile();
            InputStream is;
            do {
                try {
                    String fileName = SAVE_FILE_BASE + fileNum;
                    is = TheBrain.this.openFileInput(fileName);
                    Log.e("TheBrain", "Open existing " + fileName);
                } catch (FileNotFoundException e) {
                    is = TheBrain.this.getResources().openRawResource(R.raw.genres);
                    isNew = true;
                    Log.e("TheBrain", "Open new");
                }
                try {
                    BufferedReader reader = new BufferedReader(new InputStreamReader(is));
                    String line = reader.readLine();
                    String jString = "";
                    while (line != null) {
                        jString += line;
                        line = reader.readLine();
                    }
                    reader.close();
                    is.close();
                    JSONArray ja = new JSONArray(jString);
                    if (isNew) readNew(ja);
                    else readOld(ja);

                    // Enable shuffle controller to use calculated values to choose songs
                    mShuffleController.setLoadCompleted();
                    numTries = 2;
                } catch (IOException e) {
                    e.printStackTrace();
                    numTries++;
                    fileNum = (fileNum + 1) % 2;
                    if (numTries == 2) {
                        corruptedFile();
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                    numTries++;
                    fileNum = (fileNum + 1) % 2;
                    if (numTries == 2) {
                        corruptedFile();
                    }
                }
            } while (numTries < 2);
            Log.i("TheBrain", "Finished reading file in " +
                    Long.toString(Calendar.getInstance().getTimeInMillis() - startTime) +
                    " millis.");
        }

        // Should never be called but here just in case...
        private void corruptedFile() {
            InputStream is = TheBrain.this.getResources().openRawResource(R.raw.genres);
            Log.e("TheBrain", "Opening new because corruption");
            try {
                BufferedReader reader = new BufferedReader(new InputStreamReader(is));
                String line = reader.readLine();
                String jString = "";
                while (line != null) {
                    jString += line;
                    line = reader.readLine();
                }
                reader.close();
                is.close();
                JSONArray ja = new JSONArray(jString);
                readNew(ja);

                // Enable shuffle controller to use calculated values to choose songs
                mShuffleController.setLoadCompleted();
            } catch (IOException e) {
                e.printStackTrace();
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }

        @Override
        protected Void doInBackground(Void... params) {
            attemptReadOld();
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);
            mLoaded = true;
            if (mBound) {
                mContext.refreshRecyclerViewData();
            }
        }
    }

    private int getExistingFile() {
        String[] files = fileList();
        for (int i = 0; i < files.length; i++) {
            String firstFile = files[i];
            if (firstFile.contains(SAVE_FILE_BASE)) {
                return Integer.parseInt(firstFile.replace(SAVE_FILE_BASE, ""));
            }
        }
        return -1;
    }

    private class SaveAppDataTask extends AsyncTask<Void, Integer, Void> {

        @Override
        protected Void doInBackground(Void... params) {
            if (mSaveCount <= 0) {
                return null;
            }

            Log.i("TheBrain", "Started saving file");
            long startTime = Calendar.getInstance().getTimeInMillis();
            int fileNum = getExistingFile();
            String fileName = SAVE_FILE_BASE + ((fileNum <= 0) ? 1 : 0);
            try {
                FileOutputStream fileOS = TheBrain.this.openFileOutput(
                        fileName, Context.MODE_PRIVATE);
                JSONArray ja = new JSONArray();
                ja.put(0, 1);
                ja.put(1, mGenreGraph.getGraphJSON());
                ja.put(2, mSongManager.getSongJSON());
                fileOS.write(ja.toString().getBytes());
                fileOS.close();
                deleteFile(SAVE_FILE_BASE + ((fileNum <= 0) ? 0 : 1));
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            } catch (JSONException e) {
                e.printStackTrace();
            }
            Log.i("TheBrain", "Finished saving file " + fileName + " in " +
                    Long.toString(Calendar.getInstance().getTimeInMillis() - startTime) +
                    " millis.");

            if (mSaveCount > 1) {
                mSaveCount = 1;
            } else {
                mSaveCount = 0;
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);
        }
    }

    @Override
    public void onCreate() {
        Log.e("TheBrain", "Started service");
        mSaveCount = 0;
        mIsInit = false;
        mBound = false;
        mHasAudioFocus = false;
        mLoaded = false;
        mPreparing = false;
        mPauseAfterLoad = false;
        mUpNext = new UpNext();
        mPlayTimer = new PlayTimer();
        mNoisyAudioStreamReceiver = new NoisyAudioStreamReceiver();
        mMPPreparedListener = new MediaPlayer.OnPreparedListener() {
            @Override
            public void onPrepared(MediaPlayer mp) {
                if (!mPauseAfterLoad) {
                    resumePlayback();
                    setUI(true);
                } else {
                    setUI(false);
                    mPauseAfterLoad = false;
                }
                mPreparing = false;
            }
        };
        super.onCreate();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        String action = intent.getAction();
        if (mIsInit && action != null) {
            switch (intent.getAction()) {
                case TOGGLE_PLAY:
                    togglePlay();
                    break;

                case PLAY_NEXT:
                    playNext();
                    break;

                case PLAY_LAST:
                    playLast();
                    break;

                case PLAY_STOP:
                    pausePlayback();
                    unregisterAudio();
                    stopForeground(true);
                    if (mBound) {
                        mContext.updateSongView();
                    } else {
                        mContext.serviceFinished();
                        mMediaPlayer.release();
                        stopSelf();
                    }
                    break;

                default:
                    break;
            }
        }
        return START_NOT_STICKY;
    }

    @Override
    public IBinder onBind(Intent intent) {
        return mBinder;
    }

    @Override
    public boolean onUnbind(Intent intent) {
        mBound = false;
        return super.onUnbind(intent);
    }

    public class LocalBinder extends Binder {
        public TheBrain getServiceInstance() {
            return TheBrain.this;
        }
    }

    public void registerClient(MainActivity activity) {
        mContext = activity;
        mBound = true;
        if (!mIsInit) {
            init();
            mIsInit = true;
        } else {
            mContext.setRecyclerViewData();
            mContext.showSong();
        }
    }

    private void setNotificationStatus(boolean isPlaying) {
        if (mPlaying == null) {
            stopForeground(true);
            return;
        }

        if (isPlaying) {
            mNotificationView.setInt(R.id.play_notif_button, "setBackgroundResource", R.drawable.ic_pause_hint);
        } else {
            mNotificationView.setInt(R.id.play_notif_button, "setBackgroundResource", R.drawable.ic_play_hint);
        }

        startForeground(534, mNotification);
    }

    // This is needed to keep the background service running
    private void setNotification(FireMixtape song) {
        if (mPlaying == null) {
            stopForeground(true);
            return;
        }
        setMetadata(song);

        if (mNotification == null || mNotificationView == null) {
            Intent notificationIntent = new Intent(this, MainActivity.class);
            PendingIntent pendingIntent = PendingIntent.getActivity(mContext, 0, notificationIntent, 0);

            mNotificationView = new RemoteViews(getPackageName(), R.layout.notification);

            mNotification = new Notification.Builder(this)
                    .setSmallIcon(R.mipmap.ic_launcher)
                    .setContentIntent(pendingIntent)
                    .setContent(mNotificationView).build();
        }

        Intent servicePlayIntent = new Intent(getApplicationContext(), TheBrain.class);
        servicePlayIntent.setAction(TheBrain.TOGGLE_PLAY);
        PendingIntent pendingPlayIntent = PendingIntent.getService(mContext, 0, servicePlayIntent, 0);
        Intent serviceNextIntent = new Intent(getApplicationContext(), TheBrain.class);
        serviceNextIntent.setAction(TheBrain.PLAY_NEXT);
        PendingIntent pendingNextIntent = PendingIntent.getService(mContext, 0, serviceNextIntent, 0);
        Intent serviceStopIntent = new Intent(getApplicationContext(), TheBrain.class);
        serviceStopIntent.setAction(TheBrain.PLAY_STOP);
        PendingIntent pendingStopIntent = PendingIntent.getService(mContext, 0, serviceStopIntent, 0);

        mNotificationView.setTextViewText(R.id.notif_title, mPlaying.title);
        mNotificationView.setTextViewText(R.id.notif_artist, mPlaying.artist);
        mNotificationView.setOnClickPendingIntent(R.id.play_notif_button, pendingPlayIntent);
        mNotificationView.setOnClickPendingIntent(R.id.next_notif_button, pendingNextIntent);
        mNotificationView.setOnClickPendingIntent(R.id.close_notif_button, pendingStopIntent);

        setNotificationStatus(true);
    }

    private void setMetadata(FireMixtape song) {
        PlaybackStateCompat state = new PlaybackStateCompat.Builder()
                .setActions(
                        PlaybackStateCompat.ACTION_PLAY | PlaybackStateCompat.ACTION_PLAY_PAUSE |
                                PlaybackStateCompat.ACTION_PAUSE | PlaybackStateCompat.ACTION_SEEK_TO |
                                PlaybackStateCompat.ACTION_SKIP_TO_NEXT | PlaybackStateCompat.ACTION_SKIP_TO_PREVIOUS)
                .setState(PlaybackStateCompat.STATE_PLAYING, 0, 0.0f)
                .build();
        MediaSessionCompatHelper.applyState(mSession, state);
        mSession.setMetadata(new MediaMetadataCompat.Builder()
                .putString(MediaMetadataCompat.METADATA_KEY_TITLE, song.title)
                .putString(MediaMetadataCompat.METADATA_KEY_ALBUM_ARTIST, song.artist)
                .putLong(MediaMetadata.METADATA_KEY_DURATION, Long.parseLong(song.duration))
                .build());
    }

    public void setProgress(int pos, boolean isPlaying) {
        mMediaPlayer.seekTo(pos);
        int speed = isPlaying ? 1 : 0;
        int playState = isPlaying ? PlaybackStateCompat.STATE_PLAYING : PlaybackStateCompat.STATE_PAUSED;
        PlaybackStateCompat state = new PlaybackStateCompat.Builder()
                .setActions(
                        PlaybackStateCompat.ACTION_PLAY | PlaybackStateCompat.ACTION_PLAY_PAUSE |
                                PlaybackStateCompat.ACTION_PAUSE | PlaybackStateCompat.ACTION_SEEK_TO |
                                PlaybackStateCompat.ACTION_SKIP_TO_NEXT | PlaybackStateCompat.ACTION_SKIP_TO_PREVIOUS)
                .setState(playState, pos, speed)
                .build();
        MediaSessionCompatHelper.applyState(mSession, state);
    }

    // save all data that needs to persist in between sessions
    public void savePersistentState() {
        Log.e("TheBrain", "Request Save");
        mSaveCount++;
        if (mLoaded) {
            saveAppData();
        }
    }

    private void init() {
        mSongManager = new SongManager(this);
        mGenreGraph = new GenreGraph(this);
        mSongHistory = new SongHistory(HISTORY_SIZE);
        mShuffleController = new ShuffleController(this, mGenreGraph, mSongManager, mUpNext);
        mMediaPlayer = new MediaPlayer();

        // Setting wake locks
        mMediaPlayer.setWakeMode(getApplicationContext(), PowerManager.PARTIAL_WAKE_LOCK);
        PowerManager powerManager = (PowerManager) getSystemService(Context.POWER_SERVICE);
        mWakeLock = powerManager.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, WAKE_LOCK_ID);
        mWakeLock.acquire();

        mAudioManager = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
        mMediaPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
            @Override
            public void onCompletion(MediaPlayer mp) {
                if (!mPreparing) {
                    playNext();
                }
            }
        });
        mMediaPlayer.setOnErrorListener(new MediaPlayer.OnErrorListener() {
            @Override
            public boolean onError(MediaPlayer mp, int what, int extra) {
                Log.e("TheBrain", "Error");
                return false;
            }
        });
        mMediaPlayer.setOnPreparedListener(mMPPreparedListener);
        mContext.setVolumeControlStream(AudioManager.STREAM_MUSIC);
    }

    public void postInit() {
        // Set the list for the shuffle controller to use
        mShuffleController.updateList();

        if (mBound) {
            mContext.setRecyclerViewData();
            mContext.showSong();
        }
        readAppData();
    }

    public FireMixtape getSongPlaying() {
        return mPlaying;
    }

    private void readAppData() {
        new LoadAppDataTask().execute();
    }

    private void saveAppData() {
        new SaveAppDataTask().execute();
    }

    public List<FireMixtape> getDataByTitle() {
        return mSongManager.getByTitle();
    }

    public List<FireMixtape> getDataByArtist() {
        return mSongManager.getByArtist();
    }

    public List<FireMixtape> getUpNext() {
        return mUpNext.data();
    }

    // Main functions that starts playback of a song
    public void playSong(FireMixtape song, boolean saveLast) {
        FireMixtape oldPlaying = mPlaying;
        mPlaying = song;

        if (song.isSoundcloud && !isNetworkAvailable()) {
            playNext();
            return;
        }

        if (mPlaying != null) {
            // request audio focus if already doesn't have it
            registerAudio();
            updateListItem(mPlaying);
            if (oldPlaying != null) {
                donePlayback(oldPlaying, mPlayTimer.reset());
                if (saveLast) {
                    mSongHistory.push(oldPlaying);
                }
            } else {
                mShuffleController.setSongValuesAsync();
            }
            Log.e("TheBrain", "Playing song " + mPlaying.title);
            mMediaPlayer.reset();
            mMediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
            try {
                mMediaPlayer.setDataSource(mPlaying.data);
                mMediaPlayer.prepareAsync();
                mPreparing = true;
                setNotification(mPlaying);
                if (mBound) {
                    mContext.playSong(mPlaying);
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        } else {
            Log.e("TheBrain", "No song provided");
        }
    }

    public boolean isNetworkAvailable() {
        ConnectivityManager connectivityManager
                = (ConnectivityManager) getSystemService(Service.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
        return activeNetworkInfo != null && activeNetworkInfo.isConnected();
    }

    public void addSongToFront(FireMixtape song) {
        mUpNext.pushFront(song);
        updateListItem(song);
        updateUpNextUI();
    }

    public void addSongToQueue(FireMixtape song) {
        mUpNext.pushBackUser(song);
        updateListItem(song);
        updateUpNextUI();
    }

    public void removeSong(FireMixtape song) {
        boolean isAuto = mUpNext.remove(song);
        updateListItem(song);
        updateUpNextUI();

        if (isAuto) {
            // Send an empty song play
            PlayInstance.setSongDate(song);
            mShuffleController.recalculateSong(song);
        } else {
            mShuffleController.loadNext();
        }
    }

    public void toggleSongInLibrary(FireMixtape song) {
        if (!mSongManager.addSong(song)) {
            mSongManager.removeSong(song);
        }
        if (mBound) {
            mContext.refreshRecyclerViewData();
        }
        savePersistentState();
        mShuffleController.updateList();
        mShuffleController.setSongValuesAsync();
    }

    public boolean isSongInLibrary(FireMixtape song) {
        return mSongManager.containsSong(song);
    }

    public void updateListItem(FireMixtape song) {
        mContext.mListAdapter.updateItem(song);
    }

    public void updateUpNextUI() {
        if (mBound) {
            mContext.mDrawerFragment.updateRecyclerView();
        }
    }

    public void updateUpNextUIAsync() {
        if (mBound) {
            mContext.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    mContext.mDrawerFragment.updateRecyclerView();
                }
            });
        }
    }

    private void donePlayback(FireMixtape song, int duration) {
        PlayInstance playInstance = new PlayInstance(song, duration);
        mShuffleController.addPlayInstance(playInstance);
        savePersistentState();
    }

    // Registers audio and media session
    private void registerAudio() {
        if (mHasAudioFocus || !mIsInit) {
            return;
        }
        mHasAudioFocus = true;

        // Add audio focus change listener
        mAFChangeListener = new AudioManager.OnAudioFocusChangeListener() {
            public void onAudioFocusChange(int focusChange) {
                if (focusChange == AudioManager.AUDIOFOCUS_LOSS_TRANSIENT) {
                    pausePlayback();
                    setUI(false);
                } else if (focusChange == AudioManager.AUDIOFOCUS_GAIN) {
                    // Does nothing cause made me play music at work
                } else if (focusChange == AudioManager.AUDIOFOCUS_LOSS) {
                    pausePlayback();
                    unregisterAudio();
                    setUI(false);
                }
            }
        };

        mAudioManager.requestAudioFocus(mAFChangeListener, AudioManager.STREAM_MUSIC, AudioManager.AUDIOFOCUS_GAIN);

        // Add headphone out listener
        registerReceiver(mNoisyAudioStreamReceiver, new IntentFilter(AudioManager.ACTION_AUDIO_BECOMING_NOISY));

        // Add notification and transport controls
        ComponentName eventReceiver = new ComponentName(getPackageName(), RemoteControlEventReceiver.class.getName());
        mSession = new MediaSessionCompat(this, "FireSession", eventReceiver, null);
        mSession.setFlags(MediaSessionCompat.FLAG_HANDLES_TRANSPORT_CONTROLS | MediaSessionCompat.FLAG_HANDLES_MEDIA_BUTTONS);
        mSession.setPlaybackToLocal(AudioManager.STREAM_MUSIC);
        mSession.setMetadata(new MediaMetadataCompat.Builder()
                .putString(MediaMetadataCompat.METADATA_KEY_TITLE, "")
                .putString(MediaMetadataCompat.METADATA_KEY_ALBUM_ARTIST, "")
                .putLong(MediaMetadata.METADATA_KEY_DURATION, -1)
                .build());

        mSession.setCallback(new MediaSessionCompat.Callback() {

            @Override
            public void onSeekTo(long pos) {
                super.onSeekTo(pos);
                setProgress((int) pos, isPlaying());
            }
        });
        mSession.setActive(true);
    }

    private void unregisterAudio() {
        if (!mHasAudioFocus) return;
        mHasAudioFocus = false;
        unregisterReceiver(mNoisyAudioStreamReceiver);
        mAudioManager.abandonAudioFocus(mAFChangeListener);
        mSession.release();
    }

    public void togglePlay() {
        if (isPlaying() || mPreparing) {
            pausePlayback();
            setUI(false);
        } else {
            if (!hasSong()) {
                playNext();
            } else {
                resumePlayback();
                registerAudio();
                setMetadata(mPlaying);
                setUI(true);
            }
        }
    }

    private void pausePlayback() {
        if (mPreparing) {
            mPauseAfterLoad = true;
            return;
        }
        mPlayTimer.stop();
        mMediaPlayer.pause();
    }

    private void resumePlayback() {
        mPlayTimer.start();
        mMediaPlayer.start();
    }

    // Changes UI as required
    private void setUI(boolean isPlaying) {
        setProgress(mMediaPlayer.getCurrentPosition(), isPlaying);
        setNotificationStatus(isPlaying);
        if (mBound) {
            mContext.updateSongView();
        }
    }

    public void playNext() {
        mShuffleController.loadNext();
        if (mUpNext.size() > 0) {
            playSong(mUpNext.popFront(), true);
        }
        updateUpNextUI();
    }

    public void playLast() {
        if (mSongHistory.isEmpty()) {
            setUI(false);
            return;
        }
        FireMixtape song = mSongHistory.pop();
        if (mPlaying != null) {
            mUpNext.pushFront(mPlaying);
        }
        playSong(song, false);
        updateUpNextUI();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        mWakeLock.release();
        mMediaPlayer.release();
        mSession.release();
    }

    public boolean hasSong() {
        return mPlaying != null;
    }

    public boolean isPlaying() {
        try {
            return mMediaPlayer.isPlaying();
        } catch (Exception e) {
            return false;
        }
    }
}