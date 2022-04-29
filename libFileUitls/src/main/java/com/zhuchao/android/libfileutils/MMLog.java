package com.zhuchao.android.libfileutils;

import android.util.Log;

import java.util.Properties;

public class MMLog {
    private static final String MTAG = "MMLog";
    private static final String LOG_SYSTEM_PROPERTY = "persist.app.log.on.off";
    private static String debugOnOff = "false";

    public static void setDebugOnOff(String onOff) {
        debugOnOff = onOff;
    }

    public static void pollingDebug() {
        Properties properties = System.getProperties();
        debugOnOff = properties.getProperty(LOG_SYSTEM_PROPERTY);
    }

    public static void v(String TAG, String logMsg) {
        if (debugOnOff.equals("true"))
            Log.v(MTAG + "." + TAG, logMsg);
    }

    public static void log(String TAG, String logMsg) {
        if (debugOnOff.equals("true"))
            Log.d(MTAG + "." + TAG, logMsg);
    }

    public static void d(String TAG, String logMsg) {
        if (debugOnOff.equals("true"))
            Log.d(MTAG + "." + TAG, logMsg);
    }

    public static void i(String TAG, String logMsg) {
        if (debugOnOff.equals("true"))
            Log.i(MTAG + "." + TAG, logMsg);
    }

    public static void w(String TAG, String logMsg) {
        if (debugOnOff.equals("true"))
            Log.w(MTAG + "." + TAG, logMsg);
    }

    public static void e(String TAG, String logMsg) {
        if (debugOnOff.equals("true"))
            Log.e(MTAG + "." + TAG, logMsg);
    }
}
