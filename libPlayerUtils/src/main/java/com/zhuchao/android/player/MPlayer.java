package com.zhuchao.android.player;

import static android.media.MediaPlayer.SEEK_CLOSEST;
import static android.media.MediaPlayer.TrackInfo.MEDIA_TRACK_TYPE_AUDIO;

import android.content.Context;
import android.content.res.AssetFileDescriptor;
import android.media.AudioAttributes;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Build;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.TextureView;

import androidx.annotation.NonNull;

import com.zhuchao.android.callbackevent.PlaybackEvent;
import com.zhuchao.android.callbackevent.PlayerCallback;
import com.zhuchao.android.fileutils.MMLog;

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

    private final String TAG = "MPlayer>>>>";
    private MediaPlayer mediaPlayer = null;
    private ProgressThread progressThread = null;
    //private static int playStatus = PlaybackEvent.Status_NothingIdle;
    //private static long lDuration = 0;
    //private static long lPosition = 0;
    //private static boolean surfaceCalled = false;

    public MPlayer(Context context, PlayerCallback callback) {
        super(context, callback);
        //playStatus = PlaybackEvent.Status_NothingIdle;
        //playerStatusInfo.setEventType(PlaybackEvent.Status_NothingIdle);
        freeSurfaceView();
        mediaPlayer = new MediaPlayer();
    }

    @Override
    public String getTAG() {
        return TAG;
    }

    @Override
    public void setSource(String filePath) {
        try {
            PreparePlayerComponent();
            playerStatusInfo.setEventType(PlaybackEvent.Status_Opening);
            CallbackProgress(0);
            mediaPlayer.setDataSource(filePath);
        } catch (IllegalArgumentException e) {
            MMLog.e(TAG, "setSource() " + e.toString() + " filePath = " + filePath);
            playerStatusInfo.setEventType(PlaybackEvent.Status_Error);
        } catch (IOException e) {
            MMLog.e(TAG, "setSource() " + e.toString());
            playerStatusInfo.setEventType(PlaybackEvent.Status_Error);
        } catch (Exception e) {
            MMLog.e(TAG, "setSource() " + e.toString());
            playerStatusInfo.setEventType(PlaybackEvent.Status_Error);
        }
    }

    @Override
    public void setSource(Uri uri) {
        try {
            PreparePlayerComponent();
            playerStatusInfo.setEventType(PlaybackEvent.Status_Opening);
            CallbackProgress(0);
            mediaPlayer.setDataSource(mContext, uri);
        } catch (IllegalArgumentException e) {
            MMLog.e(TAG, "setSource() " + e.toString());
            playerStatusInfo.setEventType(PlaybackEvent.Status_Error);
        } catch (IOException e) {
            MMLog.e(TAG, "setSource() " + e.toString());
            playerStatusInfo.setEventType(PlaybackEvent.Status_Error);
        } catch (Exception e) {
            MMLog.e(TAG, "setSource() " + e.toString());
            playerStatusInfo.setEventType(PlaybackEvent.Status_Error);
        }
    }

    @Override
    public void setSource(AssetFileDescriptor afd) {
        try {
            PreparePlayerComponent();
            playerStatusInfo.setEventType(PlaybackEvent.Status_Opening);
            CallbackProgress(0);
            mediaPlayer.setDataSource(afd);
        } catch (IllegalArgumentException e) {
            MMLog.e(TAG, "setSource() " + e.toString());
            playerStatusInfo.setEventType(PlaybackEvent.Status_Error);
        } catch (IOException e) {
            MMLog.e(TAG, "setSource() " + e.toString());
            playerStatusInfo.setEventType(PlaybackEvent.Status_Error);
        } catch (Exception e) {
            MMLog.e(TAG, "setSource() " + e.toString());
            playerStatusInfo.setEventType(PlaybackEvent.Status_Error);
        }
    }

    @Override
    public void setSource(FileDescriptor fd) {
        try {
            PreparePlayerComponent();
            playerStatusInfo.setEventType(PlaybackEvent.Status_Opening);
            CallbackProgress(0);
            mediaPlayer.setDataSource(fd);
        } catch (IllegalArgumentException e) {
            MMLog.e(TAG, "setSource() " + e.toString());
            playerStatusInfo.setEventType(PlaybackEvent.Status_Error);
        } catch (IOException e) {
            MMLog.e(TAG, "setSource() " + e.toString());
            playerStatusInfo.setEventType(PlaybackEvent.Status_Error);
        } catch (Exception e) {
            MMLog.e(TAG, "setSource() " + e.toString());
            playerStatusInfo.setEventType(PlaybackEvent.Status_Error);
        }

    }

    @Override
    public void setSurfaceView(@NonNull SurfaceView surfaceView) {
        mSurfaceView = surfaceView;
        mTextureView = null;
        if (mediaPlayer == null)
            mediaPlayer = new MediaPlayer();
        if (mSurfaceView.getHolder() == null) {
            MMLog.log(TAG, "setSurfaceView() mSurfaceView / Holder = null");
            return;
        }
        try {
            mSurfaceView.getHolder().addCallback(this);
            MMLog.d(TAG, "setSurfaceView().addCallback to  " + surfaceView.toString());
        } catch (Exception e) {
            //e.printStackTrace();
            MMLog.e(TAG, "setSurfaceView().addCallback " + e.toString());
        }

        if(mediaPlayer != null)
        {
            try {
                if(!mSurfaceView.getHolder().isCreating())
                {
                    if (mSurfaceView.getHolder().getSurface().isValid()) {
                        mediaPlayer.setDisplay(mSurfaceView.getHolder());
                        MMLog.d(TAG, "setSurfaceView().setDisplay to  " + surfaceView.toString());
                    }
                    else
                        MMLog.d(TAG, "setSurfaceView().setDisplay failed ,is invalid");
                }
                else
                    MMLog.d(TAG, "setSurfaceView().getHolder is not created ,is invalid");
            } catch (Exception e) {
                //e.printStackTrace();
                MMLog.d(TAG, "setSurfaceView().setDisplay " + e.toString());
            }
        }
        else
            MMLog.d(TAG, "setSurfaceView().mediaPlayer is null");
    }

    @Override
    public void setTextureView(TextureView textureView) {
        mSurfaceView = null;
        mTextureView = textureView;
    }

    @Override//播放中变换
    public void reAttachSurfaceView(@NonNull SurfaceView surfaceView) {
        if(mediaPlayer == null) mediaPlayer = new MediaPlayer();
        try {
            surfaceView.getHolder().addCallback(this);
        } catch (Exception e) {
            MMLog.e(TAG, "reAttachSurfaceView().addCallback " + e.toString());
        }

        try {
            mediaPlayer.setDisplay(surfaceView.getHolder());
            MMLog.d(TAG, "reAttachSurfaceView() to  " + surfaceView.toString());
            mSurfaceView = surfaceView;
            mTextureView = null;
        } catch (Exception e) {
            MMLog.e(TAG, "reAttachSurfaceView() setDisplay " + e.toString());
        }
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
        if (mediaPlayer == null) {
            MMLog.log(TAG, "mediaPlayer == null");
            return;
        }
        if (mSurfaceView == null) {
            MMLog.log(TAG, "mSurfaceView == null");
            return;
        }
        try {
            AsyncPlayProcess(playerStatusInfo.getEventType());
        } catch (Exception e) {
            MMLog.e(TAG, "play() " + "playStatus = " + playerStatusInfo.getEventType() + " " + e.getMessage());
        }
    }

    @Override
    public void pause() {
        if (mediaPlayer == null) return;
        mediaPlayer.pause();
        playerStatusInfo.setEventType(PlaybackEvent.Status_Paused);
        //playStatus = PlaybackEvent.Status_Paused;
    }

    @Override
    public void playPause() {
        if (mediaPlayer == null) return;
        if (playerStatusInfo.getEventType() == PlaybackEvent.Status_Playing) {
            pause();
            //MMLog.log(TAG, "playPause.pause playStatus = " + playStatus);
        } else {
            AsyncPlayProcess(playerStatusInfo.getEventType());
        }
    }

    @Override
    public void stop() {
        if (mediaPlayer == null) return;
        if (playerStatusInfo.getEventType() == PlaybackEvent.Status_Stopped) return;
        try {
            mediaPlayer.stop();
        } catch (IllegalStateException e) {
            MMLog.e(TAG, "stop() " + e.toString());
        }
        playerStatusInfo.setEventType(PlaybackEvent.Status_Stopped);
    }

    @Override
    public void resume() {

    }

    @Override
    public Boolean isPlaying() {
        if (mediaPlayer != null) {
            if(playerStatusInfo.getEventType() == PlaybackEvent.Status_Playing)
                return true;
            else
                return mediaPlayer.isPlaying();
        } else {
            return false;
        }
    }

    @Override
    public void setPlayTime(long l) {
        if (mediaPlayer == null) return;
        try {
            MMLog.log(TAG, "seek position to = " + l);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                mediaPlayer.seekTo((int) l,SEEK_CLOSEST );
            }
            else
            {
                mediaPlayer.seekTo((int) l);
            }
        } catch (IllegalStateException e) {
            MMLog.e(TAG, "seek to " + e.toString());
        }
    }

    @Override
    public void setVolume(int var1) {
        if (var1 < 0) return;
        DefaultVolumeValue = var1;
        try {
            if ((mediaPlayer != null) && (mediaPlayer.isPlaying()))
                mediaPlayer.setVolume(playerStatusInfo.getVolume() / 100, playerStatusInfo.getVolume() / 100);
        } catch (Exception e) {
            MMLog.e(TAG, "setVolume() " + e.toString());
        }
    }

    @Override
    public int getVolume() {
        return playerStatusInfo.getVolume();
    }

    @Override
    public Long getTime() {
        try {
            if ((mediaPlayer != null) && (isPlaying()))
                playerStatusInfo.setTimeChanged(Long.valueOf(mediaPlayer.getCurrentPosition()));
        } catch (Exception e) {
            MMLog.e(TAG, "getTime() " + e.toString());
        }
        return playerStatusInfo.getTimeChanged();
    }

    @Override
    public float getPosition() {
        try {
            if ((mediaPlayer != null) && (mediaPlayer.isPlaying()))
                playerStatusInfo.setPositionChanged(mediaPlayer.getCurrentPosition());
        } catch (Exception e) {
            MMLog.e(TAG, "getPosition() " + e.toString());
        }
        return playerStatusInfo.getPositionChanged();
    }

    @Override
    public void setPosition(float f) {

    }

    @Override
    public long getLength() {
        return playerStatusInfo.getLengthChanged();
    }

    @Override
    public int getPlayerStatus() {
        //if ((mediaPlayer != null) && (mediaPlayer.isPlaying()))
        //    playStatus = PlaybackEvent.Status_Playing;
        //else if (mediaPlayer == null)
        //    playStatus = PlaybackEvent.Status_NothingIdle;
        return playerStatusInfo.getEventType();
    }

    @Override
    public void setRate(float v) {
        try {
            if ((mediaPlayer != null))//&& (mediaPlayer.isPlaying())
                mediaPlayer.setPlaybackParams(mediaPlayer.getPlaybackParams().setSpeed(v));
        } catch (Exception e) {
            MMLog.e(TAG, "setRate() " + e.toString() + " v = " + v);
        }
    }

    @Override
    public void setWindowSize(int width, int height) {
        if(mSurfaceView != null && playerStatusInfo.isSurfacePrepared())
        {
            mSurfaceView.getHolder().setFixedSize(width,height);
        }
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
        if (mediaPlayer != null) {
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

        for (int i = 0; i < trackInfo.length; i++) {
            //if (trackInfo[i].getTrackType() == MediaPlayer.TrackInfo.MEDIA_TRACK_TYPE_AUDIO)
            mtd.put(i, trackInfo[i].getLanguage());
            MMLog.d(TAG, "getAudioTracks() " + trackInfo[i].toString());
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
        if (mediaPlayer == null) return;
        try {
            MediaPlayer.TrackInfo[] trackInfo = mediaPlayer.getTrackInfo();
            if (index <= 0) {
                mediaPlayer.deselectTrack(getAudioTrack());
                return;
            }
            for (int i = 0; i < trackInfo.length; i++) {
                MediaPlayer.TrackInfo Info = trackInfo[i];
                //if (Info.getTrackType() == MediaPlayer.TrackInfo.MEDIA_TRACK_TYPE_AUDIO)
                if (index == i) {
                    mediaPlayer.selectTrack(i);
                    MMLog.d(TAG, "setAudioTrack() track info = " + Info.toString());
                    break;
                }
            }
        } catch (IllegalStateException e) {
            MMLog.e(TAG, "setAudioTrack() " + e.toString());
        }
    }

    @Override
    public void deselectTrack(int index) {
        if (mediaPlayer == null) return;
        try {
            MediaPlayer.TrackInfo[] trackInfo = mediaPlayer.getTrackInfo();
            for (int i = 0; i < trackInfo.length; i++) {
                MediaPlayer.TrackInfo Info = trackInfo[i];
                //if (Info.getTrackType() == MediaPlayer.TrackInfo.MEDIA_TRACK_TYPE_AUDIO)
                if (index == i) {
                    mediaPlayer.deselectTrack(i);
                    MMLog.d(TAG, "deselectTrack() track info = " + Info.toString());
                    break;
                }
            }
        } catch (IllegalStateException e) {
            MMLog.e(TAG, "setAudioTrack() " + e.toString());
        }
    }

    @Override
    public void pushTo(String filePath,boolean duplicated) {

    }

    @Override
    public void setCallback(PlayerCallback callback) {
        playerEventCallBack = callback;
    }

    @Override
    public boolean onError(MediaPlayer mediaPlayer, int i, int i1) {
        playerStatusInfo.setEventType(PlaybackEvent.Status_Error);
        MMLog.e(TAG, "onError:" + i + "," + i1 + " playStatus = " + PlaybackEvent.Status_Error);
        playerStatusInfo.setLastError(PlaybackEvent.Status_Error);
        //return true;//
        return false;//// to call OnCompletionListener.onCompletion()方法。/自身结束错误
    }

    @Override
    public void onCompletion(MediaPlayer mediaPlayer) {
        //playStatus = PlaybackEvent.Status_Ended;
        playerStatusInfo.setEventType(PlaybackEvent.Status_Ended);
        MMLog.log(TAG, "onCompletion playing ended  playStatus = " + PlaybackEvent.Status_Ended);
    }

    @Override
    public void onSeekComplete(MediaPlayer mediaPlayer) {
        MMLog.log(TAG, "onSeekComplete:" + mediaPlayer.toString());
        if (mediaPlayer == null) return;
        AsyncPlayProcess(PlaybackEvent.Status_SEEKING);
    }

    @Override
    public void onBufferingUpdate(MediaPlayer mediaPlayer, int i) {
        //MLog.log(TAG, "onBufferingUpdate:" + i);
        CallbackProgress(i);
    }

    @Override
    public void surfaceCreated(SurfaceHolder surfaceHolder) {
        MMLog.log(TAG, "surfaceCreated," + surfaceHolder.toString());
        playerStatusInfo.setSurfacePrepared(true);
        if (mediaPlayer == null)
            mediaPlayer = new MediaPlayer();
        mediaPlayer.setDisplay(surfaceHolder);
        //surfaceHolder.setSizeFromLayout();
        //surfaceHolder.setFixedSize(w,h);
    }

    @Override
    public void surfaceChanged(SurfaceHolder surfaceHolder, int i, int i1, int i2) {
        MMLog.log(TAG, "surfaceChanged, f = " + i + " w = "+i1 + " h = " +i2);
        playerStatusInfo.setSurfaceW(i1);
        playerStatusInfo.setSurfaceH(i2);
    }

    @Override
    public void surfaceDestroyed(SurfaceHolder surfaceHolder) {
        MMLog.log(TAG, "surfaceDestroyed," + surfaceHolder.toString());
        playerStatusInfo.setSurfacePrepared(false);
    }

    @Override
    public void onVideoSizeChanged(MediaPlayer mediaPlayer, int i, int i1) {
        MMLog.log(TAG, "onVideoSizeChanged, w = " + i + " h = "+i1);
        playerStatusInfo.setVideoW(i);
        playerStatusInfo.setVideoH(i1);
    }

    @Override
    public void free() {
        if (mediaPlayer == null) return;

        try {
            if(playerStatusInfo != null) {
                playerStatusInfo.setEventType(PlaybackEvent.Status_FreeDoNothing);
                playerStatusInfo.setLastError(0);
            }

            CallbackProgress(0);//通知doNothing

            if (progressThread != null)
                progressThread.finish();
            progressThread = null;
            if(mediaPlayer.isPlaying())
               mediaPlayer.stop();
            mediaPlayer.reset();
            mediaPlayer.setOnCompletionListener(null);
            mediaPlayer.setOnSeekCompleteListener(null);
            mediaPlayer.setOnErrorListener(null);
            mediaPlayer.setOnBufferingUpdateListener(null);
            mediaPlayer.release();
            mediaPlayer = null;

            if(playerStatusInfo != null)
                playerStatusInfo.setEventType(PlaybackEvent.Status_NothingIdle);

            freeSurfaceView();
            MMLog.d(TAG, "free() freed all component ");
        } catch (Exception e) {
            MMLog.e(TAG, "freed() " + e.toString());
        }

    }

    private void freeSurfaceView()
    {
        playerStatusInfo.setSurfacePrepared(false);
        playerStatusInfo.setSurfaceW(0);
        playerStatusInfo.setSurfaceW(0);
        if(mSurfaceView != null) {
            mSurfaceView.getHolder().removeCallback(this);
            mSurfaceView = null;
        }
    }
    ////////////////////////////////////////////////////////////////////////////////////////////////
    private synchronized void PreparePlayerComponent() {
        MMLog.d(TAG, "PreparePlayerComponent() ,playStatus = " + playerStatusInfo.getEventType());
        //if (playStatus == PlaybackEvent.Status_Error) {
        //    ResetPlayerComponent();
        //}
        try {
            if (mediaPlayer == null)
                mediaPlayer = new MediaPlayer();
            mediaPlayer.reset();//必须调用mediaPlayer.prepareAsync();
        } catch (Exception e) {
            MMLog.e(TAG, "PreparePlayerComponent create failed " + e.toString());
            mediaPlayer = null;
            playerStatusInfo.setEventType(PlaybackEvent.Status_Error);
            return;
        }
        playerStatusInfo.setSourcePrepared(false);
        //mediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
        mediaPlayer.setAudioAttributes(new AudioAttributes.Builder().setContentType(AudioAttributes.CONTENT_TYPE_MOVIE).build());
        mediaPlayer.setOnCompletionListener(this);
        mediaPlayer.setOnSeekCompleteListener(this);
        mediaPlayer.setOnErrorListener(this);
        mediaPlayer.setOnBufferingUpdateListener(this);
        playerStatusInfo.setEventType(PlaybackEvent.Status_NothingIdle);
        playerStatusInfo.setLastError(PlaybackEvent.Status_NothingIdle);
        StartPlayProgressThread(); //开启进度 pooling 线程
    }

    /*private synchronized void ResetPlayerComponent() {
        if (mediaPlayer == null) return;
        try {
            mediaPlayer.reset();
            mediaPlayer.setOnCompletionListener(null);
            mediaPlayer.setOnSeekCompleteListener(null);
            mediaPlayer.setOnErrorListener(null);
            mediaPlayer.setOnBufferingUpdateListener(null);
            mediaPlayer.release();
            mediaPlayer = null;
        } catch (Exception e) {
            MMLog.e(TAG, "ResetPlayerComponent() " + e.toString());
        }
    }*/

    //错误处理，播放状态异常内部自动复位
    private synchronized void AsyncPlayProcess(int Status) {
        MMLog.log(TAG, "AsyncPlayProcess() enter, playStatus = " + Status);
        try {
            switch (Status) {
                case PlaybackEvent.Status_NothingIdle:
                    MMLog.log(TAG, "AsyncPlayProcess() player is idle, nothing to do playStatus = " + Status);
                    break;
                case PlaybackEvent.Status_Stopped:
                case PlaybackEvent.Status_Opening:
                    playerStatusInfo.setEventType(PlaybackEvent.Status_Buffering);//开始准备
                    mediaPlayer.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
                        @Override
                        public void onPrepared(MediaPlayer mp) {
                            try {
                                playerStatusInfo.setLengthChanged(mediaPlayer.getDuration());
                                playerStatusInfo.setEventType(PlaybackEvent.Status_HasPrepared);
                                playerStatusInfo.setSourcePrepared(true);
                                CallbackProgress(0);//给前端seek to 的机会
                            } catch (Exception e) {
                                MMLog.e(TAG, "AsyncPlayProcess().getDuration() " + "playStatus = " + playerStatusInfo.getEventType() + " " + e.toString());
                            }
                            if(mp.isPlaying() == false) {
                                mp.start();
                                playerStatusInfo.setEventType(PlaybackEvent.Status_Playing);
                                MMLog.log(TAG, "AsyncPlayProcess() start playing... playStatus = " + playerStatusInfo.getEventType());
                            }
                        }
                    });
                    try {
                        mediaPlayer.prepareAsync();
                    } catch (IllegalStateException e) {//不做任何处理交给onError()处理
                        MMLog.e(TAG, "AsyncPlayProcess().prepareAsync() " + "playStatus = " + playerStatusInfo.getEventType() + " " + e.toString());
                        if(playerStatusInfo.getEventType() == PlaybackEvent.Status_Buffering)//没有激活onError()
                            playerStatusInfo.setEventType(PlaybackEvent.Status_Error);//激活错误处理
                        else
                            ;//onError() 复位后进入end模式跳到下一曲
                    } catch (Exception e) {//不做任何处理交给onError()处理
                        MMLog.e(TAG, "AsyncPlayProcess().prepareAsync() " + "playStatus = " + playerStatusInfo.getEventType() + " " + e.toString());
                        if(playerStatusInfo.getEventType() == PlaybackEvent.Status_Buffering)//没有激活onError()
                        {
                            //free();
                            playerStatusInfo.setEventType(PlaybackEvent.Status_Error);//激活错误处理
                        }
                        else
                            ;//onError() 复位后进入end模式跳到下一曲
                    }
                    break;
                case PlaybackEvent.Status_Buffering:
                case PlaybackEvent.Status_Playing:
                    MMLog.log(TAG, "AsyncPlayProcess is playing playStatus = " + Status);
                    break;
                case PlaybackEvent.Status_Paused:
                case PlaybackEvent.Status_SEEKING:
                    MMLog.log(TAG, "AsyncPlayProcess start to play directly playStatus = " + Status);
                    if(playerStatusInfo.isSourcePrepared() == false)
                    {
                        MMLog.log(TAG, "source is not prepared can not start to play = " + Status);
                        break;
                    }
                    mediaPlayer.start();
                    playerStatusInfo.setEventType(PlaybackEvent.Status_Playing);
                    break;
                case PlaybackEvent.Status_Ended:
                case PlaybackEvent.Status_Error:
                default:
                    MMLog.log(TAG, "AsyncPlayProcess do nothing end/error, playStatus = " + Status);
                    break;
            }
        } catch (IllegalStateException e) {
            MMLog.e(TAG, "AsyncPlayProcess() " + "playStatus = " + playerStatusInfo.getEventType() + " " + e.toString());
        } catch (Exception e) {
            MMLog.e(TAG, "AsyncPlayProcess() " + "playStatus = " + playerStatusInfo.getEventType() + " " + e.toString());
        }
    }

    private void CallbackProgress(int buffering) {
        switch (playerStatusInfo.getEventType()) {
            case PlaybackEvent.Status_Opening:
                playerStatusInfo.setLengthChanged(0);
                playerStatusInfo.setTimeChanged(0);
                playerStatusInfo.setPositionChanged(0);
                break;
            case PlaybackEvent.Status_Buffering:
            case PlaybackEvent.Status_HasPrepared:
            case PlaybackEvent.Status_Playing:
            case PlaybackEvent.Status_Paused:
                getTime();
                break;
            case PlaybackEvent.Status_Stopped:
            case PlaybackEvent.Status_Ended:
            case PlaybackEvent.Status_Error:
            default:
                break;
        }

        if (playerEventCallBack != null && playerStatusInfo != null)
            playerEventCallBack.onEventPlayerStatus(playerStatusInfo);
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
        private boolean isActive = true;
        @Override
        public void run() {
            super.run();
            while (isActive) {
                //内部状态不向外传递
                if (playerStatusInfo.getEventType() >= PlaybackEvent.Status_INTERNAL) continue;
                //缓冲状态已经在上面调用过了，不再常规调用
                if (playerStatusInfo.getEventType() == PlaybackEvent.Status_Buffering) continue;

                try {
                    CallbackProgress(0);
                } catch (IllegalStateException e) {
                    MMLog.e(TAG, "Progress polling " + e.toString() + " playStatus = " + playerStatusInfo.getEventType());
                } catch (Exception e) {
                    MMLog.e(TAG, "Progress polling " + e.toString() + " playStatus = " + playerStatusInfo.getEventType());
                }
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    MMLog.e(TAG, "Progress polling " + e.toString() + " playStatus = " + playerStatusInfo.getEventType());
                }
            }
        }

        void finish() {
            isActive = false;
        }

        boolean isActive() {
            return isActive;
        }
    }
}
