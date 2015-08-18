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
