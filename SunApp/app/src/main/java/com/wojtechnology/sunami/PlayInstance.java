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
        song.lastPlayed = mDateTime;

        //Log.e("PlayInstance", "Received " + song.title + " played for " + duration + "ms at " + mDateTime.getTime().toString());
    }

    public static void setSongDate(FireMixtape song) {
        Calendar dateTime = Calendar.getInstance();
        song.lastPlayed = dateTime;
    }

    // Returns the fraction of the length of the song that the song was played for
    public double getFractionPlayed(){
        int songDuration = Integer.parseInt(mSong.duration);
        if (songDuration == 0) return 0.0;
        return ((double) mDuration) / ((double) songDuration);
    }

    public FireMixtape getSong() {
        return mSong;
    }

}
