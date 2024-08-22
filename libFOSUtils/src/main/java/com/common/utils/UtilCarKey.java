package com.common.utils;

import android.app.ActivityOptions;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.util.Log;

import com.zhuchao.android.fbase.MMLog;

import java.lang.reflect.Method;
import java.util.List;
import java.util.Objects;

public class UtilCarKey {
    private final static String TAG = "UtilCarKey";

    ///public static final String PROPERTIESFILE = MyCmd.VENDOR_DIR + ".properties_file";

    ///	private final static String CLD_GPS_NAME = "com.autonavi.amapauto";
    ///	private final static String CLD_GPS_CLASS_NAME = "com.autonavi.auto.remote.fill.UsbFillActivity";
    public static boolean mBtMusicInBTapk = false;

    static {
        String s = MachineConfig.getPropertyReadOnly(MachineConfig.KEY_BT_MUSIC_INSIDE);
        if (MachineConfig.VALUE_ON.equals(s)) {
            mBtMusicInBTapk = true;
        }
    }

    public static void doKeyAudio(Context context) {
        if (!AppConfig.CAR_UI_AUDIO.equals(AppConfig.getTopActivity())) {
            UtilSystem.doRunActivity(context, AppConfig.getCarAppPackageName(context), AppConfig.getCarAppActivityClassName(context,"audio.MusicActivity"));
        }
    }

    public static void doKeyTV(Context context) {
        if (!AppConfig.CAR_DTV.equals(AppConfig.getTopActivity())) {
            UtilSystem.doRunActivity(context, AppConfig.getCarAppPackageName(context), AppConfig.getCarAppActivityClassName(context,"tv.TVActivity"));
        }
    }

    public static boolean isAKVoiceAssistantEnabled(Context c) {
        try {
            String enable = SystemConfig.getProperty(c, SystemConfig.KEY_ENABLE_AK_VIOCE_ASSISTANT);
            return enable != null && (enable.equals("1") || enable.equals("true"));
        } catch (Exception e) {
            ///e.printStackTrace();
        }
        return false;
    }

    public static boolean isTXZViInstalled(Context c, String pkgName) {
        List<PackageInfo> packageInfoList = c.getPackageManager().getInstalledPackages(0);
        for (int i = 0; i < packageInfoList.size(); i++) {
            PackageInfo pkgInfo = packageInfoList.get(i);
            if ((pkgInfo.applicationInfo.flags & ApplicationInfo.FLAG_SYSTEM) == 1) {
                continue;
            }
            if (pkgInfo.applicationInfo.packageName != null) {
                if (pkgName.equals(pkgInfo.applicationInfo.packageName)) {
                    if (pkgInfo.versionName != null)//	&& pkgInfo.versionName.toLowerCase().indexOf("vi") >= 0) //by allen
                        return true;
                    break;
                }
            }
        }
        return false;
    }

    private final static String ZLINK_BROAST = "com.zjinnova.zlink";

    private static void sendKeyToZlink(Context context, int code) {
        Log.d("UtilCarKey", "sendKeyToZlink::" + code);

        Intent it = new Intent(ZLINK_BROAST);
        it.putExtra("command", "REQ_SPEC_FUNC_CMD");
        it.putExtra("specFuncCode", code);

        context.sendBroadcast(it);
    }

