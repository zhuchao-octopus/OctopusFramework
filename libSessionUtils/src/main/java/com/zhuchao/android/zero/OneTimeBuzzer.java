package com.zhuchao.android.zero;

public class OneTimeBuzzer extends TonePlayer {
    protected double duration = 5.0;

    public OneTimeBuzzer(double duration) {
        this.duration = duration;
    }

    public OneTimeBuzzer() {
    }

    public double getDuration() {
        return this.duration;
    }

    public void setDuration(double duration) {
        this.duration = duration;
    }

    protected void asyncPlayTrack() {
        this.playerWorker = new Thread(new Runnable() {
            public void run() {
                OneTimeBuzzer.this.playTone(OneTimeBuzzer.this.duration);
            }
        });
        this.playerWorker.start();
    }
}