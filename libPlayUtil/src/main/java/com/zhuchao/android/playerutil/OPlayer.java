package com.zhuchao.android.playerutil;

import android.content.Context;
import android.content.res.AssetFileDescriptor;
import android.net.Uri;
import android.util.Log;
import android.view.SurfaceView;
import android.view.TextureView;

import com.zhuchao.android.callbackevent.PlayerCallback;

import org.videolan.libvlc.IVLCVout;
import org.videolan.libvlc.LibVLC;
import org.videolan.libvlc.Media;
import org.videolan.libvlc.MediaPlayer;

import java.io.FileDescriptor;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

//import org.videolan.libvlc.MediaPlayCallback;

public class OPlayer {
    private String TAG = "OPlayer>>>";
    private Context mContext;
    private MediaPlayer mMediaPlayer = null;
    private Media media = null;
    private LibVLC mLibVLC = null;
    private IVLCVout vlcVout;
    private IVLCVoutCallBack mIVLCVoutCallBack;
    private PlayerCallback mOnPlayerEventCallBack = null;
    private TextureView mTextureView = null;
    private SurfaceView mSurfaceView = null;
    private Boolean HWDecoderEnabled = true;
    private String mURL = "";

    // mMediaPlayer.getPlayerState()
    //int libvlc_NothingSpecial=0;
    //int libvlc_Opening=1;
    //int libvlc_Buffering=2;
    //int libvlc_Playing=3;
    //int libvlc_Paused=4;
    //int libvlc_Stopped=5;
    //int libvlc_Ended=6;
    //int libvlc_Error=7;

    private MediaPlayer.EventListener mEventListener = new MediaPlayer.EventListener() {
        @Override
        public void onEvent(MediaPlayer.Event event) {
            if (mOnPlayerEventCallBack != null)
                mOnPlayerEventCallBack.OnEventCallBack(
                        event.type,
                        event.getTimeChanged(),
                        event.getLengthChanged(),
                        event.getPositionChanged(),
                        event.getVoutCount(),
                        event.getEsChangedType(),
                        event.getEsChangedID(),
                        event.getBuffering(),
                        getLength());
        }
    };

    public OPlayer(Context context, PlayerCallback callback) {
        mContext = context;
        mIVLCVoutCallBack = new IVLCVoutCallBack();
        mOnPlayerEventCallBack = callback;

        mLibVLC = PlayerUtil.getSingleLibVLC(mContext, null);//new LibVLC(mContext);
        mMediaPlayer = new MediaPlayer(mLibVLC);
        mMediaPlayer.setScale(0);

        mMediaPlayer.setEventListener(mEventListener);
        vlcVout = mMediaPlayer.getVLCVout();
        vlcVout.addCallback(mIVLCVoutCallBack);
    }

    public OPlayer(Context context, ArrayList<String> options, PlayerCallback callback) {
        mContext = context;
        ArrayList<String> Options = new ArrayList<String>();
        mIVLCVoutCallBack = new IVLCVoutCallBack();
        mOnPlayerEventCallBack = callback;

        Options.clear();
        Options.add("--file-caching=1500");//文件缓存
        Options.add("--network-caching=10000");//网络缓存
        Options.add("--live-caching=10000");//直播缓存
        Options.add("--sout-mux-caching=1500");//输出缓存

        if (options == null)
            options = Options;

        try {
            mLibVLC = PlayerUtil.getSingleLibVLC(mContext, options);//new LibVLC(mContext, options);
        } catch (IllegalStateException e) {
            e.printStackTrace();
        }

        mMediaPlayer = new MediaPlayer(mLibVLC);

        mMediaPlayer.setEventListener(mEventListener);
        vlcVout = mMediaPlayer.getVLCVout();
        vlcVout.addCallback(mIVLCVoutCallBack);
    }

    public boolean setSource(String path) {
        if (media != null) {
            media.release();
            media = null;
        }
        mURL = path;
        if (mURL.isEmpty()) return false;
        if (mURL.startsWith("http") || mURL.startsWith("ftp")) {
            Uri uri = Uri.parse(mURL);
            setSource(uri);
            return true;
        }
        media = new Media(mLibVLC, mURL);
        //media.addOption(":no-audio");
        media.addOption(":fullscreen");
        media.addOption(":no-autoscale");
        media.addOption(":file-caching=10000");//文件缓存
        media.addOption(":network-caching=10000");//网络缓存
        media.addOption(":live-caching=10000");//直播缓存
        media.addOption(":sout-mux-caching=10000");//输出缓存
        if (mMediaPlayer == null)
            mMediaPlayer = new MediaPlayer(mLibVLC);
        mMediaPlayer.stop();
        mMediaPlayer.setMedia(media);
        mMediaPlayer.setAspectRatio(null);
        mMediaPlayer.setScale(0);
        return true;
    }

