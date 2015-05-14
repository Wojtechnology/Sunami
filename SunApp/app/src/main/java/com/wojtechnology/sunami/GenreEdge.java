package com.wojtechnology.sunami;

/**
 * Created by wojtekswiderski on 15-05-11.
 */
public class GenreEdge {

    private GenreVertex from;
    private GenreVertex to;

    private double similarity;

    public GenreEdge(GenreVertex from, GenreVertex to, double similarity){
        this.from = from;
        this.to = to;
        this.similarity = similarity;
    }

}
