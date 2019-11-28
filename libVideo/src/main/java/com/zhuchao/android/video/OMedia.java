package com.zhuchao.android.video;

import android.content.Context;
import android.content.res.AssetFileDescriptor;
import android.net.Uri;
import android.view.SurfaceView;
import android.view.TextureView;

import com.zhuchao.android.callbackevent.PlayerCallback;
import com.zhuchao.android.playerutil.OPlayer;
import com.zhuchao.android.playerutil.PlayerUtil;

import java.io.FileDescriptor;
import java.io.Serializable;
import java.util.ArrayList;

/*
 *
 * OMedia oMedia= new OMedia()
 * oMedia.with().play().callback();
 *
 * */

public class OMedia implements Serializable {
    static final long serialVersionUID = 727566175075960653L;
    private Movie mMovie = new Movie(null);
    //private OPlayer mOPlayer = null; //单例
    private PlayerCallback mCallback = null;
    private OMedia mPreOMedia = null;
    private OMedia mNextOMedia = null;
    private Context mContext = null;
    private ArrayList<String> mOptions = null;
    private float mPlayRate = 1;
    private int  PlayOrder=0;

    public OMedia() {
    }

    public OMedia(Context context) {
        mContext = context;
    }

    public OMedia(Movie movie) {
        if (movie != null)
            this.mMovie = movie;
    }

    public OMedia(String path) {
        this.mMovie = new Movie(path);
    }

    public void callback(PlayerCallback mCallback) {
        setCallback(mCallback);
        return;
    }

    public OMedia with(Context context) {
        mContext = context;
        //PlayerUtil.getSingleOPlayer(context, mCallback);
        return this;
    }

    public OMedia with(Context context, ArrayList<String> options) {
        mContext = context;
        mOptions = options;
        //PlayerUtil.getSingleOPlayer(context, options, mCallback);
        return this;
    }

    public OMedia play() {
        PlayerUtil.getSingleOPlayer(mContext, mOptions, mCallback);
        getOPlayer().setSource(mMovie.getSourceUrl());
        getOPlayer().play();
        return this;
    }

    public OMedia play(String path) {
        PlayerUtil.getSingleOPlayer(mContext, mOptions, mCallback);
        mMovie.setSourceUrl(path);
        getOPlayer().setSource(path);
        getOPlayer().play();
        return this;
    }

    public OMedia play(Uri uri) {
        PlayerUtil.getSingleOPlayer(mContext, mOptions, mCallback);
        mMovie.setSourceUrl(uri.getPath());
        getOPlayer().setSource(uri);
        getOPlayer().play();
        return this;
    }

    public OMedia play(FileDescriptor fd) {
        PlayerUtil.getSingleOPlayer(mContext, mOptions, mCallback);
        getOPlayer().setSource(fd);
        getOPlayer().play();
        return this;
    }

    public OMedia play(AssetFileDescriptor afd) {
        PlayerUtil.getSingleOPlayer(mContext, mOptions, mCallback);
        getOPlayer().setSource(afd);
        getOPlayer().play();
        return this;
    }

    public OMedia playOn(SurfaceView playView) {
        PlayerUtil.getSingleOPlayer(mContext, mOptions, mCallback);
        getOPlayer().setSurfaceView(playView);
        getOPlayer().setSource(mMovie.getSourceUrl());
        getOPlayer().play();
        return this;
    }

    public OMedia playOn(TextureView playView) {
        PlayerUtil.getSingleOPlayer(mContext, mOptions, mCallback);
        getOPlayer().setTextureView(playView);
        getOPlayer().setSource(mMovie.getSourceUrl());
        getOPlayer().play();
        return this;
    }

    public void playPause() {
        getOPlayer().playPause();
    }

    public void pause() {
        getOPlayer().pause();
    }

    public void stop() {

        try {
            getOPlayer().stop();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void setNoAudio() {
        getOPlayer().setNoAudio();
    }

    public void setVolume(int var1) {
        getOPlayer().setVolume(var1);
    }

    public void fastForward(int x) {
        getOPlayer().fastForward(x);
    }

    public void fastBack(int x) {
        getOPlayer().fastBack(x);
    }

    public void fastForward(long x) {
        getOPlayer().fastForward(x);
    }

    public void fastBack(long x) {
        getOPlayer().fastBack(x);
    }


    public void fastForward() {
        mPlayRate = mPlayRate + 0.1f;
        getOPlayer().setRate(mPlayRate);
    }
    public void fastBack() {
        mPlayRate = mPlayRate - 0.1f;
        if(mPlayRate<=0) mPlayRate =1;
        getOPlayer().setRate(mPlayRate);
    }

    public boolean isPlaying() {
        return getOPlayer().isPlaying();
    }

    public int getPlayState() {
        return getOPlayer().getPlayerState();
    }


    private void setCallback(PlayerCallback callBack) {
        this.mCallback = callBack;
        getOPlayer().setCallback(this.mCallback);
    }

    public OMedia getPreOMedia() {
        return mPreOMedia;
    }

    public void setPreOMedia(OMedia mPreOMedia) {
        this.mPreOMedia = mPreOMedia;
    }

    public OMedia getNextOMedia() {
        return mNextOMedia;
    }

    public void setNextOMedia(OMedia mNextOMedia) {
        this.mNextOMedia = mNextOMedia;
    }

    public Movie getMovie() {
        return mMovie;
    }

    public void setMovie(Movie mMovie) {
        this.mMovie = mMovie;
    }

    private OPlayer getOPlayer() {

        return PlayerUtil.getSingleOPlayer(mContext, mOptions, mCallback);
    }

    public float getPosition() {
        return getOPlayer().getCurrentPosition();
    }

    public long getLength() {
        return getOPlayer().getLength();
    }

    public void setNormalRage()
    {
        mPlayRate =1;
        getOPlayer().setRate(mPlayRate);
    }

    public int getPlayOrder() {
        return PlayOrder;
    }

    public OMedia setPlayOrder(int playOrder) {
        PlayOrder = playOrder;
        return this;
    }
    //public void setRate(float v) {
    //    getOPlayer().setRate(v);
    //}

}
