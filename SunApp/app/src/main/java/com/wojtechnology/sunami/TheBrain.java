package com.wojtechnology.sunami;

import android.content.Context;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.util.Log;
import android.widget.ProgressBar;

import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;

/**
 * Created by wojtekswiderski on 15-04-19.
 */

// Class that manages the smart shuffle
public class TheBrain {

    private static final int UP_NEXT_MIN = 10;

    private Context context;
    private boolean mChangedState;

    // Contains list of songs
    private SongManager songManager;

    // Contains list of genres
    private GenreGraph genreGraph;

    private Queue<FireMixtape> mUpNext;
    private FireMixtape playing;

    public MediaPlayer mediaPlayer;

    public TheBrain(Context context){
        this.context = context;
        mChangedState = false;
        mUpNext = new LinkedList<>();
        init();
    }

    // save all data that needs to persist in between sessions
    public void savePersistentState(){
        if (mChangedState) {
            genreGraph.saveGraph();
        }
    }

    private void init(){
        songManager = new SongManager(context);
        // Sort mixtapes for display
        genreGraph = new GenreGraph(context);
        mediaPlayer = new MediaPlayer();
    }

    private void loadQueue(){
        while (mUpNext.size() < UP_NEXT_MIN && mUpNext.size() < songManager.size()) {
            int random = (int) (Math.random() * songManager.size());
            FireMixtape song = songManager.getSong(songManager.getSongId(random));
            if(!mUpNext.contains(song)){
                mUpNext.add(song);
            }
        }
    }

    public List<FireMixtape> getDataByTitle() {
        return songManager.getByTitle();
    }

    public void playSong(String _id){
        playing = songManager.getSong(_id);

        if(playing != null) {
            Log.e("TheBrain", "Playing song " + playing.title);
            try{
                mediaPlayer.reset();
                mediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
                mediaPlayer.setDataSource(playing.data);
                mediaPlayer.prepare();
                mediaPlayer.start();
                ((MainActivity) context).playSong(playing);
            } catch (IOException e) {
                Log.e("TheBrain", "Player broked");
            }
        }else{
            Log.e("TheBrain", "Song with id " + _id + " not found");
        }
    }

    public void playNext(){
        loadQueue();
        playSong(mUpNext.remove()._id);
    }

    public boolean hasSong(){
        return playing == null ? false : true;
    }
}