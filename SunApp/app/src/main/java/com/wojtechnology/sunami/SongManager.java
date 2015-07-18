package com.wojtechnology.sunami;

import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.os.AsyncTask;
import android.provider.MediaStore;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

/**
 * Created by wojtekswiderski on 15-05-15.
 */
public class SongManager {

    private Context context;

    private List<FireMixtape> mSongList;
    private Map<String, FireMixtape> mSongDict;

    private boolean genresUpdated;

    // needs to be current with JSON file containing genres
    public static final String DEFAULT_GENRE = "__notfound__";

    public SongManager(Context context) {
        this.context = context;
        genresUpdated = false;
        mSongList = new ArrayList<>();
        mSongDict = new HashMap<>();
        initSongs();
    }

    public List<FireMixtape> getByTitle() {
        List<FireMixtape> displayList = new ArrayList<>(mSongList);
        if (mSongList.size() <= 0) {
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
        while (firstLetter(displayList.get(i).title) < 'A' ||
                firstLetter(displayList.get(i).title) > 'Z') {
            i++;
            if (i >= displayList.size()) return displayList;
        }
        int sum = i;
        int j = i;

        for (char headerTitle = 'A'; headerTitle <= 'Z' && i < displayList.size(); i++) {
            if (firstLetter(displayList.get(i).title) != headerTitle) {
                letters.put(headerTitle, i - j);
                headerTitle++;
                j = i;
                i--;
            }
        }

        for (char headerTitle = 'A'; headerTitle <= 'Z'
                && letters.containsKey(headerTitle); headerTitle++) {
            if (letters.get(headerTitle) != 0) {
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

    public static char firstLetter(String word) {
        word = word.toUpperCase();
        if (word.length() > 4) {
            if (word.substring(0, 4).equals("THE ")) {
                word = word.substring(4);
            }
        }
        return word.charAt(0);
    }

    public List<FireMixtape> getFire() {
        return mSongList;
    }

    public int size() {
        return mSongList.size();
    }

    public FireMixtape getSongAtIndex(int index) {
        return mSongList.get(index);
    }

    private class InitSongsTask extends AsyncTask<Void, Integer, Void> {

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

            Uri uri = android.provider.MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;
            ;

            Cursor cursor = context.getContentResolver().query(
                    uri,
                    projection,
                    selection,
                    null,
                    null
            );

            if (cursor == null) {
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
                current.genre = DEFAULT_GENRE;

                mSongList.add(current);
                mSongDict.put(current.data, current);

            }
            cursor.close();

            Log.i("SongManager", "Finished getFire() in " +
                    Long.toString(Calendar.getInstance().getTimeInMillis() - startTime) +
                    " millis.");
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);
            ((TheBrain) context).postInit();
        }
    }

    private void initSongs() {
        new InitSongsTask().execute();
    }


    public void genresFromDB(Set<String> genres) {
        long startTime = Calendar.getInstance().getTimeInMillis();
        //Some audio may be explicitly marked as not being music
        String selection = MediaStore.Audio.Media.IS_MUSIC + " != 0";

        String[] projection = {
                MediaStore.Audio.Media.DATA
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

        if (projectionCursor == null) {
            return;
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
                FireMixtape song = mSongDict.get(cursor.getString(0));
                song.genre = genre_name.toLowerCase();
                setActualGenre(song, genres);

            }
            cursor.close();
        }
        projectionCursor.close();

        Log.i("SongManager", "Finished genresFromDB() in " +
                Long.toString(Calendar.getInstance().getTimeInMillis() - startTime) +
                " millis.");
    }

    private void setActualGenre(FireMixtape song, Set<String> genres) {
        if (genres.contains(song.genre)) {
            song.actualGenre = song.genre;
            return;
        }
        String[] keyWords = song.genre.split("[\\s./-]+");
        int max = 0;
        String bestMatch = song.actualGenre;
        for (String genre : genres) {
            int sum = 0;
            for (String word : keyWords) {
                if (genre.contains(word)) {
                    sum += word.length();
                }
            }
            if (sum > max) {
                max = sum;
                bestMatch = genre;
            } else if (sum != 0 && sum == max && genre.length() < bestMatch.length()) {
                bestMatch = genre;
            }
        }
        song.actualGenre = bestMatch;
    }

    public void updateGenres(Set<String> genres, JSONArray songs) throws JSONException {
        long startTime = Calendar.getInstance().getTimeInMillis();
        for (int i = 0; i < songs.length(); i++) {
            JSONArray ja = (JSONArray) songs.get(i);
            try {
                FireMixtape song = mSongDict.get(ja.getString(0));
                song.genre = ja.getString(1);
                song.actualGenre = ja.getString(2);
                song.multiplier = ja.getDouble(3);
                Calendar cal = Calendar.getInstance();
                SimpleDateFormat sdf = new SimpleDateFormat("EEE MMM dd HH:mm:ss z yyyy", Locale.ENGLISH);
                cal.setTime(sdf.parse(ja.getString(4)));
                song.lastPlayed = cal;
            } catch (Exception e) {
                Log.e("Song Wrong", i + ": " + ja.getString(0) + ", " + ja.getString(1));
                genresFromDB(genres);
                return;
            }
        }
        Log.i("SongManager", "Finished updateGenres() in " +
                Long.toString(Calendar.getInstance().getTimeInMillis() - startTime) +
                " millis.");
    }

    public JSONArray getSongJSON() throws JSONException {
        JSONArray ja = new JSONArray();
        for (int i = 0; i < mSongList.size(); i++) {
            JSONArray song = new JSONArray();
            song.put(0, mSongList.get(i).data);
            song.put(1, mSongList.get(i).genre);
            song.put(2, mSongList.get(i).actualGenre);
            song.put(3, mSongList.get(i).multiplier);
            song.put(4, mSongList.get(i).lastPlayed.getTime().toString());
            ja.put(song);
        }
        return ja;
    }

    private void printSongs() {
        for (int i = 0; i < mSongList.size(); i++) {
            Log.i("SongManager", mSongList.get(i).title + ": Genre - " +
                    mSongList.get(i).genre + ", Best Match - " + mSongList.get(i).actualGenre);
        }
    }
}
