package com.wojtechnology.sunami;

import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.provider.MediaStore;
import android.util.Log;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by wojtekswiderski on 15-05-15.
 */
public class SongManager {

    private Context context;
    private List<FireMixtape> fireMixtapes;

    public SongManager(Context context) {
        this.context = context;
        fireMixtapes = new ArrayList<>();
        initSongs();
    }

    public void sortByTitle() {
        Collections.sort(fireMixtapes, new Comparator<FireMixtape>() {
            @Override
            public int compare(FireMixtape lhs, FireMixtape rhs) {
                String lTitle = lhs.title.toLowerCase();
                String rTitle = rhs.title.toLowerCase();

                if (lTitle.length() > 4) {
                    if (lTitle.substring(0, 4).equals("the ")) {
                        lTitle = lTitle.substring(4);
                    }
                }
                if (rTitle.length() > 4) {
                    if (rTitle.substring(0, 4).equals("the ")) {
                        rTitle = rTitle.substring(4);
                    }
                }

                return lTitle.compareTo(rTitle);
            }
        });

        Map<Character, Integer> letters = new HashMap<>();

        int i = 0;
        while(firstLetter(fireMixtapes.get(i).title) != 'A'){
            i++;
        }
        int sum = i;
        int j = i;

        for(char headerTitle = 'A'; headerTitle <= 'Z' && i < fireMixtapes.size(); i++){
            if(firstLetter(fireMixtapes.get(i).title) != headerTitle){
                letters.put(headerTitle, i - j);
                headerTitle++;
                j = i;
                i--;
            }
        }

        for(char headerTitle = 'A'; headerTitle <= 'Z'
                && letters.containsKey(headerTitle); headerTitle++){
            if(letters.get(headerTitle) != 0) {
                FireMixtape mixtape = new FireMixtape(context);
                mixtape.title = Character.toString(headerTitle);
                mixtape.genre = "__header__";
                fireMixtapes.add(sum, mixtape);
                sum++;
            }
            sum += letters.get(headerTitle);
        }
    }

    private char firstLetter(String word){
        word = word.toUpperCase();
        if(word.length() > 4) {
            if (word.substring(0, 4).equals("THE ")) {
                word = word.substring(4);
            }
        }
        return word.charAt(0);
    }

    public List<FireMixtape> getFire() {
        return fireMixtapes;
    }

    public FireMixtape getSong(String _id){
        for(int i = 0; i < fireMixtapes.size(); i++){
            if(fireMixtapes.get(i)._id == _id){
                return fireMixtapes.get(i);
            }
        }
        return null;
    }

    private void initSongs(){
        long startTime = Calendar.getInstance().getTimeInMillis();

        //Some audio may be explicitly marked as not being music
        String selection = MediaStore.Audio.Media.IS_MUSIC + " != 0";

        String[] projection = {
                MediaStore.Audio.Media._ID,
                MediaStore.Audio.Media.TITLE,
                MediaStore.Audio.Media.DISPLAY_NAME,
                MediaStore.Audio.Media.ALBUM,
                MediaStore.Audio.Media.ARTIST,
                MediaStore.Audio.Media.DURATION,
                MediaStore.Audio.Media.YEAR,
                MediaStore.Audio.Media.DATA,
                MediaStore.Audio.Media.SIZE,
        };

        String[] genresProjection = {
                MediaStore.Audio.Genres._ID,
                MediaStore.Audio.Genres.NAME
        };

        Uri uri = MediaStore.Audio.Genres.EXTERNAL_CONTENT_URI;
        Cursor projectionCursor = context.getContentResolver().query(
                uri,
                genresProjection,
                null,
                null,
                null
        );

        while (projectionCursor.moveToNext()) {

            String genre_id = projectionCursor.getString(0);
            String genre_name = projectionCursor.getString(1);

            uri = MediaStore.Audio.Genres.Members.getContentUri("external", Long.parseLong(genre_id));

            Cursor cursor = context.getContentResolver().query(
                    uri,
                    projection,
                    selection,
                    null,
                    null
            );

            while (cursor.moveToNext()) {

                // Create song object
                FireMixtape current = new FireMixtape(context);

                current._id = cursor.getString(0);
                current.title = cursor.getString(1);
                current.display_name = cursor.getString(2);
                current.album = cursor.getString(3);
                current.artist = cursor.getString(4);
                current.duration = cursor.getString(5);
                current.year = cursor.getString(6);
                current.data = cursor.getString(7);
                current.size = cursor.getString(8);
                current.genre = genre_name;

                fireMixtapes.add(current);

            }

            cursor.close();

        }
        projectionCursor.close();

        Log.i("SongManager", "Finished getFire() in " +
                Long.toString(Calendar.getInstance().getTimeInMillis() - startTime) +
                " millis.");
    }
}
