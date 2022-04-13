package com.zhuchao.android.playsession;

import static com.zhuchao.android.libfileutils.FilesManager.getDownloadDir;
import static com.zhuchao.android.libfileutils.FilesManager.getFileName;

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
import com.zhuchao.android.libfileutils.MLog;
import com.zhuchao.android.video.Movie;
import com.zhuchao.android.video.OMedia;
import com.zhuchao.android.video.VideoList;

import java.io.FileDescriptor;

public class PlayManager implements PlayerCallback, SessionCompleteCallback, NormalRequestCallback {
    private final String TAG = "PlayManager";
    private int MagicNum = 0;
    private Context context;
    private SurfaceView surfaceView = null;
    private OMedia oMedia = null;
    private boolean isPlaying = false;
    private PlayerCallback callback = null;
    private String cachePath = null;
    private int playOrder = SessionID.SESSION_SOURCE_ALL;
    private int autoPlay = SessionID.SESSION_SOURCE_NONE;
    private VideoList playingList = null;
    private OPlayerSessionManager sessionManager = null;

    public PlayManager(Context mContext, SurfaceView mSurfaceView) {
        this.context = mContext;
        this.surfaceView = mSurfaceView;
        playingList = new VideoList(this);
        cachePath = getDownloadDir(null);
        setMagicNum(0);
    }

    public void setSessionManager(OPlayerSessionManager sessionManager) {
        this.sessionManager = sessionManager;
        this.sessionManager.setUserSessionCallback(this);
    }

    public OMedia getPlayingMedia() {
        return oMedia;
    }

