package com.wojtechnology.sunami;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
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
            ItemHolder itemHolder = (ItemHolder) holder;
            final FireMixtape current = mData.get(position);
            itemHolder.title.setText(current.title);
            itemHolder.artist.setText(current.artist);
            itemHolder.duration.setText(displayTime(current.duration));
            itemHolder.icon.setImageResource(R.mipmap.ic_launcher);
            itemHolder.background.setOnClickListener(new View.OnClickListener(){
                @Override
                public void onClick(View v) {
                    mTheBrain.playSong(current._id);
                }
            });
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

        public ItemHolder(View itemView) {
            super(itemView);
            title = (TextView) itemView.findViewById(R.id.list_title);
            artist = (TextView) itemView.findViewById(R.id.list_artist);
            duration = (TextView) itemView.findViewById(R.id.list_duration);
            icon = (ImageView) itemView.findViewById(R.id.list_icon);
            background = (RelativeLayout) itemView.findViewById(R.id.list_background);
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
