package com.wojtechnology.sunami;

import android.content.ContentValues;
import android.content.Context;
import android.content.SharedPreferences;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import com.wojtechnology.sunami.archiveJava.GenreBase;
import com.wojtechnology.sunami.archiveJava.GenreDBHelper;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.StringWriter;
import java.io.UnsupportedEncodingException;
import java.io.Writer;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.json.*;


/**
 * Created by wojtekswiderski on 15-05-11.
 */
public class GenreContainer {

    private Context context;
    private Map<String, GenreVertex> mGenreRef;
    private Map<GenreVertex, List<GenreEdge>> mEdges;
    private GenreDBHelper mDB;

    public GenreContainer(Context context){
        this.mDB = new GenreDBHelper(context);
        this.mEdges = new HashMap<>();
        this.mGenreRef = new HashMap<>();
        this.context = context;
        populateGraph();
    }

    // Load data for genres.json resource
    public void populateGraph() {
        long startTime = Calendar.getInstance().getTimeInMillis();
        InputStream is;
        try {
            is = context.openFileInput("genresStore.json");
            Log.e("GenreContainer", "Open existing");
        } catch (FileNotFoundException e) {
            is = context.getResources().openRawResource(R.raw.genres);
            Log.e("GenreContainer", "Open new");
        }
        try {
            BufferedReader reader = new BufferedReader(new InputStreamReader(is));
            String line = reader.readLine();
            String jString = "";
            while (line != null) {
                jString += line;
                line = reader.readLine();
            }
            reader.close();
            is.close();
            JSONArray ja = new JSONArray(jString);
            for(int i = 0; i < ja.length(); i++){
                JSONObject jo = ja.getJSONObject(i);
                String genre = jo.getString("genre");
                GenreVertex gv = new GenreVertex(genre, jo.getDouble("st"), jo.getDouble("lt"));
                mGenreRef.put(genre, gv);
            }
            for(int i = 0; i < ja.length(); i++){
                GenreVertex gv = mGenreRef.get(ja.getJSONObject(i).getString("genre"));
                List<GenreEdge> edgeList = new ArrayList<>();
                JSONArray subGenres = ja.getJSONObject(i).getJSONArray("assoc");
                for(int j = 0; j < subGenres.length(); j++){
                    GenreEdge subGenre = new GenreEdge(gv,
                            mGenreRef.get(subGenres.getJSONObject(j).getString("name")),
                            subGenres.getJSONObject(j).getDouble("similarity"));
                    edgeList.add(subGenre);
                }
                mEdges.put(gv, edgeList);
            }
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (JSONException e) {
            e.printStackTrace();
        }
        Log.i("GenreContainer: ", "Finished populateGraph() in " +
                Long.toString(Calendar.getInstance().getTimeInMillis() - startTime) +
                " millis.");
    }

    public void saveGraph(){
        long startTime = Calendar.getInstance().getTimeInMillis();
        FileOutputStream outputStream;
        try {
            FileOutputStream fileOS = context.openFileOutput(
                    "genresStore.json", Context.MODE_PRIVATE);
            JSONArray ja = new JSONArray();
            Set<GenreVertex> vertices = mEdges.keySet();
            for (GenreVertex vertex : vertices){
                List<GenreEdge> edgeList = mEdges.get(vertex);
                JSONObject jo = new JSONObject();
                jo.put("genre", vertex.genre);
                jo.put("lt", vertex.longTerm);
                jo.put("st", vertex.shortTerm);
                JSONArray assocList = new JSONArray();
                for(GenreEdge edge : edgeList){
                    JSONObject assocJO = new JSONObject();
                    assocJO.put("name", edge.to.genre);
                    assocJO.put("similarity", edge.similarity);
                    assocList.put(assocJO);
                }
                jo.put("assoc", assocList);
                ja.put(jo);
            }
            fileOS.write(ja.toString().getBytes());
            fileOS.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (JSONException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        Log.i("GenreContainer: ", "Finished saveGraph() in " +
                Long.toString(Calendar.getInstance().getTimeInMillis() - startTime) +
                " millis.");
    }

    // Implement the update value method for a positive change in a genre
    // Will need more params
    public void promoteGenre(String genre){
        // LOL
    }

    // Implement the update value method for a negative change in a genre
    // Will need more params
    public void demoteGenre(String genre){
        // LOL
    }

    public double getGenreST(String genre){
        return mGenreRef.get(genre).shortTerm;
    }

    public double getGenreLT(String genre){
        return mGenreRef.get(genre).longTerm;
    }
}