package com.zhuchao.android.fbase;

import android.util.Log;

public class MMLog {
    private static final String MTAG = "MMLog";
    private static boolean mDebugLogOnOff = true;
    //private static boolean logProperty = false;
    private static final StringBuilder mStringBuffer = new StringBuilder();
    public static final String LOG_SYSTEM_PROPERTY = "com.zhuchao.android.log.OnOff";

    public static void setDebugOnOff(boolean debugOnOff) {
        MMLog.mDebugLogOnOff = debugOnOff;
    }

    public static void setLogOnOff(boolean debugOnOff) {
        MMLog.mDebugLogOnOff = debugOnOff;
    }

    public static void v(String TAG, String logMsg) {
        if (mDebugLogOnOff) Log.v(MTAG + "." + TAG, logMsg);
    }

    public static void log(String TAG, String logMsg) {
        if (mDebugLogOnOff) Log.d(MTAG + "." + TAG, logMsg);
    }

    public static void d(String TAG, String logMsg) {
        if (mDebugLogOnOff) Log.d(MTAG + "." + TAG, logMsg);
    }
    public static void debug(String TAG, String logMsg) {
        if (mDebugLogOnOff) Log.d(MTAG + "." + TAG, logMsg);
    }
    public static void mm(String logMsg) {
        if (logMsg == null) mStringBuffer.setLength(0);
        else {
            String line = System.getProperty("line.separator");
            mStringBuffer.append(logMsg).append(line);
        }
    }

    public static void m(String TAG) {
        d(TAG, mStringBuffer.toString());
        mStringBuffer.setLength(0);
    }

    public static void i(String TAG, String logMsg) {
        if (mDebugLogOnOff) Log.i(MTAG + "." + TAG, logMsg);
    }

    public static void w(String TAG, String logMsg) {
        if (mDebugLogOnOff) Log.w(MTAG + "." + TAG, logMsg);
    }

    public static void e(String TAG, String logMsg) {
        if (mDebugLogOnOff) Log.e(MTAG + "." + TAG, logMsg);
    }
}
