package com.zhuchao.android.playsession;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.res.AssetFileDescriptor;
import android.net.Uri;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.text.TextUtils;
import android.util.Log;
import android.view.SurfaceView;

import com.zhuchao.android.callbackevent.PlayerCallback;
import com.zhuchao.android.libfileutils.FilesManager;
import com.zhuchao.android.video.Movie;
import com.zhuchao.android.video.OMedia;
import com.zhuchao.android.video.VideoList;

import java.io.FileDescriptor;

import static com.zhuchao.android.libfileutils.FilesManager.getDownloadDir;
import static com.zhuchao.android.libfileutils.FilesManager.getFileName;

public class PlayManager implements PlayerCallback, SessionCompleteCallback {
    private final String TAG = "PlayManager";
    private Context context;
    private SurfaceView surfaceView;
    private OPlayerSessionManager sessionManager = null;
    private OMedia oMedia = null;
    private boolean isPlaying = false;
    private PlayerCallback callback = null;
    private String cachePath = getDownloadDir(null);
    private boolean threadLock = false;
    private int playOrder = Data.SESSION_SOURCE_ALL;
    private int autoPlay = Data.SESSION_SOURCE_ALL;
    public VideoList playingList = new VideoList();

    public PlayManager(Context mContext, SurfaceView mSurfaceView) {
        this.context = mContext;
        this.surfaceView = mSurfaceView;
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
        int sta = oMedia.getPlayState();
        if (sta >= 1 && sta <= 3)
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
        if (oMedia == null) {
            Log.d(TAG, "There is no media to play！！！");
            return;
        }
        if (surfaceView == null) {
            Log.d(TAG, "no surfaceView ！！！！！！");
            return;
        }
        if (oMedia != null) {
            if (!oMedia.isAvailable(cachePath)) {
                Log.d(TAG, "The Source is not available! go next---> " + oMedia.getMovie().getsUrl());
                playHandler.sendEmptyMessage(100);
                return;
            }
        }
        this.oMedia = oMedia;
        Log.d(TAG, "StartPlay --> " + oMedia.getMovie().getsUrl() + ", Last time = " + oMedia.getLastPlayTime());
        this.oMedia.with(context);
        this.oMedia.setNormalRate();
        this.oMedia.callback(this);
        this.oMedia.onView(surfaceView);
        this.oMedia.playCache(cachePath);
        isPlaying = true;
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
        if (oMedia != null)
            oMedia.setSurfaceView(surfaceView);
        this.surfaceView = surfaceView;
    }

