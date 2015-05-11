package com.wojtechnology.sunami;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedList;
import java.util.List;


/**
 * Created by wojtekswiderski on 15-05-11.
 */
public class GenreGraph {
    private List<GenreVertex> vertices;
    private List<GenreEdge> edges;
    private GenreVertex rootVertex;

    public GenreGraph(){
        vertices = new ArrayList<GenreVertex>();
        edges = new ArrayList<GenreEdge>();
    }

    public void populateGraph(String url){}
}
