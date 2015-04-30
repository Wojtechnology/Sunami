package com.wojtechnology.sunami;

import android.media.AudioManager;
import android.media.MediaPlayer;
import android.net.Uri;
import android.util.Log;

import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.PriorityQueue;
import java.util.Queue;

/**
 * Created by wojtekswiderski on 15-04-19.
 */

// Class that manages the smart shuffle
public class ShuffleManager {

    private List<FireMixtape> fireMixtapes;
    private FireMixtape playing;
    private MediaPlayer mediaPlayer;

    public ShuffleManager(List<FireMixtape> data){
        fireMixtapes = new ArrayList<FireMixtape>();
        for(FireMixtape item : data){
            fireMixtapes.add(item);
        }
        mediaPlayer = new MediaPlayer();
    }

    // Pseudorandomly shuffles the data
    private void randomShuffle(){

    }

    // Returns the current music list in current order
    public List<FireMixtape> getMusicList(){
        List<FireMixtape> data = new ArrayList<FireMixtape>();
        for(FireMixtape item : this.fireMixtapes){
            data.add(item);
        }
        return data;
    }

    public void playSong(String _id){
        for(int i = 0; i < fireMixtapes.size(); i++){
            if(fireMixtapes.get(i)._id == _id){
                playing = fireMixtapes.get(i);
                break;
            }
        }
        Log.e("PlaySong", playing.title);

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
