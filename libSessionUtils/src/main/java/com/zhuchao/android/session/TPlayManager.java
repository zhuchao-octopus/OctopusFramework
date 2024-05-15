package com.zhuchao.android.session;

import static android.content.Context.BIND_AUTO_CREATE;
import static com.zhuchao.android.fbase.FileUtils.MD5;
import static com.zhuchao.android.fbase.MessageEvent.MESSAGE_EVENT_AIDL_MUSIC_CLASS_NAME;
import static com.zhuchao.android.fbase.MessageEvent.MESSAGE_EVENT_AIDL_PACKAGE_NAME;
import static com.zhuchao.android.fbase.MessageEvent.MESSAGE_EVENT_OCTOPUS_ACTION_MULTIMEDIA_SERVICE;
import static com.zhuchao.android.fbase.MessageEvent.MESSAGE_EVENT_OCTOPUS_AIDL_PLAYING_STATUS;
import static com.zhuchao.android.fbase.MessageEvent.MESSAGE_EVENT_OCTOPUS_NEXT;
import static com.zhuchao.android.fbase.MessageEvent.MESSAGE_EVENT_OCTOPUS_PAUSE;
import static com.zhuchao.android.fbase.MessageEvent.MESSAGE_EVENT_OCTOPUS_PLAY;
import static com.zhuchao.android.fbase.MessageEvent.MESSAGE_EVENT_OCTOPUS_PLAY_PAUSE;
import static com.zhuchao.android.fbase.MessageEvent.MESSAGE_EVENT_OCTOPUS_PREV;
import static com.zhuchao.android.fbase.MessageEvent.MESSAGE_EVENT_OCTOPUS_STOP;
import static com.zhuchao.android.fbase.PlaybackEvent.Status_NothingIdle;

import android.annotation.SuppressLint;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.res.AssetFileDescriptor;
import android.net.Uri;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.os.Message;
import android.os.RemoteException;
import android.view.SurfaceView;

import androidx.annotation.NonNull;

import com.zhuchao.android.car.aidl.IMyAidlInterfaceListener;
import com.zhuchao.android.car.aidl.IMyMediaAidlInterface;
import com.zhuchao.android.car.aidl.PEventCourier;
import com.zhuchao.android.car.aidl.PMovie;
import com.zhuchao.android.fbase.DataID;
import com.zhuchao.android.fbase.EventCourier;
import com.zhuchao.android.fbase.FileUtils;
import com.zhuchao.android.fbase.MMLog;
import com.zhuchao.android.fbase.MessageEvent;
import com.zhuchao.android.fbase.ObjectList;
import com.zhuchao.android.fbase.PlaybackEvent;
import com.zhuchao.android.fbase.PlayerStatusInfo;
import com.zhuchao.android.fbase.TAppProcessUtils;
import com.zhuchao.android.fbase.eventinterface.PlayerCallback;
import com.zhuchao.android.fbase.eventinterface.SessionCallback;
import com.zhuchao.android.video.Movie;
import com.zhuchao.android.video.OMedia;
import com.zhuchao.android.video.VideoList;

import java.io.FileDescriptor;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Objects;

public class TPlayManager implements PlayerCallback, SessionCallback {
    private final String TAG = "PlayManager";
    private final Context mContext;
    private SurfaceView surfaceView = null;
    private OMedia oMediaPlaying = null;
    private OMedia oMediaSearching = null;
    ///private boolean oMediaLoading = false;
    private PlayerCallback mUserCallback = null;
    private int playOrder = DataID.PLAY_MANAGER_PLAY_ORDER2;
    private int autoPlaySource = DataID.SESSION_SOURCE_FAVORITELIST;
    private long lStartTick_Play = 0;
    private long lStartTick_Next = 0;
    private long lStartTick_Pre = 0;
    private int magicNumber = 0;
    private int tryCountForError = 3;
    private int tryPlayCount = 0;
    private long playTimeOut = 20000;
    private int playMethod = 2;
    private boolean playingLock = true;
    private int videoOutWidth = 0;
    private int videoOutHeight = 0;
    private final ObjectList allPlayLists = new ObjectList();

    private VideoList mLocalMediaVideos = new VideoList();
    private VideoList mLocalUSBMediaVideos = new VideoList();
    private VideoList mLocalSDMediaVideos = new VideoList();

    private VideoList mLocalMediaAudios = new VideoList();
    private VideoList mLocalUSBMediaAudios = new VideoList();
    private VideoList mLocalSDMediaAudios = new VideoList();

    private final VideoList mPlayingList = new VideoList();
    private final VideoList mPlayingHistoryList = new VideoList();
    private final VideoList mFavouriteList = new VideoList();//收藏

    private ObjectList mArtistList = new ObjectList();
    private ObjectList mAlbumList = new ObjectList();
    private ObjectList mAlbumListID = new ObjectList();
    private TMediaManager tMediaManager = null;///由于代理的原因需要额外初始化
    private boolean isClientProxy = false;
    ////////////////////////////////////////////////////////////////////////////////////////////////
    ////////////////////////////////////////////////////////////////////////////////////////////////
    public final String PLAY_LIST_NAME_PLAY_LIST = "play.list";
    public final String PLAY_LIST_NAME_LOCAL_VIDEO = "local.video";
    public final String PLAY_LIST_NAME_LOCAL_AUDIO = "local.audio";
    public final String PLAY_LIST_NAME_LOCAL_USB_VIDEO = "local.usb.video";
    public final String PLAY_LIST_NAME_LOCAL_USB_AUDIO = "local.usb.audio";
    public final String PLAY_LIST_NAME_LOCAL_SD_VIDEO = "local.sd.video";
    public final String PLAY_LIST_NAME_LOCAL_SD_AUDIO = "local.sd.audio";
    @SuppressLint("StaticFieldLeak")
    private static TPlayManager tPlayManager = null;

    public synchronized static TPlayManager getInstance(Context context) {
        if (tPlayManager == null && context != null) {
            tPlayManager = new TPlayManager(context, null);
            ///tPlayManager.setPlayOrder(DataID.PLAY_MANAGER_PLAY_ORDER2);//循环顺序播放
            tPlayManager.setAutoPlaySource(DataID.SESSION_SOURCE_FAVORITELIST);//自动播放源列表
        }
        return tPlayManager;
    }

