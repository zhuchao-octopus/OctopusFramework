package com.zhuchao.android.playerutil;

import android.content.Context;

import com.zhuchao.android.callbackevent.PlayerCallback;

import org.videolan.libvlc.LibVLC;

import java.util.ArrayList;


public class PlayerUtil {
    private static LibVLC libVLC = null;
    private static OPlayer mOPlayer = null;

    public synchronized static LibVLC getSingleLibVLC(Context context, ArrayList<String> options) throws IllegalStateException {
        if (libVLC == null) {
            if (options == null) {
                libVLC = new LibVLC(context);
            } else {
                libVLC = new LibVLC(context, options);
            }
        }
        return libVLC;
    }

    private synchronized static LibVLC getMultiLibVLC(Context context, ArrayList<String> options) throws IllegalStateException {

        if (options == null) {
            libVLC = new LibVLC(context);
        } else {
            libVLC = new LibVLC(context, options);
        }

        return libVLC;
    }

    public synchronized static OPlayer getSingleOPlayer(Context context, PlayerCallback callback) {
        if (mOPlayer == null) {
            mOPlayer = new OPlayer(context, callback);
        }
        return mOPlayer;
    }

    public synchronized static OPlayer getMultiOPlayer(Context context, PlayerCallback callback) {
        OPlayer mOPlayer = new OPlayer(context, callback);
        return mOPlayer;
    }


    public synchronized static OPlayer getSingleOPlayer(Context context, ArrayList<String> options, PlayerCallback callback) {
        if (mOPlayer == null) {
            mOPlayer = new OPlayer(context, options, callback);
        }
        return mOPlayer;
    }

    public synchronized static OPlayer getMultiOPlayer(Context context, ArrayList<String> options, PlayerCallback callback) {
        OPlayer mOPlayer = new OPlayer(context, options, callback);
        return mOPlayer;
    }

    public synchronized static void FreeSinglePlayer() {
        if (mOPlayer != null)
            mOPlayer.free();
        mOPlayer = null;
    }
    public synchronized static void FreeSingleVLC() {
        if (libVLC != null)
            libVLC.release();
        libVLC = null;
    }
}