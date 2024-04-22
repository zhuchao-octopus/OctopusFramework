package com.zhuchao.android.fbase;

import android.content.Intent;
import android.hardware.usb.UsbManager;

public class MessageEvent {
    ////////////////////////////////////////////////////////////////////////////////////
    public static final int MESSAGE_EVENT_USB_DEVICE = 2000;
    public static final int MESSAGE_EVENT_USB_MOUNTED = 2001;//USB 存储器挂载成功
    public static final int MESSAGE_EVENT_USB_UNMOUNT = 2002;//USB 存储器卸载完成
    public static final int MESSAGE_EVENT_USB_CHECKING = 2003;//USB 磁盘检测中
    public static final int MESSAGE_EVENT_USB_EJECT = 2004;//USB 存储器卸载失败
    public static final int MESSAGE_EVENT_USB_ATTACHED = 2005;//USB 插上
    public static final int MESSAGE_EVENT_USB_REMOVED = 2006;////USB 完全拔出
    public static final int MESSAGE_EVENT_USB_DETACHED = 2007;//USB 完全拔出

    ////////////////////////////////////////////////////////////////////////////////////
    public static final int MESSAGE_EVENT_MEDIA_LIBRARY = 3000; //媒体库
    public static final int MESSAGE_EVENT_LOCAL_VIDEO = 3001;   //本地媒体库视频更新
    public static final int MESSAGE_EVENT_LOCAL_AUDIO = 3002;   //本地媒体库音频更新
    public static final int MESSAGE_EVENT_USB_VIDEO = 3003;     //USB媒体库视频更新
    public static final int MESSAGE_EVENT_USB_AUDIO = 3004;     //USB媒体库音频更新
    public static final int MESSAGE_EVENT_SD_VIDEO = 3005;      //USB媒体SD卡更新
    public static final int MESSAGE_EVENT_SD_AUDIO = 3006;      //USB媒体SD卡更新
}
