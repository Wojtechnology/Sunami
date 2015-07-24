package com.wojtechnology.sunami;


import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.support.v7.widget.Toolbar;
import android.widget.ImageView;
import android.widget.LinearLayout;

import java.util.ArrayList;
import java.util.List;


/**
 * A simple {@link Fragment} subclass.
 */
public class NavigationDrawerFragment extends Fragment {

    private RecyclerView recyclerView;
    public static final String PREF_FILE_NAME = "testpref";
    public static final String KEY_USER_LEARNED_DRAWER = "user_learned_drawer";
    private ActionBarDrawerToggle mDrawerToggle;
    private DrawerLayout mDrawerLayout;
    private UpNextAdapter mListAdapter;
    private RecyclerView mRecyclerView;
    private boolean mUserLearnedDrawer;
    private boolean mFromSavedInstanceState;
    private View containerView;
    private boolean mOpened;
    private Context mContext;

    private LinearLayout mSongButton;
    private LinearLayout mArtistButton;

    public NavigationDrawerFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        SharedPreferences sharedPref = getActivity().getSharedPreferences(
                getString(R.string.preference_file_key), Context.MODE_PRIVATE);
        mUserLearnedDrawer = sharedPref.getBoolean(
                getString(R.string.user_learn_drawer), false);
        if(savedInstanceState != null){
            mFromSavedInstanceState = true;
        }
        mOpened = false;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_navigation_drawer, container, false);
    }

    public void setUp(int fragmentID
            ,DrawerLayout drawerLayout
            ,final Toolbar toolbar
            ,final Context context) {
        containerView = getActivity().findViewById(fragmentID);
        mDrawerLayout = drawerLayout;
        mContext = context;
        mDrawerToggle = new ActionBarDrawerToggle(getActivity(), drawerLayout, toolbar, R.string.drawer_open, R.string.drawer_close) {

            @Override
            public void onDrawerOpened(View drawerView) {
                super.onDrawerOpened(drawerView);
                if(!mUserLearnedDrawer){
                    mUserLearnedDrawer = true;
                    SharedPreferences.Editor editor = getActivity().getSharedPreferences(
                            getString(R.string.preference_file_key), Context.MODE_PRIVATE).edit();
                    editor.putBoolean(getString(R.string.user_learn_drawer), mUserLearnedDrawer);
                    editor.commit();
                }
                toolbar.setTitle(R.string.title_side_fragment);
                //getActivity().invalidateOptionsMenu();
            }

            @Override
            public void onDrawerClosed(View drawerView) {
                super.onDrawerClosed(drawerView);
                toolbar.setTitle(R.string.title_activity_main);
                //getActivity().invalidateOptionsMenu();
            }

            @Override
            public void onDrawerSlide(View drawerView, float slideOffset) {
                super.onDrawerSlide(drawerView, slideOffset);
                if(slideOffset > 0.0f){
                    if(!mOpened) {
                        ((MainActivity) context).hideSong();
                        mOpened = true;
                    }
                }else{
                    ((MainActivity) context).showSong();
                    mOpened = false;
                }
            }
        };
        if(!mUserLearnedDrawer && !mFromSavedInstanceState){
            mDrawerLayout.openDrawer(containerView);
            mOpened = true;
        }else{
            mOpened = false;
        }

        mDrawerLayout.setDrawerListener(mDrawerToggle);
        mDrawerLayout.post(new Runnable() {
            @Override
            public void run() {
                mDrawerToggle.syncState();
            }
        });

        mSongButton = (LinearLayout) getActivity().findViewById(R.id.by_song_button);
        mArtistButton = (LinearLayout) getActivity().findViewById(R.id.by_artist_button);

        mSongButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mDrawerLayout.closeDrawers();
                ((MainActivity) mContext).setState(MainActivity.STATE_SONGS);
            }
        });

        mArtistButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mDrawerLayout.closeDrawers();
                ((MainActivity) mContext).setState(MainActivity.STATE_ARTISTS);
            }
        });
    }

    public void closeDrawer() {
        mDrawerLayout.closeDrawers();
    }

    public void updateChoices(int state) {
        ImageView songIcon = (ImageView) getActivity().findViewById(R.id.by_song_icon);
        ImageView artistIcon = (ImageView) getActivity().findViewById(R.id.by_artist_icon);
        Drawable songD;
        Drawable artistD;
        if (state == MainActivity.STATE_SONGS) {
            songD = getActivity().getResources().getDrawable(R.drawable.ic_my_library_music_white_36dp);
            songD.setColorFilter(0xffffab40, PorterDuff.Mode.MULTIPLY);
            artistD = getActivity().getResources().getDrawable(R.drawable.ic_recent_actors_grey600_36dp);
        } else {
            songD = getActivity().getResources().getDrawable(R.drawable.ic_my_library_music_grey600_36dp);
            artistD = getActivity().getResources().getDrawable(R.drawable.ic_recent_actors_white_36dp);
            artistD.setColorFilter(0xffffab40, PorterDuff.Mode.MULTIPLY);
        }
        songIcon.setImageDrawable(songD);
        artistIcon.setImageDrawable(artistD);
    }

    public void setUpRecyclerView (TheBrain theBrain) {
        mRecyclerView = (RecyclerView) getActivity().findViewById(R.id.up_next_drawer_list);
        mListAdapter = new UpNextAdapter(getActivity(), theBrain.getUpNext(), theBrain);
        mRecyclerView.setAdapter(mListAdapter);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
    }

    public void updateRecyclerView () {
        mListAdapter.notifyDataSetChanged();
    }

    public static void saveToPreferences(Context context, String preferenceName, String preferenceValue) {
        SharedPreferences sharedPreferences = context.getSharedPreferences(PREF_FILE_NAME, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString(preferenceName, preferenceValue);
        editor.apply();
    }

    public static String readFromPreferences(Context context, String preferenceName, String defaultValue) {
        SharedPreferences sharedPreferences = context.getSharedPreferences(PREF_FILE_NAME, Context.MODE_PRIVATE);
        return sharedPreferences.getString(preferenceName, defaultValue);
    }

    public boolean isOpen() {
        return mOpened;
    }
}
