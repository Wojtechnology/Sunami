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

/**
 * Created by wojtekswiderski on 15-04-13.
 */
public class FireMixtape {
    public int icon_id;

    public String _id;
    public String artist;
    public String title;
    public String data;
    public String display_name;
    public String duration;
    public String album_id;
    public Boolean icon_loaded;

    private static final int ICON_WIDTH = 48;
    private static final int ICON_HEIGHT = 48;
    private static float display_density;

    private Context context;

    public FireMixtape(Context context) {
        this.icon_id = R.mipmap.ic_launcher;
        this.context = context;
        this.icon_loaded = false;
        DisplayMetrics metrics = new DisplayMetrics();
        WindowManager wm = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
        wm.getDefaultDisplay().getMetrics(metrics);
        display_density = metrics.density;
    }

    public Bitmap getAlbumArt() {
        {
            Bitmap bm = null;
            try {
                Uri sArtworkUri = Uri.parse("content://media/external/audio/albumart");
                Uri uri = ContentUris.withAppendedId(sArtworkUri, Long.parseLong(album_id));
                ContentResolver res = context.getContentResolver();
                InputStream in = res.openInputStream(uri);
                bm = BitmapFactory.decodeStream(in);
            } catch (Exception e) {
            }
            return bm;
        }
    }

    public static Bitmap scaleBitmap(Bitmap bm){
        return Bitmap.createScaledBitmap(bm, (int) (ICON_WIDTH * display_density),
                (int) (ICON_HEIGHT * display_density), true);
    }
}
