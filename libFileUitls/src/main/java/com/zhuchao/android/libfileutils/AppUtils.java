package com.zhuchao.android.libfileutils;

import static com.zhuchao.android.libfileutils.FileUtils.EmptyString;

import android.app.ActivityManager;
import android.app.usage.UsageStats;
import android.app.usage.UsageStatsManager;
import android.content.Context;
import android.os.Build;
import android.text.TextUtils;

import java.util.List;

public class AppUtils {
    private static final String TAG ="ForegroundAppUtil";
    //private static final long END_TIME = System.currentTimeMillis();
    //private static final long TIME_INTERVAL = 7 * 24 * 60 * 60 * 1000L;
    //private static final long START_TIME = END_TIME - TIME_INTERVAL;

    /**
     * 获取栈顶的应用包名
     */
    public static String getForegroundActivityName(Context context) {
        String topClassName = null;
        long END_TIME = System.currentTimeMillis();
        ActivityManager activityManager = (ActivityManager) context.getApplicationContext().getSystemService(Context.ACTIVITY_SERVICE);
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP)
        {
            topClassName = activityManager.getRunningTasks(1).get(0).topActivity.getPackageName();
        }
        else
        {
            UsageStats initStat = getForegroundUsageStats(context, END_TIME-60000, END_TIME);
            if (initStat != null)
            {
                topClassName = initStat.getPackageName();
                MMLog.log(TAG,"getForegroundActivityName topClassName="+topClassName);
            }
        }
        if(EmptyString(topClassName))
        {
            List<ActivityManager.RunningAppProcessInfo> appProcessInfoList = activityManager.getRunningAppProcesses();
            for (ActivityManager.RunningAppProcessInfo processInfo : appProcessInfoList)
            {
                if (processInfo.importance == ActivityManager.RunningAppProcessInfo.IMPORTANCE_FOREGROUND) {
                    {
                        topClassName= processInfo.processName;
                    }
                }
            }
        }
        return topClassName;
    }

    /**
     * 判断当前应用是否在前台
     */
    public static boolean isForegroundApp(Context context) {
        return TextUtils.equals(getForegroundActivityName(context), context.getPackageName());
    }

    /**
     * 获取时间段内，
     */
    public static long getTotalForegroundTime(Context context) {
        long END_TIME = System.currentTimeMillis();
        UsageStats usageStats = getPackageUsageStats(context, END_TIME-60000, END_TIME);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            return usageStats != null ? usageStats.getTotalTimeInForeground() : 0;
        }
        return 0;
    }

    /**
     * 获取记录前台应用的UsageStats对象
     *  <uses-permission android:name="android.permission.PACKAGE_USAGE_STATS"/>
     */
    private static UsageStats getForegroundUsageStats(Context context, long startTime, long endTime) {
        UsageStats usageStatsResult = null;
        if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP)
        {
            List<UsageStats> UsageStatsList = getUsageStatsList(context, startTime, endTime);
            if (UsageStatsList == null || UsageStatsList.isEmpty()) return null;
            for (UsageStats usageStats : UsageStatsList)
            {
                if (usageStatsResult == null || usageStatsResult.getLastTimeUsed() < usageStats.getLastTimeUsed()) {
                    usageStatsResult = usageStats;//找出最最近使用过的APP
                }
            }
        }
        return usageStatsResult;
    }

    /**
     * 获取记录当前应用的UsageStats对象
     */
    public static UsageStats getPackageUsageStats(Context context, long startTime, long endTime) {
        if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP)
        {
            List<UsageStats> UsageStatsList = getUsageStatsList(context, startTime, endTime);
            if (UsageStatsList == null || UsageStatsList.isEmpty()) return null;
            for (UsageStats usageStats : UsageStatsList)
            {
                if (TextUtils.equals(usageStats.getPackageName(), context.getPackageName())) {
                    return usageStats;
                }
            }
        }
        return null;
    }

    /**
     * 通过UsageStatsManager获取List<UsageStats>集合
     */
    public static List<UsageStats> getUsageStatsList(Context context, long startTime, long endTime) {
        if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP)
        {
            UsageStatsManager manager = (UsageStatsManager) context.getApplicationContext().getSystemService(Context.USAGE_STATS_SERVICE);
            //UsageStatsManager.INTERVAL_WEEKLY，UsageStatsManager的参数定义了5个，具体查阅源码
            List<UsageStats> UsageStatsList = manager.queryUsageStats(UsageStatsManager.INTERVAL_BEST, startTime, endTime);
            if (UsageStatsList == null || UsageStatsList.size() == 0)
            {// 没有权限，获取不到数据
                //Intent intent = new Intent(Settings.ACTION_USAGE_ACCESS_SETTINGS);
                //intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                //context.getApplicationContext().startActivity(intent);
                //Toast t = Toast.makeText(context,"need permmition to access settings .", Toast.LENGTH_LONG);
                //t.show();
                MMLog.log(TAG,"getUsageStatsList fail,need Settings.ACTION_USAGE_ACCESS_SETTINGS permission");
                return null;
            }
            return UsageStatsList;
        }
        return null;
    }
}