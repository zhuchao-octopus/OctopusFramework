package com.common.util;

import android.os.Build;

import com.zhuchao.android.fbase.MMLog;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.nio.file.Files;
import java.util.Objects;
import java.util.Properties;

public class MachineConfig {
    private final static String TAG = "MachineConfig";
    public final static String KEY_CANBOX_DOOR_TYPE = "key_canbox_door_type";
    public final static String KEY_STATUSBAR_POWER = "key_statusbar_power";
    public final static String KEY_ARM_MEMORY_FOR_MCU = "key_memory_for_mcu";
    public final static String KEY_CALIBRATION = "key_calibration";

    public final static String KEY_GPS_PACKAGE = "key_gps_package";
    public final static String KEY_GPS_CLASS = "key_gps_class";
    public final static String KEY_GPS_UID = "key_gps_uid";
    public final static String KEY_GPS_VOLUME_CHANNEL = "key_gps_volume_channel";

    public final static String KEY_IFLY_PACKAGE = "ifly_package";
    public final static String KEY_MEDIA_PATH1 = "media_path1";
    public final static String KEY_MEDIA_PATH2 = "media_path2";
    public final static String KEY_CAR_TYPE = "key_car_type";
    public final static String KEY_OTG_TEST = "key_otg_test";
    public final static String KEY_LOG_TEST = "key_log_test";
    public final static String KEY_RDS = "key_rds";
    public final static String KEY_RADIO_REGION = "key_radio_region";
    public final static String KEY_RADIO_ANT_POWER = "key_radio_ant_power";
    public final static String KEY_SAVE_DRIVER = "save_driver";
    public final static String KEY_SAVE_DRIVER_PACKAGE = "save_driver_package";
    public final static String KEY_MODE_DELAY_TIME = "key_mode_delay_time";


    public final static String KEY_SETTINGS_SHOW_EXT = "settings_show_ext";
    public final static String KEY_SETTINGS_HIDE_EXT = "settings_hide_ext";

    public static final String KEY_SWITCH_TO_FRONT_CAMER = "switch_to_front_camera";

    public static final String KEY_FCAMERA_SHOW_USBCMAERA = "fcamera_show_usbcamera";
    public static final String KEY_SHOW_CAMERA_IN_BACKRADAR = "show_camera_in_backradar";

    public final static String KEY_LED_TYPE = "key_led_type";
    public final static String KEY_DVR_MIRROR = "key_dvr_mirror";


    public final static String KEY_UPDATE_NAME = "key_update_name";
    public final static String KEY_CAR_SETTING_TYPE = "key_car_setting_type";

    public final static String KEY_TOUCH_KEY_TYPE = "key_touch_key_type";

    public final static String KEY_DVR_FOLDER = "key_dvr_folder";
    public final static String KEY_BT_MIC_GAIN = "key_bt_mic_gain";

    public final static String FACTROY_PASSWD = "factroy_passwd";
    public final static String DRIVING_SET_PASSWD = "driving_set_passwd";


    public final static String ANDROID_VERSION_SHOW = "android_version_show";

    public final static String KEY_DVR_MODE = "key_dvr_mode";

    public final static String KEY_NO_REVERSE = "key_no_reverse";


    public final static String KEY_ACC_DELAY_OFF = "key_acc_delay_off";

    public final static String KEY_CAMERA_TYPE = "key_camera_type";
    public final static String KEY_AVM_CAMERA_MODE = "cam_mode";

    public final static String KEY_WIFI_TYPE = "wifi_type";

    public final static String KEY_AVM_UI = "avm_ui";
    public final static String KEY_AVM_DISABLE_360 = "avm_disable_360";

    public final static String KEY_SUPPORT_FIXED_AD_STD = "support_fixed_std";
    public final static String KEY_AD_STD = "ad_std";

    public final static String KEY_RADIO_BUTTON_TYPE = "key_radio_button_type";    //1992: 1.长按搜索键搜索

    public final static String KEY_USB_DVD = "key_usb_dvd";

    public final static String MCU_ILLUM_ACC_NODE = "MCU_ILLUM_ACC_NODE";

    public static final int VAULE_CAMERA_FRONT = 0;
    public static final int VAULE_CAMERA4 = 1;
    public static final int VAULE_CAMERA_HIDEBUTTON = 2;

    public final static String KEY_MODE_KEY_STYLE = "key_mode_key_style";

    public final static String KEY_IFLY_CHANNEL = "key_ifly_channel";
    //	public final static String KEY_DVR_RECORD_SOUND = "key_dvr_record_sound"; //use system setting instead
    public final static String KEY_CUSTOMER_NAME = "key_customer_name";
    public final static String KEY_BOOTANIM_PATH = "bootanim_path";

