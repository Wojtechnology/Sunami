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

    public UpNext () {
        mUpNext = new LinkedList<>();
        mUserIndexStart = 0;
        mUserIndexFinish = 0;
    }

    public List<FireMixtape> data () {
        return mUpNext;
    }

    public boolean contains(FireMixtape song) {
        return mUpNext.contains(song);
    }

    public int size() {
        return mUpNext.size();
    }

    public void pushBack (FireMixtape song) {
        mUpNext.addLast(song);
    }

    public void pushUser (FireMixtape song) {
        mUpNext.add(mUserIndexFinish++, song);
    }

    public void pushFront (FireMixtape song) {
        mUpNext.addFirst(song);
        if (mUserIndexFinish > 0) {
            mUserIndexStart++;
            mUserIndexFinish++;
        }
    }

    public FireMixtape popFront () {
        if (size() <= 0) {
            return null;
        }
        if (mUserIndexFinish > 0) mUserIndexFinish--;
        if (mUserIndexStart > 0) mUserIndexStart--;
        return mUpNext.removeFirst();
    }
}