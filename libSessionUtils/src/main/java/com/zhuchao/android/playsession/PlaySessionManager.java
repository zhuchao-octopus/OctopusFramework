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
import com.zhuchao.android.libfileutils.MMLog;
import com.zhuchao.android.libfileutils.SessionID;
import com.zhuchao.android.video.Movie;
import com.zhuchao.android.video.OMedia;
import com.zhuchao.android.video.VideoList;

import java.io.FileDescriptor;

public class PlaySessionManager implements PlayerCallback, SessionCallback, NormalRequestCallback {
    private final String TAG = "PlayManager";
    private final int ACTION_DELAY = 500;
    private int MagicNum = 0;
    private Context context;
    private SurfaceView surfaceView = null;
    private OMedia oMedia = null;
    private boolean oMediaLoading = false;
    private PlayerCallback callback = null;
    private String playingListPath = null;
    private String downloadPath = null;
    private int playOrder = SessionID.PLAY_MANAGER_PLAY_ORDER2;
    private int autoPlaySource = SessionID.SESSION_SOURCE_NONE;
    private VideoList playingList = null;
    private VideoList favoriteList = null;
    private SessionManager sessionManager = null;
    private long lStartTick = 0;
    //private MonitorThread monitorThread = null;

    public PlaySessionManager(Context mContext, SurfaceView sfView) {
        this.context = mContext;
        this.surfaceView = sfView;
        downloadPath = FilesManager.getDownloadDir(null);//播放目录和，下载缓存目录不一样
        playingList = new VideoList(null);
        favoriteList = new VideoList(this);
        playingList.setTAG("PlayManager.VideoList0");
        playingList.setTAG("PlayManager.VideoList1");
        setMagicNum(0);
    }

    public PlaySessionManager callback(PlayerCallback mCallback) {
        this.callback = mCallback;
        this.lStartTick = 0;
        return this;
    }

    public void setSessionManager(SessionManager sessionManager) {
        this.sessionManager = sessionManager;
        this.sessionManager.setUserSessionCallback(this);
    }

    public OMedia getPlayingMedia() {
        return oMedia;
    }

    public String getPlayingListPath() {
        return playingListPath;
    }

    public int getPlayOrder() {
        return playOrder;
    }

    public void setPlayOrder(int playOrder) {
        this.playOrder = playOrder;
    }

    public boolean isPlaying() {
        if (oMedia == null) {
            return false;
        }
        int sta = oMedia.getPlayStatus();
        if (sta >= PlaybackEvent.Status_Opening && sta <= PlaybackEvent.Status_Playing)
            return true;
        else
            return false;
    }

