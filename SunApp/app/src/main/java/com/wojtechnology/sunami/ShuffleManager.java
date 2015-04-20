package com.wojtechnology.sunami;

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

    private Queue<FireMixtape> fireMixtapes;

    public ShuffleManager(List<FireMixtape> data){
        fireMixtapes = new LinkedList<FireMixtape>();
        for(FireMixtape item : data){
            fireMixtapes.add(item);
        }
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
}
