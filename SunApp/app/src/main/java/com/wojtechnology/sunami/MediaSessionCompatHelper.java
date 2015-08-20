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

import android.media.RemoteControlClient;
import android.support.v4.media.session.MediaSessionCompat;
import android.support.v4.media.session.PlaybackStateCompat;

public class MediaSessionCompatHelper {

    public static void applyState(MediaSessionCompat session, PlaybackStateCompat playbackState) {
        session.setPlaybackState(playbackState);

        ensureTransportControls(session, playbackState);

    }

    private static void ensureTransportControls(MediaSessionCompat session, PlaybackStateCompat playbackState) {
        long actions = playbackState.getActions();
        Object remoteObj = session.getRemoteControlClient();
        if(actions != 0 && remoteObj != null) {

            int transportControls = 0;

            if((actions & PlaybackStateCompat.ACTION_SKIP_TO_PREVIOUS) != 0){
                transportControls |= RemoteControlClient.FLAG_KEY_MEDIA_PREVIOUS;
            }

            if((actions & PlaybackStateCompat.ACTION_REWIND) != 0){
                transportControls |= RemoteControlClient.FLAG_KEY_MEDIA_REWIND;
            }

            if((actions & PlaybackStateCompat.ACTION_PLAY) != 0){
                transportControls |= RemoteControlClient.FLAG_KEY_MEDIA_PLAY;
            }

            if((actions & PlaybackStateCompat.ACTION_PLAY_PAUSE) != 0){
                transportControls |= RemoteControlClient.FLAG_KEY_MEDIA_PLAY_PAUSE;
            }

            if((actions & PlaybackStateCompat.ACTION_PAUSE) != 0){
                transportControls |= RemoteControlClient.FLAG_KEY_MEDIA_PAUSE;
            }

            if((actions & PlaybackStateCompat.ACTION_STOP) != 0){
                transportControls |= RemoteControlClient.FLAG_KEY_MEDIA_STOP;
            }

            if((actions & PlaybackStateCompat.ACTION_FAST_FORWARD) != 0){
                transportControls |= RemoteControlClient.FLAG_KEY_MEDIA_FAST_FORWARD;
            }

            if((actions & PlaybackStateCompat.ACTION_SKIP_TO_NEXT) != 0){
                transportControls |= RemoteControlClient.FLAG_KEY_MEDIA_NEXT;
            }

            if((actions & PlaybackStateCompat.ACTION_SEEK_TO) != 0){
                transportControls |= RemoteControlClient.FLAG_KEY_MEDIA_POSITION_UPDATE;
            }

            if((actions & PlaybackStateCompat.ACTION_SET_RATING) != 0){
                transportControls |= RemoteControlClient.FLAG_KEY_MEDIA_RATING;
            }

            ((RemoteControlClient)remoteObj).setTransportControlFlags(transportControls);
        }
    }



}