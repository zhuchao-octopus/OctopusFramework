package com.zhuchao.android.player;

import android.media.AudioFormat;
import android.media.AudioManager;
import android.media.AudioTrack;

import java.io.FileInputStream;
import java.io.IOException;

public class AudioPlayer {
    private AudioTrack mAudioTrack;
    private int mSampleRate = 44100;
    private int mChannelConfig = AudioFormat.CHANNEL_OUT_MONO;
    private int mAudioFormat = AudioFormat.ENCODING_PCM_16BIT;

    public AudioPlayer() {
        int bufferSize = AudioTrack.getMinBufferSize(mSampleRate, mChannelConfig, mAudioFormat);
        mAudioTrack = new AudioTrack(AudioManager.STREAM_MUSIC, mSampleRate, mChannelConfig, mAudioFormat, bufferSize, AudioTrack.MODE_STREAM);
    }

    public AudioPlayer(int sampleRate, int channelConfig, int audioFormat) {
        int bufferSize = AudioTrack.getMinBufferSize(sampleRate, channelConfig, audioFormat);
        mAudioTrack = new AudioTrack(AudioManager.STREAM_MUSIC, sampleRate, channelConfig, audioFormat, bufferSize, AudioTrack.MODE_STREAM);
    }

    public void play(byte[] pcmData) {
        mAudioTrack.play();
        mAudioTrack.write(pcmData, 0, pcmData.length);
    }

    public void stop() {
        mAudioTrack.stop();
        mAudioTrack.release();
    }

    public byte[] loadPCMFile(String fileName) {
        try {
            FileInputStream fis = new FileInputStream(fileName);
            byte[] buffer = new byte[fis.available()];
            fis.read(buffer);
            fis.close();
            return buffer;
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }
}