    public synchronized static TPlayManager getInstance() {
        return tPlayManager;
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////
    ////////////////////////////////////////////////////////////////////////////////////////////////
    public TPlayManager(Context context, SurfaceView sfView) {
        this.mContext = context;
        this.surfaceView = sfView;
        this.setMagicNumber(0);
        ///this.allPlayLists = new ObjectList();
        this.allPlayLists.addObject(PLAY_LIST_NAME_PLAY_LIST, mPlayingList);
        this.allPlayLists.addObject(PLAY_LIST_NAME_LOCAL_VIDEO, mLocalMediaVideos);
        this.allPlayLists.addObject(PLAY_LIST_NAME_LOCAL_AUDIO, mLocalMediaAudios);
        this.allPlayLists.addObject(PLAY_LIST_NAME_LOCAL_USB_VIDEO, mLocalUSBMediaVideos);
        this.allPlayLists.addObject(PLAY_LIST_NAME_LOCAL_USB_AUDIO, mLocalUSBMediaAudios);
        this.allPlayLists.addObject(PLAY_LIST_NAME_LOCAL_SD_VIDEO, mLocalSDMediaVideos);
        this.allPlayLists.addObject(PLAY_LIST_NAME_LOCAL_SD_AUDIO, mLocalSDMediaVideos);

        this.mFavouriteList.setListName("Favourite.list");
        this.mPlayingHistoryList.setListName("Playing.history.list");
        ///Cabinet.getEventBus().registerEventObserver(this);
        loadFromFile();
        if (!TAppProcessUtils.getProcessName(context).equals(MessageEvent.MESSAGE_EVENT_AIDL_PROCESS_SERVICE_NAME)) {
            ///MMLog.d(TAG,"Init play manager "+TAppProcessUtils.getProcessName(context));
            isClientProxy = true;
            initialMyMediaAidlInterface(mContext);
        } else {
            isClientProxy = false;
        }
    }

    public void updateMusicsToPlayList() {
        this.allPlayLists.clear();
        this.allPlayLists.addObject(PLAY_LIST_NAME_LOCAL_AUDIO, mLocalMediaAudios);
        this.allPlayLists.addObject(PLAY_LIST_NAME_LOCAL_USB_AUDIO, mLocalUSBMediaAudios);
        this.allPlayLists.addObject(PLAY_LIST_NAME_LOCAL_SD_AUDIO, mLocalSDMediaVideos);
    }

    public void updateMoviesToPlayList() {
        this.allPlayLists.clear();
        this.allPlayLists.addObject(PLAY_LIST_NAME_LOCAL_VIDEO, mLocalMediaVideos);
        this.allPlayLists.addObject(PLAY_LIST_NAME_LOCAL_USB_VIDEO, mLocalUSBMediaVideos);
        this.allPlayLists.addObject(PLAY_LIST_NAME_LOCAL_SD_VIDEO, mLocalSDMediaVideos);
    }

    public void updateAllPlayList() {
        this.allPlayLists.clear();
        this.allPlayLists.addObject(PLAY_LIST_NAME_PLAY_LIST, mPlayingList);
        this.allPlayLists.addObject(PLAY_LIST_NAME_LOCAL_VIDEO, mLocalMediaVideos);
        this.allPlayLists.addObject(PLAY_LIST_NAME_LOCAL_AUDIO, mLocalMediaAudios);
        this.allPlayLists.addObject(PLAY_LIST_NAME_LOCAL_USB_VIDEO, mLocalUSBMediaVideos);
        this.allPlayLists.addObject(PLAY_LIST_NAME_LOCAL_USB_AUDIO, mLocalUSBMediaAudios);
        this.allPlayLists.addObject(PLAY_LIST_NAME_LOCAL_SD_VIDEO, mLocalSDMediaVideos);
        this.allPlayLists.addObject(PLAY_LIST_NAME_LOCAL_SD_AUDIO, mLocalSDMediaVideos);
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

    public TPlayManager callback(PlayerCallback callback) {
        this.mUserCallback = callback;
        return this;
    }

    public void registerStatusListener(PlayerCallback callback) {
        this.mUserCallback = callback;
    }

    public boolean isLocalPlaying() {
        if (oMediaPlaying == null) {
            return false;
        }
        int sta = oMediaPlaying.getPlayStatus();
        return sta >= PlaybackEvent.Status_Opening && sta <= PlaybackEvent.Status_Playing;
    }

    public boolean isPlaying() {
        if (isLocalPlaying()) return true;
        else return isAidlProxyPlaying();
    }

    public synchronized void restartPlay() {
        boolean playingLock_old = playingLock;
        if (oMediaPlaying == null) {
            MMLog.log(TAG, "There is no media to restartPlay!");
            return;
        }
        playingLock = false;//stopPlay();
        MMLog.log(TAG, "restartPlay " + oMediaPlaying.getPathName());
        startPlay(oMediaPlaying);
        playingLock = playingLock_old;
    }

    private void stopLocalPlayer() {
        if (isLocalPlaying()) oMediaPlaying.stop();
    }

    public void setTime(long time) {
        if (hasPlayAidlProxy() && isAidlProxyPlaying()) {
            try {
                getMyMediaAidlInterface().setTime(time);
            } catch (RemoteException e) {
                ///throw new RuntimeException(e);
            }
        } else if (oMediaPlaying != null) {
            oMediaPlaying.setTime(time);
        }
    }

    public synchronized void startPlay(OMedia oMedia) {

        if (surfaceView == null && oMedia.isVideo()) {
            MMLog.log(TAG, "Don't forget to set surfaceView,it is not ready!");
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

        if (hasPlayAidlProxy() && oMedia.isAudio()) {//后台代理播放
            mediaAidlProxyInterfaceAction(MESSAGE_EVENT_OCTOPUS_PLAY, oMedia);
            mPlayingHistoryList.add(new OMedia(oMedia.getPathName()));
            stopLocalPlayer();//停止本地播放
            return;
        } else {
            stopProxyPlayer();
        }

        if (playingLock && oMediaPlaying != null) {
            if (oMedia.equals(oMediaPlaying) && oMediaPlaying.isPlaying()) {
                MMLog.log(TAG, "oMedia is playing lock for " + oMediaPlaying.getPathName());
                return;//同一个曲目不允许反复调用，播放锁定该调用
            }
        }

        MMLog.log(TAG, "StartPlay--> " + oMedia.getMovie().getSrcUrl() + ",playMethod = " + playMethod);
        //this.oMediaLoading = true;
        if (tryPlayCount <= 0) tryPlayCount = tryCountForError;
        this.oMediaPlaying = oMedia;
        this.oMediaPlaying.with(mContext);
        this.oMediaPlaying.setMagicNumber(magicNumber);


        this.oMediaPlaying.callback(this);

        ///if (surfaceView == null)
        /// MMLog.log(TAG, "surfaceView = null");
        ///else
        /// MMLog.log(TAG, "surfaceView = " + surfaceView.toString());

        oMediaPlaying.setScale(0);
        oMediaPlaying.setAspectRatio(null);

        if (playMethod > 0) this.oMediaPlaying.playOn_t(surfaceView);
        else this.oMediaPlaying.playOn(surfaceView);

        ///this.oMediaPlaying.onView(surfaceView);//set surface view
        ///this.oMedia.playCache(downloadPath);//set source path
        ///this.oMediaPlaying.play();
        ///this.oMediaLoading = false;
        mPlayingHistoryList.add(new OMedia(oMediaPlaying.getPathName()));
        ///MMLog.log(TAG, "Playing " + oMediaPlaying.getPathName());
    }

    public synchronized void startPlay(String url) {
        OMedia oMedia = getOMediaFromPlayLists(url);// defaultPlayingList.findByPath(url);
        if (oMedia == null) {
            oMedia = new OMedia(url);
        }
        startPlay(oMedia);
    }

    public synchronized void startPlay(FileDescriptor FD) {
        OMedia oMedia = getOMediaFromPlayLists(FD.toString());// defaultPlayingList.findByPath(FD.toString());
        if (oMedia == null) {
            oMedia = new OMedia(FD);
        }
        startPlay(oMedia);
    }

    public synchronized void startPlay(AssetFileDescriptor AFD) {
        OMedia oMedia = getOMediaFromPlayLists(AFD.toString());//defaultPlayingList.findByPath(AFD.toString());
        if (oMedia == null) {
            oMedia = new OMedia(AFD);
        }
        startPlay(oMedia);
    }

    public synchronized void startPlay(Uri uri) {
        OMedia oMedia = getOMediaFromPlayLists(uri.getPath());//defaultPlayingList.findByPath(uri.getPath());
        if (oMedia == null) {
            oMedia = new OMedia(uri);
        }
        startPlay(oMedia);
    }

    public synchronized void stopPlay() {
        if (oMediaPlaying != null) {
            oMediaPlaying.stop();
            if (hasPlayAidlProxy()) {
                try {
                    getMyMediaAidlInterface().playStop();
                } catch (RemoteException ignored) {
                }
            }
        }
    }

    public synchronized void stopIdle() {
        if (oMediaPlaying != null) {
            oMediaPlaying.stop();
            mediaAidlProxyInterfaceAction(MESSAGE_EVENT_OCTOPUS_STOP, oMediaPlaying);
            oMediaPlaying = null;
        }
    }

    public synchronized void stopFree() {
        if (oMediaPlaying != null) {
            MMLog.log(TAG, "call stopFree()");
            oMediaPlaying.stopFree();
            mediaAidlProxyInterfaceAction(MESSAGE_EVENT_OCTOPUS_STOP, oMediaPlaying);
            oMediaPlaying = null;
        }
    }

    public synchronized void stopFreeFree() {
        if (oMediaPlaying != null) {
            MMLog.log(TAG, "call stopFree()");
            oMediaPlaying.stopFreeFree();
            mediaAidlProxyInterfaceAction(MESSAGE_EVENT_OCTOPUS_STOP, oMediaPlaying);
            oMediaPlaying = null;
        }
    }

    public synchronized void stopFree_t() {
        if (oMediaPlaying != null) {
            MMLog.log(TAG, "call stopFree_t()");
            oMediaPlaying.stopFree_t();
            mediaAidlProxyInterfaceAction(MESSAGE_EVENT_OCTOPUS_STOP, oMediaPlaying);
            oMediaPlaying = null;
        }
    }

    public synchronized void resumePlay() {
        if (oMediaPlaying != null) {
            MMLog.log(TAG, "resume to play ");
            oMediaPlaying.resume();
            //startPlay(oMediaPlaying);
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

        if ((isAidlProxyPaused() || isAidlProxyPlaying())) {
            mediaAidlProxyInterfaceAction(MESSAGE_EVENT_OCTOPUS_PLAY_PAUSE, oMediaPlaying);
            return;
        }

        if (oMediaPlaying == null) {
            if (!hasPlayAidlProxy()) autoPlay();
            return;
        }
        MMLog.log(TAG, "playPause() playStatus = " + oMediaPlaying.getPlayStatus());
        switch (oMediaPlaying.getPlayStatus()) {
            ///case PlaybackEvent.Status_NothingIdle:
            ///MMLog.log(TAG, "PlaybackEvent.Status_NothingIdle go next");
            ///playNext();//play next
            ///break;
            case PlaybackEvent.Status_Opening:
            case PlaybackEvent.Status_Buffering:
                if (oMediaPlaying.tTask_play.isTimeOut(playTimeOut)) { //playTimeOut秒没有打开文件，结束播放
                    MMLog.log(TAG, "playPause() timeout = " + playTimeOut);
                    //oMediaPlaying.stopFree();
                    stopFree();
                }
                break;
            ///case PlaybackEvent.Status_Buffering:
            ///    break;
            case PlaybackEvent.Status_Playing:
            case PlaybackEvent.Status_Paused:
                MMLog.log(TAG, "playPause() timeout = " + playTimeOut);
                if (oMediaPlaying.getFPlayer() != null && oMediaPlaying.getFPlayer().getPlayerStatusInfo().isSourcePrepared())
                    oMediaPlaying.playPause_t();
                else autoPlay();
                break;
            case PlaybackEvent.Status_Stopped:
                resumePlay();
                break;
            case PlaybackEvent.Status_Ended:
            case PlaybackEvent.Status_Error:
                if (!hasPlayAidlProxy()) playNext();//play next
                break;
            case PlaybackEvent.Status_NothingIdle:
            default:
                if (!hasPlayAidlProxy()) autoPlay();
                break;
        }
    }

    public synchronized void playNext() {
        final int ACTION_DELAY = 600;
        if ((System.currentTimeMillis() - lStartTick_Next) <= ACTION_DELAY) {
            MMLog.log(TAG, "playNext() not allowed to do this now");
            return;
        }
        lStartTick_Next = System.currentTimeMillis();

        OMedia oMedia = null;
        if (playOrder == DataID.PLAY_MANAGER_PLAY_ORDER5) oMedia = getRandomOMediaFromPlayLists();///随机模式获取下一个随机对象
        else oMedia = getNextAvailable();//获取下一个有效的资源

        if (oMedia != null) {
            MMLog.log(TAG, "Go to next " + oMedia.getPathName());
            startPlay(oMedia);
        } else {
            MMLog.log(TAG, "Next is null,start auto play");
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

        OMedia oMedia = null;
        if (playOrder == DataID.PLAY_MANAGER_PLAY_ORDER5) oMedia = getRandomOMediaFromPlayLists();///随机模式获取下一个随机对象
        else oMedia = getPreAvailable();//获取上一个有效的资源
        if (oMedia != null) {
            MMLog.log(TAG, "Go to prev = " + oMedia.getPathName());
            startPlay(oMedia);
        } else {
            MMLog.log(TAG, "Prev is  null,start auto play");
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

    public synchronized void attachSurfaceView(SurfaceView sfView) {
        this.surfaceView = sfView;
        if (oMediaPlaying != null) {
            oMediaPlaying.reAttachSurfaceView(surfaceView);
        }
    }

    public void setOption(String option) {
        if (oMediaPlaying != null) oMediaPlaying.setOption(option);
    }

    public void setVolume(int var1) {
        if (oMediaPlaying != null) this.oMediaPlaying.setVolume(var1);
    }

    public int getVolume() {
        if (oMediaPlaying != null) return this.oMediaPlaying.getVolume();
        return 0;
    }

    public void setMediaToPlay(OMedia oMediaPlaying) {
        this.oMediaPlaying = oMediaPlaying;
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

    public void deletePlayList(String name) {
        allPlayLists.delete(name);
    }

    public void deleteALLPlayList() {
        allPlayLists.clear();
    }

    public ObjectList getAllPlayLists() {
        return allPlayLists;
    }

    public void createPlayingListOrder(VideoList videoList) {
        String name = "VideoList_" + FileUtils.getRandom(10000);
        allPlayLists.clear();
        allPlayLists.addObject(name, videoList);
        videoList.updateLinkOrder();
    }

    public void createPlayingListOrder(String name, VideoList videoList) {
        if (FileUtils.EmptyString(name)) {
            name = "VideoList_" + FileUtils.getRandom(10000);
        }
        allPlayLists.clear();
        allPlayLists.addObject(name, videoList);
        videoList.updateLinkOrder();
    }

    public void createPlayingListOrder(String name, VideoList videoList, boolean append) {
        if (FileUtils.EmptyString(name)) {
            name = "VideoList_" + FileUtils.getRandom(10000);
        }
        if (!append) allPlayLists.clear();
        allPlayLists.addObject(name, videoList);
        videoList.updateLinkOrder();
    }

    public VideoList getLocalMediaVideos() {
        return mLocalMediaVideos;
    }

    public VideoList getLocalUSBMediaVideos() {
        return mLocalUSBMediaVideos;
    }

    public VideoList getLocalSDMediaVideos() {
        return mLocalSDMediaVideos;
    }

    public VideoList getLocalMediaAudios() {
        return mLocalMediaAudios;
    }

    public VideoList getLocalUSBMediaAudios() {
        return mLocalUSBMediaAudios;
    }

    public VideoList getLocalSDMediaAudios() {
        return mLocalSDMediaAudios;
    }

    public VideoList getPlayingList() {
        return mPlayingList;
    }

    public VideoList getPlayingHistoryList() {
        return mPlayingHistoryList;
    }

    public VideoList getFavouriteList() {
        return mFavouriteList;
    }

    public void setMagicNumber(int magicNumber) {
        this.magicNumber = magicNumber;
        if (oMediaPlaying != null) oMediaPlaying.setMagicNumber(this.magicNumber);
    }

    public int getMagicNumber() {
        return magicNumber;
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

    public TMediaManager getMediaManager() {
        return tMediaManager;
    }

    public ObjectList getArtistList() {
        return mArtistList;
    }

    public ObjectList getAlbumList() {
        return mAlbumList;
    }

    public ObjectList getAlbumListID() {
        return mAlbumListID;
    }

    public void setWindowSize(int width, int height) {
        if (oMediaPlaying != null) {
            oMediaPlaying.setWindowSize(width, height);
        }
        this.videoOutWidth = width;
        this.videoOutHeight = height;
    }

    public void setFitSize(int width, int height) {
        if (oMediaPlaying != null) {
            oMediaPlaying.setWindowSize(width, height);
            oMediaPlaying.setScale(0);
            oMediaPlaying.setAspectRatio(null);
        }
        this.videoOutWidth = width;
        this.videoOutHeight = height;
    }

    public int getPlayerStatus() {
        if (oMediaPlaying != null) return oMediaPlaying.getPlayStatus();
        else return Status_NothingIdle;
    }

    private OMedia getNextAvailable() {
        Collection<Object> objects = allPlayLists.getAllObject();
        if (oMediaPlaying == null) {
            for (Object o : objects) {
                OMedia oOMedia = null;
                if (oMediaSearching != null) oOMedia = ((VideoList) o).getNextAvailable(oMediaSearching);
                else oOMedia = ((VideoList) o).getNextAvailable(null);

                if (oOMedia != null) {
                    oMediaSearching = oOMedia;
                    return oOMedia;
                }
            }
            return null;
        }

        OMedia oOMedia = null;
        OMedia retOMedia = null;
        for (Object o : objects) {
            if (oMediaPlaying.equals(oOMedia))//是自己只有一首
                oOMedia = ((VideoList) o).getNextAvailable(null);//跳到下一个列表的第一个
            else oOMedia = ((VideoList) o).getNextAvailable(oMediaPlaying);

            if (oOMedia != null) {//只有一首,多列表搜索下一个
                if (oMediaPlaying.equals(oOMedia))//是自己只有一首
                {
                    retOMedia = oOMedia;
                    continue;//多列表搜索下一个，跳到下一个列表
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
                OMedia oOMedia = null;
                if (oMediaSearching != null) oOMedia = ((VideoList) o).getPreAvailable(oMediaSearching);
                else oOMedia = ((VideoList) o).getPreAvailable(null);

                if (oOMedia != null) {
                    oMediaSearching = oOMedia;
                    return oOMedia;
                }
            }
            return null;
        }

        for (Object o : objects) {
            OMedia oOMedia = ((VideoList) o).getPreAvailable(oMediaPlaying);
            if (oOMedia != null) return oOMedia;
        }
        return null;
    }

    /////////////////////////////////////////////////////////////////////////////////////////////////////////////
    /////////////////////////////////////////////////////////////////////////////////////////////////////////////

    public VideoList getAllMedia() {
        VideoList videoList = new VideoList();
        ///Collection<Object> objects = allPlayLists.getAllObject();
        ///for (Object o : objects) {
        ///    videoList.loadAllRawMediaFrom((VideoList) o);
        ///}
        videoList.loadAllRawAudioFrom(mLocalMediaAudios);
        videoList.loadAllRawVideoFrom(mLocalMediaVideos);
        return videoList;
    }

    public VideoList getAllMusic() {
        VideoList videoList = new VideoList();
        ///Collection<Object> objects = allPlayLists.getAllObject();
        ///for (Object o : objects) {
        ///    videoList.loadAllRawAudioFrom((VideoList) o);
        ///}
        ///videoList.loadAllRawAudioFrom(mLocalMediaAudios);
        return mLocalMediaAudios;
    }

    public VideoList getAllVideo() {
        ///VideoList videoList = new VideoList();
        ///Collection<Object> objects = allPlayLists.getAllObject();
        ///for (Object o : objects) {
        ///    videoList.loadAllRawVideoFrom((VideoList) o);
        ///}
        return mLocalMediaVideos;
    }

    public int getAllMediaCount() {
        ///if (allPlayLists.getCount() <= 0) return 0;
        ///int count = 0;
        ///Collection<Object> objects = allPlayLists.getAllObject();
        ///for (Object o : objects) {
        ///    count = count + ((VideoList) o).getCount();
        ///}
        return mLocalMediaAudios.getCount() + mLocalMediaVideos.getCount();
    }

    public OMedia searchMediaFromMediaLibrary(String filePathName) {
        OMedia oMedia = null;
        if (FileUtils.EmptyString(filePathName)) return null;
        oMedia = mLocalMediaVideos.findByPath(filePathName);
        if (oMedia != null) return oMedia;
        oMedia = mLocalMediaAudios.findByPath(filePathName);
        return oMedia;
    }

    public OMedia searchMediaFromMediaLibrary(Movie movie) {
        OMedia oMedia = null;
        if (movie == null || FileUtils.EmptyString(movie.getSrcUrl())) return null;

        oMedia = mLocalMediaVideos.findByMovie(movie);
        if (oMedia != null) return oMedia;
        oMedia = mLocalMediaAudios.findByMovie(movie);
        if (oMedia != null) return oMedia;

        oMedia = mLocalMediaVideos.findByPath(movie.getSrcUrl());
        if (oMedia != null) return oMedia;
        oMedia = mLocalMediaAudios.findByPath(movie.getSrcUrl());
        return oMedia;
    }

    public OMedia getOMediaFromPlayLists(String urlPath) {

        try {
            Collection<Object> objects = allPlayLists.getAllObject();
            for (Object o : objects) {
                OMedia oMedia = ((VideoList) o).findByPath(urlPath);
                if (oMedia != null) return oMedia;
            }
        } catch (Exception e) {
            MMLog.e(TAG, String.valueOf(e));
        }
        return null;
    }

    public OMedia getOMediaFromPlayLists(Movie movie) {
        try {
            Collection<Object> objects = allPlayLists.getAllObject();
            for (Object o : objects) {
                OMedia oMedia = ((VideoList) o).findByMovie(movie);
                if (oMedia != null) return oMedia;
            }
        } catch (Exception e) {
            MMLog.e(TAG, String.valueOf(e));
        }
        return null;
    }

    public OMedia getFirstOMediaFromPlayLists() {
        try {
            Collection<Object> objects = allPlayLists.getAllObject();
            for (Object o : objects) {
                OMedia oMedia = ((VideoList) o).getFirstItem();
                if (oMedia != null) return oMedia;
            }
        } catch (Exception e) {
            MMLog.e(TAG, String.valueOf(e));
        }
        return null;
    }

    public OMedia getRandomOMediaFromPlayLists() {
        try {
            Collection<Object> objects = allPlayLists.getAllObject();
            for (Object o : objects) {
                OMedia oMedia = ((VideoList) o).findAny();
                if (oMedia != null) return oMedia;
            }
        } catch (Exception e) {
            MMLog.e(TAG, String.valueOf(e));
        }
        return null;
    }

    /////////////////////////////////////////////////////////////////////////////////////////////////
    /////////////////////////////////////////////////////////////////////////////////////////////////
    /////////////////////////////////////////////////////////////////////////////////////////////////
    public synchronized void autoPlay() {
        if (isPlaying()) return;
        autoPlay(autoPlaySource);
    }

    public synchronized void autoPlay(int autoPlayType) {
        OMedia ooMedia = null;
        this.autoPlaySource = autoPlayType;
        switch (autoPlaySource) {
            case DataID.SESSION_SOURCE_PLAYLIST:
            case DataID.SESSION_SOURCE_ALL:
            case DataID.SESSION_SOURCE_FAVORITELIST:
            default:
                ooMedia = getFirstOMediaFromPlayLists();
                break;
        }
        if (oMediaPlaying != null) {
            oMediaPlaying.setRestorePlay(true);
            startPlay(oMediaPlaying);
        } else if (ooMedia != null) {
            ooMedia.setRestorePlay(true);
            startPlay(ooMedia);
        }
    }

    @Override
    public void onEventPlayerStatus(PlayerStatusInfo playerStatusInfo) {///oMediaPlaying callback 当前播放的callback
        if (this.mUserCallback != null && oMediaPlaying != null) {
            //this.callback.OnEventCallBack(EventType, TimeChanged, LengthChanged, PositionChanged, OutCount, ChangedType, ChangedID, Buffering, Length);
            Message msg = playHandler.obtainMessage();
            playerStatusInfo.setObj(oMediaPlaying);
            msg.obj = playerStatusInfo;
            msg.what = playerStatusInfo.getEventType();
            msg.arg1 = (int) playerStatusInfo.getPositionChanged();
            msg.arg2 = (int) playerStatusInfo.getLengthChanged();
            playHandler.sendMessage(msg);//UI 层
        }
        if (isClientProxy) //服务端不负责调度管理  ///本地CALLBACK
        {
            LocalScheduling(playerStatusInfo);
        }
    }

    private void LocalScheduling(PlayerStatusInfo playerStatusInfo) {
        switch (playerStatusInfo.getEventCode()) {//media.class 原生事件
            case PlaybackEvent.Buffering:
            case PlaybackEvent.EndReached:
                break;
            case PlaybackEvent.Playing:
                if (videoOutHeight > 10 && videoOutWidth > 10) {
                    MMLog.log(TAG, "LocalScheduling set video size to " + videoOutWidth + ":" + videoOutHeight);
                    setWindowSize(videoOutWidth, videoOutHeight);
                }
                break;
        }
        switch (playerStatusInfo.getEventType()) {
            case PlaybackEvent.Status_NothingIdle:
                ///if (autoPlaySource >= DataID.SESSION_SOURCE_ALL) //立即跳转到收藏列表
                ///{
                ///MMLog.log(TAG, "LocalScheduling.EventType = " + playerStatusInfo.getEventType() + ", " + oMediaPlaying.getPathName());
                ///playEventHandler(playOrder);//继续播放，跳到上一首或下一首
                ///}
                break;
            case PlaybackEvent.Status_Opening:
                if (oMediaPlaying != null) {
                    playerStatusInfo.setObj(oMediaPlaying);
                    Message msg = playHandler.obtainMessage();
                    msg.what = PlaybackEvent.Status_Changed;
                    msg.obj = playerStatusInfo;
                    if (oMediaPlaying.getPre() != null && oMediaPlaying.getNext() != null) {
                        if (oMediaPlaying.getPre().getNext() == oMediaPlaying) msg.what = PlaybackEvent.Status_Next;
                        else if (oMediaPlaying.getNext().getPre() == oMediaPlaying) msg.what = PlaybackEvent.Status_Prev;
                    }
                    playHandler.sendMessage(msg);
                }
                break;
            case PlaybackEvent.Status_Buffering:
            case PlaybackEvent.Status_Playing:
            case PlaybackEvent.Status_Paused:
            case PlaybackEvent.Status_Stopped:
            case PlaybackEvent.MediaChanged:
                break;
            case PlaybackEvent.Status_Error:
                MMLog.log(TAG, "LocalScheduling.EventType.Status_Error stop play " + oMediaPlaying.getPathName());
                break;
            case PlaybackEvent.Status_Ended:
                if (playerStatusInfo.getLastError() == PlaybackEvent.Status_Error) {//处理错误
                    if (oMediaPlaying == null) break;
                    playerStatusInfo.setLastError(PlaybackEvent.Status_Ended);
                    if (tryCountForError > 0 && tryPlayCount > 0) {
                        MMLog.d(TAG, "LocalScheduling.EventType.Status_Error try again " + oMediaPlaying.getPathName());
                        playEventHandler(DataID.PLAY_MANAGER_PLAY_ORDER4);
                        tryPlayCount--;
                    } else {
                        playEventHandler(playOrder);//继续播放，跳到上一首或下一首
                    }
                } else {
                    tryPlayCount = tryCountForError;
                    MMLog.i(TAG, "LocalScheduling.EventType.Status_Ended " + oMediaPlaying.getPathName());
                    playEventHandler(playOrder);//继续播放，跳到上一首或下一首
                }
                break;
        }
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
                if (!isPlaying()) playNext();
                else MMLog.log(TAG, "playEventHandler isPlaying=" + isPlaying());
                break;
            case DataID.PLAY_MANAGER_PLAY_ORDER3://反向列表循环
                if (!isPlaying()) playPre();
                break;
            case DataID.PLAY_MANAGER_PLAY_ORDER4://单曲循环或者再次播放当前歌曲
                if (!isPlaying()) startPlay(oMediaPlaying);
                break;
            case DataID.PLAY_MANAGER_PLAY_ORDER5://随机播放
                if (!isPlaying()) startPlay(getRandomOMediaFromPlayLists());
                break;
            case DataID.PLAY_MANAGER_PLAY_ORDER6:
            default:
                break;
        }
    }

    private final Handler playHandler = new Handler(Looper.getMainLooper()) {
        @SuppressLint(value = "HandlerLeak")
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            PlayerStatusInfo playerStatusInfo = (PlayerStatusInfo) msg.obj;
            if (mUserCallback != null && playerStatusInfo != null) mUserCallback.onEventPlayerStatus(playerStatusInfo);
        }
    };

    @Override ///来自本地媒体库的callback
    public void OnSessionComplete(int session_id, String result, int count) {
        MMLog.d(TAG, "OnSessionComplete id=" + session_id + ":" + result + ":" + count);
        if (tMediaManager != null) {
            switch (session_id) {
                case MessageEvent.MESSAGE_EVENT_LOCAL_VIDEO:
                    ///mLocalMediaVideos.clear();
                    mLocalMediaVideos = (tMediaManager.getLocalVideoSession().getVideoList());
                    this.allPlayLists.addObject(PLAY_LIST_NAME_LOCAL_VIDEO, mLocalMediaVideos);
                    //mLocalMediaVideos.printAll();
                    break;
                case MessageEvent.MESSAGE_EVENT_LOCAL_AUDIO:
                    ///mLocalMediaAudios.clear();
                    mLocalMediaAudios = (tMediaManager.getLocalAudioSession().getVideoList());
                    this.allPlayLists.addObject(PLAY_LIST_NAME_LOCAL_AUDIO, mLocalMediaAudios);
                    //mLocalMediaAudios.printAll();
                    break;
                case MessageEvent.MESSAGE_EVENT_USB_VIDEO:
                    mLocalUSBMediaVideos.clear();
                    mLocalUSBMediaVideos.add(tMediaManager.getUSBVideoSession().getVideoList());
                    this.allPlayLists.addObject(PLAY_LIST_NAME_LOCAL_USB_VIDEO, mLocalUSBMediaVideos);
                    //mLocalUSBMediaVideos.printAll();
                    break;
                case MessageEvent.MESSAGE_EVENT_USB_AUDIO:
                    mLocalUSBMediaAudios.clear();
                    mLocalUSBMediaAudios.add(tMediaManager.getUSBAudioSession().getVideoList());
                    this.allPlayLists.addObject(PLAY_LIST_NAME_LOCAL_USB_AUDIO, mLocalUSBMediaAudios);
                    //mLocalUSBMediaAudios.printAll();
                    break;
                case MessageEvent.MESSAGE_EVENT_SD_VIDEO:
                    mLocalSDMediaVideos.clear();
                    mLocalSDMediaVideos.add(tMediaManager.getSDVideoSession().getVideoList());
                    this.allPlayLists.addObject(PLAY_LIST_NAME_LOCAL_SD_VIDEO, mLocalSDMediaVideos);
                    //mLocalSDMediaVideos.printAll();
                    break;
                case MessageEvent.MESSAGE_EVENT_SD_AUDIO:
                    mLocalSDMediaAudios.clear();
                    mLocalSDMediaAudios.add(tMediaManager.getSDAudioSession().getVideoList());
                    this.allPlayLists.addObject(PLAY_LIST_NAME_LOCAL_SD_AUDIO, mLocalSDMediaAudios);
                    //mLocalSDMediaAudios.printAll();
                    break;
            }
            mAlbumList = tMediaManager.getAlbumList();
            mAlbumListID = tMediaManager.getAlbumListID();
            mArtistList = tMediaManager.getArtistList();
            playHandler.sendEmptyMessage(session_id);
            Cabinet.getEventBus().post(new EventCourier(session_id));//通知本地UI外部数据加载完毕
        }
    }

    ///@TCourierSubscribe(threadMode = MethodThreadMode.threadMode.BACKGROUND)
    public void onTCourierSubscribeEventAidl(PEventCourier pEventCourier) {///内部更新
        ///MMLog.d(TAG, pEventCourier.toStr());
        switch (pEventCourier.getId()) {
            case MessageEvent.MESSAGE_EVENT_LOCAL_VIDEO:
                this.mLocalMediaVideos = getAidlOMedias(pEventCourier.getId());
                this.allPlayLists.addObject(PLAY_LIST_NAME_LOCAL_VIDEO, mLocalMediaVideos);
                updateArtistAndAlbum(mLocalMediaVideos);
                MMLog.d(TAG, "Update Aidl LocalMediaVideos.count=" + mLocalMediaVideos.getCount());
                break;
            case MessageEvent.MESSAGE_EVENT_USB_VIDEO:
                this.mLocalUSBMediaVideos = getAidlOMedias(pEventCourier.getId());
                this.allPlayLists.addObject(PLAY_LIST_NAME_LOCAL_USB_VIDEO, mLocalUSBMediaVideos);
                updateArtistAndAlbum(mLocalUSBMediaVideos);
                MMLog.d(TAG, "Update Aidl LocalUSBMediaVideos.count=" + mLocalUSBMediaVideos.getCount());
                break;
            case MessageEvent.MESSAGE_EVENT_SD_VIDEO:
                this.mLocalSDMediaVideos = getAidlOMedias(pEventCourier.getId());
                this.allPlayLists.addObject(PLAY_LIST_NAME_LOCAL_SD_VIDEO, mLocalSDMediaVideos);
                updateArtistAndAlbum(mLocalSDMediaVideos);
                MMLog.d(TAG, "Update Aidl LocalSDMediaVideos.count=" + mLocalSDMediaVideos.getCount());
                break;
            case MessageEvent.MESSAGE_EVENT_LOCAL_AUDIO:
                this.mLocalMediaAudios = getAidlOMedias(pEventCourier.getId());
                this.allPlayLists.addObject(PLAY_LIST_NAME_LOCAL_AUDIO, mLocalMediaAudios);
                updateArtistAndAlbum(mLocalMediaAudios);
                MMLog.d(TAG, "Update Aidl LocalMediaAudios.count=" + mLocalMediaAudios.getCount());
                break;
            case MessageEvent.MESSAGE_EVENT_USB_AUDIO:
                this.mLocalUSBMediaAudios = getAidlOMedias(pEventCourier.getId());
                this.allPlayLists.addObject(PLAY_LIST_NAME_LOCAL_USB_AUDIO, mLocalUSBMediaAudios);
                updateArtistAndAlbum(mLocalUSBMediaAudios);
                MMLog.d(TAG, "Update Aidl LocalUSBMediaAudios.count=" + mLocalUSBMediaAudios.getCount());
                break;
            case MessageEvent.MESSAGE_EVENT_SD_AUDIO:
                this.mLocalSDMediaAudios = getAidlOMedias(pEventCourier.getId());
                this.allPlayLists.addObject(PLAY_LIST_NAME_LOCAL_SD_AUDIO, mLocalSDMediaAudios);
                updateArtistAndAlbum(mLocalSDMediaAudios);
                MMLog.d(TAG, "Update Aidl LocalSDMediaAudios.count=" + mLocalSDMediaAudios.getCount());
                break;
            case MessageEvent.MESSAGE_EVENT_MEDIA_LIBRARY:
            case MessageEvent.MESSAGE_EVENT_OCTOPUS_AIDL_START_REGISTER:
                this.mLocalMediaVideos = getAidlOMedias(MessageEvent.MESSAGE_EVENT_LOCAL_VIDEO);
                this.mLocalUSBMediaVideos = getAidlOMedias(MessageEvent.MESSAGE_EVENT_USB_VIDEO);
                this.mLocalSDMediaVideos = getAidlOMedias(MessageEvent.MESSAGE_EVENT_SD_VIDEO);

                this.mLocalMediaAudios = getAidlOMedias(MessageEvent.MESSAGE_EVENT_LOCAL_AUDIO);
                this.mLocalUSBMediaAudios = getAidlOMedias(MessageEvent.MESSAGE_EVENT_USB_AUDIO);
                this.mLocalSDMediaAudios = getAidlOMedias(MessageEvent.MESSAGE_EVENT_SD_AUDIO);

                MMLog.d(TAG, "Update Aidl LocalVideos.count=" + mLocalMediaVideos.getCount());
                MMLog.d(TAG, "Update Aidl LocalAudios.count=" + mLocalMediaAudios.getCount());
                MMLog.d(TAG, "Update Aidl LocalUSBVideos.count=" + mLocalUSBMediaVideos.getCount());
                MMLog.d(TAG, "Update Aidl LocalUSBAudios.count=" + mLocalUSBMediaAudios.getCount());
                MMLog.d(TAG, "Update Aidl LocalSDVideos.count=" + mLocalSDMediaVideos.getCount());
                MMLog.d(TAG, "Update Aidl LocalSDAudios.count=" + mLocalSDMediaAudios.getCount());

                updateArtistAndAlbum(mLocalMediaVideos);
                updateArtistAndAlbum(mLocalUSBMediaVideos);
                updateArtistAndAlbum(mLocalSDMediaVideos);
                updateArtistAndAlbum(mLocalMediaAudios);
                updateArtistAndAlbum(mLocalUSBMediaAudios);
                updateArtistAndAlbum(mLocalSDMediaAudios);
                break;
        }
        MMLog.d(TAG, "Update Aidl mAlbumList.count=" + mAlbumList.getCount());
        MMLog.d(TAG, "Update Aidl mArtistList.count=" + mArtistList.getCount());
        Cabinet.getEventBus().post(new EventCourier(pEventCourier.getId()));//通知本地UI外部数据加载完毕
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////////////
    ////////////////////////////////////////////////////////////////////////////////////////////////////////
    private IMyMediaAidlInterface tIMyMediaAidlInterface = null;
    private boolean tIMyMediaAidlInterfaceIsBound = false;

    private boolean hasPlayAidlProxy() {
        return tIMyMediaAidlInterfaceIsBound && (tIMyMediaAidlInterface != null);
    }

    public boolean isAidlProxyPaused() {
        if (hasPlayAidlProxy()) {
            int status = 0;
            try {
                status = getMyMediaAidlInterface().getPlayerStatus();
            } catch (RemoteException e) {
                ///throw new RuntimeException(e);
            }
            return status == PlaybackEvent.Status_Paused;
        }
        return false;
    }

    public boolean isAidlProxyPlaying() {
        if (hasPlayAidlProxy()) {
            try {
                return getMyMediaAidlInterface().isPlaying();
            } catch (RemoteException e) {
                ///throw new RuntimeException(e);
            }
        }
        return false;
    }

    private void stopProxyPlayer() {
        if (hasPlayAidlProxy()) {
            try {
                getMyMediaAidlInterface().playStop();
            } catch (RemoteException e) {
                ///throw new RuntimeException(e);
            }
        }
    }

    //aidl
    public void initialMyMediaAidlInterface(Context context) {
        ///Intent intent = new Intent(mContext, MMCarService.class);
        //MMLog.d(TAG,"initial MMCarService proxy");
        Intent intent = new Intent(MESSAGE_EVENT_OCTOPUS_ACTION_MULTIMEDIA_SERVICE);
        //intent.setPackage(mContext.getPackageName());
        intent.setComponent(new ComponentName(MESSAGE_EVENT_AIDL_PACKAGE_NAME, MESSAGE_EVENT_AIDL_MUSIC_CLASS_NAME));
        //Intent newIntent = new Intent(createExplicitFromImplicitIntent(mContext,intent));
        tIMyMediaAidlInterfaceIsBound = context.bindService(intent, mMediaServiceConnection, BIND_AUTO_CREATE);
        if (!tIMyMediaAidlInterfaceIsBound) MMLog.d(TAG, "Connect to multimedia service proxy failed!");
    }

    private final ServiceConnection mMediaServiceConnection = new ServiceConnection() {

        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            MMLog.d(TAG, "Connect to multimedia proxy successfully!");
            tIMyMediaAidlInterface = IMyMediaAidlInterface.Stub.asInterface(service);
            try {
                tIMyMediaAidlInterface.registerListener(mIMyMediaAidlInterfaceListener);
            } catch (RemoteException e) {
                //throw new RuntimeException(e);
            }
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            tIMyMediaAidlInterface = null;
            tIMyMediaAidlInterfaceIsBound = false;
            MMLog.d(TAG, "Multimedia service disconnected!");
        }
    };

    private final IMyAidlInterfaceListener mIMyMediaAidlInterfaceListener = new IMyAidlInterfaceListener.Stub() {

        @Override
        public void onMessageAidlInterface(PEventCourier msg) {
            ///MMLog.d(TAG, TAG + "." + msg.toStr());
            switch (msg.getId()) { ///加载外部数据
                case MessageEvent.MESSAGE_EVENT_LOCAL_VIDEO:
                case MessageEvent.MESSAGE_EVENT_USB_VIDEO:
                case MessageEvent.MESSAGE_EVENT_SD_VIDEO:
                case MessageEvent.MESSAGE_EVENT_LOCAL_AUDIO:
                case MessageEvent.MESSAGE_EVENT_USB_AUDIO:
                case MessageEvent.MESSAGE_EVENT_SD_AUDIO:
                case MessageEvent.MESSAGE_EVENT_MEDIA_LIBRARY:
                case MessageEvent.MESSAGE_EVENT_OCTOPUS_AIDL_START_REGISTER:
                    onTCourierSubscribeEventAidl(msg);
                    break;
            }
        }

        @Override
        public void onMessageMusic(int MsgId, int status, long timeChanged, long length, PMovie pMovie) {

            PlayerStatusInfo playerStatusInfo = new PlayerStatusInfo();
            playerStatusInfo.setEventType(MsgId);
            playerStatusInfo.setTimeChanged(timeChanged);
            playerStatusInfo.setLength(length);
            playerStatusInfo.setEventCode(MESSAGE_EVENT_OCTOPUS_AIDL_PLAYING_STATUS);

            if (oMediaPlaying != null) {///曲库搜索
                if (!Objects.equals(oMediaPlaying.getPathName(), pMovie.getSrcUrl())) oMediaPlaying = searchMediaFromMediaLibrary(pMovie);
            } else {
                oMediaPlaying = searchMediaFromMediaLibrary(pMovie);
            }
            if (oMediaPlaying == null)///播放列表搜索
                oMediaPlaying = getOMediaFromPlayLists(pMovie.getSrcUrl());

            if (oMediaPlaying != null) {///匹配代理端曲库信息
                if (oMediaPlaying.getMovie().getDuration() == 0) oMediaPlaying.getMovie().setDuration(length);
                oMediaPlaying.setPlayStatus(status);
                onEventPlayerStatus(playerStatusInfo);//呼叫本地状态机，解析解析状态
            } else {
                MMLog.d(TAG, "onMessageMusic lost information " + pMovie.getSrcUrl());
            }
            ///if (oMediaPlaying != null) MMLog.d(TAG, playerStatusInfo.toString() + "," + filePathName);
            ///if (mUserCallback != null) mUserCallback.onEventPlayerStatus(playerStatusInfo);
        }
    };

    private List<Movie> transformToMovie(List<PMovie> list) {
        List<Movie> movies = new ArrayList<>();
        for (PMovie pMovie : list) {
            movies.add((Movie) (pMovie));
        }
        return movies;
    }

    public VideoList transformToVideoList(List<String> list) {
        VideoList videoList = new VideoList();
        for (String str : list) {
            OMedia oMedia = searchMediaFromMediaLibrary(str);
            if (oMedia != null) videoList.add(oMedia);
            else videoList.add(str);
        }
        return videoList;
    }

    private List<PMovie> getMovies(int typeID) {
        List<PMovie> pMovies = new ArrayList<>();
        if (tIMyMediaAidlInterface != null) {
            try {
                pMovies = tIMyMediaAidlInterface.getMediaList(typeID);
            } catch (RemoteException e) {
                MMLog.d(TAG, String.valueOf(e));
            }
        }
        return pMovies;
    }

    private VideoList getAidlOMedias(int typeID) {
        VideoList videoList = new VideoList();
        List<PMovie> pMovies = new ArrayList<>();
        if (tIMyMediaAidlInterface != null) {
            try {
                pMovies = tIMyMediaAidlInterface.getMediaList(typeID);
                List<Movie> movies = transformToMovie(pMovies);
                for (Movie movie : movies) {
                    OMedia oMedia = new OMedia(movie);
                    videoList.add(oMedia);
                }
            } catch (RemoteException e) {
                MMLog.d(TAG, String.valueOf(e));
            }
        }
        return videoList;
    }

    public void disconnectedMyAidlService(Context context) {

        if (tIMyMediaAidlInterface != null) {///&& tIMyMediaAidlInterface.asBinder().isBinderAlive()
            try {
                tIMyMediaAidlInterface.playStop();
                context.unbindService(mMediaServiceConnection);
                tIMyMediaAidlInterface.unregisterListener(mIMyMediaAidlInterfaceListener);
                tIMyMediaAidlInterface = null;
                tIMyMediaAidlInterfaceIsBound = false;
                MMLog.d(TAG, "Disconnect aidl proxy connection!");
            } catch (RemoteException e) {
                //throw new RuntimeException(e);
            }
        }
    }

    private IMyMediaAidlInterface getMyMediaAidlInterface() {
        return tIMyMediaAidlInterface;
    }

    private void mediaAidlProxyInterfaceAction(int msg_id, OMedia oMedia) {
        if (hasPlayAidlProxy()) {
            try {
                switch (msg_id) {
                    case MESSAGE_EVENT_OCTOPUS_PLAY_PAUSE:
                        getMyMediaAidlInterface().playPause();
                        break;
                    case MESSAGE_EVENT_OCTOPUS_PLAY:
                        getMyMediaAidlInterface().startPlay(oMedia.getPathName());
                        break;
                    case MESSAGE_EVENT_OCTOPUS_PAUSE:
                        getMyMediaAidlInterface().pausePlay();
                        break;
                    case MESSAGE_EVENT_OCTOPUS_NEXT:
                        getMyMediaAidlInterface().playNext();
                        break;
                    case MESSAGE_EVENT_OCTOPUS_PREV:
                        getMyMediaAidlInterface().playPrev();
                        break;
                    case MESSAGE_EVENT_OCTOPUS_STOP:
                        getMyMediaAidlInterface().playStop();
                        break;
                }
            } catch (RemoteException e) {
                ///throw new RuntimeException(e);
            }
        }
    }

    //////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    ///MediaManager
    public void initialMediaLibrary() {
        if (hasPlayAidlProxy()) {///从代理初始化
            onTCourierSubscribeEventAidl(new PEventCourier(MessageEvent.MESSAGE_EVENT_MEDIA_LIBRARY));
        } else {///从本地媒体库初始化
            if (tMediaManager == null) this.tMediaManager = new TMediaManager(mContext, this);
            tMediaManager.updateMedias();
        }
    }

    public void updateLocalMedias() {
        if (hasPlayAidlProxy()) {
            onTCourierSubscribeEventAidl(new PEventCourier(MessageEvent.MESSAGE_EVENT_MEDIA_LIBRARY));
        } else if (tMediaManager != null) {
            tMediaManager.updateMedias();
        }
    }

    private void updateArtistAndAlbum(VideoList videoList) {
        OMedia oMedia = null;
        //for (Map.Entry<String, Object> entry : mAllSessions.getAll().entrySet())
        {
            //for (HashMap.Entry<String, Object> m : ((LiveVideoSession) entry.getValue()).getAllVideos().getMap().entrySet())
            for (HashMap.Entry<String, Object> m : videoList.getMap().entrySet())
            {
                oMedia = (OMedia) m.getValue();
                if (oMedia.getMovie().getAlbum() != null) {
                    mAlbumList.putString(oMedia.getMovie().getAlbum(), oMedia.getMovie().getAlbum());
                    mAlbumListID.putInt(oMedia.getMovie().getAlbum(),oMedia.getMovie().getSource_id());
                }
                if (oMedia.getMovie().getArtist() != null)
                    mArtistList.putString((oMedia.getMovie().getArtist()), oMedia.getMovie().getArtist());
            }
        }
    }

    public void saveToFile() {
        if (mFavouriteList.getCount() > 100) mFavouriteList.deleteFrom(mFavouriteList.getFirstItem(), mFavouriteList.getCount() - 100);
        mFavouriteList.saveToFile(mContext, "Medias_Favourite");
        if (mPlayingHistoryList.getCount() > 100)
            mPlayingHistoryList.deleteFrom(mPlayingHistoryList.getFirstItem(), mPlayingHistoryList.getCount() - 100);
        mPlayingHistoryList.saveToFile(mContext, "Medias_History");
    }

    public void loadFromFile() {
        mFavouriteList.loadFromFile(mContext, "Medias_Favourite");
        mPlayingHistoryList.loadFromFile(mContext, "Medias_History");
    }

    public void free() {
        try {
            saveToFile();
            if (getPlayingMedia() != null) getPlayingMedia().free();
            this.allPlayLists.clear();
            Cabinet.getEventBus().unRegisterEventObserver(this);
            if (tMediaManager != null) tMediaManager.freeFree();
            disconnectedMyAidlService(mContext);
        } catch (Exception e) {
            MMLog.e(TAG, "free() " + e.toString());
        }
    }

}