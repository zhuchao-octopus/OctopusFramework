package com.zhuchao.android.session;

import com.zhuchao.android.eventinterface.HttpCallback;
import com.zhuchao.android.eventinterface.InvokeInterface;
import com.zhuchao.android.eventinterface.TRequestEventInterface;
import com.zhuchao.android.eventinterface.TaskCallback;
import com.zhuchao.android.fbase.DataID;
import com.zhuchao.android.fbase.ObjectList;
import com.zhuchao.android.fbase.TTask;
import com.zhuchao.android.fbase.TTaskInterface;
import com.zhuchao.android.net.HttpUtils;

//Session0 本身不是一个基于Thread的TTask，而是一个实现了指向TTask的接口，在这里最终指向内部一个TTask实例。
public class Session0 implements TRequestEventInterface, TTaskInterface, InvokeInterface {
    private final String TAG = "UpdateSession";
    private String SessionName;
    private String requestURL = DataID.SESSION_UPDATE_TEST_INIT;
    private String requestParameter = null;
    private String requestMethod = "PUT";
    private static final TTask tTask = new TTask(DataID.SESSION_UPDATE_JHZ_TEST_UPDATE_NAME);

    public Session0() {
        SessionName = tTask.getTTag();
        tTask.invoke(this);
    }

    @Override
    public String getRequestParameter() {
        return requestParameter;
    }

    @Override
    public void setRequestParameter(String requestParameter) {
        this.requestParameter = requestParameter;
    }

    @Override
    public String getRequestMethod() {
        return requestMethod;
    }

    @Override
    public void setRequestMethod(String requestMethod) {
        this.requestMethod = requestMethod;
    }

    @Override
    public String getRequestURL() {
        return null;
    }

    @Override
    public void setRequestURL(String requestURL) {

    }

    ////////////////////////////////////////////////////////////////////////////////////////////////
    ////////////////////////////////////////////////////////////////////////////////////////////////
    @Override
    public TTask invoke(InvokeInterface callFunction) {
        return tTask.invoke(callFunction);
    }

    @Override
    public TTask callbackHandler(TaskCallback taskCallback) {
        return tTask.callbackHandler(taskCallback);
    }

    @Override
    public InvokeInterface getInvokeInterface() {
        return tTask.getInvokeInterface();
    }

    @Override
    public TaskCallback getCallBackHandler() {
        return tTask.getCallBackHandler();
    }

    @Override
    public String getTTag() {
        return tTask.getTTag();
    }

    @Override
    public long getTaskID() {
        return tTask.getTaskID();
    }

    @Override
    public void setTTag(String tTag) {
        tTask.setTTag(tTag);
    }

    @Override
    public String getTName() {
        return tTask.getTName();
    }

    @Override
    public void setTName(String tName) {
        tTask.setTName(tName);
    }

    @Override
    public ObjectList getProperties() {
        return tTask.getProperties();
    }

    @Override
    public boolean isKeeping() {
        return tTask.isKeeping();
    }

    @Override
    public void setKeeping(boolean keeping) {
        tTask.setKeeping(keeping);
    }

    @Override
    public void lock() {
        tTask.lock();
    }

    @Override
    public void unLock() {
        tTask.unLock();
    }

    @Override
    public int getInvokedCount() {
        return tTask.getInvokedCount();
    }

    @Override
    public boolean isBusy() {
        return tTask.isBusy();
    }

    @Override
    public void free() {
        tTask.free();
    }

    @Override
    public void freeFree() {
        tTask.freeFree();
    }

    @Override
    public TTask reset() {
        tTask.reset();
        return tTask;
    }

    @Override
    public void resetAll() {
        tTask.resetAll();
    }

    @Override
    public void start() {
        tTask.start();
    }

    @Override
    public void startAgain() {
        tTask.startAgain();
    }

    @Override
    public void startWait() {
        tTask.startWait();
    }

    @Override
    public void startDelayed(long millis) {
        tTask.startDelayed(millis);
    }

    @Override
    public void startAgainDelayed(long millis) {
        tTask.startAgainDelayed(millis);
    }

    @Override
    public void unPark() {

    }

    @Override
    public void pack() {

    }

    @Override
    public void CALLTODO(String tag) {
        if (requestMethod.equals("PUT")) {
            HttpUtils.requestPut(tag, requestURL, requestParameter, new HttpCallback() {
                @Override
                public void onEventHttpRequest(String tag, String fromUrl, String toUrl, long progress, long total, String result, int status) {
                    if (status == DataID.TASK_STATUS_ERROR)
                        reset();
                    //MMLog.i(TAG, status + "," + fromUrl + "?" + requestParameter);
                    //MMLog.i(TAG, status + "," + result);
                }
            });
        }
        if (requestMethod.equals("GET")) {

        }
        if (requestMethod.equals("POST")) {

        }
    }
}
