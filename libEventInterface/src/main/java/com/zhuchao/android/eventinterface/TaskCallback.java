package com.zhuchao.android.eventinterface;

public interface TaskCallback {
    //void onEventTask(String tag, String fromUrl, String toUrl, long progress, long total, String result, int status);
    void onEventTask(Object obj, int status);
}
