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

import android.content.Intent;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.support.v7.widget.PopupMenu;
import android.support.v7.widget.RecyclerView;
import android.util.Pair;
import android.view.LayoutInflater;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Created by wojtekswiderski on 15-04-12.
 */
public class ListAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private MainActivity mContext;
    private TheBrain mTheBrain;

    private static int TYPE_FINAL_SOUNDCLOUD = 4;
    private static int TYPE_FINAL = 3;
    private static int TYPE_HEADER_SOUNDCLOUD = 2;
    private static int TYPE_HEADER = 1;
    private static int TYPE_LIST = 0;

    private LayoutInflater mInflater;
    private List<FireMixtape> mData = Collections.emptyList();
    private List<FireMixtape> mVisibleData = Collections.emptyList();

    public boolean mIsSearching;

    private Pair<Integer, Integer> mThumbnailDimens;

    private ThumbnailManager mThumbnailManager;
    private Soundcloud mSoundCloud;

    public ListAdapter(MainActivity context, List<FireMixtape> data, TheBrain theBrain, Soundcloud soundCloud) {
        mContext = context;
        setData(data);
        flushVisibleData();
        mInflater = LayoutInflater.from(mContext);
        mTheBrain = theBrain;
        mSoundCloud = soundCloud;
        mThumbnailManager = new ThumbnailManager(context);
        mIsSearching = false;
    }

    public void flushVisibleData() {
        if (mContext.mFastScroller != null) {
            mContext.mFastScroller.setVisibility(View.VISIBLE);
        }
        mVisibleData = new ArrayList<>(mData);
        notifyDataSetChanged();
    }

    public void setData(List<FireMixtape> data) {
        mData = data;
    }

    public void setFilter(String q, boolean addFinal) {
        if (q.equals("")) {
            flushVisibleData();
            mIsSearching = false;
            return;
        }
        mIsSearching = true;
        if (mContext.mFastScroller != null) {
            mContext.mFastScroller.setVisibility(View.INVISIBLE);
        }
        mVisibleData = new ArrayList<>(0);
        for (int i = 0; i < mData.size(); i++) {
            if (mData.get(i).genre == "__header__" || mData.get(i).genre == "__final__") {
                // Do nothing
            } else if (mData.get(i).artist.toLowerCase().contains(q.toLowerCase()) ||
                    mData.get(i).title.toLowerCase().contains(q.toLowerCase())) {
                mVisibleData.add(mData.get(i));
            }
        }
        if (addFinal) {
            FireMixtape finalHeader = new FireMixtape(mContext);
            finalHeader.title = getFinalLabel();
            finalHeader.genre = "__final__";
            mVisibleData.add(finalHeader);
        }
        notifyDataSetChanged();
    }

    public void setFilterSubmit(String q) {
        setFilter(q, false);

        if (mContext.isSoundcloudEnabled()) {
            FireMixtape fireMixtape = HeaderHelper.makeSoundcloudFinal(mContext, "Searching...");
            mVisibleData.add(fireMixtape);

            mSoundCloud.getTracks(q, new SoundcloudCallback() {
                @Override
                public void callback(List<FireMixtape> data) {
                    mVisibleData.remove(mVisibleData.size() - 1);

                    if (data.size() > 0) {
                        FireMixtape foundHeader = HeaderHelper.makeSoundcloudHeader(mContext,
                                data.size() + " results");
                        mVisibleData.add(foundHeader);

                        mVisibleData.addAll(data);

                        FireMixtape foundFinal = HeaderHelper.makeFinal(mContext,
                                (mVisibleData.size() - 1) + ((mVisibleData.size() - 1 == 1) ? " song found" : " songs found"));
                        mVisibleData.add(foundFinal);
                    } else {
                        FireMixtape fireMixtape = HeaderHelper.makeFinal(mContext, getFinalLabel());
                        mVisibleData.add(fireMixtape);
                    }

                    notifyDataSetChanged();
                }
            });
        } else {
            FireMixtape fireMixtape = HeaderHelper.makeFinal(mContext, getFinalLabel());
            mVisibleData.add(fireMixtape);
        }
        notifyDataSetChanged();
    }

    private String getFinalLabel() {
        return mVisibleData.size() + ((mVisibleData.size() == 1) ? " song found" : " songs found");
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        if (viewType == TYPE_HEADER) {
            View view = mInflater.inflate(R.layout.fire_header, parent, false);
            HeaderHolder holder = new HeaderHolder(view);
            return holder;
        } else if (viewType == TYPE_HEADER_SOUNDCLOUD) {
            View view = mInflater.inflate(R.layout.fire_header_soundcloud, parent, false);
            HeaderSoundcloudHolder holder = new HeaderSoundcloudHolder(view);
            return holder;
        } else if (viewType == TYPE_FINAL) {
            View view = mInflater.inflate(R.layout.fire_final, parent, false);
            FinalHolder holder = new FinalHolder(view);
            return holder;
        } else if (viewType == TYPE_FINAL_SOUNDCLOUD) {
            View view = mInflater.inflate(R.layout.fire_final_soundcloud, parent, false);
            FinalSoundcloudHolder holder = new FinalSoundcloudHolder(view);
            return holder;
        } else {
            View view = mInflater.inflate(R.layout.fire_mixtape, parent, false);
            ItemHolder holder = new ItemHolder(view);
            return holder;
        }
    }

    @Override
    public void onBindViewHolder(final RecyclerView.ViewHolder holder, int position) {
        final FireMixtape current = mVisibleData.get(position);
        if (holder instanceof HeaderHolder) {
            HeaderHolder headerHolder = (HeaderHolder) holder;
            headerHolder.label.setText(current.title);
        } else if (holder instanceof HeaderSoundcloudHolder) {
            HeaderSoundcloudHolder headerSoundcloudHolder = (HeaderSoundcloudHolder) holder;
            headerSoundcloudHolder.label.setText(current.title);
            headerSoundcloudHolder.logo.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    openSoundcloudInBrowser();
                }
            });
        } else if (holder instanceof FinalHolder) {
            FinalHolder finalHolder = (FinalHolder) holder;
            finalHolder.label.setText(current.title);
        } else if (holder instanceof FinalSoundcloudHolder) {
            FinalSoundcloudHolder finalSoundcloudHolder = (FinalSoundcloudHolder) holder;
            finalSoundcloudHolder.label.setText(current.title);
            finalSoundcloudHolder.logo.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    openSoundcloudInBrowser();
                }
            });
        } else {
            final ItemHolder itemHolder = (ItemHolder) holder;
            final boolean isInLibrary = !current.isSoundcloud || mTheBrain.isSongInLibrary(current);

            itemHolder.title.setText(current.title);
            itemHolder.artist.setText(current.artist);

            getThumbnailSize(itemHolder);
            mThumbnailManager.setAlbumThumbnail(current, mThumbnailDimens, itemHolder.icon);

            // Set duration text color
            if (!isInLibrary) {
                itemHolder.duration.setTextColor(mContext.getResources().getColor(R.color.accentColor));
            } else {
                itemHolder.duration.setTextColor(mContext.getResources().getColor(R.color.primaryColorDark));
            }
            itemHolder.duration.setText(displayTime(current.duration));

            // Set add button resourse
            Drawable addToQueue;
            if (current.isUpNext) {
                addToQueue = mContext.getResources().getDrawable(R.drawable.ic_playlist_add_white_24dp);
                addToQueue.setColorFilter(0xffffab40, PorterDuff.Mode.MULTIPLY);
            } else {
                addToQueue = mContext.getResources().getDrawable(R.drawable.ic_queue_add);
            }
            itemHolder.addButton.setBackground(addToQueue);

            // Set fire resource
            Drawable fire = mContext.getResources().getDrawable(current.isSoundcloud ?
                    R.drawable.logo_big_soundcloud : R.drawable.fire_mixtape);
            float val = calculateNormalized(current);
            if (val > 0.2f) {
                fire.setColorFilter(0xffffab40, PorterDuff.Mode.MULTIPLY);
                itemHolder.fireView.setAlpha((val - 1.0f) * 0.5f + 1.0f);
            } else {
                fire.setColorFilter(0xff03a9f4, PorterDuff.Mode.MULTIPLY);
                itemHolder.fireView.setAlpha(0.6f - val);
            }
            itemHolder.fireView.setImageDrawable(fire);

            itemHolder.background.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (mTheBrain.mPlaying != current) {
                        if (!mTheBrain.checkAndNotifySoundcloudEnabled(current)) return;
                        mTheBrain.playSong(current, true);
                    }
                }
            });
            itemHolder.addButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    PopupMenu popup = new PopupMenu(mContext, v);
                    MenuInflater inflater = popup.getMenuInflater();
                    if (current.isSoundcloud) {
                        inflater.inflate(R.menu.menu_song_soundcloud, popup.getMenu());
                        MenuItem menuItem = popup.getMenu().findItem(R.id.manage_library_button);
                        if (isInLibrary) {
                            menuItem.setTitle("Remove from library");
                        } else {
                            menuItem.setTitle("Add to library");
                        }
                    } else {
                        inflater.inflate(R.menu.menu_song, popup.getMenu());
                    }
                    popup.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                        @Override
                        public boolean onMenuItemClick(MenuItem item) {
                            int id = item.getItemId();

                            switch (id) {
                                case R.id.play_next_button:
                                    mTheBrain.addSongToFront(current);
                                    break;
                                case R.id.add_queue_button:
                                    mTheBrain.addSongToQueue(current);
                                    break;
                                case R.id.manage_library_button:
                                    mTheBrain.toggleSongInLibrary(current);
                                    updateItem(current);
                                    break;
                                case R.id.open_with_soundcloud:
                                    openSoundcloudSongInBrowser(current.permalink_url);
                                    break;
                                default:
                                    break;
                            }

                            return true;
                        }
                    });
                    popup.show();
                }
            });
        }
    }

    private Pair<Integer, Integer> getThumbnailSize(ItemHolder itemHolder) {
        if (mThumbnailDimens == null) {
            itemHolder.icon.measure(View.MeasureSpec.UNSPECIFIED, View.MeasureSpec.UNSPECIFIED);
            mThumbnailDimens = new Pair<>(itemHolder.icon.getMeasuredWidth(), itemHolder.icon.getMeasuredHeight());
        }
        return mThumbnailDimens;
    }

    private void openSoundcloudInBrowser() {
        Intent intent = new Intent();
        intent.setAction(Intent.ACTION_VIEW);
        intent.addCategory(Intent.CATEGORY_BROWSABLE);
        intent.setData(Uri.parse("https://soundcloud.com"));
        mContext.startActivity(intent);
    }

    private void openSoundcloudSongInBrowser(String link) {
        Intent intent = new Intent();
        intent.setAction(Intent.ACTION_VIEW);
        intent.addCategory(Intent.CATEGORY_BROWSABLE);
        intent.setData(Uri.parse(link));
        mContext.startActivity(intent);
    }

    private float calculateNormalized(FireMixtape song) {
        if (FireMixtape.maxCalculatedValue == 1.0) {
            return 0.0f;
        }
        float normalizedValue = (float) ((song.calculatedValue - 1.0) / (FireMixtape.maxCalculatedValue - 1.0));
        if (normalizedValue > 1.0f) return 1.0f;
        else if (normalizedValue < 0.0f) return 0.0f;
        else return normalizedValue;
    }

    public void updateItem(FireMixtape song) {
        int i;
        for (i = 0; i < getItemCount(); i++) {
            if (song == mVisibleData.get(i)) {
                break;
            }
        }
        if (i < getItemCount()) {
            notifyItemChanged(i);
        }
    }

    @Override
    public int getItemViewType(int position) {
        if (mVisibleData.get(position).genre.equals("__header__")) {
            return TYPE_HEADER;
        } else if (mVisibleData.get(position).genre.equals("__headersoundcloud__")) {
            return TYPE_HEADER_SOUNDCLOUD;
        } else if (mVisibleData.get(position).genre.equals("__final__")) {
            return TYPE_FINAL;
        } else if (mVisibleData.get(position).genre.equals("__finalsoundcloud__")) {
            return TYPE_FINAL_SOUNDCLOUD;
        } else {
            return TYPE_LIST;
        }
    }

    @Override
    public int getItemCount() {
        return mVisibleData.size();
    }

    public String getTextToShowInBubble(int pos) {
        FireMixtape song = mVisibleData.get(pos);
        int state = mContext.getState();
        String important = (state == MainActivity.STATE_SONGS) ? song.title : song.artist;
        char firstLetter = SongManager.firstLetter(important);
        if (firstLetter >= 'A' && firstLetter <= 'Z') {
            return Character.toString(firstLetter);
        }
        return "#";
    }

    class ItemHolder extends RecyclerView.ViewHolder {
        private TextView title;
        private TextView artist;
        private TextView duration;
        private ImageView icon;
        private RelativeLayout background;
        private Button addButton;
        private ImageView fireView;

        public ItemHolder(View itemView) {
            super(itemView);
            title = (TextView) itemView.findViewById(R.id.list_title);
            artist = (TextView) itemView.findViewById(R.id.list_artist);
            duration = (TextView) itemView.findViewById(R.id.list_duration);
            icon = (ImageView) itemView.findViewById(R.id.list_icon);
            background = (RelativeLayout) itemView.findViewById(R.id.list_background);
            addButton = (Button) itemView.findViewById(R.id.song_menu_button);
            fireView = (ImageView) itemView.findViewById(R.id.fire);
        }
    }

    class HeaderHolder extends RecyclerView.ViewHolder {
        private TextView label;

        public HeaderHolder(View itemView) {
            super(itemView);
            label = (TextView) itemView.findViewById(R.id.header_label);
        }
    }

    class HeaderSoundcloudHolder extends RecyclerView.ViewHolder {
        private TextView label;
        private ImageView logo;

        public HeaderSoundcloudHolder(View itemView) {
            super(itemView);
            label = (TextView) itemView.findViewById(R.id.header_label);
            logo = (ImageView) itemView.findViewById(R.id.soundcloud_logo);
        }
    }

    class FinalHolder extends RecyclerView.ViewHolder {
        private TextView label;

        public FinalHolder(View itemView) {
            super(itemView);
            label = (TextView) itemView.findViewById(R.id.final_label);
        }
    }

    class FinalSoundcloudHolder extends RecyclerView.ViewHolder {
        private TextView label;
        private ImageView logo;

        public FinalSoundcloudHolder(View itemView) {
            super(itemView);
            label = (TextView) itemView.findViewById(R.id.final_label);
            logo = (ImageView) itemView.findViewById(R.id.soundcloud_logo);
        }
    }

    public static String displayTime(String time) {
        String newTime = "";
        int intTime = Integer.parseInt(time);
        boolean hasHour = false;

        if (intTime >= 3600000) {
            newTime += Integer.toString(intTime / 3600000) + ":";
            intTime = intTime % 3600000;
            hasHour = true;
        }

        if (intTime >= 60000) {
            String minutes = Integer.toString(intTime / 60000);
            if (minutes.length() == 1 && hasHour) {
                newTime += "0" + minutes + ":";
            } else {
                newTime += minutes + ":";
            }
            intTime = intTime % 60000;
        } else if (hasHour) {
            newTime += "00:";
        } else {
            newTime += "0:";
        }

        String seconds = Integer.toString(intTime / 1000);
        if (seconds.length() == 1) {
            newTime += "0" + seconds;
        } else {
            newTime += seconds;
        }
        return newTime;
    }
}