    public static void doKeyMic(Context context) {
        String s = MachineConfig.getProperty(MachineConfig.KEY_BT_TYPE);
        int mBTType = 0;
        if (s != null) {
            try {
                mBTType = Integer.parseInt(s);
            } catch (Exception ignored) {
            }
        }
        if (mBTType != MachineConfig.VAULE_BT_TYPE_PARROT) {
            boolean sendToCarPlay = false;
            String car_play = Util.getProperty("car_play_connect");
            Log.d("UtilCarKey", "car_play::" + car_play);
            if ("1".equals(car_play)) {
                //String top = AppConfig.getTopActivity();
                //if (top != null && top.contains("com.suding.speedplay")) {
                sendKeyToZlink(context, 1500);
                sendToCarPlay = true;
                //}
            }

            if (!sendToCarPlay) {
                if (isTXZViInstalled(context, "com.txznet.smartadapter")) {
                    try {
                        context.sendBroadcast(new Intent("txz.intent.action.smartwakeup.triggerRecordButton").setPackage("com.txznet.txz"));
                    } catch (Exception e) {
                        MMLog.e(TAG, String.valueOf(e));
                    }
                } else if (isAKVoiceAssistantEnabled(context)) {
                    if (!UtilSystem.doRunActivity(context, "com.ak.speechrecog", "com.ak.speechrecog.MainActivity")) {
                        UtilSystem.doRunActivity(context, "com.google.android.googlequicksearchbox", "com.google.android.googlequicksearchbox.VoiceSearchActivity");
                    }
                } else {
                    UtilSystem.doRunActivity(context, "com.google.android.googlequicksearchbox", "com.google.android.googlequicksearchbox.VoiceSearchActivity");
                }
            }
        } else {
            UtilSystem.doRunActivity(context, "com.my.bt", "com.my.bt.VoiceControlActivity");
        }
    }

    public static void doKeyMicEx(Context context) { // no parrot mic

        boolean sendToCarPlay = false;
        String car_play = Util.getProperty("car_play_connect");
        Log.d("UtilCarKey", "doKeyMicEx car_play::" + car_play);
        if ("1".equals(car_play)) {
            //String top = AppConfig.getTopActivity();
            //if (top != null && top.contains("com.suding.speedplay")) {
            sendKeyToZlink(context, 1500);
            sendToCarPlay = true;
            //}
        }

        if (!sendToCarPlay) {

            if (isTXZViInstalled(context, "com.txznet.smartadapter")) {
                try {
                    context.sendBroadcast(new Intent("txz.intent.action.smartwakeup.triggerRecordButton").setPackage("com.txznet.txz"));
                } catch (Exception e) {
                    MMLog.e(TAG, String.valueOf(e));
                }
            } else if (isAKVoiceAssistantEnabled(context)) {
                if (!UtilSystem.doRunActivity(context, "com.ak.speechrecog", "com.ak.speechrecog.MainActivity")) {
                    UtilSystem.doRunActivity(context, "com.google.android.googlequicksearchbox", "com.google.android.googlequicksearchbox.VoiceSearchActivity");
                }
            } else {
                UtilSystem.doRunActivity(context, "com.google.android.googlequicksearchbox", "com.google.android.googlequicksearchbox.VoiceSearchActivity");
            }
        }
    }

    public static void doKeyVideo(Context context) {
        if (!AppConfig.CAR_UI_VIDEO.equals(AppConfig.getTopActivity())) {
            UtilSystem.doRunActivity(context, AppConfig.getCarAppPackageName(context), "com.my.video.VideoActivity");
        }
    }

    public static void doKeyAuxIn(Context context) {
        if (!AppConfig.CAR_UI_AUX_IN.equals(AppConfig.getTopActivity())) {
            UtilSystem.doRunActivity(context, AppConfig.getCarAppPackageName(context), "com.my.auxplayer.AUXPlayer");
        }
    }

    public static void doKeyBTMusic(Context context) {
        if (!mBtMusicInBTapk) {
            doKeyBTMusicAlone(context);
        } else {
            doKeyBTMusicInBT(context);
        }
    }

    public static void doKeyBTMusicAlone(Context context) {
        if (!AppConfig.CAR_UI_BT_MUSIC.equals(AppConfig.getTopActivity())) {
            UtilSystem.doRunActivity(context, AppConfig.getCarAppPackageName(context), AppConfig.getCarAppActivityClassName(context, "btmusic.BTMusicActivity"));
        }
    }

    public static void doKeyBTMusicInBT(Context context) {
        try {
            Intent it = new Intent(Intent.ACTION_VIEW);
            it.setClassName("com.my.bt", "com.my.bt.ATBluetoothActivity");
            it.putExtra("music", 1);
            it.setFlags(Intent.FLAG_ACTIVITY_BROUGHT_TO_FRONT | Intent.FLAG_ACTIVITY_NEW_TASK);
            context.startActivity(it);
        } catch (Exception e) {
            Log.e(TAG, e.getMessage());
        }
    }

