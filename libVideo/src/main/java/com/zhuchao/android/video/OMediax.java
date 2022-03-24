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

public class OMediax implements Serializable {
    static final long serialVersionUID = 727566175075960653L;
    private Movie mMovie = new Movie(null);
    private OPlayer mOPlayer = null; //单例
    private PlayerCallback mCallback = null;
    private OMedia mPreOMedia = null;
    private OMedia mNextOMedia = null;

    public OMediax() {
    }

    public OMediax(Movie movie) {
        if (movie != null)
            this.mMovie = movie;
    }

    public OMediax(String path) {
        this.mMovie = new Movie(path);
    }

    public void callback(PlayerCallback mCallback) {
        setCallback(mCallback);
        return;
    }

    public OMediax with(Context context) {
        if (mOPlayer == null)
            mOPlayer = PlayerUtil.getMultiOPlayer(context, mCallback);
        return this;
    }

    public OMediax with(Context context, ArrayList<String> options) {
        if (mOPlayer == null)
            mOPlayer = PlayerUtil.getMultiOPlayer(context, options, mCallback);
        return this;
    }

    public OMediax play() {
        mOPlayer.setSource(mMovie.getsUrl());
        mOPlayer.play();
        return this;
    }

    public OMediax play(String path) {
        mMovie.setsUrl(path);
        mOPlayer.setSource(path);
        mOPlayer.play();
        return this;
    }

    public OMediax play(Uri uri) {
        mMovie.setsUrl(uri.getPath());
        mOPlayer.setSource(uri);
        mOPlayer.play();
        return this;
    }

    public OMediax play(FileDescriptor fd) {
        mOPlayer.setSource(fd);
        mOPlayer.play();
        return this;
    }

    public OMediax play(AssetFileDescriptor afd) {
        mOPlayer.setSource(afd);
        mOPlayer.play();
        return this;
    }

    public OMediax playOn(SurfaceView playView) {
        mOPlayer.setSurfaceView(playView);
        mOPlayer.setSource(mMovie.getsUrl());
        mOPlayer.play();
        return this;
    }

    public OMediax playOn(TextureView playView) {
        mOPlayer.setTextureView(playView);
        mOPlayer.setSource(mMovie.getsUrl());
        mOPlayer.play();
        return this;
    }

    public void playPause() {
        mOPlayer.playPause();
    }

    public void pause() {
        mOPlayer.pause();
    }

    public void stop() {
        mOPlayer.stop();
    }

    public void setNoAudio() {
        mOPlayer.setNoAudio();
    }

    public void setVolume(int var1) {
        mOPlayer.setVolume(var1);
    }


    public boolean isPlaying() {
        if (mOPlayer != null)
            return mOPlayer.isPlaying();
        return false;
    }

    public int getPlayState() {
        if (mOPlayer != null)
            return mOPlayer.getPlayerState();
        else
            return 0;
    }


    public void free() {
        if (mOPlayer != null) {
            mOPlayer.free();
            mOPlayer = null;
        }
    }


    public void setCallback(PlayerCallback callBack) {
        this.mCallback = callBack;
        if (mOPlayer != null)
            mOPlayer.setCallback(this.mCallback);
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

    //public OPlayer getOPlayer() {
    //     return mOPlayer;
    //}

}
