package com.zhuchao.android.fbase;

import com.zhuchao.android.eventinterface.InvokeInterface;
import com.zhuchao.android.eventinterface.TaskCallback;

public abstract interface TTaskInterface {
    public TTask invoke(InvokeInterface callFunction);

    public TTask callbackHandler(TaskCallback taskCallback);

    public InvokeInterface getInvokeInterface();

    public TaskCallback getCallBackHandler();

    public ObjectList getProperties();

    public long getTaskID();

    public int getInvokedCount();

    public String getTaskTag();

    public void setTaskTag(String tTag);

    public String getTaskName();

    public void setTaskName(String tName);

    public TTask setKeep(boolean keeping);

    public boolean isKeeping();

    public boolean isBusy();

    public void free();

    public void freeFree();

    public TTask reset();

    public TTask resetAll();

    public void start();

    public void startAgain();

    public void startWait();

    public void startDelayed(long millis);

    public void startAgainDelayed(long millis);

    public void lock();

    public void unLock();

    public void unPark();

    public void pack();

    public TTask setPriority(int newPriority);
    public long getStartTick();
    public boolean isTimeOut(long timeOutMillis);
}