    public static void doKeyDVD(Context context) {
        if (!AppConfig.isUSBDvd()) {
            if (!AppConfig.CAR_UI_DVD.equals(AppConfig.getTopActivity())) {
                UtilSystem.doRunActivity(context, AppConfig.getCarAppPackageName(context), "com.my.dvd.DVDPlayer");
            }
        } else {
            if (!(AppConfig.getTopActivity().contains(AppConfig.USB_DVD))) {
                UtilSystem.doRunActivity(context, "com.car.dvdplayer", AppConfig.USB_DVD);
            }
        }
    }

    public static boolean doKeyBT(Context context) {
        if (!AppConfig.CAR_BT.equals(AppConfig.getTopActivity())) {
            UtilSystem.doRunActivity(context, "com.my.bt", "com.my.bt.ATBluetoothActivity");
            return true;
        }
        return false;

    }

    public static void doKeyEQ(Context context) {
        if (!AppConfig.CAR_EQ.equals(AppConfig.getTopActivity())) {
            UtilSystem.doRunActivity(context, "com.eqset", "com.eqset.EQActivity");
        }
    }

    public static void doKeyEQ2(Context context, int PageIndex) {
        //if (!AppConfig.CAR_EQ.equals(AppConfig.getTopActivity()))
        {
            try {
                Intent it = new Intent(Intent.ACTION_VIEW);
                it.setClassName("com.eqset", "com.eqset.EQActivity");
                it.putExtra("PageIndex", PageIndex);
                it.setFlags(Intent.FLAG_ACTIVITY_BROUGHT_TO_FRONT | Intent.FLAG_ACTIVITY_NEW_TASK);
                context.startActivity(it);

                MMLog.d("EQ", "doKeyEQ2 PageIndex=" + it.getIntExtra("PageIndex", 0));
            } catch (Exception e) {
                Log.e(TAG, Objects.requireNonNull(e.getMessage()));
            }
        }
    }

    public static boolean doKeyRadio(Context context) {
        if (!AppConfig.CAR_UI_RADIO.equals(AppConfig.getTopActivity())) {
            UtilSystem.doRunActivity(context, AppConfig.getCarAppPackageName(context), AppConfig.getCarAppActivityClassName(context, "radio.RadioActivity"));
            return true;
        }
        return false;
    }

    public static void doKeyAudioEx(Context context, String song) {
        try {
            Intent it = new Intent();
            it.setClassName("com.my.audio", "com.my.audio.MusicActivity");
            it.putExtra("song", song);
            it.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            context.startActivity(it);
        } catch (Exception e) {
            Log.e(TAG, Objects.requireNonNull(e.getMessage()));
        }
    }

    public static void doKeyDVR(Context context) {
        if (!"com.my.dvr".equals(AppConfig.getTopActivity())) {
            UtilSystem.doRunActivity(context, "com.my.dvr", "com.my.dvr.MainActivity");
        }
    }

    public static void doKeyPic(Context context) {
        if (!"com.my.gallery".equals(AppConfig.getTopActivity())) {
            UtilSystem.doRunActivity(context, "com.my.gallery", "com.my.gallery.GalleryActivity");
        }

    }

    public static void doKeySet(Context context) {
        if (!"com.android.settings".equals(AppConfig.getTopActivity())) {
            UtilSystem.doRunActivity(context, "com.android.settings", "com.android.settings.Settings");
        }

    }

    public static void doKeyPhoneConnect(Context context) {
        if (!"com.easyconn".equals(AppConfig.getTopActivity())) {

            UtilSystem.doRunActivity(context, "net.easyconn", "net.easyconn.WelcomeActivity");
        }

    }

    public static void doKeyCarHome(Context context) {
        // if (!"com.my.video".equals(AppConfig.getTopActivity())) {
        // UtilSystem.doRunActivity(context, "com.my.video",
        // "com.my.video.MusicActivity");
        // }
        Kernel.doKeyEvent(172);
    }

