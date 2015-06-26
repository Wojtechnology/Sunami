package com.wojtechnology.sunami;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

/**
 * Created by wojtekswiderski on 15-06-26.
 */
public class RemoteControlEventReceiver extends BroadcastReceiver{
    @Override
    public void onReceive(Context context, Intent intent) {
        Log.e("TheBrain", "Got the shit");
    }
}
