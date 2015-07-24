package com.wojtechnology.sunami;

import android.util.Log;

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
}
