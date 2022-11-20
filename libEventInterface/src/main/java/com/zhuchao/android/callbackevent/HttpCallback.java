package com.zhuchao.android.callbackevent;


public interface HttpCallback {
    void onEventHttpRequest(String tag, String fromUrl, String toUrl, long progress, long total, String result, int status);
}

