package com.wojtechnology.sunami;

import android.app.Service;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.util.Log;

/**
 * Created by wojtekswiderski on 15-08-09.
 */
public class MainPrefs {
    public boolean isSmartEnabled;
    public boolean isSoundcloudEnabled;
    public boolean forceOverWifi;

    public long songCycle;

    private Service mService;

    public MainPrefs(Service service) {
        mService = service;
        updateValues();
    }

    public void updateValues() {
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(mService);
        isSmartEnabled = sharedPreferences.getBoolean("prefSmartShuffleEnabled", true);
        isSoundcloudEnabled = sharedPreferences.getBoolean("prefSoundcloudEnabled", true);
        forceOverWifi = sharedPreferences.getBoolean("prefOverWifi", false);
        songCycle = 60000 * Long.parseLong(sharedPreferences.getString("prefSongCycle", "120"));
    }

    private void printValues() {
        Log.e("MainPrefs", "isSmartEnabled: " + isSmartEnabled);
        Log.e("MainPrefs", "isSoundcloudEnabled: " + isSoundcloudEnabled);
        Log.e("MainPrefs", "forceOverWifi: " + forceOverWifi);
        Log.e("MainPrefs", "songCycle: " + songCycle);
    }
}
