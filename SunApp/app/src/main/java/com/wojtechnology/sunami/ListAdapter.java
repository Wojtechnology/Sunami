package com.wojtechnology.sunami;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.Collections;
import java.util.List;

/**
 * Created by wojtekswiderski on 15-04-12.
 */
public class ListAdapter extends RecyclerView.Adapter<ListAdapter.ListViewHolder> {

    private LayoutInflater inflater;
    List<FireMixtape> data = Collections.emptyList();

    public ListAdapter(Context context, List<FireMixtape> data){
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
    public void onBindViewHolder(ListViewHolder holder, int position) {
        FireMixtape current = data.get(position);
        holder.title.setText(current.title);
        holder.icon.setImageResource(Integer.parseInt(current.icon_id));
    }

    @Override
    public int getItemCount() {
        return data.size();
    }

    class ListViewHolder extends RecyclerView.ViewHolder {
        private TextView title;
        private ImageView icon;
        public ListViewHolder(View itemView) {
            super(itemView);
            title = (TextView) itemView.findViewById(R.id.list_text);
            icon = (ImageView) itemView.findViewById(R.id.list_icon);
        }
    }
}
