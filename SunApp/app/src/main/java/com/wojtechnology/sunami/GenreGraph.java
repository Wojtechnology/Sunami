package com.wojtechnology.sunami;

import android.content.Context;
import android.util.Log;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.json.*;


/**
 * Created by wojtekswiderski on 15-05-11.
 */
public class GenreGraph {

    private Context context;
    private Map<String, GenreVertex> mGenreRef;
    private Map<GenreVertex, List<GenreEdge>> mEdges;

    private boolean isPopulated;

    public GenreGraph(Context context) {
        this.mEdges = new HashMap<>();
        this.mGenreRef = new HashMap<>();
        this.context = context;
        isPopulated = false;
    }

    public void populateGraphJSON(JSONArray ja) {
        long startTime = Calendar.getInstance().getTimeInMillis();
        try {
            for (int i = 0; i < ja.length(); i++) {
                JSONObject jo = ja.getJSONObject(i);
                String genre = jo.getString("genre").toLowerCase();
                GenreVertex gv = new GenreVertex(genre, jo.getDouble("st"), jo.getDouble("lt"));
                mGenreRef.put(genre, gv);
            }
            for (int i = 0; i < ja.length(); i++) {
                GenreVertex gv = mGenreRef.get(ja.getJSONObject(i).getString("genre"));
                List<GenreEdge> edgeList = new ArrayList<>();
                JSONArray subGenres = ja.getJSONObject(i).getJSONArray("assoc");
                for (int j = 0; j < subGenres.length(); j++) {
                    GenreEdge subGenre = new GenreEdge(gv,
                            mGenreRef.get(subGenres.getJSONObject(j).getString("name").toLowerCase()),
                            subGenres.getJSONObject(j).getDouble("similarity"));
                    edgeList.add(subGenre);
                }
                mEdges.put(gv, edgeList);
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
        Log.i("GenreGraph", "Finished populateGraphJSON() in " +
                Long.toString(Calendar.getInstance().getTimeInMillis() - startTime) +
                " millis.");
        isPopulated = true;
    }

    public JSONArray getGraphJSON() {
        long startTime = Calendar.getInstance().getTimeInMillis();
        try {
            int numGenres = 0;
            JSONArray ja = new JSONArray();
            Set<GenreVertex> vertices = mEdges.keySet();
            for (GenreVertex vertex : vertices) {
                numGenres++;
                List<GenreEdge> edgeList = mEdges.get(vertex);
                JSONObject jo = new JSONObject();
                jo.put("genre", vertex.genre);
                jo.put("lt", vertex.longTerm);
                jo.put("st", vertex.shortTerm);
                JSONArray assocList = new JSONArray();
                for (GenreEdge edge : edgeList) {
                    JSONObject assocJO = new JSONObject();
                    assocJO.put("name", edge.to.genre);
                    assocJO.put("similarity", edge.similarity);
                    assocList.put(assocJO);
                }
                jo.put("assoc", assocList);
                ja.put(jo);
            }
            Log.i("GenreGraph", "Saved " + numGenres + " genres.");
            return ja;
        } catch (JSONException e) {
            e.printStackTrace();
        }
        Log.i("GenreGraph", "Finished getGraphJSON() in " +
                Long.toString(Calendar.getInstance().getTimeInMillis() - startTime) +
                " millis.");
        return new JSONArray();
    }

    public Set<String> getGenreSet () {
        return mGenreRef.keySet();
    }

    // Implement the update value method for a change in a genre
    public void modifyGenre(PlayInstance playInstance) {
        // LOL
    }

    public double getGenreST(String genre) {
        return mGenreRef.get(genre).shortTerm;
    }

    public double getGenreLT(String genre) {
        return mGenreRef.get(genre).longTerm;
    }

    private void printGenres() {
        Set<GenreVertex> vertices = mEdges.keySet();
        for (GenreVertex vertex : vertices) {
            Log.i("GenreGraph", vertex.genre);
        }
    }
}