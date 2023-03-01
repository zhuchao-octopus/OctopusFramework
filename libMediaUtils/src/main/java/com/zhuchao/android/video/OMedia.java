package com.zhuchao.android.video;


import static com.zhuchao.android.fbase.FileUtils.EmptyString;
import static com.zhuchao.android.fbase.FileUtils.NotEmptyString;
import static com.zhuchao.android.player.PlayerManager.MPLAYER;

import android.content.Context;
import android.content.res.AssetFileDescriptor;
import android.net.Uri;
import android.view.SurfaceView;
import android.view.TextureView;

import androidx.annotation.NonNull;

import com.zhuchao.android.eventinterface.InvokeInterface;
import com.zhuchao.android.eventinterface.PlaybackEvent;
import com.zhuchao.android.eventinterface.PlayerCallback;
import com.zhuchao.android.eventinterface.PlayerStatusInfo;
import com.zhuchao.android.fbase.FileUtils;
import com.zhuchao.android.fbase.MMLog;
import com.zhuchao.android.fbase.MediaFile;
import com.zhuchao.android.fbase.TTask;
import com.zhuchao.android.persist.SPreference;
import com.zhuchao.android.player.PlayControl;
import com.zhuchao.android.player.PlayerManager;
import com.zhuchao.android.player.dlna.DLNAUtil;

