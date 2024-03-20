package com.common.util;

import android.annotation.SuppressLint;

public class MyCmd {

    public interface Keycode {
        /**
         * No key
         */
        public static final byte NONE = 0x00;
        /**
         * 静音
         */
        public static final byte MUTE = 0x01;
        /**
         * 暂停/播放
         */
        public static final byte PLAY_PAUSE = 0x02;
        /**
         * 下一曲
         */
        public static final byte NEXT = 0x03;
        /**
         * 上一曲
         */
        public static final byte PREVIOUS = 0x04;
        /**
         * 出碟
         */
        public static final byte EJECT = 0x05;
        /**
         * Source 源切换
         */
        public static final byte MODLE = 0x06;
        /**
         * 播放
         */
        public static final byte PLAY = 0x07;
        /**
         * 暂停
         */
        public static final byte PAUSE = 0x08;
        /**
         * EQ 打开EQ界面
         */
        public static final byte EQ = 0x09;
        /**
         * Radio 打开收音机界面
         */
        public static final byte RADIO = 0x0a; //the function is contain baud
        /**
         * AS 收音自动存台
         */
        public static final byte AS = 0x0b;
        /**
         * Volume down 音量减
         */
        public static final byte VOLUME_DOWN = 0x0c;
        /**
         * Volume up 音量加
         */
        public static final byte VOLUME_UP = 0x0d;
        /**
         * UP 上
         */
        public static final byte UP = 0x0e;
        /**
         * DOWN 下
         */
        public static final byte DOWN = 0x0f;
        /**
         * LEFT 左
         */
        public static final byte LEFT = 0x10;
        /**
         * RIGHT 右
         */
        public static final byte RIGHT = 0x11;
        /**
         * 数字0
         */
        public static final byte NUMBER0 = 0x12;
        /**
         * 数字1
         */
        public static final byte NUMBER1 = 0x13;
        /**
         * 数字2
         */
        public static final byte NUMBER2 = 0x14;
        /**
         * 数字3
         */
        public static final byte NUMBER3 = 0x15;
        /**
         * 数字4
         */
        public static final byte NUMBER4 = 0x16;
        /**
         * 数字5
         */
        public static final byte NUMBER5 = 0x17;
        /**
         * 数字6
         */
        public static final byte NUMBER6 = 0x18;
        /**
         * 数字7
         */
        public static final byte NUMBER7 = 0x19;
        /**
         * 数字8
         */
        public static final byte NUMBER8 = 0x1a;
        /**
         * 数字9
         */
        public static final byte NUMBER9 = 0x1b;
        /**
         * SETUP 打开设置界面
         */
        public static final byte SETUP = 0x1c;
        /**
         * STOP 停止
         */
        public static final byte STOP = 0x1d;
        /**
         * APP 打开APP界面
         */
        public static final byte ALL_APP = 0x1e;
        /**
         * FAST_F 快进
         */
        public static final byte FAST_F = 0x1f;
        /**
         * FAST_R 快退
         */
        public static final byte FAST_R = 0x20;
        /**
         * ENTER 确定按键
         */
        public static final byte ENTER = 0x21;
        /**
         * SMART_CW 旋钮顺时针旋转
         */
        public static final byte SMART_CW = 0x22;
        /**
         * SMART_CCW 旋钮逆时针旋转
         */
        public static final byte SMART_CCW = 0x23;
        /**
         * HOME 安卓HOME按键
         */
        public static final byte HOME = 0x24;
        /**
         * BACK 安卓BACK按键
         */
        public static final byte BACK = 0x25;
        /**
         * MENU 安卓MENU按键
         */
        public static final byte MENU = 0x26;
        /**
         * TIME 打开时间界面
         */
        public static final byte TIME_SETTING = 0x27;
        /**
         * XING 键盘*号键
         */
        public static final byte NUMBER_STAR = 0x28;
        /**
         * JING 键盘#号键
         */
        public static final byte NUMBER_POUND = 0x29;
        /**
         * RDS TA开关
         */
        public static final byte RDS_TA_SWITCH = 0x2a;
        /**
         * ESC退出键
         */
        public static final byte ESC = 0x2b;
        /**
         * BT 打开蓝牙界面
         */
        public static final byte BT = 0x2c;
        /**
         * iPod 打开iPod界面
         */
        public static final byte IPOD = 0x2d;
        /**
         * DIAL 蓝牙拨号或接听
         */
        public static final byte BT_DIAL = 0x2e;
        /**
         * HANG 蓝牙挂断
         */
        public static final byte BT_HANG = 0x2f;
        /**
         * DVD 打开DVD界面
         */
        public static final byte DVD = 0x30;
        /**
         * NAVI 进入导航界面
         */
        public static final byte NAVIGATION = 0x31;
        /**
         * Screen Brightness切换屏幕亮度
         */
        public static final byte SCREEN_BRIGHTNESS = 0x32;
        /**
         * AUX in 打开AUXIN界面
         */
        public static final byte AUX_IN = 0x33;
        /**
         * Power 电源按键
         */
        public static final byte POWER = 0x34;
        /**
         * 上一个频道
         */
        public static final byte CH_UP = 0x35;
        /**
         * 下一个频道
         */
        public static final byte CH_DOWN = 0x36;

