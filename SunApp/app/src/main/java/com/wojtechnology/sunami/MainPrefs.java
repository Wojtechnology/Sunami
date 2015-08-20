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
