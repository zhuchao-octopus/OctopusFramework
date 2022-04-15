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
import com.zhuchao.android.libfileutils.MLog;

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
            resetPlaySession(false);
            playStatus = PlaybackEvent.Status_Opening;
            mediaPlayer.setDataSource(filePath);
        } catch (IllegalArgumentException e) {
            e.printStackTrace();
            playStatus = PlaybackEvent.Status_Error;
        } catch (IOException e) {
            e.printStackTrace();
            playStatus = PlaybackEvent.Status_Error;
        } catch (Exception e) {
            e.printStackTrace();
            playStatus = PlaybackEvent.Status_Error;
        }
    }

    @Override
    public void setSource(Uri uri) {
        try {
            resetPlaySession(false);
            playStatus = PlaybackEvent.Status_Opening;
            mediaPlayer.setDataSource(mContext, uri);
        } catch (IllegalArgumentException e) {
            e.printStackTrace();
            playStatus = PlaybackEvent.Status_Error;
        } catch (IOException e) {
            e.printStackTrace();
            playStatus = PlaybackEvent.Status_Error;
        } catch (Exception e) {
            e.printStackTrace();
            playStatus = PlaybackEvent.Status_Error;
        }
    }

    @Override
    public void setSource(AssetFileDescriptor afd) {
        try {
            resetPlaySession(false);
            playStatus = PlaybackEvent.Status_Opening;
            mediaPlayer.setDataSource(afd);
        } catch (IllegalArgumentException e) {
            e.printStackTrace();
            playStatus = PlaybackEvent.Status_Error;
        } catch (IOException e) {
            e.printStackTrace();
            playStatus = PlaybackEvent.Status_Error;
        } catch (Exception e) {
            e.printStackTrace();
            playStatus = PlaybackEvent.Status_Error;
        }
    }

    @Override
    public void setSource(FileDescriptor fd) {
        try {
            resetPlaySession(false);
            playStatus = PlaybackEvent.Status_Opening;
            mediaPlayer.setDataSource(fd);
        } catch (IllegalArgumentException e) {
            e.printStackTrace();
            playStatus = PlaybackEvent.Status_Error;
        } catch (IOException e) {
            e.printStackTrace();
            playStatus = PlaybackEvent.Status_Error;
        } catch (Exception e) {
            e.printStackTrace();
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
        resetPlaySession(false);
        MLog.log(TAG, "re attached surface view successful");
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

    @Override
    public void play() {
        asyncStatusProcess(playStatus);
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
            MLog.log(TAG, "playPause pause directly playStatus=" + playStatus);
        } else {
            asyncStatusProcess(playStatus);
        }

    }

    @Override
    public void stop() {
        mediaPlayer.stop();
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
        mediaPlayer.seekTo((int) l);
        MLog.log(TAG,"setPlayTime = " + l);
    }

    @Override
    public void setVolume(int var1) {
        if (var1 < 0) return;
        volumeValue = var1;
        if (mediaPlayer != null)
            mediaPlayer.setVolume(volumeValue / 100, volumeValue / 100);
    }

    @Override
    public int getVolume() {
        return volumeValue;
    }

    @Override
    public Long getTime() {
        long l = 0;
        if (mediaPlayer != null)
        {
            if (mediaPlayer.isPlaying())
                l= Long.valueOf(mediaPlayer.getCurrentPosition());
            else
                l= Long.valueOf(0);
        }
        MLog.log(TAG,"getTime()=" + l);
        return l;
    }

    @Override
    public float getPosition() {
        if (mediaPlayer != null) {
            if (mediaPlayer.isPlaying())
                return mediaPlayer.getCurrentPosition();
        }
        return 0;
    }

    @Override
    public void setPosition(float f) {

    }

    @Override
    public long getLength() {
        if (mediaPlayer.isPlaying())
            return mediaPlayer.getDuration();
        return 0;
    }

    @Override
    public int getPlayerStatus() {
        if(mediaPlayer != null)
        {
            if (mediaPlayer.isPlaying()) {
                playStatus = PlaybackEvent.Status_Playing;
            }
        }
        else
        {
            playStatus = PlaybackEvent.Status_NothingIdle;
        }
        return playStatus;
    }

    @Override
    public void setRate(float v) {
        if (mediaPlayer != null)
            mediaPlayer.setPlaybackParams(mediaPlayer.getPlaybackParams().setSpeed(v));
    }

    @Override
    public void setWindowSize(int width, int height) {
    }

    @Override
    public void setAspectRatio(String aspect) {

    }

    @Override
    public void setScale(float scale) {
        mediaPlayer.setVideoScalingMode((int) scale);
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
        MediaPlayer.TrackInfo[] trackInfo = mediaPlayer.getTrackInfo();
        return trackInfo.length;
    }

    @Override
    public Map<Integer, String> getAudioTracks() {
        MediaPlayer.TrackInfo[] trackInfo = mediaPlayer.getTrackInfo();
        Map<Integer, String> mtd = new HashMap<Integer, String>();
        if (mediaPlayer == null) return mtd;
        if (!mediaPlayer.isPlaying()) return mtd;
        //for (MediaPlayer.TrackInfo td : trackInfo) {
        for (int i = 0; i < trackInfo.length - 1; i++) {
            mtd.put(i, trackInfo[i].toString());
        }
        return mtd;
    }

    @Override
    public int getAudioTrack() {
        if (mediaPlayer != null)
            return mediaPlayer.getSelectedTrack(MEDIA_TRACK_TYPE_AUDIO);
        return -1;
    }

    @Override
    public void setAudioTrack(int index) {
        if (mediaPlayer != null) {
            if (mediaPlayer.isPlaying())
                mediaPlayer.selectTrack(index);
        }
    }

    @Override
    public void setCallback(PlayerCallback callback) {
        playerEventCallBack = callback;
    }

    @Override
    public void onCompletion(MediaPlayer mediaPlayer) {
        MLog.log(TAG, "onCompletion  playing ended  ");
        playStatus = PlaybackEvent.Status_Ended;
    }

    @Override
    public void onSeekComplete(MediaPlayer mediaPlayer) {
        MLog.log(TAG, "onSeekComplete:" + mediaPlayer.toString());
        asyncStatusProcess(PlaybackEvent.Status_SEEKING);
    }

    @Override
    public boolean onError(MediaPlayer mediaPlayer, int i, int i1) {
        playStatus = PlaybackEvent.Status_Error;
        MLog.log(TAG, "onError:" + i + "," + i1);
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
                    mediaPlayer.getDuration());
    }

    @Override
    public void surfaceCreated(SurfaceHolder surfaceHolder) {
        if (mediaPlayer != null) {
            surfaceHolder.setFixedSize(mediaPlayer.getVideoWidth(), mediaPlayer.getVideoHeight());
        }
        MLog.log(TAG, "surfaceCreated," + surfaceHolder.toString());
    }

    @Override
    public void surfaceChanged(SurfaceHolder surfaceHolder, int i, int i1, int i2) {
        surfaceHolder.setFixedSize(mediaPlayer.getVideoWidth(), mediaPlayer.getVideoHeight());
        MLog.log(TAG, "surfaceChanged," + surfaceHolder.toString());
    }

    @Override
    public void surfaceDestroyed(SurfaceHolder surfaceHolder) {
        MLog.log(TAG, "surfaceDestroyed," + surfaceHolder.toString());
    }

    @Override
    public void onVideoSizeChanged(MediaPlayer mediaPlayer, int i, int i1) {
        //updateVideoSize(0);
    }

    @Override
    public void free() {
        if (mediaPlayer == null) return;
        try {
            mediaPlayer.stop();
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
            e.printStackTrace();
        }
    }

    public synchronized void resetPlaySession(boolean nf) {
        if (nf) {
            free();
        }
        try {
            if (mediaPlayer == null)
                mediaPlayer = new MediaPlayer();
            mediaPlayer.reset();//必须调用mediaPlayer.prepareAsync();
            mediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
            mediaPlayer.setOnCompletionListener(this);
            mediaPlayer.setOnSeekCompleteListener(this);
            mediaPlayer.setOnErrorListener(this);
            mediaPlayer.setOnBufferingUpdateListener(this);
            mediaPlayer.setDisplay(mSurfaceView.getHolder());
            mSurfaceView.getHolder().addCallback(this);
            playStatus = PlaybackEvent.Status_NothingIdle;
        } catch (Exception e) {
            e.printStackTrace();
        }
        //MLog.log(TAG,"mplayer status  has been set to Status_NothingIdle.");
    }

    private void asyncStatusProcess(int Status) {
        try {
            switch (Status) {
                case PlaybackEvent.Status_NothingIdle:
                    MLog.log(TAG, "asyncStatusProcess player is idle, nothing to do playStatus=" + Status);
                    break;
                case PlaybackEvent.Status_Stopped:
                case PlaybackEvent.Status_Opening:
                    StartPlayProgressThread();
                    MPlayer.playStatus = PlaybackEvent.Status_Buffering;
                    mediaPlayer.prepareAsync();
                    mediaPlayer.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
                        @Override
                        public void onPrepared(MediaPlayer mp) {
                            MLog.log(TAG, "asyncStatusProcess start to play async... playStatus="+Status);
                            MPlayer.playStatus = PlaybackEvent.Status_Playing;
                            mp.start();
                        }
                    });
                    break;
                case PlaybackEvent.Status_Buffering:
                case PlaybackEvent.Status_Playing:
                    MLog.log(TAG, "asyncStatusProcess is playing playStatus=" + Status);
                    break;

                case PlaybackEvent.Status_Paused:
                case PlaybackEvent.Status_SEEKING:
                    MLog.log(TAG, "asyncStatusProcess start to play directly playStatus=" + Status);
                    MPlayer.playStatus = PlaybackEvent.Status_Playing;
                    mediaPlayer.start();
                    break;
                case PlaybackEvent.Status_Ended:
                case PlaybackEvent.Status_Error:
                default:
                    MLog.log(TAG, "asyncStatusProcess do nothing, playStatus=" + Status);
                    break;
            }

        } catch (IllegalStateException e) {
            e.printStackTrace();
        }
    }

    private void StartPlayProgressThread()
    {   //创建进度状态线程
        if (progressThread == null)
        {
            progressThread = new ProgressThread();
            progressThread.start();
        }
        else
        {
          if(!progressThread.isKeepActive())
          {
              progressThread = new ProgressThread();
              progressThread.start();
          }
        }
    }

    private class ProgressThread extends Thread {
        private boolean keepActive = true;
        private int currentPosition = 0;
        private int lDuration = 0;

        @Override
        public void run() {
            super.run();
            while (keepActive)
            {
                if (mediaPlayer != null)
                {
                    if (mediaPlayer.isPlaying()) {
                        currentPosition = mediaPlayer.getCurrentPosition();
                        lDuration = mediaPlayer.getDuration();
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
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                switch (playStatus)
                {
                    case PlaybackEvent.Status_Stopped:
                    case PlaybackEvent.Status_Ended:
                    case PlaybackEvent.Status_Error:
                        finish();
                        break;
                }
            }
        }

        void finish() {
            keepActive = false;
        }
        boolean isKeepActive()
        {
            return keepActive;
        }
    }
}
