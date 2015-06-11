package com.wojtechnology.sunami;

import java.util.LinkedList;
import java.util.List;

/**
 * Created by wojtekswiderski on 15-06-10.
 */
public class UpNext {
    private LinkedList<FireMixtape> mUpNext;

    public UpNext () {
        mUpNext = new LinkedList<>();
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

    public void pushFront (FireMixtape song) {
        mUpNext.addFirst(song);
    }

    public FireMixtape popFront () {
        if (size() <= 0) {
            return null;
        }
        return mUpNext.removeFirst();
    }
}
