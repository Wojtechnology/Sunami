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

import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.util.LruCache;
import android.util.Pair;
import android.widget.ImageView;

import java.lang.ref.WeakReference;

/**
 * Created by wojtekswiderski on 15-07-29.
 */
public class ThumbnailManager {

    private MainActivity mContext;
    private Bitmap mPlaceHolderBitmap;

    private LruCache<String, Bitmap> mMemoryCache;

    public ThumbnailManager(MainActivity context) {
        mContext = context;
        mPlaceHolderBitmap = BitmapFactory.decodeResource(context.getResources(), R.drawable.fire_mixtape_default);

        final int maxMemory = (int) (Runtime.getRuntime().maxMemory() / 1024);
        final int cacheSize = maxMemory / 8;

        mMemoryCache = new LruCache<String, Bitmap>(cacheSize) {
            @Override
            protected int sizeOf(String key, Bitmap bitmap) {
                return bitmap.getByteCount() / 1024;
            }
        };
    }

    public void setAlbumThumbnail(FireMixtape song, Pair<Integer, Integer> dimens, ImageView imageView) {
        if (cancelPotentialWork(song, imageView)) {
            Bitmap bitmap = null;
            if (song.isSoundcloud) {
                bitmap = getBitmapFromMemCache(song.album_art_url);
            } else {
                bitmap = getBitmapFromMemCache(song.album_id);
            }
            if (bitmap != null) {
                imageView.setImageBitmap(bitmap);
            } else {
                BitmapWorkerTask task = new BitmapWorkerTask(imageView, dimens);
                final AsyncDrawable asyncDrawable =
                        new AsyncDrawable(mContext.getResources(), mPlaceHolderBitmap, task);
                imageView.setImageDrawable(asyncDrawable);
                task.execute(song);
            }
        }
    }

    class BitmapWorkerTask extends AsyncTask<FireMixtape, Void, Bitmap> {

        private final WeakReference<ImageView> imageViewReference;
        private Pair<Integer, Integer> paramDimens;
        private FireMixtape song = null;

        public BitmapWorkerTask(ImageView imageView, Pair<Integer, Integer> dimens) {
            paramDimens = dimens;
            imageViewReference = new WeakReference<>(imageView);
        }

        @Override
        protected Bitmap doInBackground(FireMixtape... params) {
            song = params[0];
            Bitmap bitmap = null;
            if (song.isSoundcloud) {
                bitmap = AlbumArtHelper.decodeBitmapFromURL(song.album_art_url, false);
            } else {
                bitmap = AlbumArtHelper.decodeSampledBitmapFromAlbumId(mContext,
                        Long.parseLong(song.album_id),
                        paramDimens.first, paramDimens.second);
            }
            if (bitmap == null) return null;
            if (song.isSoundcloud) {
                addBitmapToMemoryCache(song.album_art_url, bitmap);
            } else {
                addBitmapToMemoryCache(song.album_id, bitmap);

            }
            return bitmap;
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

    public static boolean cancelPotentialWork(FireMixtape song, ImageView imageView) {
        final BitmapWorkerTask bitmapWorkerTask = getBitmapWorkerTask(imageView);

        if (bitmapWorkerTask != null) {
            final FireMixtape bitmapData = bitmapWorkerTask.song;
            // If bitmapData is not yet set or it differs from the new data
            if (bitmapData == null || bitmapData != song) {
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

    public void addBitmapToMemoryCache(String key, Bitmap bitmap) {
        if (getBitmapFromMemCache(key) == null) {
            mMemoryCache.put(key, bitmap);
        }
    }

    public Bitmap getBitmapFromMemCache(String key) {
        return mMemoryCache.get(key);
    }
}
