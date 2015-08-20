/*

    Sunami - An Android music player which knows what you want to listen to.
    Copyright (C) 2015 Wojtek Swiderski

    Sunami is free software: you can redistribute it and/or modify
    it under the terms of the GNU General Public License as published by
    the Free Software Foundation, either version 3 of the License, or
    (at your option) any later version.

    Sunami is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU General Public License for more details.

    The GNU General Public License can be found at the root of this repository.

    To contact me, email me at wojtek.technology@gmail.com

 */

package com.wojtechnology.sunami;

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
