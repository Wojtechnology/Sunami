package com.wojtechnology.sunami;

import android.content.Context;
import android.util.Log;

import com.loopj.android.http.AsyncHttpResponseHandler;
import com.loopj.android.http.RequestParams;

import org.apache.http.Header;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by wojtekswiderski on 15-07-23.
 */
public class Soundcloud {
    private Context mContext;

    public Soundcloud(MainActivity context) {
        mContext = context;
    }

    // Note that this is an asynchronous function
    public void getTracks(String q, final SoundcloudCallback callback) {

        RequestParams requestParams = new RequestParams();
        requestParams.put("q", q);
        requestParams.put("limit", 25);

        SoundcloudRestClient.get("/tracks", requestParams, new AsyncHttpResponseHandler() {
            @Override
            public void onSuccess(int statusCode, Header[] headers, byte[] responseBody) {
                try {
                    List<FireMixtape> fireMixtapeList = new ArrayList<>();
                    String response = new String(responseBody);
                    JSONArray songs = new JSONArray(response);
                    for (int i = 0; i < songs.length(); i++) {
                        JSONObject song = (JSONObject) songs.get(i);
                        FireMixtape fireMixtape = new FireMixtape(mContext);
                        fireMixtape.title = song.getString("title");
                        fireMixtape.artist = song.getJSONObject("user").getString("username");
                        fireMixtape.duration = song.getString("duration");
                        fireMixtape.data = SoundcloudRestClient.generateStreamUrl(song.getString("stream_url"));
                        fireMixtape.isSoundcloud = true;
                        fireMixtapeList.add(fireMixtape);
                    }
                    callback.callback(fireMixtapeList);
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