    public final static String KEY_SCREEN_W = "disp_w";
    public final static String KEY_SCREEN_H = "disp_h";

    public final static String KEY_SCREEN1_W = "aux_disp_w";
    public final static String KEY_SCREEN1_H = "aux_disp_h";
    //	public final static String KEY_SCREEN_WALLPAPER_NAME = "key_screen1_wallpaper";

    public final static String KEY_RADIO_SWITCH = "radio_switch";
    public final static String KEY_RADIO_FREQ = "radio_freq";

    public final static String KEY_THIRD_APP_SOUND_FIRST = "key_3rd_app_snd_first";
    public final static String KEY_CAN_BOX = "key_can_box";
    public final static String KEY_CAN_BOX_EX = "key_can_box_ex";
    public final static String KEY_APP_HIDE = "key_app_hide";
    public final static String KEY_APP_HIDE3 = "key_app_hide3";
    public final static String KEY_CAN_BOX_SHOW_APP = "key_can_box_show_app";

    public final static String KEY_CAN_BOX_PG_SWITCH = "key_lr_reverse_switch";


    public final static String KEY_EXTERNAL_BOX = "key_external_box";
    public final static String KEY_SUDING_MIC_GAIN = "persist.suding.mic.gainlevel.default";

    public final static String KEY_TV_TYPE = "tv_type"; //0:default hdme, other is cvbs custom code. 2 bytes
    public final static String KEY_TPMS_TYPE = "tpms_type";
    public final static String KEY_FTJ_TYPE = "key_ftj";

    public final static String KEY_LOGO_PATH = "logo_path";
    public final static String KEY_LOGO_PATH_K = "logo_path_kernel";

    public final static String KEY_TP_X_INV = "tp_x_inv";
    public final static String KEY_TP_Y_INV = "tp_y_inv";
    public final static String KEY_TP_CALIB = "tp_calib";


    public final static String KEY_SCREEN1_VIEW = "screen1_view";

    public final static String KEY_RUDDER = "rudder";

    public final static String KEY_VIDEO_ON_DRIVING = "mcu_video_on_driving";
    public final static String KEY_MCU_BEEP = "mcu_beep";

    public final static String KEY_DISP2_ENABLE = "disp2_enable";

    public final static String KEY_GPS_CONFIG_LIST = "gps_config_list";

    public final static String KEY_SCREEN1_BT = "screen1_bt";
    public final static String KEY_SCREEN1_REVERSE = "screen1_reverse";
    public final static String KEY_SCREEN1_VOLUME = "screen1_volume";
    public final static String KEY_SCREEN1_CANBOX_AIR = "screen1_canbox_air";

    public final static String KEY_ACC_SPLASH_PIC = "acc_splash_pic";
    //only in parameter. read only
    //	public final static String KEY_LAUNCHER_SHOW_HOTSEAT_ALLAPPP = "show_hoteat_in_allapp";
    //	public final static String KEY_LAUNCHER_HIDE_WIDGET = "hide_widget_page";

    // hide_widget_page,icon_type,show_hoteat_in_allapp
    public final static String KEY_LAUNCHER_CONFIG = "launcher_config";

    public final static String KEY_APP_HIDE_FOREVER = "key_app_hide_forever";

    public final static String KEY_APP_HIDE_APPINFO = "key_app_hide_appinfo";    //ww+,for settings appinfo hide app

    public final static String KEY_PANEL_KEY_DEF_CONFIG = "panel_key_defcfg";
    public final static String KEY_SWC_KEY_DEF_CONFIG = "swc_key_defcfg";

    //no use
    public final static String KEY_DVR_PATH = "dvr_path"; //move to SystemConfig
    public final static String KEY_DVR_TIME = "dvr_time";
    public final static String KEY_DVR_RECORDING = "key_dvr_recording";
    //no use end
    // bootanim_path=/mnt/parameter/bootanim_kry.zip

    public final static String KEY_DEFAULT_GPS = "default_gps";
    public final static String KEY_DEFAULT_360 = "default_360";

    public final static String KEY_HIDE_BT_MUSIC_CONNECT_INFO = "key_hide_bt_music_connect_info";
    public final static String KEY_MODEL = "key_model";
    public final static String KEY_MCU_PREFIX = "key_mcu_prefix";
    public final static String KEY_TOUCH3_IDENTIFY = "key_touch3_identify";

