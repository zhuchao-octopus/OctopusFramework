package com.zhuchao.android.player;

import static com.zhuchao.android.fileutils.FileUtils.EmptyString;

import android.content.Context;
import android.content.res.AssetFileDescriptor;
import android.net.Uri;
import android.view.SurfaceView;
import android.view.TextureView;

import androidx.annotation.NonNull;

import com.zhuchao.android.callbackevent.PlaybackEvent;
import com.zhuchao.android.callbackevent.PlayerCallback;
import com.zhuchao.android.fileutils.MMLog;

import org.videolan.libvlc.LibVLC;
import org.videolan.libvlc.Media;
import org.videolan.libvlc.MediaPlayer;

import org.videolan.libvlc.interfaces.IVLCVout;
//import org.videolan.libvlc.IVLCVout;

import java.io.FileDescriptor;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;


public class OPlayer extends PlayControl {
    private String TAG = "OPlayer";
    private MediaPlayer mMediaPlayer = null;
    private Media media = null;
    private LibVLC FLibVLC = null;
    private IVLCVout vlcVout;
    private IVLCVoutCallBack mIVLCVoutCallBack;

    @Override
    public String getTAG() {
        return TAG;
    }

    private MediaPlayer.EventListener mEventListener = new MediaPlayer.EventListener() {
        @Override
        public void onEvent(MediaPlayer.Event event) {
            playerStatusInfo.setEventType(mMediaPlayer.getPlayerState());
            playerStatusInfo.setPositionChanged(event.getPositionChanged());
            playerStatusInfo.setTimeChanged(event.getTimeChanged());
            playerStatusInfo.setLengthChanged(event.getLengthChanged());//getLength()
            playerStatusInfo.setOutCount(event.getVoutCount());
            playerStatusInfo.setChangedType(event.getEsChangedType());
            playerStatusInfo.setBuffering(event.getBuffering());
            if (playerEventCallBack != null)
                playerEventCallBack.onEventPlayerStatus(playerStatusInfo);
        }
    };

    public OPlayer(Context context, PlayerCallback callback) {
        super(context, callback);
        //ArrayList<String> Options = new ArrayList<String>();
        //Options.add("--cr-average=10000");
        mIVLCVoutCallBack = new IVLCVoutCallBack();
        try {
            FLibVLC = new LibVLC(mContext);
            mMediaPlayer = new MediaPlayer(FLibVLC);
            mMediaPlayer.setScale(0);
            mMediaPlayer.setEventListener(mEventListener);
            vlcVout = mMediaPlayer.getVLCVout();
            vlcVout.addCallback(mIVLCVoutCallBack);
            mMediaPlayer.setVolume(DefaultVolumeValue);
            //MLog.log(TAG,"OPlayer=========>");
        } catch (Exception e) {
            //e.printStackTrace();
            MMLog.log(TAG, e.toString());
        }
    }

    public OPlayer(Context context, ArrayList<String> options, PlayerCallback callback) {
        super(context, callback);
        mIVLCVoutCallBack = new IVLCVoutCallBack();
        //options.add("--cr-average=10000");
        //Options.add("--file-caching=300");//文件缓存
        //Options.add("--network-caching=10000");//网络缓存
        //Options.add("--live-caching=10000");//直播缓存
        //Options.add("--sout-mux-caching=1500");//输出缓存
        try {
            FLibVLC = new LibVLC(mContext, options);
            mMediaPlayer = new MediaPlayer(FLibVLC);
            mMediaPlayer.setEventListener(mEventListener);
            vlcVout = mMediaPlayer.getVLCVout();
            vlcVout.addCallback(mIVLCVoutCallBack);
            mMediaPlayer.setVolume(DefaultVolumeValue);
            //MLog.log(TAG, "OPlayer=========>");
        } catch (IllegalStateException e) {
            MMLog.log(TAG, e.toString());
        }
    }

    private void freeMedia() {
        if (media != null) {
            media.release();
            media = null;
        }
    }