        public static final byte SPEECH = 0x37;

        public static final byte ZOOM_IN = 0x38;

        public static final byte ZOOM_OUT = 0x39;

        public static final byte POWER_OFF = 0x3a;

        public static final byte POWER_ON = (byte) 0xa9;

        public static final byte DVR = 0x3b;
        public static final byte AUDIO = 0x3c;
        public static final byte VIDEO = 0x3d;
        public static final byte PICTURE = 0x3e;

        public static final byte BT_MUSIC = 0x3f;

        public static final byte RADIO_POWER = 0x40;

        public static final byte DARK = 0x41;

        public static final byte BRIGHTNESS = 0x42;

        public static final byte KEY_SAVE_PLAY_STATUS = 0x43;
        public static final byte KEY_RESET_PLAY_STATUS = 0x44;


        public static final byte KEY_SUB_T = 0x45;
        public static final byte KEY_TITLE = 0x46;
        public static final byte KEY_PBC = 0x47;

        public static final byte KEY_TV = 0x48;


        public static final byte KEYAMS_RPT = 0x49;//AMS/RPT 复用功能	收音时为PS功能；		DVD时为重复按钮
        public static final byte KEY_LDC_RDM = 0x4a;//LOC/RDM 复用功能		收音时为LOC/DX切换；		DVD时为随机按钮

        public static final byte KEY_NUM10 = 0x4b;

        public static final byte KEY_ZOOM = 0x4c;
        public static final byte KEY_GOTO = 0x4d;


        public static final byte KEY_ST_PROG = 0x4e;

        public static final byte KEY_ANGLE = 0x4f;//AMS/RPT 复用功能	收音时为SCAN功能；		DVD时为ANGLE

        public static final byte KEY_OSD = 0x50;

        public static final byte KEY_LOUDNESS = 0x51;


        public static final byte KEY_SEEK_NEXT = 0x52;
        public static final byte KEY_SEEK_PREV = 0x53;

        public static final byte KEY_TURN_A = 0x54;
        public static final byte KEY_TURN_D = 0x55;
        public static final byte KEY_MIC = 0x56;


        //psa key

        public static final byte KEY_CAR_INFO = 0x57;
        public static final byte KEY_MEM_INFO = 0x58;
        public static final byte KEY_CHECK = 0x59;
        public static final byte KEY_LIST = 0x5a;

        public static final byte KEY_DVD_SETUP = 0x5b;

        public static final byte KEY_DVD_UP = 0x5c;
        public static final byte KEY_DVD_DOWN = 0x5d;
        public static final byte KEY_DVD_LEFT = 0x5e;
        public static final byte KEY_DVD_RIGHT = 0x5f;
        public static final byte KEY_DVD_ENTER = 0x60;


        public static final byte KEY_JOY_UP = 0x61;
        public static final byte KEY_JOY_DOWN = 0x62;
        public static final byte KEY_JOY_LEFT = 0x63;
        public static final byte KEY_JOY_RIGHT = 0x64;
        public static final byte KEY_JOY_ENTER = 0x65;
        public static final byte KEY_JOY_ROLL_LEFT = 0x66;
        public static final byte KEY_JOY_ROLL_RIGHT = 0x67;


        public static final byte KEY_JOY_HOME = 0x68;
        public static final byte KEY_JOY_BACK = 0x69;


        public static final byte KEY_BT_VOICE_SPEAKER = 0x6a;
        public static final byte KEY_BT_VOICE_PHONE = 0x6b;

        public static final byte KEY_RECENT_APPS = 0x6c;

        public static final byte KEY_RADIO_ONLY = 0x6d;
        /* yx special */
        public static final byte KEY_ROOL_RIGHT = 0x6e;
        public static final byte KEY_ROOL_LEFT = 0x6f;
        public static final byte KEY_TAKE_PICTURE = 0x70; // for dvr
        //		public static final byte KEY_DVR_ZOOM_IN = 0x71;
        //		public static final byte KEY_DVR_ZOOM_OUT = 0x72;

        public static final byte KEY_REPEAT = 0x71;
        public static final byte KEY_SHUFFLE = 0x72;


        public static final byte KEY_AM = 0x73;
        public static final byte KEY_FM = 0x74;


        public static final byte KEY_CAR_SETTING = 0x76;

        public static final byte KEY_CUSTOM_APP = 0x77;

        public static final byte KEY_EQ_SEL = 0x78; //only open & quit eq

        public static final byte KEY_EQ_MODE = 0x79; // just switch mode

        public static final byte KEY_RADIO_SCAN = 0x7a; //
        public static final byte KEY_RADIO_PS = 0x7b; //


