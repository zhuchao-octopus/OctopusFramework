package com.zhuchao.android.zero;


import static android.media.AudioManager.STREAM_ALARM;

import android.media.AudioManager;
import android.media.AudioTrack;
import android.util.Log;

import com.zhuchao.android.fbase.MMLog;

import java.util.Arrays;

public abstract class TonePlayer {
    private static final String TAG = "TonePlayer";
    protected double toneFreqInHz = 440.0;
    protected final Object toneFreqInHzSyncObj = new Object();
    protected int volume = 100;
    protected AudioTrack audioTrack = null;
    protected int audTrackBufferSize = 0;
    protected boolean isPlaying = false;
    protected Thread playerWorker;
    protected double lastToneFreqInHz = 0.0;
    protected int lastNumSamplesCount = 0;
    protected double[] lastDoubleSamples = null;
    protected byte[] lastSoundBytes = null;

    protected TonePlayer() {
    }

    public TonePlayer(double toneFreqInHz) {
        this.toneFreqInHz = toneFreqInHz;
    }

    public void play() {
        if (!this.isPlaying) {
            this.isPlaying = true;
            this.asyncPlayTrack();
        }
    }

    public void stop() {
        ///Log.d("TAG", "stop: fadfadfs", new Exception());
        this.isPlaying = false;
        //if (this.audioTrack != null)
        {
            this.tryStopPlayer();
        }
    }

    public void setVolume(int volume) {
        this.volume = volume;
    }

    public int getVolume() {
        return this.volume;
    }

    public double getToneFreqInHz() {
        synchronized (this.toneFreqInHzSyncObj) {
            return this.toneFreqInHz;
        }
    }

    public void setToneFreqInHz(double toneFreqInHz) {
        synchronized (this.toneFreqInHzSyncObj) {
            this.toneFreqInHz = toneFreqInHz;
        }
    }

    protected abstract void asyncPlayTrack();

    protected void tryStopPlayer() {
        MMLog.d(TAG, "tryStopPlayer: 111");
        this.isPlaying = false;

        try {
            if (this.playerWorker != null) {
                this.playerWorker.interrupt();
                MMLog.d("TAG", "tryStopPlayer: 222");
            }

            if (this.audioTrack != null) {
                //this.audioTrack.stop();
                this.audioTrack.pause();
                this.audioTrack.flush();
                this.audioTrack.release();
                this.audioTrack = null;
                MMLog.d(TAG, "tryStopPlayer: 333");
            }

        } catch (IllegalStateException var2) {
            MMLog.e(TAG, String.valueOf(var2));
        }
    }

    protected void playTone(double seconds, boolean continuousFlag) {
        int sampleRate = 8000;
        double dnumSamples = seconds * (double) sampleRate;
        dnumSamples = Math.ceil(dnumSamples);
        int numSamples = (int) dnumSamples;
        double freqInHz = this.getToneFreqInHz();
        double[] sample;
        byte[] soundData;
        if (numSamples == this.lastNumSamplesCount) {
            if (freqInHz == this.lastToneFreqInHz) {
                this.playSound(sampleRate, this.lastSoundBytes);
                return;
            }

            sample = this.lastDoubleSamples;
            soundData = this.lastSoundBytes;
        } else {
            sample = this.lastDoubleSamples = new double[numSamples];
            soundData = this.lastSoundBytes = new byte[2 * numSamples];
            this.lastNumSamplesCount = numSamples;
        }

        this.lastToneFreqInHz = freqInHz;

        int idx;
        for (idx = 0; idx < numSamples; ++idx) {
            sample[idx] = Math.sin(freqInHz * 2.0 * Math.PI * (double) idx / (double) sampleRate);
        }

        idx = 0;
        int i = 0;
        int ramp = numSamples / (continuousFlag ? 200 : 20);

        double dVal;
        short val;
        for (i = i; i < numSamples - ramp; ++i) {
            dVal = sample[i];
            val = (short) ((int) (dVal * 32767.0));
            soundData[idx++] = (byte) (val & 255);
            soundData[idx++] = (byte) ((val & '\uff00') >>> 8);
        }

        for (i = i; i < numSamples; ++i) {
            dVal = sample[i];
            val = (short) ((int) (dVal * 32767.0 * (double) (numSamples - i) / (double) ramp));
            soundData[idx++] = (byte) (val & 255);
            soundData[idx++] = (byte) ((val & '\uff00') >>> 8);
        }

        this.playSound(sampleRate, soundData);
    }

    protected void playTone(double seconds) {
        this.playTone(seconds, false);
    }

    protected void playSound(int sampleRate, byte[] soundData) {
        MMLog.d(TAG, "playSound: sampleRate=" + sampleRate + "  size = " + Arrays.toString(soundData));
        try {
            int bufferSize = AudioTrack.getMinBufferSize(sampleRate, 4, 2);
            if (bufferSize != this.audTrackBufferSize || this.audioTrack == null) {
                this.audioTrack = new AudioTrack(AudioManager.STREAM_SYSTEM, sampleRate, 4, 2, bufferSize, 1);
                this.audTrackBufferSize = bufferSize;
            }

            float gain = (float) ((double) this.volume / 100.0);
            this.audioTrack.setStereoVolume(gain, gain);
            this.audioTrack.play();
            this.audioTrack.write(soundData, 0, soundData.length);
        } catch (Exception var5) {
            MMLog.e(TAG, String.valueOf(var5));
        }

    }
}
