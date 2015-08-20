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

import java.util.ArrayList;
import java.util.List;

/**
 * Created by wojtekswiderski on 15-06-09.
 */
public class SongHistory {

    private FireMixtape[] mLastPlayed;
    private int mIndex;
    private final int mSize;

    public SongHistory(int size) {
        mSize = size;
        mIndex = -1;
        mLastPlayed = new FireMixtape[mSize];
    }

    public void push(FireMixtape song) {
        mIndex = (mIndex + 1) % mSize;
        mLastPlayed[mIndex] = song;
    }

    public FireMixtape pop() {
        if (mLastPlayed[mIndex] == null) {
            return null;
        }
        FireMixtape song = mLastPlayed[mIndex];
        mLastPlayed[mIndex] = null;
        mIndex = (mSize + mIndex - 1) % mSize;
        return song;
    }

    public List<FireMixtape> getHistoryList() {
        List<FireMixtape> songs = new ArrayList<>();
        int index = mIndex;
        while (mLastPlayed[index] != null) {
            songs.add(mLastPlayed[mIndex]);
            index = (mSize + index - 1) % mSize;
            if (index == mIndex) break;
        }
        return songs;
    }

    public boolean isEmpty () {
        return mIndex == -1 || mLastPlayed[mIndex] == null;
    }

    public void setHistoryList(List<FireMixtape> songs) {
        int index = mIndex, i = 0;
        while (mLastPlayed[index] != null) {
            index = (index - 1) % mSize;
            if (index == mIndex) return;
        }
        while (i < songs.size()) {
            if (mLastPlayed[index] != null) break;
            mLastPlayed[index] = songs.get(i++);
            index = (index - 1) % mSize;
        }
    }
}
