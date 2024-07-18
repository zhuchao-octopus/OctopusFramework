package com.common.util;

import static com.zhuchao.android.TPlatform.systemPropertiesGet;

import android.app.WallpaperManager;
import android.content.Context;
import android.graphics.drawable.Drawable;
import android.media.AudioManager;
import android.view.View;

import com.zhuchao.android.fbase.MMLog;
import com.zhuchao.android.fbase.TAppUtils;

import java.io.BufferedReader;
import java.io.FileReader;
import java.util.Arrays;
import java.util.HashSet;
//import android.os.SystemProperties;

public class AppConfig {
    private final static String TAG = "AppConfig";

    public static final int MEDIA_SOUND_CHANNEL = AudioManager.STREAM_MUSIC;

    // public static final int NAVI_SOUND_CHANNEL =
    // AudioManager.STREAM_NOTIFICATION;
    // public static final int IFLY_SOUND_CHANNEL = AudioManager.STREAM_MUSIC;;

    public static final int MEDIA_SOUND_VOLUME = 15;

    public static final int[] IFLY_SOUND_STEP = new int[]{8, 9, 11, 13};
    public static final int[] MAP_SOUND_STEP = new int[]{1, 2, 3, 4};

    public static final int SOUND_DEFAULT_STEP = 2;

    public static final String SETTINGS_KEY_SOUND_DEFAULT_STEP = "key_gps_volume";
    public static final String SETTINGS_KEY_MEDIA_VOLUME = "key_media_volume";
    public final static String SETTINGS_KEY_DVR_RECORD_SOUND = "key_dvr_record_sound";

    public static String mCarAppsPackageName = null;
    public static String mLauncherPackage = null;
    public static String mCarServicePackageName = null;

    //public final static String PACKAGE_CAR_SERVICE = "com.my.out";
    public final static String PACKAGE_RADIO = "com.my.radio";
    public final static String PACKAGE_AUDIO = "com.my.audio";
    public final static String PACKAGE_EQ = "com.eqset";
    public final static String PACKAGE_LAUNCHER = "com.android.launcher";

    public final static String CARUI_PATH = "/system/etc/carui/";
    public final static String CARUI_BACKGROUND = CARUI_PATH + "background.png";

    public final static String[] PACKAGE_SAVE_DRIVE = {"com.car.ui", "com.my.filemanager", "com.canboxsetting"};
    public final static String[] PACKAGE_NAME = {"com.car.ui", "com.eqset", "com.android.settings", "com.my.audio", "com.my.video", "com.my.setting", "com.my.bt", "net.easyconn", "com.my.out", "com.my.filemanager", "com.canboxsetting", "com.focussync"};
    public final static String CAR_UI = PACKAGE_NAME[0];

    public final static String CAR_UI_AUDIO = "com.car.ui/com.my.audio.MusicActivity";
    public final static String CAR_UI_VIDEO = "com.car.ui/com.my.video.VideoActivity";
    public final static String CAR_UI_DVD = "com.car.ui/com.my.dvd.DVDPlayer";
    public final static String CAR_UI_AUX_IN = "com.car.ui/com.my.auxplayer.AUXPlayer";
    public final static String CAR_UI_FRONT_CAMERA = "com.car.ui/com.my.frontcamera.FrontCameraActivity";
    public final static String CAR_UI_RADIO = "com.car.ui/com.my.radio.RadioActivity";
    public final static String CAR_UI_BT_MUSIC = "com.car.ui/com.my.btmusic.BTMusicActivity";
    public final static String CAR_BT = "com.my.bt/com.my.bt.ATBluetoothActivity";
    public final static String CAR_EQ = "com.eqset/com.eqset.EQActivity";
    public final static String CAR_AUXIN = "com.car.ui/com.my.auxplayer.AUXPlayer";
    public final static String CAR_DTV = "com.car.ui/com.my.tv.TVActivity";
    public final static String USB_DVD = "com.car.dvdplayer.DVDPlayerActivity";

    public static String getLauncherPackage() {
        if (mLauncherPackage == null) {
            String s = MachineConfig.getPropertyReadOnly(MachineConfig.KEY_HIDE_LAUNCHER);
            if (s == null || !s.contains("Launcher2")) {
                mLauncherPackage = "com.android.launcher";
            } else {
                mLauncherPackage = "com.android.launcher3";
            }
        }
        return mLauncherPackage;
    }

