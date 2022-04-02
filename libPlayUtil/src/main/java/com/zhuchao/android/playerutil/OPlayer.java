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
    private String TAG = "oPlayer>>>";
    private Context mContext;
    private MediaPlayer mMediaPlayer = null;
    private Media media = null;
    private LibVLC FLibVLC = null;
    private IVLCVout vlcVout;
    private IVLCVoutCallBack mIVLCVoutCallBack;
    private PlayerCallback mOnPlayerEventCallBack = null;
    private TextureView mTextureView = null;
    private SurfaceView mSurfaceView = null;
    private Boolean HWDecoderEnabled = true;
    private int volumeVaule=30;
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
        try {
            FLibVLC = new LibVLC(mContext);
        } catch (Exception e) {
            e.printStackTrace();
        }
        mMediaPlayer = new MediaPlayer(FLibVLC);
        mMediaPlayer.setScale(0);
        mMediaPlayer.setEventListener(mEventListener);
        vlcVout = mMediaPlayer.getVLCVout();
        vlcVout.addCallback(mIVLCVoutCallBack);
        mMediaPlayer.setVolume(volumeVaule);
    }

    public OPlayer(Context context, ArrayList<String> options, PlayerCallback callback) {
        mContext = context;
        ArrayList<String> Options = new ArrayList<String>();
        mIVLCVoutCallBack = new IVLCVoutCallBack();
        mOnPlayerEventCallBack = callback;
        Options.clear();
        Options.add("--file-caching=10000");//文件缓存
        Options.add("--network-caching=10000");//网络缓存
        Options.add("--live-caching=10000");//直播缓存
        Options.add("--sout-mux-caching=1500");//输出缓存
        if (options == null)
            options = Options;
        try {
            FLibVLC = new LibVLC(mContext, options);
        } catch (IllegalStateException e) {
            e.printStackTrace();
        }
        mMediaPlayer = new MediaPlayer(FLibVLC);
        mMediaPlayer.setEventListener(mEventListener);
        vlcVout = mMediaPlayer.getVLCVout();
        vlcVout.addCallback(mIVLCVoutCallBack);
        mMediaPlayer.setVolume(volumeVaule);
    }

    public boolean setSource(String pathName) {
        if (media != null) {
            media.release();
            media = null;
        }
        if (pathName.isEmpty()) return false;
        if (pathName.startsWith("http") || pathName.startsWith("ftp") || pathName.startsWith("file")) {
            Uri uri = Uri.parse(pathName);
            setSource(uri);
            return true;
        }
        media = new Media(FLibVLC, pathName);
        //media.addOption(":no-audio");
        media.addOption(":fullscreen");
        media.addOption(":no-autoscale");
        media.addOption(":file-caching=10000");//文件缓存
        media.addOption(":network-caching=10000");//网络缓存
        media.addOption(":live-caching=10000");//直播缓存
        media.addOption(":sout-mux-caching=10000");//输出缓存
        setHWDecoderEnabled(true);
        mMediaPlayer.stop();
        mMediaPlayer.setMedia(media);
        mMediaPlayer.setAspectRatio(null);
        mMediaPlayer.setScale(0);
        mMediaPlayer.setVolume(volumeVaule);
        return true;
    }

    public boolean setSource(Uri uri) {
        if (media != null) {
            media.release();
            media = null;
        }
        if (uri == null) return false;
        media = new Media(FLibVLC, uri);
        //media.addOption(":no-audio");
        media.addOption(":fullscreen");
        media.addOption(":no-autoscale");
        media.addOption(":file-caching=10000");//文件缓存
        media.addOption(":network-caching=10000");//网络缓存
        media.addOption(":live-caching=10000");//直播缓存
        media.addOption(":sout-mux-caching=10000");//输出缓存
        setHWDecoderEnabled(true);
        mMediaPlayer = new MediaPlayer(FLibVLC);
        mMediaPlayer.stop();
        mMediaPlayer.setMedia(media);
        mMediaPlayer.setAspectRatio(null);
        mMediaPlayer.setScale(0);
        mMediaPlayer.setVolume(volumeVaule);
        return true;
    }

    public boolean setSource(AssetFileDescriptor afd) {
        if (media != null) {
            media.release();
            media = null;
        }
        if (afd == null) return false;
        media = new Media(FLibVLC, afd);
        //media.addOption(":no-audio");
        media.addOption(":fullscreen");
        media.addOption(":no-autoscale");
        media.addOption(":file-caching=10000");//文件缓存
        media.addOption(":network-caching=10000");//网络缓存
        media.addOption(":live-caching=10000");//直播缓存
        media.addOption(":sout-mux-caching=10000");//输出缓存
        setHWDecoderEnabled(true);
        mMediaPlayer = new MediaPlayer(FLibVLC);
        mMediaPlayer.stop();
        mMediaPlayer.setMedia(media);
        mMediaPlayer.setAspectRatio(null);
        mMediaPlayer.setScale(0);
        mMediaPlayer.setVolume(volumeVaule);
        return true;
    }

    public boolean setSource(FileDescriptor fd) {
        if (media != null) {
            media.release();
            media = null;
        }
        if (fd == null) return false;
        media = new Media(FLibVLC, fd);
        //media.addOption(":no-audio");
        media.addOption(":fullscreen");
        media.addOption(":no-autoscale");
        media.addOption(":file-caching=10000");//文件缓存
        media.addOption(":network-caching=10000");//网络缓存
        media.addOption(":live-caching=10000");//直播缓存
        media.addOption(":sout-mux-caching=10000");//输出缓存
        setHWDecoderEnabled(true);
        mMediaPlayer = new MediaPlayer(FLibVLC);
        mMediaPlayer.stop();
        mMediaPlayer.setMedia(media);
        mMediaPlayer.setAspectRatio(null);
        mMediaPlayer.setScale(0);
        mMediaPlayer.setVolume(volumeVaule);
        return true;
    }

    public void setSurfaceView(SurfaceView surfaceView) {
        vlcVout = this.mMediaPlayer.getVLCVout();
        if (surfaceView == null) return;
        if (mSurfaceView != null)
            if (mSurfaceView.equals(surfaceView)) return;
        if (vlcVout.areViewsAttached())
            vlcVout.detachViews();
        vlcVout.addCallback(mIVLCVoutCallBack);
        vlcVout.setVideoView(surfaceView);
        vlcVout.attachViews();
        mTextureView = null;
        mSurfaceView = surfaceView;
        mSurfaceView.getHolder().setKeepScreenOn(true);
    }

    public void setTextureView(TextureView textureView) {
        if (textureView == null) return;
        if (mTextureView != null)
            if (mTextureView.equals(textureView)) return;
        if (vlcVout.areViewsAttached())
            vlcVout.detachViews();
        vlcVout.addCallback(mIVLCVoutCallBack);
        vlcVout.setVideoView(textureView);
        vlcVout.attachViews();
        mSurfaceView = null;
        mTextureView = textureView;
    }

    public void reAttachSurfaceView(SurfaceView surfaceView) {
        vlcVout = mMediaPlayer.getVLCVout();
        if (surfaceView == null) {
            if (vlcVout.areViewsAttached())
                vlcVout.detachViews();
            return;
        }
        if (vlcVout.areViewsAttached())
            vlcVout.detachViews();
        vlcVout.setVideoView(surfaceView);
        vlcVout.attachViews();
        //vlcVout.removeCallback(mIVLCVoutCallBack);
        //vlcVout.addCallback(mIVLCVoutCallBack);
        mSurfaceView = surfaceView;
        mTextureView = null;
    }

    public void reAttachSurfaceView(TextureView textureView) {
        if (textureView == null) return;
        mTextureView = textureView;
        if (vlcVout.areViewsAttached())
            vlcVout.detachViews();
        //vlcVout.removeCallback(mIVLCVoutCallBack);
        vlcVout.setVideoView(mTextureView);
        vlcVout.attachViews();
        //vlcVout.addCallback(mIVLCVoutCallBack);
        mTextureView = textureView;
        mSurfaceView =null;
    }

    public void play() {
        if (media == null) return;
        mMediaPlayer.play();
    }

    public void pause() {
        if (media == null) return;
        mMediaPlayer.pause();
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

    public void stop() {
        try {
            if (mMediaPlayer != null) {
                if(mMediaPlayer.getPlayerState() != Media.State.Stopped)
                mMediaPlayer.stop();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void resume() {
        if (mMediaPlayer != null) {
            mMediaPlayer.play();
        }
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
            if (FLibVLC != null)
                FLibVLC.release();
            FLibVLC = null;
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void setNoAudio() {
        media.addOption(":no-audio");
    }

    public void setOption(String option) {
        this.media.addOption(option);
        mMediaPlayer.setMedia(media);
    }

    public int getAudioTracksCount() {
        return mMediaPlayer.getAudioTracksCount();
    }

    public Map<Integer, String> getAudioTracks() {
        Map<Integer, String> mtd = new HashMap<Integer, String>();
        if (mMediaPlayer == null) return mtd;
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
        volumeVaule = var1;
        mMediaPlayer.setVolume(volumeVaule);
    }

    public int getVolume() {
        volumeVaule = mMediaPlayer.getVolume();
        return volumeVaule;
    }

    public Long getTime() {
        return mMediaPlayer.getTime();
    }

    public float getPosition() {
        return mMediaPlayer.getPosition();
    }

    public void setPosition(float f) {
        mMediaPlayer.setPosition(f);
    }

    public long getLength() {
        return mMediaPlayer.getLength();
    }

    public int getPlayerState() {
        return mMediaPlayer.getPlayerState();
    }

    public void setRate(float v) {
        mMediaPlayer.setRate(v);
    }

    public void setWindowSize(int width, int height) {
        mMediaPlayer.getVLCVout().setWindowSize(width, height);
    }

    public void setAspectRatio(String aspect) {
        mMediaPlayer.setAspectRatio(aspect);
    }

    public void setScale(float scale) {
        mMediaPlayer.setScale(scale);
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

    class IVLCVoutCallBack implements IVLCVout.Callback, IVLCVout.OnNewVideoLayoutListener {
        @Override
        public void onNewVideoLayout(IVLCVout vlcVout, int width, int height, int visibleWidth, int visibleHeight, int sarNum, int sarDen) {
            Log.d(TAG, "IVLCVoutCallBack ---> width=" + width + ",height=" + height + ",visibleWidth=" + visibleWidth + ",visibleHeight=" + visibleHeight
                    + ",sarNum=" + sarNum + ",sarDen=" + sarDen);
        }

        @Override
        public void onSurfacesCreated(IVLCVout ivlcVout) {
            Log.d(TAG, "IVLCVoutCallBack ---> onSurfacesCreated");
            if (mSurfaceView != null) {
                vlcVout.setWindowSize(mSurfaceView.getWidth(), mSurfaceView.getHeight());
            } else if (mTextureView != null) {
                vlcVout.setWindowSize(mTextureView.getWidth(), mTextureView.getHeight());
            }
            //mMediaPlayer.setAspectRatio(null);
            //mMediaPlayer.setScale(0);
        }

        @Override
        public void onSurfacesDestroyed(IVLCVout ivlcVout) {
            //Log.d(TAG, "IVLCVoutCallBack ---> onSurfacesDestroyed");
        }
    }
}

