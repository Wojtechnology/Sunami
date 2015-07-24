package com.wojtechnology.sunami;

import android.util.Log;

import com.loopj.android.http.AsyncHttpResponseHandler;
import com.loopj.android.http.JsonHttpResponseHandler;

import org.apache.http.Header;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.Collections;
import java.util.List;

/**
 * Created by wojtekswiderski on 15-07-23.
 */
public class Soundcloud {

    private MainActivity mContext;

    public Soundcloud(MainActivity context) {
        mContext = context;
    }

    // Note that this is an asynchronous function
    public void getTracks() {

        SoundcloudRestClient.get("/tracks", null, new AsyncHttpResponseHandler() {
            @Override
            public void onSuccess(int statusCode, Header[] headers, byte[] responseBody) {
                try {
                    String response = new String(responseBody);
                    JSONArray songs = new JSONArray(response);
                    for (int i = 0; i < songs.length(); i++) {
                        JSONObject song = (JSONObject) songs.get(i);
                        Log.e("Soundcloud", song.getString("title"));

                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, byte[] responseBody, Throwable error) {
                Log.e("Soundcloud", "Status: " + statusCode);
            }
        });
    }
}
