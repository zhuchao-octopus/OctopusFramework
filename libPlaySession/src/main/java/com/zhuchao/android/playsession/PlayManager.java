package com.zhuchao.android.playsession;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.res.AssetFileDescriptor;
import android.net.Uri;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.text.TextUtils;
import android.view.SurfaceView;

import com.zhuchao.android.callbackevent.NormalRequestCallback;
import com.zhuchao.android.callbackevent.PlaybackEvent;
import com.zhuchao.android.callbackevent.PlayerCallback;
import com.zhuchao.android.libfileutils.FilesManager;
import com.zhuchao.android.libfileutils.MLog;
import com.zhuchao.android.video.Movie;
import com.zhuchao.android.video.OMedia;
import com.zhuchao.android.video.VideoList;

import java.io.FileDescriptor;

public class PlayManager implements PlayerCallback, SessionCompleteCallback, NormalRequestCallback {
    private final String TAG = "PlayManager";
    private final int ACTION_DELAY = 500;
    private int MagicNum = 0;
    private Context context;
    private SurfaceView surfaceView = null;
    private OMedia oMedia = null;
    private boolean isPlaying = false;
    private PlayerCallback callback = null;
    private String playPath = null;
    private String downloadPath = null;
    private int playOrder = SessionID.PLAY_MANAGER_PLAY_ORDER2;
    private int autoPlaySource = SessionID.SESSION_SOURCE_NONE;
    private VideoList playingList = null;
    private OPlayerSessionManager sessionManager = null;
    private long lStartTick = 0;

    public PlayManager(Context mContext, SurfaceView mSurfaceView) {
        this.context = mContext;
        this.surfaceView = mSurfaceView;
        playingList = new VideoList(this);
        playingList.setTAG("PlayManager.VideoList");
        downloadPath = FilesManager.getDownloadDir(null);//播放目录和，下载缓存目录不一样
        setMagicNum(0);
    }

    public PlayManager callback(PlayerCallback mCallback) {
        this.callback = mCallback;
        return this;
    }

    public void setSessionManager(OPlayerSessionManager sessionManager) {
        this.sessionManager = sessionManager;
        this.sessionManager.setUserSessionCallback(this);
    }

    public OMedia getPlayingMedia() {
        return oMedia;
    }

    public String getPlayPath() {
        return playPath;
    }

    public int getPlayOrder() {
        return playOrder;
    }

    public void setPlayOrder(int playOrder) {
        this.playOrder = playOrder;
    }

    public boolean isPlaying() {
        if (oMedia == null) {
            isPlaying = false;
            return false;
        }
        int sta = oMedia.getPlayStatus();
        if (sta >= PlaybackEvent.Status_Opening && sta <= PlaybackEvent.Status_Playing)
            isPlaying = true;
        else
            isPlaying = false;
        return isPlaying;
    }

    public synchronized void startPlay(OMedia oMedia) {
        if (surfaceView == null) {
            MLog.log(TAG, "no surfaceView ！！！！！！");
            return;
        }
        if (oMedia == null) {
            MLog.log(TAG, "There is no media to play！！！");
            return;
        } else if (!oMedia.isAvailable(playPath)) {
            MLog.log(TAG, "The Source is not available! go next/pre---> " + oMedia.getMovie().getsUrl());
            playHandler.sendEmptyMessage(this.playOrder);
            return;
        }

        MLog.log(TAG, "StartPlay --> " + oMedia.getMovie().getsUrl());
        this.isPlaying = true;
        this.oMedia = oMedia;
        this.oMedia.setMagicNum(MagicNum);
        this.oMedia.with(context);
        this.oMedia.setNormalRate();
        this.oMedia.callback(this);
        this.oMedia.onView(surfaceView);
        this.oMedia.playCache(downloadPath);
    }

