package com.wojtechnology.sunami;

import android.content.ContentValues;
import android.content.Context;
import android.content.SharedPreferences;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import com.wojtechnology.sunami.archiveJava.GenreBase;
import com.wojtechnology.sunami.archiveJava.GenreDBHelper;

import java.io.BufferedReader;
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
        InputStream is = context.getResources().openRawResource(R.raw.genres);
        Writer writer = new StringWriter();
        char[] buffer = new char[1024];
        try {
            Reader reader = new BufferedReader(new InputStreamReader(is, "UTF-8"));
            int n;
            while ((n = reader.read(buffer)) != -1){
                writer.write(buffer, 0, n);
            }
            is.close();
            String js = writer.toString();
            JSONObject jo = new JSONObject(js);
            Log.i("GenreContainer: ", "Read populateGraph() in " +
                    Long.toString(Calendar.getInstance().getTimeInMillis() - startTime) +
                    " millis.");
            startTime = Calendar.getInstance().getTimeInMillis();
            Iterator iterator = jo.keys();
            while(iterator.hasNext()){
                String genre = (String)iterator.next();
                GenreVertex gv = new GenreVertex(genre, 0.0, 0.0);
                mGenreRef.put(genre, gv);
            }
            Set<String> genres = mGenreRef.keySet();
            for(String genre : genres){
                List<GenreEdge> edgeList = new ArrayList<>();
                JSONArray subGenres = jo.getJSONArray(genre);
                for(int i = 0; i < subGenres.length(); i++){
                    GenreEdge subGenre = new GenreEdge(mGenreRef.get(genre),
                            mGenreRef.get(subGenres.getJSONObject(i).getString("name")),
                            subGenres.getJSONObject(i).getDouble("similarity"));
                    edgeList.add(subGenre);
                }
                mEdges.put(mGenreRef.get(genre), edgeList);
            }
            Set<GenreVertex> vertSet = mEdges.keySet();
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

    public void updateGenre(String genre){

    }

    public void getGenre(String genre){

    }
}