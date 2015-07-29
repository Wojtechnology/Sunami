package com.wojtechnology.sunami;

import android.content.Context;
import android.content.Intent;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.media.Image;
import android.net.Uri;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
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

    private Soundcloud mSoundCloud;

    public ListAdapter(MainActivity context, List<FireMixtape> data, TheBrain theBrain, Soundcloud soundCloud) {
        mContext = context;
        setData(data);
        flushVisibleData();
        mInflater = LayoutInflater.from(mContext);
        mTheBrain = theBrain;
        mSoundCloud = soundCloud;
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
            return;
        }
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

        if (mContext.isNetworkAvailable()) {
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
            itemHolder.title.setText(current.title);
            itemHolder.artist.setText(current.artist);

            if (current.isSoundcloud) {
                itemHolder.duration.setTextColor(mContext.getResources().getColor(R.color.accentColor));
            } else {
                itemHolder.duration.setTextColor(mContext.getResources().getColor(R.color.primaryColorDark));
            }

            itemHolder.duration.setText(displayTime(current.duration));
            itemHolder.icon.setImageResource(R.mipmap.ic_launcher);

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
            Drawable fire = mContext.getResources().getDrawable(R.drawable.fire_mixtape);
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
                    mTheBrain.playSong(current, true);
                }
            });
            itemHolder.addButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    mTheBrain.toggleSongInQueue(current);
                }
            });
        }
    }

    private void openSoundcloudInBrowser() {
        Intent intent = new Intent();
        intent.setAction(Intent.ACTION_VIEW);
        intent.addCategory(Intent.CATEGORY_BROWSABLE);
        intent.setData(Uri.parse("https://soundcloud.com"));
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
        if (mVisibleData.get(position).genre == "__header__") {
            return TYPE_HEADER;
        } else if (mVisibleData.get(position).genre == "__headersoundcloud__") {
            return TYPE_HEADER_SOUNDCLOUD;
        } else if (mVisibleData.get(position).genre == "__final__") {
            return TYPE_FINAL;
        } else if (mVisibleData.get(position).genre == "__finalsoundcloud__") {
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
            addButton = (Button) itemView.findViewById(R.id.add_queue_button);
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

    private String displayTime(String time) {
        String newTime = "";
        int intTime = Integer.parseInt(time);
        if (intTime < 60000) {
            newTime += "0";
        } else {
            newTime += Integer.toString(intTime / 60000);
            intTime = intTime % 60000;
        }
        newTime += ":";
        String seconds = Integer.toString(intTime / 1000);
        if (seconds.length() == 1) {
            newTime += "0" + seconds;
        } else {
            newTime += seconds;
        }
        return newTime;
    }
}
