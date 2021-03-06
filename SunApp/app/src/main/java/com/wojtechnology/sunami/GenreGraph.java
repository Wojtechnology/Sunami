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
import android.util.Log;

import java.util.ArrayList;
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

    private final double SHORT_GENRE_MIN = 1.0;
    private final double SHORT_GENRE_MAX = 2.0;
    private final double SHORT_GENRE_MED_MULTI = 0.1;
    private final double SHORT_GENRE_OFF_MULTI = 1.0;
    private final double SHORT_GENRE_POS_MULTI = 0.4;
    private final double SHORT_GENRE_NEG_MULTI = 0.4;
    private final double LONG_GENRE_MIN = 1.0;
    private final double LONG_GENRE_MAX = 1.2;
    private final double LONG_GENRE_MED_MULTI = 0.01;
    private final double LONG_GENRE_OFF_MULTI = 0.01;
    private final double LONG_GENRE_POS_MULTI = 1.0;
    private final double LONG_GENRE_NEG_MULTI = 1.0;

    public GenreGraph(Context context) {
        this.mEdges = new HashMap<>();
        this.mGenreRef = new HashMap<>();
        this.context = context;
    }

    public void populateGraphJSON(JSONArray ja) {
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
    }

    public JSONArray getGraphJSON() {
        try {
            JSONArray ja = new JSONArray();
            Set<GenreVertex> vertices = mEdges.keySet();
            for (GenreVertex vertex : vertices) {
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
            return ja;
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return new JSONArray();
    }

    public Set<String> getGenreSet () {
        return mGenreRef.keySet();
    }

    // Implement the update value method for a change in a genre
    public void modifyGenre(String genreString, double r) {
        try {
            GenreVertex genre = mGenreRef.get(genreString);

            double stDelta = shortTermGenreChange(genre, r);
            double stVal = genre.shortTerm + stDelta;

            if (stVal > SHORT_GENRE_MAX) {
                stVal = SHORT_GENRE_MAX;
            } else if (stVal < SHORT_GENRE_MIN) {
                stVal = SHORT_GENRE_MIN;
            }
            genre.shortTerm = stVal;

            double ltDelta = longTermGenreChange(genre, r);
            double ltVal = genre.longTerm + ltDelta;

            if (ltVal > LONG_GENRE_MAX) {
                ltVal = LONG_GENRE_MAX;
            } else if (ltVal < LONG_GENRE_MIN) {
                ltVal = LONG_GENRE_MIN;
            }
            genre.longTerm = ltVal;
            branchGenres(genre, r);
        } catch (Exception e) {
            Log.e("GenreGraph", "Could not find genre " + genreString);
        }
    }

    // Can be made into recursive for future
    private void branchGenres(GenreVertex genre, double r) {
        List<GenreEdge> edges = mEdges.get(genre);

        for (int i = 0; i < edges.size(); i++) {
            GenreEdge edge = edges.get(i);

            double stDelta = shortTermGenreChange(edge.to, r);
            double stVal = edge.to.shortTerm + stDelta * edge.similarity;

            if (stVal > SHORT_GENRE_MAX) {
                stVal = SHORT_GENRE_MAX;
            } else if (stVal < SHORT_GENRE_MIN) {
                stVal = SHORT_GENRE_MIN;
            }
            edge.to.shortTerm = stVal;

            double ltDelta = longTermGenreChange(edge.to, r);
            double ltVal = edge.to.longTerm + ltDelta * edge.similarity;

            if (ltVal > LONG_GENRE_MAX) {
                ltVal = LONG_GENRE_MAX;
            } else if (ltVal < LONG_GENRE_MIN) {
                ltVal = LONG_GENRE_MIN;
            }
            edge.to.longTerm = ltVal;
        }
    }

    public double getGenreST(String genre) {
        return mGenreRef.get(genre).shortTerm;
    }

    public double getGenreLT(String genre) {
        return mGenreRef.get(genre).longTerm;
    }

    public double shortTermGenreChange(GenreVertex genre, double r) {
        double genreVal = genre.shortTerm;
        double med = 0.5 * (SHORT_GENRE_MIN + SHORT_GENRE_MAX);
        double spread = SHORT_GENRE_MAX - SHORT_GENRE_MIN;
        double offsetRatio = r < 0.0 ? 0.6 : 0.4;
        double multi = r < 0.0 ? SHORT_GENRE_NEG_MULTI : SHORT_GENRE_POS_MULTI;
        double offset = offsetRatio * (SHORT_GENRE_MAX - SHORT_GENRE_MIN) + SHORT_GENRE_MIN;
        double medVal = ShuffleController.getBellValue(genreVal, med, 1.0);
        double offVal = ShuffleController.getBellValue(genreVal, offset, spread);
        double fullVal = SHORT_GENRE_MED_MULTI * medVal +
                SHORT_GENRE_OFF_MULTI * offVal;
        return multi * fullVal * r;
    }

    public double longTermGenreChange(GenreVertex genre, double r) {
        double genreVal = genre.longTerm;
        double med = 0.5 * (LONG_GENRE_MIN + LONG_GENRE_MAX);
        double spread = LONG_GENRE_MAX - LONG_GENRE_MIN;
        double offsetRatio = r < 0.0 ? 0.6 : 0.4;
        double multi = r < 0.0 ? LONG_GENRE_NEG_MULTI : LONG_GENRE_POS_MULTI;
        double offset = offsetRatio * (LONG_GENRE_MAX - LONG_GENRE_MIN) + LONG_GENRE_MIN;
        double medVal = ShuffleController.getBellValue(genreVal, med, 1.0);
        double offVal = ShuffleController.getBellValue(genreVal, offset, spread);
        double fullVal = LONG_GENRE_MED_MULTI * medVal +
                LONG_GENRE_OFF_MULTI * offVal;
        return multi * fullVal * r;
    }

    public void associateGenre(FireMixtape song) {
        if (canEdit(song)) {
            Set<String> keys = mGenreRef.keySet();
            double max = (SHORT_GENRE_MAX + SHORT_GENRE_MIN) / 2.0;
            String maxGenre = song.actualGenre;
            for (String key : keys) {
                double stVal = mGenreRef.get(key).shortTerm;
                if (stVal > max) {
                    max = stVal;
                    maxGenre = key;
                }
            }
            song.actualGenre = maxGenre;
        }
    }

    public boolean canEdit(FireMixtape song) {
        return song.genre.equals(SongManager.DEFAULT_GENRE);
    }

    private void printGenres() {
        Set<GenreVertex> vertices = mEdges.keySet();
        for (GenreVertex vertex : vertices) {
            Log.i("GenreGraph", vertex.genre);
        }
    }

    // Checks if genre exists
    public boolean isGenre(String genre) {
        Set<GenreVertex> vertices = mEdges.keySet();
        for (GenreVertex vertex : vertices) {
            if(vertex.genre.equals(genre)) return true;
        }
        return false;
    }
}