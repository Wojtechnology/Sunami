package com.wojtechnology.sunami;

import android.os.AsyncTask;
import android.support.v4.media.session.PlaybackStateCompat;
import android.util.Log;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;

/**
 * Created by wojtekswiderski on 15-07-02.
 */
public class ShuffleController {

    private GenreGraph mGenreGraph;
    private SongManager mSongManager;
    private TheBrain mTheBrain;
    private UpNext mUpNext;
    private List<FireMixtape> mSongList;

    private final int BUFFER_SIZE = 4;
    private final double SONG_DURATION_OFFSET = 0.1;
    private final double SONG_DURATION_SPREAD = 0.05;
    private final double SHORT_GENRE_MIN = 0.5;
    private final double SHORT_GENRE_MAX = 2.0;
    private final double SHORT_GENRE_MED_MULTI = 0.1;
    private final double SHORT_GENRE_OFF_MULTI = 1.0;
    private final double SHORT_GENRE_POS_MULTI = 0.25;
    private final double SHORT_GENRE_NEG_MULTI = 0.25;
    private final double LONG_GENRE_MIN = 0.5;
    private final double LONG_GENRE_MAX = 2.0;
    private final double LONG_GENRE_MED_MULTI = 0.1;
    private final double LONG_GENRE_OFF_MULTI = 1.0;
    private final double LONG_GENRE_POS_MULTI = 0.05;
    private final double LONG_GENRE_NEG_MULTI = 0.05;
    private final double SONG_MIN = 0.5;
    private final double SONG_MAX = 2.0;
    private final double SONG_MED_MULTI = 0.1;
    private final double SONG_OFF_MULTI = 1.0;
    private final double SONG_POS_MULTI = 0.4;
    private final double SONG_NEG_MULTI = 0.4;
    private final long TIME_SPREAD = 1800000;
    private final long TIME_OFFSET = 3600000;
    private final double RANDOM_SPREAD = 0.2;

    private boolean mIsLoaded;

    private class SetSongValuesTask extends AsyncTask<Void, Integer, Void> {
        @Override
        protected Void doInBackground(Void... params) {
            return null;
        }
    }

    public ShuffleController(TheBrain theBrain, GenreGraph genreGraph, SongManager songManager, UpNext upNext) {
        mGenreGraph = genreGraph;
        mSongManager = songManager;
        mTheBrain = theBrain;
        mIsLoaded = false;
        mUpNext = upNext;
    }

    public void setLoadCompleted() {
        updateList();
        mIsLoaded = true;
    }

    public void updateList() {
        mSongList = new ArrayList<>(mSongManager.getFire());
        setSongValues();
    }

    private void setSongValues() {
        for (int i = 0; i < mSongList.size(); i++) {
            mSongList.get(i).calculatedValue = calculateSongValue(mSongList.get(i));
        }
    }

    // Calculates the song value which ranks songs on fire level
    private double calculateSongValue(FireMixtape song) {
        double val = 1.0;
        if (mGenreGraph.isGenre(song.actualGenre)) {
            val *= mGenreGraph.getGenreLT(song.actualGenre);
            val *= mGenreGraph.getGenreST(song.actualGenre);
        }
        val *= song.multiplier;
        val *= getLastPlayedMultiplier(song);
        val *= getRandomMultiplier();
        return val;
    }

    private void randomLoadQueue() {
        while (mUpNext.size() < mUpNext.UP_NEXT_MIN && mUpNext.size() < mSongManager.size()) {
            int random = (int) (Math.random() * mSongManager.size());
            FireMixtape song = mSongManager.getSongAtIndex(random);
            if (!mUpNext.contains(song)) {
                mUpNext.pushBack(song);
            }
        }
    }

    private void calculatedLoadQueue() {
        int i = 0;
        while (mUpNext.size() < mUpNext.UP_NEXT_MIN && i < mSongList.size() && mUpNext.size() < mSongList.size()) {
            FireMixtape song = mSongList.get(i);
            if (!isContained(song)) {
                mUpNext.pushBack(song);
                printSongValues(song);
            }
            i++;
        }
    }

    public void loadNext() {
        if (!mIsLoaded) {
            // If file not yet read, RNJesus
            randomLoadQueue();
        } else {
            calculatedLoadQueue();
        }
    }

    private boolean isContained(FireMixtape song) {
        return mUpNext.contains(song) ||
                mTheBrain.getSongPlaying() == song;
    }

