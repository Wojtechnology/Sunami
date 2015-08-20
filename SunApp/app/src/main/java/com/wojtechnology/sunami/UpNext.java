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
