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
    private List<FireMixtape> mSongList;
    private Queue<FireMixtape> mSongBuffer;

    private final int UP_NEXT_MIN;
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

    private boolean mSortingState;
    private boolean mIsLoaded;

    public ShuffleController(TheBrain theBrain, GenreGraph genreGraph, SongManager songManager, int min) {
        mGenreGraph = genreGraph;
        mSongManager = songManager;
        mTheBrain = theBrain;
        mIsLoaded = false;
        mSortingState = false;
        UP_NEXT_MIN = min;

        mSongBuffer = new LinkedList<>();
    }

    public void setLoadCompleted() {
        updateList();
        mIsLoaded = true;
    }

    public void updateList() {
        mSongList = new ArrayList<>(mSongManager.getFire());
        sortList();
    }

    public void sortList() {
        new SortListTask().execute();
    }

    private class SortListTask extends AsyncTask<Void, Integer, Void> {

        @Override
        protected Void doInBackground(Void... params) {
            if (mSongList == null && mSortingState) {
                return null;
            }
            mSortingState = true;
            Collections.sort(mSongList, new Comparator<FireMixtape>() {
                @Override
                public int compare(FireMixtape lhs, FireMixtape rhs) {
                    double l = calculateSongValue(lhs);
                    double r = calculateSongValue(rhs);
                    if (l == r) {
                        return 0;
                    } else if (l > r) {
                        return -1;
                    } else {
                        return 1;
                    }
                }
            });

            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);
            mSortingState = false;
        }
    }

    // Calculates the song value which ranks songs on fire level
    private double calculateSongValue(FireMixtape song) {
        double val = 1.0;
        if (mGenreGraph.isGenre(song.genre)) {
            val *= mGenreGraph.getGenreLT(song.genre);
            val *= mGenreGraph.getGenreST(song.genre);
        }
        val *= song.multiplier;
        return val;
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
        if (mSongBuffer.size() < BUFFER_SIZE) loadBuffer(upNext);
        while (upNext.size() < UP_NEXT_MIN && upNext.size() < mSongManager.size()) {
            FireMixtape song = mSongBuffer.poll();
            if (!upNext.contains(song)) {
                upNext.pushBack(song);
            }
        }
    }

    public void loadNext(UpNext upNext) {
        if (!mIsLoaded) {
            // If file not yet read, RNJesus
            randomLoadQueue(upNext);
        } else {
            calculatedLoadQueue(upNext);
        }
    }

    private boolean isContained(UpNext upNext, FireMixtape song) {
        return upNext.contains(song) ||
                mSongBuffer.contains(song) ||
                mTheBrain.getSongPlaying() == song;
    }

    private void loadBuffer(UpNext upNext) {
        int i = 0;
        while(mSongBuffer.size() < BUFFER_SIZE && i < mSongList.size()) {
            FireMixtape song = mSongList.get(i);
            if (!isContained(upNext, song)) {
                mSongBuffer.offer(song);
            }
            i++;
        }
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
            sortList();
            return;
        }
        double stDelta = shortTermGenreChange(genre, r);
        double ltDelta = longTermGenreChange(genre, r);
        mGenreGraph.modifyGenre(genre, stDelta, ltDelta);
        Log.i("ShuffleController", "Modified " + genre + " to st: " + stDelta + " and lt: " + ltDelta);
        sortList();
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

}
