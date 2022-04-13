package com.zhuchao.android.video;

import android.content.Context;
import android.content.res.AssetFileDescriptor;
import android.net.Uri;
import android.text.TextUtils;
import android.view.SurfaceView;
import android.view.TextureView;

import com.zhuchao.android.callbackevent.NormalRequestCallback;
import com.zhuchao.android.callbackevent.PlayerCallback;
import com.zhuchao.android.databaseutil.SPreference;
import com.zhuchao.android.libfileutils.FilesManager;
import com.zhuchao.android.libfileutils.MLog;
import com.zhuchao.android.libfileutils.MediaFile;
import com.zhuchao.android.netutil.OkHttpUtils;
import com.zhuchao.android.playerutil.PlayControl;
import com.zhuchao.android.playerutil.PlayerManager;

import java.io.FileDescriptor;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Map;

/*
 * OMedia oMedia= new OMedia() or OMedia oMedia= new OMedia(url)
 * oMedia.with(this).On(surfaceView).play().callback(this);
 * oMedia.with(this).On(sextureView).play(url).callback(this);
 * oMedia.with(this).playOn(surfaceView).callback(this);
 * */

public class OMedia implements Serializable, PlayerCallback {
    static final long serialVersionUID = 727566175075960653L;
    private final String TAG = "OMedia";
    protected PlayControl FPlayer;
    protected Context context = null;
    protected ArrayList<String> options = null;
    protected int MagicNum = 0;
    private PlayerCallback callback = null;
    private OMedia preOMedia = null;
    private OMedia nextOMedia = null;
    private float playRate = 1;
    private long playTime = 0;
    private Movie movie = null;//new Movie(null);
    private FileDescriptor fileDescriptor;
    private AssetFileDescriptor assetFileDescriptor;
    private Uri uri;

    public void callback(PlayerCallback mCallback) {
        setCallback(mCallback);
        return;
    }

    public OMedia(final String url) {
        this.fileDescriptor = null;
        this.assetFileDescriptor = null;
        this.uri = null;
        this.movie = new Movie(url);
    }

    public OMedia(FileDescriptor FD) {
        this.assetFileDescriptor = null;
        this.uri = null;
        this.fileDescriptor = FD;
        this.movie = new Movie(FD.toString());
    }

    public OMedia(AssetFileDescriptor AFD) {
        this.fileDescriptor = null;
        this.uri = null;
        this.assetFileDescriptor = AFD;
        this.movie = new Movie(AFD.toString());
    }

    public OMedia(Uri uri) {
        this.fileDescriptor = null;
        this.assetFileDescriptor = null;
        this.uri = uri;
        this.movie = new Movie(uri.getPath());
    }

    public OMedia(Movie movie) {
        this.fileDescriptor = null;
        this.assetFileDescriptor = null;
        this.uri = null;
        this.movie = movie;
    }

    public OMedia with(Context context) {
        this.context = context;
        return this;
    }

    public OMedia with(Context context, ArrayList<String> options) {
        this.context = context;
        this.options = options;
        return this;
    }

    public OMedia play() {
        if (assetFileDescriptor != null)
            return play(assetFileDescriptor);
        else if (fileDescriptor != null)
            return play(fileDescriptor);
        else if (uri != null)
            return play(uri);
        else {
            return play(movie.getsUrl());
        }
    }

    public OMedia playCache(String cachePath) {
        String cacheFile = cachePath + movie.getName();
        if (assetFileDescriptor != null)
            return play(assetFileDescriptor);
        else if (fileDescriptor != null)
            return play(fileDescriptor);
        else if (FilesManager.isExists(cacheFile))
            return play(cacheFile);
        else if (uri != null)
            return play(uri);
        else
            return play(movie.getsUrl());
    }

    private OMedia play(String url) {
        getOPlayer().setSource(url);
        getOPlayer().play();
        return this;
    }

    private OMedia play(Uri uri) {
        getOPlayer().setSource(uri);
        getOPlayer().play();
        return this;
    }

    private OMedia play(FileDescriptor fd) {
        getOPlayer().setSource(fd);
        getOPlayer().play();
        return this;
    }

    private OMedia play(AssetFileDescriptor afd) {
        getOPlayer().setSource(afd);
        getOPlayer().play();
        return this;
    }

    public OMedia playOn(SurfaceView playView) {
        getOPlayer().setSurfaceView(playView);
        return play();
    }

    public OMedia playOn(TextureView playView) {
        getOPlayer().setTextureView(playView);
        return play();
    }

    public OMedia onView(TextureView playView) {
        getOPlayer().setTextureView(playView);
        return this;
    }

    public OMedia onView(SurfaceView playView) {
        getOPlayer().setSurfaceView(playView);
        return this;
    }

    public void setSurfaceView(SurfaceView surfaceView) {
        if (!isPlayerReady()) return;
        if (surfaceView == null) return;
        if (getOPlayer().getSurfaceView() == null)
            getOPlayer().setSurfaceView(surfaceView);
        else if (!getOPlayer().getTAG().equals("OPlayer>>>")) {
            playTime = getOPlayer().getTime();
            getOPlayer().reAttachSurfaceView(surfaceView);
            play();
        } else {
            getOPlayer().stop();
            getOPlayer().reAttachSurfaceView(surfaceView);
            getOPlayer().resume();
        }
    }