        // 长按短按下的复合按键
        public static final byte MULT_MUTE_AND_POWER = 0x75;
        public static final byte MULT_BACK_AND_HOME = 0x7c;
        // 不同条件下的复合按键


        public static final byte MULT_SOURCE_AND_BT = 0x7d;
        public static final byte MULT_NEXT_AND_HANG = 0x7e;
        public static final byte MULT_PREV_AND_RECEIVE = 0x7f;


        // > 0x80 for


        public static final byte IXB_VOICE = (byte) 0x81;
        public static final byte IXB_360_DISPLAY = (byte) 0x82;

        public static final byte SPEED_UP = (byte) 0x83; //the fuction clean memory in systemui


        public static final byte BACKLIGHT_ON = (byte) 0x84;
        public static final byte BACKLIGHT_OFF = (byte) 0x85;


        public static final byte KEY_REPEAT_ONE = (byte) 0x86;
        public static final byte KEY_CAMERA = (byte) 0x87;
        public static final byte KEY_SIDE_CAMERA = (byte) 0x88;
        public static final byte KEY_SCREENT_SHOT = (byte) 0x89;
        public static final byte KEY_SPLIT_WINDOW = (byte) 0x8a;
        public static final byte KEY_SPEED = (byte) 0x8b;

        public static final byte DVD_FORCE_EJECT = (byte) 0x8c;

        public static final byte KEY_TS_CALIBRATION = (byte) 0x8d;
        public static final byte KEY_DISPLAY = (byte) 0x8e;
        public static final byte KEY_AIR_CONTROL = (byte) 0x8f;


        public static final byte MULT_BACK_AND_HANG = (byte) 0x90;
        public static final byte MULT_SPEECH_AND_BT = (byte) 0x91;
        public static final byte MULT_MUTE_AND_HANG = (byte) 0x92;
        public static final byte MULT_OK_AND_POWER = (byte) 0x93;
        public static final byte MULT_MUTE_AND_BT = (byte) 0x94;

        public static final byte MULT_SEEK_PRE_HANG = (byte) 0x95;
        public static final byte MULT_SEEK_NEXT_RECV = (byte) 0x96;
        public static final byte MULT_MODE_AND_RECV = (byte) 0x97;

        public static final byte MULT_NEXT_AND_RECEIVE = (byte) 0x98;
        public static final byte MULT_PREV_AND_HANG = (byte) 0x99;

        public static final byte EASY_CONNECT = (byte) 0xa0;
        public static final byte VOLUME_ROLL_UP = (byte) 0xa1;
        public static final byte VOLUME_ROLL_DOWN = (byte) 0xa2;
        public static final byte ROLL_NEXT = (byte) 0xa3;
        public static final byte ROLL_PREV = (byte) 0xa4;

        public static final byte HDMI = (byte) 0xa5;
        public static final byte VTR = (byte) 0xa6;

        public static final byte FF2 = (byte) 0xa7;
        public static final byte FR2 = (byte) 0xa8;
        public static final byte KEY_F_CAMERA = (byte) 0xaa;

        //0xD0 ~ 0xEF is Canbox function

        public static final byte CANBOX_AC_ON = (byte) 0xD0;
        public static final byte CANBOX_AC_OFF = (byte) 0xD1;
        public static final byte CANBOX_AC_WIND_UP = (byte) 0xD2;
        public static final byte CANBOX_AC_WIND_DOWN = (byte) 0xD3;
        public static final byte CANBOX_AC_TEMP_UP = (byte) 0xD4;
        public static final byte CANBOX_AC_TEMP_DOWN = (byte) 0xD5;
        public static final byte CANBOX_AC_WIND_MODE = (byte) 0xD6;

        public static final byte CANBOX_OPEN_AC_VIEW = (byte) 0xD7;

        public static final byte CANBOX_FUNCTION_START = CANBOX_AC_ON;
        public static final byte CANBOX_FUNCTION_END = (byte) 0xEF;


        public static final byte NISSIAN_360 = (byte) 0xF0;
        public static final byte KEY_360 = (byte) 0xF1;

        public static final byte MULT_SPEECH_MODE = (byte) 0xF2;
        public static final byte KEY_SPEECH_CARPLAY = (byte) 0xF3;
        public static final int EX_KEY2_MAIN = (byte) 0xFE;//ex key byte 3, data in byte 1~2
        //		public static final int MULT_RECENT_AND_BT = 0x101;

        public static final int EX_KEY2_PLAY_MUSIC_SONG = 0x01000000; //ex key byte 3, data in byte 1~2

    }

    public static int makeEx2Key(int key, int data) {
        return ((key & 0xff000000) | ((data & 0xffff) << 8) | (Keycode.EX_KEY2_MAIN & 0xff));
    }

    public interface Dvd {
        public final static int DVD_POWER_ON = 1;
        public final static int DVD_POWER_OFF = 0;