    public final static String KEY_FACTORY_AUDIO_GAIN = "key_factory_audio_gain";
    public final static String KEY_USER_AUDIO_GAIN = "key_user_audio_gain";

    public final static String KEY_LOCALES = "key_locales";

    public final static String KEY_MODEL_VA = "model_va";

    public final static String KEY_DISPAUX_ENABLE = "dispaux_enable";                    //.config_properties
    public final static String KEY_DISPAUX_APPAUTO_DISABLE = "dispaux_appauto_disable";    //settings

    public final static String KEY_PASSWD_UNINSTALL = "passwd_uninstall";    //settings apps uninstall


    public final static String KEY_PASSWD_CUSTOMER_SERVICE_MODE = "passwd_customer_service_mode";    //
    public final static String KEY_CUSTOMER_SERVICE_MODE_SWITCH = "customer_service_mode_switch";    //

    public final static String KEY_PASSWD_CUSTOMER_SERVICE_MODE_EX = "passwd_customer_service_mode_ex";    //

    public final static String VALUE_SCREEN1_VIEW_BT = "bt";
    public final static String VALUE_SCREEN1_VIEW_REVERSE = "reverse";

    public final static String VALUE_CUSTOM_NAME_YX = "yx";
    public final static String VALUE_CUSTOM_NAME_CZL = "czl";

    public final static String VALUE_CAR_TYPE_BENZ = "benz";
    public final static String VALUE_CAR_TYPE_BMW = "bmw";
    public final static String VALUE_CAR_TYPE_AUDIQ5 = "audiQ5";
    public final static String VALUE_CAR_TYPE_AUDIA4L = "audiA4L";
    public final static String VALUE_CAR_TYPE_AUDIA4L1024 = "audiA4L1024";
    public final static String VALUE_CAR_TYPE_AUDIQ51024 = "audiQ51024";
    public final static String VALUE_CAR_TYPE_GM = "gm";
    // logo
    public final static String VALUE_BOOTANIM_PATH = "/mnt/paramter/";
    public final static String VALUE_BOOTANIM_AUDI = "audi.zip";
    public final static String VALUE_BOOTANIM_AUDI1024 = "audi1024.zip";
    public final static String VALUE_BOOTANIM_BENZ = "benz.zip";
    public final static String VALUE_BOOTANIM_BMW = "bmw.zip";

    // gm use
    public final static String VALUE_BOOTANIM_GM = "gm.zip";
    public final static String VALUE_BOOTANIM_VOLVO = "volvo.zip";
    public final static String VALUE_BOOTANIM_INFINITI = "infiniti.zip";
    public final static String VALUE_BOOTANIM_PEUGEOT = "peugeot.zip";
    public final static String VALUE_BOOTANIM_CITROEN = "citroen.zip";
    public final static String VALUE_BOOTANIM_DONGFENG = "dongfeng.zip";

    public final static String VALUE_BOOTANIM_PORSCHE = "porsche.zip";
    public final static String VALUE_BOOTANIM_JAGUAR = "jaguar.zip";
    public final static String VALUE_BOOTANIM_CADILLAC = "cadillac.zip";
    public final static String VALUE_BOOTANIM_LANDLOVER = "landlover.zip";
    public final static String VALUE_BOOTANIM_OTHER = "other.zip";
    public final static String VALUE_BOOTANIM_OTHER1024 = "other1024.zip";

    public final static String VALUE_LOGO_PATH = "/mnt/paramter/logo/";

    public final static String VALUE_CUSTOMER_YX = "yx";
    public final static String VALUE_BOOTANIM_CZL = "czl";

    public final static String VALUE_ON = "1";
    public final static String VALUE_OFF = "0";

