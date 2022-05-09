package com.zhuchao.android.session;

import static com.zhuchao.android.libfileutils.FileUtils.EmptyString;
import static com.zhuchao.android.libfileutils.FileUtils.NotEmptyString;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.res.AssetFileDescriptor;
import android.net.Uri;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.view.SurfaceView;

import com.zhuchao.android.callbackevent.NormalCallback;
import com.zhuchao.android.callbackevent.PlaybackEvent;
import com.zhuchao.android.callbackevent.PlayerCallback;
import com.zhuchao.android.libfileutils.DataID;
import com.zhuchao.android.libfileutils.FileUtils;
import com.zhuchao.android.libfileutils.MMLog;
import com.zhuchao.android.video.Movie;
import com.zhuchao.android.video.OMedia;
import com.zhuchao.android.video.VideoList;

import java.io.FileDescriptor;

public class TPlayManager implements PlayerCallback, NormalCallback {
    private final String TAG = "PlayManager";
    private final int ACTION_DELAY = 500;
    private int MagicNum = 0;
    private Context context;
    private SurfaceView surfaceView = null;
    private OMedia oMedia = null;
    private boolean oMediaLoading = false;
    private PlayerCallback callback = null;
    private String playingPath = null;
    private String downloadPath = null;
    private int playOrder = DataID.PLAY_MANAGER_PLAY_ORDER2;
    private int autoPlaySource = DataID.SESSION_SOURCE_NONE;
    private VideoList playingList = null;
    private VideoList favoriteList = null;
    private long lStartTick = 0;

    public TPlayManager(Context mContext, SurfaceView sfView) {
        this.context = mContext;
        this.surfaceView = sfView;
        downloadPath = FileUtils.getDownloadDir(null);//播放目录和，下载缓存目录不一样
        playingList = new VideoList(null);
        favoriteList = new VideoList(this);
        playingList.setTAG("PlayManager.VideoList0");
        playingList.setTAG("PlayManager.VideoList1");
        setMagicNum(0);
    }

    public TPlayManager callback(PlayerCallback mCallback) {
        this.callback = mCallback;
        this.lStartTick = 0;
        return this;
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
        if (oMediaLoading) {
            MMLog.log(TAG, "oMedia is loading! status = " + getPlayerStatus());
            return;
        }
        if (surfaceView == null) {
            MMLog.log(TAG, "surfaceView  is not ready! return");
            return;
        }

        if (oMedia == null) {
            MMLog.log(TAG, "There is no media to play!");
            return;
        } else if (!oMedia.isAvailable(playingPath)) {
            MMLog.log(TAG, "The oMedia is not available! ---> " + oMedia.getMovie().getsUrl());
            return;
        }

        MMLog.log(TAG, "StartPlay--> " + oMedia.getMovie().getsUrl());
        this.oMediaLoading = true;
        this.oMedia = oMedia;
        this.oMedia.setMagicNum(MagicNum);
        this.oMedia.with(context);
        //this.oMedia.setNormalRate();
        this.oMedia.callback(this);
        this.oMedia.onView(surfaceView);//set surface view
        this.oMedia.playCache(downloadPath);//set source
        this.oMediaLoading = false;
    }

    public synchronized void startPlay(String url) {
        oMedia = playingList.findByPath(url);
        if (oMedia == null) {
            oMedia = new OMedia(url);
        }
        startPlay(oMedia);
    }

    public synchronized void startPlay(FileDescriptor FD) {
        oMedia = playingList.findByPath(FD.toString());
        if (oMedia == null) {
            oMedia = new OMedia(FD);
        }
        startPlay(oMedia);
    }

    public synchronized void startPlay(AssetFileDescriptor AFD) {
        oMedia = playingList.findByPath(AFD.toString());
        if (oMedia == null) {
            oMedia = new OMedia(AFD);
        }
        startPlay(oMedia);
    }

    public synchronized void startPlay(Uri uri) {
        oMedia = playingList.findByPath(uri.getPath());
        if (oMedia == null) {
            oMedia = new OMedia(uri);
        }
        startPlay(oMedia);
    }

    public synchronized void startPlay(int Index) {
        oMedia = getMedia(Index);
        if (oMedia != null) {
            startPlay(oMedia);
        }
    }

    public synchronized void stopPlay() {
        if (oMedia != null) {
            oMedia.stop();
        }
    }

    public synchronized void stopFree() {
        if (oMedia != null) {
            oMedia.stop();
            oMedia.free();
        }
    }

    public synchronized void resumePlay() {
        if (oMedia != null) {
            oMedia.setSurfaceView(this.surfaceView);
            oMedia.resume();
        } else {
            autoPlay();
        }
    }

