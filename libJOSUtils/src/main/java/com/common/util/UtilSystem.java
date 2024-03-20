package com.common.util;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;


import android.app.Activity;
import android.app.ActivityManager;
import android.app.ActivityManager.RunningTaskInfo;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.Build;
import android.os.Environment;
import android.os.Build.VERSION;
import android.os.Build.VERSION_CODES;
import android.os.storage.StorageManager;
import android.provider.Settings;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;

public class UtilSystem {
    private final static String TAG = "UtilSystem";


    public static void setStatusBarTransparent(Activity context) {
        if (VERSION.SDK_INT >= VERSION_CODES.LOLLIPOP) {
            Window window = context.getWindow();
            window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS | WindowManager.LayoutParams.FLAG_TRANSLUCENT_NAVIGATION);
            window.getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION | View.SYSTEM_UI_FLAG_LAYOUT_STABLE);
            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
            window.setStatusBarColor(Color.TRANSPARENT);
            window.setNavigationBarColor(Color.TRANSPARENT);
        }
    }

    public static boolean isTopActivity(Context context, String packageName) { // < 4.3 can used
        ActivityManager activityManager = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
        List<RunningTaskInfo> tasksInfo = activityManager.getRunningTasks(1);
        if (tasksInfo.size() > 0) {

            if (packageName.equals(tasksInfo.get(0).topActivity.getPackageName())) {
                return true;
            }
        }
        return false;
    }

    public static boolean doRunActivity(Context context, String pacageName, String name) {
        boolean ret = false;
        try {
            Intent it = new Intent(Intent.ACTION_VIEW);
            it.setClassName(pacageName, name);
            it.setFlags(Intent.FLAG_ACTIVITY_BROUGHT_TO_FRONT | Intent.FLAG_ACTIVITY_NEW_TASK);
            context.startActivity(it);
            ret = true;
        } catch (Exception e) {
            Log.e(TAG, e.getMessage());
        }
        return ret;
    }

    public static void doRunActivity(Context context, String intent_name) {
        try {
            Intent it = new Intent(intent_name);
            it.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            context.startActivity(it);
        } catch (Exception e) {
            Log.e(TAG, e.getMessage());
        }
    }

    public static void killProcess(String name) {

        try {

            Process psProcess = Runtime.getRuntime().exec("ps | grep " + name);

            psProcess.waitFor();

            InputStream inputStream = psProcess.getInputStream();
            InputStreamReader buInputStreamReader = new InputStreamReader(inputStream);
            BufferedReader bufferedReader = new BufferedReader(buInputStreamReader);
            String str = null;

            str = bufferedReader.readLine();
            str = bufferedReader.readLine();


            String[] ss = str.split(" ");

            int j = 0;
            int i = 0;
            for (; i < ss.length; ++i) {
                if (ss[i].length() > 0) {
                    ++j;
                }
                if (j == 2) {
                    break;
                }
            }
            //			int memory = Integer.parseInt(ss[i]);

            str = ss[i];

            Util.sudoExec("kill:" + str);

        } catch (Exception e) {

        }
    }
    //	public static void sendActivityStatus(Context context, int status) {
    //		Intent i = new Intent(MyCmd.BROADCAST_ACTIVITY_STATUS);
    //		i.putExtra(MyCmd.EXTRA_COMMON_CMD, context.getPackageName());
    //		i.putExtra(MyCmd.EXTRA_COMMON_DATA, status);
    //		context.sendBroadcast(i);
    //
    //	}

    public static class StorageInfo {
        public String mPath;
        public String mState;
        public int mType;

        public final static int TYPE_INTERAL = 0;
        public final static int TYPE_SD = 1;
        public final static int TYPE_USB = 2;


        public StorageInfo(String path, int type) {
            mPath = path;
            mType = type;
        }

    }

    public static List<StorageInfo> listAllStorage(Context context) {
        ArrayList<StorageInfo> storages = new ArrayList<StorageInfo>();
        if (Build.VERSION.SDK_INT >= 25) {

            StorageManager storageManager = (StorageManager) context.getSystemService(Context.STORAGE_SERVICE);
            try {
                Class<?>[] paramClasses = {};
                Method getVolumeList = StorageManager.class.getMethod("getVolumes", paramClasses);
                Object[] params = {};
                List<Object> VolumeInfo = (List<Object>) getVolumeList.invoke(storageManager, params);

                if (VolumeInfo != null) {
                    for (Object volumeinfo : VolumeInfo) {

                        Method getPath = volumeinfo.getClass().getMethod("getPath", new Class[0]);

                        File path = (File) getPath.invoke(volumeinfo, new Object[0]);

                        Method getDisk = volumeinfo.getClass().getMethod("getDisk", new Class[0]);

                        Object diskinfo = getDisk.invoke(volumeinfo, new Object[0]);
                        int type = StorageInfo.TYPE_INTERAL;
                        if (diskinfo != null) {
                            Method isSd = diskinfo.getClass().getMethod("isSd", new Class[0]);

                            type = ((Boolean) isSd.invoke(diskinfo, new Object[0])) ? StorageInfo.TYPE_SD : StorageInfo.TYPE_USB;
                            if (path != null) {
                                StorageInfo si = new StorageInfo(path.toString(), type);
                                storages.add(si);
                            }
                        }

                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
            storages.trimToSize();
        } else {
            String[] path = {"/storage/usbdisk1/", "/storage/usbdisk2/", "/storage/usbdisk3/", "/storage/usbdisk4/", "/storage/sdcard1/", "/storage/sdcard2/"};
            for (String s : path) {
                File sdcardDir = new File(s);
                String state = Environment.getExternalStorageState(sdcardDir);
                if (Environment.MEDIA_MOUNTED.equals(state)) {
                    int type = StorageInfo.TYPE_USB;
                    if (s.contains("sdcard")) {
                        type = StorageInfo.TYPE_SD;
                    }
                    StorageInfo si = new StorageInfo(s, type);
                    storages.add(si);
                }
            }

        }
        return storages;
    }

    public static List<StorageInfo> listAllStorage(Context context, int filtFlag) {
        ArrayList<StorageInfo> storages = new ArrayList<StorageInfo>();
        if (Build.VERSION.SDK_INT >= 25) {
            StorageManager storageManager = (StorageManager) context.getSystemService(Context.STORAGE_SERVICE);
            try {
                Class<?>[] paramClasses = {};
                Method getVolumeList = StorageManager.class.getMethod("getVolumes", paramClasses);
                Object[] params = {};
                List<Object> VolumeInfo = (List<Object>) getVolumeList.invoke(storageManager, params);
                if (VolumeInfo != null) {
                    for (Object volumeinfo : VolumeInfo) {
                        Method getPath = volumeinfo.getClass().getMethod("getPath", new Class[0]);
                        File path = (File) getPath.invoke(volumeinfo, new Object[0]);
                        Method getDisk = volumeinfo.getClass().getMethod("getDisk", new Class[0]);

                        Object diskinfo = getDisk.invoke(volumeinfo, new Object[0]);
                        int type = StorageInfo.TYPE_INTERAL;
                        if (diskinfo != null) {
                            Method isSd = diskinfo.getClass().getMethod("isSd", new Class[0]);
                            type = ((Boolean) isSd.invoke(diskinfo, new Object[0])) ? StorageInfo.TYPE_SD : StorageInfo.TYPE_USB;
                            if (path != null) {
                                if ((type == StorageInfo.TYPE_INTERAL && (filtFlag & (1 << StorageInfo.TYPE_INTERAL)) != 0) || (type == StorageInfo.TYPE_SD && (filtFlag & (1 << StorageInfo.TYPE_SD)) != 0) || (type == StorageInfo.TYPE_USB && (filtFlag & (1 << StorageInfo.TYPE_USB)) != 0)) {
                                    StorageInfo si = new StorageInfo(path.toString(), type);
                                    storages.add(si);
                                }
                            }
                        }
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
            storages.trimToSize();
        } else {
            String[] path = {"/storage/usbdisk1/", "/storage/usbdisk2/", "/storage/usbdisk3/", "/storage/usbdisk4/", "/storage/sdcard1/", "/storage/sdcard2/"};
            for (String s : path) {
                File sdcardDir = new File(s);
                String state = Environment.getExternalStorageState(sdcardDir);
                if (Environment.MEDIA_MOUNTED.equals(state)) {
                    int type = StorageInfo.TYPE_USB;
                    if (s.contains("sdcard")) {
                        type = StorageInfo.TYPE_SD;
                    }
                    StorageInfo si = new StorageInfo(s, type);
                    storages.add(si);
                }
            }
        }
        return storages;
    }
}
