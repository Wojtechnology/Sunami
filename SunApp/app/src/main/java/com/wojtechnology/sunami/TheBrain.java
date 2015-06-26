package com.wojtechnology.sunami;

import android.app.Notification;
import android.app.PendingIntent;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.media.AudioManager;
import android.media.MediaMetadata;
import android.media.MediaPlayer;
import android.media.RemoteControlClient;
import android.os.AsyncTask;
import android.os.Binder;
import android.os.IBinder;
import android.support.v4.media.MediaMetadataCompat;
import android.support.v4.media.session.MediaSessionCompat;
import android.support.v4.media.session.PlaybackStateCompat;
import android.util.Log;

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
public class TheBrain extends Service{

    public static final String ACTION_PLAY = "action_play";
    public static final String ACTION_PAUSE = "action_pause";
    public static final String ACTION_NEXT = "action_next";
    public static final String ACTION_PREVIOUS = "action_previous";
    public static final String ACTION_STOP = "action_stop";

    private static final int UP_NEXT_MIN = 1;
    private static final int HISTORY_SIZE = 4;

    private MainActivity mContext;
    private boolean mChangedState;
    private boolean mIsInit;
    private boolean mBound;
    private boolean mHasAudioFocus;

    // Contains list of songs
    private SongManager mSongManager;
    private SongHistory mSongHistory;
    private PlayTimer mPlayTimer;

    // Contains list of genres
    private GenreGraph mGenreGraph;
    public FireMixtape mPlaying;
    public MediaPlayer mMediaPlayer;
    private AudioManager mAudioManager;
    public UpNext mUpNext;

    // Sessions and Notifications
    private MediaSessionCompat mSession;


    private final IBinder mBinder = new LocalBinder();

