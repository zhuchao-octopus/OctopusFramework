package com.common.utils;

import android.annotation.SuppressLint;
import android.content.Context;
import android.os.Build;
import android.provider.Settings;

public class SettingProperties {

    public final static String KEY_ACC_DELAY_OFF = "key_acc_delay_off";
    public final static String KEY_ACC_DELAY_OFF_DVR = "key_acc_delay_off_dvr";
    public static final String GPS_AUTO_UPDATE_TIME = "gps_auto_update_time";

    public static final String REVERSE_STATIC_TRACK = "REVERSE_STATIC_TRACK";
    public static final String REVERSE_DYNC_TRACK = "REVERSE_DYNC_TRACK";
    public static final String REVERSE_RADAR = "REVERSE_RADAR";

    public static final String CANBOX_EQ_VOLUME = "CANBOX_EQ_VOLUME";
    public static final String CANBOX_TEMP_UNIT = "CANBOX_TEMP_UNIT";
    public static final String CANBOX_MILEAGE_UNIT = "CANBOX_MILEAGE_UNIT";
    public static final String AUTO_PLAY_MUSIC_DEVICES_MOUNTED = "AUTO_PLAY_MUSIC_DEVICES_MOUNTED";
    public static final String SHOW_FOCUS_CAR_WARNING_MSG = "SHOW_FOCUS_CAR_WARNING_MSG";
    public static final String KEY_CUSTOM_APP = "KEY_CUSTOM_APP";
    public final static String MIRROR_PREVIEW = "mirror_preview";
    public final static String MEDIA_INFO_TOAST_BACKGROUND = "media_info_toast_background";

    public final static String GPS_BRAKE = "gps_brake";
    public final static String CANBOX_DOOR_VOICE = "canbox_door_voice";
    public final static String CANBOX_FRONT_RADAR_OPEN_CAMERA = "canbox_front_radar_open_camera";

    public final static String KEY_DVR_PATH = "dvr_path";
    public final static String KEY_DVR_TIME = "dvr_time";
    public final static String KEY_DVR_RECORDING = "key_dvr_recording";
    public final static String KEY_DVR_ACTITUL_RECORDING = "key_dvr_actitul_recording";

    public final static String KEY_DVR_FOLDER = "key_dvr_folder";

    public final static String KEY_DVR_MODE = "key_dvr_mode";

    public final static String KEY_REAR_VIDEO_L_MODE = "key_rear_video_l_mode";

    //	public final static String KEY_SEASON_SET = "key_season_set";
    public final static String KEY_NISSIAN_360_SYSTEM = "key_nissian_360_system";
    public final static String KEY_NISSIAN_360_SYSTEM_BUTTON = "key_nissian_360_system_button";
    public final static String KEY_SCREEN1_WALLPAPER_NAME = "key_screen1_wallpaper";
    public final static String KEY_SCREEN0_WALLPAPER_NAME = "key_screen0_wallpaper";
    public final static String KEY_CE_STYLE_WALLPAPER_NAME = "key_ce_style_wallpaper";


    public final static String KEY_CE_STYLE = "key_ce_style";
    public final static String KEY_MIC_TYPE = "key_mic_type";
    public final static String KEY_MIC_GAIN = "key_mic_gain";


    public final static String KEY_SCREEN_SAVE_STYLE = "key_screen_save_style";
    public final static String KEY_SCREEN1_SCREENSAVER_TYPE = "key_screen1_screensaver_type";

    public final static String KEY_DATE_FORMAT = "key_date_format";
    public final static String KEY_LAUNCHER_UI_RM10 = "key_launcher_ui_rm10";
    public final static String KEY_LAUNCHER_UI_RM10_WORKSPACE_RELOAD = "key_launcher_ui_workspace_reload";

    public final static String KEY_SCREEN1_BACKLIGHT = "key_screen1_backlight";
    public final static String KEY_DEFAULT_RESET_VOLUME_LEVEL = "key_default_reset_volume_level";

    public final static String KEY_REVERSE_BACKLIGHT = "key_reverse_backlight";
    public final static String KEY_REVERSE_CONTRAST = "key_reverse_contrast";
    public final static String KEY_EQ_INDEPEND_SWITCH = "key_eq_independ_swtich";
    public final static String KEY_EQ_INDEPEND = "key_eq_independ";

