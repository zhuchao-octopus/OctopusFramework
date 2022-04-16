package com.zhuchao.android.callbackevent;


public interface HttpCallBack {
    void onHttpRequestProgress(String tag, String url, String lrl, long progress, long total);

    void onHttpRequestComplete(String tag, String url, String lrl, long progress, long total);

    void onHttpRequestProgress(int tag, String url, String lrl, long progress, long total);
}

