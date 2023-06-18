package com.zhuchao.android.fbase;

import static com.zhuchao.android.fbase.FileUtils.MD5;
import static java.lang.Thread.NORM_PRIORITY;


import com.zhuchao.android.fbase.eventinterface.InvokeInterface;
import com.zhuchao.android.fbase.eventinterface.TaskCallback;

import java.util.concurrent.locks.LockSupport;

public class TTask implements TTaskInterface {
    private final String TAG = "TTask";
    protected final String TASK_CALLBACK = "TTask.taskCallbackInterfaceName_";
    protected String tName = null;
    protected String tTag = null;
    protected InvokeInterface invokeInterface = null;
    //protected TaskCallback taskCallback = null;
    protected ObjectList properties = null;
    protected boolean isKeeping = false;
    protected int invokedCount = 0;
    protected long delayedMillis = 0;
    protected int taskCallbackCount = 0;
    protected long startTimeStamp = 0;
    private boolean autoFreeRemove = false;
    private boolean autoRemove = false;
    protected Thread ttThread = null;
    protected TaskCallback threadPoolCallback = null;
    protected int newPriority = NORM_PRIORITY;

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

    public TTask(String tName, InvokeInterface invokeInterface, TaskCallback taskCallback) {
        this.tName = tName;
        this.tTag = MD5(tName);
        this.invokeInterface = invokeInterface;
        this.properties = new ObjectList();
        //this.taskCallback = TaskCallback;
        this.properties.putObject(TASK_CALLBACK + taskCallbackCount, taskCallback);
        taskCallbackCount++;
    }

    public boolean isAutoFreeRemove() {
        return autoFreeRemove;
    }

    public void setAutoFreeRemove(boolean autoFreeRemove) {
        this.autoFreeRemove = autoFreeRemove;
    }

    public boolean isAutoRemove() {
        return autoRemove;
    }

    public void setAutoRemove(boolean autoRemove) {
        this.autoRemove = autoRemove;
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
        if(invokeInterface != null)
           invokeInterface = callFunction;
        return this;
    }

    public InvokeInterface getInvokeInterface() {
        return invokeInterface;
    }

    public void setInvokeInterface(InvokeInterface invokeInterface) {
        this.invokeInterface = invokeInterface;
    }

    public int getTaskCallbackCount() {
        return taskCallbackCount;
    }

    //任务完成后的回调
    public TTask callbackHandler(TaskCallback taskCallback) {
        //this.taskCallback = taskCallback;
        this.properties.putObject(TASK_CALLBACK + taskCallbackCount, taskCallback);
        taskCallbackCount++;
        return this;
    }

    public void deleteTaskCallback(TaskCallback taskCallback) {
        //this.properties
    }

    public TaskCallback getCallBackHandler() {
        Object obj = properties.get(TASK_CALLBACK + 0);
        if (obj != null)
            return (TaskCallback) obj;
        else
            return null;
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

    public boolean isWorking() {
        return isBusy();
    }

    public void free() {
        isKeeping = false;
        delayedMillis = 0;
        startTimeStamp = 0;
    }

    public void freeFree() {
        free();
        properties.clear();
        taskCallbackCount = 0;
        invokeInterface = null;
        //taskCallback = null;
        threadPoolCallback = null;
        ttThread = null;
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
    }//与主题任务同步

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
            MMLog.log(TAG, "TTask is working tName:" + tName);
            return;
        }
        if (properties.getInt(DataID.TASK_STATUS_INTERNAL_) == DataID.TASK_STATUS_FINISHED_STOP) {
            MMLog.log(TAG, "TTask can not start again tName:" + tName);
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
            MMLog.log(TAG, "TTask start failed Keeping " + isKeeping + " tName:" + tName);
            //MMLog.log(TAG, "TTask start failed " + e.toString() + " tTag:" + tTag);
        }
    }

    private class MyThread extends Thread {
        @Override
        public void run() {
            super.run();
            //MMLog.log("MyThread","start ............ ");
            startTimeStamp = System.currentTimeMillis();

            doRunFunction();//主题任务

            sleepWaiting();

            MMLog.log(TTask.this.TAG, "Ttask finish tName = " + tName + " count = " + invokedCount);
            properties.putInt(DataID.TASK_STATUS_INTERNAL_, DataID.TASK_STATUS_FINISHED_STOP);

            //任务完成回调必须放到线程池回调前面，线程池自动释放掉匿名线程导致安全隐患
            doCallBackHandle(DataID.TASK_STATUS_FINISHED_STOP);//异步等待标记
            if (threadPoolCallback != null) {
                //内部使用，当前任务已经完成，宿主任务终止
                //（内部使用）任务结束、终止、停止不再需要运行，305
                threadPoolCallback.onEventTask(TTask.this, DataID.TASK_STATUS_FINISHED_STOP);
            } else if (isAutoFreeRemove())//没有threadPool
            {
                freeFree();
            }
            ttThread = null;
        }

        private void sleepWaiting() {
            while (isKeeping) {//hold住线程，等待异步任务完成，调用者来结束。
                try {  //v1.8 去掉 宿主任务可以提前结束
                    Thread.sleep(1000);
                    //任务主题可以是个异步任务
                    doCallBackHandle(DataID.TASK_STATUS_FINISHED_WAITING);//异步等待标记
                } catch (InterruptedException e) {
                    MMLog.e(TAG, "run() " + e.getMessage());
                }
            }//while
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
        //MMLog.log(TAG, "TTask invokes Interface, tTag = " + tTag + ",invokedCount = " + invokedCount);
        invokeInterface.CALLTODO(this.tTag);//asynchronous 异步任务体
    }//doRunFunction()

    public void doCallBackHandle(int status) {
        for (int i = 0; i < taskCallbackCount; i++) {
            TaskCallback taskCallback = null;
            try {
                taskCallback = (TaskCallback) this.properties.getObject(TASK_CALLBACK + i);
            } catch (Exception e) {
                //e.printStackTrace();
            }
            if (taskCallback != null) {
                taskCallback.onEventTask(this, status);
            }
        }
    }


}