    public static String getCarAppPackageName(Context context) {
        if (mCarAppsPackageName == null) {
            if (TAppUtils.isAppInstalled(context, "com.octopus.android.carapps")) mCarAppsPackageName = "com.octopus.android.carapps";
            else mCarAppsPackageName = "com.car.ui";
        }
        return mCarAppsPackageName;
    }

    public static String getCarAppActivityClassName(Context context, String activityName) {
        getCarAppPackageName(context);
        if ("com.octopus.android.carapps".equals(mCarAppsPackageName)) return mCarAppsPackageName + "." + activityName;
        else return "com.my." + activityName;
    }

    public static String getCarServicePackageName(Context context) {
        if (mCarServicePackageName == null) {
            if (TAppUtils.isAppInstalled(context, "com.zhuchao.android.car")) mCarServicePackageName = "com.zhuchao.android.car";
            else mCarServicePackageName = "com.my.out";
        }
        return mCarServicePackageName;
    }

    // public final static String CAR_UI_ = PACKAGE_NAME[0];
    public static boolean isPackageSaveSpecApp(String packageName) {
        for (String s : PACKAGE_SAVE_DRIVE) {
            if (packageName.startsWith(s)) return true;
        }
        return false;
    }

    public static boolean isSystemApp(String packageName) {
        for (String s : PACKAGE_NAME) {
            if (packageName.startsWith(s)) return true;
        }
        return false;
    }

    public final static String[] PACKAGE_NAME_LAUNCHER_BACKGROUND = {

            "com.car.ui", "com.eqset",
            /* "com.android.settings", */
            "com.my.bt", "com.my.dvr",
            /* "com.my.tv", */
            "com.SwcApplication",
            /* "com.canboxsetting", */
            /*
             * "com.android.browser", "com.android.gallery3d",
             */
            /*
             * "com.my.filemanager", "com.focussync" , "com.my.instructions",
             * "com.android.calculator2", "com.android.deskclock",
             */

    };

    public static boolean isNoNeedLauncherBackground(String packageName) {

        for (String s : PACKAGE_NAME_LAUNCHER_BACKGROUND) {
            if (packageName.startsWith(s)) {
                return true;
            }
        }
        return false;
    }

    public static boolean isNoNeedLauncherBackground(String packageName, String activityName) {
        for (String s : PACKAGE_NAME_LAUNCHER_BACKGROUND) {
            if (packageName.startsWith(s)) {
                if (packageName.startsWith("com.my.bt")) {
                    if (activityName.startsWith("com.my.bt.VoiceControlActivity")) {
                        return false;
                    } else {
                        return true;
                    }
                } else {
                    return true;
                }
            }
        }
        return false;
    }

    public static boolean isHomeApp(String packageName) {
        if (packageName.startsWith(PACKAGE_NAME[0])) {
            return true;
        }
        return false;
    }

    public static boolean isMusicApp(String packageName) {
        if (packageName.startsWith(PACKAGE_NAME[2])) {
            return true;
        }
        return false;
    }

    public static boolean isDVRApp(String packageName) {
        if (packageName.startsWith(PACKAGE_NAME[1])) {
            return true;
        }
        return false;
    }

    public static boolean isVideoApp(String packageName) {
        if (packageName.startsWith(PACKAGE_NAME[3])) {
            return true;
        }
        return false;
    }

    public static boolean isGpsApp(Context context, String packageName) {
        String gps = SystemConfig.getProperty(context, MachineConfig.KEY_GPS_PACKAGE);
        if (gps == null) {
            gps = MachineConfig.DEFAULT_GPS_PACKAGE;
        }

        if (packageName != null && packageName.startsWith(gps)) {
            return true;
        }
        return false;
    }

