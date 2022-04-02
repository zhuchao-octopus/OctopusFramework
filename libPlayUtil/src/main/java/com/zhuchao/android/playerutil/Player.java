package com.zhuchao.android.playerutil;

import android.content.Context;

import com.zhuchao.android.callbackevent.PlayerCallback;

import java.util.ArrayList;


public class Player {
    private static OPlayer FOPlayer = null;

    public synchronized static OPlayer getSingleOPlayer(Context context, PlayerCallback callback) {
        if (FOPlayer == null) {
            FOPlayer = new OPlayer(context, callback);
        }
        return FOPlayer;
    }

    public synchronized static OPlayer getSingleOPlayer(Context context, ArrayList<String> options, PlayerCallback callback) {
        if (FOPlayer == null) {
            FOPlayer = new OPlayer(context, options, callback);
        }
        return FOPlayer;
    }

    public synchronized static OPlayer getMultiOPlayer(Context context, PlayerCallback callback) {
        return new OPlayer(context, callback);
    }

    public synchronized static OPlayer getMultiOPlayer(Context context, ArrayList<String> options, PlayerCallback callback) {
        return new OPlayer(context, options, callback);
    }


}