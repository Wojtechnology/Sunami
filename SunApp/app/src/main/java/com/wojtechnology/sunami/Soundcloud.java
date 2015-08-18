package com.wojtechnology.sunami;

import android.content.Context;

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
                        if (!song.getString("streamable").equals("null") && song.getBoolean("streamable")) {
                            FireMixtape fireMixtape = new FireMixtape(mContext);
                            fireMixtape.title = song.getString("title");
                            fireMixtape.artist = song.getJSONObject("user").getString("username");
                            fireMixtape.duration = song.getString("duration");
                            fireMixtape.album_art_url = fetchArtwork(song);
                            fireMixtape.data = SoundcloudRestClient.generateGoodUrl(song.getString("stream_url"));
                            fireMixtape.permalink_url = song.getString("permalink_url");
                            fireMixtape.isSoundcloud = true;
                            if (!((MainActivity) mContext).mTheBrain.isSongInLibrary(fireMixtape)) {
                                fireMixtapeList.add(fireMixtape);
                            }
                        }
                    }
                    callback.callback(fireMixtapeList);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }

            private String fetchArtwork(JSONObject song) throws JSONException {
                String artwork = song.getString("artwork_url");
                if (artwork.equals("null")) {
                    artwork = song.getJSONObject("user").getString("avatar_url");
                }
                return artwork;
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, byte[] responseBody, Throwable error) {
            }
        });
    }
}