    public static String getTopActivity() {

        String topPackageName = "";
        if (Util.isAndroidLaterP()) {
            try {
                //topPackageName = SystemProperties.get("ak.status.public_top_activity");
                topPackageName = systemPropertiesGet("ak.status.public_top_activity");
            } catch (Exception e) {
                MMLog.e(TAG, "getTopActivity Exception: " + e);
            }
        } else {
            FileReader fr = null;
            try {
                fr = new FileReader("/sys/class/ak/mcu/public_top_activity");
                BufferedReader reader = new BufferedReader(fr, 256);
                topPackageName = reader.readLine();
                reader.close();
                fr.close();
            } catch (Exception e) {
                MMLog.e(TAG, "getTopActivity exception: " + e);
            }
        }
        return topPackageName == null ? "" : topPackageName;
    }

    public static String getTopActivity(int displayId) {

        String topPackageName = "";
        if (Util.isAndroidLaterP()) {
            try {
                //topPackageName = SystemProperties.get("ak.status.public_top_activity" + (displayId > 0 ? displayId : ""));
                topPackageName = systemPropertiesGet("ak.status.public_top_activity" + (displayId > 0 ? displayId : ""));
            } catch (Exception e) {
                MMLog.e(TAG, "getTopActivity displayId:" + displayId + " Exception: " + e);
            }
        }
        return topPackageName == null ? "" : topPackageName;

    }

    private static final HashSet<String> mSetHideApp = new HashSet<String>();
    // these set for hide
    public static final String HIDE_APP_DVD = "DVD";
    public static final String HIDE_APP_AUX = "AUX";
    public static final String HIDE_USB_DVD = "USBDISC";
    public static final String HIDE_APP_FRONT_CMAERA = "FCAM";
    public static final String HIDE_APP_VIDEO_OUT = "VOUT";
    public static final String HIDE_APP_DVR = "DVR";
    public static final String HIDE_APP_VIOCE_CONTROL = "VOICE";

    public static final String HIDE_APP_JOYSTUDY = "JOYSTUDY";
    public static final String HIDE_APP_WHEELKEYSTUDY = "WHEELKEYSTUDY";

    public static final String HIDE_TPMS = "TPMS";

    public static final String HIDE_BACK_CAMERA = "BACKCAM";
    // these set for show
    public static final String HIDE_CANBOX_AC = "ac";
    public static final String HIDE_CANBOX_INFO = "info";
    public static final String HIDE_CANBOX_SET = "set";
    public static final String HIDE_CANBOX_CD = "cd";
    public static final String HIDE_CANBOX_TPMS = "tpms";
    public static final String HIDE_CANBOX_EQ = "eq";
    public static final String HIDE_CANBOX_SYNC = "sync";
    public static final String HIDE_CANBOX_RADIO = "radio";
    public static final String HIDE_CANBOX_COMPASS = "compass";
    public static final String HIDE_CANBOX_PLAYER = "player";
    public static final String HIDE_CANBOX_TIMESET = "time";
    public static final String HIDE_CANBOX_ANSTART = "anstart";
    public static final String HIDE_CANBOX_SEATHEAT = "seatheat";
    public static final String HIDE_CANBOX_360 = "360";
    private static int mUsbDvd = 0;

