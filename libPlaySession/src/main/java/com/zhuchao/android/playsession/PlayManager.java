package com.zhuchao.android.playsession;

import android.content.Context;
import android.text.TextUtils;
import android.util.Log;
import android.view.SurfaceView;

import com.zhuchao.android.callbackevent.PlayerCallback;
import com.zhuchao.android.libfilemanager.FilesManager;
import com.zhuchao.android.libfilemanager.MediaFile;
import com.zhuchao.android.video.Movie;
import com.zhuchao.android.video.OMedia;
import com.zhuchao.android.video.VideoList;

import java.util.List;

import static com.zhuchao.android.libfilemanager.FilesManager.getDownloadDir;
import static com.zhuchao.android.libfilemanager.FilesManager.getFileName;

public class PlayManager implements PlayerCallback, SessionCompleteCallback {
    private final String TAG = "PlayManager ";
    private Context mContext;
    private SurfaceView mSurfaceView;
    private VideoList mMyVideoList = new VideoList();
    private OPlayerSessionManager mSessionManager = null;
    private OMedia oMedia = null;
    private int mPlayType = 1;
    private boolean mIsPlaying = false;
    private PlayerCallback mCallback = null;
    private String mCachePath = getDownloadDir(null);
    private boolean mThreandLock = false;

    private synchronized OPlayerSessionManager getSessionManager() {//通过上下文得到实例，用实例去调用非静态方法
        if (mSessionManager == null)
            mSessionManager = new OPlayerSessionManager(mContext, null, this);
        return mSessionManager;
    }

    public PlayManager(Context mContext, SurfaceView mSurfaceView) {
        this.mContext = mContext;
        this.mSurfaceView = mSurfaceView;
        //getSessionManager();

        //getMediasFromPath(mContext, getDownloadDir(null), Data.MEDIA_SOURCE_ID_VIDEO);
    }

    public PlayManager(Context mContext, SurfaceView mSurfaceView, OPlayerSessionManager mSessionManager) {
        this.mContext = mContext;
        this.mSurfaceView = mSurfaceView;
        this.mSessionManager = mSessionManager;
        //getMediasFromPath(mContext, getDownloadDir(null), Data.MEDIA_SOURCE_ID_VIDEO);
    }

    public PlayManager setSessionManager(OPlayerSessionManager mSessionManager) {
        this.mSessionManager = mSessionManager;
        return this;
    }

    public VideoList getmMyVideoList() {
        return mMyVideoList;
    }

    public SurfaceView getmSurfaceView() {
        return mSurfaceView;
    }

    public PlayManager setmSurfaceView(SurfaceView mSurfaceView) {
        this.mSurfaceView = mSurfaceView;
        return this;
    }

    public OMedia getoMedia() {
        return oMedia;
    }

    public int getmType() {
        return mPlayType;
    }

    public String getmCachePath() {
        return mCachePath;
    }

    public void setmCachePath(String CacheDir) {
        if (TextUtils.isEmpty(CacheDir)) return;

        this.mCachePath = CacheDir;

        if (FilesManager.isExists(mCachePath)) {
            if (mThreandLock) return;
            new Thread() {     //不可在主线程中调用
                public void run() {
                    mThreandLock = true;
                    try {
                        getMediasFromPath(mContext, mCachePath, Data.MEDIA_SOURCE_ID_VIDEO);
                    } catch (Exception e) {
                        e.printStackTrace();

                    }
                    mThreandLock = false;
                }

            }.start();
        }

    }

    public void updateCache() {
        getMediasFromPath(mContext, mCachePath, Data.MEDIA_SOURCE_ID_VIDEO);
    }

    public PlayManager setmType(int mType) {
        this.mPlayType = mType;
        return this;
    }

