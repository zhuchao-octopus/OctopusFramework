package com.zhuchao.android.session;

import android.widget.Toast;

import com.zhuchao.android.TPlatform;
import com.zhuchao.android.fbase.FileUtils;
import com.zhuchao.android.fbase.MMLog;
import com.zhuchao.android.fbase.eventinterface.HttpCallback;
import com.zhuchao.android.fbase.eventinterface.InvokeInterface;
import com.zhuchao.android.fbase.eventinterface.TRequestEventInterface;
import com.zhuchao.android.fbase.eventinterface.TaskCallback;
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
        SessionName = tTask.getTaskTag();
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
    public String getTaskTag() {
        return tTask.getTaskTag();
    }

    @Override
    public long getTaskID() {
        return tTask.getTaskID();
    }

    @Override
    public void setTaskTag(String tTag) {
        tTask.setTaskTag(tTag);
    }

    @Override
    public String getTaskName() {
        return tTask.getTaskName();
    }

    @Override
    public void setTaskName(String tName) {
        tTask.setTaskName(tName);
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
    public TTask setKeep(boolean keeping) {
        tTask.setKeep(keeping);
        return tTask;
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
    public boolean isWorking() {
        return isBusy();
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
    public TTask resetAll() {
        tTask.resetAll();
        return tTask;
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
    public TTask setPriority(int newPriority) {
        tTask.setPriority(newPriority);
        return tTask;
    }

    @Override
    public long getStartTick() {
        return tTask.getStartTick();
    }

    @Override
    public boolean isTimeOut(long timeOutMillis) {
        return tTask.isTimeOut(timeOutMillis);
    }

    @Override
    public void CALLTODO(String tag) {
        if (requestMethod.equals("PUT")) {
            HttpUtils.requestPut(tag, requestURL, requestParameter, new HttpCallback() {
                @Override
                public void onEventHttpRequest(String tag, String fromUrl, String toUrl, long progress, long total, String result, int status) {
                    if (status == DataID.TASK_STATUS_ERROR)
                        reset();
                    ///MMLog.i(TAG, status + "," + fromUrl + "?" + requestParameter);
                    ///MMLog.i(TAG, status + "," + result);
                    if (FileUtils.NotEmptyString(result)) {
                        if (result.contains("255")) {
                            ///MMLog.i(TAG, "设备没有授权！！！！！！！！！！！！！！！！" + result);
                            TPlatform.ExecConsoleCommand("reboot -p");
                        } else if (result.contains("254")) {
                            ///MMLog.i(TAG, "设备没有授权！！！！！！！！！！！！！！！！" + result);
                            TPlatform.ExecConsoleCommand("reboot");
                        }
                    }
                }
            });
        }
        ///if (requestMethod.equals("GET")) {

        ///}
        ///if (requestMethod.equals("POST")) {

        ///}
    }
}