    //mMediaPlayer.getPlayerState()//int NothingSpecial=0;//int Opening=1;//int Buffering=2;//int Playing=3;//int Paused=4;
    //int Stopped=5; //int Ended=6;//int Error=7;
    @Override
    public void OnEventCallBack(int EventType, long TimeChanged, long LengthChanged, float PositionChanged, int OutCount, int ChangedType, int ChangedID, float Buffering, long Length) {
        int ii = oMedia.getPlayState();
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
                        startPlay(oMedia.getNext());
                    }
                }
                else if (playOrder == 1)//反向列表循环
                {
                    if (oMedia.getPre() != null) {
                        startPlay(oMedia.getPre());
                    }
                }
                else if (playOrder == 2) //单曲循环
                {
                    startPlay(oMedia);
                }
                else if (playOrder == 3) //随机播放
                {
                    startPlay(playingList.findAny());
                }
                else
                {
                    //单次播放，只播放一次
                }
                break;
        }
        if (this.callback != null)
            this.callback.OnEventCallBack(EventType, TimeChanged, LengthChanged, PositionChanged, OutCount, ChangedType, ChangedID, Buffering, Length);
    }

    public void setCachePath(String CacheDir) {
        this.cachePath = CacheDir;
        if (playingList.getCount() > 0) return;
        if (FilesManager.isExists(cachePath)) {
            if (threadLock) return;
            new Thread() {     //不可在主线程中调用
                public void run() {
                    threadLock = true;
                    try {
                        updatePlayingList();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    threadLock = false;
                }
            }.start();
        }
    }

    public synchronized void updatePlayingList() {
        if (threadLock) return;
        playingList.clear();
        playingList.loadFromDir(cachePath,Data.MEDIA_TYPE_ID_AllMEDIA);
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

    public void free() {
        try {
            playingList.clear();
            playingList = null;
            if(getPlayingMedia()!=null)
                getPlayingMedia().stopFree();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void autoPlay(int autoPlayType) {
        OMedia ooMedia = null;
        this.autoPlay = autoPlayType;
        if (autoPlay < 0) return;
        if (autoPlay == Data.SESSION_SOURCE_ALL)
        {
            if (playingList.getCount() > 0)
                ooMedia = playingList.findByIndex(0);
            else if (sessionManager != null)
            {
                if (sessionManager.getMobileSession().getVideoList().getCount() > 0)
                    ooMedia = sessionManager.getMobileSession().getVideos().findByIndex(0);
                else if (sessionManager.getLocalSession().getVideoList().getCount() > 0)
                    ooMedia = sessionManager.getLocalSession().getVideos().findByIndex(0);
                else if (sessionManager.getFileSession().getVideoList().getCount() > 0)
                    ooMedia = sessionManager.getFileSession().getVideos().findByIndex(0);
            }
        } else if (autoPlay == Data.SESSION_SOURCE_MOBILE_USB) {
            if (sessionManager != null)
                if (sessionManager.getMobileSession().getVideoList().getCount() > 0)
                    ooMedia = sessionManager.getMobileSession().getVideos().findByIndex(0);
                else
                    sessionManager.initSessionFromMobileDisc();
        } else if (autoPlay == Data.SESSION_SOURCE_LOCAL_INTERNAL) {
            if (sessionManager != null)
                if (sessionManager.getLocalSession().getVideoList().getCount() > 0)
                    ooMedia = sessionManager.getLocalSession().getVideos().findByIndex(0);
                else
                    sessionManager.initSessionFromLocal();
        } else if (autoPlay == Data.SESSION_SOURCE_EXTERNAL) {
            if (sessionManager != null)
                if (sessionManager.getFileSession().getVideoList().getCount() > 0)
                    ooMedia = sessionManager.getFileSession().getVideos().findByIndex(0);
        } else {
            if (playingList.getCount() > 0)
                ooMedia = playingList.findByIndex(0);
        }
        ////////////////////////////////////////////////////////////////////////////////////////////
        //跳过无效的资源
        if (oMedia == null)
            startPlay(ooMedia);
        else if (ooMedia != null) {
            if (playOrder == 1)
                startPlay(ooMedia.getNext());
            else if (playOrder == 2)
                startPlay(ooMedia.getPre());
            else ;
        } else ;
    }

    //public static final int SESSION_TYPE_LOCALMEDIA = 10; //本地媒体
    @Override
    public void OnSessionComplete(int sessionId, String result) {
        Log.d(TAG, "OnSessionComplete: " + "sessionId = " + sessionId + ",result   = " + result);
        Log.d(TAG, "OnSessionComplete: " + "sessionId = " + sessionId + ",autoPlay = " + autoPlay + ",getAllVideoList().size() = " + sessionManager.getAllMedias().size());
        if(sessionId <=0)
        {
            if(isPlaying())  this.playPause();
        }
        if (sessionId == Data.SESSION_SOURCE_MOBILE_USB) {
            Log.d(TAG, "OnSessionComplete: " + "sessionId = " + sessionId + ",getMobileSession().getVideoList().getCount() = " + sessionManager.getMobileSession().getVideoList().getCount());
            if (isPlaying() == false && (autoPlay == Data.SESSION_SOURCE_MOBILE_USB || autoPlay == Data.SESSION_SOURCE_ALL)) {
                autoPlay(sessionId);
            }
        }
        else if (sessionId == Data.SESSION_SOURCE_LOCAL_INTERNAL) {
            Log.d(TAG, "OnSessionComplete: " + "sessionId = " + sessionId + ",getLocalSession().getVideoList().getCount() = " + sessionManager.getLocalSession().getVideoList().getCount());
            if (isPlaying() == false && (autoPlay == Data.SESSION_SOURCE_LOCAL_INTERNAL|| autoPlay == Data.SESSION_SOURCE_ALL)) {
                autoPlay(sessionId);
            }
        }
        else if (sessionId == Data.SESSION_SOURCE_PATH) {
            Log.d(TAG, "OnSessionComplete: " + "sessionId = " + sessionId + ",getMobileTFSession().getVideoList().getCount() = " + sessionManager.getFileSession().getVideoList().getCount());
            if (isPlaying() == false && (autoPlay == Data.SESSION_SOURCE_LOCAL_INTERNAL || autoPlay == Data.SESSION_SOURCE_ALL)) {
                autoPlay(Data.SESSION_SOURCE_ALL);
            }
        }

        if (isPlaying() == false && (autoPlay == Data.SESSION_SOURCE_LOCAL_INTERNAL || autoPlay == Data.SESSION_SOURCE_ALL)) {
            autoPlay(Data.SESSION_SOURCE_ALL);
        }
        //Log.d(TAG, "getMobileTFSession().getVideoList().getCount() = " + sessionManager.getMobileTFSession().getVideoList().getCount());
    }

    private Handler playHandler = new Handler(Looper.getMainLooper()) {
        @SuppressLint(value = "HandlerLeak")
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            //Log.i(TAG, "   handlerMessage msg.what= " + msg.what);
            if (msg.what == 100) {
                autoPlay(autoPlay);
            }
        }
    };
}