    public boolean isPlaying() {
        if (oMedia == null) {
            mIsPlaying = false;
            return mIsPlaying;
        }

        //if (oMedia.isPlaying()) {
        //   mIsPlaying = true;
        //   return true;
        //}

        if (oMedia.getPlayState() > 0 && oMedia.getPlayState() < 5)
            mIsPlaying = true;
        else
            mIsPlaying = false;

        return mIsPlaying;
    }

    public PlayManager Callback(PlayerCallback mCallback) {
        this.mCallback = mCallback;
        return this;
    }

    public boolean hasUSBMedias() {
        return getSessionManager().getMobileSession().getVideos().size() > 0;
    }

    public void StartPlay(int type) {


        if (type == 1) {
            if (mMyVideoList.getMovieCount() > 0)
                oMedia = mMyVideoList.getVideos().get(0);
        } else if (type == Data.SESSION_TYPE_MOBILEMEDIA9) {
            if (getSessionManager().getMobileSession().getmVideoList().getMovieCount() > 0)
                oMedia = getSessionManager().getMobileSession().getVideos().get(0);
        } else if (type == Data.SESSION_TYPE_LOCALMEDIA) {
            if (getSessionManager().getLocalSession().getmVideoList().getMovieCount() > 0)
                oMedia = getSessionManager().getLocalSession().getVideos().get(0);
        } else if (type == Data.MEDIA_SOURCE_ID_AllMEDIA) {
            if (getSessionManager().getMomileTFSession().getmVideoList().getMovieCount() > 0)
                oMedia = getSessionManager().getMomileTFSession().getVideos().get(0);
        } else {
            if (mMyVideoList.getMovieCount() > 0)
                oMedia = mMyVideoList.getVideos().get(0);
            else if (getSessionManager().getMobileSession().getmVideoList().getMovieCount() > 0)
                oMedia = getSessionManager().getMobileSession().getVideos().get(0);
            else if (getSessionManager().getLocalSession().getmVideoList().getMovieCount() > 0)
                oMedia = getSessionManager().getLocalSession().getVideos().get(0);
            else if (getSessionManager().getMomileTFSession().getmVideoList().getMovieCount() > 0)
                oMedia = getSessionManager().getMomileTFSession().getVideos().get(0);
            else
                oMedia = null;
        }

        if (oMedia == null) {
            Log.d(TAG, "StartPlay but no media !!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!! type = " + type);

        } else {
            StartPlay(oMedia);
        }
    }

    public void StartPlay(String url, long postion) {

        oMedia = mMyVideoList.findMovieByPath(url);
        if (oMedia == null) {
            oMedia = new OMedia(url);
        }

        oMedia.setmLastPlayTime(postion);
        StartPlay(oMedia);
    }

    public void StartPlay(OMedia oMedia) {

        if (oMedia != null) {
            if (!oMedia.isAvailable(mCachePath)) {
                Log.d(TAG, "not exit " + oMedia.getMovie().getSourceUrl());
                return;
            }
            this.oMedia = oMedia;
        }

        if (this.oMedia == null) {
            Log.d(TAG, "invalid media !!!!!!!!!!!!!!!!!!!!!!");
            return;
        }
        //if(mSurfaceView != null) mSurfaceView.setVisibility(View.VISIBLE);

        Log.d(TAG, "StartPlay ----> " + oMedia.getMovie().getSourceUrl() + " : " + oMedia.getmLastPlayTime());

        this.oMedia.with(mContext);
        this.oMedia.setNormalRate();
        this.oMedia.callback(this);

        this.oMedia.playOn(mSurfaceView, mCachePath);
        mIsPlaying = true;
    }

    public void StopPlay() {
        if (oMedia != null)
            oMedia.stop();
    }

    public void PausePlay() {
        if (oMedia != null)
            oMedia.playPause();
    }

    public void PlayNext() {
        if (oMedia != null)
            StartPlay(oMedia.getNextOMedia());
    }

    public void PlayPre() {
        if (oMedia != null)
            StartPlay(oMedia.getPreOMedia());
    }

