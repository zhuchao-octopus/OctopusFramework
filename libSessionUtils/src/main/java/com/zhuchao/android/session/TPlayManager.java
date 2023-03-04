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
    private final Context context;
    private SurfaceView surfaceView = null;
    private OMedia oMediaPlaying = null;
    //private boolean oMediaLoading = false;
    private PlayerCallback callback = null;
    private int playOrder = DataID.PLAY_MANAGER_PLAY_ORDER2;
    private int autoPlaySource = DataID.SESSION_SOURCE_NONE;
    private ObjectList allPlayLists = null;
    private long lStartTick_Play = 0;
    private long lStartTick_Next = 0;
    private long lStartTick_Pre = 0;
    private int magicNumber = 0;
    private int tryCountForError = 3;
    private int tryPlayCount = 0;
    private long playTimeOut = 20000;
    private int playMethod = 2;
    private boolean playingLock = true;

    public TPlayManager(Context mContext, SurfaceView sfView) {
        this.context = mContext;
        this.surfaceView = sfView;
        allPlayLists = new ObjectList();
        setMagicNumber(0);
    }

    public int getTryCountForError() {
        return tryCountForError;
    }

    public void setTryCountForError(int tryCountForError) {
        this.tryCountForError = tryCountForError;
    }

    public void setPlayTimeOut(long playTimeOut) {
        this.playTimeOut = playTimeOut;
    }

    public void setPlayMethod(int playMethod) {
        this.playMethod = playMethod;
    }

    public boolean isPlayingLock() {
        return playingLock;
    }

    public void setPlayingLock(boolean playingLock) {
        this.playingLock = playingLock;
    }

    public TPlayManager callback(PlayerCallback mCallback) {
        this.callback = mCallback;
        return this;
    }

    public boolean isPlaying() {
        if (oMediaPlaying == null) {
            return false;
        }
        int sta = oMediaPlaying.getPlayStatus();
        return sta >= PlaybackEvent.Status_Opening && sta <= PlaybackEvent.Status_Playing;
    }

    public synchronized void startPlay(OMedia oMedia) {
        //if (oMediaLoading) {
        //    MMLog.log(TAG, "oMedia is loading! status = " + getPlayerStatus());
        //    return;
        //}

        if (surfaceView == null) {
            MMLog.log(TAG, "surfaceView  is not ready! return");
            return;
        }

        if (oMedia == null) {
            MMLog.log(TAG, "There is no media to play!");
            return;
        }

        if (!oMedia.isAvailable(null)) {
            MMLog.log(TAG, "The oMedia is not available! ---> " + oMedia.getMovie().getSrcUrl());
            return;
        }

        if(playingLock && oMediaPlaying != null)
        {
            if(oMedia.equals(oMediaPlaying) && oMediaPlaying.isPlaying()) {
                MMLog.log(TAG, "oMedia is playing lock for "+oMedia.getPathName());
                return;//同一个曲目不允许反复调用，播放锁定该调用
            }
        }

        {
            MMLog.log(TAG, "StartPlay--> " + oMedia.getMovie().getSrcUrl());
            //this.oMediaLoading = true;
            if (tryPlayCount <= 0)
                tryPlayCount = tryCountForError;
            this.oMediaPlaying = oMedia;
            this.oMediaPlaying.setMagicNumber(magicNumber);
            this.oMediaPlaying.with(context);
            this.oMediaPlaying.callback(this);
            if(playMethod > 0)
               this.oMediaPlaying.playOn_t(surfaceView);
            else
               this.oMediaPlaying.playOn(surfaceView);

            //this.oMediaPlaying.onView(surfaceView);//set surface view
            //this.oMedia.playCache(downloadPath);//set source path
            //this.oMediaPlaying.play();
            //this.oMediaLoading = false;
        }
    }

    public synchronized void startPlay(String url) {
        oMediaPlaying = getOMediaFromPlayLists(url);// defaultPlayingList.findByPath(url);
        if (oMediaPlaying == null) {
            oMediaPlaying = new OMedia(url);
        }
        startPlay(oMediaPlaying);
    }

    public synchronized void startPlay(FileDescriptor FD) {
        oMediaPlaying = getOMediaFromPlayLists(FD.toString());// defaultPlayingList.findByPath(FD.toString());
        if (oMediaPlaying == null) {
            oMediaPlaying = new OMedia(FD);
        }
        startPlay(oMediaPlaying);
    }

    public synchronized void startPlay(AssetFileDescriptor AFD) {
        oMediaPlaying = getOMediaFromPlayLists(AFD.toString());//defaultPlayingList.findByPath(AFD.toString());
        if (oMediaPlaying == null) {
            oMediaPlaying = new OMedia(AFD);
        }
        startPlay(oMediaPlaying);
    }

    public synchronized void startPlay(Uri uri) {
        oMediaPlaying = getOMediaFromPlayLists(uri.getPath());//defaultPlayingList.findByPath(uri.getPath());
        if (oMediaPlaying == null) {
            oMediaPlaying = new OMedia(uri);
        }
        startPlay(oMediaPlaying);
    }

    public synchronized void stopPlay() {
        if (oMediaPlaying != null) {
            oMediaPlaying.stop();
        }
    }

    public synchronized void stopFree() {
        if (oMediaPlaying != null) {
            oMediaPlaying.stopFree();
        }
    }

    public synchronized void resumePlay() {
        if (oMediaPlaying != null) {
            oMediaPlaying.resume();
        } else {
            autoPlay();
        }
    }

    public void playPause() {//非阻塞模式
        final int ACTION_DELAY = 600;
        if ((System.currentTimeMillis() - lStartTick_Play) <= ACTION_DELAY) {
            MMLog.log(TAG, "playPause() not allowed to do this now");
            return;
        }
        lStartTick_Play = System.currentTimeMillis();

        if (oMediaPlaying == null) {
            autoPlay();
            return;
        }
        MMLog.log(TAG, "playPause() playStatus = " + oMediaPlaying.getPlayStatus());
        switch (oMediaPlaying.getPlayStatus()) {
            //case PlaybackEvent.Status_NothingIdle:
            //     MMLog.log(TAG, "PlaybackEvent.Status_NothingIdle go next");
            //     playNext();//play next
            //     break;
            case PlaybackEvent.Status_Opening:
                if(oMediaPlaying.tTask_play.isTimeOut(playTimeOut))
                { //playTimeOut秒没有打开文件，结束播放
                    MMLog.log(TAG, "playPause() timeout = "+playTimeOut);
                    oMediaPlaying.stopFree();
                }
                break;
            case PlaybackEvent.Status_Buffering:
                break;
            case PlaybackEvent.Status_Playing:
            case PlaybackEvent.Status_Paused:
                if (oMediaPlaying.getFPlayer() != null && oMediaPlaying.getFPlayer().getPlayerStatusInfo().isSourcePrepared())
                    oMediaPlaying.playPause();
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
        final int ACTION_DELAY = 600;
        if ((System.currentTimeMillis() - lStartTick_Next) <= ACTION_DELAY) {
            MMLog.log(TAG, "playPause() not allowed to do this now");
            return;
        }
        lStartTick_Next = System.currentTimeMillis();

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
        final int ACTION_DELAY = 600;
        if ((System.currentTimeMillis() - lStartTick_Pre) <= ACTION_DELAY) {
            MMLog.log(TAG, "playPause() not allowed to do this now");
            return;
        }
        lStartTick_Pre = System.currentTimeMillis();

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
        if (oMediaPlaying != null) {
            oMediaPlaying.setSurfaceView(this.surfaceView);
        }
    }

    public synchronized void setSurfaceViewDelay(@NonNull SurfaceView sfView) {
        this.surfaceView = sfView;
    }

    public synchronized void attachSurfaceView(@NonNull SurfaceView sfView) {
        this.surfaceView = sfView;
        if (oMediaPlaying != null) {
            oMediaPlaying.reAttachSurfaceView(surfaceView);
        }
    }

    public void setOption(String option) {
        if (oMediaPlaying != null)
            oMediaPlaying.setOption(option);
    }

    public void setVolume(int var1) {
        if (oMediaPlaying != null)
            this.oMediaPlaying.setVolume(var1);
    }

    public int getVolume() {
        if (oMediaPlaying != null)
            return this.oMediaPlaying.getVolume();
        return 0;
    }

    public OMedia getPlayingMedia() {
        return oMediaPlaying;
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

    public int getTotalMediaCount() {
        if (allPlayLists.getCount() <= 0) return 0;
        int count = 0;
        Collection<Object> objects = allPlayLists.getAllObject();
        for (Object o : objects) {
            count = count + ((VideoList) o).getCount();
        }
        return count;
    }

    public void setMagicNumber(int magicNumber) {
        this.magicNumber = magicNumber;
        if (oMediaPlaying != null)
            oMediaPlaying.setMagicNumber(this.magicNumber);
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
        if (oMediaPlaying != null)
            return oMediaPlaying.getPlayStatus();
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
        if (oMediaPlaying == null) {
            for (Object o : objects) {
                OMedia oOMedia = ((VideoList) o).getNextAvailable(null);
                if (oOMedia != null) return oOMedia;
            }
            return null;
        }

        OMedia oOMedia = null;
        OMedia retOMedia = null;
        for (Object o : objects) {
            if (oMediaPlaying.equals(oOMedia))//是自己只有一首
                oOMedia = ((VideoList) o).getNextAvailable(null);//跳到下一个列表
            else
                oOMedia = ((VideoList) o).getNextAvailable(oMediaPlaying);

            if (oOMedia != null) {
                if (oMediaPlaying.equals(oOMedia))//是自己只有一首
                {
                    retOMedia = oOMedia;
                    continue;//多列表搜索下一个
                }
                return oOMedia;
            }
        }
        return retOMedia;
    }

    private OMedia getPreAvailable() {
        //OMedia ooMedia = null;
        Collection<Object> objects = allPlayLists.getAllObject();
        if (oMediaPlaying == null) {
            for (Object o : objects) {
                OMedia oOMedia = ((VideoList) o).getPreAvailable(null);
                if (oOMedia != null) return oOMedia;
            }
            return null;
        }

        for (Object o : objects) {
            OMedia oOMedia = ((VideoList) o).getPreAvailable(oMediaPlaying);
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
                    MMLog.log(TAG, "OnEventCallBack.EventType = " + playerStatusInfo.getEventType() + ", " + oMediaPlaying.getPathName());
                    //playEventHandler(playOrder);//继续播放，跳到上一首或下一首
                }
                break;
            case PlaybackEvent.Status_Opening:
            case PlaybackEvent.Status_Buffering:
            case PlaybackEvent.Status_Playing:
            case PlaybackEvent.Status_Paused:
            case PlaybackEvent.Status_Stopped:
            case PlaybackEvent.MediaChanged:
                break;
            case PlaybackEvent.Status_Error:
                MMLog.log(TAG, "OnEventCallBack.EventType = Status_Error, stop play " + oMediaPlaying.getPathName());
                break;
            case PlaybackEvent.Status_Ended:
                if (playerStatusInfo.getLastError() == PlaybackEvent.Status_Error)
                {//处理错误
                    playerStatusInfo.setLastError(PlaybackEvent.Status_Ended);
                    if (tryCountForError > 0 && tryPlayCount > 0)
                    {
                        playEventHandler(DataID.PLAY_MANAGER_PLAY_ORDER4);
                        tryPlayCount--;
                    } else {

                        playEventHandler(playOrder);//继续播放，跳到上一首或下一首
                    }
                }
                else
                {
                    tryPlayCount = tryCountForError;
                    MMLog.log(TAG, "OnEventCallBack.EventType = Status_Ended, " + oMediaPlaying.getPathName());
                    playEventHandler(playOrder);//继续播放，跳到上一首或下一首

                }
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
            case DataID.PLAY_MANAGER_PLAY_ORDER4://单曲循环或者再次播放当前歌曲
                if (!isPlaying())
                    startPlay(oMediaPlaying);
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
            if (oMediaPlaying == null || oMediaPlaying.getFPlayer() == null) return;
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