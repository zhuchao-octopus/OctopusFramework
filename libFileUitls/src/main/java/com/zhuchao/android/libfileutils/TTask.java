package com.zhuchao.android.libfileutils;

import com.zhuchao.android.callbackevent.InvokeInterface;
import com.zhuchao.android.callbackevent.TaskCallback;

public class TTask extends Thread {
    private final String TAG = "TTask";
    protected String tTag = null;
    protected InvokeInterface invokeInterface = null;
    protected TaskCallback taskCallback = null;
    protected ObjectList properties = null;
    protected boolean isKeeping = false;

    public TTask(String tag, InvokeInterface invokeInterface) {
        this.tTag = tag;
        this.invokeInterface = invokeInterface;
        this.properties = new ObjectList();
    }

    public TTask(String tag, InvokeInterface invokeInterface, TaskCallback TaskCallback) {
        this.tTag = tag;
        this.invokeInterface = invokeInterface;
        this.taskCallback = TaskCallback;
        this.properties = new ObjectList();
    }

    public TTask invoke(InvokeInterface callFunction) {
        invokeInterface = callFunction;
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

    public ObjectList getProperties() {
        return properties;
    }

    public boolean isKeeping() {
        return isKeeping;
    }

    public void free() {
        isKeeping = false;
    }

    public void freeFree() {
        properties.clear();
        isKeeping = false;
        invokeInterface = null;
    }

    @Override
    public synchronized void start() {
        if (this.isAlive() || this.isKeeping) {
            MMLog.log(TAG, "TTask already been started  isActive = " + isKeeping);
            return;
        }
       if(properties.getInt(DataID.TASK_STATUS_INTERNAL_) == DataID.TASK_STATUS_FINISHED)
       {
           MMLog.log(TAG, "TTask already finished, no need to run again!");
           return;
       }
        try {
            isKeeping = true;
            super.start();
        } catch (Exception e) {
            // e.printStackTrace();
            isKeeping = false;
            MMLog.log(TAG, "TTask start failed " + e.toString());
        }
    }

    @Override
    public void run() {
        super.run();
        if (invokeInterface == null) {
            MMLog.log(TAG, "call TTask function null,nothing to do break TTask,tTag = " + tTag);
            free();
            return;
        }

        if(invokeInterface != null) {//召唤。。。
            MMLog.log(TAG, "invoke TTask demon tTag = " + tTag);
            invokeInterface.CALLTODO(this.tTag);//asynchronous
        }
        while (isKeeping) {
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                MMLog.e(TAG, "run() " + e.getMessage());
            }
        }
    }
}