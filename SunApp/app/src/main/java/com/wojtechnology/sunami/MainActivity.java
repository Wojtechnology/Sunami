package com.wojtechnology.sunami;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.Handler;
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
    private Handler mHandler;
    private boolean mSongPlayingChecked;

    public TheBrain mTheBrain;
    public NavigationDrawerFragment mDrawerFragment;

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
        mHandler = new Handler();

        // Setup navigation drawer from left
        mDrawerFragment = (NavigationDrawerFragment)
                getSupportFragmentManager().findFragmentById(R.id.fragment_navigation_drawer);
        mDrawerFragment.setUp(R.id.fragment_navigation_drawer
                ,(DrawerLayout) findViewById(R.id.drawer_layout)
                ,mToolbar
                ,this);

        Intent serviceIntent = new Intent(MainActivity.this, TheBrain.class);
        startService(serviceIntent);
        bindService(serviceIntent, mConnection, Context.BIND_AUTO_CREATE);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unbindService(mConnection);
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
            mOuterLayout.updatePlayIcon();

            MainActivity.this.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    if (mTheBrain != null) {
                        if (mTheBrain.mMediaPlayer != null && mTheBrain.mMediaPlayer.isPlaying()) {
                            mOuterLayout.setProgress(mTheBrain.mMediaPlayer.getCurrentPosition());
                        }
                        mHandler.postDelayed(this, 1000);
                    }
                }
            });
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            Log.e("MainActivity", "Service disconnected");
            serviceFinished();
        }
    };

    public void serviceFinished() {
        mTheBrain = null;
    }

    public void setRecyclerViewData(){
        mRecyclerView = (RecyclerView) findViewById(R.id.drawer_list);
        mListAdapter = new ListAdapter(this, mTheBrain.getDataByTitle(), this.mTheBrain);
        mRecyclerView.setAdapter(mListAdapter);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        mDrawerFragment.setUpRecyclerView(mTheBrain);
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
        mOuterLayout.setProgress(mTheBrain.mMediaPlayer.getCurrentPosition());
        mOuterLayout.playSong(song);
    }

    public void updateMediaPlayerProgress(int progress) {
        if (mTheBrain == null) return;
        if (mTheBrain.mMediaPlayer != null) {
            mTheBrain.mMediaPlayer.seekTo(progress);
        }
    }

    public void updateSongView() {
        mOuterLayout.updatePlayIcon();
    }

    public void hideSong(){
        mOuterLayout.hideSong();
    }

    public void showSong() {
        mOuterLayout.showSong();
    }
}
