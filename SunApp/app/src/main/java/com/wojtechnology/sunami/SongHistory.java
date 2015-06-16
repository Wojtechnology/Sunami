package com.wojtechnology.sunami;

import android.util.Log;

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