    public void setSource(@NonNull String filePath) {
        if (EmptyString(filePath)) return;
        if (filePath.startsWith("http") ||
                filePath.startsWith("rtsp") ||
                filePath.startsWith("ftp") ||
                filePath.startsWith("file")) {
            Uri uri = Uri.parse(filePath);
            setSource(uri);
            return;
        }
        freeMedia();
        media = new Media(FLibVLC, filePath);
        //media.addOption(":no-audio");
        media.addOption(":fullscreen");
        //media.addOption(":no-autoscale");
        //media.addOption(":file-caching=10000");//文件缓存
        //media.addOption(":network-caching=10000");//网络缓存
        //media.addOption(":live-caching=10000");//直播缓存
        //media.addOption(":sout-mux-caching=10000");//输出缓存
        setHWDecoderEnabled(true);
        mMediaPlayer.setMedia(media);
        playerStatusInfo.setSourcePrepared(true);
        //mMediaPlayer.setAspectRatio(null);
        //mMediaPlayer.setScale(0);
        mMediaPlayer.setVolume(DefaultVolumeValue);
    }

    public void setSource(@NonNull Uri uri) {
        freeMedia();
        media = new Media(FLibVLC, uri);
        //media.addOption(":no-audio");
        media.addOption(":fullscreen");
        //media.addOption(":no-autoscale");
        //media.addOption(":file-caching=10000");//文件缓存
        //media.addOption(":network-caching=10000");//网络缓存
        //media.addOption(":live-caching=10000");//直播缓存
        //media.addOption(":sout-mux-caching=10000");//输出缓存
        setHWDecoderEnabled(true);
        mMediaPlayer.setMedia(media);
        playerStatusInfo.setSourcePrepared(true);
        //mMediaPlayer.setAspectRatio(null);
        //mMediaPlayer.setScale(0);
        mMediaPlayer.setVolume(DefaultVolumeValue);
    }

    public void setSource(@NonNull AssetFileDescriptor afd) {
        freeMedia();
        media = new Media(FLibVLC, afd);
        //media.addOption(":no-audio");
        media.addOption(":fullscreen");
        //media.addOption(":no-autoscale");
        //media.addOption(":file-caching=10000");//文件缓存
        //media.addOption(":network-caching=10000");//网络缓存
        //media.addOption(":live-caching=10000");//直播缓存
        //media.addOption(":sout-mux-caching=10000");//输出缓存
        setHWDecoderEnabled(true);
        mMediaPlayer.setMedia(media);
        playerStatusInfo.setSourcePrepared(true);
        //mMediaPlayer.setAspectRatio(null);
        //mMediaPlayer.setScale(0);
        mMediaPlayer.setVolume(DefaultVolumeValue);
    }

    public void setSource(@NonNull FileDescriptor fd) {
        freeMedia();
        media = new Media(FLibVLC, fd);
        //media.addOption(":no-audio");
        media.addOption(":fullscreen");
        //media.addOption(":no-autoscale");
        //media.addOption(":file-caching=10000");//文件缓存
        //media.addOption(":network-caching=10000");//网络缓存
        //media.addOption(":live-caching=10000");//直播缓存
        //media.addOption(":sout-mux-caching=10000");//输出缓存
        setHWDecoderEnabled(true);
        mMediaPlayer.setMedia(media);
        playerStatusInfo.setSourcePrepared(true);
        //mMediaPlayer.setAspectRatio(null);
        //mMediaPlayer.setScale(0);
        mMediaPlayer.setVolume(DefaultVolumeValue);
    }

    public void setSurfaceView(@NonNull SurfaceView surfaceView) {
        if (mMediaPlayer == null) return;
        vlcVout = mMediaPlayer.getVLCVout();
        if (surfaceView.equals(mSurfaceView)) return;

        if (vlcVout.areViewsAttached())
            vlcVout.detachViews();

        vlcVout.setVideoView(surfaceView);
        vlcVout.attachViews();
        mTextureView = null;
        mSurfaceView = surfaceView;
        mSurfaceView.getHolder().setKeepScreenOn(true);
    }

    public void setTextureView(@NonNull TextureView textureView) {

        if (textureView.equals(mTextureView)) return;

        if (vlcVout.areViewsAttached())
            vlcVout.detachViews();

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

        mSurfaceView = surfaceView;
        mTextureView = null;
        MMLog.log(TAG, "re attached surface view successful");
    }

