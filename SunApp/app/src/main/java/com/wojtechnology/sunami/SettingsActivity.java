package com.wojtechnology.sunami;

import android.content.Intent;
import android.os.Bundle;
import android.preference.PreferenceActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
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
