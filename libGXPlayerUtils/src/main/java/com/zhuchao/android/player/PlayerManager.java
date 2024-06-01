package com.zhuchao.android.player;

import android.annotation.SuppressLint;
import android.content.Context;

import com.zhuchao.android.fbase.MMLog;
import com.zhuchao.android.fbase.eventinterface.PlayerCallback;

import java.util.ArrayList;


public class PlayerManager {
    private final static String TAG = "PlayerManager";
    @SuppressLint("StaticFieldLeak")
    private static PlayControl FOPlayer = null;
    @SuppressLint("StaticFieldLeak")
    private static PlayControl FMPlayer = null;
    public static final String MPLAYER = "MPlayer";
    public static final String OPLAYER = "OPlayer";

    public synchronized static PlayControl getSingleOPlayer(Context context, PlayerCallback callback) {
        if (FOPlayer == null) {//不可以注释非常重要，全局单例对象
            try {
                FOPlayer = new OPlayer(context, callback);
            } catch (Exception e) {
                ///throw new RuntimeException(e);
                MMLog.e(TAG, "getSingleOPlayer " + e.toString());
            }
        }
        return FOPlayer;
    }

    public synchronized static PlayControl getSingleOPlayer(Context context, ArrayList<String> options, PlayerCallback callback) {
        if (FOPlayer == null) {
            try {
                FOPlayer = new OPlayer(context, options, callback);
            } catch (Exception e) {
                ///throw new RuntimeException(e);
                MMLog.e(TAG, "getSingleOPlayer " + e.toString());
            }
        }
        return FOPlayer;
    }

    public synchronized static PlayControl getMultiOPlayer(Context context, PlayerCallback callback) {
        return new OPlayer(context, callback);
    }

    public synchronized static PlayControl getMultiOPlayer(Context context, ArrayList<String> options, PlayerCallback callback) {
        return new OPlayer(context, options, callback);
    }

    public synchronized static PlayControl getSingleMPlayer(Context context, PlayerCallback callback) {
        if (FMPlayer == null) {//重用已有的播放器
            try {
                FMPlayer = new MPlayer(context, callback);
            } catch (Exception e) {
                ///throw new RuntimeException(e);
                MMLog.e(TAG, "getSingleMPlayer " + e.toString());
            }
        }
        return FMPlayer;
    }

    public synchronized static PlayControl getMultiMPlayer(Context context, PlayerCallback callback) {
        return new MPlayer(context, callback);
    }

    public synchronized static void free() {
        if (FOPlayer != null) FOPlayer.free();
        FOPlayer = null;

        if (FMPlayer != null) FMPlayer.free();
        FMPlayer = null;
    }
}