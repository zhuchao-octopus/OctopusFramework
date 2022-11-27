package com.zhuchao.android.fileutils;

import com.zhuchao.android.callbackevent.InvokeInterface;
import com.zhuchao.android.callbackevent.TaskCallback;

public class TTask extends Thread {
    private final String TAG = "TTask";
    protected String tName = null;
    protected String tTag = null;
    protected InvokeInterface invokeInterface = null;
    protected TaskCallback taskCallback = null;
    protected ObjectList properties = null;
    protected boolean isKeeping = false;
    protected int invokedCount = 0;

    public TTask(String tag, InvokeInterface invokeInterface) {
        this.tTag = tag;
        this.tName = tag;
        this.invokeInterface = invokeInterface;
        this.properties = new ObjectList();
    }

    public TTask(String tag, InvokeInterface invokeInterface, TaskCallback TaskCallback) {
        this.tTag = tag;
        this.tName = tag;
        this.invokeInterface = invokeInterface;
        this.taskCallback = TaskCallback;
        this.properties = new ObjectList();
    }

    public TTask invoke(InvokeInterface callFunction) {
        if(isBusy()) {
            MMLog.i(TAG, "this task is busy! tag = " + this.tTag);
            return this;
        }
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

    public String getTName() {
        return tName;
    }

    public void setTName(String tName) {
        this.tName = tName;
    }

    public ObjectList getProperties() {
        return properties;
    }

    public boolean isKeeping() {
        return isKeeping;
    }

    public void setKeeping(boolean keeping) {
        isKeeping = keeping;
    }

    public int getInvokedCount() {
        return invokedCount;
    }

    public boolean isBusy() {
        return this.isAlive() || this.isKeeping;
    }

    public void free() {
        isKeeping = false;
    }

    public void freeFree() {
        properties.clear();
        isKeeping = false;
        invokeInterface = null;
        taskCallback = null;
    }

    public TTask reset() {
        properties.putInt(DataID.TASK_STATUS_INTERNAL_, DataID.TASK_STATUS_CAN_RESTART);
        free();
        return this;
    }

    public TTask resetAll() {
        properties.putInt(DataID.TASK_STATUS_INTERNAL_, DataID.TASK_STATUS_CAN_RESTART);
        freeFree();
        return this;
    }

    public synchronized void startAgain() {
        properties.putInt(DataID.TASK_STATUS_INTERNAL_, DataID.TASK_STATUS_CAN_RESTART);
        start();
    }

    public synchronized void startWait() {
        isKeeping =true;
        start();
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////
    //任务只有在 !isAlive && !isKeeping &&
    // properties.getInt(DataID.TASK_STATUS_INTERNAL_) != DataID.TASK_STATUS_FINISHED_STOP
    //的情况下才能启动
    @Override
    public synchronized void start() {
        if (this.isAlive() || this.isKeeping) {
            //MMLog.log(TAG, "TTask already been started isAlive Keeping = " + isKeeping+",tag = "+tTag);
            return;
        }
        if (properties.getInt(DataID.TASK_STATUS_INTERNAL_) == DataID.TASK_STATUS_FINISHED_STOP) {
            MMLog.log(TAG, "TTask already finished, no need to run again! tag = "+getTTag());
            return;
        }
        try {
            //isKeeping = true;有选择的keep
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
            MMLog.log(TAG, "call TTask function null,nothing to do, break TTask,tTag = " + tTag);
            free();
            return;
        }
        //android.os.Process.setThreadPriority(android.os.Process.THREAD_PRIORITY_BACKGROUND);
        setPriority(MAX_PRIORITY);
        //召唤主题。。。
        invokedCount++;
        MMLog.log(TAG, "TTask invokes demon, tTag = " + tTag+",invokedCount = "+invokedCount);
        invokeInterface.CALLTODO(this.tTag);//asynchronous 异步任务体
        //任务主题可以是个异步任务
        if (taskCallback != null)
            taskCallback.onEventTask(this, DataID.TASK_STATUS_FINISHED_WAITING);//异步等待标记

        //v1.8 去掉 宿主任务可以提前结束
        while (isKeeping) {//hold住线程，等待异步任务完成，调用者来结束。
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                MMLog.e(TAG, "run() " + e.getMessage());
            }
        }
        //内部使用，当前任务已经完成，宿主任务终止
        //（内部使用）任务结束、终止、停止不再需要运行，305
        properties.putInt(DataID.TASK_STATUS_INTERNAL_, DataID.TASK_STATUS_FINISHED_STOP);
    }
}