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
    private long lDuration = 0;
    //private MediaPlayer.OnCompletionListener onCompletionListener;
    //private MediaPlayer.OnErrorListener onErrorListener;
    //private MediaPlayer.OnPreparedListener onPreparedListener;
    private ProgressThread progressThread = null;
    private int playStatus = Status_NothingIdle;

    public MPlayer(Context context, PlayerCallback callback) {
        super(context, callback);
        playStatus = Status_NothingIdle;
        //MLog.logTAG,"MPlayer=========>");
    }

    @Override
    public String getTAG() {
        return TAG;
    }

    @Override
    public void setSource(String filePath) {
        try {
            playStatus = Status_Opening;
            createPlayer(false);
            mediaPlayer.setDataSource(filePath);
        } catch (IllegalArgumentException e) {
            e.printStackTrace();
            playStatus = Status_Error;
        } catch (IOException e) {
            e.printStackTrace();
            playStatus = Status_Error;
        } catch (Exception e) {
            e.printStackTrace();
            playStatus = Status_Error;
        }
    }

    @Override
    public void setSource(Uri uri) {
        try {
            playStatus = Status_Opening;
            createPlayer(false);
            mediaPlayer.setDataSource(mContext, uri);
        } catch (IllegalArgumentException e) {
            e.printStackTrace();
            playStatus = Status_Error;
        } catch (IOException e) {
            e.printStackTrace();
            playStatus = Status_Error;
        } catch (Exception e) {
            e.printStackTrace();
            playStatus = Status_Error;
        }
    }

    @Override
    public void setSource(AssetFileDescriptor afd) {
        try {
            playStatus = Status_Opening;
            createPlayer(false);
            mediaPlayer.setDataSource(afd);
        } catch (IllegalArgumentException e) {
            e.printStackTrace();
            playStatus = Status_Error;
        } catch (IOException e) {
            e.printStackTrace();
            playStatus = Status_Error;
        } catch (Exception e) {
            e.printStackTrace();
            playStatus = Status_Error;
        }
    }

    @Override
    public void setSource(FileDescriptor fd) {
        try {
            playStatus = Status_Opening;
            createPlayer(false);
            mediaPlayer.setDataSource(fd);
        } catch (IllegalArgumentException e) {
            e.printStackTrace();
            playStatus = Status_Error;
        } catch (IOException e) {
            e.printStackTrace();
            playStatus = Status_Error;
        } catch (Exception e) {
            e.printStackTrace();
            playStatus = Status_Error;
        }
    }

    @Override
    public void setSurfaceView(SurfaceView surfaceView) {
        if (surfaceView == null) return;
        if (surfaceView == mSurfaceView) return;
        mSurfaceView = surfaceView;
        mTextureView = null;
        createPlayer(false);
        if (mSurfaceView == null || mSurfaceView.getHolder() == null)
            return;
        try {
            mediaPlayer.setDisplay(mSurfaceView.getHolder());
            mSurfaceView.getHolder().addCallback(this);
        } catch (Exception e) {
            e.printStackTrace();
        }
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
        playStatus = Status_Paused;
    }

    @Override
    public void stop() {
        if (playStatus != Status_Stopped)
            mediaPlayer.stop();
        playStatus = Status_Stopped;
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
        return mediaPlayer.getCurrentPosition();
    }

    @Override
    public void setPosition(float f) {

    }

    @Override
    public long getLength() {
        if (mediaPlayer.isPlaying())
            lDuration = mediaPlayer.getDuration();
        return lDuration;
    }

    @Override
    public int getPlayerState() {
        if (mediaPlayer.isPlaying())
            playStatus = Status_Playing;
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
    public void free() {
        playStatus = Status_NothingIdle;
        if (mediaPlayer == null) return;

        try {
            mediaPlayer.stop();
            mediaPlayer.reset();
            mSurfaceView.getHolder().removeCallback(this);
            mSurfaceView = null;
            mTextureView = null;
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
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onCompletion(MediaPlayer mediaPlayer) {
        playCompletion();
        MLog.log(TAG, "onCompletion:" + mediaPlayer.toString());
    }

    @Override
    public void onSeekComplete(MediaPlayer mediaPlayer) {
        mediaPlayer.start();
        MLog.log(TAG, "onSeekComplete:" + mediaPlayer.toString());
    }

    @Override
    public boolean onError(MediaPlayer mediaPlayer, int i, int i1) {
        playStatus = Status_Error;
        MLog.log(TAG, "onError:" + i + "," + i1);
        return false;
    }

    @Override
    public void onBufferingUpdate(MediaPlayer mediaPlayer, int i) {
        if (playerEventCallBack != null)
            playerEventCallBack.OnEventCallBack(
                    playStatus,
                    0,
                    mediaPlayer.getCurrentPosition(),
                    0,
                    0,
                    0,
                    0,
                    i,
                    lDuration);
        //MLog.logTAG, "onBufferingUpdate:" + i);
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

    private void playCompletion() {
        if (progressThread != null)
            progressThread.finish();
        playStatus = Status_Ended;
        if (playerEventCallBack != null)
            playerEventCallBack.OnEventCallBack(
                    playStatus,
                    0,
                    mediaPlayer.getCurrentPosition(),
                    0,
                    0,
                    0,
                    0,
                    0,
                    lDuration);
    }

    private void createPlayer(boolean nf) {
        if (nf) free();
        if (mediaPlayer == null) {
            mediaPlayer = new MediaPlayer();
            mediaPlayer.reset();
            mediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
            mediaPlayer.setOnCompletionListener(this);
            // mediaPlayer.setOnPreparedListener(this);
            mediaPlayer.setOnSeekCompleteListener(this);
            mediaPlayer.setOnErrorListener(this);
            mediaPlayer.setOnBufferingUpdateListener(this);
            if (progressThread != null)
                progressThread.finish();
            progressThread = null;
        } else {
            mediaPlayer.reset();
        }
    }

    private void asyncPreparePlay() {
        switch (playStatus) {
            case Status_Paused:
            case Status_Ended: {
                mediaPlayer.start();
                playStatus = Status_Playing;
                return;
            }
            case Status_Buffering:
            case Status_Playing:
            case Status_Error:
                return;
        }

        mediaPlayer.prepareAsync();
        playStatus = Status_Buffering;
        mediaPlayer.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
            @Override
            public void onPrepared(MediaPlayer mp) {
                MLog.log(TAG, "onPrepared start to play..." + mediaPlayer.toString());
                mp.start();
                playStatus = Status_Playing;
                if (progressThread == null) {
                    progressThread = new ProgressThread();
                    progressThread.start();
                }
            }
        });
    }

    class ProgressThread extends Thread {
        boolean keepActive = true;

        @Override
        public void run() {
            super.run();
            while (keepActive) {
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                if (!mediaPlayer.isPlaying()) continue;
                int currentPosition = mediaPlayer.getCurrentPosition();
                lDuration = mediaPlayer.getDuration();
                if (currentPosition >= lDuration)
                    finish();
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

        public void pPause() {

        }
    }

    public void updateVideoSize(int Orientation) {
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