    private static void updateHideAppConboxVersion1(String value, int mCarType, String appHide) {
        boolean hideSync = true;
        boolean hideConboxSetting = true;
        boolean hideCanboxCarInfo = true;
        boolean hideCanboxAirControl = true;
        boolean hideTpms = true;
        boolean hide360 = true;
        boolean hideEQ = false;
        boolean hideCarCD = true;
        boolean hideCarRadio = true;
        boolean hideCarCompass = true;
        boolean hideCarPlayer = true;
        boolean hideTimeSet = true;
        boolean hideAnStart = true;
        boolean hideSeatHeat = true;

        if (null != value) {
            if (value.equals(MachineConfig.VALUE_CANBOX_FORD_SIMPLE) || value.equals(MachineConfig.VALUE_CANBOX_FORD_RAISE) || value.equals(MachineConfig.VALUE_CANBOX_FORD_EXPLORER_SIMPLE)) {
                if (value.equals(MachineConfig.VALUE_CANBOX_FORD_SIMPLE) || value.equals(MachineConfig.VALUE_CANBOX_FORD_RAISE)) {
                    hideCanboxCarInfo = false;
                }
                hideConboxSetting = false;
                if (1 != mCarType) {
                    hideSync = false;
                    if (mCarType == 2 || mCarType == 3) {
                        hideAuxin = true;
                    }
                }
            } else if (value.equals(MachineConfig.VALUE_CANBOX_VW) || value.equals(MachineConfig.VALUE_CANBOX_GM_SIMPLE) || value.equals(MachineConfig.VALUE_CANBOX_HONDA_RAISE) || value.equals(MachineConfig.VALUE_CANBOX_PSA_BAGOO) || value.equals(MachineConfig.VALUE_CANBOX_BMW_E90X1_UNION) || value.equals(MachineConfig.VALUE_CANBOX_TOYOTA) || value.equals(MachineConfig.VALUE_CANBOX_TOYOTA_RAISE) || value.equals(MachineConfig.VALUE_CANBOX_TOYOTA_BINARYTEK) || value.equals(MachineConfig.VALUE_CANBOX_ACCORD_BINARYTEK) || value.equals(MachineConfig.VALUE_CANBOX_ACCORD2013) || value.equals(MachineConfig.VALUE_CANBOX_PETGEO_RAISE) || value.equals(MachineConfig.VALUE_CANBOX_PETGEO_SCREEN_RAISE) || value.equals(MachineConfig.VALUE_CANBOX_LANDROVER_HAOZHENG) || value.equals(MachineConfig.VALUE_CANBOX_OUSHANG_RAISE) || value.equals(MachineConfig.VALUE_CANBOX_FIAT_EGEA_RAISE)) {
                hideConboxSetting = false;
                hideCanboxCarInfo = false;
            } else if (value.equals(MachineConfig.VALUE_CANBOX_TOUAREG_HIWORLD) || value.equals(MachineConfig.VALUE_CANBOX_PSA) || value.equals(MachineConfig.VALUE_CANBOX_HONDA_DA_SIMPLE)) {
                hideConboxSetting = false;
                hideCanboxCarInfo = false;
                hideCanboxAirControl = false;
            } else if (value.equals(MachineConfig.VALUE_CANBOX_PEUGEOT206) || value.equals(MachineConfig.VALUE_CANBOX_PORSCHE_UNION) || value.equals(MachineConfig.VALUE_CANBOX_OBD_BINARUI) || value.equals(MachineConfig.VALUE_CANBOX_PSA206_SIMPLE) || value.equals(MachineConfig.VALUE_CANBOX_MONDEO_DAOJUN) || value.equals(MachineConfig.VALUE_CANBOX_HY_RAISE) || value.equals(MachineConfig.VALUE_CANBOX_NISSAN_RAISE)) {
                hideCanboxCarInfo = false;
            } else if (value.equals(MachineConfig.VALUE_CANBOX_OPEL) || value.equals(MachineConfig.VALUE_CANBOX_RAM_FIAT) || value.equals(MachineConfig.VALUE_CANBOX_MITSUBISHI_OUTLANDER_SIMPLE) || value.equals(MachineConfig.VALUE_CANBOX_CHERY_OD) || value.equals(MachineConfig.VALUE_CANBOX_NISSAN2013) || value.equals(MachineConfig.VALUE_CANBOX_HAFER_H2) || value.equals(MachineConfig.VALUE_CANBOX_SMART_HAOZHENG) || value.equals(MachineConfig.VALUE_CANBOX_MAZDA_CX5_SIMPLE) || value.equals(MachineConfig.VALUE_CANBOX_SUBARU_SIMPLE)) {
                hideConboxSetting = false;
            } else if (value.equals(MachineConfig.VALUE_CANBOX_JEEP_SIMPLE)) {
                if (mCarType == 0 || mCarType == 1 || mCarType == 4 || mCarType == 6 || mCarType == 7) {
                    hideCanboxAirControl = false;
                }
                hideCanboxCarInfo = false;
                hideConboxSetting = false;
                hideCarCD = false;
            } else if (value.equals(MachineConfig.VALUE_CANBOX_MAZDA3_BINARYTEK)) {
                hideCanboxCarInfo = false;
                hideConboxSetting = false;
                hideCarCD = false;
            } else if (value.equals(MachineConfig.VALUE_CANBOX_MAZDA_XINBAS)) {
                hideCanboxCarInfo = false;
                hideConboxSetting = false;
                if (mCarType != 1) {
                    hideAuxin = true;
                    hideCarCD = false;
                }
            } else if (value.equals(MachineConfig.VALUE_CANBOX_MAZDA3_SIMPLE)) {
                if (mCarType != 1) {
                    hideAuxin = true;
                    hideCarCD = false;
                }
                hideConboxSetting = false;
                hideCanboxCarInfo = false;
            } else if (value.equals(MachineConfig.VALUE_CANBOX_HAFER_H2)) {
                hideFrontCamera = true;
            } else if (value.equals(MachineConfig.VALUE_CANBOX_RX330_HAOZHENG)) {
                hideCanboxCarInfo = false;
                hideCarCD = false;
                hideAuxin = true;
                hideCanboxAirControl = false;
            } else if (value.equals(MachineConfig.VALUE_CANBOX_X30_RAISE)) {
                if (mCarType == 1 || mCarType == 2) {
                    hideCanboxAirControl = false;
                }
            } else if (value.equals(MachineConfig.VALUE_CANBOX_JEEP_XINBAS)) {
                hideConboxSetting = false;
                hideCarCD = false;
                hideCanboxAirControl = false;
            } else if (value.equals(MachineConfig.VALUE_CANBOX_GM_OD)) {
                hideAuxin = true;
                hideCarCD = false;
                hideCanboxAirControl = false;
            } else if (value.equals(MachineConfig.VALUE_CANBOX_VW_GOLF_SIMPLE) || value.equals(MachineConfig.VALUE_CANBOX_VW_MQB_RAISE) || value.equals(MachineConfig.VALUE_CANBOX_KADJAR_RAISE)) {
                hideConboxSetting = false;
                hideCanboxCarInfo = false;
                hideCanboxAirControl = false;
                hideTpms = false;
            } else if (value.equals(MachineConfig.VALUE_CANBOX_MAZDA_RAISE)) {
                hideConboxSetting = false;
                hideCanboxCarInfo = false;
                hideCarRadio = false;
                hideCarCD = false;
            }
        }

        if (hideSync) {
            mSetHideApp.add("com.focussync.MainActivity");
        }
        if (hideConboxSetting) {
            mSetHideApp.add("com.canboxsetting.MainActivity");
        }
        if (hideCanboxCarInfo) {
            mSetHideApp.add("com.canboxsetting.CarInfoActivity");
        }
        if (hideCanboxAirControl || (appHide != null && appHide.contains(HIDE_CANBOX_AC))) {
            mSetHideApp.add("com.canboxsetting.CanAirControlActivity");
        }
        if (hideTpms) {
            mSetHideApp.add("com.canboxsetting.TPMSActivity");
        }
        if (hide360) {
            mSetHideApp.add("com.canboxsetting.AVMActivity");
        }
        if (hideEQ) {
            mSetHideApp.add("com.canboxsetting.eqset.EQActivity");
        }

        if (hideCarRadio) {
            mSetHideApp.add("com.canboxsetting.RadioActivity");
        }

        // if (!(MachineConfig.VALUE_CANBOX_JEEP_SIMPLE.equals(value)
        // || MachineConfig.VALUE_CANBOX_MAZDA3_BINARYTEK.equals(value)
        // || MachineConfig.VALUE_CANBOX_MAZDA_XINBAS.equals(value))) {
        if (hideCarCD) {
            mSetHideApp.add("com.canboxsetting.JeepCarCDPlayerActivity");
        }
        if (hideCarCompass) {
            mSetHideApp.add("com.canboxsetting.CarCompassActivity");
        }
        if (hideCarPlayer) {
            mSetHideApp.add("com.canboxsetting.CarPlayerActivity");
        }
        if (hideTimeSet) {
            mSetHideApp.add("com.canboxsetting.TimeSetActivity");
        }
        if (hideAnStart) {
            mSetHideApp.add("com.canboxsetting.AnStartActivity");
        }
        if (hideSeatHeat) {
            mSetHideApp.add("com.canboxsetting.SeatHeatActivity");
        }
    }

