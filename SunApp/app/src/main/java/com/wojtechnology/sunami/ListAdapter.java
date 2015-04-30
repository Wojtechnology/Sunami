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
public class ListAdapter extends RecyclerView.Adapter<ListAdapter.ListViewHolder> {

    private Context context;

    private LayoutInflater inflater;
    List<FireMixtape> data = Collections.emptyList();

    public ListAdapter(Context context, List<FireMixtape> data) {
        this.context = context;
        inflater = LayoutInflater.from(context);
        this.data = data;
    }

    @Override
    public ListViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = inflater.inflate(R.layout.fire_mixtape, parent, false);
        ListViewHolder holder = new ListViewHolder(view);
        return holder;
    }

    @Override
    public void onBindViewHolder(final ListViewHolder holder, int position) {
        final FireMixtape current = data.get(position);
        holder.title.setText(current.title);
        holder.artist.setText(current.artist);
        holder.icon.setImageResource(R.mipmap.ic_launcher);
        if(position % 2 == 1){
            int color = context.getResources().getColor(R.color.listOddColor);
            holder.background.setBackgroundColor(color);
        }else{
            int color = context.getResources().getColor(R.color.listEvenColor);
            holder.background.setBackgroundColor(color);
        }
    }

    @Override
    public int getItemCount() {
        return data.size();
    }

    class ListViewHolder extends RecyclerView.ViewHolder {
        private TextView title;
        private TextView artist;
        private ImageView icon;
        private LinearLayout background;

        public ListViewHolder(View itemView) {
            super(itemView);
            title = (TextView) itemView.findViewById(R.id.list_title);
            artist = (TextView) itemView.findViewById(R.id.list_artist);
            icon = (ImageView) itemView.findViewById(R.id.list_icon);
            background = (LinearLayout) itemView.findViewById(R.id.list_background);
        }
    }
}
