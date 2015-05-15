package com.wojtechnology.sunami;

/**
 * Created by wojtekswiderski on 15-05-11.
 */
public class GenreEdge {

    public GenreVertex from;
    public GenreVertex to;

    public double similarity;

    public GenreEdge(GenreVertex from, GenreVertex to, double similarity){
        this.from = from;
        this.to = to;
        this.similarity = similarity;
    }

}