    private static void updateHideAppConboxVersion2(String appHide) {
        String appShow = MachineConfig.getPropertyOnce(MachineConfig.KEY_CAN_BOX_SHOW_APP);

        if (appShow == null || !appShow.contains(HIDE_CANBOX_SYNC)) {
            mSetHideApp.add("com.focussync.MainActivity");
        }
        if (appShow == null || !appShow.contains(HIDE_CANBOX_SET)) {
            mSetHideApp.add("com.canboxsetting.MainActivity");
        }

        if (appShow == null || !appShow.contains(HIDE_CANBOX_INFO)) {
            mSetHideApp.add("com.canboxsetting.CarInfoActivity");
        }

        if (appShow == null || !appShow.contains(HIDE_CANBOX_AC) || (appHide != null && appHide.contains(HIDE_CANBOX_AC))) {
            mSetHideApp.add("com.canboxsetting.CanAirControlActivity");
        }

        if (appShow == null || !appShow.contains(HIDE_CANBOX_CD)) {
            mSetHideApp.add("com.canboxsetting.JeepCarCDPlayerActivity");
        }

        if (appShow == null || !appShow.contains(HIDE_CANBOX_TPMS)) {
            mSetHideApp.add("com.canboxsetting.TPMSActivity");
        }

        if (appShow == null || !appShow.contains(HIDE_CANBOX_EQ)) {
            mSetHideApp.add("com.canboxsetting.eqset.EQActivity");
        }
        if (appShow == null || !appShow.contains(HIDE_CANBOX_360)) {
            mSetHideApp.add("com.canboxsetting.AVMActivity");
        }

        if (appShow == null || !appShow.contains(HIDE_CANBOX_RADIO)) {
            mSetHideApp.add("com.canboxsetting.RadioActivity");
        }
        if (appShow == null || !appShow.contains(HIDE_CANBOX_COMPASS)) {
            mSetHideApp.add("com.canboxsetting.CarCompassActivity");
        }
        if (appShow == null || !appShow.contains(HIDE_CANBOX_PLAYER)) {
            mSetHideApp.add("com.canboxsetting.CarPlayerActivity");
        }
        if (appShow == null || !appShow.contains(HIDE_CANBOX_TIMESET)) {
            mSetHideApp.add("com.canboxsetting.TimeSetActivity");
        }
        if (appShow == null || !appShow.contains(HIDE_CANBOX_ANSTART)) {
            mSetHideApp.add("com.canboxsetting.AnStartActivity");
        }
        if (appShow == null || !appShow.contains(HIDE_CANBOX_SEATHEAT)) {
            mSetHideApp.add("com.canboxsetting.SeatHeatActivity");
        }
        if (appShow != null && appShow.contains(HIDE_APP_AUX)) {
            hideAuxin = true;
        }

        if (appShow != null && appShow.contains(HIDE_APP_FRONT_CMAERA)) {
            hideFrontCamera = true;
        }
    }

