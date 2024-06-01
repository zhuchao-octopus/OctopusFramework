package com.zhuchao.android.fbase;

public class MessageEvent {
    ////////////////////////////////////////////////////////////////////////////////////
    public static final int MESSAGE_EVENT_TEST = 2000;
    public static final int MESSAGE_EVENT_USB_MOUNTED = 2001;//USB 存储器挂载成功
    public static final int MESSAGE_EVENT_USB_UNMOUNT = 2002;//USB 存储器卸载完成
    public static final int MESSAGE_EVENT_USB_CHECKING = 2003;//USB 磁盘检测中
    public static final int MESSAGE_EVENT_USB_EJECT = 2004;//USB 存储器卸载
    public static final int MESSAGE_EVENT_USB_ATTACHED = 2005;//USB 插上
    public static final int MESSAGE_EVENT_USB_REMOVED = 2006;////USB 完全拔出
    public static final int MESSAGE_EVENT_USB_DETACHED = 2007;//USB 完全拔出
    public static final int MESSAGE_EVENT_USB_SCANNING_FINISHED = 2008;//USB
    ////////////////////////////////////////////////////////////////////////////////////
    public static final int MESSAGE_EVENT_MEDIA_LIBRARY = 3000; //媒体库
    public static final int MESSAGE_EVENT_LOCAL_VIDEO = 3001;   //本地媒体库视频更新
    public static final int MESSAGE_EVENT_LOCAL_AUDIO = 3002;   //本地媒体库音频更新
    public static final int MESSAGE_EVENT_USB_VIDEO = 3003;     //USB媒体库视频更新
    public static final int MESSAGE_EVENT_USB_AUDIO = 3004;     //USB媒体库音频更新
    public static final int MESSAGE_EVENT_SD_VIDEO = 3005;      //USB媒体SD卡更新
    public static final int MESSAGE_EVENT_SD_AUDIO = 3006;      //USB媒体SD卡更新
    public static final int MESSAGE_EVENT_FILES = 3007;        //文件

    public static final int MESSAGE_EVENT_OCTOPUS_CAR_CLIENT = 3100;
    public static final int MESSAGE_EVENT_OCTOPUS_CAR_SERVICE = 3101;

    public static final int MESSAGE_EVENT_OCTOPUS_PLAY_PAUSE = 3200;
    public static final int MESSAGE_EVENT_OCTOPUS_PLAY = 3201;
    public static final int MESSAGE_EVENT_OCTOPUS_PAUSE =3202;
    public static final int MESSAGE_EVENT_OCTOPUS_NEXT = 3203;
    public static final int MESSAGE_EVENT_OCTOPUS_PREV = 3204;
    public static final int MESSAGE_EVENT_OCTOPUS_STOP = 3205;
    public static final int MESSAGE_EVENT_OCTOPUS_TITLE_CHANGED = 3206;
    public static final int MESSAGE_EVENT_OCTOPUS_PLAYING_STATUS = 3207;
    public static final int MESSAGE_EVENT_OCTOPUS_AIDL_PLAYING_STATUS = 3208;
    public static final int MESSAGE_EVENT_OCTOPUS_AIDL_START_REGISTER = 3209;
    ////////////////////////////////////////////////////////////////////////////////////////////////////////////
    ////////////////////////////////////////////////////////////////////////////////////////////////////////////
    //事件常量
    public static final String MESSAGE_EVENT_OCTOPUS_ACTION_TEST = "com.octopus.android.action.OCTOPUS_TEST";
    public static final String MESSAGE_EVENT_OCTOPUS_ACTION_HELLO = "com.octopus.android.action.OCTOPUS_HELLO";
    public static final String MESSAGE_EVENT_OCTOPUS_ACTION_CAR_CLIENT = "com.octopus.android.action.CAR_CLIENT";
    public static final String MESSAGE_EVENT_OCTOPUS_ACTION_CAR_SERVICE = "com.octopus.android.action.CAR_SERVICE";

    public static final String MESSAGE_EVENT_OCTOPUS_ACTION_CANBOX_SERVICE = "com.zhuchao.android.car.action.CANBOX_SERVICE";
    public static final String MESSAGE_EVENT_OCTOPUS_ACTION_MULTIMEDIA_SERVICE = "com.zhuchao.android.car.action.MULTIMEDIA_SERVICE";
    public static final String MESSAGE_EVENT_OCTOPUS_ACTION_RECORD_SERVICE = "com.zhuchao.android.car.action.RECORDER_SERVICE";
    public static final String MESSAGE_EVENT_MACHINE_ACTION_CONFIG_UPDATE = "com.octopus.android.MachineConfig_update";
    public static final String MESSAGE_EVENT_OCTOPUS_ACTION_DISABLE_AUTO_PLAY = "com.octopus.android.action.DISABLE_AUTO_PLAY";
    public static final String MESSAGE_EVENT_OCTOPUS_ACTION_AUTO_PLAY = "com.octopus.android.action.AUTO_PLAY";
    public static final String MESSAGE_EVENT_OCTOPUS_ACTION_PLAY_PAUSE = "com.octopus.android.action.PLAY_PAUSE";
    public static final String MESSAGE_EVENT_OCTOPUS_ACTION_PLAY = "com.octopus.android.action.PLAY";
    public static final String MESSAGE_EVENT_OCTOPUS_ACTION_PAUSE = "com.octopus.android.action.PAUSE";
    public static final String MESSAGE_EVENT_OCTOPUS_ACTION_NEXT = "com.octopus.android.action.NEXT";
    public static final String MESSAGE_EVENT_OCTOPUS_ACTION_PREV = "com.octopus.android.action.PREVIOUS";

    public static final String MESSAGE_EVENT_OCTOPUS_ACTION_RECORDER = "com.octopus.android.action.RECORDER";
    public static final String MESSAGE_EVENT_OCTOPUS_ACTION_RECORDER_START = "com.octopus.android.action.RECORDER.START";
    public static final String MESSAGE_EVENT_OCTOPUS_ACTION_RECORDER_STOP = "com.octopus.android.action.RECORDER.STOP";
    ////////////////////////////////////////////////////////////////////////////////////////////////////////////
    ///第三方消息事件
    public final static String MESSAGE_EVENT_LINK_Z = "com.zjinnova.zlink";
    public final static String MESSAGE_EVENT_LINK_CARLETTER = "com.carletter.link";

    ////////////////////////////////////////////////////////////////////////////////////////////////////////////
    //组件名称
    public final static String OCTOPUS_COMPONENT_NAME_MIDDLE_SERVICE = "com.zhuchao.android.car.service";
    public final static String MESSAGE_EVENT_AIDL_PROCESS_SERVICE_NAME = "com.zhuchao.android.car";
    public final static String MESSAGE_EVENT_AIDL_PACKAGE_NAME = "com.zhuchao.android.car";
    public final static String MESSAGE_EVENT_AIDL_CANBOX_CLASS_NAME = "com.zhuchao.android.car.service.CanboxService";
    public final static String MESSAGE_EVENT_AIDL_MUSIC_CLASS_NAME = "com.zhuchao.android.car.service.MultimService";
    public final static String MESSAGE_EVENT_AIDL_BT_CLASS_NAME = "com.zhuchao.android.car.service.BtService";
    public final static String MESSAGE_EVENT_AIDL_RECORDER_CLASS_NAME = "com.zhuchao.android.car.service.RecordService";


}
