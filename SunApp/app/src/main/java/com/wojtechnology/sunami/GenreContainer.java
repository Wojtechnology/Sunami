package com.wojtechnology.sunami;

import android.content.Context;
import android.util.Log;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.StringWriter;
import java.io.UnsupportedEncodingException;
import java.io.Writer;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.json.*;


/**
 * Created by wojtekswiderski on 15-05-11.
 */
public class GenreContainer {

    private Context context;

    private Map<String, GenreVertex> genreReference;
    private Map<GenreVertex, List<GenreEdge>> edges;

    public GenreContainer(Context context){
        genreReference = new HashMap<>();
        edges = new HashMap<>();
        this.context = context;
    }

    public void populateGraph(String url){
        InputStream is = context.getResources().openRawResource(R.raw.genres);
        Writer writer = new StringWriter();
        char[] buffer = new char[1024];
        try {
            Reader reader = new BufferedReader(new InputStreamReader(is, "UTF-8"));
            int n;
            while ((n = reader.read(buffer)) != -1){
                writer.write(buffer, 0, n);
            }
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                is.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        String jsonString = writer.toString();
        Log.e("Hello: ", jsonString);
    }
}
