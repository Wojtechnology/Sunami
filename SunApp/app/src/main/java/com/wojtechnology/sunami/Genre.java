package com.wojtechnology.sunami;

/**
 * Created by wojtekswiderski on 15-04-18.
 */

// com.wojtechnology.sunami.Genre class used for sorting songs by genre
public class Genre {

    private String _id;
    private String name;

    public Genre(String _id, String name){
        this._id = _id;
        this.name = name;
    }

    public String getName(){
        return this.name;
    }
}
