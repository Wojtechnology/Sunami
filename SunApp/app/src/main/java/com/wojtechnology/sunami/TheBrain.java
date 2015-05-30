package com.wojtechnology.sunami;

import android.app.Activity;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.os.AsyncTask;
import android.os.Binder;
import android.os.IBinder;
import android.util.Log;
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

    // Contains list of songs
    private SongManager songManager;

    // Contains list of genres
    private GenreGraph mGenreGraph;
    private Queue<FireMixtape> mUpNext;
    private FireMixtape playing;
    public MediaPlayer mediaPlayer;

    private final IBinder mBinder = new LocalBinder();

    @Override
    public void onCreate() {
        Log.e("TheBrain", "Started service");
        mChangedState = false;
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

    public class LocalBinder extends Binder {
        public TheBrain getServiceInstance() {
            return TheBrain.this;
        }
    }

    public void registerClient(MainActivity activity) {
        mContext = activity;
        init();
    }

    // save all data that needs to persist in between sessions
    public void savePersistentState() {
        if (mChangedState) {
            saveAppData();
        }
    }

    private void init() {
        songManager = new SongManager(mContext);
        // Sort mixtapes for display
        mGenreGraph = new GenreGraph(mContext);
        mediaPlayer = new MediaPlayer();
    }

    public void postInit() {
        loadQueue();
        mContext.setProgressBar(false);
        mContext.setRecyclerViewData();
        readAppData();
    }

    private class LoadAppDataTask extends AsyncTask<Void, Integer, Void> {

        @Override
        protected Void doInBackground(Void... params) {
            long startTime = Calendar.getInstance().getTimeInMillis();
            InputStream is;
            try {
                is = mContext.openFileInput("appData.json");
                Log.e("GenreGraph", "Open existing");
            } catch (FileNotFoundException e) {
                is = mContext.getResources().openRawResource(R.raw.genres);
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
                FileOutputStream fileOS = mContext.openFileOutput(
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
        while (mUpNext.size() < UP_NEXT_MIN && mUpNext.size() < songManager.size()) {
            int random = (int) (Math.random() * songManager.size());
            FireMixtape song = songManager.getSong(songManager.getSongId(random));
            if (!mUpNext.contains(song)) {
                mUpNext.add(song);
            }
        }
    }

    public List<FireMixtape> getDataByTitle() {
        return songManager.getByTitle();
    }

    public void playSong(String _id) {
        playing = songManager.getSong(_id);

        if (playing != null) {
            Log.e("TheBrain", "Playing song " + playing.title);
            try {
                mediaPlayer.reset();
                mediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
                mediaPlayer.setDataSource(playing.data);
                mediaPlayer.prepare();
                mediaPlayer.start();
                mContext.playSong(playing);
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
    }

    public boolean hasSong() {
        return playing != null;
    }
}