    public void setSurfaceView(TextureView textureView) {
        if (!isPlayerReady()) return;
        getOPlayer().stop();
        getOPlayer().reAttachTextureView(textureView);
        getOPlayer().resume();
    }

    public void playPause() {
        if (isPlayerReady())
            getOPlayer().playPause();
    }

    public void pause() {
        if (isPlayerReady())
            getOPlayer().pause();
    }

    public void stop() {
        try {
            playTime = getTime();
            if (isPlayerReady())
                getOPlayer().stop();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void stopFree() {
        try {
            //this.save();
            if (FPlayer != null)
                FPlayer.free();
            FPlayer = null;
            PlayerManager.reset();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void resume() {
        if (isPlayerReady())
            getOPlayer().resume();
    }

    private void setCallback(PlayerCallback callBack) {
        this.callback = callBack;
        getOPlayer().setCallback(this);
    }

    public long getPlayTime() {
        return playTime;
    }

    public OMedia setPlayTime(long mLastPlayTime) {
        this.playTime = mLastPlayTime;
        return this;
    }

    public OMedia getPre() {
        return preOMedia;
    }

    public void setPre(OMedia mPreOMedia) {
        this.preOMedia = mPreOMedia;
    }

    public OMedia getNext() {
        return nextOMedia;
    }

    public void setNext(OMedia mNextOMedia) {
        this.nextOMedia = mNextOMedia;
    }

    public void setNoAudio() {
        if (isPlayerReady())
            getOPlayer().setNoAudio();
    }

    public void setOption(String option) {
        if (isPlayerReady())
            getOPlayer().setOption(option);
    }

    public void setVolume(int var1) {
        if (isPlayerReady())
            getOPlayer().setVolume(var1);
    }

    public int getVolume() {
        if (isPlayerReady())
            return getOPlayer().getVolume();
        return 0;
    }

    public void fastForward(int x) {
        if (isPlayerReady())
            getOPlayer().fastForward(x);
    }

    public void fastForward(long x) {
        if (isPlayerReady())
            getOPlayer().fastForward(x);
    }

    public void fastForward() {
        if (isPlayerReady()) {
            playRate = playRate + 0.1f;
            getOPlayer().setRate(playRate);
        }
    }

    public void fastBack(int x) {
        playTime = -1;
        if (isPlayerReady())
            getOPlayer().fastBack(x);
    }

    public void fastBack(long x) {
        playTime = -1;
        if (isPlayerReady())
            getOPlayer().fastBack(x);
    }

    public void fastBack() {
        playTime = -1;
        if (isPlayerReady()) {
            playRate = playRate - 0.1f;
            if (playRate <= 0) playRate = 1;
            getOPlayer().setRate(playRate);
        }
    }

    public boolean isPlaying() {
        if (isPlayerReady())
            return getOPlayer().isPlaying();
        else
            return false;
    }

    public int getPlayStatus() {
        if (isPlayerReady())
            return getOPlayer().getPlayerState();
        else
            return 0;
    }

    public void setTime(long time) {
        if (isPlayerReady() && time >= 0)
            getOPlayer().setPlayTime(time);
    }

    public long getTime() {
        if (isPlayerReady())
            return getOPlayer().getTime();
        else
            return -1;
    }

    public Movie getMovie() {
        return movie;
    }

    public float getPosition() {
        if (isPlayerReady())
            return getOPlayer().getPosition();
        else
            return -1;
    }

    public void setPosition(long position) {
        if (isPlayerReady())
            getOPlayer().setPosition(position);
    }

    public long getLength() {
        if (isPlayerReady())
            return getOPlayer().getLength();
        else
            return 0;
    }

    public void setNormalRate() {
        playRate = 1;
        if (isPlayerReady())
            getOPlayer().setRate(playRate);
    }

    public int getAudioTracksCount() {
        if (isPlayerReady())
            return getOPlayer().getAudioTracksCount();
        else return -1;
    }

    public Map<Integer, String> getAudioTracks() {
        if (isPlayerReady())
            return getOPlayer().getAudioTracks();
        else
            return null;
    }

    public int getAudioTrack() {
        if (isPlayerReady())
            return getOPlayer().getAudioTrack();
        else
            return -1;
    }

    public void setAudioTrack(int index) {
        if (isPlayerReady())
            getOPlayer().setAudioTrack(index);
    }

    public void setAspectRatio(String aspect) {
        if (isPlayerReady())
            getOPlayer().setAspectRatio(aspect);
    }

    public void setScale(float scale) {
        if (isPlayerReady())
            getOPlayer().setScale(scale);
    }

    public void setWindowSize(int width, int height) {
        if (isPlayerReady())
            getOPlayer().setWindowSize(width, height);
    }

    public String getPathName() {
        return movie.getsUrl();
    }

    public String getName() {
        return movie.getName();
    }

    public boolean isPlayerReady() {
        if (FPlayer == null) return false;
        else
            return true;
    }

    //int NothingSpecial=0;//int Opening=1;//int Buffering=2;//int Playing=3;//int Paused=4;
    //int Stopped=5; //int Ended=6;//int Error=7;
    @Override
    public void OnEventCallBack(int EventType, long TimeChanged, long LengthChanged, float PositionChanged, int OutCount, int ChangedType, int ChangedID, float Buffering, long Length) {
        //int ii = getPlayState();
        switch (EventType) {
            case 0:
            case 1:
            case 2:
            case 3:
            case 4:
                if (playTime > getTime()) {
                    MLog.log(TAG, "go to position = " + playTime);
                    setTime(playTime);
                    playTime = 0;
                }
                break;
            case 5:
            case 6:
            case 7:
                playTime = 0;
                break;
        }
        if (this.callback != null) {
            callback.OnEventCallBack(EventType, TimeChanged, LengthChanged, PositionChanged, OutCount, ChangedType, ChangedID, Buffering
                    , Length);
        }
    }

    public String md5() {
        return FilesManager.md5(movie.getsUrl());
    }

    public void save() {
        if (movie == null) return;
        if (context == null) return;
        if (TextUtils.isEmpty(movie.getsUrl())) return;
        String md5 = FilesManager.md5(movie.getsUrl());
        //SPreference.saveSharedPreferences(mContext, md5, "name", mMovie.getMovieName());
        //SPreference.saveSharedPreferences(mContext, md5, "url", mMovie.getSourceUrl());
        playTime = getTime();
        SPreference.putLong(context, md5, "playTime", playTime);
    }

    public void load() {
        if (movie == null) return;
        if (context == null) return;
        if (TextUtils.isEmpty(movie.getsUrl())) return;
        String md5 = FilesManager.md5(movie.getsUrl());
        playTime = SPreference.getLong(context, md5, "playTime");
    }

    public void download() {
        final String dlpath = FilesManager.getDownloadDir(null) + movie.getName() + ".d";
        final String lpath = FilesManager.getDownloadDir(null) + movie.getName();
        if (FilesManager.isExists(lpath)) {
            return;
        }
        if (FilesManager.isExists(dlpath)) {
            FilesManager.deleteFile(dlpath);
        }
        try {
            OkHttpUtils.Download(movie.getsUrl(), dlpath, movie.getName(), new NormalRequestCallback() {
                @Override
                public void onRequestComplete(String result, int resultIndex) {
                    if (resultIndex >= 0) {
                        FilesManager.renameFile(dlpath, lpath);
                    }
                }
            });
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void downloadTo(String toPath) {
        final String dlpath = toPath + "/" + movie.getName() + ".d";
        final String lpath = toPath + "/" + movie.getName();

        if (FilesManager.isExists(lpath)) {
            return;
        }
        if (FilesManager.isExists(dlpath)) {
            FilesManager.deleteFile(dlpath);
        }
        try {
            OkHttpUtils.Download(movie.getsUrl(), dlpath, movie.getName(), new NormalRequestCallback() {
                @Override
                public void onRequestComplete(String result, int resultIndex) {
                    if (resultIndex >= 0) {
                        FilesManager.renameFile(dlpath, lpath);
                    }
                }
            });
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public String getDownloadFrom(String fromPath) {
        final String lath = fromPath + "/" + movie.getName();
        if (!FilesManager.isExists(lath)) return null;
        return lath;
    }

    public String getDownload() {
        final String lath = FilesManager.getDownloadDir(null) + movie.getName();
        if (!FilesManager.isExists(lath)) return null;
        return lath;
    }

    public boolean isAvailable(String cachePath) {
        boolean bf = false;
        if (this.uri != null || this.assetFileDescriptor != null || this.fileDescriptor != null)
            bf = true;
        else if (FilesManager.isExists(movie.getsUrl()))
            bf = MediaFile.isMediaFile(movie.getsUrl());
        else if (FilesManager.isExists(cachePath + movie.getName()))
            bf = MediaFile.isMediaFile(cachePath + movie.getName());
        return bf;
    }

    public void setMagicNum(int MagicNum) {
        if (this.MagicNum != this.MagicNum) {
            if (FPlayer != null)
                stopFree();
        }
        this.MagicNum = MagicNum;
        getOPlayer();
    }

    protected PlayControl getOPlayer() {
        if (this.context == null) return null;
        if (FPlayer == null) {
            switch (MagicNum) {
                case 1:
                    FPlayer = PlayerManager.getSingleOPlayer(context, options, this);
                    MLog.log(TAG, FPlayer.getTAG() + ",MagicNum = " + MagicNum);
                    break;
                case 0:
                default:
                    FPlayer = PlayerManager.getSingleMPlayer(context, this);
                    MLog.log(TAG, FPlayer.getTAG() + ",MagicNum = " + MagicNum);
                    break;
            }
        }
        return FPlayer;
    }
}
