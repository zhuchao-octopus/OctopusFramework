package com.zhuchao.android.fbase;

import com.zhuchao.android.eventinterface.InvokeInterface;
import com.zhuchao.android.eventinterface.TaskCallback;

public abstract interface TTaskInterface {
    public TTask invoke(InvokeInterface callFunction);
    public TTask callbackHandler(TaskCallback taskCallback);
    public InvokeInterface getInvokeInterface();
    public TaskCallback getCallBackHandler();
    public String getTTag();
    public long getTaskID();
    public void setTTag(String tTag);
    public String getTName();
    public void setTName(String tName);
    public ObjectList getProperties();
    public boolean isKeeping();
    public void setKeeping(boolean keeping);
    public void lock();
    public void unLock();
    public int getInvokedCount();
    public boolean isBusy();
    public void free();
    public void freeFree();
    public TTask reset();
    public void resetAll();
    public void start();
    public void startAgain();
    public void startWait();
    public void startDelayed(long millis);
    public void startAgainDelayed(long millis);

    public void unPark();
    public void pack();
}
