package com.wojtechnology.sunami;

import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.util.Pair;
import android.widget.ImageView;

import java.lang.ref.WeakReference;

/**
 * Created by wojtekswiderski on 15-07-29.
 */
public class ThumbnailManager {

    private MainActivity mContext;
    private Bitmap mPlaceHolderBitmap;

    public ThumbnailManager(MainActivity context) {
        mContext = context;
        mPlaceHolderBitmap = BitmapFactory.decodeResource(context.getResources(), R.mipmap.ic_launcher);
    }

    public void setAlbumThumbnail(FireMixtape song, Pair<Integer, Integer> dimens, ImageView imageView) {
        Long album_id = Long.parseLong(song.album_id);
        if (cancelPotentialWork(album_id, imageView)) {
            BitmapWorkerTask task = new BitmapWorkerTask(imageView, dimens);
            final AsyncDrawable asyncDrawable =
                    new AsyncDrawable(mContext.getResources(), mPlaceHolderBitmap, task);
            imageView.setImageDrawable(asyncDrawable);
            task.execute(album_id);
        }
    }

    class BitmapWorkerTask extends AsyncTask<Long, Void, Bitmap> {

        private final WeakReference<ImageView> imageViewReference;
        private Pair<Integer, Integer> paramDimens;
        private long album_id = 0;

        public BitmapWorkerTask(ImageView imageView, Pair<Integer, Integer> dimens) {
            paramDimens = dimens;
            imageViewReference = new WeakReference<>(imageView);
        }

        @Override
        protected Bitmap doInBackground(Long... params) {
            album_id = params[0];
            return AlbumArtHelper.decodeSampledBitmapFromUri(mContext, album_id,
                            paramDimens.first, paramDimens.second);
        }

        @Override
        protected void onPostExecute(Bitmap bitmap) {
            if (isCancelled()) {
                bitmap = null;
            }

            if (imageViewReference != null && bitmap != null) {
                final ImageView imageView = imageViewReference.get();
                if (imageView != null) {
                    imageView.setImageBitmap(bitmap);
                }
            }
        }
    }

    static class AsyncDrawable extends BitmapDrawable {
        private final WeakReference<BitmapWorkerTask> bitmapWorkerTaskReference;

        public AsyncDrawable(Resources res, Bitmap bitmap,
                             BitmapWorkerTask bitmapWorkerTask) {
            super(res, bitmap);
            bitmapWorkerTaskReference =
                    new WeakReference<>(bitmapWorkerTask);
        }

        public BitmapWorkerTask getBitmapWorkerTask() {
            return bitmapWorkerTaskReference.get();
        }
    }

    public static boolean cancelPotentialWork(long album_id, ImageView imageView) {
        final BitmapWorkerTask bitmapWorkerTask = getBitmapWorkerTask(imageView);

        if (bitmapWorkerTask != null) {
            final long bitmapData = bitmapWorkerTask.album_id;
            // If bitmapData is not yet set or it differs from the new data
            if (bitmapData == 0 || bitmapData != album_id) {
                // Cancel previous task
                bitmapWorkerTask.cancel(true);
            } else {
                // The same work is already in progress
                return false;
            }
        }
        // No task associated with the ImageView, or an existing task was cancelled
        return true;
    }

    private static BitmapWorkerTask getBitmapWorkerTask(ImageView imageView) {
        if (imageView != null) {
            final Drawable drawable = imageView.getDrawable();
            if (drawable instanceof AsyncDrawable) {
                final AsyncDrawable asyncDrawable = (AsyncDrawable) drawable;
                return asyncDrawable.getBitmapWorkerTask();
            }
        }
        return null;
    }
}