    // can box
    public final static String VALUE_CANBOX_NONE = "none";
    public final static String VALUE_CANBOX_HY = "hy";
    public final static String VALUE_CANBOX_TOYOTA = "toyota";
    public final static String VALUE_CANBOX_MAZDA = "mazda";
    public final static String VALUE_CANBOX_BESTURN_X80 = "besturn_x80";
    public final static String VALUE_CANBOX_TEANA_2013 = "teana_2013";
    public final static String VALUE_CANBOX_OPEL = "opel";// mazda & opel low(simple)
    public final static String VALUE_CANBOX_VW = "vw";
    public final static String VALUE_CANBOX_TOYOTA_LOW = "toyota_low";
    public final static String VALUE_CANBOX_FORD_SIMPLE = "ford_simple";
    public final static String VALUE_CANBOX_PSA_BAGOO = "psa_bagoo";
    public final static String VALUE_CANBOX_GM_SIMPLE = "gm_simple";
    public final static String VALUE_CANBOX_GM_RAISE = "gm_raise";
    public final static String VALUE_CANBOX_HONDA_DA_SIMPLE = "honda_da_simple";
    public final static String VALUE_CANBOX_VW_GOLF_SIMPLE = "golf_simple";
    public final static String VALUE_CANBOX_RAM_FIAT = "ram_fiat_simple";
    public final static String VALUE_CANBOX_RENAULT_MEGANE_FLUENCE_SMPLE = "renault_megane_fluence_imple";
    public final static String VALUE_CANBOX_BMW_E90X1_UNION = "bmw_e90x1_union";
    public final static String VALUE_CANBOX_FIAT = "fiat_simple";
    public final static String VALUE_CANBOX_FORD_MONDEO = "ford_mondeo_simple";
    public final static String VALUE_CANBOX_PSA = "psa_simple";
    public final static String VALUE_CANBOX_BENZ_BAGOO = "benz_bagoo";
    public final static String VALUE_CANBOX_KADJAR_RAISE = "kadjar_raise";
    public final static String VALUE_CANBOX_GMC_SIMPLE = "gmc_simple";
    public final static String VALUE_CANBOX_BENZ_B200_UNION = "benz_b200_union";
    public final static String VALUE_CANBOX_MITSUBISHI_OUTLANDER_SIMPLE = "mitsubishi_outlander";
    public final static String VALUE_CANBOX_MAZDA_BT50_SIMPLE = "mazda_bt50";
    public final static String VALUE_CANBOX_JEEP_SIMPLE = "jeep_simple";
    public final static String VALUE_CANBOX_ACCORD7_CHANGYUANTONG = "accord_cyt";
    public final static String VALUE_CANBOX_TOYOTA_BINARYTEK = "toyota_binarytek";
    public final static String VALUE_CANBOX_MAZDA_XINBAS = "mazda_xinbas";
    public final static String VALUE_CANBOX_PEUGEOT206 = "peugeot206";
    public final static String VALUE_CANBOX_ACCORD2013 = "accord2013";
    public final static String VALUE_CANBOX_NISSAN2013 = "nissan2013";
    public final static String VALUE_CANBOX_PORSCHE_UNION = "porsche_union";
    public final static String VALUE_CANBOX_MAZDA3_BINARYTEK = "mazda_binarytek";
    public final static String VALUE_CANBOX_BRAVO_UNION = "bravo_union";
    public final static String VALUE_CANBOX_TOUAREG_HIWORLD = "touareg_hiworld";
    public final static String VALUE_CANBOX_DACIA_SIMPLE = "dacia_simple";
    public final static String VALUE_CANBOX_BENZ_VITO_SIMPLE = "benz_vito_simple";

    public final static String VALUE_CANBOX_NISSAN_RAISE = "nissan_raise";
    public final static String VALUE_CANBOX_PETGEO_RAISE = "petgeo_raise";

    public final static String VALUE_CANBOX_EX_HY = "ford_simple";


    public final static String VALUE_CANBOX_FORD_EXPLORER_SIMPLE = "for_explorer_simple";

    public final static String VALUE_CANBOX_ACCORD_BINARYTEK = "accord_binarytek";
    public final static String VALUE_CANBOX_AUDI_SIMPLE = "audi_simple";
    public final static String VALUE_CANBOX_SUBARU_ODS = "subaru_ods";
    public final static String VALUE_CANBOX_MINI_HIWORD = "mini_hiword";


    public final static String VALUE_CANBOX_NISSAN_BINARYTEK = "nissan_binarytek";

    public final static String VALUE_CANBOX_VW_MQB_RAISE = "vw_mqb_raise";


    public final static String VALUE_CANBOX_CHERY_OD = "chery_od";

    public final static String VALUE_CANBOX_CHRYSLER_SIMPLE = "chrysler_simple";
    public final static String VALUE_CANBOX_MAZDA3_SIMPLE = "mszda3_simple";


    public final static String VALUE_CANBOX_OBD_BINARUI = "obd_binarytek";
    public final static String VALUE_CANBOX_HAFER_H2 = "hafer_h2";

    public final static String VALUE_CANBOX_HONDA_RAISE = "honda_da_raise";

    public final static String VALUE_CANBOX_PETGEO_SCREEN_RAISE = "petgeo_screen_raise";

    public final static String VALUE_CANBOX_FORD_RAISE = "ford_raise";
    public final static String VALUE_CANBOX_SMART_HAOZHENG = "smart_haozheng";
    public final static String VALUE_CANBOX_LANDROVER_HAOZHENG = "landrover_haozheng";
    public final static String VALUE_CANBOX_PEUGEOT307_UNION = "peugeot307_union";

