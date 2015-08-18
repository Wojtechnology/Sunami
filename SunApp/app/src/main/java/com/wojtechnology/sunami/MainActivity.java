package com.wojtechnology.sunami;

import android.app.SearchManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.graphics.Bitmap;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ProgressBar;
import android.support.v7.widget.SearchView;
import java.util.ArrayList;

// This is the beginning of an amazing summer
public class MainActivity extends ActionBarActivity implements SearchView.OnQueryTextListener {

    public static int STATE_SONGS = 0;
    public static int STATE_ARTISTS = 1;

    private Toolbar mToolbar;
    private RecyclerView mRecyclerView;
    private ProgressBar mProgressBar;
    private OuterLayout mOuterLayout;
    private Handler mHandler;
    private Soundcloud mSoundcloud;
    private boolean mSongPlayingChecked;
    private int mState;

    private SearchView mSearchView;

    public TheBrain mTheBrain;
    public NavigationDrawerFragment mDrawerFragment;
    public ListAdapter mListAdapter;
    public FastScroller mFastScroller;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mSongPlayingChecked = false;
        mSoundcloud = new Soundcloud(this);

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

        mState = STATE_SONGS;
        mDrawerFragment.updateChoices(mState);

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
            mOuterLayout.showSong();
        }
    }

    private ServiceConnection mConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
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
            serviceFinished();
        }
    };

    public void serviceFinished() {
        mTheBrain = null;
    }

    public void setState(int state) {
        if (mRecyclerView == null) { return; }
        if (state != mState) {
            mState = state;
            resetRecyclerView();
            new GetDataTask().execute();
            if (mDrawerFragment != null) {
                mDrawerFragment.updateChoices(state);
            }
        }
    }

    public void refreshRecyclerViewData() {
        new GetDataTask().execute();
    }

    public int getState() {
        return mState;
    }

    public void setRecyclerViewData(){
        setProgressBar(false);
        mRecyclerView = (RecyclerView) findViewById(R.id.drawer_list);
        mListAdapter = new ListAdapter(this, mTheBrain.getDataByTitle(), mTheBrain, mSoundcloud);
        mRecyclerView.setAdapter(mListAdapter);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        mFastScroller = (FastScroller) findViewById(R.id.fast_scroller);
        mFastScroller.setRecyclerView(mRecyclerView);
        mDrawerFragment.setUpRecyclerView(mTheBrain);
    }

    private void resetRecyclerView() {
        mListAdapter.setData(new ArrayList<FireMixtape>(0));
        mListAdapter.flushVisibleData();
        setProgressBar(true);
    }

    @Override
    public boolean onQueryTextSubmit(String s) {
        mListAdapter.setFilterSubmit(s);
        if (mSearchView != null) {
            mSearchView.clearFocus();
        }
        return true;
    }

    @Override
    public boolean onQueryTextChange(String s) {
        mListAdapter.setFilter(s, true);
        return true;
    }

    public boolean isSoundcloudEnabled() {
        return mTheBrain.isSoundcloudEnabled();
    }

    class GetDataTask extends AsyncTask<Boolean, Integer, Void> {
        @Override
        protected Void doInBackground(Boolean... params) {
            if (mState == STATE_SONGS) {
                mListAdapter.setData(mTheBrain.getDataByTitle());
            } else {
                mListAdapter.setData(mTheBrain.getDataByArtist());
            }
            if (!mListAdapter.mIsSearching) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        mListAdapter.flushVisibleData();
                        setProgressBar(false);
                    }
                });
            }
            return null;
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);

        // Associate searchable configuration with the SearchView
        SearchManager searchManager =
                (SearchManager) getSystemService(Context.SEARCH_SERVICE);
        mSearchView = (SearchView) menu.findItem(R.id.search).getActionView();
        mSearchView.setSearchableInfo(
                searchManager.getSearchableInfo(getComponentName()));
        mSearchView.setOnQueryTextListener(this);
        final SearchView sv = mSearchView;
        mSearchView.setOnQueryTextFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (v == sv && hasFocus) {
                    hideSong();
                    mDrawerFragment.closeDrawer();
                }
                else if (v == sv && !hasFocus) {
                    showSong();
                }
            }
        });

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.search) {
            return true;
        } else if (id == R.id.action_settings) {
            Intent intent = new Intent(this, SettingsActivity.class);
            startActivity(intent);
        }

        return super.onOptionsItemSelected(item);
    }

    // Needed if I want to receive intent from search
    // Currently using OnQueryTextListenerInstead
    /*@Override
    protected void onNewIntent(Intent intent) {
        handleIntent(intent);
    }

    private void handleIntent(Intent intent) {

        if (Intent.ACTION_SEARCH.equals(intent.getAction())) {
            String query = intent.getStringExtra(SearchManager.QUERY);
            Log.e("MainActivity", query);
        }
    }*/

    public void setArtwork(Bitmap art) {
        mOuterLayout.setArtwork(art);
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
        mOuterLayout.setProgress(mTheBrain.mMediaPlayer.getCurrentPosition());
    }

    public void updateMediaPlayerProgress(int progress) {
        if (mTheBrain == null) return;
        if (mTheBrain.mMediaPlayer != null && mTheBrain.mPlaying != null) {
            mTheBrain.mMediaPlayer.seekTo(progress);
            mTheBrain.setProgress(progress, mTheBrain.isPlaying());
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

    public boolean drawerOpen() {
        return mDrawerFragment.isOpen();
    }
}
