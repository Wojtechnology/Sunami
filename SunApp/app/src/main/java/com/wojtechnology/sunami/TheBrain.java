package com.wojtechnology.sunami;

import android.app.Notification;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.os.AsyncTask;
import android.os.Binder;
import android.os.IBinder;
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
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;

/**
 * Created by wojtekswiderski on 15-04-19.
 */

// Class that manages the smart shuffle
public class TheBrain extends Service{

    private static final int UP_NEXT_MIN = 1;

    private MainActivity mContext;
    private boolean mChangedState;
    private boolean mIsInit;
    private boolean mBound;

    // Contains list of songs
    private SongManager mSongManager;

    // Contains list of genres
    private GenreGraph mGenreGraph;
    private LinkedList<FireMixtape> mUpNext;
    public FireMixtape mPlaying;
    public MediaPlayer mMediaPlayer;

    private final IBinder mBinder = new LocalBinder();

    @Override
    public void onCreate() {
        Log.e("TheBrain", "Started service");
        mChangedState = false;
        mIsInit = false;
        mBound = false;
        mUpNext = new LinkedList<>();
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
        mMediaPlayer = new MediaPlayer();
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

        @Override
        protected Void doInBackground(Void... params) {
            long startTime = Calendar.getInstance().getTimeInMillis();
            InputStream is;
            try {
                is = TheBrain.this.openFileInput("appData.json");
                Log.e("GenreGraph", "Open existing");
            } catch (FileNotFoundException e) {
                is = TheBrain.this.getResources().openRawResource(R.raw.genres);
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
                Log.i("TheBrain", "Finished reading file in " +
                        Long.toString(Calendar.getInstance().getTimeInMillis() - startTime) +
                        " millis.");
                JSONArray ja = new JSONArray(jString);
                mGenreGraph.populateGraphJSON(ja);
            } catch (JSONException e1) {
                e1.printStackTrace();
            } catch (IOException e1) {
                e1.printStackTrace();
            }
            return null;
        }
    }

    private void readAppData() {
        new LoadAppDataTask().execute();
    }

    private class SaveAppDataTask extends AsyncTask<Void, Integer, Void>{

        @Override
        protected Void doInBackground(Void... params) {
            try{
                FileOutputStream fileOS = TheBrain.this.openFileOutput(
                        "appData.json", Context.MODE_PRIVATE);
                JSONArray ja = mGenreGraph.getGraphJSON();
                fileOS.write(ja.toString().getBytes());
                fileOS.close();
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
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
                mUpNext.add(song);
            }
        }
    }

    public List<FireMixtape> getDataByTitle() {
        return mSongManager.getByTitle();
    }

    public List<FireMixtape> getUpNext() {
        return mUpNext;
    }

    public void playSong(String _id) {
        FireMixtape oldPlaying = mPlaying;
        mPlaying = mSongManager.getSong(_id);

        if (mPlaying != null) {
            if (oldPlaying == null){
                setForegroundService();
            }
            Log.e("TheBrain", "Playing song " + mPlaying.title);
            try {
                mMediaPlayer.reset();
                mMediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
                mMediaPlayer.setDataSource(mPlaying.data);
                mMediaPlayer.prepare();
                mMediaPlayer.start();
                if (mBound) {
                    mContext.playSong(mPlaying);
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        } else {
            Log.e("TheBrain", "Song with id " + _id + " not found");
        }
    }

    public void playNext() {
        if(mUpNext.size() > 0) {
            playSong(mUpNext.remove()._id);
        }
        loadQueue();
        mContext.mDrawerFragment.updateRecyclerView(this);
    }

    public boolean hasSong() {
        return mPlaying != null;
    }

    public void togglePlay(){
        if(isPlaying()){
            mMediaPlayer.pause();
        }else{
            if (!hasSong()) {
                playNext();
            } else {
                mMediaPlayer.start();
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