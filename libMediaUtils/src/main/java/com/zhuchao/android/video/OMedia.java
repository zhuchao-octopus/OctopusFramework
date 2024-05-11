package com.zhuchao.android.video;


import static android.media.MediaPlayer.VIDEO_SCALING_MODE_SCALE_TO_FIT;
import static android.media.MediaPlayer.VIDEO_SCALING_MODE_SCALE_TO_FIT_WITH_CROPPING;
import static com.zhuchao.android.fbase.FileUtils.EmptyString;
import static com.zhuchao.android.fbase.FileUtils.NotEmptyString;
import static com.zhuchao.android.player.PlayerManager.MPLAYER;
import static java.lang.Thread.MAX_PRIORITY;

import android.content.Context;
import android.content.res.AssetFileDescriptor;
import android.net.Uri;
import android.view.SurfaceView;
import android.view.TextureView;

import com.zhuchao.android.fbase.FileUtils;
import com.zhuchao.android.fbase.MMLog;
import com.zhuchao.android.fbase.MediaFile;
import com.zhuchao.android.fbase.PlaybackEvent;
import com.zhuchao.android.fbase.PlayerStatusInfo;
import com.zhuchao.android.fbase.TTask;
import com.zhuchao.android.fbase.eventinterface.InvokeInterface;
import com.zhuchao.android.fbase.eventinterface.PlayerCallback;
import com.zhuchao.android.persist.SPreference;
import com.zhuchao.android.player.MPlayer;
import com.zhuchao.android.player.PlayControl;
import com.zhuchao.android.player.PlayerManager;
import com.zhuchao.android.player.dlna.DLNAUtil;

import org.cybergarage.upnp.Device;

import java.io.FileDescriptor;
import java.util.ArrayList;
import java.util.Map;



/*
 * OMedia oMedia= new OMedia() or OMedia oMedia= new OMedia(url)
 * oMedia.with(this).On(surfaceView).play().callback(this);
 * oMedia.with(this).On(sextureView).play(url).callback(this);
 * oMedia.with(this).playOn(surfaceView).callback(this);
 * */

public class OMedia implements PlayerCallback {
    static final long serialVersionUID = 727566175075960653L;
    private final String TAG = "OMedia";
    private OMedia preOMedia = null;
    private OMedia nextOMedia = null;
    private Movie movie = null;//new Movie(null);
    private final FileDescriptor fileDescriptor;
    private final AssetFileDescriptor assetFileDescriptor;
    private final Uri uri;
    private boolean restorePlay = false;

    private float playRate = 1;
    private long playTime = 0;
    private int videoOutWidth = 0;
    private int videoOutHeight = 0;
    protected int magicNumber = 0;
    protected PlayControl FPlayer = null;
    protected Context context = null;
    protected ArrayList<String> options = null;
    private PlayerCallback callback = null;
    public final TTask tTask_play = new TTask("OMedia.task.play", null);
    private final TTask tTask_stop = new TTask("OMedia.task.stop", null);

    ////////////////////////////////////////////////////////////////////////////////////////////////
    ////////////////////////////////////////////////////////////////////////////////////////////////
    public OMedia with(Context context) {
        this.context = context;
        if (PlayerCallback.class.isAssignableFrom(context.getClass())) setCallback((PlayerCallback) context);
        return this;
    }

    public OMedia with(Context context, ArrayList<String> options) {
        this.context = context;
        this.options = options;
        if (PlayerCallback.class.isAssignableFrom(context.getClass())) setCallback((PlayerCallback) context);
        return this;
    }

    public OMedia callback(PlayerCallback mCallback) {
        setCallback(mCallback);
        return this;
    }

    public OMedia setMagicNumber(int magicNum) {
        if (this.magicNumber != magicNum) {
            ///MMLog.log(TAG, "setMagicNumber() this.magicNumber = " + this.magicNumber + ",magicNum = " + magicNum);
            this.magicNumber = magicNum;
            if (FPlayer != null) free();
        }
        getPlayer();//获取播放器实例
        if (callback != null) setCallback(callback);
        tTask_play.setPriority(MAX_PRIORITY);
        tTask_stop.setPriority(MAX_PRIORITY);
        this.videoOutWidth = 0;
        this.videoOutHeight = 0;
        return this;
    }