        public final static int DVD_STATUS_UNKNOW = 0;
        public final static int DVD_STATUS_DISK_INSIDE = 1;
        public final static int DVD_STATUS_DISK_IN_ENTRY = 2;
        public final static int DVD_STATUS_IN_ING = 3;
        public final static int DVD_STATUS_OUT_ING = 4;
        public final static int DVD_STATUS_NO_DISK = 5;
    }

    public interface KeyPressType {
        /**
         * 短按
         */
        public final static byte SHORT_PRESS = 0x01;
        /**
         * 长按
         */
        public final static byte LONG_PRESS = 0x02;
        /**
         * 双击
         */
        public final static byte DOUBLE_PRESS = 0x03;
    }

    public interface Cmd {
        // all uses
        public final static int SET_SOURCE = 0x01;

        public final static int QUERY_CURRENT_SOURCE = 0x02;


        public final static int BACKLIGHT_ON = 0x03;
        public final static int BACKLIGHT_OFF = 0x04;
        public final static int BACKLIGHT_STEP = 0x05;
        public final static int REVERSE_STATUS = 0x06;


        public final static int SET_SCREEN1_SOURCE = 0x07;
        public final static int SET_SCREEN0_SOURCE = 0x08;

        public final static int SHOW_MY_RECENT = 0x09;
        public final static int BT_PHONE_STATUS = 0x10;


        public final static int REQUEST_BRAKE_STATUS = 0x11;

        public final static int TOUCH_STUDY_START = 0x12;
        public final static int TOUCH_STUDY_KEY = 0x13;
        public final static int TOUCH_STUDY_END = 0x14;
        public final static int TOUCH_STUDY_CLEAR = 0x15;


        public final static int APP_REQUEST_SEND_KEY = 0x16;

        public final static int CANBOX_PHONE_STATUS = 0x17;


        public final static int REQUEST_CANBOX_VERSION = 0x18;


        public final static int STUDY_QUIT_WITHOUT_SAVE = 0x19;

        public final static int SET_DTV_CMD = 0x20;

        public final static int SET_VCOM = 0x21;


        public final static int SET_DTV_CMD_EX = 0x22;


        public final static int SET_AUDIO_GAIN = 0x23;


        public final static int SEND_CANBOX_DATA = 0x24;

        public final static int SET_AUDIO_SPECTRUM = 0x25;

        public final static int RETURN_AUDIO_SPECTRUM = 0x26;


        public final static int SET_SPECTRUM_SCREEN_SAVE = 0x27;

        public final static int SEND_UPDATE_CANBOX_SET = 0x28;

        public final static int SET_OBD_SCREEN_SAVE = 0x29;


        public final static int LAUNCHER_ON_CREATE = 0x2a;


        public final static int SET_RADIO_ANT_POWER = 0x2b;
        public final static int RETURN_CANBOX_VERSION = 0x2c;

        public final static int SET_REAR_SOURCE_L = 0x2d;
        public final static int NOTIFY_DISPLAY_OFF = 0x2e;

        public final static int FRAMEWORK_AUDIO_CONTROL = 0x60;


        public final static int SET_OUT_DOOR_TEMP = 0x61;
        public final static int SHOW_RECENT_APPS = 0x62;

        public final static int SHOW_AIR_CONTROL = 0x63;
        public final static int SHOW_CUR_APP_NAME = 0x64;
        public final static int ANDROID_POWEROFF = 0x65;


        public final static int ACC_POWER_OFF = 0x66;
        public final static int ACC_POWER_ON = 0x67;


        public final static int SYSTEMUI_STATUS_BAR_VISIBLE = 0x68;
        public final static int SYSTEMUI_STATUS_BAR_GONE = 0x69;

        public final static int SYSTEMUI_LONG_PRESS_RECENT = 0x6a;

        public final static int SYSTEMUI_SPEED_UP = 0x6b;


        public final static int MCU_BATTERY = 0x6c;
        public final static int BT_BATTERY_SIGNAL = 0x6d;


        public final static int DO_SPLIT_WINDOW = 0x6e;
        public final static int DO_SPEED = 0x6f;
        public final static int DO_SCREEN_SHOT = 0x70;
        // car service use only send
        public final static int SOURCE_CHANGE = 0x71;
        public final static int RETURN_CURRENT_SOURCE = 0x72;
        public final static int RETURN_REVERSE_STATUS = 0x73;
        public final static int RETURN_POWERKEY_STATUS = 0x74;
        public final static int RETURN_ABANDON_FOCUS = 0x77;

        public final static int RETURN_CURRENT_VOLUME = 0x78;

        public final static int REQUEST_SCREEN1_SHOW = 0x75;


        public final static int LAUNCHER_SHOW_ALL_APP = 0x79;
        public final static int UPDATE_CANBOX = 0x7a;
        public final static int SET_FRONT_CAMERA_POWER = 0x7b;

        public final static int BT_PHONE_CALLLOG_LIST = 0x7c;


