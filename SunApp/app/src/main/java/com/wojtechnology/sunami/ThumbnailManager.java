package com.wojtechnology.sunami;

import android.graphics.Bitmap;
import android.os.AsyncTask;
import android.util.Pair;
import android.widget.ImageView;

import java.lang.ref.WeakReference;

/**
 * Created by wojtekswiderski on 15-07-29.
 */
public class ThumbnailManager {

    private MainActivity mContext;

    public ThumbnailManager(MainActivity context) {
        mContext = context;
    }

    public void setAlbumThumbnail(FireMixtape song, Pair<Integer, Integer> dimens, ImageView imageView) {
        BitmapWorkerTask task = new BitmapWorkerTask(imageView, dimens);
        task.execute(Long.parseLong(song.album_id));
    }

    class BitmapWorkerTask extends AsyncTask<Long, Void, Bitmap> {

        private final WeakReference<ImageView> imageViewReference;
        private Pair<Integer, Integer> paramDimens;
        private long album_id;

        public BitmapWorkerTask(ImageView imageView, Pair<Integer, Integer> dimens) {
            paramDimens = dimens;
            imageViewReference = new WeakReference<ImageView>(imageView);
        }

        @Override
        protected Bitmap doInBackground(Long... params) {
            album_id = params[0];
            return AlbumArtHelper.decodeSampledBitmapFromUri(mContext, album_id,
                            paramDimens.first, paramDimens.second);
        }

        @Override
        protected void onPostExecute(Bitmap bitmap) {
            if (imageViewReference != null && bitmap != null) {
                final ImageView imageView = imageViewReference.get();
                if (imageView != null) {
                    imageView.setImageBitmap(bitmap);
                }
            }
        }
    }
}
