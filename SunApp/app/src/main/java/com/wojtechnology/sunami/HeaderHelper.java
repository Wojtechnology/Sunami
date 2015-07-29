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