    public boolean setSource(Uri uri) {
        if (media != null) {
            media.release();
            media = null;
        }
        if (uri == null) return false;
        mURL = uri.toString();
        media = new Media(mLibVLC, uri);

        //media.addOption(":no-audio");
        media.addOption(":fullscreen");
        media.addOption(":no-autoscale");
        media.addOption(":file-caching=10000");//文件缓存
        media.addOption(":network-caching=10000");//网络缓存
        media.addOption(":live-caching=10000");//直播缓存
        media.addOption(":sout-mux-caching=10000");//输出缓存
        if (mMediaPlayer == null)
            mMediaPlayer = new MediaPlayer(mLibVLC);
        mMediaPlayer.stop();
        mMediaPlayer.setMedia(media);
        mMediaPlayer.setAspectRatio(null);
        mMediaPlayer.setScale(0);
        return true;
    }

    public boolean setSource(AssetFileDescriptor afd) {
        if (media != null) {
            media.release();
            media = null;
        }
        if (afd == null) return false;
        mURL = afd.toString();
        media = new Media(mLibVLC, afd);

        //media.addOption(":no-audio");
        media.addOption(":fullscreen");
        media.addOption(":no-autoscale");
        media.addOption(":file-caching=10000");//文件缓存
        media.addOption(":network-caching=10000");//网络缓存
        media.addOption(":live-caching=10000");//直播缓存
        media.addOption(":sout-mux-caching=10000");//输出缓存
        if (mMediaPlayer == null)
            mMediaPlayer = new MediaPlayer(mLibVLC);
        mMediaPlayer.stop();
        mMediaPlayer.setMedia(media);
        mMediaPlayer.setAspectRatio(null);
        mMediaPlayer.setScale(0);
        return true;
    }

    public boolean setSource(FileDescriptor fd) {
        if (media != null) {
            media.release();
            media = null;
        }
        if (fd == null) return false;
        mURL = fd.toString();
        media = new Media(mLibVLC, fd);

        //media.addOption(":no-audio");
        media.addOption(":fullscreen");
        media.addOption(":no-autoscale");
        media.addOption(":file-caching=10000");//文件缓存
        media.addOption(":network-caching=10000");//网络缓存
        media.addOption(":live-caching=10000");//直播缓存
        media.addOption(":sout-mux-caching=10000");//输出缓存
        if (mMediaPlayer == null)
            mMediaPlayer = new MediaPlayer(mLibVLC);
        mMediaPlayer.stop();
        mMediaPlayer.setMedia(media);
        mMediaPlayer.setAspectRatio(null);
        mMediaPlayer.setScale(0);
        return true;
    }