        public final static int CANBOX_RQUEST_DRIVE_DATA = 0x7d;
        public final static int CANBOX_RETURN_DRIVE_DATA = 0x7e;

        public final static int CANBOX_VOICE_CONTROL = 0x7f;

        public final static int AUTO_TEST_SHOW_UI = 0x100; //data use source
        public final static int AUTO_TEST_START = 0x101; //
        public final static int AUTO_TEST_RESULT = 0x102;
        public final static int AUTO_TEST_END = 0x103; //data use source


        public final static int SHOW_DEBUG_MSG = 0x403; //data use source
        public final static int SHOW_RULER_SET_UI = 0x404; //data use source
        public final static int HOME_BEFORE = 0x405; //data use source
        /* mcu data use */

        public final static int MCU_RADIO_SEND_CMD = 0x1000;
        public final static int MCU_RDS_SEND_CMD = 0x1001;
        public final static int MCU_AUDIO_SEND_CMD = 0x1004;
        public final static int MCU_DVD_SEND_CMD = 0x1006;

        public final static int MCU_RADIO_RECEIVE_DATA = 0x2000;
        public final static int MCU_AUDIO_RECEIVE_DATA = 0x2001;
        public final static int MCU_CANBOX_RECEIVE_DATA = 0x2002;
        public final static int MCU_DVD_RECEIVE_DATA = 0x2003;


        public final static int MCU_BRAK_CAR_STATUS = 0x3001;


        public final static int EQ_QUIT = 0x3002;

        public final static int MCU_AUTO_MUTE = 0x3003;


        public final static int MCU_SET_VOLUME = 0x3004;

        public final static int MCU_QUERY_VOLUME = 0x3005;


        public final static int MCU_HIDE_VOLUME_UI = 0x3006;


        public final static int MCU_SET_VOLTAGE_PROTECT = 0x3007;

        public final static int MCU_QUERY_VOLTAGE_PROTECT = 0x3008;

        public final static int MCU_RETURN_VOLTAGE_PROTECT = 0x3009;


        public final static int MCU_QUERY_LIGHT_DECTECT = 0x300a;

        public final static int MCU_RETURN_LIGHT_DECTECT = 0x300b;
        public final static int MCU_SET_LIGHT_DECTECT = 0x300c;

        // screen 2 use
        // service->apps

        public final static int SCREEN0_REQUEST_SOURCE = 0x10000000;
        public final static int SCREEN0_SOURCE_CHANGE = 0x10000001;
        public final static int SCREEN1_REQUEST_SOURCE = 0x10000002;
        public final static int SCREEN1_SOURCE_CHANGE = 0x10000003;
        // apps->service
        public final static int SCREEN1_SHOW = 0x10000010;
        public final static int SCREEN1_HIDE = 0x10000011;

        // bt service use

        public final static int BT_RECEIVE_DATA_KEY = 0x20000001;

        public final static int BT_REQUEST_A2DP_INFO = 0x20000002;

        public final static int BT_REQUEST_A2DP_LIST_INFO = 0x20000003;

        public final static int BT_REQUEST_HFP_INFO = 0x20000004;

        public final static int BT_REQUEST_CALLING_INFO = 0x20000005;

        public final static int BT_REQUEST_MIC_GAIN = 0x20000006;
        public final static int BT_CARPLAY_STATUS = 0x20000007;


        public final static int BT_CALLING_INFO_DATA_ONLY = 1;
        public final static int BT_CALLING_INFO_HIDE_INCOMING_UI = 2;

        public final static int BT_SEND_A2DP_STATUS = 0x20000010;

        public final static int BT_SEND_ID3_INFO = 0x20000011;

        public final static int BT_SEND_TIME_STATUS = 0x20000012;

        public final static int BT_SEND_A2DP_CONNECT_STATUS = 0x20000013;


        public final static int BT_SEND_A2DP_CUR_FOLDER = 0x20000014;

        public final static int BT_SEND_A2DP_LIST_INFO = 0x20000015;


        public final static int BT_SEND_HFP_STATUS = 0x20000016;
        public final static int BT_SEND_HFP_STATUS2 = 0x20000017;    //1050 launcher use, with connect device name
        public final static int BT_SEND_CALLING_INFO = 0x20000018;    //1050 launcher use
        public final static int BT_SEND_MIC_GAIN = 0x20000019;
        //music service


        public final static int MUSIC_SEND_PLAY_STATUS = 0x20000040;

        public final static int MUSIC_REQUEST_PLAY_STATUS = 0x20000041;

        public final static int MUSIC_SEND_PLAY_TIME = 0x20000042;

        //////
        public final static int APP_USER_PRIVATE = 0x80000000;

    }

    public interface AppFlag {

        public final static int NEED_SAVE_STATUS = 0x01 << 0;
        public final static int NEED_RESET_STATUS = 0x01 << 1;

        public final static int NEED_MEDIA_KEY = 0x01 << 2;
        public final static int NEED_BT_KEY = 0x01 << 3;