    /**
     * If the navigation is already displayed on DISPLAY1 then display to DEFAULT_DISPLAY.
     */
    private static boolean naviToDefaultDisplay(Context context, String pkgName, String clsName) {
        if (MachineConfig.getPropertyIntReadOnly(MachineConfig.KEY_DISPAUX_ENABLE) == 1) {
            String disp2Top = AppConfig.getTopActivity(1);
            // Log.d("allen", "disp2Top=" + disp2Top);
            if (disp2Top.startsWith(pkgName + "/")) {
                try {
                    Class<?> ClassAO = Class.forName("android.app.ActivityOptions");
                    Method method_makeBasic = ClassAO.getDeclaredMethod("makeBasic");
                    // options = ActivityOptions.makeBasic();
                    ActivityOptions options = (ActivityOptions) method_makeBasic.invoke(ClassAO);
                    if (options != null) {
                        Class<?> ClassO = options.getClass();
                        Method setLaunchDisplayId = ClassO.getMethod("setLaunchDisplayId", int.class);
                        setLaunchDisplayId.invoke(options, 0);
                        // options.setLaunchDisplayId(0);
                        Intent intent = new Intent();
                        intent.setClassName(pkgName, clsName);
                        context.startActivity(intent, options.toBundle());
                    }
                } catch (Exception e) {
                    Log.e("allen", "map to DEFAULT_DISPLAY failed: " + e);
                }
                return true;
            }
        }
        return false;
    }

    private static String mPreActivityBeforeGps = null;

    // public static MyDialog mMyDialog;
    public static boolean doKeyGpsEx(Context context, String errStiring) {

        boolean ret = false;

        String packageName = SystemConfig.getProperty(context, MachineConfig.KEY_GPS_PACKAGE);
        String className = SystemConfig.getProperty(context, MachineConfig.KEY_GPS_CLASS);

        //		Log.d("allen", "doKeyGpsEx: " + packageName + "/" + className);
        if (packageName == null || className == null) {
            packageName = MachineConfig.DEFAULT_GPS_PACKAGE;
            className = MachineConfig.DEFAULT_GPS_CLASS;
        } else {
            if (Util.isAndroidQ() && (Util.isPX30() || Util.isPX6()) && naviToDefaultDisplay(context, packageName, className)) return true;
        }

        if (AppConfig.getTopActivity().contains(packageName)) {
            boolean run = false;
            if (mPreActivityBeforeGps != null) {

                Intent it = new Intent();
                try {
                    String[] ss = mPreActivityBeforeGps.split("/");

                    if (ss[0].equals("com.anrdoid.launcher") || ss[0].equals("com.android.launcher3")) {
                        context.startActivity(new Intent(Intent.ACTION_MAIN).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP).addCategory(Intent.CATEGORY_HOME));
                    } else {
                        it.setClassName(ss[0], ss[1]);
                        it.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                        context.startActivity(it);
                    }

                    run = true;
                } catch (Exception e) {
                    ///e.printStackTrace();
                    MMLog.e(TAG, String.valueOf(e));
                }

            }

            if (!run) {//home
                context.startActivity(new Intent(Intent.ACTION_MAIN).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP).addCategory(Intent.CATEGORY_HOME));
            }
        } else {

            mPreActivityBeforeGps = AppConfig.getTopActivity();
            Intent it = new Intent();
            try {

                it.setClassName(packageName, className);
                it.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                context.startActivity(it);

                ret = true;
            } catch (Exception e) {
                e.printStackTrace();
            }

            if (!ret) {
                // Toast.makeText(context, errStiring,
                // Toast.LENGTH_SHORT).show();

                // com.my.factory.intent.action.GeneralSettings
                try {
                    it = new Intent();
                    it.setAction("com.my.factory.intent.action.GeneralSettings");
                    it.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    it.putExtra("navi", 1);
                    context.startActivity(it);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }

        return ret;
    }

    public static boolean doKeyGps(Context context, String errStiring) {
        return doKeyGpsEx(context, errStiring);
    }

    public static boolean doKeyGps(Context context) {
        return doKeyGpsEx(context, null);
    }

}
