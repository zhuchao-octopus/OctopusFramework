package com.zhuchao.android.playerutil;

import static android.media.MediaPlayer.TrackInfo.MEDIA_TRACK_TYPE_AUDIO;

import android.content.Context;
import android.content.res.AssetFileDescriptor;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.net.Uri;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.TextureView;

import com.zhuchao.android.callbackevent.PlaybackEvent;
import com.zhuchao.android.callbackevent.PlayerCallback;
import com.zhuchao.android.libfileutils.MMLog;

import java.io.FileDescriptor;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class MPlayer extends PlayControl implements MediaPlayer.OnCompletionListener,
        MediaPlayer.OnSeekCompleteListener,
        MediaPlayer.OnErrorListener,
        MediaPlayer.OnBufferingUpdateListener,
        MediaPlayer.OnVideoSizeChangedListener,
        SurfaceHolder.Callback {

    private final String TAG = "MPlayer>>>";
    private MediaPlayer mediaPlayer = null;
    private ProgressThread progressThread = null;
    private static int playStatus = PlaybackEvent.Status_NothingIdle;
    private static long lDuration = 0;
    public MPlayer(Context context, PlayerCallback callback) {
        super(context, callback);
        playStatus = PlaybackEvent.Status_NothingIdle;
    }

    @Override
    public String getTAG() {
        return TAG;
    }

    @Override
    public void setSource(String filePath) {
        try {
            resetComponent(false);
            playStatus = PlaybackEvent.Status_Opening;
            mediaPlayer.setDataSource(filePath);
        } catch (IllegalArgumentException e) {
            MMLog.e(TAG, "setSource() " + e.toString() +" filePath = "+filePath);
            playStatus = PlaybackEvent.Status_Error;
        } catch (IOException e) {
            MMLog.e(TAG, "setSource() " + e.toString());
            playStatus = PlaybackEvent.Status_Error;
        } catch (Exception e) {
            MMLog.e(TAG, "setSource() " + e.toString());
            playStatus = PlaybackEvent.Status_Error;
        }
    }

    @Override
    public void setSource(Uri uri) {
        try {
            resetComponent(false);
            playStatus = PlaybackEvent.Status_Opening;
            mediaPlayer.setDataSource(mContext, uri);
        } catch (IllegalArgumentException e) {
            MMLog.e(TAG, "setSource() " + e.toString());
            playStatus = PlaybackEvent.Status_Error;
        } catch (IOException e) {
            MMLog.e(TAG, "setSource() " + e.toString());
            playStatus = PlaybackEvent.Status_Error;
        } catch (Exception e) {
            MMLog.e(TAG, "setSource() " + e.toString());
            playStatus = PlaybackEvent.Status_Error;
        }
    }

    @Override
    public void setSource(AssetFileDescriptor afd) {
        try {
            resetComponent(false);
            playStatus = PlaybackEvent.Status_Opening;
            mediaPlayer.setDataSource(afd);
        } catch (IllegalArgumentException e) {
            MMLog.e(TAG, "setSource() " + e.toString());
            playStatus = PlaybackEvent.Status_Error;
        } catch (IOException e) {
            MMLog.e(TAG, "setSource() " + e.toString());
            playStatus = PlaybackEvent.Status_Error;
        } catch (Exception e) {
            MMLog.e(TAG, "setSource() " + e.toString());
            playStatus = PlaybackEvent.Status_Error;
        }
    }

    @Override
    public void setSource(FileDescriptor fd) {
        try {
            resetComponent(false);
            playStatus = PlaybackEvent.Status_Opening;
            mediaPlayer.setDataSource(fd);
        } catch (IllegalArgumentException e) {
            MMLog.e(TAG, "setSource() " + e.toString());
            playStatus = PlaybackEvent.Status_Error;
        } catch (IOException e) {
            MMLog.e(TAG, "setSource() " + e.toString());
            playStatus = PlaybackEvent.Status_Error;
        } catch (Exception e) {
            MMLog.e(TAG, "setSource() " + e.toString());
            playStatus = PlaybackEvent.Status_Error;
        }
    }

    @Override
    public void setSurfaceView(SurfaceView surfaceView) {
        if (surfaceView == null) return;
        if (surfaceView == mSurfaceView) return;
        mSurfaceView = surfaceView;
        mTextureView = null;
    }

    @Override
    public void setTextureView(TextureView textureView) {
        mSurfaceView = null;
        mTextureView = textureView;
    }

    @Override
    public void reAttachSurfaceView(SurfaceView surfaceView) {
        if (surfaceView == null) return;
        if (surfaceView == mSurfaceView) return;
        free();
        setSurfaceView(surfaceView);
        resetComponent(false);
        MMLog.log(TAG, "re attached surface view successful");
    }

    @Override
    public void reAttachTextureView(TextureView textureView) {

    }

    @Override
    public void fastForward(float x) {

    }

    @Override
    public void fastBack(float x) {

    }

    @Override
    public void fastForward(int x) {

    }

    @Override
    public void fastBack(int x) {

    }

    @Override
    public void fastForward(long x) {

    }

    @Override
    public void fastBack(long x) {

    }
    //播放循序
    //setSource(url);
    //.play();
    @Override
    public void play() {
        try {
            asyncPlayProcess(playStatus);
        } catch (Exception e) {
            MMLog.e(TAG, "play() " + "playStatus = "+playStatus +" "+ e.getMessage());
        }
    }

    @Override
    public void pause() {
        mediaPlayer.pause();
        playStatus = PlaybackEvent.Status_Paused;
    }

    @Override
    public void playPause() {
        if (mediaPlayer == null) return;
        if (mediaPlayer.isPlaying()) {
            pause();
            MMLog.log(TAG, "playPause pause directly playStatus=" + playStatus);
        } else {
            asyncPlayProcess(playStatus);
        }

    }

    @Override
    public void stop() {
        if (playStatus == PlaybackEvent.Status_Stopped) return;
        try {
            if (mediaPlayer != null)
                mediaPlayer.stop();
        } catch (IllegalStateException e) {
            MMLog.e(TAG, "stop() " + e.toString());
        }
        playStatus = PlaybackEvent.Status_Stopped;
    }

    @Override
    public void resume() {

    }

    @Override
    public Boolean isPlaying() {
        if (mediaPlayer != null) {
            return mediaPlayer.isPlaying();
        } else {
            return false;
        }
    }

    @Override
    public void setPlayTime(long l) {
        if (mediaPlayer == null) return;
        try {
            mediaPlayer.seekTo((int) l);
        } catch (IllegalStateException e) {
            MMLog.e(TAG, "setPlayTime() " + e.toString());
        }
        MMLog.log(TAG, "setPlayTime = " + l);
    }

    @Override
    public void setVolume(int var1) {
        if (var1 < 0) return;
        volumeValue = var1;
        try {
            if ((mediaPlayer != null) && (mediaPlayer.isPlaying()))
                mediaPlayer.setVolume(volumeValue / 100, volumeValue / 100);
        } catch (Exception e) {
            MMLog.e(TAG, "setVolume() " + e.toString());
        }
    }

    @Override
    public int getVolume() {
        return volumeValue;
    }

    @Override
    public Long getTime() {
        long l = 0;
        try {
            if ((mediaPlayer != null) && (mediaPlayer.isPlaying()))
                l = Long.valueOf(mediaPlayer.getCurrentPosition());
        } catch (Exception e) {
            MMLog.e(TAG, "getTime() " + e.toString());
        }
        return l;
    }

    @Override
    public float getPosition() {
        try {
            if ((mediaPlayer != null) && (mediaPlayer.isPlaying()))
                return mediaPlayer.getCurrentPosition();
        } catch (Exception e) {
            MMLog.e(TAG, "getPosition() " + e.toString());
        }
        return 0;
    }

    @Override
    public void setPosition(float f) {

    }

    @Override
    public long getLength() {
        return lDuration;
    }

    @Override
    public int getPlayerStatus() {
        if ((mediaPlayer != null) && (mediaPlayer.isPlaying()))
            playStatus = PlaybackEvent.Status_Playing;
        else if (mediaPlayer == null)
            playStatus = PlaybackEvent.Status_NothingIdle;
        return playStatus;
    }

    @Override
    public void setRate(float v) {
        try {
            if ((mediaPlayer != null) && (mediaPlayer.isPlaying()))
                mediaPlayer.setPlaybackParams(mediaPlayer.getPlaybackParams().setSpeed(v));
        } catch (Exception e) {
            MMLog.e(TAG, "setRate() " + e.toString()+ " v = " + v);
        }
    }

    @Override
    public void setWindowSize(int width, int height) {
    }

    @Override
    public void setAspectRatio(String aspect) {

    }

    @Override
    public void setScale(float scale) {
        try {
            if (mediaPlayer != null)
                mediaPlayer.setVideoScalingMode((int) scale);
        } catch (Exception e) {
            MMLog.e(TAG, "setScale() " + e.toString());
        }
    }

    @Override
    public void setHWDecoderEnabled(Boolean HWDecoderEnabled) {

    }

    @Override
    public void setNoAudio() {
        mediaPlayer.setVolume(0, 0);
    }

    @Override
    public void setOption(String option) {

    }

    @Override
    public int getAudioTracksCount() {
        MediaPlayer.TrackInfo[] trackInfo = null;
        if (mediaPlayer != null)  {
            try {
                trackInfo = mediaPlayer.getTrackInfo();
            } catch (IllegalStateException e) {
                MMLog.e(TAG, "getAudioTracksCount() " + e.toString());
            }
            return trackInfo.length;
        } else
            return 0;
    }

    @Override
    public Map<Integer, String> getAudioTracks() {
        MediaPlayer.TrackInfo[] trackInfo = null;
        Map<Integer, String> mtd = new HashMap<Integer, String>();
        if (mediaPlayer == null) return mtd;

        try {
            trackInfo = mediaPlayer.getTrackInfo();
        } catch (IllegalStateException e) {
            MMLog.e(TAG, "getAudioTracks() " + e.toString());
        }

        for (int i = 0; i < trackInfo.length - 1; i++) {
            mtd.put(i, trackInfo[i].toString());
        }
        return mtd;
    }

    @Override
    public int getAudioTrack() {
        try {
            if (mediaPlayer != null)
                return mediaPlayer.getSelectedTrack(MEDIA_TRACK_TYPE_AUDIO);
        } catch (IllegalStateException e) {
            MMLog.e(TAG, "getAudioTrack() " + e.toString());
        }
        return -1;
    }

    @Override
    public void setAudioTrack(int index) {
        try {
            if (mediaPlayer != null) {
                MMLog.e(TAG, "setAudioTrack() track = " + index);
                mediaPlayer.selectTrack(index);
            }
        } catch (IllegalStateException e) {
            MMLog.e(TAG, "setAudioTrack() " + e.toString());
        }
    }

    @Override
    public void setCallback(PlayerCallback callback) {
        playerEventCallBack = callback;
    }

    @Override
    public void onCompletion(MediaPlayer mediaPlayer) {
        playStatus = PlaybackEvent.Status_Ended;
        MMLog.log(TAG, "onCompletion playing ended  playStatus = " + playStatus);
    }

    @Override
    public void onSeekComplete(MediaPlayer mediaPlayer) {
        MMLog.log(TAG, "onSeekComplete:" + mediaPlayer.toString());
        asyncPlayProcess(PlaybackEvent.Status_SEEKING);
    }

    @Override
    public boolean onError(MediaPlayer mediaPlayer, int i, int i1) {
        playStatus = PlaybackEvent.Status_Error;
        MMLog.log(TAG, "onError:" + i + "," + i1 + " playStatus = " + playStatus);
        return false;//return false to call OnCompletionListener.onCompletion()方法。
    }

    @Override
    public void onBufferingUpdate(MediaPlayer mediaPlayer, int i) {
        //MLog.log(TAG, "onBufferingUpdate:" + i);
        if (playerEventCallBack != null)
            playerEventCallBack.OnEventCallBack(
                    playStatus,
                    mediaPlayer.getCurrentPosition(),
                    mediaPlayer.getCurrentPosition(),
                    mediaPlayer.getCurrentPosition(),
                    0,
                    0,
                    0,
                    i,
                    getLength());
    }

    @Override
    public void surfaceCreated(SurfaceHolder surfaceHolder) {
        MMLog.log(TAG, "surfaceCreated," + surfaceHolder.toString());
        try {
            if (mediaPlayer != null && surfaceHolder != null) {
                mediaPlayer.setDisplay(surfaceHolder);
                surfaceHolder.setFixedSize(mediaPlayer.getVideoWidth(), mediaPlayer.getVideoHeight());
            }
        } catch (Exception e) {
            MMLog.e(TAG, "surfaceCreated() " + e.toString());
        }
    }

    @Override
    public void surfaceChanged(SurfaceHolder surfaceHolder, int i, int i1, int i2) {
        MMLog.log(TAG, "surfaceChanged," + surfaceHolder.toString());
    }

    @Override
    public void surfaceDestroyed(SurfaceHolder surfaceHolder) {
        MMLog.log(TAG, "surfaceDestroyed," + surfaceHolder.toString());
    }

    @Override
    public void onVideoSizeChanged(MediaPlayer mediaPlayer, int i, int i1) {
    }

    @Override
    public void free() {
        if (mediaPlayer == null) return;
        try {
            //mediaPlayer.stop();
            mediaPlayer.reset();
            mSurfaceView.getHolder().removeCallback(this);
            mediaPlayer.setOnCompletionListener(null);
            mediaPlayer.setOnSeekCompleteListener(null);
            mediaPlayer.setOnErrorListener(null);
            mediaPlayer.setOnBufferingUpdateListener(null);
            mediaPlayer.release();
            mediaPlayer = null;
            if (progressThread != null)
                progressThread.finish();
            progressThread = null;
            playStatus = PlaybackEvent.Status_NothingIdle;
        } catch (Exception e) {
            MMLog.e(TAG, "free() " + e.toString());
        }
    }

    public synchronized void resetComponent(boolean nf) {
        MMLog.e(TAG, "resetComponent() nf = " + nf);
        if (nf)   free();

        try {
            if (mediaPlayer == null)
                mediaPlayer = new MediaPlayer();
            mediaPlayer.reset();//必须调用mediaPlayer.prepareAsync();
        } catch (Exception e) {
            MMLog.e(TAG, "resetComponent() create failed " + e.toString());
            mediaPlayer = null;
            return;
        }

        try
        {
            if (mSurfaceView != null) {
                mSurfaceView.getHolder().addCallback(this);
                if (mSurfaceView.getHolder() != null) {
                    mediaPlayer.setDisplay(mSurfaceView.getHolder());
                } else MMLog.log(TAG, "resetComponent().mSurfaceView.getHolder() = null");
            } else MMLog.log(TAG, "resetComponent().mSurfaceView = null");

        } catch (Exception e) {
            MMLog.e(TAG, "resetComponent() mSurfaceView " + e.toString());
            //这里可能报异常，setSource后要重新 setSurfaceView
            //setSurfaceView 异常，依然可以播放，此处异常不重要
        }
        lDuration = 0;
        mediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
        mediaPlayer.setOnCompletionListener(this);
        mediaPlayer.setOnSeekCompleteListener(this);
        mediaPlayer.setOnErrorListener(this);
        mediaPlayer.setOnBufferingUpdateListener(this);
        playStatus = PlaybackEvent.Status_NothingIdle;
    }

    private synchronized void asyncPlayProcess(int Status) {
        MMLog.log(TAG, "enter asyncPlayProcess() playStatus = " + Status);
        try {
            switch (Status) {
                case PlaybackEvent.Status_NothingIdle:
                    MMLog.log(TAG, "asyncPlayProcess player is idle, nothing to do playStatus = " + Status);
                    break;
                case PlaybackEvent.Status_Stopped:
                case PlaybackEvent.Status_Opening:
                    StartPlayProgressThread(); //开启进度 pooling 线程
                    playStatus = PlaybackEvent.Status_Buffering;
                    try {
                        mediaPlayer.prepareAsync();
                    } catch (IllegalStateException e) {
                        MMLog.e(TAG, "asyncPlayProcess() " + e.toString());
                        playStatus = PlaybackEvent.Status_Error;
                        resetComponent(true);//复位
                    }
                    catch (Exception e) {
                        MMLog.e(TAG, "asyncPlayProcess().prepareAsync() " + "playStatus = "+playStatus +" "+e.toString() );
                        playStatus = PlaybackEvent.Status_Error;
                        resetComponent(true);//复位
                    }
                    mediaPlayer.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
                        @Override
                        public void onPrepared(MediaPlayer mp) {
                            MMLog.log(TAG, "asyncPlayProcess start to play async... playStatus = " + Status);
                            playStatus = PlaybackEvent.Status_Playing;
                            try {
                                lDuration = mediaPlayer.getDuration();
                            } catch (Exception e) {
                                MMLog.e(TAG, "asyncPlayProcess().getDuration() "+ "playStatus = "+playStatus +" " + e.toString());
                            }
                            mp.start();
                        }
                    });
                    break;
                case PlaybackEvent.Status_Buffering:
                case PlaybackEvent.Status_Playing:
                    MMLog.log(TAG, "asyncPlayProcess is playing playStatus = " + Status);
                    break;

                case PlaybackEvent.Status_Paused:
                case PlaybackEvent.Status_SEEKING:
                    MMLog.log(TAG, "asyncPlayProcess start to play directly playStatus = " + Status);
                    playStatus = PlaybackEvent.Status_Playing;
                    mediaPlayer.start();
                    break;
                case PlaybackEvent.Status_Ended:
                case PlaybackEvent.Status_Error:
                default:
                    MMLog.log(TAG, "asyncPlayProcess do nothing, playStatus = " + Status);
                    break;
            }
        } catch (IllegalStateException e) {
            MMLog.e(TAG, "asyncPlayProcess() "+ "playStatus = "+playStatus +" " + e.toString());
        }
        catch (Exception e) {
            MMLog.e(TAG, "asyncPlayProcess() " + "playStatus = "+playStatus +" "+ e.getMessage());
        }
    }

    private void StartPlayProgressThread() {   //创建进度状态线程
        if (progressThread == null) {
            progressThread = new ProgressThread();
            progressThread.start();
        } else {
            if (!progressThread.isActive()) {
                progressThread = new ProgressThread();
                progressThread.start();
            }
        }
    }

    private class ProgressThread extends Thread {
        private boolean keepActive = true;
        private int currentPosition = 0;

        @Override
        public void run() {
            super.run();
            while (keepActive) {
                try {
                    if ((mediaPlayer != null) && (mediaPlayer.isPlaying())) {
                        try {
                            currentPosition = mediaPlayer.getCurrentPosition();
                        }
                        catch (IllegalStateException e) {
                            MMLog.e(TAG, "Progress polling getCurrentPosition() " + e.toString());
                        }
                        catch (Exception e) {
                            MMLog.e(TAG, "Progress polling getCurrentPosition() " + e.toString());
                        }
                    }
                    if (playerEventCallBack != null)
                        playerEventCallBack.OnEventCallBack(
                                playStatus,
                                currentPosition,
                                currentPosition,
                                currentPosition,
                                0,
                                0,
                                0,
                                0,
                                lDuration);
                } catch (Exception e) {
                    MMLog.e(TAG, "Progress polling " + e.toString());
                }
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    MMLog.e(TAG, "Progress polling " + e.toString());
                }
                switch (playStatus) {
                    case PlaybackEvent.Status_Stopped:
                        break; //此状态待外部处理
                    case PlaybackEvent.Status_Ended:
                    case PlaybackEvent.Status_Error:
                        break;
                }
            }
        }

        void finish() {
            keepActive = false;
        }

        boolean isActive() {
            return keepActive;
        }
    }
}