    public final static String VALUE_CANBOX_MAZDA_CX5_SIMPLE = "mazda_cx5_simple";
    public final static String VALUE_CANBOX_RX330_HAOZHENG = "rx33_haozheng";
    public final static String VALUE_CANBOX_PSA206_SIMPLE = "psa_206_simple";
    public final static String VALUE_CANBOX_X30_RAISE = "x30_raise";
    public final static String VALUE_CANBOX_MONDEO_DAOJUN = "mondeo_daojun";
    public final static String VALUE_CANBOX_JEEP_XINBAS = "jeep_xinbas";
    public final static String VALUE_CANBOX_OUSHANG_RAISE = "oushang_raise";
    public final static String VALUE_CANBOX_FIAT_EGEA_RAISE = "fiat_egea_raise";
    public final static String VALUE_CANBOX_HY_RAISE = "hy_raise";
    public final static String VALUE_CANBOX_ALPHA_BAGOO = "alpha_bagoo";
    public final static String VALUE_CANBOX_SUBARU_SIMPLE = "subaru_simple";
    public final static String VALUE_CANBOX_ZHONGXING_SIMPLE = "zhongxing_simple";

    public final static String VALUE_CANBOX_GM_OD = "gm_od";
    public final static String VALUE_CANBOX_ZHONGXING_OD = "zhongxing_od";

    public final static String VALUE_CANBOX_MAZDA_RAISE = "mazda_raise";

    public final static String VALUE_CANBOX_TOYOTA_RAISE = "toyota_raise";
    public final static String VALUE_CANBOX_MINI_HAOZHENG = "mini_haozheng";


    public final static String VALUE_CANBOX_PRO = "pro"; //this for new pro version >= 3


    public final static String KEY_SUB_CANBOX_AIR_CONDITION = "a";
    public final static String KEY_SUB_CANBOX_CHANGE_KEY = "c";
    public final static String KEY_SUB_CANBOX_EQ = "e";
    public final static String KEY_SUB_CANBOX_FRONT_DOOR = "f";
    public final static String KEY_SUB_CANBOX_CAR_CONFIG = "h"; //car high middle low config
    public final static String KEY_SUB_CANBOX_ID = "i"; //this id is for settings jason used
    public final static String KEY_SUB_CANBOX_MCU_BAUD = "j";
    public final static String KEY_SUB_CANBOX_KEY_TYPE = "k";
    public final static String KEY_SUB_CANBOX_MCU_CONFIG = "l";
    public final static String KEY_SUB_CANBOX_OTHER = "o";
    public final static String KEY_SUB_CANBOX_CAR_TYPE2 = "p";
    public final static String KEY_SUB_CANBOX_REAR_DOOR = "r";
    public final static String KEY_SUB_CANBOX_REQUEST_REPEAT_CMD = "s";
    public final static String KEY_SUB_CANBOX_CAR_TYPE = "t";
    public final static String KEY_SUB_CANBOX_UPDATE_TIME = "u";
    public final static String KEY_SUB_CANBOX_PROTOCAL_VERSION = "v";
    public final static String KEY_SUB_CANBOX_PROTOCAL_INDEX = "z"; //version>=3 support


    public final static int VALUE_CANBOX_OTHER_RADAR_BEEP = 0x2;
    public final static int VALUE_CANBOX_OTHER_RADAR_UI = 0x4;
    public final static int VALUE_CANBOX_OTHER_RADAR_UI_ONLY_IN_REVERSE = 0x8;
    public final static int VALUE_CANBOX_OTHER_HOUR_ADD_1 = 0x16;
    public final static int VALUE_CANBOX_OTHER_HOUR_MINUS_1 = 0x32;
    public final static int VALUE_CANBOX_OTHER_RIGHT_CAMERA = ((0x1) << 6);
    public final static int VALUE_CANBOX_OTHER_DATA_DISTRIBUTION = ((0x1) << 7);
    //	public final static String DEFAULT_GPS_PACKAGE = "com.autonavi.amapauto";
    //	public final static String DEFAULT_GPS_CLASS = "com.autonavi.auto.remote.fill.UsbFillActivity";
    //

    public static String DEFAULT_GPS_PACKAGE = "none";
    public static String DEFAULT_GPS_CLASS = "none";

