package com.zhuchao.android.zero;

import android.annotation.SuppressLint;
import android.content.Context;
import android.media.ToneGenerator;

import com.zhuchao.android.fbase.MMLog;

public class BuzzerManager {
    private static final String TAG = "BuzzerManager";
    @SuppressLint("StaticFieldLeak")
    private static BuzzerManager instance;
    private Thread thread;
    private int sleepTime;
    private Context mContent;
    private final ToneGenerator toneGenerator;
    boolean isRun = false;

    private final OneTimeBuzzer oneTimeBuzzer;

    private BuzzerManager(Context context) {
        this.mContent = context;
        this.toneGenerator = new ToneGenerator(5, 100);
        this.oneTimeBuzzer = new OneTimeBuzzer();
    }

    public static BuzzerManager getInstance(Context context) {
        if (instance == null) {
            instance = new BuzzerManager(context);
        }
        return instance;
    }

    public void playBeep(int sleep) {
        MMLog.d(TAG, "playBeep: oneTimeBuzzer.isPlaying=" + this.oneTimeBuzzer.isPlaying + " sleep=" + sleep);
        if (sleep == 0) {
            this.sleepTime = 0;
            if (this.thread != null && this.thread.isAlive()) {
                this.isRun = false;
                this.thread = null;
            }
        } else if (this.thread != null && this.thread.isAlive()) {
            this.sleepTime = sleep;
            if (this.oneTimeBuzzer != null && this.oneTimeBuzzer.isPlaying) {
                this.oneTimeBuzzer.stop();
            }
        } else {
            this.isRun = true;
            this.sleepTime = sleep;
            if (this.oneTimeBuzzer != null && this.oneTimeBuzzer.isPlaying) {
                this.oneTimeBuzzer.stop();
            }

            this.thread = new Thread(new Runnable() {
                public void run() {
                    while (BuzzerManager.this.isRun) {
                        BuzzerManager.this.toneGenerator.startTone(25, 5000);

                        try {
                            Thread.sleep((long) BuzzerManager.this.sleepTime);
                        } catch (InterruptedException ignored) {
                        }
                    }
                }
            });
            this.thread.start();
        }
    }

    public void playLongBeep(int time, int hz) {
        this.playBeep(0);
        this.oneTimeBuzzer.duration = (double) time;
        this.oneTimeBuzzer.volume = 70;
        this.oneTimeBuzzer.toneFreqInHz = (double) hz;
        this.oneTimeBuzzer.play();
    }

    public void stop() {
        if (this.oneTimeBuzzer != null) {
            this.oneTimeBuzzer.stop();
        }

        if (this.toneGenerator != null) {
            playBeep(0);
            //this.toneGenerator.stopTone();
        }
    }

    public void release() {
        if (this.toneGenerator != null) {
            this.toneGenerator.release();
        }
    }

}
