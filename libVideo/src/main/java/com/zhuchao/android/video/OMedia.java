package com.zhuchao.android.video;

import android.content.Context;
import android.content.res.AssetFileDescriptor;
import android.net.Uri;
import android.view.SurfaceView;
import android.view.TextureView;

import com.zhuchao.android.callbackevent.NormalRequestCallback;
import com.zhuchao.android.callbackevent.PlayerCallback;
import com.zhuchao.android.databaseutil.SPreference;
import com.zhuchao.android.libfilemanager.FilesManager;
import com.zhuchao.android.netutil.OkHttpUtils;
import com.zhuchao.android.playerutil.OPlayer;
import com.zhuchao.android.playerutil.PlayerUtil;

import java.io.FileDescriptor;
import java.io.Serializable;
import java.util.ArrayList;

import static com.zhuchao.android.playerutil.PlayerUtil.isOplayerReady;

/*
 *
 * OMedia oMedia= new OMedia()
 * oMedia.with().play().callback();
 *
 * */

public class OMedia implements Serializable, PlayerCallback {
    static final long serialVersionUID = 727566175075960653L;
    private Movie mMovie = null;//new Movie(null);
    //private OPlayer mOPlayer = null; //单例
    private PlayerCallback mCallback = null;
    private OMedia mPreOMedia = null;
    private OMedia mNextOMedia = null;
    private Context mContext = null;
    private ArrayList<String> mOptions = null;
    private float mPlayRate = 1;
    private int mPlayOrder = 0;
    private long mLastPlayTime = -1;

    public OMedia() {
    }

    public OMedia(Context context) {
        mContext = context;
    }

    public OMedia(Movie movie) {
        if (movie != null)
            this.mMovie = movie;
    }

