package com.zhuchao.android.session;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.res.AssetFileDescriptor;
import android.net.Uri;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.view.SurfaceView;

import androidx.annotation.NonNull;

import com.zhuchao.android.eventinterface.NormalCallback;
import com.zhuchao.android.eventinterface.PlaybackEvent;
import com.zhuchao.android.eventinterface.PlayerCallback;
import com.zhuchao.android.eventinterface.PlayerStatusInfo;
import com.zhuchao.android.fbase.DataID;
import com.zhuchao.android.fbase.MMLog;
import com.zhuchao.android.fbase.ObjectList;
import com.zhuchao.android.video.OMedia;
import com.zhuchao.android.video.VideoList;

import java.io.FileDescriptor;
import java.util.Collection;

public class TPlayManager implements PlayerCallback, NormalCallback {
    private final String TAG = "PlayManager";
    private final int ACTION_DELAY = 600;
    private int magicNumber = 0;
    private Context context;
    private SurfaceView surfaceView = null;
    private OMedia oMedia = null;
    private boolean oMediaLoading = false;
    private PlayerCallback callback = null;
    private int playOrder = DataID.PLAY_MANAGER_PLAY_ORDER2;
    private int autoPlaySource = DataID.SESSION_SOURCE_NONE;
    private ObjectList allPlayLists = null;
    private long lStartTick = 0;

    public TPlayManager(Context mContext, SurfaceView sfView) {
        this.context = mContext;
        this.surfaceView = sfView;
        allPlayLists = new ObjectList();
        setMagicNumber(0);
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
        } else if (!oMedia.isAvailable(null)) {
            MMLog.log(TAG, "The oMedia is not available! ---> " + oMedia.getMovie().getsUrl());
            return;
        }

