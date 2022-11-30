package com.zhuchao.android.player;

import android.content.Context;

import com.zhuchao.android.eventinterface.PlayerCallback;

import java.util.ArrayList;


public class PlayerManager {
    private static PlayControl FOPlayer = null;
    private static PlayControl FMPlayer = null;
    public static final String MPLAYER = "MPlayer";
    public static final String OPLAYER = "OPlayer";

    public synchronized static PlayControl getSingleOPlayer(Context context, PlayerCallback callback) {
        if (FOPlayer == null) {//不可以注释非常重要，全局单例对象
            FOPlayer = new OPlayer(context, callback);
        }
        return FOPlayer;
    }

    public synchronized static PlayControl getSingleOPlayer(Context context, ArrayList<String> options, PlayerCallback callback) {
        if (FOPlayer == null) {
            FOPlayer = new OPlayer(context, options, callback);
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
            FMPlayer = new MPlayer(context, callback);
        }
        return FMPlayer;
    }

    public synchronized static PlayControl getMultiMPlayer(Context context, PlayerCallback callback) {
        return new MPlayer(context, callback);
    }

    public synchronized static void free() {
        if (FOPlayer != null)
            FOPlayer.free();
        FOPlayer = null;

        if (FMPlayer != null)
            FMPlayer.free();
        FMPlayer = null;
    }
}