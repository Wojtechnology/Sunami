package com.wojtechnology.sunami;

import android.content.Context;
import java.util.Calendar;

/**
 * Created by wojtekswiderski on 15-04-13.
 */
public class FireMixtape{

    // static value used for opacity values for songs
    public static double maxCalculatedValue = 1.0;

    // If no artwork available
    public int default_icon_id;

    // Some required variables for Android functions
    private Context mContext;

    // Helpful booollllssss
    public Boolean icon_loaded;
    public Boolean isUpNext;
    public Boolean isSoundcloud;

    // Song data from SQL
    public String _id;
    public String title;
    public String display_name;
    public String album;
    public String artist;
    public String genre;
    public String duration;
    public String year;
    public String data;
    public String size;

    // Used for getting thumbnails
    public String album_id;
    public String album_art_url;
    public String permalink_url;

    // Values that are used to calculate popularity of song
    public Calendar lastPlayed;
    public double multiplier;

    public double calculatedValue;

    // The genre that is recognized by the app
    public String actualGenre;

    public FireMixtape(Context context) {
        default_icon_id = R.mipmap.ic_launcher;
        mContext = context;
        icon_loaded = false;
        isUpNext = false;
        isSoundcloud = false;
        multiplier = 1.0;
        calculatedValue = 1.0;
        genre = SongManager.DEFAULT_GENRE;
        actualGenre = SongManager.DEFAULT_GENRE;
        lastPlayed = Calendar.getInstance();
        lastPlayed.set(Calendar.YEAR, lastPlayed.get(Calendar.YEAR) - 1);
    }

    public long getMillisSinceLastPlay() {
        long now = Calendar.getInstance().getTimeInMillis();
        long delta = now - lastPlayed.getTimeInMillis();
        if (delta < 0) {
             return 0;
        }
        return delta;
    }
}
