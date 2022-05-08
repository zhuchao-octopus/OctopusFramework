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

import com.zhuchao.android.callbackevent.ApplicationChangedCallback;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class TAppUtils {
    private static final String TAG = "AppUtils";
    public static final String UNINSTALL_ACTION = "UNINSTALL";
    public static final String INSTALL_ACTION = "INSTALL";
    public static final String SCANING_ACTION = "SCANING";
    public static final String SCANING_COMPLETE_ACTION = "SCANINGCOMPLETE";

    private Context mContext;
    private ApplicationChangedCallback mApplicationChangedCallback = null;
    //private ExecutorService mExecutorService;
    private PackageManager mPackageManager;
    private List<AppInfo> AllAppInfo = null;
    //private List<AppInfor> UserAppInfors = new ArrayList<AppInfor>();
    //private List<String> Filter = new ArrayList<String>();


    public TAppUtils(Context context) {
        mContext = context;
        mApplicationChangedCallback = null;
        mPackageManager = mContext.getPackageManager();
        AllAppInfo = new ArrayList<AppInfo>();
        //mExecutorService = Executors.newSingleThreadExecutor();
        updateApplicationInfo();
    }

    public List<AppInfo> getAllAppInfo() {
        return AllAppInfo;
    }

    private synchronized void getAllAppsInfor() {
        AllAppInfo.clear();
        //获得所有的安装包
        List<PackageInfo> installedPackages = mPackageManager.getInstalledPackages(0);
        //遍历每个安装包，获取对应的信息
        for (PackageInfo packageInfo : installedPackages) {
            final AppInfo appInfo = new AppInfo();
            appInfo.setApplicationInfo(packageInfo.applicationInfo);
            appInfo.setVersionCode(packageInfo.versionCode);
            appInfo.setVersionCodeName(packageInfo.versionName);
            //得到icon
            Drawable drawable = packageInfo.applicationInfo.loadIcon(mPackageManager);
            appInfo.setIcon(drawable);
            //得到程序的名字
            String apkName = packageInfo.applicationInfo.loadLabel(mPackageManager).toString();
            appInfo.setName(apkName);
            //得到程序的包名
            String packageName = packageInfo.packageName;
            appInfo.setPackageName(packageName);
            //得到程序的资源文件夹
            String sourceDir = packageInfo.applicationInfo.sourceDir;
            appInfo.setFilePath(sourceDir);
            File file = new File(sourceDir);
            //得到apk的大小
            long size = file.length();
            appInfo.setSize(size);
            //获取到安装应用程序的标记
            int flags = packageInfo.applicationInfo.flags;
            if ((flags & ApplicationInfo.FLAG_SYSTEM) != 0) {
                //表示系统app
                appInfo.setUserApp(false);
            } else {
                //表示用户app
                appInfo.setUserApp(true);
            }
            if ((flags & ApplicationInfo.FLAG_EXTERNAL_STORAGE) != 0) {
                //表示在sd卡
                appInfo.setRom(false);
            } else {
                //表示内存
                appInfo.setRom(true);
            }
            if (!AllAppInfo.contains(appInfo)) {
                AllAppInfo.add(appInfo);
                callUserCallback(packageName);
            }
        }//for
        callUserCallback(null);
    }

    public void registerApplicationsReceiver() {
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
            //e.printStackTrace();
            MMLog.log(TAG, e.toString());
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
            //MMLog.log(TAG, "BroadcastReceiver:" + intent.getAction());
            switch (action) {
                case Intent.ACTION_PACKAGE_ADDED:
                case Intent.ACTION_PACKAGE_REMOVED:
                case Intent.ACTION_PACKAGE_REPLACED: {
                    updateApplicationInfo();
                }
            }
        }
    };

    private void updateApplicationInfo() {
        ThreadUtils.runThread(new Runnable() {
            @Override
            public void run() {
                getAllAppsInfor();
            }
        });
    }

    private void callUserCallback(String packageName) {
        if ((mApplicationChangedCallback != null)) {
            ThreadUtils.runOnMainUiThread(new Runnable() {
                @Override
                public void run() {
                    mApplicationChangedCallback.onApplicationChanged(SCANING_COMPLETE_ACTION, packageName);
                }
            });
        }
    }

    public void setApplicationChangedListener(ApplicationChangedCallback applicationChangedCallback) {
        mApplicationChangedCallback = applicationChangedCallback;
    }

    public void free() {
        unRegAppReceiver();
        AllAppInfo.clear();
    }

    public AppInfo getAppInfo(String packageName) {
        for (AppInfo Info : AllAppInfo) {
            if (Info == null) continue;
            if (packageName == null) return null;
            if (packageName.equals(Info.getPackageName()))
                return Info;
        }
        return null;
    }

    public AppInfo getAppInfoByName(String Name) {
        for (AppInfo Info : AllAppInfo) {
            if (Info.getName().equals(Name))
                return Info;
        }
        return null;
    }

    public boolean existApp(String packageName) {
        if (getAppInfo(packageName) != null)
            return true;
        else
            return false;
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

    public static boolean isAppInstalled(Context context, String packageName) {
        if (EmptyString(packageName))
            return false;
        try {
            ApplicationInfo info = context.getPackageManager().getApplicationInfo(packageName, 0);
            return info.enabled;
        } catch (PackageManager.NameNotFoundException e) {
            //e.printStackTrace();
            return false;
        }
    }

    public static boolean startApp(Context context, String packageName) {
        if (EmptyString(packageName)) {
            return false;
        }
        Intent intent = context.getPackageManager().getLaunchIntentForPackage(packageName);
        if (intent != null) {
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            context.startActivity(intent);
            MMLog.log(TAG, "LaunchApp>>>>" + packageName);
            return true;
        } else {
            MMLog.log(TAG, "LaunchApp not found>>>>" + packageName);
            return false;
        }
    }

    public static void install(Context context, String filePath) {
        Intent intent = new Intent(Intent.ACTION_VIEW);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            intent.setFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
            Uri contentUri = FileProvider.getUriForFile(context, BuildConfig.LIBRARY_PACKAGE_NAME + ".fileProvider", new File(filePath));
            intent.setDataAndType(contentUri, "application/vnd.android.package-archive");
        } else {
            intent.setDataAndType(Uri.fromFile(new File(filePath)), "application/vnd.android.package-archive");
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        }
        context.startActivity(intent);
    }

    public static void uninstall(Context context, String packageName) {
        Uri uri = Uri.parse("package:" + packageName);
        Intent intent = new Intent(Intent.ACTION_DELETE, uri);
        context.startActivity(intent);
    }

    public static boolean isForegroundApp(Context context) {
        return TextUtils.equals(getForegroundActivityName(context), context.getPackageName());
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


   public static class AppInfo {
        private ApplicationInfo applicationInfo;
        private String packageName;
        private String name;
        private Drawable icon;
        private long size;
        private boolean isUserApp;//用户APP 还是系统APP
        private boolean isRom; //安装位置
        private String filePath;//文件位置
        private long versionCode = 0;
        private String versionCodeName = "0";

        public AppInfo() {
        }

       public AppInfo(String name, Drawable icon) {
           this.name = name;
           this.icon = icon;
       }

       public ApplicationInfo getApplicationInfo() {
            return applicationInfo;
        }

        public void setApplicationInfo(ApplicationInfo applicationInfo) {
            this.applicationInfo = applicationInfo;
        }

        public String getPackageName() {
            return packageName;
        }

        public void setPackageName(String packageName) {
            this.packageName = packageName;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public Drawable getIcon() {
            return icon;
        }

        public void setIcon(Drawable icon) {
            this.icon = icon;
        }

        public long getSize() {
            return size;
        }

        public void setSize(long size) {
            this.size = size;
        }

        public boolean isUserApp() {
            return isUserApp;
        }

        public void setUserApp(boolean userApp) {
            this.isUserApp = userApp;
        }

        public boolean isRom() {
            return isRom;
        }

        public void setRom(boolean rom) {
            this.isRom = rom;
        }

        public String getFilePath() {
            return filePath;
        }

        public void setFilePath(String filePath) {
            this.filePath = filePath;
        }

        public long getVersionCode() {
            return versionCode;
        }

        public void setVersionCode(long versionCode) {
            this.versionCode = versionCode;
        }

        public String getVersionCodeName() {
            return versionCodeName;
        }

        public void setVersionCodeName(String versionCodeName) {
            this.versionCodeName = versionCodeName;
        }

        @Override
        public String toString() {
            String str = "Name=" + name +
                    ",Version=" + versionCode +
                    ",PackageName=" + packageName +
                    ",Ico=" + icon +
                    ",Size=" + size +
                    ",User=" + isUserApp +
                    ",Rom=" + isRom +
                    ",Path=" + filePath;
            return str;
        }
    }
}
