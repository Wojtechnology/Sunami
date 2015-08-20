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

import android.content.Context;

/**
 * Created by wojtekswiderski on 15-07-28.
 */
public class HeaderHelper {

    public static FireMixtape makeHeader(Context context, String s) {
        FireMixtape fireMixtape = new FireMixtape(context);
        fireMixtape.title = s;
        fireMixtape.genre = "__header__";
        return fireMixtape;
    }

    public static FireMixtape makeSoundcloudHeader(Context context, String s) {
        FireMixtape fireMixtape = new FireMixtape(context);
        fireMixtape.title = s;
        fireMixtape.genre = "__headersoundcloud__";
        return fireMixtape;
    }

    public static FireMixtape makeFinal(Context context, String s) {
        FireMixtape fireMixtape = new FireMixtape(context);
        fireMixtape.title = s;
        fireMixtape.genre = "__final__";
        return fireMixtape;
    }

    public static FireMixtape makeSoundcloudFinal(Context context, String s) {
        FireMixtape fireMixtape = new FireMixtape(context);
        fireMixtape.title = s;
        fireMixtape.genre = "__finalsoundcloud__";
        return fireMixtape;
    }

}