    public OMedia build(int magicNum) {
        if (!isPlayerReady()) {
            setMagicNumber(magicNum);
        }
        return this;
    }

    public OMedia build() {
        if (!isPlayerReady()) {
            getPlayer();//获取播放器实例
            if (callback != null) setCallback(callback);
            tTask_play.setPriority(MAX_PRIORITY);
            tTask_stop.setPriority(MAX_PRIORITY);
            this.videoOutWidth = 0;
            this.videoOutHeight = 0;
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

    private OMedia play(String url) {
        if (!isPlayerReady()) {
            MMLog.log(TAG, "oMedia play failed no player found, check your context");
            return null;
        }
        if (FileUtils.existFile(url)) {
            FPlayer.setSource(url);
            FPlayer.play();
        } else {
            MMLog.d(TAG, "File is not exists " + url);
        }
        return this;
    }

    private OMedia play(Uri uri) {
        FPlayer.setSource(uri);
        FPlayer.play();
        return this;
    }

    private OMedia play(FileDescriptor fd) {
        FPlayer.setSource(fd);
        FPlayer.play();
        return this;
    }

    private OMedia play(AssetFileDescriptor afd) {
        FPlayer.setSource(afd);
        FPlayer.play();
        return this;
    }

    private void _play() {
        if (assetFileDescriptor != null) play(assetFileDescriptor);
        else if (fileDescriptor != null) play(fileDescriptor);
        else if (uri != null) play(uri);
        else {
            play(movie.getSrcUrl());
        }
    }

    public OMedia play() {
        playOn((SurfaceView) null);
        return this;
    }

    public void playOn(SurfaceView playView) {
        if (!isPlayerReady()) {
            MMLog.i(TAG, "playOn failed,FPlayer is invalid!");
            return;
        }
        setSurfaceView(playView);
        _play();
    }

    public void playOn_t(SurfaceView playView) {
        if (tTask_stop.isBusy()) {
            MMLog.i(TAG, "call playOn_t(), tTask_stop is busy!!");
            return;
        }
        if (tTask_play.isBusy()) {
            MMLog.i(TAG, "playOn_t(),is busy! Keeping=" + tTask_play.isKeeping() + "," + tTask_play.getProperties().getString("title"));
            if (tTask_play.isTimeOut(15000)) {
                MMLog.i(TAG, "task is timeout free ");
                tTask_play.freeFree();
                stopFree();
            }
            return;
        }
        tTask_play.clearInvokeInterface();
        tTask_play.invoke(new InvokeInterface() {
            @Override
            public void CALLTODO(String tag) {
                if (!isPlayerReady()) {
                    MMLog.i(TAG, "FPlayer is invalid");
                    return;
                }

                try {
                    setSurfaceView(playView);
                    _play();
                } catch (Exception e) {
                    //e.printStackTrace();
                    MMLog.log(TAG, e.toString());
                }
            }
        });
        tTask_play.getProperties().putString("title", getName());
        tTask_play.startAgain();//.reset().start();
    }

    public void playOn(TextureView playView) {
        FPlayer.setTextureView(playView);
        play();
    }

    public OMedia playCache(String cachedPath) {
        String cachedFile = cachedPath + movie.getName();
        if (FileUtils.existFile(cachedFile))//缓存优先与在线播放
            return play(cachedFile);
        else return play();
    }

    public OMedia onView(TextureView playView) {
        FPlayer.setTextureView(playView);
        return this;
    }

    public OMedia onView(SurfaceView playView) {
        setSurfaceView(playView);
        return this;
    }

    public OMedia setSurfaceView(SurfaceView surfaceView) {
        if (isPlayerReady()) FPlayer.setSurfaceView(surfaceView);
        return this;
    }

    public OMedia setSurfaceView(TextureView textureView) {
        if (isPlayerReady()) FPlayer.setTextureView(textureView);
        return this;
    }

    public void reAttachSurfaceView(SurfaceView surfaceView) {
        if (isPlayerReady()) FPlayer.reAttachSurfaceView(surfaceView);
    }

    public void push(boolean duplicated) {
        if (isPlayerReady()) {
            FPlayer.pushTo(movie.getSrcUrl(), duplicated);
            FPlayer.play();
        }
    }

    public void pushTo(String fromHost, Device toDevice, boolean duplicated) {
        boolean isExternalLinks = FileUtils.isExternalLinks(getPathName());
        if (toDevice == null) {
            MMLog.log(TAG, "toDevices = null");
            return;
        }
        if (!isExternalLinks) {
            FPlayer.pushTo(movie.getSrcUrl(), duplicated);
            FPlayer.play();
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
        if (toDevices == null) {
            MMLog.log(TAG, "toDevices = null");
            return;
        }
        if (toDevices.isEmpty()) {
            MMLog.log(TAG, "toDevices isEmpty");
            return;
        }

        if (!isExternalLinks) {
            FPlayer.pushTo(movie.getSrcUrl(), duplicated);
            FPlayer.play();
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

    public void playPause_t() {
        if (tTask_play.isBusy()) {
            MMLog.i(TAG, "playPause(),player is busy!isKeeping=" + tTask_play.isKeeping() + "," + tTask_play.getProperties().getString("title"));
            return;
        }
        tTask_play.clearInvokeInterface();
        tTask_play.invoke(new InvokeInterface() {
            @Override
            public void CALLTODO(String tag) {
                if (isPlayerReady()) {
                    MMLog.i(TAG, "playPause_t() on " + FPlayer.getTAG());
                    FPlayer.playPause();
                }
            }
        }).startAgain();
    }

    public void pause() {
        if (isPlayerReady()) FPlayer.pause();
    }

    public void stop() {
        try {
            ///MMLog.d(TAG, "Call stop() with " + FPlayer.getTAG());
            if (isPlayerReady()) {
                if (FPlayer.getPlayerStatusInfo().getEventType() != PlaybackEvent.Status_Stopped) {
                    long stopTime = getTime();
                    if (stopTime > 100) playTime = stopTime;
                    //MMLog.log(TAG, "OMedia playing time = " + playTime);
                }
                FPlayer.stop();
                MMLog.log(TAG, "OMedia has stopped at time " + playTime + " status=" + getPlayStatus());
            }
        } catch (Exception e) {
            //e.printStackTrace();
            MMLog.e(TAG, e.toString());
        }
    }

    public void stopFree() {
        //if(magicNumber < 2)
        stop();
        if (isPlayerReady()) {
            FPlayer.stopFree();
        }
        free();
    }

    public void stopFree_t() {
        if (tTask_stop.isBusy()) {
            MMLog.i(TAG, "call stopFree_t(), but player is busy!!");
            return;
        }
        tTask_play.freeFree();

        tTask_stop.invoke(new InvokeInterface() {
            @Override
            public void CALLTODO(String tag) {
                if (isPlayerReady()) {
                    MMLog.i(TAG, "call stopFree_t() with this " + FPlayer.getTAG());
                    stopFree();
                }
            }
        }).startAgain();
    }

    public void stopFreeFree() {
        stop();
        freeFree();
    }

    public void resume() {//唤醒，恢复播放
        ///if (isPlayerReady())
        ///{//允许此处创建新的播放器
        if (FPlayer.getTAG().startsWith(MPLAYER)) {
            restorePlay = true;
            MMLog.log(TAG, "OMedia resume to last time " + playTime);
            play();
        } else {
            restorePlay = true;
            MMLog.log(TAG, "OMedia resume to last time " + playTime);
            FPlayer.resume();
        }
        ///}
    }

    private void setCallback(PlayerCallback callBack) {
        this.callback = callBack;
        if (isPlayerReady()) FPlayer.setCallback(this);
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
        if (isPlayerReady()) FPlayer.setNoAudio();
    }

    public void setOption(String option) {
        if (isPlayerReady()) FPlayer.setOption(option);
    }

    public void setVolume(int var1) {
        if (isPlayerReady()) FPlayer.setVolume(var1);
    }

    public int getVolume() {
        if (isPlayerReady()) return FPlayer.getVolume();
        return 0;
    }

    public void fastForward(int x) {
        if (isPlayerReady()) FPlayer.fastForward(x);
    }

    public void fastForward(long x) {
        if (isPlayerReady()) FPlayer.fastForward(x);
    }

    public void fastForward() {
        if (isPlayerReady()) {
            playRate = playRate + 0.1f;
            FPlayer.setRate(playRate);
        }
    }

    public void fastBack(int x) {
        if (isPlayerReady()) FPlayer.fastBack(x);
    }

    public void fastBack(long x) {
        if (isPlayerReady()) {
            FPlayer.fastBack(x);
        }
    }

    public void fastBack() {
        if (isPlayerReady()) {
            playRate = playRate - 0.1f;
            if (playRate <= 0) playRate = 1;
            FPlayer.setRate(playRate);
        }
    }

    public void setRate(float v) {
        if (isPlayerReady()) {
            playRate = v;
            if (FPlayer.isPlaying()) FPlayer.setRate(playRate);
        }
    }

    public void setNormalRate() {
        playRate = 1;
        if (isPlayerReady()) FPlayer.setRate(playRate);
    }

    public boolean isPlaying() {
        if (isPlayerReady()) return FPlayer.isPlaying();
        else return false;
    }

    public int getPlayStatus() {
        if (isPlayerReady()) return FPlayer.getPlayerStatus();
        else return PlaybackEvent.Status_NothingIdle;
    }

    public void setTime(long time) {
        if (isPlayerReady() && time >= 0) {
            if (isPlayerReady()) FPlayer.setPlayTime(time);
        }
    }

    public long getTime() {
        if (isPlayerReady()) return FPlayer.getTime();
        else return 0;
    }

    public Movie getMovie() {
        return movie;
    }

    public float getPosition() {
        if (isPlayerReady()) return FPlayer.getPosition();
        else return -1;
    }

    public void setPosition(long position) {
        if (isPlayerReady()) FPlayer.setPosition(position);
    }

    public long getLength() {
        if (isPlayerReady()) return FPlayer.getLength();
        else return 0;
    }

    public int getAudioTracksCount() {
        if (isPlayerReady()) return FPlayer.getAudioTracksCount();
        else return -1;
    }

    public Map<Integer, String> getAudioTracks() {
        if (isPlayerReady()) return FPlayer.getAudioTracks();
        else return null;
    }

    public int getAudioTrack() {
        if (isPlayerReady()) return FPlayer.getAudioTrack();
        else return -1;
    }

    public void setAudioTrack(int index) {
        if (isPlayerReady()) FPlayer.setAudioTrack(index);
    }

    public void deselectTrack(int index) {
        if (isPlayerReady()) FPlayer.deselectTrack(index);
    }

    public void setAspectRatio(String aspect) {
        if (isPlayerReady()) FPlayer.setAspectRatio(aspect);
    }

    public void setScale(float scale) {
        if (!isPlayerReady()) return;
        //if (FPlayer.getTAG().startsWith(MPLAYER))
        if (FPlayer instanceof MPlayer) {
            if (scale <= 0) FPlayer.setScale(VIDEO_SCALING_MODE_SCALE_TO_FIT_WITH_CROPPING);
            else FPlayer.setScale(VIDEO_SCALING_MODE_SCALE_TO_FIT);
        } else {
            FPlayer.setScale(scale);
        }
    }

    public int getVideoOutWidth() {
        return videoOutWidth;
    }

    public void setVideoOutWidth(int videoOutWidth) {
        this.videoOutWidth = videoOutWidth;
    }

    public int getVideoOutHeight() {
        return videoOutHeight;
    }

    public void setVideoOutHeight(int videoOutHeight) {
        this.videoOutHeight = videoOutHeight;
    }

    public void setWindowSize(int width, int height) {
        if (isPlayerReady()) FPlayer.setWindowSize(width, height);
    }

    public void setRestorePlay(boolean restorePlay) {
        this.restorePlay = restorePlay;
    }

    public String getName() {
        return movie.getName();
    }

    public String getPathName() {
        return movie.getSrcUrl();
    }

    public int getVideoWidth() {
        if (isPlayerReady()) return FPlayer.getVideoWidth();
        else return 0;
    }

    public int getVideoHeight() {
        if (isPlayerReady()) return FPlayer.getVideoHeight();
        else return 0;
    }

    @Override
    public void onEventPlayerStatus(PlayerStatusInfo playerStatusInfo) {
        switch (playerStatusInfo.getEventCode()) {//media.class 原生事件
            case PlaybackEvent.Buffering:
            case PlaybackEvent.EndReached:
                break;
            case PlaybackEvent.Playing:
                if (videoOutHeight > 10 && videoOutWidth > 10) {
                    MMLog.log(TAG, "onEventPlayerStatus set video size to " + videoOutWidth + ":" + videoOutHeight);
                    setWindowSize(videoOutWidth, videoOutHeight);
                }
                break;
        }
        switch (playerStatusInfo.getEventType()) {
            ///case PlaybackEvent.Status_NothingIdle:
            ///    break;
            case PlaybackEvent.Status_HasPrepared:
                if (restorePlay && playerStatusInfo.isSourcePrepared() && magicNumber <= 1)
                    restorePlay(playerStatusInfo.getTimeChanged(), playerStatusInfo.getLength());
                break;
            case PlaybackEvent.Status_Opening:
                ///此时正在打开文件，设置源，调用 setSource()
                break;
            case PlaybackEvent.Status_Buffering:
                ///忽略buffering,此时正在读取文件
                break;
            case PlaybackEvent.Status_Playing:
                if (playRate != playerStatusInfo.getPlayRate()) setRate(playRate);//动态调整播放速度
                if (magicNumber >= 2 && restorePlay) restorePlay(playerStatusInfo.getTimeChanged(), playerStatusInfo.getLength());
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
                MMLog.log(TAG, "OnEventCallBack seek position from " + position + " to " + playTime + "/" + Length);
                setTime(playTime);
                restorePlay = false;
            }
            ///else {
            ///MMLog.log(TAG, "OnEventCallBack seek to position " + position + "->" + playTime + " Length = " + Length);
            ///}
        } catch (Exception e) {
            ///e.printStackTrace();
            MMLog.e(TAG, "restorePlay() " + e.toString());
        }
    }

    public void save() {
        if (movie == null) return;
        if (context == null) return;
        if (EmptyString(movie.getSrcUrl())) return;
        String md5 = FileUtils.MD5(movie.getSrcUrl());
        ///SPreference.saveSharedPreferences(mContext, md5, "name", mMovie.getMovieName());
        ///SPreference.saveSharedPreferences(mContext, md5, "url", mMovie.getSourceUrl());
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
        if (this.uri != null || this.assetFileDescriptor != null || this.fileDescriptor != null) bf = true;
        else if (FileUtils.existFile(movie.getSrcUrl())) bf = true;//MediaFile.isMediaFile(movie.getsUrl());
        else if (NotEmptyString(cachePath) && FileUtils.existFile(cachePath + "/" + movie.getName()))
            bf = MediaFile.isMediaFile(cachePath + "/" + movie.getName());

        return bf;
    }

    public boolean isVideo() {
        return MediaFile.isVideoFile(movie.getSrcUrl());
    }

    public boolean isAudio() {
        return MediaFile.isAudioFile(movie.getSrcUrl());
    }

    public boolean isPImage() {
        return MediaFile.isImageFile(movie.getSrcUrl());
    }

    protected synchronized void getPlayer() {
        if (this.context == null) return;
        switch (magicNumber) {
            case 0:
            default:
                FPlayer = PlayerManager.getSingleMPlayer(context, this);
                break;
            case 1:
                if (FPlayer == null) FPlayer = PlayerManager.getMultiMPlayer(context, this);
                break;
            case 2:
                FPlayer = PlayerManager.getSingleOPlayer(context, options, this);
                break;
            case 3:
                if (FPlayer == null) FPlayer = PlayerManager.getMultiOPlayer(context, options, this);
                break;
            ///default:
            ///    FPlayer = PlayerManager.getSingleMPlayer(context, this);
            ///    break;
        }
        MMLog.d(TAG, "GetPlayer() MagicNumber = " + magicNumber + ", FPlayer = " + FPlayer.getTAG());

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
                MMLog.i(TAG, "call free() with this");
                //FPlayer.free();//释放播放器里面的资源
                FPlayer = null;//释放本实力里面的资源
            }//播放器实例还在
            else {
                MMLog.i(TAG, "call free() FPlayer = null");
            }
        } catch (Exception e) {
            //e.printStackTrace();
            MMLog.e(TAG, "free() " + e.toString());
        }
    }

    public void freeFree() {
        try {
            if (FPlayer != null) {
                MMLog.i(TAG, "call freeFree() with PlayerManager " + FPlayer.getTAG());
                free();
                PlayerManager.free();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    ///三个free()
    // 1：PlayerManager.free(); //释放整个播放器实例，释放所有的资源
    // 2：getOPlayer().free;//释放播放器里面的资源
    // 3:free//释放本实力里面的资源
}
