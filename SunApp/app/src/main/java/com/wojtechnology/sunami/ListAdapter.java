package com.wojtechnology.sunami;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import org.w3c.dom.Text;

import java.util.Collections;
import java.util.List;

/**
 * Created by wojtekswiderski on 15-04-12.
 */
public class ListAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private Context context;
    private ShuffleManager shuffleManager;

    private static int TYPE_HEADER = 1;
    private static int TYPE_LIST = 0;

    private LayoutInflater inflater;
    List<FireMixtape> data = Collections.emptyList();

    public ListAdapter(Context context, List<FireMixtape> data, ShuffleManager shuffleManager) {
        this.context = context;
        inflater = LayoutInflater.from(context);
        this.data = data;
        this.shuffleManager = shuffleManager;
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        if(viewType == TYPE_HEADER){
            View view = inflater.inflate(R.layout.fire_header, parent, false);
            HeaderHolder holder = new HeaderHolder(view);
            return holder;
        }else {
            View view = inflater.inflate(R.layout.fire_mixtape, parent, false);
            ItemHolder holder = new ItemHolder(view);
            return holder;
        }
    }

    @Override
    public void onBindViewHolder(final RecyclerView.ViewHolder holder, int position) {
        if(holder instanceof HeaderHolder){
            HeaderHolder headerHolder = (HeaderHolder) holder;
            final FireMixtape current = data.get(position);
            headerHolder.label.setText(current.title);
        }else{
            ItemHolder itemHolder = (ItemHolder) holder;
            final FireMixtape current = data.get(position);
            itemHolder.title.setText(current.title);
            itemHolder.artist.setText(current.artist);
            itemHolder.icon.setImageResource(R.mipmap.ic_launcher);
            itemHolder.background.setOnClickListener(new View.OnClickListener(){
                @Override
                public void onClick(View v) {
                    shuffleManager.playSong(current._id);
                }
            });
        }
    }

    @Override
    public int getItemViewType(int position) {
        if(data.get(position).genre == "__header__"){
            return TYPE_HEADER;
        }else{
            return TYPE_LIST;
        }
    }

    @Override
    public int getItemCount() {
        return data.size();
    }

    class ItemHolder extends RecyclerView.ViewHolder {
        private TextView title;
        private TextView artist;
        private ImageView icon;
        private LinearLayout background;

        public ItemHolder(View itemView) {
            super(itemView);
            title = (TextView) itemView.findViewById(R.id.list_title);
            artist = (TextView) itemView.findViewById(R.id.list_artist);
            icon = (ImageView) itemView.findViewById(R.id.list_icon);
            background = (LinearLayout) itemView.findViewById(R.id.list_background);
        }
    }

    class HeaderHolder extends RecyclerView.ViewHolder {
        private TextView label;

        public HeaderHolder(View itemView) {
            super(itemView);
            label = (TextView) itemView.findViewById(R.id.header_label);
        }
    }
}
