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

import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.AsyncHttpResponseHandler;
import com.loopj.android.http.RequestParams;

/**
 * Created by wojtekswiderski on 15-07-23.
 */
public class SoundcloudRestClient {
    private static final String BASE_URL = "https://api.soundcloud.com";
    private static final String CLIENT_ID = "6544b86b0ff17f97aa2c1d1a2a6f37cf";

    private static AsyncHttpClient client = new AsyncHttpClient();

    public static void get(String url, RequestParams params, AsyncHttpResponseHandler responseHandler) {
        client.get(getAbsoluteUrl(url), addClientID(params), responseHandler);
    }

    private static String getAbsoluteUrl(String relativeUrl) {
        return BASE_URL + relativeUrl;
    }

    private static RequestParams addClientID(RequestParams params) {
        if (params == null) {
            params = new RequestParams();
        }
        params.put("client_id", CLIENT_ID);
        return params;
    }

    public static String generateGoodUrl(String stream) {
        return stream + "?client_id=" + CLIENT_ID;
    }
}
