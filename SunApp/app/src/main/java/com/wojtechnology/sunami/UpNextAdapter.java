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

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
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
        View view = mInflater.inflate(R.layout.up_next_mixtape, parent, false);
        ItemHolder holder = new ItemHolder(view);
        return holder;
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        ItemHolder itemHolder = (ItemHolder) holder;
        final FireMixtape current = mData.get(position);
        itemHolder.title.setText(current.title);
        itemHolder.artist.setText(current.artist);
        itemHolder.close.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mTheBrain.removeSong(current);
            }
        });
    }

    @Override
    public int getItemCount() {
        return mData.size();
    }

    class ItemHolder extends RecyclerView.ViewHolder {
        private TextView title;
        private TextView artist;
        private Button close;

        public ItemHolder(View itemView) {
            super(itemView);
            title = (TextView) itemView.findViewById(R.id.list_title);
            artist = (TextView) itemView.findViewById(R.id.list_artist);
            close = (Button) itemView.findViewById(R.id.close_song_button);
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
