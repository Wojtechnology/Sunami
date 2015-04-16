package com.wojtechnology.sunami;

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import java.util.Collections;
import java.util.List;

/**
 * Created by wojtekswiderski on 15-04-12.
 */
public class ListAdapter extends RecyclerView.Adapter<ListAdapter.ListViewHolder> {

    private Context context;

    private LayoutInflater inflater;
    List<FireMixtape> data = Collections.emptyList();

    public ListAdapter(Context context, List<FireMixtape> data){
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
        if(!current.icon_loaded){
            new DisplayArtwork(current, holder, this.context).execute("");
        }
    }

    // Display artwork in the background
    private class DisplayArtwork extends AsyncTask<String, Void, String>{

        final private Context context;
        final private FireMixtape current;
        final private ListViewHolder holder;

        public DisplayArtwork(FireMixtape current, ListViewHolder holder, Context context){
            this.current = current;
            this.holder = holder;
            this.context = context;
        }

        @Override
        protected String doInBackground(String... params) {
            Bitmap icon = this.current.getAlbumArt();
            if(icon != null){
                final Bitmap send_icon = FireMixtape.scaleBitmap(icon); // to send into runnable
                // Updates UI on UI thread
                ((Activity)this.context).runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        holder.icon.setImageBitmap(send_icon);
                    }
                });
            }else{
                final int send_icon = this.current.icon_id; // to be able to send into runnable
                // Updates UI on UI thread
                ((Activity)this.context).runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        holder.icon.setImageResource(send_icon);
                    }
                });
            }
            current.icon_loaded = true;
            return "Executed";
        }

        @Override
        protected void onPostExecute(String result) {}
        @Override
        protected void onPreExecute() {}
        @Override
        protected void onProgressUpdate(Void... values) {}
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
