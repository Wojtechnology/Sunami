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

import android.os.AsyncTask;
import android.util.Log;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

/**
 * Created by wojtekswiderski on 15-07-02.
 */
public class ShuffleController {

    private GenreGraph mGenreGraph;
    private SongManager mSongManager;
    private TheBrain mTheBrain;
    private UpNext mUpNext;
    private List<FireMixtape> mSongList;

    private final double SONG_DURATION_OFFSET = 0.1;
    private final double SONG_DURATION_SPREAD = 0.05;
    private final double SONG_MIN = 1.0;
    private final double SONG_MAX = 1.8;
    private final double SONG_MED_MULTI = 0.1;
    private final double SONG_OFF_MULTI = 1.0;
    private final double SONG_POS_MULTI = 0.4;
    private final double SONG_NEG_MULTI = 0.5;
    private final double RANDOM_SPREAD = 0.2;

    private double mTimeSpread;
    private double mTimeOffset;

    // Cutoff threshold for song multiplier, currently 2 minutes
    private final long SONG_MULTIPLIER_CUTOFF = 120000;

    private boolean mIsLoaded;

    // Async wrapper for setSongValues function
    private class SetSongValuesTask extends AsyncTask<Void, Integer, Void> {
        @Override
        protected Void doInBackground(Void... params) {
            setSongValues();
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);
            loadNextAsync();
        }
    }

    // Async wrapper for loadNext function
    private class LoadNextTask extends AsyncTask<Void, Integer, Void> {
        @Override
        protected Void doInBackground(Void... params) {
            loadNext();
            return null;
        }
    }

    public ShuffleController(TheBrain theBrain, GenreGraph genreGraph, SongManager songManager, UpNext upNext) {
        mGenreGraph = genreGraph;
        mSongManager = songManager;
        mTheBrain = theBrain;
        mIsLoaded = false;
        mUpNext = upNext;
        updateValues();
    }

    public void updateValues() {
        mTimeSpread = mTheBrain.mMainPrefs.songCycle / 2;
        mTimeOffset = mTheBrain.mMainPrefs.songCycle;
    }

    public void setLoadCompleted() {
        updateList();
        setSongValuesAsync();
        mIsLoaded = true;
    }

    public void updateList() {
        mSongList = new ArrayList<>(mSongManager.getFire());
    }

    private void setSongValues() {
        double maxCalculatedValue = 0.0;
        for (int i = 0; i < mSongList.size(); i++) {
            double calculatedValue = calculateSongValue(mSongList.get(i));
            mSongList.get(i).calculatedValue = calculatedValue;
            if (calculatedValue > maxCalculatedValue) {
                maxCalculatedValue = calculatedValue;
            }
        }
        FireMixtape.maxCalculatedValue = maxCalculatedValue;
    }

    // Runs setSongValues in a separate thread and then loads the UpNext
    public void setSongValuesAsync() {
        new SetSongValuesTask().execute();
    }

    // Calculates the song value which ranks songs on fire level
    private double calculateSongValue(FireMixtape song) {
        double val = 1.0;
        if (mGenreGraph.isGenre(song.actualGenre)) {
            val *= mGenreGraph.getGenreLT(song.actualGenre);
            val *= mGenreGraph.getGenreST(song.actualGenre);
        }

        // Do not apply song multiplier is song is below cutoff
        if (Long.parseLong(song.duration) > SONG_MULTIPLIER_CUTOFF) {
            val *= song.multiplier;
        }
        val *= getLastPlayedMultiplier(song);
        val *= getRandomMultiplier();
        return val;
    }

    // Randomly load a song that isn't currently playing or in upnext
    private void randomLoadOne() {
        int random = (int) (Math.random() * mSongManager.size());
        FireMixtape song = mSongManager.getSongAtIndex(random);
        if (!isContained(song) && (mTheBrain.isSoundcloudEnabled() || !song.isSoundcloud)) {
            mUpNext.pushBack(song);
        }
    }

    // Find the highest valued song that isn't in the upnext or currently playing
    private boolean calculatedLoadOne() {
        double max = 0.0;
        FireMixtape highestSong = null;

        // Find the highest `calculatedValue`
        for (int i = 0; i < mSongList.size(); i++) {
            FireMixtape song = mSongList.get(i);
            if (!isContained(song) && song.calculatedValue > max && (mTheBrain.isSoundcloudEnabled() || !song.isSoundcloud)) {
                max = song.calculatedValue;
                highestSong = song;
            }
        }

        if (highestSong == null) {
            return false;
        }

        mUpNext.pushBack(highestSong);
        return true;
    }

    public void loadNext() {
        // `mSongList.size() - 1` to account for the song that is currently playing
        while (mUpNext.size() < mUpNext.UP_NEXT_MIN && mUpNext.size() < mSongList.size() - 1) {
            if (!mIsLoaded || !mTheBrain.isSmartEnabled()) {
                // If file not yet read, RNJesus
                randomLoadOne();
            } else {
                if (!calculatedLoadOne()) return;
            }
        }
        mTheBrain.updateUpNextUIAsync();
    }

    private void loadNextAsync() {
        new LoadNextTask().execute();
    }

    private boolean isContained(FireMixtape song) {
        return mUpNext.contains(song) ||
                mTheBrain.getSongPlaying() == song;
    }

    // Pass in the play instance and modify genre and song values based on play duration
    public void addPlayInstance(PlayInstance playInstance) {
        // cannot alter values if genres aren't in memory yet
        if (!mIsLoaded) return;

        // sets required variables
        double r = getPlayMultiplier(playInstance.getFractionPlayed(), SONG_DURATION_OFFSET, SONG_DURATION_SPREAD);
        FireMixtape song = playInstance.getSong();

        // Makes changes to song multipliers
        double songDelta = songChange(song.multiplier, r);
        song.multiplier = songDelta;
        //Log.i("ShuffleController", "Modified song value to " + songDelta);

        mGenreGraph.modifyGenre(song.actualGenre, r);

        song.calculatedValue = calculateSongValue(song);
        setSongValuesAsync();
    }

    public void recalculateSong(FireMixtape song) {
        if (!mIsLoaded) return;
        song.calculatedValue = calculateSongValue(song);
        setSongValuesAsync();
    }

    // Determines whether the play counts as a skip or other and calculates multiplier
    // Uses sigmoid function
    private double getPlayMultiplier(double fractionPlayed, double offset, double spread) {
        if (spread == 0.0) return 0.0;
        double k = -1.0 / spread;
        double denominator = 1.0 + Math.pow(Math.E, k * (fractionPlayed - offset));
        if (denominator == 0.0) return 0.0;
        double sigmoid = 1.0 / denominator;
        return sigmoid * 2.0 - 1.0;
    }

    private double getLastPlayedMultiplier(FireMixtape song) {
        long delta = song.getMillisSinceLastPlay();
        double k = -1.0 / ((double) mTimeSpread);
        double denominator = 1.0 + Math.pow(Math.E, k * ((double) (delta - mTimeOffset)));
        if (denominator == 0.0) return 0.0;
        double sigmoid = 1.0 / denominator;
        return sigmoid * 0.9 + 0.1;
    }

    private double getRandomMultiplier() {
        double random = Math.random();
        return random * RANDOM_SPREAD + 1 - (RANDOM_SPREAD / 2.0);
    }

    private double songChange(double songVal, double r) {
        double y = songVal;
        double med = 0.5 * (SONG_MIN + SONG_MAX);
        double spread = SONG_MAX - SONG_MIN;
        double offsetRatio = r < 0.0 ? 0.6 : 0.4;
        double multi = r < 0.0 ? SONG_NEG_MULTI : SONG_POS_MULTI;
        double offset = offsetRatio * (SONG_MAX - SONG_MIN) + SONG_MIN;
        double medVal = getBellValue(songVal, med, 1.0);
        double offVal = getBellValue(songVal, offset, spread);
        double fullVal = SONG_MED_MULTI * medVal +
                SONG_OFF_MULTI * offVal;
        y += multi * fullVal * r;

        if (y > SONG_MAX) {
            y = SONG_MAX;
        } else if (y < SONG_MIN) {
            y = SONG_MIN;
        }
        return y;
    }

    // returns value from bellish shaped function (e ^ (-1 * x ^ 2)
    public static double getBellValue(double val, double offset, double spread) {
        double k = 1.0 / spread;
        double x = k * (val - offset);
        return Math.pow(Math.E, -1.0 * x * x);
    }

    private void printCurrentValues() {
        List<FireMixtape> newList = new ArrayList<>(mSongList);
        Collections.sort(newList, new Comparator<FireMixtape>() {
            @Override
            public int compare(FireMixtape lhs, FireMixtape rhs) {
                if (lhs.calculatedValue < rhs.calculatedValue) {
                    return -1;
                } else if (lhs.calculatedValue == rhs.calculatedValue) {
                    return 0;
                } else {
                    return 1;
                }
            }
        });
        FireMixtape song = newList.get(newList.size() - 1);
        String message = song.title + " - " + song.calculatedValue;
        Log.e("ShuffleController", message);
    }

    private void printSongValues(FireMixtape song) {
        String message = song.title + " - " + getLastPlayedMultiplier(song) + " - " + song.multiplier;
        if (mGenreGraph.isGenre(song.actualGenre)) {
            message += " - " + mGenreGraph.getGenreLT(song.actualGenre);
            message += " - " + mGenreGraph.getGenreST(song.actualGenre);
        }
        message += " - " + getLastPlayedMultiplier(song);
        message += " = " + song.calculatedValue;

        Log.e("ShuffleController", message);
    }
}
