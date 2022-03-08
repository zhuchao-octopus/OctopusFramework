package com.zhuchao.android.playsession;

import android.content.Context;
import android.content.res.AssetFileDescriptor;
import android.net.Uri;
import android.text.TextUtils;
import android.util.Log;
import android.view.SurfaceView;

import com.zhuchao.android.callbackevent.PlayerCallback;
import com.zhuchao.android.libfileutils.FilesManager;
import com.zhuchao.android.playerutil.PlayerUtil;
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
    private VideoList playingList = new VideoList();
    private OPlayerSessionManager sessionManager = null;
    private OMedia oMedia = null;
    private boolean isPlaying = false;
    private PlayerCallback callback = null;
    private String cachePath = getDownloadDir(null);
    private boolean threadLock = false;
    private int playOrder = 0;
    private int autoPlaySource = 1;

    public PlayManager(Context mContext, SurfaceView mSurfaceView) {
        this.context = mContext;
        this.surfaceView = mSurfaceView;
    }

    public void setSessionManager(OPlayerSessionManager sessionManager) {
        this.sessionManager = sessionManager;
        this.sessionManager.setUserSessionCallback(this);
    }

    public VideoList getPlayingList() {
        return playingList;
    }

    public void setPlayingList(VideoList playingList) {
        this.playingList = playingList;
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
        if(surfaceView == null) {
            Log.d(TAG, "no surfaceView ！！！！！！");
                return;
        }
        if (oMedia != null) {
            if (!oMedia.isAvailable(cachePath)) {
                Log.d(TAG, "not found " + oMedia.getMovie().getSourceUrl());
                return;
            }
            this.oMedia = oMedia;
        }
        if (this.oMedia == null) {
            Log.d(TAG, "invalid media ！！！！！！");
            return;
        }

        Log.d(TAG, "StartPlay ----> " + oMedia.getMovie().getSourceUrl() + " : " + oMedia.getLastPlayTime());
        this.oMedia.with(context);
        this.oMedia.setNormalRate();
        this.oMedia.callback(this);
        this.oMedia.onView(surfaceView);
        this.oMedia.playCache(cachePath);
        isPlaying = true;
    }

    public void startPlay(String url) {
        oMedia = playingList.findMovieByPath(url);
        if (oMedia == null) {
            oMedia = new OMedia(url);
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

    @Override
    public void OnEventCallBack(int EventType, long TimeChanged, long LengthChanged, float PositionChanged, int OutCount, int ChangedType, int ChangedID, float Buffering, long Length) {
        //mMediaPlayer.getPlayerState()
        //int NothingSpecial=0;
        //int Opening=1;
        //int Buffering=2;
        //int Playing=3;
        //int Paused=4;
        //int Stopped=5;
        //int Ended=6;
        //int Error=7;
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
                if (playOrder == 0) //列表循环
                {
                    if (oMedia.getNext() != null)
                        oMedia = oMedia.getNext();
                    oMedia.playCache(cachePath);
                } else if (playOrder == 1)//单次播放，只播放一次
                {
                    //oMedia.play();
                } else//单曲循环
                {
                    oMedia.playCache(cachePath);
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
                        playingList.loadFromDir(context, cachePath, Data.MEDIA_SOURCE_ID_VIDEO);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    threadLock = false;
                }

            }.start();
        }
    }

    public void updatePlayList() {
        playingList.clear();
        playingList.loadFromDir(context, cachePath, Data.MEDIA_SOURCE_ID_VIDEO);
    }

    public void addSource(String Url) {
        Movie movie = new Movie(Url);
        String filename = getFileName(movie.getSourceUrl());
        if (!TextUtils.isEmpty(filename))
            movie.setMovieName(filename);
        playingList.addVideo(new OMedia(movie));
    }

    public void addSource(FileDescriptor FD) {
        if (FD != null)
            playingList.addVideo(new OMedia(FD));
    }

    public void addSource(AssetFileDescriptor AFD) {
        if (AFD != null)
            playingList.addVideo(new OMedia(AFD));
    }

    public void addSource(Uri uri) {
        if (uri != null)
            playingList.addVideo(new OMedia(uri));
    }

    public OMedia getMedia(String url) {
        OMedia oo = playingList.findMovieByPath(oMedia.getMovie().getSourceUrl());
        return oo;
    }

    public OMedia getMedia(int Index) {
        if (playingList.getCount() <= 0)
            return null;
        return playingList.getVideos().get(Index);
    }

    public void free() {
        try {
            playingList.clear();
            playingList = null;
            PlayerUtil.FreeSinglePlayer();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void autoPlay(int autoPlaySource) {
        this.autoPlaySource = autoPlaySource;
        if (autoPlaySource == 1) {
            if (playingList.getCount() > 0)
                oMedia = playingList.getVideos().get(0);
            else {
                playingList.loadFromDir(context, cachePath, Data.MEDIA_SOURCE_ID_VIDEO);
                if (playingList.getCount() > 0)
                    oMedia = playingList.getVideos().get(0);
            }
        } else if (autoPlaySource == Data.SESSION_TYPE_MOBILEMEDIA9) {
            if (sessionManager != null)
                if (sessionManager.getMobileSession().getmVideoList().getCount() > 0)
                    oMedia = sessionManager.getMobileSession().getVideos().get(0);
        } else if (autoPlaySource == Data.SESSION_TYPE_LOCALMEDIA) {
            if (sessionManager != null)
                if (sessionManager.getLocalSession().getmVideoList().getCount() > 0)
                    oMedia = sessionManager.getLocalSession().getVideos().get(0);
        } else if (autoPlaySource == Data.MEDIA_SOURCE_ID_AllMEDIA) {
            if (sessionManager != null)
                if (sessionManager.getMobileTFSession().getmVideoList().getCount() > 0)
                    oMedia = sessionManager.getMobileTFSession().getVideos().get(0);
        } else {
            if (playingList.getCount() > 0)
                oMedia = playingList.getVideos().get(0);
            else if (sessionManager != null) {
                if (sessionManager.getMobileSession().getmVideoList().getCount() > 0)
                    oMedia = sessionManager.getMobileSession().getVideos().get(0);
                else if (sessionManager.getLocalSession().getmVideoList().getCount() > 0)
                    oMedia = sessionManager.getLocalSession().getVideos().get(0);
                else if (sessionManager.getMobileTFSession().getmVideoList().getCount() > 0)
                    oMedia = sessionManager.getMobileTFSession().getVideos().get(0);
                else oMedia = null;
            } else oMedia = null;
        }

        if (oMedia == null) {
            Log.d(TAG, "StartPlay but no media !!!!, autoPlaySource = " + autoPlaySource);
        } else {
            startPlay(oMedia);
        }
    }

    //public static final int SESSION_TYPE_LOCALMEDIA = 10; //本地媒体
    @Override
    public void OnSessionComplete(int sessionId, String result) {
        Log.d(TAG, "OnSessionComplete: " + "sessionId = " + sessionId + " result = " + result);
        Log.d(TAG, "OnSessionComplete: " + "autoPlaySource = " + autoPlaySource);
        if (sessionId == Data.SESSION_TYPE_MOBILEMEDIA9) {
            if (isPlaying() == false && autoPlaySource == Data.SESSION_TYPE_MOBILEMEDIA9) {
                startPlay(sessionId);
            }
            Log.d(TAG, "OnSessionComplete count = " + sessionManager.getMobileSession().getmVideoList().getCount());
        }
        if (sessionId == Data.SESSION_TYPE_LOCALMEDIA) {
            if (isPlaying() == false && autoPlaySource == Data.SESSION_TYPE_LOCALMEDIA) {
                startPlay(sessionId);
            }
            Log.d(TAG, "OnSessionComplete count = " + sessionManager.getLocalSession().getmVideoList().getCount());
        }
        if (sessionId == Data.MEDIA_SOURCE_ID_AllMEDIA) {
            if (isPlaying() == false && autoPlaySource == Data.MEDIA_SOURCE_ID_AllMEDIA) {
                startPlay(sessionId);
            }
            Log.d(TAG, "OnSessionComplete count = " + sessionManager.getMobileTFSession().getmVideoList().getCount());
        }
    }
}
