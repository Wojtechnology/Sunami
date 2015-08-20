/*

    Sunami - An Android music player which knows what you want to listen to.
    Copyright (C) 2015 Wojtek Swiderski

    Sunami is free software: you can redistribute it and/or modify
    it under the terms of the GNU General Public License as published by
    the Free Software Foundation, either version 3 of the License, or
    (at your option) any later version.

    Sunami is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU General Public License for more details.

    The GNU General Public License can be found at the root of this repository.

    To contact me, email me at wojtek.technology@gmail.com

 */

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
