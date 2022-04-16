package com.zhuchao.android.libfileutils;

import android.os.Bundle;
import android.text.TextUtils;

import com.zhuchao.android.callbackevent.CallbackFunction;
import com.zhuchao.android.callbackevent.CallBackHandler;

public class TTask extends Thread {
    private final String TAG = "TTask";
    protected String tTag = null;
    protected CallbackFunction callbackFunction = null;
    protected CallBackHandler callBackHandler = null;
    protected Bundle Properties = null;
    protected boolean keepActive = false;

    public TTask(String tag, CallbackFunction callbackFunction) {
        this.tTag = tag;
        this.callbackFunction = callbackFunction;
        this.Properties = new Bundle();
    }

    public TTask(String tag, CallbackFunction callbackFunction, CallBackHandler CallBackHandler) {
        this.tTag = tag;
        this.callbackFunction = callbackFunction;
        this.callBackHandler = CallBackHandler;
        this.Properties = new Bundle();
    }

    public TTask call(CallbackFunction callFunction) {
        callbackFunction = callFunction;
        return this;
    }

    public TTask callbackHandler(CallBackHandler CallBackHandler) {
        this.callBackHandler = CallBackHandler;
        return this;
    }

    public CallBackHandler getCallBackHandler() {
        return callBackHandler;
    }

    public String gettTag() {
        return tTag;
    }

    public long getTaskID() {
        return this.getId();
    }

    public void settTag(String tTag) {
        this.tTag = tTag;
    }

    public Bundle getProperties() {
        return Properties;
    }

    public void free() {
        keepActive = false;
        //Properties.clear();
    }

    @Override
    public synchronized void start() {
        if(this.isAlive() || keepActive) return;
        keepActive = true;
        super.start();
    }

    @Override
    public void run() {
        super.run();
        if (callbackFunction == null) {
            MMLog.log(TAG, "call TTask function null,nothing to do break TTask,tTag = " + tTag);
            free();
            return;
        }

        if (TextUtils.isEmpty(this.tTag)) {
            free();
            MMLog.log(TAG, "invalid PTask name/tag finished,tag = " + tTag);
            return;
        }
        try {
            MMLog.log(TAG, "call TTask function tTag = " + tTag);
            callbackFunction.call(this.tTag);//asynchronous
        } catch (Exception e) {
            e.printStackTrace();
        }

        while (keepActive) {
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }
}