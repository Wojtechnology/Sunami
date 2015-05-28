package com.wojtechnology.sunami;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import java.util.Collections;
import java.util.List;
import java.util.Queue;

/**
 * Created by wojtekswiderski on 15-05-26.
 */
public class UpNextAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private Context context;
    private TheBrain theBrain;

    private LayoutInflater inflater;
    Queue<FireMixtape> data;

    public UpNextAdapter(Context context, Queue<FireMixtape> data, TheBrain theBrain) {
        this.context = context;
        inflater = LayoutInflater.from(context);
        this.data = data;
        this.theBrain = theBrain;
    }


    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        return null;
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {

    }

    @Override
    public int getItemCount() {
        return data.size();
    }
}