    public final static String KEY_HIDE_SCREEN_SHOT = "hide_screen_shot";
    public final static String KEY_BT_TYPE = "bt_type";
    public final static String KEY_HIDE_LAUNCHER = "hide_launcher";
    public final static String KEY_LAUNCHER_UI = "launcher_ui";


    public final static String KEY_SHOW_BACK_CAMERA = "show_back_camera";
    public final static String KEY_SHOW_DARK_MODE = "show_dark_mode";
    public final static String KEY_MODE_KEY_CONTENT = "mode_key_content";

    public static final int VAULE_BT_TYPE_IVT = 0;
    public static final int VAULE_BT_TYPE_PARROT = 1;
    public static final int VAULE_BT_TYPE_GOC = 2;
    public static final int VAULE_BT_TYPE_GOC_8761 = 3;
    public static final int VAULE_BT_TYPE_GOC_RF210 = 4;
    public static final int VAULE_BT_TYPE_SD_816 = 5;

    public final static String KEY_1992_LIST_COLOR = "1992_list_color";

    public final static String KEY_EXT_UI = "key_ext_ui";
    //read only in /mnt/parameter/.config_properties
    public final static String KEY_SYSTEM_UI = "system_ui";
    public final static String VALUE_SYSTEM_UI_KLD1 = "kld20170831";    //800-> sw322dp , 1024->sw323dp 2605ui launcher only
    public final static String VALUE_SYSTEM_UI_KLD2 = "kld20171124";    //800-> sw324dp , 1024->sw325dp skv2
    public final static String VALUE_SYSTEM_UI_KLD3 = "kld20171125";    //800-> sw326dp , 1024->sw327dp this app with all app
    public final static String VALUE_SYSTEM_UI_KLD5 = "kld20180131";    //800-> sw328dp , 1024->sw329dp launcher only
    public final static String VALUE_SYSTEM_UI_KLD7_1992 = "kld5_1992_20180423"; //800-> sw330dp , 1024->sw331dp from 2605ui launcher only
    public final static String VALUE_SYSTEM_UI_KLD3_8702 = "kld6_8702";    //800-> sw332dp , 1024->sw333dp this app with all app
    public final static String VALUE_SYSTEM_UI_KLD10_887 = "kld10_887";    //800-> sw334dp , 1024->sw335dp this launcher only
    public final static String VALUE_SYSTEM_UI_KLD11_200 = "kld11_200";
    public final static String VALUE_SYSTEM_UI_KLD12_80 = "kld12_80";    //800-> sw334dp , 1024->sw335dp this launcher only
    public final static String VALUE_SYSTEM_UI_KLD15_6413 = "kld15_6413";    //800-> sw334dp , 1024->sw335dp this launcher only
    public final static String VALUE_SYSTEM_UI19_KLD1 = "kld1";
    public final static String VALUE_SYSTEM_UI20_RM10_1 = "kld20_rm10_1";
    public final static String VALUE_SYSTEM_UI21_RM10_2 = "kld21_rm10_2";
    public final static String VALUE_SYSTEM_UI21_RM12 = "rm12";
    public final static String VALUE_SYSTEM_UI22_1050 = "kld22_1050";
    public final static String VALUE_SYSTEM_UI16_7099 = "7099";
    public final static String VALUE_SYSTEM_UI24_616 = "616";
    public final static String VALUE_SYSTEM_UI28_7451 = "7451";
    public final static String VALUE_SYSTEM_UI31_KLD7 = "kld7";
    public final static String VALUE_SYSTEM_UI32_KLD8 = "kld8";
    public final static String VALUE_SYSTEM_UI33_IXB = "ixb";
    public final static String VALUE_SYSTEM_UI34_KLD9 = "kld9";
    public final static String VALUE_SYSTEM_UI35_KLD813 = "kld813";
    public final static String VALUE_SYSTEM_UI35_KLD813_2 = "kld813_2";
    public final static String VALUE_SYSTEM_UI36_664 = "664";
    public final static String VALUE_SYSTEM_UI37_KLD10 = "kld10";
    public final static String VALUE_SYSTEM_UI40_KLD90 = "kld90";
    public final static String VALUE_SYSTEM_UI41_2007 = "2007";
    public final static String VALUE_SYSTEM_UI42_13 = "13";     //old 913
    public final static String VALUE_SYSTEM_UI42_913 = "913";
    public final static String VALUE_SYSTEM_UI43_3300 = "3300";
    public final static String VALUE_SYSTEM_UI43_3300_1 = "3300_1";
    public final static String VALUE_SYSTEM_UI44_KLD007 = "kld007";
    public final static String VALUE_SYSTEM_UI45_8702_2 = "8702_2";
    public final static String VALUE_SYSTEM_UI_887_90 = "887_90";    //800-> sw334dp , 1024->sw335dp this launcher only

