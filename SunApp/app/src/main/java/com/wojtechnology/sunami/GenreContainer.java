package com.wojtechnology.sunami;

import java.util.HashMap;
import java.util.List;
import java.util.Map;


/**
 * Created by wojtekswiderski on 15-05-11.
 */
public class GenreContainer {

    private Map<String, GenreVertex> genreReference;
    private Map<GenreVertex, List<GenreEdge>> edges;

    public GenreContainer(){
        genreReference = new HashMap<>();
        edges = new HashMap<>();
    }

    public void populateGraph(String url){

    }
}
