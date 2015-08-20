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

import android.content.Intent;
import android.os.Bundle;
import android.preference.PreferenceActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;

/**
 * Created by wojtekswiderski on 15-08-09.
 */
public class SettingsActivity extends PreferenceActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        addPreferencesFromResource(R.xml.settings);
        setContentView(R.layout.activity_settings_legacy);

        Toolbar actionbar = (Toolbar) findViewById(R.id.app_bar);
        actionbar.setTitle("Settings");
        actionbar.setNavigationIcon(getResources().getDrawable(R.drawable.abc_ic_ab_back_mtrl_am_alpha));
        actionbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        Intent serviceUpdateIntent = new Intent(getApplicationContext(), TheBrain.class);
        serviceUpdateIntent.setAction(TheBrain.UPDATE_SETTINGS);
        startService(serviceUpdateIntent);
    }
}
