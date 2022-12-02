package com.zhuchao.android.session;

import com.zhuchao.android.eventinterface.HttpCallback;
import com.zhuchao.android.eventinterface.InvokeInterface;
import com.zhuchao.android.eventinterface.TaskCallback;
import com.zhuchao.android.fileutils.DataID;
import com.zhuchao.android.fileutils.TTask;
import com.zhuchao.android.net.HttpUtils;

public class TNetTask extends TTask implements InvokeInterface {
    private final String TAG = "TNetTask";
    private String requestURL = DataID.SESSION_UPDATE_TEST_INIT;
    private String requestParameter = null;
    private String requestMethod = "PUT";

    public TNetTask(String tName) {
        super(tName);
        invoke(this);
    }

    public TNetTask(String tName, InvokeInterface invokeInterface) {
        super(tName, invokeInterface);
    }

    public TNetTask(String tName, InvokeInterface invokeInterface, TaskCallback TaskCallback) {
        super(tName, invokeInterface, TaskCallback);
    }

    public String getRequestParameter() {
        return requestParameter;
    }

    public void setRequestParameter(String requestJson) {
        this.requestParameter = requestJson;
    }

    public String getRequestMethod() {
        return requestMethod;
    }

    public void setRequestMethod(String requestMethod) {
        this.requestMethod = requestMethod;
    }

    public String getRequestURL() {
        return requestURL;
    }

    public void setRequestURL(String requestURL) {
        this.requestURL = requestURL;
    }

    @Override
    public void CALLTODO(String tag) {
        if (this.requestMethod.equals("PUT")) {
            HttpUtils.requestPut(tag, requestURL, requestParameter, new HttpCallback() {
                @Override
                public void onEventHttpRequest(String tag, String fromUrl, String toUrl, long progress, long total, String result, int status) {
                    if (status == DataID.TASK_STATUS_ERROR) {
                        //MMLog.e(TAG, "requestPut " + fromUrl + "," + result);
                        reset();
                    }
                    //MMLog.i(TAG, status + "," + fromUrl + "?" + requestParameter);
                    //MMLog.i(TAG, status + "," + result);
                }
            });
        }
        if (this.requestMethod.equals("GET")) {

        }
        if (this.requestMethod.equals("POST")) {

        }
    }

}
