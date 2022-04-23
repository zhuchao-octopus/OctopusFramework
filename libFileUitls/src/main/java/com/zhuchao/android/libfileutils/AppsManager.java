package com.zhuchao.android.libfileutils;

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
import android.os.Environment;
import android.text.TextUtils;

import androidx.core.content.FileProvider;

import com.zhuchao.android.callbackevent.AppsCallback;
import com.zhuchao.android.libfileutils.bean.AppInfor;
import com.zhuchao.android.utils.MMLog;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;


public class AppsManager {
    private static final String TAG = "MyAppsManager";
    public static final String UNINSTALL_ACTION = "UNINSTALL";
    public static final String INSTALL_ACTION = "INSTALL";
    public static final String SCANING_ACTION = "SCANING";
    public static final String SCANING_COMPLETE_ACTION = "SCANINGCOMPLETE";
    public static final String ADDTOMYAPPS_ACTION = "ADDTOMYAPPS";
    public static final String DELFROMMYAPPS_ACTION = "DELFROMMYAPPS";
    public static final String USED_HISTORY_ACTION = "USEDHISTORY";
    public static final String HOT_CLEAR_ACTION = "HOTCLEAR";
    //private static MyAppsManager mInstance;
    private static Context mContext;
    //private static Object mLock = new Object();
    private AppsCallback mAppsCallback = null;
    private ExecutorService mExecutorService;
    private PackageManager mPackageManager;

    private List<AppInfor> AllAppInfors = new ArrayList<AppInfor>();
    private List<AppInfor> UserAppInfors = new ArrayList<AppInfor>();
    //private List<AppInfor> MyAppInfors = new ArrayList<AppInfor>();
    //private List<String> RecentlyUsed = new ArrayList<String>();
    private List<String> Filter = new ArrayList<String>();

//    public static MyAppsManager getInstance(Context context){
//         if (mInstance == null){
//             synchronized (mLock){
//                 if (mInstance == null){
//                     mContext = context;
//                     mInstance = new MyAppsManager(mContext);
//                 }
//             }
//         }
//        return mInstance;
//    }

    public AppsManager(Context context, AppsCallback appsCallback) {
        mContext = context;
        mAppsCallback = appsCallback;
        mPackageManager = mContext.getPackageManager();
        mExecutorService = Executors.newSingleThreadExecutor();
        UpdateAppsInfor();
        registerAppsReceiver();
        MMLog.log(TAG, "Init>>>>>>>>>>>>>>>>>>>>>>>>>>");
    }

    public List<String> getFilter() {
        return Filter;
    }

    public void Free() {
        unRegAppReceiver();
        AppChangedReceiver = null;
    }

    //public List<String> getRecentlyUsed() {
    //    return RecentlyUsed;
    //}

    public List<AppInfor> getAllAppInfors() {
        return AllAppInfors;
    }

    public AppsManager setmAppsChangedCallback(AppsCallback mAppsCallback) {
        this.mAppsCallback = mAppsCallback;
        return this;
    }

    /**
     * 获取已安装apk的列表
     */
    private void UpdateAppsInfor() {
        Runnable runnable = new Runnable() {
            @Override
            public void run() {
                getAllAppsInfos();
            }
        };
        mExecutorService.submit(runnable);
    }

