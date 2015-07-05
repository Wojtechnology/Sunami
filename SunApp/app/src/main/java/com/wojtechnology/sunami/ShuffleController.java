package com.wojtechnology.sunami;

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

    private boolean mIsLoaded;

    public ShuffleController(TheBrain theBrain, GenreGraph genreGraph, SongManager songManager, int min) {
        mGenreGraph = genreGraph;
        mSongManager = songManager;
        mTheBrain = theBrain;
        mIsLoaded = false;
        UP_NEXT_MIN = min;

        for (double i = 0.0; i <= 1.0; i += 0.01) {
            Log.e("ShuffleController", "i: " + i + " and j: " + getPlayMultiplier(i, 0.1, 0.05));
        }
    }

    public void setLoadCompleted(){
        mIsLoaded = true;
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
        mGenreGraph.modifyGenre(playInstance);
        mSongManager.modifySong(playInstance);
    }

    // Determines whether the play counts as a skip or other
    private double getPlayMultiplier(double fractionPlayed, double offset, double spread) {
        double k = -1.0 / spread;
        double denominator = 1.0 + Math.pow(Math.E, k * (fractionPlayed - offset));
        double sigmoid = 1.0 / denominator;
        return sigmoid * 2.0 - 1.0;
    }

}
