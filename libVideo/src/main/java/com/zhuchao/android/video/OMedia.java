package com.zhuchao.android.video;

import android.content.Context;
import android.content.res.AssetFileDescriptor;
import android.view.SurfaceView;
import android.view.TextureView;

import com.zhuchao.android.callbackevent.PlayerCallBackInterface;
import com.zhuchao.android.playerutil.MyOPlayer;
import com.zhuchao.android.playerutil.OplayerUtil;

import java.io.Serializable;
import java.util.ArrayList;

/*
 *
 * */

public class OMedia implements Serializable {
    static final long serialVersionUID = 727566175075960653L;
    private Movie mMovie = null;
    private MyOPlayer mOPlayer = null; //单例
    private ArrayList<String> mOptions = null;
    private PlayerCallBackInterface mCallback = null;
    private OMedia mPreOMedia = null;
    private OMedia mNextOMedia = null;

    public OMedia(Movie mMovie) {
        this.mMovie = mMovie;
    }

    public OMedia(ArrayList<String> Options, PlayerCallBackInterface Callback) {
        this.mOptions = Options;
        this.mCallback = Callback;
    }

    public OMedia(Movie mMovie, ArrayList<String> mOptions, PlayerCallBackInterface mCallback) {
        this.mMovie = mMovie;
        this.mOptions = mOptions;
        this.mCallback = mCallback;
    }

    public OMedia(AssetFileDescriptor afd, ArrayList<String> Options, PlayerCallBackInterface Callback) {
        this.mOptions = Options;
        this.mCallback = Callback;
        //this.mMovie = new Movie(afd);
    }

    public OMedia(String VideoPath, ArrayList<String> Options, PlayerCallBackInterface Callback) {
        this.mOptions = Options;
        this.mCallback = Callback;
        this.mMovie = new Movie(VideoPath);
    }

    public OMedia getmPreOMedia() {
        return mPreOMedia;
    }

    public void setmPreOMedia(OMedia mPreOMedia) {
        this.mPreOMedia = mPreOMedia;
    }

    public OMedia getmNextOMedia() {
        return mNextOMedia;
    }

    public void setmNextOMedia(OMedia mNextOMedia) {
        this.mNextOMedia = mNextOMedia;
    }

    public Movie getmMovie() {
        return mMovie;
    }

    public void setmMovie(Movie mMovie) {
        this.mMovie = mMovie;
    }

    public MyOPlayer getmOPlayer() {
        return mOPlayer;
    }

    public ArrayList<String> getmOptions() {
        return mOptions;
    }

    public void setmOptions(ArrayList<String> mOptions) {
        this.mOptions = mOptions;
    }

    public void setCallback(PlayerCallBackInterface mCallback) {
        this.mCallback = mCallback;
        if (mOPlayer != null)
            mOPlayer.setCallback(this.mCallback);
    }

    public OMedia with(Context mContext) {
        mOPlayer = OplayerUtil.getOPlayer(mContext, mOptions, mCallback);
        return this;
    }

    public void callback(PlayerCallBackInterface mCallback) {
        setCallback(mCallback);
    }

    public OMedia playInto(SurfaceView playView) {
        mOPlayer.setSurfaceView(playView);
        if (mMovie != null)
            if (mMovie.getSourceUrl() != null)
                mOPlayer.play(mMovie.getSourceUrl());
        return this;
    }

    public OMedia playOn(SurfaceView playView) {
        mOPlayer.setSurfaceView(playView);
        if (mMovie != null)
            if (mMovie.getSourceUrl() != null)
                mOPlayer.play(mMovie.getSourceUrl());
        return this;
    }

    public OMedia playOn(TextureView playView) {
        mOPlayer.setTextureView(playView);
        if (mMovie != null)
            if (mMovie.getSourceUrl() != null)
                mOPlayer.play(mMovie.getSourceUrl());
        return this;
    }




    public void stopPlayer() {
        if (mOPlayer != null) {
            mOPlayer = null;
            OplayerUtil.FreeAndNullPlayer();
        }
    }

    public boolean isPlaying() {
        if (mOPlayer != null)
            return mOPlayer.isPlaying();
        return false;
    }

    public int getOPlayState()
    {
        if (mOPlayer != null)
            return mOPlayer.getPlayerState();
        else
            return 0;
    }

}
