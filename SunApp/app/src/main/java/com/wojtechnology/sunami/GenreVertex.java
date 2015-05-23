package com.wojtechnology.sunami;

/**
 * Created by wojtekswiderski on 15-05-11.
 */
public class GenreVertex{
    public String genre;

    // Value that is short term
    public double shortTerm;

    // Value that is long term
    public double longTerm;

    public GenreVertex(String genre, double shortTerm, double longTerm){
        this.genre = genre;
        this.shortTerm = shortTerm;
        this.longTerm = longTerm;
    }

    public void add_values(double shortTerm, double longTerm){
        this.shortTerm += shortTerm;
        this.longTerm += longTerm;
    }

    public void reset_values(){
        this.shortTerm = 0.0;
        this.longTerm = 0.0;
    }

}