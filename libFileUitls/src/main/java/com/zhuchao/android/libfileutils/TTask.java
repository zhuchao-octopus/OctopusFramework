package com.zhuchao.android.libfileutils;

import android.os.Bundle;

import com.zhuchao.android.callbackevent.InvokeFunction;
import com.zhuchao.android.callbackevent.TaskCallback;
import com.zhuchao.android.utils.MMLog;

public class TTask extends Thread {
    private final String TAG = "TTask";
    protected String tTag = null;
    protected InvokeFunction invokeFunction = null;
    protected TaskCallback taskCallback = null;
    protected Bundle Properties = null;
    protected boolean isActive = false;

    public TTask(String tag, InvokeFunction invokeFunction) {
        this.tTag = tag;
        this.invokeFunction = invokeFunction;
        this.Properties = new Bundle();
    }

    public TTask(String tag, InvokeFunction invokeFunction, TaskCallback TaskCallback) {
        this.tTag = tag;
        this.invokeFunction = invokeFunction;
        this.taskCallback = TaskCallback;
        this.Properties = new Bundle();
    }

    public TTask call(InvokeFunction callFunction) {
        invokeFunction = callFunction;
        return this;
    }

    public TTask callbackHandler(TaskCallback TaskCallback) {
        this.taskCallback = TaskCallback;
        return this;
    }

    public TaskCallback getCallBackHandler() {
        return taskCallback;
    }

    public String getTTag() {
        return tTag;
    }

    public long getTaskID() {
        return this.getId();
    }

    public void setTTag(String tTag) {
        this.tTag = tTag;
    }

    public Bundle getProperties() {
        return Properties;
    }

    public void free() {
        isActive = false;
    }

    @Override
    public synchronized void start() {
        if (this.isAlive() || this.isActive) return;
        isActive = true;
        super.start();
    }

    @Override
    public void run() {
        super.run();
        if (invokeFunction == null) {
            MMLog.log(TAG, "call TTask function null,nothing to do break TTask,tTag = " + tTag);
            free();
            return;
        }

        MMLog.log(TAG, "invoke TTask function tTag = " + tTag);
        invokeFunction.call(this.tTag);//asynchronous

        while (isActive) {
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                MMLog.e(TAG, "run() " + e.getMessage());
            }
        }
    }
}