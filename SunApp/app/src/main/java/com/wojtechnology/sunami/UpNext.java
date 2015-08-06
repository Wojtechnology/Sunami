package com.wojtechnology.sunami;

import java.util.LinkedList;
import java.util.List;

/**
 * Created by wojtekswiderski on 15-06-10.
 */
public class UpNext {
    private LinkedList<FireMixtape> mUpNext;

    // Finish index for songs added from history or user added
    private int mUserIndexFinish;

    public static final int UP_NEXT_MIN = 1;

    public UpNext() {
        mUpNext = new LinkedList<>();
        mUserIndexFinish = 0;
    }

    public List<FireMixtape> data() {
        return mUpNext;
    }

    public boolean contains(FireMixtape song) {
        return mUpNext.contains(song);
    }

    public int size() {
        return mUpNext.size();
    }

    public void pushBack(FireMixtape song) {
        mUpNext.addLast(song);
        setUpNextFlag(song);
    }

    public void pushBackUser(FireMixtape song) {
        mUpNext.add(mUserIndexFinish++, song);
        setUpNextFlag(song);
    }

    public void pushFront(FireMixtape song) {
        mUpNext.addFirst(song);
        mUserIndexFinish++;
        setUpNextFlag(song);
    }

    public FireMixtape popFront() {
        if (size() <= 0) {
            return null;
        }
        if (mUserIndexFinish > 0) mUserIndexFinish--;
        FireMixtape song = mUpNext.removeFirst();
        setUpNextFlag(song);
        return song;
    }

    public boolean remove(FireMixtape song) {
        if (!mUpNext.contains(song)) return false;
        int index = mUpNext.indexOf(song);
        mUpNext.remove(song);
        boolean isAuto = isAuto(index);
        if (!isAuto) {
            if (mUserIndexFinish > 0) mUserIndexFinish--;
        }
        setUpNextFlag(song);
        return isAuto;
    }

    private void setUpNextFlag(FireMixtape song) {
        if (!contains(song)) {
            song.isUpNext = false;
        } else {
            song.isUpNext = true;
        }
    }

    private boolean isAuto(int index) {
        return index >= mUserIndexFinish;
    }
}
