package com.wojtechnology.sunami;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.database.Cursor;
import android.graphics.Color;
import android.os.Bundle;
import android.os.IBinder;
import android.provider.MediaStore;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.net.Uri;
import android.view.View;
import android.widget.ProgressBar;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

// This is the beginning of an amazing summer
public class MainActivity extends ActionBarActivity {

    private Toolbar toolbar;
    private RecyclerView recyclerView;
    private ListAdapter listAdapter;
    private ProgressBar progressBar;
    private OuterLayout outerLayout;

    private TheBrain theBrain;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Setup toolbar at top of app
        toolbar = (Toolbar) findViewById(R.id.app_bar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setTitle(R.string.title_activity_main);
        getSupportActionBar().setElevation(25f);

        progressBar = (ProgressBar) findViewById(R.id.progressBar);
        progressBar.setVisibility(View.VISIBLE);
        outerLayout = (OuterLayout) findViewById(R.id.outer_layout);

        // Setup navigation drawer from left
        NavigationDrawerFragment drawerFragment = (NavigationDrawerFragment)
                getSupportFragmentManager().findFragmentById(R.id.fragment_navigation_drawer);
        drawerFragment.setUp(R.id.fragment_navigation_drawer, (DrawerLayout) findViewById(R.id.drawer_layout), toolbar, this);

        // Setup
        recyclerView = (RecyclerView) findViewById(R.id.drawer_list);

        Intent serviceIntent = new Intent(MainActivity.this, TheBrain.class);
        startService(serviceIntent);
        bindService(serviceIntent, mConnection, Context.BIND_AUTO_CREATE);
    }

    @Override
    protected void onDestroy() {
        unbindService(mConnection);
        super.onDestroy();
    }

    private ServiceConnection mConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            Log.e("MainActivity", "Service connected");
            TheBrain.LocalBinder binder = (TheBrain.LocalBinder) service;
            theBrain = binder.getServiceInstance();
            theBrain.registerClient(MainActivity.this);
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            Log.e("MainActivity", "Service disconnected");
        }
    };

    public void setRecyclerViewData(){
        listAdapter = new ListAdapter(this, theBrain.getDataByTitle(), this.theBrain);
        recyclerView.setAdapter(listAdapter);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    public void setProgressBar(boolean on){
        if(on){
            progressBar.setVisibility(View.VISIBLE);
        }else{
            progressBar.setVisibility(View.INVISIBLE);
        }
    }

    // The following functions call functions in OuterLayout.java
    public void playSong(FireMixtape song){
        outerLayout.playSong(song);
    }

    public void hideSong(){
        outerLayout.hideSong();
    }

    public void showSong() {
        outerLayout.showSong();
    }

    // The following functions call functions in TheBrain.java
    public void doneLoadingSongs(){
        theBrain.postInit();
    }

    public boolean isPlaying() {
        try {
            return theBrain.mediaPlayer.isPlaying();
        }catch (Exception e){
            return false;
        }
    }

    public boolean pausePlay(){
        if(theBrain.hasSong()){
            theBrain.mediaPlayer.pause();
            return true;
        }
        return false;
    }

    public boolean resumePlay(){
        if (!theBrain.hasSong()){
            theBrain.playNext();
            if(!theBrain.hasSong()){
                return false;
            }
        }else {
            theBrain.mediaPlayer.start();
        }
        return true;
    }

    public void nextPlay(){
        theBrain.playNext();
    }
}