    private synchronized void getAllAppsInfos() {

        UserAppInfors.clear();
        AllAppInfors.clear();
        //获得所有的安装包
        List<PackageInfo> installedPackages = mPackageManager.getInstalledPackages(0);
        int size0 = AllAppInfors.size();
        //遍历每个安装包，获取对应的信息
        for (PackageInfo packageInfo : installedPackages) {

            final AppInfor appInfor = new AppInfor();

            appInfor.setApplicationInfo(packageInfo.applicationInfo);
            appInfor.setVersionCode(packageInfo.versionCode);

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

            if (!AllAppInfors.contains(appInfor)) {
                AllAppInfors.add(appInfor);

                if (mAppsCallback != null)
                    mAppsCallback.OnAppsChanged(SCANING_ACTION, appInfor.toString());

            }

            boolean cached = false;
            for (String pkg : Filter) {
                if (pkg.equals(appInfor.getPackageName())) {
                    cached = true;
                    break;
                }
            }
            if (cached == false) {
                Intent intent = mPackageManager.getLaunchIntentForPackage(appInfor.getPackageName());
                if (intent != null)
                    UserAppInfors.add(appInfor);
            }
        }//for
        //&& size0 != AllAppInfors.size()
        if ((mAppsCallback != null)) {
            mAppsCallback.OnAppsChanged(SCANING_COMPLETE_ACTION, null);
        }

        //int i = 0;
        //for (AppInfor Info : AllAppInfors) {
        //    MLog.log(TAG, "Apps[" + i + "]:" + Info.toString());
        //    i++;
        //}
        //String myapp = SPreference.getSharedPreferences(mContext, "MyAppInfors", "MyAppInfors");
        //AppInfor appInfor = getAppInfor(myapp);
        //if (appInfor != null) {
        //    addToMyApp(appInfor);
        //}
    }

    public boolean isTheAppExist(String packageName) {
        if (TextUtils.isEmpty(packageName))
            return false;
        try {
            ApplicationInfo info = mContext.getPackageManager().getApplicationInfo(packageName, 0);
            return true;
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
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
            e.printStackTrace();
        }
    }

    private BroadcastReceiver AppChangedReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {

            String action = intent.getAction();

            MMLog.log(TAG, "BroadcastReceiver:" + intent.getAction());

            if (action.equals(Intent.ACTION_PACKAGE_ADDED)) {
                UpdateAppsInfor();
            }
            if (action.equals(Intent.ACTION_PACKAGE_REMOVED)) {
                UpdateAppsInfor();
            }
            if (action.equals(Intent.ACTION_PACKAGE_REPLACED)) {
                UpdateAppsInfor();
            }
        }
    };

    public AppInfor getAppInfor(String packageName) {
        for (AppInfor Info : AllAppInfors) {
            if (Info == null) continue;
            if (packageName == null) return null;
            if (packageName.equals(Info.getPackageName()))
                return Info;
        }
        return null;
    }

    public AppInfor getAppInfor(int id) {
        if (AllAppInfors.size() == 0) {
            return null;
        }
        if (id < 0) {
            return null;
        }
        if (id >= AllAppInfors.size()) {
            return null;
        }

        return AllAppInfors.get(id);
    }

    public AppInfor getAppInforByName(String Name) {
        for (AppInfor Info : AllAppInfors) {
            if (Name.equals(Info.getName()))
                return Info;
        }
        return null;
    }

    public boolean startTheApp(AppInfor infor) {

        if (TextUtils.isEmpty(infor.getPackageName())) {
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

    public boolean startTheApp(String packageName) {
        if (TextUtils.isEmpty(packageName)) {
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

    public boolean startTheApp(int id) {
        if (AllAppInfors.size() == 0) {
            return false;
        }
        if (id < 0) {
            return false;
        }
        if (id >= AllAppInfors.size()) {
            return false;
        }
        String packageName = AllAppInfors.get(id).getPackageName();
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

    public boolean startTheAppByName(String name) {
        if (TextUtils.isEmpty(name)) {
            return false;
        }
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

    public String getDownloadDir() {
        String path = Environment.getExternalStorageDirectory().getAbsolutePath() + "/MyDownload/";
        File file = new File(path);
        if (!file.exists()) {
            file.mkdirs();
        }
        return path;
    }

    public static Drawable getDrawable(Context context, String pkgName) {
        PackageManager mPm = context.getPackageManager();
        try {
            ApplicationInfo info = mContext.getPackageManager().getApplicationInfo(pkgName, 0);
            //String label = (String)info.loadLabel(mPm);//应用名
            Drawable icon = info.loadIcon(mPm);//应用icon
            return icon;
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
        return null;
    }

}
