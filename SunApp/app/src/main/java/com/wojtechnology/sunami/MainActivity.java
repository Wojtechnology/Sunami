package com.wojtechnology.sunami;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ProgressBar;

// This is the beginning of an amazing summer
public class MainActivity extends ActionBarActivity {

    private Toolbar mToolbar;
    private RecyclerView mRecyclerView;
    private ListAdapter mListAdapter;
    private ProgressBar mProgressBar;
    private OuterLayout mOuterLayout;
    private boolean mSongPlayingChecked;

    private TheBrain mTheBrain;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mSongPlayingChecked = false;

        // Setup mToolbar at top of app
        mToolbar = (Toolbar) findViewById(R.id.app_bar);
        setSupportActionBar(mToolbar);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setTitle(R.string.title_activity_main);
        getSupportActionBar().setElevation(25f);

        mProgressBar = (ProgressBar) findViewById(R.id.progressBar);
        mProgressBar.setVisibility(View.VISIBLE);
        mOuterLayout = (OuterLayout) findViewById(R.id.outer_layout);

        // Setup navigation drawer from left
        NavigationDrawerFragment drawerFragment = (NavigationDrawerFragment)
                getSupportFragmentManager().findFragmentById(R.id.fragment_navigation_drawer);
        drawerFragment.setUp(R.id.fragment_navigation_drawer, (DrawerLayout) findViewById(R.id.drawer_layout), mToolbar, this);

        // Setup
        mRecyclerView = (RecyclerView) findViewById(R.id.drawer_list);

        Intent serviceIntent = new Intent(MainActivity.this, TheBrain.class);
        startService(serviceIntent);
        bindService(serviceIntent, mConnection, Context.BIND_AUTO_CREATE);
    }

    @Override
    protected void onDestroy() {
        unbindService(mConnection);
        super.onDestroy();
    }

    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        super.onWindowFocusChanged(hasFocus);
        if (!mSongPlayingChecked) {
            mSongPlayingChecked = true;
            mOuterLayout.playSong(mTheBrain.mPlaying);
        }
    }

    private ServiceConnection mConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            Log.e("MainActivity", "Service connected");
            TheBrain.LocalBinder binder = (TheBrain.LocalBinder) service;
            mTheBrain = binder.getServiceInstance();
            mTheBrain.registerClient(MainActivity.this);
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            Log.e("MainActivity", "Service disconnected");
        }
    };

    public void setRecyclerViewData(){
        mListAdapter = new ListAdapter(this, mTheBrain.getDataByTitle(), this.mTheBrain);
        mRecyclerView.setAdapter(mListAdapter);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(this));
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
            mProgressBar.setVisibility(View.VISIBLE);
        }else{
            mProgressBar.setVisibility(View.INVISIBLE);
        }
    }

    // The following functions call functions in OuterLayout.java
    public void playSong(FireMixtape song){
        mOuterLayout.playSong(song);
    }

    public void hideSong(){
        mOuterLayout.hideSong();
    }

    public void showSong() {
        mOuterLayout.showSong();
    }

    // The following functions call functions in TheBrain.java
    public void doneLoadingSongs(){
        mTheBrain.postInit();
    }

    public boolean isPlaying() {
        try {
            return mTheBrain.mMediaPlayer.isPlaying();
        } catch (Exception e) {
            return false;
        }
    }

    public boolean pausePlay(){
        if(mTheBrain.hasSong()){
            mTheBrain.mMediaPlayer.pause();
            return true;
        }
        return false;
    }

    public boolean resumePlay(){
        if (!mTheBrain.hasSong()){
            mTheBrain.playNext();
            if(!mTheBrain.hasSong()){
                return false;
            }
        }else {
            mTheBrain.mMediaPlayer.start();
        }
        return true;
    }

    public void nextPlay(){
        mTheBrain.playNext();
    }
}
