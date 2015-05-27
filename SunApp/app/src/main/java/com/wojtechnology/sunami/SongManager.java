package com.wojtechnology.sunami;

import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.os.AsyncTask;
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

    private List<FireMixtape> songList;
    private Map<String, FireMixtape> songDict;

    private boolean genresUpdated;

    public SongManager(Context context) {
        this.context = context;
        genresUpdated = false;
        songList = new ArrayList<>();
        songDict = new HashMap<>();
        initSongs();
    }

    public List<FireMixtape> getByTitle() {
        List<FireMixtape> displayList = songList;
        if (songList.size() <= 0){
            return displayList;
        }

        Collections.sort(displayList, new Comparator<FireMixtape>() {
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
        while(firstLetter(displayList.get(i).title) < 'A' ||
                firstLetter(displayList.get(i).title) > 'Z'){
            i++;
        }
        int sum = i;
        int j = i;

        for(char headerTitle = 'A'; headerTitle <= 'Z' && i < displayList.size(); i++){
            if(firstLetter(displayList.get(i).title) != headerTitle){
                letters.put(headerTitle, i - j);
                headerTitle++;
                j = i;
                i--;
            }
        }

        for(char headerTitle = 'A'; headerTitle <= 'Z'
                && letters.containsKey(headerTitle); headerTitle++){
            if(letters.get(headerTitle) != 0) {
                FireMixtape mixtape = new FireMixtape(null);
                mixtape.title = Character.toString(headerTitle);
                mixtape.genre = "__header__";
                displayList.add(sum, mixtape);
                sum++;
            }
            sum += letters.get(headerTitle);
        }

        return displayList;
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
        return songList;
    }

    public FireMixtape getSong(String _id){
        for(int i = 0; i < songList.size(); i++){
            if(songList.get(i)._id == _id){
                return songList.get(i);
            }
        }
        return null;
    }

    public int size(){
        return songList.size();
    }

    public String getSongId(int index){
        return songList.get(index)._id;
    }

    private class InitSongsTask extends AsyncTask<Void, Integer, Void>{

        @Override
        protected Void doInBackground(Void... params) {
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

            Uri uri = android.provider.MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;;

            Cursor cursor = context.getContentResolver().query(
                    uri,
                    projection,
                    selection,
                    null,
                    null
            );

            if (cursor == null){
                return null;
            }

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

                songList.add(current);
                songDict.put(current._id, current);

            }

            cursor.close();

            Log.i("SongManager", "Finished getFire() in " +
                    Long.toString(Calendar.getInstance().getTimeInMillis() - startTime) +
                    " millis.");

            new UpdateGenresTask().execute();
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);
            ((MainActivity) context).setProgressBar(false);
            ((MainActivity) context).setRecyclerViewData();
        }
    }

    private void initSongs(){
        new InitSongsTask().execute();
    }

    private class UpdateGenresTask extends AsyncTask<Void, Integer, Void>{

        @Override
        protected Void doInBackground(Void... params) {
            long startTime = Calendar.getInstance().getTimeInMillis();

            //Some audio may be explicitly marked as not being music
            String selection = MediaStore.Audio.Media.IS_MUSIC + " != 0";

            String[] projection = {
                    MediaStore.Audio.Media._ID,
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

            if (projectionCursor == null){
                return null;
            }

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

                    String _id = cursor.getString(0);
                    songDict.get(_id).genre = genre_name;

                }

                cursor.close();

            }
            projectionCursor.close();

            Log.i("SongManager", "Finished updateGenres() in " +
                    Long.toString(Calendar.getInstance().getTimeInMillis() - startTime) +
                    " millis.");

            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);
            genresUpdated = true;
        }
    }

    private void printSongs(){
        for(int i = 0; i < songList.size(); i++){
            Log.i("SongManager", songList.get(i).title + ": Genre - " + songList.get(i).genre);
        }
    }
}
