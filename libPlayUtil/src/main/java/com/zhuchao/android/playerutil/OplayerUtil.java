package com.zhuchao.android.playerutil;

import android.content.Context;

import com.zhuchao.android.callbackevent.PlayerCallBackInterface;

import org.videolan.libvlc.LibVLC;

import java.util.ArrayList;


public class OplayerUtil {
    private static LibVLC libVLC = null;
    private static MyOPlayer mMyOPlayer =null;

    public synchronized static LibVLC getLibVLC(Context context, ArrayList<String> options) throws IllegalStateException {
        if (libVLC == null) {
            if (options == null) {
                libVLC = new LibVLC(context);
            } else {
                libVLC = new LibVLC(context,options);
            }
        }
        return libVLC;
    }

    public synchronized static MyOPlayer getOPlayer(Context context, ArrayList<String> Options, PlayerCallBackInterface callback) throws IllegalStateException {
        if (mMyOPlayer == null) {
            mMyOPlayer = new MyOPlayer(context,Options,callback);
        }
        return mMyOPlayer;
    }

    public synchronized static void FreeAndNullAll()
    {
        if(mMyOPlayer != null)
            mMyOPlayer.Free();
        if(libVLC != null)
        libVLC.release();
        mMyOPlayer =null;
        libVLC = null;
    }

    public synchronized static void FreeAndNullPlayer()
    {

        try {
            if(mMyOPlayer != null) {
                mMyOPlayer.Free();
            }
            mMyOPlayer = null;
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}