    public synchronized void playPause() {
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
                MMLog.log(TAG, "PlaybackEvent.Status_NothingIdle go next");
                playNext();//play next
                break;
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

    public synchronized void playNext() {
        OMedia oo = getNextAvailable();//获取下一个有效的资源
        if (oo != null) {
            MMLog.log(TAG, "Go to next = " + oo.getPathName());
            startPlay(oo);
        } else {
            MMLog.log(TAG, "Next = null,go to auto play");
            autoPlay();
        }
    }

    public synchronized void playPre() {
        OMedia oo = getPreAvailable();//获取上一个有效的资源
        if (oo != null) {
            MMLog.log(TAG, "Go Prev = " + oo.getPathName());
            startPlay(oo);
        } else {
            MMLog.log(TAG, "Prev = null,go to auto play");
            autoPlay();
        }
    }

    public synchronized void setSurfaceView(SurfaceView sfView) {
        if (sfView == null) {
            MMLog.log(TAG, "setSurfaceView = null");
            return;
        }
        if (sfView.equals(this.surfaceView)) {
            MMLog.log(TAG, "setSurfaceView same surface view = " + sfView.getId());
            return;
        }
        this.surfaceView = sfView;
        if (oMedia != null) {
            oMedia.setSurfaceView(this.surfaceView);
        }
    }

    public void setOption(String option) {
        if (oMedia != null)
            oMedia.setOption(option);
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

    public OMedia getPlayingMedia() {
        return oMedia;
    }

    public String getPlayingPath() {
        return playingPath;
    }

    public int getPlayOrder() {
        return playOrder;
    }

    public void setPlayOrder(int playOrder) {
        this.playOrder = playOrder;
    }

    public VideoList getPlayingList() {
        return playingList;
    }

    public VideoList getFavoriteList() {
        return favoriteList;
    }

    public synchronized void setPlayingPath(String CachedPath) {
        this.playingPath = CachedPath;
        playingList.loadFromDir(playingPath, DataID.MEDIA_TYPE_ID_AllMEDIA);
    }

    public String getDownloadPath() {
        return downloadPath;
    }

    public synchronized void setDownloadPath(String downloadDirectory) {
        this.downloadPath = FileUtils.getDownloadDir(downloadDirectory);//播放目录和，下载缓存目录不一样;
        if(EmptyString(downloadPath)) {
            MMLog.log(TAG, "get getDownloadDir() failed downloadPath = " + this.downloadPath);
            return;
        }
        this.favoriteList.loadFromDir(this.downloadPath, DataID.MEDIA_TYPE_ID_AllMEDIA);
    }

    public void updatePlayingList()
    {
        if (FileUtils.existDirectory(playingPath)) {
            playingList.loadFromDir(playingPath, DataID.MEDIA_TYPE_ID_AllMEDIA);
        }
        MMLog.log(TAG,"updatePlayingList() = " + playingPath);
        if(FileUtils.existDirectory(downloadPath)) {
            this.favoriteList.loadFromDir(downloadPath, DataID.MEDIA_TYPE_ID_AllMEDIA);
        }
        MMLog.log(TAG,"updatePlayingList() = " + downloadPath);
    }
    public void addSource(String Url) {
        Movie movie = new Movie(Url);
        String fileName = FileUtils.getFileName(movie.getsUrl());
        if(NotEmptyString(fileName))
            movie.setName(fileName);
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
            MMLog.e(TAG, "free() " + e.toString());
        }
    }

    private OMedia getNextAvailable() {
        OMedia ooMedia = null;
        MMLog.log(TAG, "getNextAvailable() playingList.count = "+playingList.getCount()+" favoriteList.count = "+favoriteList.getCount());
        if (favoriteList.exist(oMedia))
            ooMedia = favoriteList.getNextAvailable(oMedia);
        else
            ooMedia = favoriteList.getNextAvailable(null);

        if (ooMedia == null) {
            if (playingList.exist(oMedia))
                ooMedia = playingList.getNextAvailable(oMedia);
            else
                ooMedia = playingList.getNextAvailable(null);
        }
        return ooMedia;
    }

    private OMedia getPreAvailable() {
        OMedia ooMedia = null;
        MMLog.log(TAG, "getPreAvailable() Count = " + playingList.getCount() + " | " + favoriteList.getCount());
        if (favoriteList.exist(oMedia))
            ooMedia = favoriteList.getPreAvailable(oMedia);
        else
            ooMedia = favoriteList.getPreAvailable(null);

        if (ooMedia == null) {
            if (playingList.exist(oMedia))
                ooMedia = playingList.getPreAvailable(oMedia);
            else
                ooMedia = playingList.getPreAvailable(null);
        }
        return ooMedia;
    }

    public synchronized void autoPlay() {
        //自动播放就是播放指定位置的第一个
        if (isPlaying())
            return;
        autoPlay(autoPlaySource);
    }

    public synchronized void autoPlay(int autoPlayType) {
        OMedia ooMedia = null;
        this.autoPlaySource = autoPlayType;
        if (autoPlaySource == DataID.SESSION_SOURCE_NONE) return;

        switch (autoPlaySource) {
            case DataID.SESSION_SOURCE_PLAYLIST:
                ooMedia = playingList.getFirstItem();
                break;
            case DataID.SESSION_SOURCE_ALL:
            case DataID.SESSION_SOURCE_FAVORITELIST:
                if (favoriteList.getCount() > 0)
                    ooMedia = favoriteList.getFirstItem();//优先切换到第二收藏列表，优先播放收藏列表
                else
                    ooMedia = playingList.getFirstItem();
                break;
            default:
                break;
        }

        MMLog.log(TAG, "playingList.count = "+playingList.getCount()+",favoriteList.count = "+favoriteList.getCount());
        if (ooMedia != null) {
            MMLog.log(TAG, "Auto play " + ooMedia.getPathName());
            startPlay(ooMedia);
        } else {
           //MMLog.log(TAG, "No oMedia found in playing list");
        }
    }

    @Override
    public void onEventPlayerStatus(int EventType, long TimeChanged, long LengthChanged, float PositionChanged, int OutCount, int ChangedType, int ChangedID, float Buffering, long Length) {
        if (this.callback != null) {
            //this.callback.OnEventCallBack(EventType, TimeChanged, LengthChanged, PositionChanged, OutCount, ChangedType, ChangedID, Buffering, Length);
            Message msg = playHandler.obtainMessage();
            msg.what = EventType;
            msg.arg1 = (int) PositionChanged;
            msg.arg2 = (int) Length;
            playHandler.sendMessage(msg);
        }
        switch (EventType) {
            case PlaybackEvent.Status_NothingIdle:
                if (autoPlaySource >= DataID.SESSION_SOURCE_ALL) {
                    MMLog.log(TAG, "OnEventCallBack.EventType = " + EventType + ", " + oMedia.getPathName());
                    playEventHandler(playOrder);
                }
                break;
            case PlaybackEvent.Status_Opening:
            case PlaybackEvent.Status_Buffering:
            case PlaybackEvent.Status_Playing:
            case PlaybackEvent.Status_Paused:
            case PlaybackEvent.Status_Stopped:
            case PlaybackEvent.MediaChanged:
                break;
            case PlaybackEvent.Status_Ended:
            case PlaybackEvent.Status_Error:
                MMLog.log(TAG, "OnEventCallBack.EventType = " + EventType + ", " + oMedia.getPathName());
                playEventHandler(playOrder);//继续播放，跳到上一首或下一首
                break;
        }
    }

    @Override
    public void onEventRequest(String Result, int Index) {
        //MLog.log(TAG, "onRequestComplete," + Result + "," + Index + ",autoPlaySource = " + autoPlaySource);
        if (autoPlaySource == DataID.SESSION_SOURCE_FAVORITELIST) //立即跳转到收藏列表
        {
            if (favoriteList.getTAG().equals(Result)) {
                if (!isPlaying() || !favoriteList.exist(oMedia)) {
                    autoPlay(autoPlaySource);
                }
            }
        }
    }

    private void playEventHandler(int playOrder) {
        switch (playOrder) {
            case DataID.PLAY_MANAGER_PLAY_ORDER0://播放第一个
                autoPlay();
                break;
            case DataID.PLAY_MANAGER_PLAY_ORDER1://强制播放第一个
                autoPlay(autoPlaySource);
                break;
            case DataID.PLAY_MANAGER_PLAY_ORDER2://循序列表循环
                if (!isPlaying())
                    playNext();
                else
                    MMLog.log(TAG, "playEventHandler isPlaying=" + isPlaying());
                break;
            case DataID.PLAY_MANAGER_PLAY_ORDER3://反向列表循环
                if (!isPlaying())
                    playPre();
                break;
            case DataID.PLAY_MANAGER_PLAY_ORDER4://单曲循环
                if (!isPlaying())
                    startPlay(oMedia);
                break;
            case DataID.PLAY_MANAGER_PLAY_ORDER5://随机播放
                if (!isPlaying())
                    startPlay(playingList.findAny());
                break;
            case DataID.PLAY_MANAGER_PLAY_ORDER6:
            default:
                break;
        }
    }

    private Handler playHandler = new Handler(Looper.getMainLooper()) {
        @SuppressLint(value = "HandlerLeak")
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            callback.onEventPlayerStatus(msg.what, msg.arg1, msg.arg1, msg.arg1, 0, 0, 0, 0, msg.arg2);
        }
    };

}