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
import java.util.List;

/**
 * Created by wojtekswiderski on 15-04-13.
 */
public class FireMixtape {

    // Constants
    private final String LOG_MESSAGE = "FIREMIXTAPE";

    // If no artwork available
    public int default_icon_id;

    // Some required variables for Android functions
    private Context context;

    // Helpful booollllssss
    public Boolean icon_loaded;

    // Song data from SQL
    public String _id;
    public String album;
    public String album_id;
    public String artist;
    public String artist_id;
    public String bookmark;
    public String composer;
    public String duration;
    public String title_key;
    public String year;
    public String data;
    public String date_added;
    public String display_name;
    public String mime_type;
    public String size;
    public String title;

    // List of genres for particular song
    private List<Genre> genres;

    public FireMixtape(Context context) {
        this.default_icon_id = R.mipmap.ic_launcher;
        this.context = context;
        this.icon_loaded = false;
        this.genres = new ArrayList<Genre>();
    }

    public void addGenre(String _id, String name){
        Genre newGenre = new Genre(_id, name);
        this.genres.add(newGenre);
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

    // Some Testing Functions
    public void printGenres(){
        Log.i(LOG_MESSAGE, this.display_name);
        for(int i = 0; i < this.genres.size(); i++){
            Log.i(LOG_MESSAGE, "Genre " + i + 1 + ": " + this.genres.get(i));
        }
    }
}