    // Pass in the play instance and modify genre and song values based on play duration
    public void addPlayInstance(PlayInstance playInstance) {
        if (!mIsLoaded) return;
        double r = getPlayMultiplier(playInstance.getFractionPlayed(), SONG_DURATION_OFFSET, SONG_DURATION_SPREAD);
        double songDelta = songChange(playInstance.getMulti(), r);
        playInstance.setMulti(songDelta);
        Log.i("ShuffleController", "Modified song value to " + songDelta);
        String genre = playInstance.getGenre();
        if (!mGenreGraph.isGenre(genre)) {
            // May want to find the most suitable genre for song here
            return;
        }
        double stDelta = shortTermGenreChange(genre, r);
        double ltDelta = longTermGenreChange(genre, r);
        mGenreGraph.modifyGenre(genre, stDelta, ltDelta);
        Log.i("ShuffleController", "Modified " + genre + " to st: " + stDelta + " and lt: " + ltDelta);
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
        double k = -1.0 / ((double) TIME_SPREAD);
        double denominator = 1.0 + Math.pow(Math.E, k * ((double) (delta - TIME_OFFSET)));
        if (denominator == 0.0) return 0.0;
        double sigmoid = 1.0 / denominator;
        return sigmoid * 0.75 + 0.25;
    }

    private double getRandomMultiplier() {
        double random = Math.random();
        return random * RANDOM_SPREAD + 1 - (RANDOM_SPREAD / 2.0);
    }

    private double shortTermGenreChange(String genre, double r) {
        double genreVal = mGenreGraph.getGenreST(genre);
        double y = genreVal;
        double med = 0.5 * (SHORT_GENRE_MIN + SHORT_GENRE_MAX);
        double spread = 0.5 * (SHORT_GENRE_MAX - SHORT_GENRE_MIN);
        double offsetRatio = r < 0.0 ? 0.75 : 0.25;
        double multi = r < 0.0 ? SHORT_GENRE_NEG_MULTI : SHORT_GENRE_POS_MULTI;
        double offset = offsetRatio * (SHORT_GENRE_MAX - SHORT_GENRE_MIN) + SHORT_GENRE_MIN;
        double medVal = getBellValue(genreVal, med, 1.0);
        double offVal = getBellValue(genreVal, offset, spread);
        double fullVal = SHORT_GENRE_MED_MULTI * medVal +
                SHORT_GENRE_OFF_MULTI * offVal;
        y += multi * fullVal * r;

        if (y > SHORT_GENRE_MAX) {
            y = SHORT_GENRE_MAX;
        } else if (y < SHORT_GENRE_MIN) {
            y = SHORT_GENRE_MIN;
        }
        return y;
    }

    private double longTermGenreChange(String genre, double r) {
        double genreVal = mGenreGraph.getGenreLT(genre);
        double y = genreVal;
        double med = 0.5 * (LONG_GENRE_MIN + LONG_GENRE_MAX);
        double spread = 0.5 * (LONG_GENRE_MAX - LONG_GENRE_MIN);
        double offsetRatio = r < 0.0 ? 0.75 : 0.25;
        double multi = r < 0.0 ? LONG_GENRE_NEG_MULTI : LONG_GENRE_POS_MULTI;
        double offset = offsetRatio * (LONG_GENRE_MAX - LONG_GENRE_MIN) + LONG_GENRE_MIN;
        double medVal = getBellValue(genreVal, med, 1.0);
        double offVal = getBellValue(genreVal, offset, spread);
        double fullVal = LONG_GENRE_MED_MULTI * medVal +
                LONG_GENRE_OFF_MULTI * offVal;
        y += multi * fullVal * r;

        if (y > LONG_GENRE_MAX) {
            y = LONG_GENRE_MAX;
        } else if (y < LONG_GENRE_MIN) {
            y = LONG_GENRE_MIN;
        }
        return y;
    }

    private double songChange(double songVal, double r) {
        double y = songVal;
        double med = 0.5 * (SONG_MIN + SONG_MAX);
        double spread = 0.5 * (SONG_MAX - SONG_MIN);
        double offsetRatio = r < 0.0 ? 0.75 : 0.25;
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
    private double getBellValue(double val, double offset, double spread) {
        double k = 1.0 / spread;
        double x = k * (val - offset);
        return Math.pow(Math.E, -1.0 * x * x);
    }

    private void printCurrentValues() {
        for (int i = 0; i < mSongList.size(); i++) {
            printSongValues(mSongList.get(i));
        }
    }

    private void printSongValues(FireMixtape song) {
        String message = song.title + " - " + song.getMillisSinceLastPlay() + " - " + song.multiplier;
        if (mGenreGraph.isGenre(song.actualGenre)) {
            message += " - " + mGenreGraph.getGenreLT(song.actualGenre);
            message += " - " + mGenreGraph.getGenreST(song.actualGenre);
        }
        message += " - " + getLastPlayedMultiplier(song);
        message += " = " + calculateSongValue(song);

        Log.e("ShuffleController", message);
    }
}
