package com.wojtechnology.sunami;

import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.WindowManager;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Comparator;
import java.util.List;

/**
 * Created by wojtekswiderski on 15-04-13.
 */
public class FireMixtape{

    // If no artwork available
    public int default_icon_id;

    // Some required variables for Android functions
    private Context mContext;

    // Helpful booollllssss
    public Boolean icon_loaded;

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
        multiplier = 1.0;
        calculatedValue = 1.0;
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

    /* public Bitmap getAlbumArt() {
        {
            if(!icon_loaded) {
                Bitmap bm = null;
                try {
                    Uri sArtworkUri = Uri.parse("content://media/external/audio/albumart");
                    Uri uri = ContentUris.withAppendedId(sArtworkUri, Long.parseLong(album_id));
                    ContentResolver res = context.getContentResolver();
                    InputStream in = res.openInputStream(uri);
                    bm = BitmapFactory.decodeStream(in);
                    this.art_bitmap = scaleBitmap(bm);
                } catch (Exception e) {
                }
                this.icon_loaded = true;
            }
            return this.art_bitmap;
        }
    }

    public static Bitmap scaleBitmap(Bitmap bm){
        return Bitmap.createScaledBitmap(bm, (int) (ICON_WIDTH * display_density),
                (int) (ICON_HEIGHT * display_density), true);
    } */
}
