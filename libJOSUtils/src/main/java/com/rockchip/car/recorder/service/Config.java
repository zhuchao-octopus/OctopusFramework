package com.rockchip.car.recorder.service;

import android.content.Intent;
import android.os.Environment;

/**
 * Created by Administrator on 2016/8/22.
 */
public class Config {
    //================ Storage Config ================
//    public static final String EXTENAL_SD = Environment.getExternalStorageDirectory().toString();
    public static final String DCIM = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DCIM).toString();
    public static final String DIRECTORY = DCIM + "/Camera";
    public static String DISK_SD_ID = "";
    public static String EXTENAL_SD = "/mnt/sdcard";
    public static String EXTERNAL_DCIM = EXTENAL_SD + "/DCIM";
    public static String VIDEO_DIRECTORY_A = EXTERNAL_DCIM + "/VIDA";
    public static String VIDEO_DIRECTORY_B = EXTERNAL_DCIM + "/VIDB";
    public static String VIDEO_LOCK_DIRECTORY = EXTERNAL_DCIM + "/LOCK";
    public static String JPEG_DIRECTORY = EXTERNAL_DCIM + "/PIC";
    public static final String JPEG_POSTFIX = ".jpg";

    public static long LOW_STORAGE_THRESHOLD_BYTES = 500*1024*1024L;


    public static int mMaxRecordTime;


    // ================ Intent Action ================
    public static final String ACTION_RECORD_RESTART = "android.intent.action.RECORDER_RESTART";
    public static final String ACTION_BOOT_COMPLETED = Intent.ACTION_BOOT_COMPLETED;
    public static final String ACTION_LAUNCHER_STARTED = "android.intent.action.LAUNCHER_STARTED";
    public static final String ACTION_ACC_EVENT = "android.intent.action.ACC_EVENT";
    public static final String ACTION_REVERSE_EVENT = "android.intent.action.REVERSE_EVENT";
    public static final String EXTRA_REVERSE_STATE = "android.intent.extra.REVERSE_STATE";
    public static final String EXTRA_ACC_STATE = "android.intent.extra.ACC_STATE";

    // ================ Bundle Extra ================
    public static final String EXTRA_BOOT_START = "boot_start";
    public static final String EXTRA_BOOT_START_BG = "boot_start_BG";
    public static final String EXTRA_EVENT_ACC = "event_acc";
    public static final String EXTRA_EVENT_REVERSE = "event_reverse";
    public static final String EXTRA_PREVIEWING = "preview";
    public static final String EXTRA_RECORDING = "recording";


    // ================ Pref Key ================
    public static final String KEY_PREVIEW_SIZE = "pref_preview_size";
    public static final String KEY_PICTURE_SIZE = "pref_picture_size";

    public static final String KEY_WHITE_BALANCE = "pref_camera_whitebalance_key";
    public static final String KEY_EXPOSURE = "pref_camera_exposure_key";
    public static final String KEY_COLOR_EFFECT = "pref_camera_color_effect";
    public static final String KEY_RECORD_DURATION = "pref_record_duration";
    public static final String KEY_VIDEO_QUALITY = "pref_back_video_quality_key";
    public static final String KEY_RECORD_MODE = "pref_record_mode";
    public static final String KEY_ENCORDING_RATE = "pref_encording_rate";
    public static final String KEY_PREVIEW_FPS = "pref_preview_fps";


    public static final String KEY_BOOT_START = "pref_boot_start";
    public static final String KEY_PIC_IN_PIC = "pref_pic_in_pic";
    public static final String KEY_RECORD_AUDIO_ENABLE = "pref_record_audio_enable";
    public static final String KEY_MISFIRE_RECORD = "pref_misfire_record";
    public static final String KEY_INFRARED_RED= "pref_infrared_led";
    public static final String KEY_TTS_VOICE = "pref_tts_voice";
    public static final String KEY_WATER_MARK = "pref_water_mark";
    public static final String KEY_GSENSOR_LEVEL = "pref_gsensor_level";
    public static final String KEY_MOTION_DETECT = "pref_motion_detect";
    public static final String KEY_BACK_MIRROR = "pref_back_mirror";
    public static final String KEY_SHOW_FLOAT_VIEW = "pref_show_float_view";
    public static final String KEY_ADAS_ON = "pref_adas_on";
    public static final String KEY_ADAS_VANISH_LINE = "vanish_line";
    public static final String KEY_ADAS_HOOD_LINE = "hood_line";
    public static final String KEY_ADAS_DETECTION_MODE = "detection_mode";
    public static final String KEY_CVBS_CHANNEL = "cvbs_channel";

    public static final String KEY_PREVIEW_SWITCH = "pref_preview_switch";

}
