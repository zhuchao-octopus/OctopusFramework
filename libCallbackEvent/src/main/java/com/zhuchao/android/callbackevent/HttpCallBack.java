package com.zhuchao.android.callbackevent;


public interface HttpCallBack {
    void onHttpRequestComplete(String tag, String fromUrl, String toUrl, long progress, long total,String result, int status);
}