    private static void updateHideAppConbox(String appHide) {
        String value = MachineConfig.getPropertyOnce(MachineConfig.KEY_CAN_BOX);
        int mCarType = 0;
        int mProVersion = 0;
        if (value != null) {
            String[] ss = value.split(",");
            value = ss[0];

            for (int i = 1; i < ss.length; ++i) {
                if (ss[i].startsWith(MachineConfig.KEY_SUB_CANBOX_CAR_TYPE)) {
                    try {
                        mCarType = Integer.parseInt(ss[i].substring(1));
                    } catch (Exception ignored) {
                    }
                } else if (ss[i].startsWith(MachineConfig.KEY_SUB_CANBOX_PROTOCAL_VERSION)) {
                    try {
                        mProVersion = Integer.parseInt(ss[i].substring(1));
                    } catch (Exception ignored) {
                    }
                }
            }
        }

        if (value == null || MachineConfig.VALUE_CANBOX_NONE.equals(value) || mProVersion >= 2) {
            updateHideAppConboxVersion2(appHide); // in version2 the CANBOX APP
        } else {
            updateHideAppConboxVersion1(value, mCarType, appHide);
        }
    }

    public static boolean hideAuxin = false;
    public static boolean hideFrontCamera = false;

    public static void updateHideAppConfig() {
        hideAuxin = false;
        hideFrontCamera = false;
        mSetHideApp.clear();
        // mSetHideApp.add("com.my.filemanager.FileManagerActivity");
        mUsbDvd = MachineConfig.getPropertyIntOnce(MachineConfig.KEY_USB_DVD);
        if (mUsbDvd != 1) {
            // if (value.contains(HIDE_USB_DVD)) {
            mSetHideApp.add(USB_DVD);
            // }
        }

        String value = MachineConfig.getPropertyOnce(MachineConfig.KEY_APP_HIDE);
        String appHide = value;
        if (null != value) {
            if (value.contains("DTV")) {
                mSetHideApp.add("com.my.tv.TVActivity");
            }
            if (value.contains(HIDE_APP_AUX)) {
                // mSetHideApp.add("com.my.auxplayer.AUXPlayer");
                hideAuxin = true;
            }
            if (value.contains("BT")) {
                mSetHideApp.add("com.my.bt.ATBluetoothActivity");
            }
            if (value.contains(HIDE_APP_DVD)) {
                mSetHideApp.add("com.my.dvd.DVDPlayer");
            }
            // if (value.contains(HIDE_USB_DVD)) {
            // mSetHideApp.add("com.car.dvdplayer.DVDPlayerActivity");
            // }

            if (value.contains(HIDE_APP_FRONT_CMAERA)) {
                // mSetHideApp.add("com.my.frontcamera.FrontCameraActivity");
                hideFrontCamera = true;
            }
            if (value.contains(HIDE_APP_VIDEO_OUT)) {
                mSetHideApp.add("com.my.videoout.VideoOutActivity");
            }
            if (value.contains(HIDE_APP_DVR)) {
                mSetHideApp.add("com.my.dvr.MainActivity");
            }
            if (value.contains(HIDE_APP_JOYSTUDY)) {
                mSetHideApp.add("com.SwcApplication.JoyActivity");
            }
            if (value.contains(HIDE_APP_WHEELKEYSTUDY)) {
                mSetHideApp.add("com.SwcApplication.SwcActivity");
            }
            if (value.contains(HIDE_TPMS)) {
                mSetHideApp.add("com.ak.tpms.ActivityMain");
            }
        } else {
            mSetHideApp.add("com.my.tv.TVActivity");
            mSetHideApp.add("com.SwcApplication.JoyActivity");
        }

        String s = MachineConfig.getProperty(MachineConfig.KEY_BT_TYPE);
        int mBTType = 0;
        if (s != null) {
            try {
                mBTType = Integer.parseInt(s);
            } catch (Exception ignored) {

            }
        }
        if (mBTType != MachineConfig.VAULE_BT_TYPE_PARROT || (value != null && value.contains(HIDE_APP_VIOCE_CONTROL))) {
            mSetHideApp.add("com.my.bt.VoiceControlActivity");
        }
        // else {
        // mSetHideApp.add("com.my.dvd.DVDPlayer"); //default is hide dvd , if
        // not set anything
        // }

        mSetHideApp.add("com.adobe.reader.AdobeReader");
        mSetHideApp.add("net.easyconn.ui.Sv05MainActivity");
        mSetHideApp.add("com.google.android.gm.ConversationListActivityGmail");
        mSetHideApp.add("am.radiogr.SplashScreenActivity");
        mSetHideApp.add("tunein.ui.activities.TuneInHomeActivity");
        mSetHideApp.add("tunein.ui.activities.SplashScreenActivity");

        mSetHideApp.add("com.canboxsetting.AVMActivity");

        mSetHideApp.add("com.android.deskclock.DeskClock");

        updateHideAppConbox(appHide);

        if (hideAuxin) {
            mSetHideApp.add("com.my.auxplayer.AUXPlayer");
        }

        if (!hideFrontCamera) {
            value = MachineConfig.getPropertyOnce(MachineConfig.KEY_CAMERA_TYPE);
            if (value != null) {
                if (value.equals(MachineConfig.VAULE_CAMERA_FRONT + "")) {
                    mSetHideApp.add("com.my.frontcamera.FrontCameraActivity4");
                } else if (value.equals(MachineConfig.VAULE_CAMERA4 + "")) {
                    mSetHideApp.add("com.my.frontcamera.FrontCameraActivity");
                }
            } else {
                mSetHideApp.add("com.my.frontcamera.FrontCameraActivity4");
            }
        } else {
            mSetHideApp.add("com.my.frontcamera.FrontCameraActivity");
            mSetHideApp.add("com.my.frontcamera.FrontCameraActivity4");
        }

        if (MachineConfig.getPropertyIntReadOnly(MachineConfig.KEY_SHOW_BACK_CAMERA) != 1) {
            mSetHideApp.add("com.my.frontcamera.BackCameraActivity");
        }
        addHiedAppForever();
    }

