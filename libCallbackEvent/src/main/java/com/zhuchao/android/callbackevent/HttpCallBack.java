package com.zhuchao.android.callbackevent;


public interface HttpCallBack {
    void onHttpRequestProgress(String tag, String fromUrl, String lrl, long progress, long total);

    void onHttpRequestComplete(String tag, String fromUrl, String lrl, long progress, long total);
}

