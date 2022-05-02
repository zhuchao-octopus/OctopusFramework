package com.zhuchao.android.libfileutils;

import static com.zhuchao.android.libfileutils.FileUtils.EmptyString;

import android.app.ActivityManager;
import android.app.usage.UsageStats;
import android.app.usage.UsageStatsManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Build;
import android.text.TextUtils;

import androidx.core.content.FileProvider;

import com.zhuchao.android.callbackevent.AppsCallback;
import com.zhuchao.android.libfileutils.bean.AppInfor;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class AppUtils {
    private static final String TAG = "MyAppsManager";
    public static final String UNINSTALL_ACTION = "UNINSTALL";
    public static final String INSTALL_ACTION = "INSTALL";
    public static final String SCANING_ACTION = "SCANING";
    public static final String SCANING_COMPLETE_ACTION = "SCANINGCOMPLETE";
    public static final String ADDTOMYAPPS_ACTION = "ADDTOMYAPPS";
    public static final String DELFROMMYAPPS_ACTION = "DELFROMMYAPPS";
    public static final String USED_HISTORY_ACTION = "USEDHISTORY";
    public static final String HOT_CLEAR_ACTION = "HOTCLEAR";
    private static Context mContext;
    private AppsCallback mAppsCallback = null;
    //private ExecutorService mExecutorService;
    private PackageManager mPackageManager;
    private List<AppInfor> AllAppInfo = new ArrayList<AppInfor>();
    private List<AppInfor> UserAppInfors = new ArrayList<AppInfor>();
    private List<String> Filter = new ArrayList<String>();


    public AppUtils(Context context) {
        mContext = context;
        mAppsCallback = null;
        mPackageManager = mContext.getPackageManager();
       // mExecutorService = Executors.newSingleThreadExecutor();
        getAllAppsInfor();
    }

    public List<AppInfor> getAllAppInfo() {
        return AllAppInfo;
    }

    private synchronized void getAllAppsInfor() {
        AllAppInfo.clear();
        //获得所有的安装包
        List<PackageInfo> installedPackages = mPackageManager.getInstalledPackages(0);
        //遍历每个安装包，获取对应的信息
        for (PackageInfo packageInfo : installedPackages) {
            final AppInfor appInfor = new AppInfor();
            appInfor.setApplicationInfo(packageInfo.applicationInfo);
            appInfor.setVersionCode(packageInfo.versionCode);
            appInfor.setVersionCodeName(packageInfo.versionName);
            //得到icon
            Drawable drawable = packageInfo.applicationInfo.loadIcon(mPackageManager);
            appInfor.setIcon(drawable);
            //得到程序的名字
            String apkName = packageInfo.applicationInfo.loadLabel(mPackageManager).toString();
            appInfor.setName(apkName);
            //得到程序的包名
            String packageName = packageInfo.packageName;
            appInfor.setPackageName(packageName);
            //得到程序的资源文件夹
            String sourceDir = packageInfo.applicationInfo.sourceDir;
            appInfor.setSourceDir(sourceDir);
            File file = new File(sourceDir);
            //得到apk的大小
            long size = file.length();
            appInfor.setSize(size);
            //获取到安装应用程序的标记
            int flags = packageInfo.applicationInfo.flags;
            if ((flags & ApplicationInfo.FLAG_SYSTEM) != 0) {
                //表示系统app
                appInfor.setUserApp(false);
            } else {
                //表示用户app
                appInfor.setUserApp(true);
            }
            if ((flags & ApplicationInfo.FLAG_EXTERNAL_STORAGE) != 0) {
                //表示在sd卡
                appInfor.setRom(false);
            } else {
                //表示内存
                appInfor.setRom(true);
            }
            if (!AllAppInfo.contains(appInfor)) {
                AllAppInfo.add(appInfor);
            }
        }//for
        if ((mAppsCallback != null)) {
            mAppsCallback.onAppsChanged(SCANING_COMPLETE_ACTION, null);
        }
    }

    public boolean isAppInstalled(String packageName) {
        if (EmptyString(packageName))
            return false;
        try {
            ApplicationInfo info = mContext.getPackageManager().getApplicationInfo(packageName, 0);
            return info.enabled;
        } catch (PackageManager.NameNotFoundException e) {
            //e.printStackTrace();
            return false;
        }
    }

    public void registerAppsReceiver() {
        try {
            IntentFilter intentFilter = new IntentFilter();
            intentFilter.addAction(Intent.ACTION_PACKAGE_ADDED);
            intentFilter.addAction(Intent.ACTION_PACKAGE_REMOVED);
            intentFilter.addAction(Intent.ACTION_PACKAGE_REPLACED);
            intentFilter.addAction(SCANING_COMPLETE_ACTION);
            intentFilter.addAction(Intent.ACTION_MEDIA_MOUNTED);
            intentFilter.addAction(Intent.ACTION_MEDIA_UNMOUNTED);

            intentFilter.addDataScheme("package");
            mContext.registerReceiver(AppChangedReceiver, intentFilter);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void unRegAppReceiver() {
        try {
            if (AppChangedReceiver != null)
                if (mContext != null)
                    mContext.unregisterReceiver(AppChangedReceiver);

        } catch (Exception e) {
            //e.printStackTrace();
        }
    }

    private BroadcastReceiver AppChangedReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            MMLog.log(TAG, "BroadcastReceiver:" + intent.getAction());
            if (action.equals(Intent.ACTION_PACKAGE_ADDED)) {
                //UpdateAppsInfor();
            }
            if (action.equals(Intent.ACTION_PACKAGE_REMOVED)) {
                //UpdateAppsInfor();
            }
            if (action.equals(Intent.ACTION_PACKAGE_REPLACED)) {
                // UpdateAppsInfor();
            }
        }
    };

    public void free() {
        unRegAppReceiver();
    }

    public AppInfor getAppInfor(String packageName) {
        for (AppInfor Info : AllAppInfo) {
            if (Info == null) continue;
            if (packageName == null) return null;
            if (packageName.equals(Info.getPackageName()))
                return Info;
        }
        return null;
    }

    public AppInfor getAppInfor(int id) {
        if (AllAppInfo.size() == 0) {
            return null;
        }
        if (id < 0) {
            return null;
        }
        if (id >= AllAppInfo.size()) {
            return null;
        }

        return AllAppInfo.get(id);
    }

    public AppInfor getAppInforByName(String Name) {
        for (AppInfor Info : AllAppInfo) {
            if (Name.equals(Info.getName()))
                return Info;
        }
        return null;
    }

    public boolean startTheApp(AppInfor infor) {
        if (EmptyString(infor.getPackageName())) {
            return false;
        }
        Intent intent = mPackageManager.getLaunchIntentForPackage(infor.getPackageName());
        if (intent != null) {
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            mContext.startActivity(intent);
            MMLog.log(TAG, "LaunchApp>>>>" + infor.getPackageName());
            return true;
        } else {
            MMLog.log(TAG, "LaunchApp not found>>>>" + infor.getPackageName());
            return false;
        }
    }

    public boolean startAppByPackage(String packageName) {
        if (EmptyString(packageName)) {
            return false;
        }
        Intent intent = mPackageManager.getLaunchIntentForPackage(packageName);
        if (intent != null) {
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            mContext.startActivity(intent);
            MMLog.log(TAG, "LaunchApp>>>>" + packageName);
            return true;
        } else {
            MMLog.log(TAG, "LaunchApp not found>>>>" + packageName);
            return false;
        }
    }

    public boolean startApp(int id) {
        if (AllAppInfo.size() == 0) {
            return false;
        }
        if (id < 0) {
            return false;
        }
        if (id >= AllAppInfo.size()) {
            return false;
        }
        String packageName = AllAppInfo.get(id).getPackageName();
        Intent intent = mPackageManager.getLaunchIntentForPackage(packageName);
        if (intent != null) {
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            mContext.startActivity(intent);
            MMLog.log(TAG, "LaunchApp>>>>" + packageName);
            return true;
        } else {
            MMLog.log(TAG, "LaunchApp not found>>>>" + packageName);
            return false;
        }
    }

    public boolean startApp(String name) {
        if (EmptyString(name))
            return false;
        AppInfor Info = getAppInforByName(name);
        String packageName = Info.getPackageName();
        Intent intent = mPackageManager.getLaunchIntentForPackage(packageName);
        if (intent != null) {
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            mContext.startActivity(intent);
            MMLog.log(TAG, "LaunchApp>>>>" + packageName);
            return true;
        } else {
            MMLog.log(TAG, "LaunchApp not found>>>>" + packageName);
            return false;
        }
    }

    public void install(String filePath) {
        Intent intent = new Intent(Intent.ACTION_VIEW);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            intent.setFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
            Uri contentUri = FileProvider.getUriForFile(mContext, BuildConfig.LIBRARY_PACKAGE_NAME + ".fileProvider", new File(filePath));
            intent.setDataAndType(contentUri, "application/vnd.android.package-archive");
        } else {
            intent.setDataAndType(Uri.fromFile(new File(filePath)), "application/vnd.android.package-archive");
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        }
        mContext.startActivity(intent);
    }

    public void uninstall(String packageName) {
        Uri uri = Uri.parse("package:" + packageName);
        Intent intent = new Intent(Intent.ACTION_DELETE, uri);
        mContext.startActivity(intent);
    }

    public static String getVersionName(Context context, String packageName) {
        if (EmptyString(packageName)) return "";
        try {
            PackageManager packageManager = context.getPackageManager();
            PackageInfo packageInfo = packageManager.getPackageInfo(packageName, 0);
            return packageInfo == null ? "" : packageInfo.versionName;
        } catch (PackageManager.NameNotFoundException e) {
            return "";
        }
    }

    public static String getAppName(Context context, String packageName) {
        if (EmptyString(packageName)) return "";
        try {
            PackageManager packageManager = context.getPackageManager();
            PackageInfo packageInfo = packageManager.getPackageInfo(packageName, 0);
            return packageInfo == null ? "" : packageInfo.applicationInfo.loadLabel(packageManager).toString();
        } catch (PackageManager.NameNotFoundException e) {
            return "";
        }
    }

    public static String getForegroundActivityName(Context context) {
        String topClassName = null;
        long END_TIME = System.currentTimeMillis();
        ActivityManager activityManager = (ActivityManager) context.getApplicationContext().getSystemService(Context.ACTIVITY_SERVICE);
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP) {
            topClassName = activityManager.getRunningTasks(1).get(0).topActivity.getPackageName();
        } else {
            UsageStats initStat = getForegroundUsageStats(context, END_TIME - 60000, END_TIME);
            if (initStat != null) {
                topClassName = initStat.getPackageName();
                MMLog.log(TAG, "getForegroundActivityName topClassName=" + topClassName);
            }
        }
        if (EmptyString(topClassName)) {
            List<ActivityManager.RunningAppProcessInfo> appProcessInfoList = activityManager.getRunningAppProcesses();
            for (ActivityManager.RunningAppProcessInfo processInfo : appProcessInfoList) {
                if (processInfo.importance == ActivityManager.RunningAppProcessInfo.IMPORTANCE_FOREGROUND) {
                    {
                        topClassName = processInfo.processName;
                    }
                }
            }
        }
        return topClassName;
    }


    public static boolean isForegroundApp(Context context) {
        return TextUtils.equals(getForegroundActivityName(context), context.getPackageName());
    }

    /**
     * 获取时间段内，
     */
    public static long getTotalForegroundTime(Context context) {
        long END_TIME = System.currentTimeMillis();
        UsageStats usageStats = getPackageUsageStats(context, END_TIME - 60000, END_TIME);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            return usageStats != null ? usageStats.getTotalTimeInForeground() : 0;
        }
        return 0;
    }

    /**
     * 获取记录前台应用的UsageStats对象
     * <uses-permission android:name="android.permission.PACKAGE_USAGE_STATS"/>
     */
    private static UsageStats getForegroundUsageStats(Context context, long startTime, long endTime) {
        UsageStats usageStatsResult = null;
        if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            List<UsageStats> UsageStatsList = getUsageStatsList(context, startTime, endTime);
            if (UsageStatsList == null || UsageStatsList.isEmpty()) return null;
            for (UsageStats usageStats : UsageStatsList) {
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
        if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            List<UsageStats> UsageStatsList = getUsageStatsList(context, startTime, endTime);
            if (UsageStatsList == null || UsageStatsList.isEmpty()) return null;
            for (UsageStats usageStats : UsageStatsList) {
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
        if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            UsageStatsManager manager = (UsageStatsManager) context.getApplicationContext().getSystemService(Context.USAGE_STATS_SERVICE);
            //UsageStatsManager.INTERVAL_WEEKLY，UsageStatsManager的参数定义了5个，具体查阅源码
            List<UsageStats> UsageStatsList = manager.queryUsageStats(UsageStatsManager.INTERVAL_BEST, startTime, endTime);
            if (UsageStatsList == null || UsageStatsList.size() == 0) {// 没有权限，获取不到数据
                //Intent intent = new Intent(Settings.ACTION_USAGE_ACCESS_SETTINGS);
                //intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                //context.getApplicationContext().startActivity(intent);
                //Toast t = Toast.makeText(context,"need permmition to access settings .", Toast.LENGTH_LONG);
                //t.show();
                MMLog.log(TAG, "getUsageStatsList fail,need Settings.ACTION_USAGE_ACCESS_SETTINGS permission");
                return null;
            }
            return UsageStatsList;
        }
        return null;
    }

}
