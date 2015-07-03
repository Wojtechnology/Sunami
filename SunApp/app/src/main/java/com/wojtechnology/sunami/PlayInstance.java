package com.wojtechnology.sunami;

import android.util.Log;

import java.util.Calendar;

/**
 * Created by wojtekswiderski on 15-07-02.
 */
public class PlayInstance {

    private FireMixtape mSong;
    private int mDuration;
    private Calendar mDateTime;

    public PlayInstance(FireMixtape song, int duration) {
        mSong = song;
        mDuration = duration;
        mDateTime = Calendar.getInstance();

        Log.e("PlayInstance", "Received " + song.title + " played for " + duration + "ms at " + mDateTime.toString());

    }

}
