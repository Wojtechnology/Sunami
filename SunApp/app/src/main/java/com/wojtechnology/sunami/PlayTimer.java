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

import android.util.Log;
import java.util.Calendar;

/**
 * Created by wojtekswiderski on 15-06-15.
 */
public class PlayTimer {

    private boolean mIsStarted;
    private boolean mIsRunning;
    private long mRunningTime;
    private long mTotalTime;

    public PlayTimer() {
        mIsStarted = false;
        mIsRunning = false;
        mRunningTime = Calendar.getInstance().getTimeInMillis();
        mTotalTime = 0;
    }

    public int reset() {
        if (mIsRunning) {
            mTotalTime += Calendar.getInstance().getTimeInMillis() - mRunningTime;
        }
        int totalTime = (int) mTotalTime;
        mTotalTime = 0;
        mIsStarted = false;
        mIsRunning = false;
        Log.e("PlayTimer", "Total time: " + totalTime);
        return totalTime;
    }

    public void start() {
        if (mIsRunning) return;
        if (!mIsStarted) {
            mTotalTime = 0;
            mRunningTime = Calendar.getInstance().getTimeInMillis();
            mIsStarted = true;
        } else {
            mRunningTime = Calendar.getInstance().getTimeInMillis();
        }
        mIsRunning = true;
    }

    public void stop() {
        if (!mIsRunning || ! mIsStarted) return;
        mTotalTime += Calendar.getInstance().getTimeInMillis() - mRunningTime;
        mIsRunning = false;
    }
}