        public final static int NEED_ALL = 0xffffffff;

    }

    public interface PhoneStatus {

        public final static byte PHONE_OFF = 0x00;
        public final static byte PHONE_ON = 0x01;

        public final static byte PHONE_BT_MUSIC_ON = 0x02;
        public final static byte PHONE_BT_MUSIC_OFF = 0x03;
        public final static byte PHONE_MUTE_ONE_TIME = 0x04;

        public final static byte PHONE_RINGING = 0x05;

        public final static byte VOICE_OFF = 0x20;
        public final static byte VOICE_ON = 0x21;

        public final static byte PHONE_CARPLAY_OFF = 0x30;
        public final static byte PHONE_CARPLAY_ON = 0x31;


        public final static byte PHONE_CONNECT = 0x7b;
        public final static byte PHONE_DISCONNECT = 0x7c;

        public final static byte PHONE_GOC_UNMUTE = 0x7d;
        public final static byte PHONE_GOC_MUTE = 0x7e;
        public final static byte PHONE_IVT_CRASH_MUTE = 0x7f;
    }

    public interface ID {
        public final static byte CAR_UI = 0x01;
    }

    public interface Screen1 {
        public final static byte SIZE_1024_600 = 0x0;
        public final static byte SIZE_800_480 = 0x1;
        public final static byte SIZE_1600_480 = 0x2;
    }
    // --------------------以下是广播定义--------------------
    // --------------------以下是通用广播定义-----------------

    public final static String ACTION_KEY_PRESS = "com.seanovo.android.ACTION_KEY_PRESS";
    public final static String EXTRAS_KEY_PRESS_TYPE = "extras_key_press_type";
    public final static String EXTRAS_KEY_CODE = "extras_key_code";
    public final static String EXTRAS_IS_NEED_DELAY = "extras_is_need_delay";

    public static final String BROADCAST_CMD_TO_CAR_SERVICE = "com.my.car.service.BROADCAST_CMD_TO_CAR_SERVICE";
    public static final String BROADCAST_CMD_TO_CAR_SERVICE_SYSTEM_ID = "com.my.car.service.BROADCAST_CMD_TO_CAR_SERVICE_SYSTEM_ID";
    public static final String BROADCAST_CMD_TO_CAR_SERVICE_BT = "com.my.car.service.BROADCAST_CMD_TO_CAR_SERVICE_BT";
    public static final String BROADCAST_CMD_TO_CAR_SERVICE_SYSTEM_UI = "com.my.car.service.BROADCAST_CMD_TO_CAR_SERVICE_com.android.systemui";
    public static final String BROADCAST_CMD_TO_CAR_SERVICE_CAR_UI = "com.my.car.service.BROADCAST_CMD_TO_CAR_SERVICE_SYSTEM_ID_com.car.ui";
    public static final String BROADCAST_CMD_TO_CAR_SERVICE_CAR_UI_FRAMEWORK = "com.my.car.service.BROADCAST_CMD_TO_CAR_SERVICE_SYSTEM_ID_framework";
    //	public static final String BROADCAST_CMD_TO_CAR_SERVICE_BT = "com.my.car.service.BROADCAST_CMD_TO_CAR_SERVICE_SYSTEM_ID";
    //	public static final String BROADCAST_CMD_TO_CAR_SERVICE_SYSTEM_ID = "com.my.car.service.BROADCAST_CMD_TO_CAR_SERVICE_SYSTEM_ID";


    public static final String BROADCAST_CAR_SERVICE_SEND = "com.my.car.service.BROADCAST_CAR_SERVICE_SEND";
    public static final String BROADCAST_CAR_SERVICE_SEND_SYSTEM_UI = "com.my.car.service.BROADCAST_CAR_SERVICE_SEND_com.android.systemui";
    public static final String EXTRA_COMMON_CMD = "cmd";
    public static final String EXTRA_COMMON_DATA = "data";
    public static final String EXTRA_COMMON_DATA2 = "data2";
    public static final String EXTRA_COMMON_DATA3 = "data3";
    public static final String EXTRA_COMMON_DATA4 = "data4";
    public static final String EXTRA_COMMON_DATA5 = "data5";
    public static final String EXTRA_COMMON_OBJECT = "object";
    public static final String EXTRA_COMMON_ID = "id";

    public static final String BROADCAST_CANDATA_CAR_SERVICE_SEND = "com.my.canbox.BROADCAST_CANDATA_CAR_SERVICE_SEND";
    public static final String BROADCAST_CMD_TO_BT = "com.commonlib.BROADCAST_CMD_TO_BT";
    public static final String BROADCAST_CMD_FROM_BT = "com.commonlib.BROADCAST_CMD_FROM_BT";

    public static final String BROADCAST_CMD_LAUNCHER_TO_BT = "com.commonlib.BROADCAST_CMD_LAUNCHER_TO_BT";

