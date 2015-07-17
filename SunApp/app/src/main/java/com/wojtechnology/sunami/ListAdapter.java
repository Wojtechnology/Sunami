package com.wojtechnology.sunami;

import android.content.Context;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import java.util.Collections;
import java.util.List;

/**
 * Created by wojtekswiderski on 15-04-12.
 */
public class ListAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private Context mContext;
    private TheBrain mTheBrain;

    private static int TYPE_HEADER = 1;
    private static int TYPE_LIST = 0;

    private LayoutInflater mInflater;
    List<FireMixtape> mData = Collections.emptyList();

    public ListAdapter(Context context, List<FireMixtape> data, TheBrain theBrain) {
        mContext = context;
        mInflater = LayoutInflater.from(mContext);
        mData = data;
        this.mTheBrain = theBrain;
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        if(viewType == TYPE_HEADER){
            View view = mInflater.inflate(R.layout.fire_header, parent, false);
            HeaderHolder holder = new HeaderHolder(view);
            return holder;
        } else {
            View view = mInflater.inflate(R.layout.fire_mixtape, parent, false);
            ItemHolder holder = new ItemHolder(view);
            return holder;
        }
    }

    @Override
    public void onBindViewHolder(final RecyclerView.ViewHolder holder, int position) {
        if(holder instanceof HeaderHolder){
            HeaderHolder headerHolder = (HeaderHolder) holder;
            final FireMixtape current = mData.get(position);
            headerHolder.label.setText(current.title);
        }else{
            final ItemHolder itemHolder = (ItemHolder) holder;
            final FireMixtape current = mData.get(position);
            itemHolder.title.setText(current.title);
            itemHolder.artist.setText(current.artist);
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

            itemHolder.background.setOnClickListener(new View.OnClickListener(){
                @Override
                public void onClick(View v) {
                    mTheBrain.playSong(current, true);
                }
            });
            itemHolder.addButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    mTheBrain.addSong(current);
                }
            });
        }
    }

    public void updateItem(FireMixtape song) {
        int i;
        for (i = 0; i < getItemCount(); i++) {
            if (song == mData.get(i)) {
                break;
            }
        }
        if (i < getItemCount()) {
            notifyItemChanged(i);
        }
    }

    @Override
    public int getItemViewType(int position) {
        if(mData.get(position).genre == "__header__"){
            return TYPE_HEADER;
        }else{
            return TYPE_LIST;
        }
    }

    @Override
    public int getItemCount() {
        return mData.size();
    }

    class ItemHolder extends RecyclerView.ViewHolder {
        private TextView title;
        private TextView artist;
        private TextView duration;
        private ImageView icon;
        private RelativeLayout background;
        private Button addButton;

        public ItemHolder(View itemView) {
            super(itemView);
            title = (TextView) itemView.findViewById(R.id.list_title);
            artist = (TextView) itemView.findViewById(R.id.list_artist);
            duration = (TextView) itemView.findViewById(R.id.list_duration);
            icon = (ImageView) itemView.findViewById(R.id.list_icon);
            background = (RelativeLayout) itemView.findViewById(R.id.list_background);
            addButton = (Button) itemView.findViewById(R.id.add_queue_button);
        }
    }

    class HeaderHolder extends RecyclerView.ViewHolder {
        private TextView label;

        public HeaderHolder(View itemView) {
            super(itemView);
            label = (TextView) itemView.findViewById(R.id.header_label);
        }
    }

    private String displayTime(String time){
        String newTime = "";
        int intTime = Integer.parseInt(time);
        if(intTime < 60000){
            newTime += "0";
        }else{
            newTime += Integer.toString(intTime / 60000);
            intTime = intTime % 60000;
        }
        newTime += ":";
        String seconds = Integer.toString(intTime / 1000);
        if(seconds.length() == 1){
            newTime += "0" + seconds;
        }else{
            newTime += seconds;
        }
        return newTime;
    }
}