        MMLog.log(TAG, "StartPlay--> " + oMedia.getMovie().getsUrl());
        this.oMediaLoading = true;
        this.oMedia = oMedia;
        this.oMedia.setMagicNumber(magicNumber);
        this.oMedia.with(context);
        //this.oMedia.setNormalRate();
        this.oMedia.callback(this);
        this.oMedia.onView(surfaceView);//set surface view
        //this.oMedia.playCache(downloadPath);//set source path
        this.oMedia.play();
        this.oMediaLoading = false;
    }

    public synchronized void startPlay(String url) {
        oMedia = getOMediaFromPlayLists(url);// defaultPlayingList.findByPath(url);
        if (oMedia == null) {
            oMedia = new OMedia(url);
        }
        startPlay(oMedia);
    }

    public synchronized void startPlay(FileDescriptor FD) {
        oMedia = getOMediaFromPlayLists(FD.toString());// defaultPlayingList.findByPath(FD.toString());
        if (oMedia == null) {
            oMedia = new OMedia(FD);
        }
        startPlay(oMedia);
    }

    public synchronized void startPlay(AssetFileDescriptor AFD) {
        oMedia = getOMediaFromPlayLists(AFD.toString());//defaultPlayingList.findByPath(AFD.toString());
        if (oMedia == null) {
            oMedia = new OMedia(AFD);
        }
        startPlay(oMedia);
    }

    public synchronized void startPlay(Uri uri) {
        oMedia = getOMediaFromPlayLists(uri.getPath());//defaultPlayingList.findByPath(uri.getPath());
        if (oMedia == null) {
            oMedia = new OMedia(uri);
        }
        startPlay(oMedia);
    }

    public synchronized void stopPlay() {
        if (oMedia != null) {
            oMedia.stop();
        }
    }

    public synchronized void stopFree() {
        if (oMedia != null) {
            oMedia.stopFree();
        }
    }

    public synchronized void resumePlay() {
        if (oMedia != null) {
            oMedia.resume();
        } else {
            autoPlay();
        }
    }

    public void playPause() {//非阻塞模式
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
            //case PlaybackEvent.Status_NothingIdle:
            //MMLog.log(TAG, "PlaybackEvent.Status_NothingIdle go next");
            //     playNext();//play next
            //     break;
            case PlaybackEvent.Status_Opening:
            case PlaybackEvent.Status_Buffering:
                break;
            case PlaybackEvent.Status_Playing:
            case PlaybackEvent.Status_Paused:
                if (oMedia.getFPlayer() != null && oMedia.getFPlayer().getPlayerStatusInfo().isSourcePrepared())
                    oMedia.playPause();
                else
                    autoPlay();
                break;
            case PlaybackEvent.Status_Stopped:
                resumePlay();
                break;
            case PlaybackEvent.Status_Ended:
            case PlaybackEvent.Status_Error:
            case PlaybackEvent.Status_NothingIdle:
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

    public synchronized void setSurfaceView(@NonNull SurfaceView sfView) {
        this.surfaceView = sfView;
        if (oMedia != null) {
            oMedia.setSurfaceView(this.surfaceView);
        }
    }

    public synchronized void setSurfaceViewDelay(@NonNull SurfaceView sfView) {
        this.surfaceView = sfView;
    }

    public synchronized void attachSurfaceView(@NonNull SurfaceView sfView) {
        this.surfaceView = sfView;
        if (oMedia != null) {
            oMedia.reAttachSurfaceView(surfaceView);
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

    public int getPlayOrder() {
        return playOrder;
    }

    public void setPlayOrder(int playOrder) {
        this.playOrder = playOrder;
    }

    public void addPlayList(String name, VideoList videoList) {
        allPlayLists.addItem(name, videoList);
    }

    public VideoList getPlayList(String name) {
        return (VideoList) allPlayLists.getObject(name);
    }

    public void DeletePlayList(String name) {
        allPlayLists.delete(name);
    }

    public void DeleteALLPlayList() {
        allPlayLists.clear();
    }

    //public synchronized void setPlayingPath(String CachedPath) {
    //    this.playingPath = CachedPath;
    //    defaultPlayingList.loadFromDir(playingPath, DataID.MEDIA_TYPE_ID_AllMEDIA);
    //}

    //public String getDownloadPath() {
    //    return downloadPath;
    //}

    /*public synchronized void setDownloadPath(String downloadDirectory) {
        this.downloadPath = FileUtils.getDownloadDir(downloadDirectory);//播放目录和，下载缓存目录不一样;
        if (EmptyString(downloadPath)) {
            MMLog.log(TAG, "get getDownloadDir() failed downloadPath = " + this.downloadPath);
            return;
        }
        this.favoriteList.loadFromDir(this.downloadPath, DataID.MEDIA_TYPE_ID_AllMEDIA);
    }

    public void updatePlayingList() {
        if (FileUtils.existDirectory(playingPath)) {
            defaultPlayingList.loadFromDir(playingPath, DataID.MEDIA_TYPE_ID_AllMEDIA);
        }
        MMLog.log(TAG, "updatePlayingList() = " + playingPath);
        if (FileUtils.existDirectory(downloadPath)) {
            this.favoriteList.loadFromDir(downloadPath, DataID.MEDIA_TYPE_ID_AllMEDIA);
        }
        MMLog.log(TAG, "updatePlayingList() = " + downloadPath);
    }

    public void addSource(String Url) {
        Movie movie = new Movie(Url);
        String fileName = FileUtils.getFileName(movie.getsUrl());
        if (NotEmptyString(fileName))
            movie.setName(fileName);
        defaultPlayingList.add(new OMedia(movie));
    }

    public void addSource(FileDescriptor FD) {
        if (FD != null)
            defaultPlayingList.add(new OMedia(FD));
    }


    public void addSource(AssetFileDescriptor AFD) {
        if (AFD != null)
            defaultPlayingList.add(new OMedia(AFD));
    }

    public void addSource(Uri uri) {
        if (uri != null)
            defaultPlayingList.add(new OMedia(uri));
    }

    public OMedia getMedia(String url) {
        OMedia oo = defaultPlayingList.findByPath(oMedia.getMovie().getsUrl());
        return oo;
    }

    public OMedia getMedia(int Index) {
        if (defaultPlayingList.getCount() <= 0)
            return null;
        return defaultPlayingList.findByIndex(Index);
    }
*/
    public void setMagicNumber(int magicNumber) {
        this.magicNumber = magicNumber;
        if (oMedia != null)
            oMedia.setMagicNumber(this.magicNumber);
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
            if (getPlayingMedia() != null)
                getPlayingMedia().free();
            allPlayLists.clear();
        } catch (Exception e) {
            MMLog.e(TAG, "free() " + e.toString());
        }
    }

    private OMedia getNextAvailable() {
        Collection<Object> objects = allPlayLists.getAllObject();
        if (oMedia == null) {
            for (Object o : objects) {
                OMedia oOMedia = ((VideoList) o).getNextAvailable(null);
                if (oOMedia != null) return oOMedia;
            }
            return null;
        }

        for (Object o : objects) {
            OMedia oOMedia = ((VideoList) o).getNextAvailable(oMedia);
            if (oOMedia != null) return oOMedia;
        }
        return null;
    }

    private OMedia getPreAvailable() {
        //OMedia ooMedia = null;
        Collection<Object> objects = allPlayLists.getAllObject();
        if (oMedia == null) {
            for (Object o : objects) {
                OMedia oOMedia = ((VideoList) o).getPreAvailable(null);
                if (oOMedia != null) return oOMedia;
            }
            return null;
        }

        for (Object o : objects) {
            OMedia oOMedia = ((VideoList) o).getPreAvailable(oMedia);
            if (oOMedia != null) return oOMedia;
        }
        return null;
    }

    public OMedia getOMediaFromPlayLists(String urlPath) {
        Collection<Object> objects = allPlayLists.getAllObject();
        for (Object o : objects) {
            OMedia oMedia = ((VideoList) o).findByPath(urlPath);
            if (oMedia != null) return oMedia;
        }
        return null;
    }

    public OMedia getFirstOMediaFromPlayLists() {
        Collection<Object> objects = allPlayLists.getAllObject();
        for (Object o : objects) {
            OMedia oMedia = ((VideoList) o).getFirstItem();
            if (oMedia != null) return oMedia;
        }
        return null;
    }

    public OMedia getRandomOMediaFromPlayLists() {
        Collection<Object> objects = allPlayLists.getAllObject();
        for (Object o : objects) {
            OMedia oMedia = ((VideoList) o).findAny();
            if (oMedia != null) return oMedia;
        }
        return null;
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
                //ooMedia = defaultPlayingList.getFirstItem();
                //break;
            case DataID.SESSION_SOURCE_ALL:
            case DataID.SESSION_SOURCE_FAVORITELIST:
                //if (favoriteList.getCount() > 0)
                //    ooMedia = favoriteList.getFirstItem();//优先切换到第二收藏列表，优先播放收藏列表
                //
                ooMedia = getFirstOMediaFromPlayLists();//defaultPlayingList.getFirstItem();
                break;
            default:
                break;
        }
        //String str = " (playingList = " + defaultPlayingList.getCount() + ",favoriteList = " + favoriteList.getCount() + ")";
        if (ooMedia != null) {
            //MMLog.log(TAG, "Auto play    " + ooMedia.getPathName() + str);
            ooMedia.setRestorePlay(true);
            startPlay(ooMedia);
        } else {
            //MMLog.log(TAG, "No oMedia found in playing list");
        }
    }

    @Override
    public void onEventPlayerStatus(PlayerStatusInfo playerStatusInfo) {
        if (this.callback != null) {
            //this.callback.OnEventCallBack(EventType, TimeChanged, LengthChanged, PositionChanged, OutCount, ChangedType, ChangedID, Buffering, Length);
            Message msg = playHandler.obtainMessage();
            msg.obj = playerStatusInfo;
            msg.what = playerStatusInfo.getEventType();
            msg.arg1 = (int) playerStatusInfo.getPositionChanged();
            msg.arg2 = (int) playerStatusInfo.getLengthChanged();
            playHandler.sendMessage(msg);
        }

        switch (playerStatusInfo.getEventType()) {
            case PlaybackEvent.Status_NothingIdle:
                if (autoPlaySource >= DataID.SESSION_SOURCE_ALL) //立即跳转到收藏列表
                {
                    MMLog.log(TAG, "OnEventCallBack.EventType = " + playerStatusInfo.getEventType() + ", " + oMedia.getPathName());
                    playEventHandler(playOrder);//继续播放，跳到上一首或下一首
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
                MMLog.log(TAG, "OnEventCallBack.EventType = Status_Ended, " + oMedia.getPathName());
                playEventHandler(playOrder);//继续播放，跳到上一首或下一首
                break;
            case PlaybackEvent.Status_Error:
                MMLog.log(TAG, "OnEventCallBack.EventType = Status_Error, stop play " + oMedia.getPathName());
                playEventHandler(playOrder);//继续播放，跳到上一首或下一首
                break;
        }
    }

    @Override
    public void onEventRequest(String Result, int Index) {

    }

    private void playEventHandler(int playOrder) {
        switch (playOrder) {
            case DataID.PLAY_MANAGER_PLAY_ORDER0://自动播放第一个
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
                    startPlay(getRandomOMediaFromPlayLists());
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
            PlayerStatusInfo playerStatusInfo = (PlayerStatusInfo) msg.obj;
            if (oMedia == null || oMedia.getFPlayer() == null) return;
            switch (playerStatusInfo.getEventType()) {
                case PlaybackEvent.Status_NothingIdle:
                case PlaybackEvent.Status_Opening:
                case PlaybackEvent.Status_Buffering:
                case PlaybackEvent.Status_Playing:
                case PlaybackEvent.Status_Ended:
                case PlaybackEvent.Status_Error:
                    break;
            }
            callback.onEventPlayerStatus(playerStatusInfo);
        }
    };

}