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
