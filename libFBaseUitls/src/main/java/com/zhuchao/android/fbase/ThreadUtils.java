package com.zhuchao.android.fbase;

import android.os.Handler;
import android.os.Looper;

public class ThreadUtils {

    private static Handler MainLooperHandler = new Handler(Looper.getMainLooper());

    public static void runOnMainUiThread(final Runnable runnable) {
        if (Looper.myLooper() == Looper.getMainLooper()) {
            runnable.run();
        }
        else
        {
            MainLooperHandler.post(runnable);//发送到主线程执行
        }
    }

    public static void runThread(final Runnable runnable) {
        runnable.run();//直接执行
    }

    public static void runThreadNotOnMainUIThread(final Runnable runnable) {
        runnable.run();
    }

}
