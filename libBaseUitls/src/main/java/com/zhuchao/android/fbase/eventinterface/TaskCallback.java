package com.zhuchao.android.fbase.eventinterface;

public interface TaskCallback {
    //void onEventTask(String tag, String fromUrl, String toUrl, long progress, long total, String result, int status);
    void onEventTaskFinished(Object obj, int status);
}