    public String getCachePath() {
        return cachePath;
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

    public PlayManager callback(PlayerCallback mCallback) {
        this.callback = mCallback;
        return this;
    }

    public void startPlay(OMedia oMedia) {
        if (surfaceView == null) {
            MLog.log(TAG, "no surfaceView ！！！！！！");
            return;
        }
        if (oMedia == null) {
            MLog.log(TAG, "There is no media to play！！！");
            return;
        } else if (!oMedia.isAvailable(cachePath)) {
            MLog.log(TAG, "The Source is not available! go next/pre---> " + oMedia.getMovie().getsUrl());
            playHandler.sendEmptyMessage(200);
            return;
        }

        MLog.log(TAG, "StartPlay --> " + oMedia.getMovie().getsUrl() + ", Last time = " + oMedia.getPlayTime());

        this.oMedia = oMedia;
        this.oMedia.setMagicNum(MagicNum);
        this.oMedia.with(context);
        this.oMedia.setNormalRate();
        this.oMedia.callback(this);
        this.oMedia.onView(surfaceView);
        this.oMedia.playCache(cachePath);
        this.isPlaying = true;
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
        if (oMedia != null)
            oMedia.stop();
    }

    public void resumePlay() {
        if (oMedia != null)
            oMedia.resume();
    }

    public void playPause() {
        if (oMedia != null)
            oMedia.playPause();
    }

    public void playNext() {
        if (oMedia != null)
            startPlay(oMedia.getNext());
    }

    public void playPre() {
        if (oMedia != null)
            startPlay(oMedia.getPre());
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

    //mMediaPlayer.getPlayerState()//int NothingSpecial=0;//int Opening=1;//int Buffering=2;//int Playing=3;//int Paused=4;
    //int Stopped=5; //int Ended=6;//int Error=7;
    @Override
    public void OnEventCallBack(int EventType, long TimeChanged, long LengthChanged, float PositionChanged, int OutCount, int ChangedType, int ChangedID, float Buffering, long Length) {
        int ii = oMedia.getPlayStatus();
        switch (ii) {
            case 0:
            case 1:
            case 2:
            case 3:
            case 4:
            case 5:
                break;
            case 6:
            case 7:
                if (playOrder == 0) //循序列表循环
                {
                    if (oMedia.getNext() != null) {
                        playHandler.sendEmptyMessage(101);
                    }
                } else if (playOrder == 1)//反向列表循环
                {
                    if (oMedia.getPre() != null) {
                        playHandler.sendEmptyMessage(102);
                    }
                } else if (playOrder == 2) //单曲循环
                {
                    playHandler.sendEmptyMessage(103);
                } else if (playOrder == 3) //随机播放
                {
                    playHandler.sendEmptyMessage(104);
                } else {
                    //单次播放，只播放一次
                }
                break;
        }
        if (this.callback != null)
            this.callback.OnEventCallBack(EventType, TimeChanged, LengthChanged, PositionChanged, OutCount, ChangedType, ChangedID, Buffering, Length);
    }

    public void setCachePath(String CacheDir) {
        this.cachePath = CacheDir;
        updateCachedFiles();
    }

    public void updateCachedFiles() {
        playingList.loadFromDir(cachePath, SessionID.MEDIA_TYPE_ID_AllMEDIA);
    }

    public void addSource(String Url) {
        Movie movie = new Movie(Url);
        String filename = getFileName(movie.getsUrl());
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

    public void setAutoPlay(int autoPlay) {
        this.autoPlay = autoPlay;
    }

    public void free() {
        try {
            playingList.clear();
            playingList = null;
            if (getPlayingMedia() != null)
                getPlayingMedia().stopFree();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void autoPlay()
    {
        autoPlay(autoPlay);
    }
    public void autoPlay(int autoPlayType) {
        OMedia ooMedia = null;
        this.autoPlay = autoPlayType;
        if (autoPlay < 0) return;

        if (oMedia != null)
        {
          if (isPlaying())
              return;
          if(oMedia.getPlayStatus() == PlaybackEvent.Status_Paused) {
              playPause();
              return;
          }
        }

        switch (autoPlay)
        {
            case SessionID.SESSION_SOURCE_ALL:
            case SessionID.SESSION_SOURCE_PLAYLIST:
                if (playingList.getCount() > 0 && autoPlay == SessionID.SESSION_SOURCE_ALL) {
                    ooMedia = playingList.findByIndex(0);
                    if (oMedia == null) {
                        startPlay(ooMedia);
                        break;
                    }
                }
                if(autoPlay == SessionID.SESSION_SOURCE_PLAYLIST)  break;
            case SessionID.SESSION_SOURCE_MOBILE_USB:
                if (autoPlay == SessionID.SESSION_SOURCE_MOBILE_USB && sessionManager != null)
                {
                    if (sessionManager.getMobileSession().getVideoList().getCount() > 0)
                        ooMedia = sessionManager.getMobileSession().getVideos().findByIndex(0);
                    if (oMedia == null)
                        startPlay(ooMedia);
                }
                break;
            case SessionID.SESSION_SOURCE_LOCAL_INTERNAL:
                if (autoPlay == SessionID.SESSION_SOURCE_LOCAL_INTERNAL && sessionManager != null)
                {
                    ooMedia = sessionManager.getLocalSession().getVideos().findByIndex(0);
                    if (oMedia == null)
                        startPlay(ooMedia);
                }
                break;
            case SessionID.SESSION_SOURCE_EXTERNAL:
                if (autoPlay == SessionID.SESSION_SOURCE_EXTERNAL && sessionManager != null)
                {
                    if (sessionManager.getFileSession().getVideoList().getCount() > 0)
                        ooMedia = sessionManager.getFileSession().getVideos().findByIndex(0);
                    if (oMedia == null)
                        startPlay(ooMedia);
                }
                break;
            default:
                break;
        }
    }

    //public static final int SESSION_TYPE_LOCALMEDIA = 10; //本地媒体
    @Override
    public void OnSessionComplete(int sessionId, String result) {
        MLog.log(TAG, "OnSessionComplete: " + "sessionId = " + sessionId + ",result   = " + result);
        MLog.log(TAG, "OnSessionComplete: " + "sessionId = " + sessionId + ",autoPlay = " + autoPlay + ",getAllVideoList().size() = " + sessionManager.getAllMedias().size());
        if (sessionId <= 0) {
            if (isPlaying()) this.playPause();
        }
        if (sessionId == SessionID.SESSION_SOURCE_MOBILE_USB) {
            MLog.log(TAG, "OnSessionComplete: " + "sessionId = " + sessionId + ",getMobileSession().getVideoList().getCount() = " + sessionManager.getMobileSession().getVideoList().getCount());
            if (isPlaying() == false && (autoPlay == SessionID.SESSION_SOURCE_MOBILE_USB || autoPlay == SessionID.SESSION_SOURCE_ALL)) {
                playHandler.sendEmptyMessage(100);
            }
        } else if (sessionId == SessionID.SESSION_SOURCE_LOCAL_INTERNAL) {
            MLog.log(TAG, "OnSessionComplete: " + "sessionId = " + sessionId + ",getLocalSession().getVideoList().getCount() = " + sessionManager.getLocalSession().getVideoList().getCount());
            if (isPlaying() == false && (autoPlay == SessionID.SESSION_SOURCE_LOCAL_INTERNAL || autoPlay == SessionID.SESSION_SOURCE_ALL)) {
                playHandler.sendEmptyMessage(100);
            }
        } else if (sessionId == SessionID.SESSION_SOURCE_PATH) {
            MLog.log(TAG, "OnSessionComplete: " + "sessionId = " + sessionId + ",getMobileTFSession().getVideoList().getCount() = " + sessionManager.getFileSession().getVideoList().getCount());
            if (isPlaying() == false && (autoPlay == SessionID.SESSION_SOURCE_LOCAL_INTERNAL || autoPlay == SessionID.SESSION_SOURCE_ALL)) {
                playHandler.sendEmptyMessage(100);
            }
        }

        if (isPlaying() == false && (autoPlay == SessionID.SESSION_SOURCE_LOCAL_INTERNAL || autoPlay == SessionID.SESSION_SOURCE_ALL)) {
            playHandler.sendEmptyMessage(100);
        }
        //MLog.log(TAG, "getMobileTFSession().getVideoList().getCount() = " + sessionManager.getMobileTFSession().getVideoList().getCount());
    }

    @Override
    public void onRequestComplete(String Result, int Index) {
        if (autoPlay >= 0)
           autoPlay(autoPlay);
    }

    private Handler playHandler = new Handler(Looper.getMainLooper()) {
        @SuppressLint(value = "HandlerLeak")
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.what) {
                case 100://自动播放
                    autoPlay(autoPlay);
                    break;
                case 101://循序列表循环
                    if (oMedia.getNext() != null)
                        startPlay(oMedia.getNext());
                    break;
                case 102://反向列表循环
                    if (playOrder == 1)
                        if (oMedia.getPre() != null)
                            startPlay(oMedia.getPre());
                    break;
                case 103://单曲循环
                    if (playOrder == 2)
                        startPlay(oMedia);
                    break;
                case 104://随机播放
                    if (playOrder == 3)
                        startPlay(playingList.findAny());
                    break;
                case 200://下一首或上一首
                    if (playOrder == 1)
                        playNext();
                    else if (playOrder == 2)
                        playPre();
                    break;
                default:
                    break;
            }
        }
    };

}