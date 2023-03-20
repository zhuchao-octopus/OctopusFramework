package com.zhuchao.android.fbase;

import android.util.Log;

public class MMLog {
    private static final String MTAG = "MMLog";
    private static boolean debugLogOnOff = true;
    //private static boolean logProperty = false;
    public static final String LOG_SYSTEM_PROPERTY = "com.zhuchao.android.log.OnOff";

    public static void setDebugOnOff(boolean debugOnOff) {
        MMLog.debugLogOnOff = debugOnOff;
    }
    public static void setLogOnOff(boolean debugOnOff) {
        MMLog.debugLogOnOff = debugOnOff;
    }

    public static void v(String TAG, String logMsg) {
        if (debugLogOnOff)
            Log.v(MTAG + "." + TAG, logMsg);
    }

    public static void log(String TAG, String logMsg) {
        if (debugLogOnOff)
            Log.d(MTAG + "." + TAG, logMsg);
    }

    public static void d(String TAG, String logMsg) {
        if (debugLogOnOff)
            Log.d(MTAG + "." + TAG, logMsg);
    }

    public static void i(String TAG, String logMsg) {
        if (debugLogOnOff)
            Log.i(MTAG + "." + TAG, logMsg);
    }

    public static void w(String TAG, String logMsg) {
        if (debugLogOnOff)
            Log.w(MTAG + "." + TAG, logMsg);
    }

    public static void e(String TAG, String logMsg) {
        if (debugLogOnOff)
            Log.e(MTAG + "." + TAG, logMsg);
    }
}
