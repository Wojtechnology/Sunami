/*

    Sunami - An Android music player which knows what you want to listen to.
    Copyright (C) 2015 Wojtek Swiderski

    Sunami is free software: you can redistribute it and/or modify
    it under the terms of the GNU General Public License as published by
    the Free Software Foundation, either version 3 of the License, or
    (at your option) any later version.

    Sunami is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU General Public License for more details.

    The GNU General Public License can be found at the root of this repository.

    To contact me, email me at wojtek.technology@gmail.com

 */

package com.wojtechnology.sunami;

import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.os.AsyncTask;
import android.provider.MediaStore;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;

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

    // needs to be current with JSON file containing genres
    public static final String DEFAULT_GENRE = "__notfound__";

    public SongManager(Context context) {
        this.context = context;
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
                return compareTitles(lhs, rhs);
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
                FireMixtape mixtape = HeaderHelper.makeHeader(context, Character.toString(headerTitle));
                displayList.add(sum, mixtape);
                sum++;
            }
            sum += letters.get(headerTitle);
        }

        FireMixtape final_header = HeaderHelper.makeFinal(context,  getFinalLabel());

        displayList.add(final_header);

        return displayList;
    }

    public List<FireMixtape> getByArtist() {
        List<FireMixtape> displayList = new ArrayList<>(mSongList);
        if (mSongList.size() <= 0) {
            return displayList;
        }

        Collections.sort(displayList, new Comparator<FireMixtape>() {
            @Override
            public int compare(FireMixtape lhs, FireMixtape rhs) {
                String lArtist = lhs.artist.toLowerCase();
                String rArtist = rhs.artist.toLowerCase();

                if (lArtist.equals(rArtist)) {
                    return compareTitles(lhs, rhs);
                }

                return lArtist.compareTo(rArtist);
            }
        });

        String lastArtist = "";
        for (int i = 0; i < displayList.size(); i++) {
            if (!lastArtist.equals(displayList.get(i).artist)) {
                lastArtist = displayList.get(i).artist;
                FireMixtape artistHeader = HeaderHelper.makeHeader(context, displayList.get(i).artist);
                artistHeader.artist = displayList.get(i).artist;
                displayList.add(i, artistHeader);
                i++;
            }
        }

        FireMixtape final_header = HeaderHelper.makeFinal(context, getFinalLabel());
        final_header.artist = getFinalLabel();

        displayList.add(final_header);
        return displayList;
    }

    private String getFinalLabel() {
        return mSongList.size() + ((mSongList.size() == 1) ? " song found" : " songs found");
    }

    private int compareTitles(FireMixtape lhs, FireMixtape rhs) {
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

    public static char firstLetter(String word, boolean removeThe) {
        word = word.toUpperCase();
        if (word.length() > 4 && removeThe) {
            if (word.substring(0, 4).equals("THE ")) {
                word = word.substring(4);
            }
        }
        return word.charAt(0);
    }

    public static char firstLetter(String word) {
        return firstLetter(word, true);
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
                    MediaStore.Audio.Media.ALBUM_ID,
            };

            Uri uri = android.provider.MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;

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
                current.album_id = cursor.getString(9);
                current.genre = DEFAULT_GENRE;

                mSongList.add(current);
                mSongDict.put(current.data, current);

            }
            cursor.close();
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
        int sum = 0;
        for (int i = 0; i < songs.length(); i++) {
            JSONArray ja = (JSONArray) songs.get(i);
            try {
                FireMixtape song;
                boolean isSoundcloud = ja.getBoolean(5);
                if (isSoundcloud) {
                    song = new FireMixtape(context);
                    song.title = ja.getString(6);
                    song.artist = ja.getString(7);
                    song.duration = ja.getString(8);
                    song.album_art_url = ja.getString(9);
                    song.permalink_url = ja.getString(10);
                    song.data = ja.getString(0);
                    song.isSoundcloud = true;
                    mSongList.add(song);
                    mSongDict.put(song.data, song);
                } else {
                    song = mSongDict.get(ja.getString(0));
                }
                song.genre = ja.getString(1);
                song.actualGenre = ja.getString(2);
                song.multiplier = ja.getDouble(3);
                Calendar cal = Calendar.getInstance();
                SimpleDateFormat sdf = new SimpleDateFormat("EEE MMM dd HH:mm:ss z yyyy", Locale.ENGLISH);
                cal.setTime(sdf.parse(ja.getString(4)));
                song.lastPlayed = cal;
                sum++;
            } catch (Exception e) {
                // Song that used to exist is missing from file system
            }
        }
        if (sum != mSongList.size()) {
            genresFromDB(genres);
        }
    }

    public JSONArray getSongJSON() throws JSONException {
        JSONArray ja = new JSONArray();
        for (int i = 0; i < mSongList.size(); i++) {
            JSONArray song = new JSONArray();
            FireMixtape fireMixtape = mSongList.get(i);
            song.put(0, fireMixtape.data);
            song.put(1, fireMixtape.genre);
            song.put(2, fireMixtape.actualGenre);
            song.put(3, fireMixtape.multiplier);
            song.put(4, fireMixtape.lastPlayed.getTime().toString());
            song.put(5, fireMixtape.isSoundcloud);
            if (fireMixtape.isSoundcloud) {
                song.put(6, fireMixtape.title);
                song.put(7, fireMixtape.artist);
                song.put(8, fireMixtape.duration);
                song.put(9, fireMixtape.album_art_url);
                song.put(10, fireMixtape.permalink_url);
            }
            ja.put(song);
        }
        return ja;
    }

    public boolean containsSong(FireMixtape song) {
        for (int i = 0; i < mSongList.size(); i++) {
            FireMixtape checkSong = mSongList.get(i);
            if (checkSong.data.equals(song.data)) {
                return true;
            }
        }
        return false;
    }

    // returns if song was added
    public boolean addSong(FireMixtape song) {
        if (!containsSong(song)) {
            mSongList.add(song);
            mSongDict.put(song.data, song);
            return true;
        }
        return false;
    }

    // returns if song was removed
    public boolean removeSong(FireMixtape song) {
        if (containsSong(song)) {
            mSongList.remove(song);
            mSongDict.remove(song.data);
            return true;
        }
        return false;
    }

    private void printSongs() {
        for (int i = 0; i < mSongList.size(); i++) {
            Log.i("SongManager", mSongList.get(i).title + ": Genre - " +
                    mSongList.get(i).genre + ", Best Match - " + mSongList.get(i).actualGenre);
        }
    }
}