    public OMedia GetMyOMedia(String url) {
        OMedia oo = mMyVideoList.findMovieByPath(oMedia.getMovie().getSourceUrl());
        return oo;
    }

    @Override
    public void OnEventCallBack(int EventType, long TimeChanged, long LengthChanged, float PositionChanged, int OutCount, int ChangedType, int ChangedID, float Buffering, long Length) {
        // mMediaPlayer.getPlayerState()
        //int libvlc_NothingSpecial=0;
        //int libvlc_Opening=1;
        //int libvlc_Buffering=2;
        //int libvlc_Playing=3;
        //int libvlc_Paused=4;
        //int libvlc_Stopped=5;
        //int libvlc_Ended=6;
        //int libvlc_Error=7;
        int ii = oMedia.getPlayState();
        switch (ii) {
            case 0:
            case 1:
            case 2:
            case 3:
            case 4:
                break;
            case 5:
            case 6:
            case 7:

                if (oMedia.getmPlayOrder() == 0) //列表循环
                {
                    if (oMedia.getNextOMedia() != null)
                        oMedia = oMedia.getNextOMedia();

                    oMedia.playCache(mCachePath);
                } else if (oMedia.getmPlayOrder() == 1)//单次播放，只播放一次
                {
                    //oMedia.play();
                } else {
                    oMedia.playCache(mCachePath);
                }
                break;
        }

        if (this.mCallback != null)
            this.mCallback.OnEventCallBack(EventType, TimeChanged, LengthChanged, PositionChanged, OutCount, ChangedType, ChangedID, Buffering, Length);

    }

    //public static final int SESSION_TYPE_MOBILEMEDIA9 = 9;
    //public static final int SESSION_TYPE_LOCALMEDIA = 10; //本地媒体
    @Override
    public void OnSessionComplete(int sessionId, String result) {
        //Log.d(TAG, "OnSessionComplete " + "sessionId = " + sessionId + " result = " + result);
        if (sessionId == Data.SESSION_TYPE_MOBILEMEDIA9) {

            if (isPlaying() == false && mPlayType == Data.SESSION_TYPE_MOBILEMEDIA9) {
                Log.d(TAG, "OnSessionComplete play  sessionId =" + sessionId);
                StartPlay(sessionId);
            }
            // Log.d(TAG, "OnSessionComplete count = " + getSessionManager().getMobileSession().getmVideoList().getMovieCount());
        }
        if (sessionId == Data.SESSION_TYPE_LOCALMEDIA) {

            if (isPlaying() == false && mPlayType == Data.SESSION_TYPE_LOCALMEDIA) {
                Log.d(TAG, "OnSessionComplete play  sessionId =" + sessionId);
                StartPlay(sessionId);
            }
            // Log.d(TAG, "OnSessionComplete count = " + getSessionManager().getLocalSession().getmVideoList().getMovieCount());
        }

        if (sessionId == Data.MEDIA_SOURCE_ID_AllMEDIA) {

            if (isPlaying() == false && mPlayType == Data.MEDIA_SOURCE_ID_AllMEDIA) {
                Log.d(TAG, "OnSessionComplete play  sessionId =" + sessionId);
                StartPlay(sessionId);
            }
            //Log.d(TAG, "OnSessionComplete count = " + getSessionManager().getMomileTFSession().getmVideoList().getMovieCount());
        }

    }

    public void getMediasFromPath(Context context, String FilePath, Integer fType) {
        List<String> FileList = MediaFile.getMediaFiles(context, FilePath, fType);

        for (int i = 0; i < FileList.size(); i++) {
            Movie movie = new Movie(FileList.get(i));
            String finame = getFileName(movie.getSourceUrl());
            if (!TextUtils.isEmpty(finame))
                movie.setMovieName(finame);
            OMedia oMedia = new OMedia(movie);
            mMyVideoList.addVideo(oMedia);
        }
    }

    public void Free() {
        try {
            mSessionManager.free();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
