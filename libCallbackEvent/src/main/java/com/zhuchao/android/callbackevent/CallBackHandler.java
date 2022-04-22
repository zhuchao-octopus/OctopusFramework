package com.zhuchao.android.callbackevent;

public interface CallBackHandler {
    void onRequestHandler(String tag, String fromUrl, String toUrl, long progress, long total,String result, int status);
}
