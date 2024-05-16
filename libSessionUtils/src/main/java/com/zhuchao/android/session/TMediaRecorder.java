package com.zhuchao.android.session;

import android.media.MediaRecorder;

import com.zhuchao.android.fbase.MMLog;

import java.io.IOException;

public class TMediaRecorder {
    private final String TAG = "TMediaRecorder";
    private final MediaRecorder mMediaRecorder;
    private String mOutputFilePath;

    public TMediaRecorder() {
        mMediaRecorder = new MediaRecorder();
        mMediaRecorder.setVideoSource(MediaRecorder.VideoSource.CAMERA);
        mMediaRecorder.setAudioSource(MediaRecorder.AudioSource.MIC);            // 不设置的话没有声音

        // 设置输出文件和编码格式
        mMediaRecorder.setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP);    // MPEG_4 , THREE_GPP 都支持
        mMediaRecorder.setVideoEncoder(MediaRecorder.VideoEncoder.H264);
        mMediaRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB);
        mMediaRecorder.setOutputFile(mOutputFilePath);
        mMediaRecorder.setMaxDuration(60 * 1000);    // 录制最大时长，单位是毫秒
        mMediaRecorder.setMaxFileSize(5 * 1024 * 1024);    // 录制文件最大长度，单位是byte
        ///mMediaRecorder.setVideoSize(size.width, size.height);// 分辨率只能设置成系统支持的分辨率，随便设置会出错
        ///int[] fpsRange = fpsRanges.get(curFpsRange);
        ///mMediaRecorder.setVideoFrameRate(fpsRange[0] / 1000);    // 每秒帧数只能设置成系统支持的，随便设置会出错
    }

    public void startRecord() {
        try {
            mMediaRecorder.prepare();
        } catch (IOException e) {
            ///e.printStackTrace();
            MMLog.e(TAG, String.valueOf(e));
        }
        mMediaRecorder.start();
    }

}
