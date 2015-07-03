package com.wojtechnology.sunami;

/**
 * Created by wojtekswiderski on 15-07-02.
 */
public class ShuffleController {

    private GenreGraph mGenreGraph;
    private SongManager mSongManager;
    private TheBrain mTheBrain;

    private final int UP_NEXT_MIN;

    private boolean mIsLoaded;

    public ShuffleController(TheBrain theBrain, GenreGraph genreGraph, SongManager songManager, int min) {
        mGenreGraph = genreGraph;
        mSongManager = songManager;
        mTheBrain = theBrain;
        mIsLoaded = false;
        UP_NEXT_MIN = min;
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

    public void loadNext(UpNext upNext) {
        randomLoadQueue(upNext);
    }

}