    public OMedia(final String path) {

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

    public OMedia playCache(String cachDir) {
        //PlayerUtil.getSingleOPlayer(mContext, mOptions, mCallback);
        getOPlayer().setSource(getAvailablePath(cachDir));
        getOPlayer().play();
        return this;
    }

    public OMedia play(String path) {
        //PlayerUtil.getSingleOPlayer(mContext, mOptions, mCallback);
        mMovie.setSourceUrl(path);
        getOPlayer().setSource(path);
        getOPlayer().play();
        return this;
    }

    public OMedia play(Uri uri) {
        //PlayerUtil.getSingleOPlayer(mContext, mOptions, mCallback);
        mMovie.setSourceUrl(uri.getPath());
        getOPlayer().setSource(uri);
        getOPlayer().play();
        return this;
    }

    public OMedia play(FileDescriptor fd) {
        //PlayerUtil.getSingleOPlayer(mContext, mOptions, mCallback);
        getOPlayer().setSource(fd);
        getOPlayer().play();
        return this;
    }

    public OMedia play(AssetFileDescriptor afd) {
        //PlayerUtil.getSingleOPlayer(mContext, mOptions, mCallback);
        getOPlayer().setSource(afd);
        getOPlayer().play();
        return this;
    }

    public OMedia playOn(SurfaceView playView, String CathPath) {
        //PlayerUtil.getSingleOPlayer(mContext, mOptions, mCallback);

        getOPlayer().setSurfaceView(playView);
        getOPlayer().setSource(getAvailablePath(CathPath));
        getOPlayer().play();
        return this;
    }

    public OMedia playOn(TextureView playView) {
        //PlayerUtil.getSingleOPlayer(mContext, mOptions, mCallback);
        //String sl = getAvailablePath();

        getOPlayer().setTextureView(playView);
        getOPlayer().setSource(getAvailablePath(null));
        getOPlayer().play();
        return this;
    }

    public void playPause() {
        if (isOplayerReady())
            getOPlayer().playPause();
    }

    public void pause() {
        if (isOplayerReady())
            getOPlayer().pause();
    }

    public void stop() {
        try {
            if (isOplayerReady()) {
                mLastPlayTime = gettime();
                //getOPlayer().stop();
                PlayerUtil.FreeSinglePlayer();
                //PlayerUtil.FreeSinglePlayer();
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public long getmLastPlayTime() {
        return mLastPlayTime;
    }

    public OMedia setmLastPlayTime(long mLastPlayTime) {
        this.mLastPlayTime = mLastPlayTime;
        return this;
    }

    public void setNoAudio() {
        if (isOplayerReady())
            getOPlayer().setNoAudio();
    }

    public void setVolume(int var1) {
        if (isOplayerReady())
            getOPlayer().setVolume(var1);
    }

    public void fastForward(int x) {
        if (isOplayerReady())
            getOPlayer().fastForward(x);
    }

    public void fastBack(int x) {
        if (isOplayerReady())
            getOPlayer().fastBack(x);
    }

    public void fastForward(long x) {
        if (isOplayerReady())
            getOPlayer().fastForward(x);
    }

    public void fastBack(long x) {
        if (isOplayerReady())
            getOPlayer().fastBack(x);
    }


    public void fastForward() {
        if (isOplayerReady()) {
            mPlayRate = mPlayRate + 0.1f;
            getOPlayer().setRate(mPlayRate);
        }
    }

    public void fastBack() {
        if (isOplayerReady()) {
            mPlayRate = mPlayRate - 0.1f;
            if (mPlayRate <= 0) mPlayRate = 1;

            getOPlayer().setRate(mPlayRate);
        }
    }

    public boolean isPlaying() {
        if (isOplayerReady())
            return getOPlayer().isPlaying();
        else
            return false;
    }

    public int getPlayState() {
        if (isOplayerReady())
            return getOPlayer().getPlayerState();
        else
            return 0;
    }

    public void settime(long time) {
        if (isOplayerReady())
            getOPlayer().setPlayTime(time);
    }

    public long gettime() {
        if (isOplayerReady())
            return getOPlayer().getCurrentTime();
        else
            return -1;
    }

    public void setView(SurfaceView surfaceView) {
        if (isOplayerReady())
            getOPlayer().reAttachSurfaceView(surfaceView);
    }

    private void setCallback(PlayerCallback callBack) {
        this.mCallback = callBack;
        if (isOplayerReady())
            getOPlayer().setCallback(this);
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

        return PlayerUtil.getSingleOPlayer(mContext, mOptions, this);
    }

    public float getPosition() {
        if (isOplayerReady())
            return getOPlayer().getCurrentPosition();
        else
            return -1;
    }

    public void setPosition(long position) {
        if (isOplayerReady())
            getOPlayer().setPosition(position);
    }

    public long getLength() {
        if (isOplayerReady())
            return getOPlayer().getLength();
        else
            return 0;
    }

    public void setNormalRate() {
        mPlayRate = 1;
        if (isOplayerReady())
            getOPlayer().setRate(mPlayRate);
    }

    public int getmPlayOrder() {
        return mPlayOrder;
    }

    public OMedia setmPlayOrder(int mPlayOrder) {
        this.mPlayOrder = mPlayOrder;
        return this;
    }

    public void download(String dirName) {
        final String dlpath = FilesManager.getDownloadDir(dirName) + mMovie.getMovieName() + ".d";
        final String lpath = FilesManager.getDownloadDir(dirName) + mMovie.getMovieName();

        if (FilesManager.isExists(lpath)) {
            return;
        }
        if (FilesManager.isExists(dlpath)) {
            FilesManager.deleteFile(dlpath);
        }

        try {
            OkHttpUtils.Download(mMovie.getSourceUrl(), dlpath, mMovie.getMovieName(), new NormalRequestCallback() {
                @Override
                public void onRequestComplete(String result, int resultIndex) {
                    if (resultIndex >= 0) {
                        //SPreference.saveSharedPreferences(mContext,mMovie.getMovieName(),"LPath",lpath);
                        FilesManager.renameFile(dlpath, lpath);
                    }
                }
            });
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void downloadTo(String toPath) {
        final String dlpath = toPath + "/" + mMovie.getMovieName() + ".d";
        final String lpath = toPath + "/" + mMovie.getMovieName() ;

        if (FilesManager.isExists(lpath)) {
            return;
        }
        if (FilesManager.isExists(dlpath)) {
            FilesManager.deleteFile(dlpath);
        }

        try {
            OkHttpUtils.Download(mMovie.getSourceUrl(), dlpath, mMovie.getMovieName(), new NormalRequestCallback() {
                @Override
                public void onRequestComplete(String result, int resultIndex) {
                    if (resultIndex >= 0) {
                        //SPreference.saveSharedPreferences(mContext,mMovie.getMovieName(),"LPath",lpath);
                        FilesManager.renameFile(dlpath, lpath);
                    }
                }
            });
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public String getDownlaoded(String dirName) {
        final String lpath = FilesManager.getDownloadDir(dirName) + mMovie.getMovieName();
        if (!FilesManager.isExists(lpath)) return null;
        return lpath;
    }

    public String getAvailablePath(String catchDir) {

        if (FilesManager.isExists(mMovie.getSourceUrl())) {
            return mMovie.getSourceUrl();
        }

        return  catchDir + mMovie.getMovieName();
    }

    public boolean isAvailable(String cachDir) {
        if (mMovie == null) return false;

        if (FilesManager.isExists(mMovie.getSourceUrl())) {
            return true;
        } else if (FilesManager.isExists(cachDir + mMovie.getMovieName())) {
            return true;
        }
        return false;
    }

    @Override
    public void OnEventCallBack(int EventType, long TimeChanged, long LengthChanged, float PositionChanged, int OutCount, int ChangedType, int ChangedID, float Buffering, long Length) {
        int ii = getPlayState();
        switch (ii) {
            case 0:
            case 1:
            case 2:
            case 3:
            case 4:
                if (mLastPlayTime > gettime()) {
                    settime(mLastPlayTime);

                }
                break;
            case 5:
            case 6:
            case 7:
                mLastPlayTime = 0;
                break;
        }

        if (this.mCallback != null) {
            mCallback.OnEventCallBack(EventType, TimeChanged, LengthChanged, PositionChanged, OutCount, ChangedType, ChangedID, Buffering
                    , Length);
        }
    }

    public void save() {
        if (mMovie == null) return;

        String md5 = FilesManager.md5(mMovie.getSourceUrl());
        SPreference.saveSharedPreferences(mContext, md5, "name", mMovie.getMovieName());
        SPreference.saveSharedPreferences(mContext, md5, "url", mMovie.getSourceUrl());
        SPreference.saveSharedPreferences(mContext, md5, "playTime", "" + gettime());
    }
    //public void setRate(float v) {
    //    getOPlayer().setRate(v);
    //}

}