    public final static String VALUE_EQ_UI_8586B = "8586b";
    public final static String VALUE_SYSTEM_UI_KLD047 = "kld047";

    public final static String VALUE_SYSTEM_UI_927_1 = "927_1";
    public final static String VALUE_SYSTEM_UI_1050_2 = "1050_2";
    public final static String VALUE_SYSTEM_UI_710 = "710";
    public final static String VALUE_SYSTEM_UI_PX30_1 = "px30_1";
    public final static String VALUE_SYSTEM_UI_KLD002 = "kld002";
    public final static String VALUE_SYSTEM_UI_KLD006 = "kld006";

    public final static String VALUE_SYSTEM_UI_8586B = "kld8586b";
    public final static String VALUE_SYSTEM_UI_9813 = "kld9813";
    public final static String VALUE_SYSTEM_UI_YJ = "yj";

    public final static String KEY_BT_SUPPORT_MACTALK = "bt_support_mactalk"; //for 200 mactalk
    public final static String KEY_BT_FAVORITE = "bt_favorite";
    public final static String KEY_BT_NAME = "bt_name";
    public final static String KEY_BT_PASSWD = "bt_passwd";
    public final static String KEY_LANGUAGE = "lang";
    public final static String KEY_TIMEZONE = "timezone";
    public final static String KEY_TIME12 = "time12";
    public final static String KEY_KEYBOARD = "keyboard";
    public final static String KEY_BT_MUSIC_INSIDE = "bt_music_inside";
    public final static String KEY_BT_AUTO_DOWNLOAD_CALLLOG = "bt_auto_download_calllog";
    public final static String KEY_BT_VOICE_BUTTON = "bt_voice_button";
    public final static String KEY_OBD_SHOW_IN_SCREEN1 = "obd_show_in_screen1";
    public final static String KEY_DASHBOARD_USE_OBD = "dashboard_use_obd";
    public final static String KEY_DASHBOARD_UI = "dashboard_ui";


    public final static String KEY_HIDE_LAUNCHER_CE_BUTTON = "hide_ce_button";

    public final static String KEY_HIDE_ADD_LANGUAGE = "key_hide_add_language";

    public final static String KEY_LAUNCHER_TRANSITION = "key_launcher_transition";

    public final static String KEY_SCREEN_BRIGHTNESS = "screen_brightness";
    public final static String KEY_SCREEN_SATURATION = "screen_saturation";
    public final static String KEY_SCREEN_CONTRAST = "screen_contrast";
    public final static String KEY_SCREEN_HUE = "screen_hue";
    public final static String KEY_SCREEN_VCOM = "screen_vcom";
    public final static String KEY_SCREEN_AVDD = "screen_avdd";
    public final static String KEY_GPU_BRIGHTNESS = "gpu_brightness";
    public final static String KEY_GPU_CONTRAST = "gpu_contrast";
    public final static String KEY_PUZZLE_BRIGHTNESS = "puzzle_brightness";
    public final static String KEY_CAMERA_CONTRAST = "camera_contrast";
    public final static String KEY_CAMERA_BRIGHTNESS = "camera_brightness";
    public final static String KEY_CAMERA_SATURATION = "camera_saturation";
    public final static String KEY_CAMERA_HUE = "camera_hue";
    public final static String KEY_CAMERA_SHARPNESS = "camera_sharpness";
    public final static String KEY_SCREEN_PARAM_INDEX = "screen_param_index";

    public final static String KEY_TOUCH_ASSISTANT = "touch_assistant";

    private static Properties mProperties = new Properties();
    private static Properties mPropertiesReadOnly;

    private final static String PROJECT_CONFIG_FILENAME = ".config_properties";
    public final static String VENDOR_DIR = "/mnt/vendor/";// test  // 7.1
    public final static String PARAMETER_DIR = "/mnt/paramter/";
    private final static String SYSTEM_CONFIG = VENDOR_DIR + PROJECT_CONFIG_FILENAME;
    private final static String PARAMETER_CONFIG = PARAMETER_DIR + PROJECT_CONFIG_FILENAME;

    private static String mParameterPath = PARAMETER_DIR;
    private final static String KEY_PARAMETER_PATH = "parameter_path";

