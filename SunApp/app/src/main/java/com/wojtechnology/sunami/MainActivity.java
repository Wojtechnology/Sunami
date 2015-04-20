package com.wojtechnology.sunami;

import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.net.Uri;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends ActionBarActivity {

    private Toolbar toolbar;
    private RecyclerView recyclerView;
    private ListAdapter listAdapter;

    private ShuffleManager shuffleManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Setup toolbar at top of app
        toolbar = (Toolbar) findViewById(R.id.app_bar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayShowHomeEnabled(true);

        getSupportActionBar().setTitle(R.string.title_activity_main);

        // Setup navigation drawer from left
        NavigationDrawerFragment drawerFragment = (NavigationDrawerFragment)
                getSupportFragmentManager().findFragmentById(R.id.fragment_navigation_drawer);
        drawerFragment.setUp(R.id.fragment_navigation_drawer, (DrawerLayout) findViewById(R.id.drawer_layout), toolbar);

        // Get music data and send to shuffle manager
        List<FireMixtape> data = this.getFire();
        this.shuffleManager = new ShuffleManager(data);

        // Setup
        recyclerView = (RecyclerView) findViewById(R.id.drawer_list);
        listAdapter = new ListAdapter(this, data);
        recyclerView.setAdapter(listAdapter);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

    }

    protected List<FireMixtape> getFire() {

        List<FireMixtape> data = new ArrayList<>();

        //Some audio may be explicitly marked as not being music
        String selection = MediaStore.Audio.Media.IS_MUSIC + " != 0";

        String[] projection = {
                MediaStore.Audio.Media._ID,
                MediaStore.Audio.Media.ALBUM,
                MediaStore.Audio.Media.ALBUM_ID,
                MediaStore.Audio.Media.ARTIST,
                MediaStore.Audio.Media.ARTIST_ID,
                MediaStore.Audio.Media.BOOKMARK,
                MediaStore.Audio.Media.COMPOSER,
                MediaStore.Audio.Media.DURATION,
                MediaStore.Audio.Media.TITLE_KEY,
                MediaStore.Audio.Media.YEAR,
                MediaStore.Audio.Media.DATA,
                MediaStore.Audio.Media.DATE_ADDED,
                MediaStore.Audio.Media.DISPLAY_NAME,
                MediaStore.Audio.Media.MIME_TYPE,
                MediaStore.Audio.Media.SIZE,
                MediaStore.Audio.Media.TITLE
        };

        String[] genresProjection = {
                MediaStore.Audio.Genres._ID,
                MediaStore.Audio.Genres.NAME
        };

        Cursor cursor = getContentResolver().query(
                MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,
                projection,
                selection,
                null,
                null
        );

        while (cursor.moveToNext()) {

            // Create song object
            FireMixtape current = new FireMixtape(this);

            current._id = cursor.getString(0);
            current.album = cursor.getString(1);
            current.album_id = cursor.getString(2);
            current.artist = cursor.getString(3);
            current.artist_id = cursor.getString(4);
            current.bookmark = cursor.getString(5);
            current.composer = cursor.getString(6);
            current.duration = cursor.getString(7);
            current.title_key = cursor.getString(8);
            current.year = cursor.getString(9);
            current.data = cursor.getString(10);
            current.date_added = cursor.getString(11);
            current.display_name = cursor.getString(12);
            current.mime_type = cursor.getString(13);
            current.size = cursor.getString(14);
            current.title = cursor.getString(15);

            int musicID = Integer.parseInt(cursor.getString(0));

            Uri uri = MediaStore.Audio.Genres.getContentUriForAudioId("external", musicID);
            Cursor projectionCursor = getContentResolver().query(
                    uri,
                    genresProjection,
                    null,
                    null,
                    null
            );

            while(projectionCursor.moveToNext()){
                current.addGenre(
                        projectionCursor.getString(0),
                        projectionCursor.getString(1)
                );
            }

            current.printGenres();

            data.add(current);
        }

        return data;

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

        if (id == R.id.navigate) {
            startActivity(new Intent(this, SubActivity.class));
        }

        return super.onOptionsItemSelected(item);
    }
}
