package com.wojtechnology.sunami;

import android.content.Context;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.util.Log;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by wojtekswiderski on 15-04-19.
 */

// Class that manages the smart shuffle
public class TheBrain {

    private Context context;
    private boolean changedState;

    // List used for the display
    private SongManager songManager;
    private FireMixtape playing;
    private FireMixtape next;
    private MediaPlayer mediaPlayer;
    private GenreGraph genreGraph;

    public TheBrain(Context context){
        this.context = context;
        this.changedState = false;
        init();
    }

    // save all data that needs to persist in between sessions
    public void savePersistentState(){
        if (changedState) {
            genreGraph.saveGraph();
        }
    }

    private void init(){
        songManager = new SongManager(context);
        // Sort mixtapes for display
        songManager.sortByTitle();
        genreGraph = new GenreGraph(context);
        mediaPlayer = new MediaPlayer();
    }

    public List<FireMixtape> getDataByTitle() {
        return songManager.getFire();
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
            } catch (IOException e) {
                e.printStackTrace();
            }
        }else{
            Log.e("TheBrain", "Song with id " + _id + " not found");
        }
    }
}