    static {
        InputStream inputStream = null;
        File configFile = new File(PARAMETER_CONFIG);
        if (configFile.exists()) {
            try {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) inputStream = Files.newInputStream(configFile.toPath());
                else inputStream = new FileInputStream(configFile);

                Properties pt = new Properties();
                pt.load(inputStream);
                inputStream.close();
                String s = pt.getProperty(KEY_PARAMETER_PATH);
                if (s != null) {
                    mParameterPath = s;
                    MMLog.d(TAG, "Parameter Path changed to " + mParameterPath);
                }
                pt = null;
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        //load default gps APK
        String s = getPropertyReadOnly(KEY_DEFAULT_GPS);
        if (s != null) {
            String[] ss = s.split("/");
            if (ss.length > 1) {
                DEFAULT_GPS_PACKAGE = ss[0];
                DEFAULT_GPS_CLASS = ss[1];
            }
        }
    }

    public static String getParameterPath() {
        return mParameterPath;
    }

    // only CarService use these functions below. other use Settings
    private static void getConfigProperties() {
        InputStream inputStream = null;
        File configFile = new File(SYSTEM_CONFIG);
        if (configFile.exists()) {
            try {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) inputStream = Files.newInputStream(configFile.toPath());
                else inputStream = new FileInputStream(configFile);

                mProperties.clear();
                mProperties.load(inputStream);
                inputStream.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    private static void initConfigProperties() {
        if (mProperties.size() == 0) {
            getConfigProperties();
        }
    }

    public static void updateConfigProperties() {

        File file = new File(SYSTEM_CONFIG);
        try {
            if (!file.exists()) {
                file.createNewFile();
                //FileUtils.setPermissions(SYSTEM_CONFIG, FileUtils.S_IRWXU | FileUtils.S_IRWXG | FileUtils.S_IRWXO, -1, -1);
            }
            //FileUtils.setPermissions(SYSTEM_CONFIG, FileUtils.S_IRWXU | FileUtils.S_IRWXG | FileUtils.S_IRWXO, -1, -1);

            Util.sudoExec("chmod:666:" + SYSTEM_CONFIG);
            FileOutputStream out = new FileOutputStream(file);
            mProperties.store(out, "");
            out.flush();
            out.getFD().sync();
            out.close();
            ///	if (!Util.isAndroidR())
            ///	Util.sudoExec("sync");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static String getProperty(String name) {
        initConfigProperties();
        return mProperties.getProperty(name);
    }

    public static int getPropertyInt(String name) {
        int ret = 0;
        try {
            return Integer.parseInt(getProperty(name));
        } catch (Exception ignored) {
        }
        return ret;
    }

    public static int getIntProperty2(String name) {
        int ret = -1;
        try {
            return Integer.parseInt(getProperty(name));
        } catch (Exception ignored) {
        }
        return ret;
    }

    public static int getPropertyIntOnce(String name) {
        int ret = 0;
        try {
            return Integer.parseInt(getPropertyOnce(name));
        } catch (Exception ignored) {
        }
        return ret;
    }

    public static Object setProperty(String name, String value) {
        Object ret;
        initConfigProperties();
        if (value == null) {
            ret = mProperties.remove(name);
        } else {
            ret = mProperties.setProperty(name, value);
        }
        updateConfigProperties();
        return ret;
    }

    public static void setIntProperty(String name, int value) {
        setProperty(name, "" + value);
    }

    public static String getPropertyOnce(String name) {
        getConfigProperties();
        return mProperties.getProperty(name);
    }
    public static String getPropertyForce(String name) {
        getConfigProperties();
        return mProperties.getProperty(name);
    }
    public static String getPropertyReadOnly(String name) {
        if (mPropertiesReadOnly == null) {
            InputStream inputStream = null;
            File configFile = new File(getParameterPath() + PROJECT_CONFIG_FILENAME);
            if (configFile.exists()) {
                try {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) inputStream = Files.newInputStream(configFile.toPath());
                    else inputStream = new FileInputStream(configFile);

                    if (mPropertiesReadOnly == null) {
                        mPropertiesReadOnly = new Properties();
                        mPropertiesReadOnly.load(inputStream);
                    }
                    inputStream.close();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
        if (mPropertiesReadOnly != null) {
            return mPropertiesReadOnly.getProperty(name);
        }
        return null;
    }

    public static int getPropertyIntReadOnly(String name) {
        int ret = 0;
        try {
            return Integer.parseInt(Objects.requireNonNull(getPropertyReadOnly(name)));
        } catch (Exception ignored) {
        }
        return ret;
    }

    public static Properties exportProperties() {
        return (Properties) mProperties.clone();
    }

    public static void importProperties(Properties prop) {
        mProperties.clear();
        mProperties = (Properties) prop.clone();
        updateConfigProperties();
    }

}
