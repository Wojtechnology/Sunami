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

    public static List<FireMixtape> sortByTitle(Context context, List<FireMixtape> data) {
        Collections.sort(data, new Comparator<FireMixtape>() {
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
        while(firstLetter(data.get(i).title) != 'A'){
            i++;
        }
        int sum = i;
        int j = i;

        for(char headerTitle = 'A'; headerTitle <= 'Z' && i < data.size(); i++){
            if(firstLetter(data.get(i).title) != headerTitle){
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
                data.add(sum, mixtape);
                sum++;
            }
            sum += letters.get(headerTitle);
        }

        return data;
    }

    private static char firstLetter(String word){
        word = word.toUpperCase();
        if(word.length() > 4) {
            if (word.substring(0, 4).equals("THE ")) {
                word = word.substring(4);
            }
        }
        return word.charAt(0);
    }

    public static List<FireMixtape> getSongs(Context context){
        long startTime = Calendar.getInstance().getTimeInMillis();

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
                current.genre_id = genre_id;
                current.genre = genre_name;

                data.add(current);

            }

            cursor.close();

        }
        projectionCursor.close();

        Log.i("SongManager", "Finished getFire() in " +
                Long.toString(Calendar.getInstance().getTimeInMillis() - startTime) +
                " millis.");
        return data;
    }
}