    public final static String KEY_CANBOX_EQ = "key_canbox_eq";
    public final static String KEY_CANBOX_TOUCH_PANNEL = "key_canbox_touch_pannel";

    public final static String PATH_WALLPAPER = "/mnt/paramter/wallpaper/";
    public final static String PATH_DEFAULT_WALLPAPER0 = "default_screen0.jpg";
    public final static String PATH_DEFAULT_WALLPAPER1 = "default_screen1.jpg";
    public final static String PATH_DEFAULT_CE_WALLPAPER = "default_ce_screen.jpg";
    public final static String PATH_DARK_MODE_WALLPAPER = "dark_mode.jpg";

    @SuppressLint("SdCardPath")
    public final static String PATH_CE_WALLPAPER = "/sdcard/.wallpaper/";

    public final static String KEY_DARK_MODE_SWITCH = "car_dark_mode_switch";
    public final static String KEY_REVERSE_VOLUME = "key_reverse_volume";
    public final static String KEY_NAVI_MIX_SOUND = "key_navi_mix_sound";

    public final static String KEY_DSP = "ak_dsp";
    public final static String KEY_DSP_SCREEN_SAVER = "ak_dsp_screen_saver";
    public final static String KEY_DSP_SCREEN_SAVER_TYPE = "ak_dsp_screen_saver_type";
    public final static String KEY_DSP_EQ_MODE = "dsp_eq_mode";
    public final static String KEY_DSP_CUSTOM = "dsp_custom";
    public final static String KEY_DSP_ZONE = "dsp_zone";

    public final static String KEY_ENABLE_AK_VIOCE_ASSISTANT = "key_enable_ak_voice_assistant";
    public final static String KEY_CAR_CELL = "key_carcell";
    public final static String KEY_BT_CELL = "key_btcell";
    public final static String KEY_VIDEO_OUT = "key_video_out";
    public final static String KEY_ILL_STATE_TRIGGER = "key_ill_state_changed";
    public final static String KEY_TOUCH_ASSISTANT_SETTINGS = "touch_assistant_settings";
    public final static String KEY_SHOW_DTV_DEBUG = "show_dtv_debug";


    public static String getProperty(Context c, String name) {
        String s = null;
        try {
            if (Build.VERSION.SDK_INT >= 22) {
                s = Settings.Global.getString(c.getContentResolver(), name);
            } else {
                s = Settings.System.getString(c.getContentResolver(), name);
            }
        } catch (Exception snfe) {
            s = null;
        }
        return s;
    }

    public static boolean setProperty(Context c, String name, String value) {
        boolean ret = false;
        try {
            if (Build.VERSION.SDK_INT >= 22) {
                ret = Settings.Global.putString(c.getContentResolver(), name, value);
            } else {
                ret = Settings.System.putString(c.getContentResolver(), name, value);
            }
        } catch (Exception snfe) {
            ret = false;
        }
        Util.sudoExecNoCheck("sync");
        return ret;
    }

    public static int getIntProperty(Context c, String name) {
        int s = 0;
        try {
            if (Build.VERSION.SDK_INT >= 22) {
                s = Settings.Global.getInt(c.getContentResolver(), name);
            } else {
                s = Settings.System.getInt(c.getContentResolver(), name);
            }
        } catch (Exception snfe) {
            s = 0;
        }
        return s;
    }


    public static int getIntProperty2(Context c, String name) {
        int s = -1;
        try {
            if (Build.VERSION.SDK_INT >= 22) {
                s = Settings.Global.getInt(c.getContentResolver(), name);
            } else {
                s = Settings.System.getInt(c.getContentResolver(), name);
            }
        } catch (Exception e) {
            s = -1;
        }
        return s;
    }

    public static boolean setIntProperty(Context c, String name, int value) {
        boolean ret = false;
        try {
            if (Build.VERSION.SDK_INT >= 22) {
                ret = Settings.Global.putInt(c.getContentResolver(), name, value);
            } else {
                ret = Settings.System.putInt(c.getContentResolver(), name, value);
            }
        } catch (Exception e) {
            ret = false;
        }
        //	Util.sudoExecNoCheck("sync");
        return ret;
    }
}