    public synchronized void startPlay(OMedia oMedia) {
        //if (oMediaLoading) {
       //     MMLog.log(TAG, "oMedia is loading ！！！！！！ status = "+ getPlayerStatus());
       //     return;
        //}
        if (surfaceView == null) {
            MMLog.log(TAG, "no surfaceView ！！！！！！");
            return;
        }
        if (surfaceView.getHolder() == null) {
            MMLog.log(TAG, "surfaceView is not ready ！！！！！！");
            return;
        }
        if (oMedia == null) {
            MMLog.log(TAG, "There is no media to play！！！");
            return;
        } else if (!oMedia.isAvailable(playingListPath)) {
            MMLog.log(TAG, "The Source is not available! go next/pre---> " + oMedia.getMovie().getsUrl());
            playHandler.sendEmptyMessage(this.playOrder);
            return;
        }

        MMLog.log(TAG, "StartPlay --> " + oMedia.getMovie().getsUrl());
        //this.oMediaLoading = true;
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
        } else {
            autoPlay();
        }
    }

    public void playPause() {
        if ((System.currentTimeMillis() - lStartTick) <= ACTION_DELAY) {
            MMLog.log(TAG, "playPause() not allowed to do this now");
            return;
        }
        lStartTick = System.currentTimeMillis();
        if (oMedia == null) {
            autoPlay();
            return;
        }
        MMLog.log(TAG, "playPause() playStatus = " + oMedia.getPlayStatus());
        switch (oMedia.getPlayStatus()) {
            case PlaybackEvent.Status_NothingIdle:
            case PlaybackEvent.Status_Opening:
                oMedia.play();
                break;
            case PlaybackEvent.Status_Buffering:
                break;
            case PlaybackEvent.Status_Playing:
            case PlaybackEvent.Status_Paused:
                oMedia.playPause();
                break;
            case PlaybackEvent.Status_Stopped:
                resumePlay();
                break;
            case PlaybackEvent.Status_Ended:
            case PlaybackEvent.Status_Error:
            default:
                playNext();//play next
                break;
        }
    }

    public void playNext() {
        OMedia oo = getNextAvailable();//获取下一个有效的资源
        if (oo != null) {
            MMLog.log(TAG, "Go Next = " + oo.getPathName());
            startPlay(oo);
        } else {
            MMLog.log(TAG, "Next = null,go to auto play");
            autoPlay();
        }
    }

    public void playPre() {
        if (oMedia != null) {
            OMedia oo = oMedia.getPre();
            if (oo != null) {
                MMLog.log(TAG, "Pre:" + oo.getPathName());
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

    public void setSurfaceView(SurfaceView sfView) {
        if (this.surfaceView == sfView) return;
        this.surfaceView = sfView;
        if (oMedia != null) {
            oMedia.setSurfaceView(this.surfaceView);
        }
    }

    public void setOption(String option) {
        if (oMedia != null)
            oMedia.setOption(option);
    }

    public VideoList getPlayingList() {
        return playingList;
    }

    public VideoList getFavoriteList() {
        return favoriteList;
    }

    public synchronized void setPlayingListPath(String CachedPath) {
        this.playingListPath = CachedPath;
        playingList.loadFromDir(playingListPath, SessionID.MEDIA_TYPE_ID_AllMEDIA);
    }

    public String getDownloadPath() {
        return downloadPath;
    }

    public synchronized void setDownloadPath(String downloadPath) {
        this.downloadPath = downloadPath;
        this.favoriteList.loadFromDir(downloadPath, SessionID.MEDIA_TYPE_ID_AllMEDIA);
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

    public int getAutoPlaySource() {
        return autoPlaySource;
    }

    public void volumeUp() {
        setVolume(getVolume() + 5);
    }

    public void volumeDown() {
        setVolume(getVolume() - 5);
    }

    public int getPlayerStatus() {
        if (oMedia != null)
            return oMedia.getPlayStatus();
        else
            return PlaybackEvent.Status_NothingIdle;
    }

    public void free() {
        try {
            playingList.clear();
            playingList = null;
            if (getPlayingMedia() != null)
                getPlayingMedia().free();
        } catch (Exception e) {
            MMLog.e(TAG,"free() "+e.toString());
        }
    }

    private OMedia getNextAvailable() {
        OMedia ooMedia = null;
        MMLog.log(TAG, "available Count = " + playingList.getCount() + " | " + favoriteList.getCount());
        if (favoriteList.exist(oMedia))
            ooMedia = favoriteList.getNextAvailable(oMedia);
        else
            ooMedia = favoriteList.getNextAvailable(null);

        if (ooMedia == null)
        {
            if (playingList.exist(oMedia))
                ooMedia = playingList.getNextAvailable(oMedia);
            else
                ooMedia = playingList.getNextAvailable(null);
        }
        return ooMedia;
    }

    public void autoPlay() {
        //自动播放就是播放指定位置的第一个
        autoPlay(autoPlaySource);
    }

    public synchronized void autoPlay(int autoPlayType) {
        OMedia ooMedia = null;
        this.autoPlaySource = autoPlayType;
        if (autoPlaySource < SessionID.SESSION_SOURCE_ALL) return;
        if (isPlaying())
            return;

        switch (autoPlaySource) {
            case SessionID.SESSION_SOURCE_ALL:
            case SessionID.SESSION_SOURCE_PLAYLIST:
                if (favoriteList.getCount() > 0)
                    ooMedia = favoriteList.getFirstItem();//优先切换到第二收藏列表，优先播放收藏列表
                else
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
            MMLog.log(TAG, "Auto play " + ooMedia.getPathName());
            startPlay(ooMedia);
        } else {
            MMLog.log(TAG, "No oMedia found in playing list");
        }
    }

    //mMediaPlayer.getPlayerState()//int NothingSpecial=0;//int Opening=1;//int Buffering=2;//int Playing=3;//int Paused=4;
    //int Stopped=5; //int Ended=6;//int Error=7;
    @Override
    public void OnEventCallBack(int EventType, long TimeChanged, long LengthChanged, float PositionChanged, int OutCount, int ChangedType, int ChangedID, float Buffering, long Length) {
        switch (EventType) {
            case PlaybackEvent.Status_NothingIdle:
                oMediaLoading = false;//复位本地变量
                playHandler.sendEmptyMessage(playOrder);//继续播放，跳到上一首或下一首
                break;
            case PlaybackEvent.Status_Opening:
            case PlaybackEvent.Status_Buffering:
            case PlaybackEvent.Status_Playing:
            case PlaybackEvent.Status_Paused:
            case PlaybackEvent.Status_Stopped:
                oMediaLoading = false;//复位本地变量
            case PlaybackEvent.MediaChanged:
                break;
            case PlaybackEvent.Status_Ended:
            case PlaybackEvent.Status_Error:
                oMediaLoading = false;//复位本地变量
                MMLog.log(TAG, "OnEventCallBack.EventType = " + EventType + ", " + oMedia.getPathName());
                playHandler.sendEmptyMessage(playOrder);//继续播放，跳到上一首或下一首
                break;
        }
        if (this.callback != null)
            this.callback.OnEventCallBack(EventType, TimeChanged, LengthChanged, PositionChanged, OutCount, ChangedType, ChangedID, Buffering, Length);
    }

    @Override
    public void OnSessionComplete(int sessionId, String result) {
        MMLog.log(TAG, "OnSessionComplete: " + "sessionId = " + sessionId + ",result   = " + result);
        MMLog.log(TAG, "OnSessionComplete: " + "sessionId = " + sessionId + ",autoPlay = " + autoPlaySource + ",getAllVideoList().size() = " + sessionManager.getAllMedias().size());
        if (sessionId <= 0) {
            if (isPlaying()) this.playPause();
        }
        if (sessionId == SessionID.SESSION_SOURCE_MOBILE_USB) {
            MMLog.log(TAG, "OnSessionComplete: " + "sessionId = " + sessionId + ",getMobileSession().getVideoList().getCount() = " + sessionManager.getMobileSession().getVideoList().getCount());
            if (autoPlaySource == SessionID.SESSION_SOURCE_MOBILE_USB || autoPlaySource == SessionID.SESSION_SOURCE_ALL) {
                playHandler.sendEmptyMessage(SessionID.PLAY_MANAGER_PLAY_ORDER0);
                return;
            }
        } else if (sessionId == SessionID.SESSION_SOURCE_LOCAL_INTERNAL) {
            MMLog.log(TAG, "OnSessionComplete: " + "sessionId = " + sessionId + ",getLocalSession().getVideoList().getCount() = " + sessionManager.getLocalSession().getVideoList().getCount());
            if ((autoPlaySource == SessionID.SESSION_SOURCE_LOCAL_INTERNAL || autoPlaySource == SessionID.SESSION_SOURCE_ALL)) {
                playHandler.sendEmptyMessage(SessionID.PLAY_MANAGER_PLAY_ORDER0);
                return;
            }
        } else if (sessionId == SessionID.SESSION_SOURCE_PATH) {
            MMLog.log(TAG, "OnSessionComplete: " + "sessionId = " + sessionId + ",getMobileTFSession().getVideoList().getCount() = " + sessionManager.getFileSession().getVideoList().getCount());
            if ((autoPlaySource == SessionID.SESSION_SOURCE_LOCAL_INTERNAL || autoPlaySource == SessionID.SESSION_SOURCE_ALL)) {
                playHandler.sendEmptyMessage(SessionID.PLAY_MANAGER_PLAY_ORDER0);
                return;
            }
        }

        if ((autoPlaySource == SessionID.SESSION_SOURCE_ALL)) {
            playHandler.sendEmptyMessage(SessionID.PLAY_MANAGER_PLAY_ORDER0);
        }
    }

    @Override
    public void onRequestComplete(String Result, int Index) {
        //MLog.log(TAG, "onRequestComplete," + Result + "," + Index + ",autoPlaySource = " + autoPlaySource);
        if (Result.equals("PlayManager.VideoList1")) {
            playHandler.sendEmptyMessage(SessionID.PLAY_MANAGER_PLAY_ORDER1);
        } else if (autoPlaySource >= SessionID.SESSION_SOURCE_ALL)
            playHandler.sendEmptyMessage(SessionID.PLAY_MANAGER_PLAY_ORDER0);
    }

    private Handler playHandler = new Handler(Looper.getMainLooper()) {
        @SuppressLint(value = "HandlerLeak")
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            //MLog.log(TAG, "playHandler Msg = " + msg.toString());
            switch (msg.what) {
                case SessionID.PLAY_MANAGER_PLAY_ORDER0://自动播放(播放第一个)
                    if (!isPlaying())
                        autoPlay(autoPlaySource);
                    break;
                case SessionID.PLAY_MANAGER_PLAY_ORDER1://强制自动播放(播放第一个)
                    autoPlay(autoPlaySource);
                    break;
                case SessionID.PLAY_MANAGER_PLAY_ORDER2://循序列表循环
                    if (!isPlaying())
                        playNext();
                    else
                        MMLog.log(TAG, "playHandler isPlaying=" + isPlaying());
                    break;
                case SessionID.PLAY_MANAGER_PLAY_ORDER3://反向列表循环
                    if (!isPlaying())
                        playPre();
                    break;
                case SessionID.PLAY_MANAGER_PLAY_ORDER4://单曲循环
                    if (!isPlaying())
                        startPlay(oMedia);
                    break;
                case SessionID.PLAY_MANAGER_PLAY_ORDER5://随机播放
                    if (!isPlaying())
                        startPlay(playingList.findAny());
                    break;
                case SessionID.PLAY_MANAGER_PLAY_ORDER6:
                default:
                    break;
            }
        }
    };

}