    public static String getCanboxSetting() {
        String mCanboxType = MachineConfig.getPropertyOnce(MachineConfig.KEY_CAN_BOX);
        if (mCanboxType != null) {
            String[] ss = mCanboxType.split(",");
            mCanboxType = ss[0];
        }
        return mCanboxType;
    }

    public static boolean isHidePackage(String pn) {
        for (String s : mSetHideApp) {
            if (pn.equalsIgnoreCase(s)) {
                return true;
            }
        }
        return false;
    }

    public static boolean isUSBDvd() {
        boolean ret = false;
        mUsbDvd = MachineConfig.getPropertyInt(MachineConfig.KEY_USB_DVD);
        if (mUsbDvd == 1) {
            boolean hideDvd = false;
            for (String s : mSetHideApp) {
                if (CAR_UI_DVD.equalsIgnoreCase(s)) {
                    hideDvd = true;
                }
            }
            if (!hideDvd) {
                ret = true;
            }
        }

        return ret;
    }

    public static Drawable mWallpaperDrawable;

    public static void updateSystemBackground(Context mContext, View v) {
        WallpaperManager wallpaperManager = WallpaperManager.getInstance(mContext);
        Drawable wallpaperDrawable = wallpaperManager.getDrawable();
        if (wallpaperDrawable != null && mWallpaperDrawable != wallpaperDrawable) {
            mWallpaperDrawable = wallpaperDrawable;
            v.setBackground(wallpaperDrawable);
        }
    }