    public void startPlay(String url) {
        oMedia = playingList.findByPath(url);
        if (oMedia == null) {
            oMedia = new OMedia(url);
        }
        startPlay(oMedia);
    }

    public void startPlay(FileDescriptor FD) {
        oMedia = playingList.findByPath(FD.toString());
        if (oMedia == null) {
            oMedia = new OMedia(FD);
        }
        startPlay(oMedia);
    }

    public void startPlay(AssetFileDescriptor AFD) {
        oMedia = playingList.findByPath(AFD.toString());
        if (oMedia == null) {
            oMedia = new OMedia(AFD);
        }
        startPlay(oMedia);
    }

    public void startPlay(Uri uri) {
        oMedia = playingList.findByPath(uri.getPath());
        if (oMedia == null) {
            oMedia = new OMedia(uri);
        }
        startPlay(oMedia);
    }

    public void startPlay(int Index) {
        oMedia = getMedia(Index);
        if (oMedia != null) {
            startPlay(oMedia);
        }
    }

    public void stopPlay() {
        if (oMedia != null) {
            oMedia.stop();
        }
    }

    public void resumePlay() {
        if (oMedia != null) {
            oMedia.resume();
        }
    }

    public void playPause() {
        if ((System.currentTimeMillis() - lStartTick) <= ACTION_DELAY)
            return;
        if (oMedia != null) {
            oMedia.playPause();
            lStartTick = System.currentTimeMillis();
        }
    }

    public void playNext() {
        if (oMedia != null) {
            OMedia oo = oMedia.getNext();
            if (oo != null) {
                MLog.log(TAG, "Next:" + oo.getPathName());
                startPlay(oo);
            }
        }
    }

    public void playPre() {
        if (oMedia != null) {
            OMedia oo = oMedia.getPre();
            if (oo != null) {
                MLog.log(TAG, "Pre:" + oo.getPathName());
                startPlay(oo);
            }
        }
    }

    public void setVolume(int var1) {
        if (oMedia != null)
            this.oMedia.setVolume(var1);
    }

    public int getVolume() {
        if (oMedia != null)
            return this.oMedia.getVolume();
        return 0;
    }

    public void setSurfaceView(SurfaceView surfaceView) {
        if (this.surfaceView == surfaceView) return;
        if (oMedia != null)
            oMedia.setSurfaceView(surfaceView);
        this.surfaceView = surfaceView;
    }

    public void setOption(String option) {
        if (oMedia != null)
            oMedia.setOption(option);
    }

    public VideoList getPlayingList() {
        return playingList;
    }

    public void setPlayPath(String CacheDir) {
        this.playPath = CacheDir;
        updateCachedFiles();
    }

    public String getDownloadPath() {
        return downloadPath;
    }

    public void setDownloadPath(String downloadPath) {
        this.downloadPath = downloadPath;
    }

    public void updateCachedFiles() {
        playingList.loadFromDir(playPath, SessionID.MEDIA_TYPE_ID_AllMEDIA);
    }

    public void addSource(String Url) {
        Movie movie = new Movie(Url);
        String filename = FilesManager.getFileName(movie.getsUrl());
        if (!TextUtils.isEmpty(filename))
            movie.setName(filename);
        playingList.add(new OMedia(movie));
    }

    public void addSource(FileDescriptor FD) {
        if (FD != null)
            playingList.add(new OMedia(FD));
    }

    public void addSource(AssetFileDescriptor AFD) {
        if (AFD != null)
            playingList.add(new OMedia(AFD));
    }

    public void addSource(Uri uri) {
        if (uri != null)
            playingList.add(new OMedia(uri));
    }

    public OMedia getMedia(String url) {
        OMedia oo = playingList.findByPath(oMedia.getMovie().getsUrl());
        return oo;
    }

    public OMedia getMedia(int Index) {
        if (playingList.getCount() <= 0)
            return null;
        return playingList.findByIndex(Index);
    }

