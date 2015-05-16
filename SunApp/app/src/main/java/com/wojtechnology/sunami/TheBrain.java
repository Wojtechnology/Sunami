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

    // List used for the display
    private List<FireMixtape> fireMixtapes;
    private FireMixtape playing;
    private FireMixtape next;
    private MediaPlayer mediaPlayer;
    private GenreGraph genreGraph;

    public TheBrain(Context context){
        this.context = context;
        init();
    }

    // save all data that needs to persist in between sessions
    public void kill(){
        genreGraph.saveGraph();
    }

    private void init(){
        fireMixtapes = SongManager.getSongs(context);
        // Sort mixtapes for display
        fireMixtapes = SongManager.sortByTitle(context, fireMixtapes);
        genreGraph = new GenreGraph(context);
        mediaPlayer = new MediaPlayer();
    }

    public List<FireMixtape> getDataByTitle() {
        return fireMixtapes;
    }

    public void playSong(String _id){
        for(int i = 0; i < fireMixtapes.size(); i++){
            if(fireMixtapes.get(i)._id == _id){
                playing = fireMixtapes.get(i);
                break;
            }
        }
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
    }
}