    private static void addHiedAppForever() {
        String s = MachineConfig.getPropertyReadOnly(MachineConfig.KEY_APP_HIDE_FOREVER);
        if (s != null) {
            String[] ss = s.split(",");
            mSetHideApp.addAll(Arrays.asList(ss));
        }
    }

    public static void addCustomHideApp(Context context) {
        String s;
        s = MachineConfig.getPropertyReadOnly(MachineConfig.KEY_PASSWD_CUSTOMER_SERVICE_MODE_EX);
        if (s != null) {
            String[] ss = s.split(";");
            if (ss.length > 1) {
                s = SystemConfig.getProperty(context, MachineConfig.KEY_PASSWD_CUSTOMER_SERVICE_MODE_EX);

                if (!"1".equals(s)) {
                    ss = ss[1].split(",");
                    mSetHideApp.addAll(Arrays.asList(ss));
                }
            }
        }
    }

    public static void updateHideAppConfigEx(Context context) {
        updateHideAppConfig();
        addCustomHideApp(context);
    }

    public static void printAppConfigInformation() {
        String appShow = MachineConfig.getPropertyOnce(MachineConfig.KEY_CAN_BOX_SHOW_APP);
        String appHide = MachineConfig.getPropertyOnce(MachineConfig.KEY_APP_HIDE);
        String canbox = MachineConfig.getPropertyOnce(MachineConfig.KEY_CAN_BOX);
        MMLog.d(TAG, "appShow=" + appShow);
        MMLog.d(TAG, "appHide=" + appHide);
        MMLog.d(TAG, "canbox=" + canbox);
        for (String app : mSetHideApp) {
            MMLog.d(TAG, app);
        }
    }
    // Drawable d = Drawable.createFromPath(AppConfig.CARUI_BACKGROUND);
    // // this.getWindow().getDecorView().setBackground(d);
    // getRootView(this).setBackground(d);

    // private static View getRootView(Activity context)
    // {
    // return
    // ((ViewGroup)context.findViewById(android.R.id.content)).getChildAt(0);
    // }

}
