package com.wojtechnology.sunami;

import java.util.LinkedList;
import java.util.List;

/**
 * Created by wojtekswiderski on 15-06-10.
 */
public class UpNext {
    private LinkedList<FireMixtape> mUpNext;
    private int mUserIndexStart;
    private int mUserIndexFinish;

    public static final int UP_NEXT_MIN = 1;

    public UpNext() {
        mUpNext = new LinkedList<>();
        mUserIndexStart = 0;
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

    public void pushFrontUser(FireMixtape song) {
        mUpNext.addFirst(song);
        mUserIndexStart++;
        mUserIndexFinish++;
        setUpNextFlag(song);
    }

    public void pushFront(FireMixtape song) {
        mUpNext.addFirst(song);
        if (mUserIndexFinish > 0) {
            mUserIndexStart++;
            mUserIndexFinish++;
        }
        setUpNextFlag(song);
    }

    public FireMixtape popFront() {
        if (size() <= 0) {
            return null;
        }
        if (!isAuto(0)) {
            if (mUserIndexFinish > 0) mUserIndexFinish--;
            if (mUserIndexStart > 0) mUserIndexStart--;
        }
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
            if (mUserIndexStart > 0) mUserIndexStart--;
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
        return index < mUserIndexStart || index >= mUserIndexFinish;
    }
}
