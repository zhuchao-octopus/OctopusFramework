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
import android.widget.RelativeLayout;

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

    private String TAG = "MPlayer>>>";
    private MediaPlayer mediaPlayer = null;
    //private MediaPlayer.OnCompletionListener onCompletionListener;
    //private MediaPlayer.OnErrorListener onErrorListener;
    //private MediaPlayer.OnPreparedListener onPreparedListener;
    private ProgressThread progressThread = null;
    private int playStatus = PlaybackEvent.Status_NothingIdle;

    public MPlayer(Context context, PlayerCallback callback) {
        super(context, callback);
        playStatus = PlaybackEvent.Status_NothingIdle;
        //MLog.logTAG,"MPlayer=========>");
    }

    @Override
    public String getTAG() {
        return TAG;
    }

    @Override
    public void setSource(String filePath) {
        try {
            preparePlaySession(false);
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
            preparePlaySession(false);
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
            preparePlaySession(false);
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
            preparePlaySession(false);
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
        asyncPreparePlay();
    }

    @Override
    public void pause() {
        mediaPlayer.pause();
        playStatus = PlaybackEvent.Status_Paused;
    }

    @Override
    public void stop() {
        if (playStatus != PlaybackEvent.Status_Stopped)
            mediaPlayer.stop();
        playStatus = PlaybackEvent.Status_Stopped;
    }

    @Override
    public void playPause() {
        if (mediaPlayer.isPlaying()) {
            pause();
        } else {
            play();
        }
    }

    @Override
    public void resume() {
        asyncPreparePlay();
    }

    @Override
    public Boolean isPlaying() {
        return mediaPlayer.isPlaying();
    }

    @Override
    public void setPlayTime(long l) {
        if (mediaPlayer == null) return;
        mediaPlayer.seekTo((int) l);
    }

    @Override
    public void setVolume(int var1) {
        volumeValue = var1;
        mediaPlayer.setVolume(volumeValue/100, volumeValue/100);
    }

    @Override
    public int getVolume() {
        return volumeValue;
    }

    @Override
    public Long getTime() {
        if (mediaPlayer.isPlaying())
            return Long.valueOf(mediaPlayer.getCurrentPosition());
        return Long.valueOf(0);
    }

    @Override
    public float getPosition() {
        if (mediaPlayer.isPlaying())
        return mediaPlayer.getCurrentPosition();
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
    public int getPlayerState() {
        if (mediaPlayer.isPlaying())
            playStatus = PlaybackEvent.Status_Playing;
        return playStatus;
    }

    @Override
    public void setRate(float v) {

    }

    @Override
    public void setWindowSize(int width, int height) {
        if (mediaPlayer != null) {
            if(mSurfaceView != null) {
                //mediaPlayer.setDisplay(mSurfaceView.getHolder());
                //mSurfaceView.getHolder().setFixedSize(mediaPlayer.getVideoWidth(), mediaPlayer.getVideoHeight());
            }
        }
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
        return mediaPlayer.getSelectedTrack(MEDIA_TRACK_TYPE_AUDIO);
    }

    @Override
    public void setAudioTrack(int index) {
        if (mediaPlayer.isPlaying())
            mediaPlayer.selectTrack(index);
    }

    @Override
    public void setCallback(PlayerCallback callback) {
        playerEventCallBack = callback;
    }

    @Override
    public void onCompletion(MediaPlayer mediaPlayer) {
        MLog.log(TAG, "onCompletion:" + mediaPlayer.toString());
        playStatus = PlaybackEvent.Status_Ended;
    }

    @Override
    public void onSeekComplete(MediaPlayer mediaPlayer) {
        MLog.log(TAG, "onSeekComplete:" + mediaPlayer.toString());
        mediaPlayer.start();
    }

    @Override
    public boolean onError(MediaPlayer mediaPlayer, int i, int i1) {
        playStatus = PlaybackEvent.Status_Error;
        MLog.log(TAG, "onError:" + i + "," + i1);
        return false;//return false to call OnCompletionListener.onCompletion()方法。
    }

    @Override
    public void onBufferingUpdate(MediaPlayer mediaPlayer, int i) {
        MLog.log(TAG, "onBufferingUpdate:" + i);
    }

    @Override
    public void surfaceCreated(SurfaceHolder surfaceHolder) {
        if (mediaPlayer != null) {
            mediaPlayer.setDisplay(surfaceHolder);
            surfaceHolder.setFixedSize(mediaPlayer.getVideoWidth(), mediaPlayer.getVideoHeight());
        }
        MLog.log(TAG, "surfaceCreated=========>" + surfaceHolder.toString());
    }

    @Override
    public void surfaceChanged(SurfaceHolder surfaceHolder, int i, int i1, int i2) {
        surfaceHolder.setFixedSize(mediaPlayer.getVideoWidth(), mediaPlayer.getVideoHeight());
        MLog.log(TAG, "surfaceChanged=========>" + surfaceHolder.toString());
    }

    @Override
    public void surfaceDestroyed(SurfaceHolder surfaceHolder) {
        MLog.log(TAG, "surfaceDestroyed=======>" + surfaceHolder.toString());
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
            //mediaPlayer.setOnPreparedListener(null);
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

    private void preparePlaySession(boolean nf) {
        if (mediaPlayer != null)
        {
            free();
        }
        else
        {
            playStatus = PlaybackEvent.Status_NothingIdle;
            //MLog.log(TAG,"start to prepare PlaySession.");
        }
        mediaPlayer = new MediaPlayer();
        mediaPlayer.reset();//必须调用mediaPlayer.prepareAsync();
        mediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
        mediaPlayer.setOnCompletionListener(this);
        // mediaPlayer.setOnPreparedListener(this);
        mediaPlayer.setOnSeekCompleteListener(this);
        mediaPlayer.setOnErrorListener(this);
        mediaPlayer.setOnBufferingUpdateListener(this);
        mediaPlayer.setDisplay(mSurfaceView.getHolder());
        mSurfaceView.getHolder().addCallback(this);
        //创建进度状态线程
        if(progressThread == null)
        {
            progressThread = new ProgressThread();
            progressThread.start();
        }
        MLog.log(TAG,"mplayer status  has been set to Status_NothingIdle.");
    }

    private void asyncPreparePlay() {
        switch (playStatus) {
            case PlaybackEvent.Status_Paused:
            {
                MLog.log(TAG,"asyncPreparePlay start() directly playStatus="+playStatus);
                mediaPlayer.start();
                playStatus = PlaybackEvent.Status_Playing;
                return;
            }
            case PlaybackEvent.Status_Buffering:
            case PlaybackEvent.Status_Playing:
            case PlaybackEvent.Status_Error:
            case PlaybackEvent.Status_Ended:
                return;
        }
        MLog.log(TAG,"asyncPreparePlay call prepareAsync() to start playStatus="+playStatus);
        mediaPlayer.prepareAsync();
        playStatus = PlaybackEvent.Status_Buffering;
        mediaPlayer.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
            @Override
            public void onPrepared(MediaPlayer mp) {
                MLog.log(TAG, "onPrepared, start to play... " + mp.toString());
                mp.start();
                playStatus = PlaybackEvent.Status_Playing;
            }
        });
    }

    class ProgressThread extends Thread {
       private boolean keepActive = true;
       private int currentPosition = 0;
       private int lDuration = 0;
        @Override
        public void run() {
            super.run();
            while (keepActive) {
                try {
                    if(playStatus <= PlaybackEvent.Status_Buffering )
                        Thread.sleep(1000);
                    else
                        Thread.sleep(500);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                if(mediaPlayer == null) {
                    finish();
                    return;
                }
                if (mediaPlayer.isPlaying()) {
                    currentPosition = mediaPlayer.getCurrentPosition();
                    lDuration = mediaPlayer.getDuration();
                }

                if (playerEventCallBack != null)
                    playerEventCallBack.OnEventCallBack(
                            playStatus,
                            0,
                            currentPosition,
                            currentPosition,
                            0,
                            0,
                            0,
                            0,
                            lDuration);
            }
        }

        void finish() {
            keepActive = false;
        }
    }

    private void updateVideoSize(int Orientation) {
        if (mediaPlayer == null) return;
        if (mSurfaceView == null) return;
        if (mSurfaceView.getWidth() <= 10) return;
        if (mSurfaceView.getHeight() <= 10) return;
        int videoWidth = mediaPlayer.getVideoWidth();
        int videoHeight = mediaPlayer.getVideoHeight();
        float max;
        if (Orientation == 0) {
            //竖屏模式下按视频宽度计算放大倍数值
            max = Math.max((float) videoWidth / (float) mSurfaceView.getWidth(), (float) videoHeight / (float) mSurfaceView.getHeight());
        } else {
            //横屏模式下按视频高度计算放大倍数值
            max = Math.max(((float) videoWidth / (float) mSurfaceView.getHeight()), (float) videoHeight / (float) mSurfaceView.getWidth());
        }
        //视频宽高分别/最大倍数值 计算出放大后的视频尺寸
        videoWidth = (int) Math.ceil((float) videoWidth / max);
        videoHeight = (int) Math.ceil((float) videoHeight / max);
        //无法直接设置视频尺寸，将计算出的视频尺寸设置到surfaceView 让视频自动填充。
        mSurfaceView.setLayoutParams(new RelativeLayout.LayoutParams(videoWidth, videoHeight));
    }

}