import org.cybergarage.upnp.Device;

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
    protected PlayControl FPlayer = null;
    protected Context context = null;
    protected ArrayList<String> options = null;
    protected int magicNumber = 0;
    private PlayerCallback callback = null;
    private OMedia preOMedia = null;
    private OMedia nextOMedia = null;
    private float playRate = 1;
    private long playTime = 0;
    private Movie movie = null;//new Movie(null);
    private final FileDescriptor fileDescriptor;
    private final AssetFileDescriptor assetFileDescriptor;
    private final Uri uri;
    private boolean restorePlay = false;
    private final TTask tTask_play = new TTask("OMedia.play", null);
    private final TTask tTask_stop = new TTask("OMedia.stop", null);

    public void callback(PlayerCallback mCallback) {
        setCallback(mCallback);
    }

    public OMedia setMagicNumber(int magicNum) {
        if (this.magicNumber != magicNum) {
            this.magicNumber = magicNum;
            free();
            getPlayer();
        }
        return this;
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

    private OMedia play(String url) {
        getPlayer();
        if (!isPlayerReady()) {
            MMLog.log(TAG, "oMedia play failed no player found, check your context");
            return null;
        }
        getPlayer().setSource(url);
        getPlayer().play();
        return this;
    }

    private OMedia play(Uri uri) {
        getPlayer().setSource(uri);
        getPlayer().play();
        return this;
    }

    private OMedia play(FileDescriptor fd) {
        getPlayer().setSource(fd);
        getPlayer().play();
        return this;
    }

    private OMedia play(AssetFileDescriptor afd) {
        getPlayer().setSource(afd);
        getPlayer().play();
        return this;
    }

    private void _play() {
        if (assetFileDescriptor != null)
            play(assetFileDescriptor);
        else if (fileDescriptor != null)
            play(fileDescriptor);
        else if (uri != null)
            play(uri);
        else {
            play(movie.getSrcUrl());
        }
    }

    public OMedia play() {
        playOn((SurfaceView) null);
        return this;
    }

    public void playOn(SurfaceView playView) {
        if (tTask_play.isBusy())
        {
            MMLog.i(TAG, "playOn(),player is busy!isKeeping=" +
                    tTask_play.isKeeping() + "," +
                    tTask_play.getProperties().getString("title"));

            if(tTask_play.isTimeOut(10000)) {
                MMLog.i(TAG, "task is timeout free ");
                //tTask_play.freeFree();
            }
            return;
        }
        tTask_play.invoke(new InvokeInterface() {
            @Override
            public void CALLTODO(String tag) {
                if (playView != null)
                    getPlayer().setSurfaceView(playView);
                _play();
                tTask_play.resetAll();
            }
        });
        tTask_play.getProperties().putString("title", getName());
        tTask_play.startAgain();//.reset().start();
    }

    public void playOn(TextureView playView) {
        getPlayer().setTextureView(playView);
        play();
    }

    public OMedia playCache(String cachedPath) {
        String cachedFile = cachedPath + movie.getName();
        if (FileUtils.existFile(cachedFile))//缓存优先与在线播放
            return play(cachedFile);
        else return play();
    }

    public OMedia onView(TextureView playView) {
        getPlayer().setTextureView(playView);
        return this;
    }

    public OMedia onView(SurfaceView playView) {
        setSurfaceView(playView);
        return this;
    }

    public OMedia setSurfaceView(@NonNull SurfaceView surfaceView) {
        getPlayer().setSurfaceView(surfaceView);
        return this;
    }

    public OMedia setSurfaceView(TextureView textureView) {
        if (!isPlayerReady()) return this;
        getPlayer().stop();
        getPlayer().reAttachTextureView(textureView);
        getPlayer().resume();
        return this;
    }

    public void reAttachSurfaceView(SurfaceView surfaceView) {
        getPlayer().reAttachSurfaceView(surfaceView);
    }

    public void push(boolean duplicated) {
        //if (isPlayerReady())
        getPlayer().pushTo(movie.getSrcUrl(), duplicated);
        getPlayer().play();
    }

    public void pushTo(String fromHost, Device toDevice, boolean duplicated) {
        boolean isExternalLinks = FileUtils.isExternalLinks(getPathName());

        if (!isExternalLinks) {
            getPlayer().pushTo(movie.getSrcUrl(), duplicated);
            getPlayer().play();
        }

        if (duplicated && isExternalLinks) {
            play();
        }

        if (isExternalLinks) {
            DLNAUtil.shareTo(getPathName(), toDevice);
            MMLog.log(TAG, "from device " + fromHost + " share to " + toDevice.getFriendlyName() + ",duplicated = " + duplicated);
        } else {
            DLNAUtil.shareTo("rtsp://" + fromHost + ":8554/0", toDevice);
            MMLog.log(TAG, "from device " + fromHost + " push to " + toDevice.getFriendlyName() + ",duplicated = " + duplicated);
        }
    }

    public void pushToDevices(String fromHost, ArrayList<Device> toDevices, boolean duplicated) {
        boolean isExternalLinks = FileUtils.isExternalLinks(getPathName());

        if (!isExternalLinks) {
            getPlayer().pushTo(movie.getSrcUrl(), duplicated);
            getPlayer().play();
        }

        if (duplicated && isExternalLinks) {
            play();
        }

        for (Device toDevice : toDevices) {
            //Device toDevice = (Device)device
            if (isExternalLinks) {
                DLNAUtil.shareTo(getPathName(), toDevice);
                MMLog.log(TAG, "from device " + fromHost + " share to " + toDevice.getFriendlyName() + ",duplicated = " + duplicated);
            } else {
                DLNAUtil.shareTo("rtsp://" + fromHost + ":8554/0", toDevice);
                MMLog.log(TAG, "from device " + fromHost + " push to " + toDevice.getFriendlyName() + ",duplicated = " + duplicated);
            }
        }
    }

    public void playPause() {
        if (tTask_play.isBusy()) {
            MMLog.i(TAG, "playPause(),player is busy!isKeeping=" +
                    tTask_play.isKeeping() + "," +
                    tTask_play.getProperties().getString("title"));
            return;
        }
        tTask_play.invoke(new InvokeInterface() {
            @Override
            public void CALLTODO(String tag) {
                if (isPlayerReady()) {
                    MMLog.i(TAG, "playPause() on " + getPlayer().getTAG());
                    getPlayer().playPause();
                }
            }
        }).startAgain();
    }

    public void pause() {
        if (isPlayerReady())
            getPlayer().pause();
    }

    public void stop() {
        try {
            if (isPlayerReady()) {
                if (getPlayer().getPlayerStatusInfo().getEventType() != PlaybackEvent.Status_Stopped) {
                    playTime = getTime();
                    MMLog.log(TAG, "o media playing time = " + playTime);
                }
                getPlayer().stop();
                MMLog.log(TAG, "o media has stopped at time " + playTime);
            }
        } catch (Exception e) {
            //e.printStackTrace();
            MMLog.log(TAG, e.toString());
        }
    }

    public void stopFree() {
        if (tTask_stop.isBusy()) {
            MMLog.i(TAG, "call stopFree(), but player is busy!!");
            return;
        }
        tTask_play.freeFree();
        tTask_stop.invoke(new InvokeInterface() {
            @Override
            public void CALLTODO(String tag) {
                if (isPlayerReady()) {
                    MMLog.i(TAG, "call stopFree() on " + getPlayer().getTAG());
                    stop();
                    free();
                }
            }
        }).startAgain();
    }

    public void resume() {//唤醒，恢复播放
        //if (isPlayerReady())
        {//允许此处创建新的播放器
            if (getPlayer().getTAG().startsWith(MPLAYER)) {
                restorePlay = true;
                MMLog.log(TAG, "oMedia resume, resumeTime = " + playTime);
                play();
            } else {
                getPlayer().resume();
            }
        }
    }

    private void setCallback(PlayerCallback callBack) {
        this.callback = callBack;
        //if (isPlayerReady())
        getPlayer().setCallback(this);
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
            getPlayer().setNoAudio();
    }

    public void setOption(String option) {
        if (isPlayerReady())
            getPlayer().setOption(option);
    }

    public void setVolume(int var1) {
        if (isPlayerReady())
            getPlayer().setVolume(var1);
    }

    public int getVolume() {
        if (isPlayerReady())
            return getPlayer().getVolume();
        return 0;
    }

    public void fastForward(int x) {
        if (isPlayerReady())
            getPlayer().fastForward(x);
    }

    public void fastForward(long x) {
        if (isPlayerReady())
            getPlayer().fastForward(x);
    }

    public void fastForward() {
        if (isPlayerReady()) {
            playRate = playRate + 0.1f;
            getPlayer().setRate(playRate);
        }
    }

    public void fastBack(int x) {
        if (isPlayerReady())
            getPlayer().fastBack(x);
    }

    public void fastBack(long x) {
        if (isPlayerReady()) {
            getPlayer().fastBack(x);
        }
    }

    public void fastBack() {
        if (isPlayerReady()) {
            playRate = playRate - 0.1f;
            if (playRate <= 0) playRate = 1;
            getPlayer().setRate(playRate);
        }
    }

    public void setRate(float v) {
        if (isPlayerReady()) {
            playRate = v;
            if (getPlayer().isPlaying())
                getPlayer().setRate(playRate);
        }
    }

    public void setNormalRate() {
        playRate = 1;
        if (isPlayerReady())
            getPlayer().setRate(playRate);
    }

    public boolean isPlaying() {
        if (isPlayerReady())
            return getPlayer().isPlaying();
        else
            return false;
    }

    public int getPlayStatus() {
        if (isPlayerReady())
            return getPlayer().getPlayerStatus();
        else
            return 0;
    }

    public void setTime(long time) {
        if (isPlayerReady() && time >= 0) {
            if (isPlayerReady())
                getPlayer().setPlayTime(time);
        }
    }

    public long getTime() {
        if (isPlayerReady())
            return getPlayer().getTime();
        else
            return 0;
    }

    public Movie getMovie() {
        return movie;
    }

    public float getPosition() {
        if (isPlayerReady())
            return getPlayer().getPosition();
        else
            return -1;
    }

    public void setPosition(long position) {
        if (isPlayerReady())
            getPlayer().setPosition(position);
    }

    public long getLength() {
        if (isPlayerReady())
            return getPlayer().getLength();
        else
            return 0;
    }

    public int getAudioTracksCount() {
        if (isPlayerReady())
            return getPlayer().getAudioTracksCount();
        else return -1;
    }

    public Map<Integer, String> getAudioTracks() {
        if (isPlayerReady())
            return getPlayer().getAudioTracks();
        else
            return null;
    }

    public int getAudioTrack() {
        if (isPlayerReady())
            return getPlayer().getAudioTrack();
        else
            return -1;
    }

    public void setAudioTrack(int index) {
        if (isPlayerReady())
            getPlayer().setAudioTrack(index);
    }

    public void deselectTrack(int index) {
        if (isPlayerReady())
            getPlayer().deselectTrack(index);
    }

    public void setAspectRatio(String aspect) {
        if (isPlayerReady())
            getPlayer().setAspectRatio(aspect);
    }

    public void setScale(float scale) {
        if (isPlayerReady())
            getPlayer().setScale(scale);
    }

    public void setWindowSize(int width, int height) {
        if (isPlayerReady())
            getPlayer().setWindowSize(width, height);
    }

    public String getPathName() {
        return movie.getSrcUrl();
    }

    public String getName() {
        return movie.getName();
    }

    public void setRestorePlay(boolean restorePlay) {
        this.restorePlay = restorePlay;
    }

    @Override
    public void onEventPlayerStatus(PlayerStatusInfo playerStatusInfo) {
        switch (playerStatusInfo.getEventType()) {
            //case PlaybackEvent.Status_NothingIdle:
            //    break;
            case PlaybackEvent.Status_HasPrepared:
                if (restorePlay && playerStatusInfo.isSourcePrepared())
                    restorePlay(playerStatusInfo.getTimeChanged(), playerStatusInfo.getLengthChanged());
                break;
            case PlaybackEvent.Status_Opening:
            case PlaybackEvent.Status_Buffering:
            case PlaybackEvent.Status_Playing:
                if (playRate != playerStatusInfo.getPlayRate())
                    setRate(playRate);//动态调整播放速度
                break;
            case PlaybackEvent.Status_Paused:
            case PlaybackEvent.Status_Stopped:
                break;
            case PlaybackEvent.Status_Ended:
            case PlaybackEvent.Status_Error:
                playTime = 0;
                break;
        }
        if (this.callback != null) {
            callback.onEventPlayerStatus(playerStatusInfo);
        }
    }

    public String md5() {
        return FileUtils.MD5(movie.getSrcUrl());
    }

    private void restorePlay(float position, long Length) {
        try {
            if (((position + 100) < playTime) && (playTime < Length - 100) && (Length > 100)) {//播放进度恢复
                MMLog.log(TAG, "OnEventCallBack seek to position " + position + "->" + playTime + " Length = " + Length);
                setTime(playTime);
                restorePlay = false;
            } else
                MMLog.log(TAG, "OnEventCallBack seek to position " + position + "->" + playTime + " Length = " + Length);
        } catch (Exception e) {
            //e.printStackTrace();
            MMLog.e(TAG, "restorePlay() " + e.toString());
        }
    }

    public void save() {
        if (movie == null) return;
        if (context == null) return;
        if (EmptyString(movie.getSrcUrl())) return;
        String md5 = FileUtils.MD5(movie.getSrcUrl());
        //SPreference.saveSharedPreferences(mContext, md5, "name", mMovie.getMovieName());
        //SPreference.saveSharedPreferences(mContext, md5, "url", mMovie.getSourceUrl());
        SPreference.putLong(context, md5, "playTime", playTime);
    }

    public void load() {
        if (movie == null) return;
        if (context == null) return;
        if (EmptyString(movie.getSrcUrl())) return;
        String md5 = FileUtils.MD5(movie.getSrcUrl());
        playTime = SPreference.getLong(context, md5, "playTime");
    }

    public boolean isAvailable(String cachePath) {
        boolean bf = false;
        if (this.uri != null || this.assetFileDescriptor != null || this.fileDescriptor != null)
            bf = true;
        else if (FileUtils.existFile(movie.getSrcUrl()))
            bf = true;//MediaFile.isMediaFile(movie.getsUrl());
        else if (NotEmptyString(cachePath) && FileUtils.existFile(cachePath + "/" + movie.getName()))
            bf = MediaFile.isMediaFile(cachePath + "/" + movie.getName());

        return bf;
    }

    public boolean isPlayingVideo() {
        return MediaFile.isVideoFile(movie.getSrcUrl());
    }

    public boolean isPlayingAudio() {
        return MediaFile.isAudioFile(movie.getSrcUrl());
    }

    public boolean isPlayingImage() {
        return MediaFile.isImageFile(movie.getSrcUrl());
    }

    protected synchronized PlayControl getPlayer() {
        if (this.context == null) return null;
        if (FPlayer == null) {
            MMLog.d(TAG, "Get Player MagicNumber = " + magicNumber);
            switch (magicNumber) {
                case 0:
                default:
                    FPlayer = PlayerManager.getSingleMPlayer(context, this);
                    break;
                case 1:
                    FPlayer = PlayerManager.getMultiMPlayer(context, this);
                    break;
                case 2:
                    FPlayer = PlayerManager.getSingleOPlayer(context, options, this);
                    break;
                case 3:
                    FPlayer = PlayerManager.getMultiOPlayer(context, options, this);
                    break;
                //default:
                //    FPlayer = PlayerManager.getSingleMPlayer(context, this);
                //    break;
            }
        }
        return FPlayer;
    }

    public PlayControl getFPlayer() {
        return FPlayer;
    }

    public boolean isPlayerReady() {
        return FPlayer != null;
    }

    public void free() {
        try {
            if (FPlayer != null) {
                FPlayer.free();//释放播放器里面的资源
                FPlayer = null;//释放本实力里面的资源
            }//播放器实例还在
        } catch (Exception e) {
            //e.printStackTrace();
            MMLog.e(TAG, "free() " + e.toString());
        }
    }

    public void freeFree() {
        free();
        PlayerManager.free();
    }

    ///三个free()
    // 1：PlayerManager.free(); //释放整个播放器实例，释放所有的资源
    // 2：getOPlayer().free;//释放播放器里面的资源
    // 3:free//释放本实力里面的资源
}
