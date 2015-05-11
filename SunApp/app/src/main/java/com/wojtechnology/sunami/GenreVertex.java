package com.wojtechnology.sunami;

/**
 * Created by wojtekswiderski on 15-05-11.
 */
public class GenreVertex{
    private String genre;

    // Value that is short term
    private double shortValue;

    // Value that is long term
    private double longValue;

    public GenreVertex(String genre){
        this.genre = genre;
        this.shortValue = 0.0;
        this.longValue = 0.0;
    }

    public void add_values(double shortValue, double longValue){
        this.shortValue += shortValue;
        this.longValue += longValue;
    }

    public void reset_values(){
        this.shortValue = 0.0;
        this.longValue = 0.0;
    }

}