    public OPlayer setHWDecoderEnabled(Boolean HWDecoderEnabled) {
        this.HWDecoderEnabled = HWDecoderEnabled;
        if (media != null) {
            try {
                media.setHWDecoderEnabled(HWDecoderEnabled, HWDecoderEnabled);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return this;
    }

    public void setSize(int width, int height) {
        mMediaPlayer.setAspectRatio("16:9");
        mMediaPlayer.getVLCVout().setWindowSize(width, height);
    }

    public void setSurfaceView(SurfaceView ViewForShow) {
        //初始化播放mSurfaceView
        if (ViewForShow == null) return;
        if (mSurfaceView != null)
            if (mSurfaceView.equals(ViewForShow)) return;

        mSurfaceView = ViewForShow;

        if (vlcVout.areViewsAttached())
            vlcVout.detachViews();
        vlcVout.addCallback(mIVLCVoutCallBack);
        vlcVout.setVideoView(mSurfaceView);
        vlcVout.attachViews();
        mSurfaceView.getHolder().setKeepScreenOn(true);
    }

    public void setTextureView(TextureView ViewForShow) {
        if (ViewForShow == null) return;
        if (mTextureView != null)
            if (mTextureView.equals(ViewForShow)) return;

        mTextureView = ViewForShow;
        if (vlcVout.areViewsAttached())
            vlcVout.detachViews();
        vlcVout.setVideoView(mTextureView);
        vlcVout.attachViews();
    }

    public void reAttachSurfaceView(SurfaceView surfaceView) {
        if (surfaceView == null) return;

        mSurfaceView = surfaceView;
        if (vlcVout.areViewsAttached())
            vlcVout.detachViews();
        vlcVout.removeCallback(mIVLCVoutCallBack);
        vlcVout.setVideoView(surfaceView);
        vlcVout.attachViews();
        vlcVout.addCallback(mIVLCVoutCallBack);
    }

    //public SurfaceView getSurfaceView() {
    //    return mSurfaceView;
    //}

    public void play() {
        if (media == null) return;
        mMediaPlayer.play();
        //Log.d(TAG, "start to play ----->" + mURL);
    }

    public void pause() {
        if (media == null) return;
        mMediaPlayer.pause();
        //Log.d(TAG, "start to pause ----->" + mURL);
    }

    public void fastForward(float x) {
        mMediaPlayer.setRate(x);
    }

    public void fastBack(float x) {
        mMediaPlayer.setRate(x);
    }

    public void fastForward(int x) {
        mMediaPlayer.setPosition(mMediaPlayer.getPosition() + x);
    }

    public void fastBack(int x) {
        mMediaPlayer.setPosition(mMediaPlayer.getPosition() - x);
    }

    public void fastForward(long x) {
        mMediaPlayer.setTime(mMediaPlayer.getTime() + x);
    }

    public void fastBack(long x) {
        mMediaPlayer.setTime(mMediaPlayer.getTime() - x);
    }

    public void playPause() {
        if (isPlaying()) {
            pause();
        } else {
            play();
        }
    }

    private void pauseDetach() {
        if (mMediaPlayer.isPlaying()) {
            mMediaPlayer.pause();
        }
        vlcVout.detachViews();
        vlcVout.removeCallback(mIVLCVoutCallBack);
        mMediaPlayer.setEventListener(null);
    }

    public void stop() {
        try {
            if (vlcVout != null) {
                vlcVout.removeCallback(mIVLCVoutCallBack);
                vlcVout.detachViews();
            }

            if (media != null) {
                media.release();
                media = null;
            }

            if (mMediaPlayer != null) {
                mMediaPlayer.stop();
                mMediaPlayer.release();
                mMediaPlayer = null;
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
        //Log.d(TAG, "start to stop ----->" + mURL);
    }

    public void free() {
        try {

            if (vlcVout != null) {
                vlcVout.removeCallback(mIVLCVoutCallBack);
                vlcVout.detachViews();
            }

            if (mMediaPlayer != null) {
                mMediaPlayer.setEventListener(null);
                mMediaPlayer.stop();
                mMediaPlayer.release();
                mMediaPlayer = null;
            }
            if (media != null) {
                media.release();
                media = null;
            }

            PlayerUtil.FreeSingleVLC();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    class IVLCVoutCallBack implements IVLCVout.Callback, IVLCVout.OnNewVideoLayoutListener {

        @Override
        public void onNewVideoLayout(IVLCVout vlcVout, int width, int height, int visibleWidth, int visibleHeight, int sarNum, int sarDen) {
            Log.d(TAG, "IVLCVoutCallBack ---> onNewVideoLayout");
        }

        @Override
        public void onSurfacesCreated(IVLCVout ivlcVout) {
            Log.d(TAG, "IVLCVoutCallBack ---> onSurfacesCreated" + mSurfaceView);
            if (mSurfaceView != null) {
                vlcVout.setWindowSize(mSurfaceView.getWidth(), mSurfaceView.getHeight());
            } else if (mTextureView != null) {
                vlcVout.setWindowSize(mTextureView.getWidth(), mTextureView.getHeight());
            }
            mMediaPlayer.setAspectRatio(null);
            mMediaPlayer.setScale(0);

            Log.d(TAG, "IVLCVoutCallBack ---> onSurfacesCreated");
        }

        @Override
        public void onSurfacesDestroyed(IVLCVout ivlcVout) {
            Log.d(TAG, "IVLCVoutCallBack ---> onSurfacesDestroyed");
        }
    }

    public void setNoAudio() {
        media.addOption(":no-audio");
        return;
    }

    public void setSourceOption(String option) {
        this.media.addOption(option);
        mMediaPlayer.setMedia(media);
    }

    public int getAudioTracksCount() {
        return mMediaPlayer.getAudioTracksCount();
    }

    public Map<Integer, String> getAudioTracks() {
        Map<Integer, String> mtd = new HashMap<Integer, String>();
        MediaPlayer.TrackDescription[] TrackDescriptions = mMediaPlayer.getAudioTracks();
        for (MediaPlayer.TrackDescription td : TrackDescriptions) {
            mtd.put(td.id, td.name);
        }
        return mtd;
    }

    public int getAudioTrack() {
        return mMediaPlayer.getAudioTrack();
    }

    public void setAudioTrack(int index) {
        mMediaPlayer.setAudioTrack(index);
    }

    public void setCallback(PlayerCallback callback) {
        this.mOnPlayerEventCallBack = callback;
    }

    public Boolean isPlaying() {
        return mMediaPlayer.isPlaying();
    }

    public boolean isSeekable() {
        return mMediaPlayer.isSeekable();
    }

    public void setPlayTime(long l) {
        mMediaPlayer.setTime(l);
    }

    public void setVolume(int var1) {
        mMediaPlayer.setVolume(var1);
    }

    public int getVolume() {
        return mMediaPlayer.getVolume();
    }

    public Long getCurrentTime() {
        return mMediaPlayer.getTime();
    }

    public float getCurrentPosition() {
        return mMediaPlayer.getPosition();
    }

    public void setPosition(float f) {
        mMediaPlayer.setPosition(f);
    }

    public long getLength() {
        return mMediaPlayer.getLength();
    }


    public MediaPlayer getMediaPlayer() {
        return mMediaPlayer;
    }

    public int getPlayerState() {
        return mMediaPlayer.getPlayerState();
    }

    public void setRate(float v) {
        mMediaPlayer.setRate(v);
    }

}

