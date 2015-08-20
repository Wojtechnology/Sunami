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

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.view.KeyEvent;

/**
 * Created by wojtekswiderski on 15-06-26.
 */
public class RemoteControlEventReceiver extends BroadcastReceiver{
    @Override
    public void onReceive(Context context, Intent intent) {
        if (Intent.ACTION_MEDIA_BUTTON.equals(intent.getAction())) {
            KeyEvent keyEvent = intent.getParcelableExtra(Intent.EXTRA_KEY_EVENT);
            if (keyEvent.getAction() == KeyEvent.ACTION_UP) {
                Intent serviceIntent = new Intent(context.getApplicationContext(), TheBrain.class);
                switch (keyEvent.getKeyCode()){
                    case (KeyEvent.KEYCODE_MEDIA_PLAY_PAUSE):
                        serviceIntent.setAction(TheBrain.TOGGLE_PLAY);
                        break;

                    case (KeyEvent.KEYCODE_MEDIA_NEXT):
                        serviceIntent.setAction(TheBrain.PLAY_NEXT);
                        break;

                    case (KeyEvent.KEYCODE_MEDIA_PREVIOUS):
                        serviceIntent.setAction(TheBrain.PLAY_LAST);
                        break;

                    default:
                        break;
                }
                context.getApplicationContext().startService(serviceIntent);
            }
        }
    }
}