    public static final String BROADCAST_CMD_TO_CARUI_CAMERA = "com.commonlib.BROADCAST_CMD_TO_CARUI_CAMERA";
    public static final String BROADCAST_CMD_FROM_CARUI_CAMERA = "com.commonlib.BROADCAST_CMD_FROM_CARUI_CAMERA";

    public static final String BROADCAST_CMD_TO_MUSIC = "com.commonlib.BROADCAST_CMD_TO_MUSIC";
    public static final String BROADCAST_CMD_FROM_MUSIC = "com.commonlib.BROADCAST_CMD_FROM_MUSIC";


    public static final String BROADCAST_SET_TOUCH_STUDY = "com.commonlib.BROADCAST_SET_TOUCH_STUDY";
    public static final String BROADCAST_RETURN_TOUCH_STUDY = "com.commonlib.BROADCAST_RETURN_TOUCH_STUDY";


    public static final String BROADCAST_SET_JOY_STUDY = "com.commonlib.BROADCAST_SET_JOY_STUDY";
    public static final String BROADCAST_RETURN_JOY_STUDY = "com.commonlib.BROADCAST_RETURN_JOY_STUDY";

    public static final String BROADCAST_SET_CANKEY_STUDY = "com.commonlib.BROADCAST_SET_CANKEY_STUDY";
    public static final String BROADCAST_RETURN_CANKEY_STUDY = "com.commonlib.BROADCAST_RETURN_CANKEY_STUDY";

    public static final String BROADCAST_POWER_OFF = "com.my.power.OFF";
    public static final String BROADCAST_POWER_ON = "com.my.power.ON";


    public static final String BROADCAST_ACC_DELAY_POWER_OFF = "com.my.BROADCAST_ACC_DELAY_POWER_OFF";
    public static final String BROADCAST_ACC_DELAY_POWER_ON = "com.my.BROADCAST_ACC_DELAY_POWER_ON";

    public static final String BROADCAST_SYSTEM_RESET = "com.my.BROADCAST_SYSTEM_RESET";

    public static final String BROADCAST_REAL_POWER_OFF = "com.my.power.REAL_POWER_OFF";

    public static final String BROADCAST_MACHINECONFIG_UPDATE = "com.my.common.update.MachineConfig";

    public static final String BROADCAST_DVR_PATH_UPDATE = "com.my.dvr.path_update";
    public static final String BROADCAST_DVR_TIME_UPDATE = "com.my.dvr.time_update";

    public static final String BROADCAST_DVR_SCREEN1_UPDATE = "com.my.dvr.screen1_update";

    public static final String BROADCAST_UPDATE_TIME = "com.car.service.updateTime"; // only

    public static final String BROADCAST_START_BACKLIGHTSETTINGS = "com.my.car.service.BROADCAST_START_BACKLIGHTSETTINGS";
    public static final String BROADCAST_START_BACKLIGHTSETTINGS_SYSTEM_UI = "com.my.car.service.BROADCAST_START_BACKLIGHTSETTINGS_SYSTEM_UI";
    public static final String BROADCAST_START_BACKLIGHTSETTINGS_SETTINGS = "com.my.car.service.BROADCAST_START_BACKLIGHTSETTINGS_SETTINGS";
    public static final String BROADCAST_START_BACKLIGHTSETTINGS_COMMON = "com.my.car.service.BROADCAST_START_BACKLIGHTSETTINGS_COMMON";
    public static final String BROADCAST_START_VOLUMESETTINGS = "com.my.car.service.BROADCAST_START_VOLUMESETTINGS"; //system ui use
    public static final String BROADCAST_START_VOLUMESETTINGS_COMMON = "com.my.car.service.BROADCAST_START_VOLUMESETTINGS_COMMON"; //no system id  use

    // /////////////////////////////
    public static final int SOURCE_NONE = 0xff;
    public static final int SOURCE_RADIO = 0x0;
    public static final int SOURCE_DVD = 0x1;
    public static final int SOURCE_DTV_CVBS = 0x3;
    public static final int SOURCE_AUX = 0x5;
    public static final int SOURCE_DTV = 0x6;
    public static final int SOURCE_MX51 = 0x7;// android源
    public static final int SOURCE_IPOD = 0x9;
    public static final int SOURCE_REVERSE = 0xb;//
    public static final int SOURCE_AUX_FRONT = 0xc;//vtr
    public static final int SOURCE_BT = 0xd;

    public static final int SOURCE_HDMI = 0xe;//hdmi
    public static final int SOURCE_VTR = SOURCE_AUX_FRONT;//vtr

    public static final int SOURCE_DAB = 0xf;
    public static final int SOURCE_RADIO_TI1 = 0x8;         //am 1620
    public static final int SOURCE_RADIO_TI2 = SOURCE_DAB;    //am 1629

    public static final int SOURCE_AV_OFF = 0x10;
    public static final int SOURCE_CANBOX_IPOD = 0x12;
    public static final int SOURCE_3G = 0x13;
    public static final int SOURCE_PHONE_RINGING = 0x14;