    public void reAttachTextureView(TextureView textureView) {
        if (textureView == null) return;
        mTextureView = textureView;
        if (vlcVout.areViewsAttached())
            vlcVout.detachViews();

        vlcVout.setVideoView(mTextureView);
        vlcVout.attachViews();

        mTextureView = textureView;
        mSurfaceView = null;
    }

    @Override
    public void pushTo(String filePath, boolean duplicated) {
        freeMedia();
        media = new Media(FLibVLC, filePath);
        //media.addOption(":no-audio");
        media.addOption(":fullscreen");
        //media.addOption(":no-autoscale");
        //media.addOption(":file-caching=10000");//文件缓存
        //media.addOption(":network-caching=10000");//网络缓存
        //media.addOption(":live-caching=10000");//直播缓存
        //media.addOption(":sout-mux-caching=10000");//输出缓存
        if (duplicated)
            media.addOption(new String(":sout=#duplicate{dst=rtp{sdp=rtsp://:8554/0},dst=display}"));//边推流边播放
        else
            media.addOption(new String(":sout=#rtp{sdp=rtsp://:8554/0}"));//只推流

        setHWDecoderEnabled(true);
        mMediaPlayer.setMedia(media);
        playerStatusInfo.setSourcePrepared(true);
        //mMediaPlayer.setAspectRatio(null);
        //mMediaPlayer.setScale(0);
        mMediaPlayer.setVolume(DefaultVolumeValue);

    }

    public void play() {
        if (media == null) return;
        mMediaPlayer.play();
        //MMLog.log(TAG,"play------>");
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
        if (mMediaPlayer.getPlayerState() != Media.State.Stopped)
            mMediaPlayer.stop();
    }

    public void resume() {
        mMediaPlayer.play();
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
        DefaultVolumeValue = var1;
        mMediaPlayer.setVolume(DefaultVolumeValue);
    }

    public int getVolume() {
        DefaultVolumeValue = mMediaPlayer.getVolume();
        return DefaultVolumeValue;
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

    public int getPlayerStatus() {
        if (mMediaPlayer != null)
            return mMediaPlayer.getPlayerState();
        else
            return PlaybackEvent.Status_Ended;
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

    public void setHWDecoderEnabled(Boolean HWDecoderEnabled) {
        this.hwDecoderEnabled = HWDecoderEnabled;
        if (media != null) {
            try {
                media.setHWDecoderEnabled(HWDecoderEnabled, HWDecoderEnabled);
            } catch (Exception e) {
                e.printStackTrace();
            }
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

    @Override
    public void deselectTrack(int index) {

    }

    public void setCallback(PlayerCallback callback) {
        this.playerEventCallBack = callback;
    }

    class IVLCVoutCallBack implements IVLCVout.Callback, IVLCVout.OnNewVideoLayoutListener {
        @Override
        public void onNewVideoLayout(IVLCVout vlcVout, int width, int height, int visibleWidth, int visibleHeight, int sarNum, int sarDen) {
            //MMLog.log(TAG, "IVLCVoutCallBack ---> width=" + width + ",height=" + height + ",visibleWidth=" + visibleWidth + ",visibleHeight=" + visibleHeight
            //        + ",sarNum=" + sarNum + ",sarDen=" + sarDen);
            playerStatusInfo.setVideoH(height);
            playerStatusInfo.setVideoW(width);
            playerStatusInfo.setSurfaceH(visibleHeight);
            playerStatusInfo.setSurfaceW(visibleWidth);
        }

        @Override
        public void onSurfacesCreated(IVLCVout ivlcVout) {
            MMLog.log(TAG, "IVLCVoutCallBack ---> onSurfacesCreated");
            if (mSurfaceView != null) {
                vlcVout.setWindowSize(mSurfaceView.getWidth(), mSurfaceView.getHeight());
            } else if (mTextureView != null) {
                vlcVout.setWindowSize(mTextureView.getWidth(), mTextureView.getHeight());
            }
            mMediaPlayer.setAspectRatio(null);
            mMediaPlayer.setScale(0);
            playerStatusInfo.setSurfacePrepared(true);
        }

        @Override
        public void onSurfacesDestroyed(IVLCVout ivlcVout) {
            //MLog.log(TAG, "IVLCVoutCallBack ---> onSurfacesDestroyed");
            playerStatusInfo.setSurfacePrepared(false);
        }
    }

}

