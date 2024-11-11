package com.zhuchao.android.fbase;

import android.app.ActivityManager;
import android.content.Context;
import android.content.pm.PackageManager;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Iterator;
import java.util.List;

public class TAppProcessUtils {
    private static final String TAG = "TAppProcessUtils";

    public static String getCurrentProcessNameAndId(Context context) {
        int pid = getProcessId();//Process.myPid();
        ActivityManager activityManager = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
        if (activityManager != null) {
            for (ActivityManager.RunningAppProcessInfo processInfo : activityManager.getRunningAppProcesses()) {
                if (processInfo.pid == pid) {
                    //MMLog.d(TAG,"ProcessId="+pid+" processName="+processInfo.processName);
                    return processInfo.processName + " " + pid;
                }
            }
        }
        return null;
    }

    public static int getProcessId() {
        return android.os.Process.myPid();
    }

    public static String getProcessName(Context context, int pID) {
        if (context == null) {
            return null;
        }
        String processName = null;
        Context appContext = context.getApplicationContext();
        ActivityManager am = (ActivityManager) appContext.getSystemService(Context.ACTIVITY_SERVICE);
        List<ActivityManager.RunningAppProcessInfo> l = am.getRunningAppProcesses();
        Iterator<ActivityManager.RunningAppProcessInfo> i = l.iterator();
        PackageManager pm = appContext.getPackageManager();
        while (i.hasNext()) {
            ActivityManager.RunningAppProcessInfo info = (ActivityManager.RunningAppProcessInfo) (i.next());
            try {
                if (info.pid == pID) {
                    processName = info.processName;
                    return processName;
                }
            } catch (Exception ignored) {
            }
        }
        return null;
    }

    public static String getProcessName(Context context) {
        if (context == null) {
            return null;
        }
        int pID = getProcessId();
        String processName = null;
        Context appContext = context.getApplicationContext();
        ActivityManager am = (ActivityManager) appContext.getSystemService(Context.ACTIVITY_SERVICE);
        List<ActivityManager.RunningAppProcessInfo> l = am.getRunningAppProcesses();
        Iterator<ActivityManager.RunningAppProcessInfo> i = l.iterator();
        PackageManager pm = appContext.getPackageManager();
        while (i.hasNext()) {
            ActivityManager.RunningAppProcessInfo info = (ActivityManager.RunningAppProcessInfo) (i.next());
            try {
                if (info.pid == pID) {
                    processName = info.processName;
                    return processName;
                }
            } catch (Exception ignored) {
            }
        }
        return null;
    }

    public static boolean isAppProcess(Context c) {
        if (c == null) {
            return false;
        }
        c = c.getApplicationContext();
        String processName = getProcessName(c);
        return processName != null && processName.equalsIgnoreCase(c.getPackageName());
    }

    public static boolean isServiceRunning(Context context, String serviceName) {
        boolean isRunning = false;
        ActivityManager am = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
        List<ActivityManager.RunningServiceInfo> lists = am.getRunningServices(100);
        for (ActivityManager.RunningServiceInfo info : lists) {//判断服务
            if (info.service.getClassName().equals(serviceName)) {
                isRunning = true;
            }
        }
        return isRunning;
    }

    public static boolean isProcessRunning(Context context, String processName) {
        boolean isRunning = false;
        ActivityManager am = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
        List<ActivityManager.RunningAppProcessInfo> lists = am.getRunningAppProcesses();
        for (ActivityManager.RunningAppProcessInfo info : lists) {
            if (info.processName.equals(processName)) {
                isRunning = true;
                break;
            }
        }
        return isRunning;
    }

    public synchronized static void killApplication(Context context, String packageName) {
        ActivityManager mActivityManager = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
        Method method = null;
        try {
            method = Class.forName("android.app.ActivityManager").getMethod("forceStopPackage", String.class);
            method.invoke(mActivityManager, packageName);
            MMLog.d(TAG, "Kill application: " + packageName);
        } catch (NoSuchMethodException | ClassNotFoundException | IllegalAccessException | InvocationTargetException e) {
            MMLog.log(TAG, e.toString());//e.printStackTrace();
        }
    }
}