    // Receiver for plugging out earphones
    private class NoisyAudioStreamReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (AudioManager.ACTION_AUDIO_BECOMING_NOISY.equals(intent.getAction())) {
                mPlayTimer.stop();
                mMediaPlayer.pause();
                mContext.updateSongView();
            }
        }
    }

    private NoisyAudioStreamReceiver mNoisyAudioStreamReceiver;

    private class RemoteControlEventListener extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
            Log.e("TheBrain", "Got the shit");
            if (Intent.ACTION_MEDIA_BUTTON.equals(intent.getAction())) {
            }
        }
    }

    @Override
    public void onCreate() {
        Log.e("TheBrain", "Started service");
        mChangedState = false;
        mIsInit = false;
        mBound = false;
        mHasAudioFocus = false;
        mUpNext = new UpNext();
        mPlayTimer = new PlayTimer();
        mNoisyAudioStreamReceiver = new NoisyAudioStreamReceiver();
        super.onCreate();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
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
            loadQueue();
            if (mBound) {
                mContext.setProgressBar(false);
                mContext.setRecyclerViewData();
            }
        }
    }

    private void setForegroundService(){
        Intent notificationIntent = new Intent(this, MainActivity.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, notificationIntent, 0);
        Notification notification = new Notification.Builder(this)
                .setContentTitle(mPlaying.title)
                .setContentText(mPlaying.artist)
                .setSmallIcon(R.mipmap.ic_launcher)
                .setContentIntent(pendingIntent)
                .build();
        startForeground(534, notification);
    }

    // save all data that needs to persist in between sessions
    public void savePersistentState() {
        if (mChangedState) {
            saveAppData();
        }
    }

    private void init() {
        mSongManager = new SongManager(this);
        mGenreGraph = new GenreGraph(this);
        mSongHistory = new SongHistory(HISTORY_SIZE);
        mMediaPlayer = new MediaPlayer();
        mAudioManager = (AudioManager) mContext.getSystemService(Context.AUDIO_SERVICE);
        mMediaPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
            @Override
            public void onCompletion(MediaPlayer mp) {
                playNext();
            }
        });
        mContext.setVolumeControlStream(AudioManager.STREAM_MUSIC);
    }

    public void postInit() {
        loadQueue();
        if (mBound) {
            mContext.setProgressBar(false);
            mContext.setRecyclerViewData();
        }
        readAppData();
    }

    private class LoadAppDataTask extends AsyncTask<Void, Integer, Void> {

        private void readNew(JSONArray ja) {
            mGenreGraph.populateGraphJSON(ja);
            mSongManager.genresFromDB(mGenreGraph.getGenreSet());
        }

        private void readOld(JSONArray ja) throws JSONException {
            mGenreGraph.populateGraphJSON(ja.getJSONArray(1));
            mSongManager.updateGenres(mGenreGraph.getGenreSet(), ja.getJSONArray(2));
        }

        @Override
        protected Void doInBackground(Void... params) {
            long startTime = Calendar.getInstance().getTimeInMillis();
            boolean isNew = false;
            InputStream is;
            try {
                is = TheBrain.this.openFileInput("appData.json");
                Log.e("GenreGraph", "Open existing");
            } catch (FileNotFoundException e) {
                is = TheBrain.this.getResources().openRawResource(R.raw.genres);
                isNew = true;
                Log.e("GenreGraph", "Open new");
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
            } catch (IOException e) {
                e.printStackTrace();
            } catch (JSONException e) {
                e.printStackTrace();
            }
            Log.i("TheBrain", "Finished reading file in " +
                    Long.toString(Calendar.getInstance().getTimeInMillis() - startTime) +
                    " millis.");
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);
            new SaveAppDataTask().execute();
        }
    }

    private void readAppData() {
        new LoadAppDataTask().execute();
    }

    private class SaveAppDataTask extends AsyncTask<Void, Integer, Void>{

        @Override
        protected Void doInBackground(Void... params) {
            long startTime = Calendar.getInstance().getTimeInMillis();
            try{
                FileOutputStream fileOS = TheBrain.this.openFileOutput(
                        "appData.json", Context.MODE_PRIVATE);
                JSONArray ja = new JSONArray();
                ja.put(0, 1);
                ja.put(1, mGenreGraph.getGraphJSON());
                ja.put(2, mSongManager.getSongJSON());
                fileOS.write(ja.toString().getBytes());
                fileOS.close();
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            } catch (JSONException e) {
                e.printStackTrace();
            }
            Log.i("TheBrain", "Finished saving file in " +
                    Long.toString(Calendar.getInstance().getTimeInMillis() - startTime) +
                    " millis.");
            return null;
        }
    }

    private void saveAppData() {
        new SaveAppDataTask().execute();
    }

    private void loadQueue() {
        while (mUpNext.size() < UP_NEXT_MIN && mUpNext.size() < mSongManager.size()) {
            int random = (int) (Math.random() * mSongManager.size());
            FireMixtape song = mSongManager.getSong(mSongManager.getSongId(random));
            if (!mUpNext.contains(song)) {
                mUpNext.pushBack(song);
            }
        }
    }

    public List<FireMixtape> getDataByTitle() {
        return mSongManager.getByTitle();
    }

    public List<FireMixtape> getUpNext() {
        return mUpNext.data();
    }

    public void playSong(FireMixtape song, boolean saveLast) {
        FireMixtape oldPlaying = mPlaying;
        mPlaying = song;

        if (mPlaying != null) {
            requestAudioFocus();
            if (oldPlaying == null){
                setForegroundService();
            } else {
                mPlayTimer.reset();
                if (saveLast) {
                    mSongHistory.push(oldPlaying);
                }
            }
            Log.e("TheBrain", "Playing song " + mPlaying.title);
            try {
                mMediaPlayer.reset();
                mMediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
                mMediaPlayer.setDataSource(mPlaying.data);
                mMediaPlayer.prepare();
                mPlayTimer.start();
                mMediaPlayer.start();
                if (mBound) {
                    mContext.playSong(mPlaying, Integer.parseInt(mPlaying.duration));
                }
                setMetaData(mPlaying);
            } catch (IOException e) {
                e.printStackTrace();
            }
        } else {
            Log.e("TheBrain", "No song provided");
        }
    }

    // Gets audio focus and registers remote controller
    private void requestAudioFocus() {
        if (mHasAudioFocus || !mIsInit) {
            return;
        }
        mHasAudioFocus = true;
        registerReceiver(mNoisyAudioStreamReceiver, new IntentFilter(AudioManager.ACTION_AUDIO_BECOMING_NOISY));
        registerMediaSession();

        AudioManager.OnAudioFocusChangeListener afChangeListener = new AudioManager.OnAudioFocusChangeListener() {
            public void onAudioFocusChange(int focusChange) {
                if (focusChange == AudioManager.AUDIOFOCUS_LOSS_TRANSIENT) {
                    mPlayTimer.stop();
                    mMediaPlayer.pause();
                    mHasAudioFocus = false;
                    unregisterReceiver(mNoisyAudioStreamReceiver);
                } else if (focusChange == AudioManager.AUDIOFOCUS_GAIN) {
                    // Does nothing cause made me play music at work
                } else if (focusChange == AudioManager.AUDIOFOCUS_LOSS) {
                    mAudioManager.abandonAudioFocus(this);
                    mHasAudioFocus = false;
                    mPlayTimer.stop();
                    mMediaPlayer.pause();
                    unregisterReceiver(mNoisyAudioStreamReceiver);
                }
                mContext.updateSongView();
            }
        };

        mAudioManager.requestAudioFocus(afChangeListener, AudioManager.STREAM_MUSIC, AudioManager.AUDIOFOCUS_GAIN);
    }

    private void registerMediaSession() {
        ComponentName eventReceiver = new ComponentName(getPackageName(), RemoteControlEventListener.class.getName());
        Intent mediaButtonIntent = new Intent(Intent.ACTION_MEDIA_BUTTON);
        mediaButtonIntent.setComponent(eventReceiver);
        PendingIntent mediaPendingIntent = PendingIntent.getBroadcast(getApplicationContext(), 0, mediaButtonIntent, 0);
        mSession = new MediaSessionCompat(mContext, "FireSession", eventReceiver, mediaPendingIntent);
        mSession.setFlags(MediaSessionCompat.FLAG_HANDLES_TRANSPORT_CONTROLS | MediaSessionCompat.FLAG_HANDLES_MEDIA_BUTTONS);
        mSession.setPlaybackToLocal(AudioManager.STREAM_MUSIC);
        // Got NPEs if didn't set this
        mSession.setMetadata(new MediaMetadataCompat.Builder()
                .putString(MediaMetadataCompat.METADATA_KEY_TITLE, "")
                .putString(MediaMetadataCompat.METADATA_KEY_ALBUM_ARTIST, "")
                .putLong(MediaMetadata.METADATA_KEY_DURATION, -1)
                .build());
        PlaybackStateCompat state = new PlaybackStateCompat.Builder()
                .setActions(
                        PlaybackStateCompat.ACTION_PLAY | PlaybackStateCompat.ACTION_PLAY_PAUSE |
                                PlaybackStateCompat.ACTION_PAUSE |
                                PlaybackStateCompat.ACTION_SKIP_TO_NEXT | PlaybackStateCompat.ACTION_SKIP_TO_PREVIOUS)
                .setState(PlaybackStateCompat.STATE_PLAYING, 0, 1, 0)
                .build();
        MediaSessionCompatHelper.applyState(mSession, state);
        mSession.setActive(true);

        Log.e("TheBrain", "registerMediaSession");
    }

    private void setMetaData(FireMixtape song) {
        mSession.setMetadata(new MediaMetadataCompat.Builder()
                .putString(MediaMetadataCompat.METADATA_KEY_TITLE, song.title)
                .putString(MediaMetadataCompat.METADATA_KEY_ALBUM_ARTIST, song.artist)
                .putLong(MediaMetadata.METADATA_KEY_DURATION, Long.parseLong(song.duration))
                .build());
    }

    public void playNext() {
        if (mUpNext.size() > 0) {
            playSong(mUpNext.popFront(), true);
        }
        loadQueue();
        mContext.mDrawerFragment.updateRecyclerView(this);
    }

    public void playLast() {
        if (mSongHistory.isEmpty()){
            return;
        }
        FireMixtape song = mSongHistory.pop();
        if (mPlaying != null) {
            mUpNext.pushFront(mPlaying);
        }
        playSong(song, false);
        mContext.mDrawerFragment.updateRecyclerView(this);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        mHasAudioFocus = false;
        unregisterReceiver(mNoisyAudioStreamReceiver);
        mSession.release();
    }

    public boolean hasSong() {
        return mPlaying != null;
    }

    public void togglePlay(){
        if(isPlaying()){
            mPlayTimer.stop();
            mMediaPlayer.pause();
        }else{
            mPlayTimer.start();
            if (!hasSong()) {
                playNext();
            } else {
                mMediaPlayer.start();
                requestAudioFocus();
            }
        }
    }

    public boolean isPlaying() {
        try {
            return mMediaPlayer.isPlaying();
        } catch (Exception e) {
            return false;
        }
    }
}