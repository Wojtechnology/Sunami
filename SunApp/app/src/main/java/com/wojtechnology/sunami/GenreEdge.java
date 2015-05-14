package com.wojtechnology.sunami;

/**
 * Created by wojtekswiderski on 15-05-11.
 */
public class GenreEdge {

    private String from;
    private String to;

    private double similarity;

    public GenreEdge(String from, String to, double similarity){
        this.from = from;
        this.to = to;
        this.similarity = similarity;
    }

}