    public static final int SOURCE_MUSIC = 0x26;
    public static final int SOURCE_VIDEO = 0x27;
    public static final int SOURCE_GPS = 0x28;
    public static final int SOURCE_MUSIC_FILEMANAGE = 0x29;
    public static final int SOURCE_VIDEO_FILEMANAGE = 0x30;
    public static final int SOURCE_OTHERS_APPS = 0x31;

    public static final int SOURCE_CANBOX_PHONE = 0x32; //
    public static final int SOURCE_CANBOX_MEDIA = 0x33; //

    public static final int SOURCE_BT_MUSIC = SOURCE_BT; // for test

    public static final int SOURCE_DVR = 0x34;

    public static final int SOURCE_BT_PHONE = 0x35;


    public static final int SOURCE_FRONT_CAMERA = 0x36; //only video source, no audio


    public static final int SOURCE_USBDVD = 0x37;

    public static final int SOURCE_RADIO_TA = 0x38;

    public static final int SOURCE_HDMI_SAME_SCREEN = 0x60;

    public static final int SOURCE_SCREEN1_LAUNCHER = 0x61;

    public static final int SOURCE_SCREEN1_SAVER = 0x62;

    public static final int SOURCE_SCREEN1_SETTINGS = 0x63;

    public static final int SOURCE_SCREEN1_WALLPAPER = 0x64;


    public static final int SOURCE_FOR_TEST_SD = 0x71;
    public static final int SOURCE_FOR_TEST_USB = 0x72;
    public static final int SOURCE_FOR_TEST_GPS = 0x73;
    public static final int SOURCE_FOR_TEST_WIFI = 0x74;

    public static final int CAMERA_SOURCE_REVERSE = 0x1;
    public static final int CAMERA_SOURCE_AUX = 0x2;
    public static final int CAMERA_SOURCE_DVD = 0x3;
    public static final int CAMERA_SOURCE_FRONT_CAMERA = 0x4;
    public static final int CAMERA_SOURCE_DTV_CVBS = 0x7;
    public static final int CAMERA_SOURCE_LEFT_CAMERA = 0x5;
    public static final int CAMERA_SOURCE_RIGHT_CAMERA = 0x6;

    // source off
    public static final int SOURCE_OFF = 0x1000;
    public static final int SOURCE_BT_OFF = SOURCE_OFF | SOURCE_BT;


    public final static String VENDOR_DIR = "/mnt/vendor/";// test  // 7.1

    //canbox use
    public static final String RADIO_SOURCE_CHANGE = "com.my.radio.SOURCE_CHANGE";
    public static final String DVD_SOURCE_CHANGE = "com.my.dvd.SOURCE_CHANGE";
    public static final String AUDIO_SOURCE_CHANGE = "com.my.audio.SOURCE_CHANGE";
    public static final String VIDEO_SOURCE_CHANGE = "com.my.video.SOURCE_CHANGE";
    public static final String IPOD_SOURCE_CHANGE = "com.my.ipod.SOURCE_CHANGE";
    //	public static final String MY_OUT_VOLUME_CHANGE = "com.my.out.VOLUME_CHANGE";
    //	public static final String MY_OUT_INDEX_CHANGE = "com.my.out.INDEX_CHANGE";
    //	public static final String ACTION_SYSTEM_POWER_OFF = "com.my.out.system.POWEROFF";
    public static final String BT_PHONE_BROADCAST = "com.my.bt.BT_PHONE_BROADCAST";


    public static final String BROADCAST_SEND_TO_CAN = "com.my.canbox.BROADCAST_SEND_TO_CAN";
    public static final String BROADCAST_SEND_TO_CAN_FROM_BT = "com.my.canbox.BROADCAST_SEND_TO_CAN_FROM_BT";
    public static final String BROADCAST_SEND_FROM_CAN = "com.my.canbox.BROADCAST_SEND_FROM_CAN";


    public static final String BROADCAST_BT_UPDATE = "com.my.bt.BROADCAST_BT_UPDATE";
    public static final String BROADCAST_BT_VERSION = "com.my.bt.BROADCAST_BT_VERSION";


    public static final String BROADCAST_ILL_STATUS = "com.my.out.BROADCAST_ILL_STATUS";

    // will del

    public static final String BROADCAST_ACTIVITY_STATUS = "com.my.android.ACTIVITY_STATUS";

    @SuppressLint("SdCardPath")
    public static final String PATH_SDCARD_ = "/sdcard/";
    public static final String PATH_SDCARD1 = "/storage/sdcard1/";
    public static final String PATH_SDCARD2 = "/storage/sdcard2/";

    public static final String PATH_USB1 = "/storage/usbdisk1/";
    public static final String PATH_USB2 = "/storage/usbdisk2/";

    public static final String PATH_DVR = PATH_USB1; // test,

    public static final String PATH_MEDIA_SD = PATH_USB2;
    public static final String PATH_MEDIA_USB = PATH_USB2;


}