    public void setMagicNum(int magicNum) {
        MagicNum = magicNum;
        if (oMedia != null)
            oMedia.setMagicNum(MagicNum);
    }

    public void setAutoPlaySource(int autoPlaySource) {
        this.autoPlaySource = autoPlaySource;
    }

    public void volumeUp() {
        setVolume(getVolume() + 5);
    }

    public void volumeDown() {
        setVolume(getVolume() - 5);
    }

    public void free() {
        try {
            playingList.clear();
            playingList = null;
            if (getPlayingMedia() != null)
                getPlayingMedia().free();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void autoPlay() {
        autoPlay(autoPlaySource);
    }

    public synchronized void autoPlay(int autoPlayType) {
        OMedia ooMedia = null;
        this.autoPlaySource = autoPlayType;
        if (autoPlaySource < SessionID.SESSION_SOURCE_ALL) return;
        if (isPlaying)
            return;

        switch (autoPlaySource) {
            case SessionID.SESSION_SOURCE_ALL:
            case SessionID.SESSION_SOURCE_PLAYLIST:
                ooMedia = playingList.getFirstItem();
                break;
            case SessionID.SESSION_SOURCE_MOBILE_USB:
                if (autoPlaySource == SessionID.SESSION_SOURCE_MOBILE_USB && sessionManager != null) {
                    ooMedia = sessionManager.getMobileSession().getVideos().getFirstItem();
                }
                break;
            case SessionID.SESSION_SOURCE_LOCAL_INTERNAL:
                if (autoPlaySource == SessionID.SESSION_SOURCE_LOCAL_INTERNAL && sessionManager != null) {
                    ooMedia = sessionManager.getLocalSession().getVideos().getFirstItem();
                }
                break;
            case SessionID.SESSION_SOURCE_EXTERNAL:
                if (autoPlaySource == SessionID.SESSION_SOURCE_EXTERNAL && sessionManager != null) {
                    ooMedia = sessionManager.getFileSession().getVideos().getFirstItem();
                }
                break;
            default:
                break;
        }
        if (ooMedia != null) {
            MLog.log(TAG, "Auto play " + ooMedia.getPathName());
            startPlay(ooMedia);
        }
    }

    //mMediaPlayer.getPlayerState()//int NothingSpecial=0;//int Opening=1;//int Buffering=2;//int Playing=3;//int Paused=4;
    //int Stopped=5; //int Ended=6;//int Error=7;
    @Override
    public void OnEventCallBack(int EventType, long TimeChanged, long LengthChanged, float PositionChanged, int OutCount, int ChangedType, int ChangedID, float Buffering, long Length) {
        //int ii = oMedia.getPlayStatus();
        switch (EventType) {
            case PlaybackEvent.Status_NothingIdle:
            case PlaybackEvent.Status_Opening:
            case PlaybackEvent.Status_Buffering:
            case PlaybackEvent.Status_Playing:
            case PlaybackEvent.Status_Paused:
            case PlaybackEvent.Status_Stopped:
            case PlaybackEvent.MediaChanged:
                break;
            case PlaybackEvent.Status_Ended:
            case PlaybackEvent.Status_Error:
                isPlaying = false;
                MLog.log(TAG, "OnEventCallBack.EventType=" + EventType + ",播放完成：" + oMedia.getPathName());
                playHandler.sendEmptyMessage(playOrder);//继续播放，跳到上一首或下一首
                break;
        }
        if (this.callback != null)
            this.callback.OnEventCallBack(EventType, TimeChanged, LengthChanged, PositionChanged, OutCount, ChangedType, ChangedID, Buffering, Length);
    }

    @Override
    public void OnSessionComplete(int sessionId, String result) {
        MLog.log(TAG, "OnSessionComplete: " + "sessionId = " + sessionId + ",result   = " + result);
        MLog.log(TAG, "OnSessionComplete: " + "sessionId = " + sessionId + ",autoPlay = " + autoPlaySource + ",getAllVideoList().size() = " + sessionManager.getAllMedias().size());
        if (sessionId <= 0) {
            if (isPlaying()) this.playPause();
        }
        if (sessionId == SessionID.SESSION_SOURCE_MOBILE_USB) {
            MLog.log(TAG, "OnSessionComplete: " + "sessionId = " + sessionId + ",getMobileSession().getVideoList().getCount() = " + sessionManager.getMobileSession().getVideoList().getCount());
            if (isPlaying() == false && (autoPlaySource == SessionID.SESSION_SOURCE_MOBILE_USB || autoPlaySource == SessionID.SESSION_SOURCE_ALL)) {
                playHandler.sendEmptyMessage(SessionID.PLAY_MANAGER_PLAY_ORDER1);
                return;
            }
        } else if (sessionId == SessionID.SESSION_SOURCE_LOCAL_INTERNAL) {
            MLog.log(TAG, "OnSessionComplete: " + "sessionId = " + sessionId + ",getLocalSession().getVideoList().getCount() = " + sessionManager.getLocalSession().getVideoList().getCount());
            if (isPlaying() == false && (autoPlaySource == SessionID.SESSION_SOURCE_LOCAL_INTERNAL || autoPlaySource == SessionID.SESSION_SOURCE_ALL)) {
                playHandler.sendEmptyMessage(SessionID.PLAY_MANAGER_PLAY_ORDER1);
                return;
            }
        } else if (sessionId == SessionID.SESSION_SOURCE_PATH) {
            MLog.log(TAG, "OnSessionComplete: " + "sessionId = " + sessionId + ",getMobileTFSession().getVideoList().getCount() = " + sessionManager.getFileSession().getVideoList().getCount());
            if (isPlaying() == false && (autoPlaySource == SessionID.SESSION_SOURCE_LOCAL_INTERNAL || autoPlaySource == SessionID.SESSION_SOURCE_ALL)) {
                playHandler.sendEmptyMessage(SessionID.PLAY_MANAGER_PLAY_ORDER1);
                return;
            }
        }

        if (isPlaying() == false && (autoPlaySource == SessionID.SESSION_SOURCE_ALL)) {
            playHandler.sendEmptyMessage(SessionID.PLAY_MANAGER_PLAY_ORDER1);
        }
    }

    @Override
    public void onRequestComplete(String Result, int Index) {
        //MLog.log(TAG, "onRequestComplete," + Result + "," + Index + ",autoPlaySource=" + autoPlaySource);
        if (autoPlaySource >= SessionID.SESSION_SOURCE_ALL)
            playHandler.sendEmptyMessage(SessionID.PLAY_MANAGER_PLAY_ORDER1);
    }

    private Handler playHandler = new Handler(Looper.myLooper()) {
        @SuppressLint(value = "HandlerLeak")
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            //MLog.log(TAG, "playHandler Msg = " + msg.toString());
            switch (msg.what) {
                case SessionID.PLAY_MANAGER_PLAY_ORDER1://继续自动播放
                    if (!isPlaying)
                        autoPlay(autoPlaySource);
                    break;
                case SessionID.PLAY_MANAGER_PLAY_ORDER2://循序列表循环
                    if (!isPlaying)
                        playNext();
                    break;
                case SessionID.PLAY_MANAGER_PLAY_ORDER3://反向列表循环
                    if (!isPlaying)
                        playPre();
                    break;
                case SessionID.PLAY_MANAGER_PLAY_ORDER4://单曲循环
                    if (!isPlaying)
                        startPlay(oMedia);
                    break;
                case SessionID.PLAY_MANAGER_PLAY_ORDER5://随机播放
                    if (!isPlaying)
                        startPlay(playingList.findAny());
                    break;
                case SessionID.PLAY_MANAGER_PLAY_ORDER6:
                default:
                    break;
            }
        }
    };

}