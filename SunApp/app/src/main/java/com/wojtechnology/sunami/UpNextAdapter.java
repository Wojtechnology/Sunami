package com.wojtechnology.sunami;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import java.util.List;

/**
 * Created by wojtekswiderski on 15-05-26.
 */
public class UpNextAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private Context mContext;
    private TheBrain mTheBrain;

    private LayoutInflater mInflater;
    List<FireMixtape> mData;

    public UpNextAdapter(Context context, List<FireMixtape> data, TheBrain theBrain) {
        mContext = context;
        mInflater = LayoutInflater.from(context);
        mData = data;
        mTheBrain = theBrain;
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = mInflater.inflate(R.layout.fire_mixtape, parent, false);
        ItemHolder holder = new ItemHolder(view);
        return holder;
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        ItemHolder itemHolder = (ItemHolder) holder;
        final FireMixtape current = mData.get(position);
        itemHolder.title.setText(current.title);
        itemHolder.artist.setText(current.artist);
        itemHolder.duration.setText(displayTime(current.duration));
        itemHolder.icon.setImageResource(R.mipmap.ic_launcher);
        itemHolder.background.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {
                mTheBrain.playSong(current._id, true);
            }
        });
    }

    public void updateData(List<FireMixtape> data) {
        mData = data;
        notifyDataSetChanged();
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
