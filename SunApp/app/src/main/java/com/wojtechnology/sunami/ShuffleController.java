package com.wojtechnology.sunami;

import android.support.v4.media.session.PlaybackStateCompat;
import android.util.Log;

/**
 * Created by wojtekswiderski on 15-07-02.
 */
public class ShuffleController {

    private GenreGraph mGenreGraph;
    private SongManager mSongManager;
    private TheBrain mTheBrain;

    private final int UP_NEXT_MIN;
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
    private final double SONG_POS_MULTI = 0.25;
    private final double SONG_NEG_MULTI = 0.25;
    private boolean mIsLoaded;

    public ShuffleController(TheBrain theBrain, GenreGraph genreGraph, SongManager songManager, int min) {
        mGenreGraph = genreGraph;
        mSongManager = songManager;
        mTheBrain = theBrain;
        mIsLoaded = false;
        UP_NEXT_MIN = min;

        /*for (double i = 0.0; i <= 1.0; i += 0.01) {
            Log.e("ShuffleController", "i: " + i + " and j: " + getPlayMultiplier(i, 0.1, 0.05));
        }*/
    }

    public void setLoadCompleted() {
        mIsLoaded = true;
        for (int i = 0; i < 25; i++){
            double genreVal = mGenreGraph.getGenreST("rock");
            double newVal = shortTermGenreChange("rock", -0.6);
            double delta = newVal - genreVal;
            mGenreGraph.modifyGenre("rock", newVal);
            Log.e("ShuffleController", "Value = " + newVal + " with delta " + delta);
        }
        for (int i = 0; i < 25; i++){
            double genreVal = mGenreGraph.getGenreST("rock");
            double newVal = shortTermGenreChange("rock", 0.75);
            double delta = newVal - genreVal;
            mGenreGraph.modifyGenre("rock", newVal);
            Log.e("ShuffleController", "Value = " + newVal + " with delta " + delta);
        }
    }

    private void randomLoadQueue(UpNext upNext) {
        while (upNext.size() < UP_NEXT_MIN && upNext.size() < mSongManager.size()) {
            int random = (int) (Math.random() * mSongManager.size());
            FireMixtape song = mSongManager.getSongAtIndex(random);
            if (!upNext.contains(song)) {
                upNext.pushBack(song);
            }
        }
    }

    private void calculatedLoadQueue(UpNext upNext) {
        randomLoadQueue(upNext);
    }

    public void loadNext(UpNext upNext) {
        if (!mIsLoaded) {
            // If file not yet read, RNJesus
            randomLoadQueue(upNext);
        } else {
            calculatedLoadQueue(upNext);
        }
    }

    // Pass in the play instance and modify genre and song values based on play duration
    public void addPlayInstance(PlayInstance playInstance) {
        double r = getPlayMultiplier(playInstance.getFractionPlayed(), SONG_DURATION_OFFSET, SONG_DURATION_SPREAD);
        String genre = playInstance.getGenre();
        if (!mGenreGraph.isGenre(genre)) {
            // May want to find the most suitable genre for song here
            return;
        }
        shortTermGenreChange(genre, r);
        /*mGenreGraph.modifyGenre(playInstance);
        mSongManager.modifySong(playInstance);*/
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
        double genreVal = mGenreGraph.getGenreST(genre);
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

    // returns value from bellish shaped function (e ^ (-1 * x ^ 2)
    private double getBellValue(double val, double offset, double spread) {
        double k = 1.0 / spread;
        double x = k * (val - offset);
        return Math.pow(Math.E, -1.0 * x * x);
    }

}
