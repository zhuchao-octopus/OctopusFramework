package com.zhuchao.android.fbase;

import static com.zhuchao.android.fbase.FileUtils.MD5;
import static java.lang.Thread.NORM_PRIORITY;

import com.zhuchao.android.eventinterface.InvokeInterface;
import com.zhuchao.android.eventinterface.TaskCallback;

import java.util.concurrent.locks.LockSupport;

public class TTask implements TTaskInterface {
    private final String TAG = "TTask";
    protected String tName = null;
    protected String tTag = null;
    protected InvokeInterface invokeInterface = null;
    protected TaskCallback taskCallback = null;
    protected ObjectList properties = null;
    protected boolean isKeeping = false;
    protected int invokedCount = 0;
    private long delayedMillis = 0;
    //private long taskTimeOut = 0;
    private long startTimeStamp  = 0;

    private Thread ttThread = null;
    private TaskCallback threadPoolCallback = null;
    private int newPriority = NORM_PRIORITY;

    public TTask(String tName) {
        this.tName = tName;
        this.tTag = MD5(tName);
        this.invokeInterface = null;
        this.properties = new ObjectList();
    }

    public TTask(String tName, InvokeInterface invokeInterface) {
        this.tName = tName;
        this.tTag = MD5(tName);
        this.invokeInterface = invokeInterface;
        this.properties = new ObjectList();
    }

    public TTask(String tName, InvokeInterface invokeInterface, TaskCallback TaskCallback) {
        this.tName = tName;
        this.tTag = MD5(tName);
        this.invokeInterface = invokeInterface;
        this.taskCallback = TaskCallback;
        this.properties = new ObjectList();
    }

    public ObjectList getProperties() {
        return properties;
    }

    public long getTaskID() {
        return 0;
    }

    public int getInvokedCount() {
        return invokedCount;
    }

    public TaskCallback getThreadPoolCallback() {
        return this.threadPoolCallback;
    }

    public void setThreadPoolCallback(TaskCallback threadPoolCallback) {
        this.threadPoolCallback = threadPoolCallback;
    }

    //任务主题
    public TTask invoke(InvokeInterface callFunction) {
        if (isBusy()) {
            MMLog.i(TAG, "this task is busy! not allow invoking another method tag = " + this.tTag);
            return this;
        }
        invokeInterface = callFunction;
        return this;
    }

    public InvokeInterface getInvokeInterface() {
        return invokeInterface;
    }

    //任务完成后的回调
    public TTask callbackHandler(TaskCallback taskCallback) {
        this.taskCallback = taskCallback;
        return this;
    }

    public TaskCallback getCallBackHandler() {
        return taskCallback;
    }

    public String getTaskTag() {
        return tTag;
    }

    public void setTaskTag(String tTag) {
        this.tTag = tTag;
    }

    public String getTaskName() {
        return tName;
    }

    public void setTaskName(String tName) {
        this.tName = tName;
    }

    public boolean isKeeping() {
        return isKeeping;
    }

    public TTask setKeep(boolean keeping) {
        isKeeping = keeping;
        return this;
    }

    public boolean isBusy() {
        return (this.ttThread != null) || this.isKeeping;
    }

    public void free() {
        isKeeping = false;
        delayedMillis = 0;
        startTimeStamp = 0;
    }

    public void freeFree() {
        free();
        properties.clear();
        invokeInterface = null;
        taskCallback = null;
        threadPoolCallback = null;
        ttThread=null;
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
        isKeeping = true;
        start();
    }

    public synchronized void startDelayed(long millis) {
        delayedMillis = millis;
        start();
    }

    public synchronized void startAgainDelayed(long millis) {
        delayedMillis = millis;
        startAgain();
    }

    //public long getTaskTimeOut() {
    //    return taskTimeOut;
    //}

    //public void setTaskTimeOut(long taskTimeOut) {
    //    this.taskTimeOut = taskTimeOut;
    //}

    public void lock() {
        isKeeping = true;
    }

    public void unLock() {
        isKeeping = false;
    }

    @Override
    public void unPark() {
        if (ttThread != null)
            LockSupport.unpark(ttThread);
    }

    @Override
    public void pack() {
        if (ttThread != null)
            LockSupport.park(ttThread);
    }

    @Override
    public TTask setPriority(int newPriority) {
        this.newPriority = newPriority;
        return this;
    }

    @Override
    public long getStartTick() {
        return this.startTimeStamp;
    }

    @Override
    public boolean isTimeOut(long timeOutMillis) {
        return (System.currentTimeMillis() - startTimeStamp) > timeOutMillis;
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////
    //任务只有在 !isAlive && !isKeeping &&
    // properties.getInt(DataID.TASK_STATUS_INTERNAL_) != DataID.TASK_STATUS_FINISHED_STOP
    //的情况下才能启动
    @Override
    public synchronized void start() {
        //if (ttThread != null || this.isKeeping) {
        if (isBusy()) {
            MMLog.log(TAG, "TTask already been started isAlive Keeping = " + isKeeping + ",tag = " + tTag);
            return;
        }
        if (properties.getInt(DataID.TASK_STATUS_INTERNAL_) == DataID.TASK_STATUS_FINISHED_STOP) {
            MMLog.log(TAG, "TTask already finished, no need to run again! tag = " + getTaskTag());
            return;
        }
        try {
            //super.start();
            ttThread = new MyThread();
            ttThread.setPriority(newPriority);
            //android.os.Process.setThreadPriority(android.os.Process.THREAD_PRIORITY_BACKGROUND);
            ttThread.start();
        } catch (Exception e) {
            //e.printStackTrace();
            MMLog.log(TAG, "TTask start failed this.isAlive=" + " isKeeping=" + isKeeping + " tName:" + tName);
            MMLog.log(TAG, "TTask start failed " + e.toString() + " tTag:" + tTag);
        }
    }

    private class MyThread extends Thread {
        @Override
        public void run() {
            super.run();
            //MMLog.log("MyThread","start ............ ");
            startTimeStamp = System.currentTimeMillis();
            doRunFunction();

            properties.putInt(DataID.TASK_STATUS_INTERNAL_, DataID.TASK_STATUS_FINISHED_STOP);
            if (threadPoolCallback != null) {
                //内部使用，当前任务已经完成，宿主任务终止
                //（内部使用）任务结束、终止、停止不再需要运行，305
                threadPoolCallback.onEventTask(this, DataID.TASK_STATUS_FINISHED_STOP);
            }
            ttThread = null;
            //MMLog.log("MyThread","stop ............ ");
        }
    }

    private void doRunFunction() {
        if (invokeInterface == null) {
            MMLog.log(TAG, "call TTask function null,nothing to do, break TTask,tTag = " + tTag);
            free();
            return;
        }
        if (delayedMillis > 0) {
            try {
                Thread.sleep(delayedMillis);
            } catch (InterruptedException ignored) {
            }
        }
        delayedMillis = 0;
        //召唤主题。。。
        invokedCount++;
        MMLog.log(TAG, "TTask invokes demon, tTag = " + tTag + ",invokedCount = " + invokedCount);
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

    }

}