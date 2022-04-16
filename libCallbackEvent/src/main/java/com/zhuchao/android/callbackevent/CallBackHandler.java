package com.zhuchao.android.callbackevent;

public interface CallBackHandler {
    void onRequestHandler(String tag, String url, String lrl, long progress, long total);
}
