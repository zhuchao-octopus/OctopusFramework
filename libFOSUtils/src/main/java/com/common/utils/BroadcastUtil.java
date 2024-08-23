package com.common.utils;

import android.content.Context;
import android.content.Intent;

public class BroadcastUtil {

    // ---------------以下是公共部分广播---------------

    /**
     * 发送按键广播
     *
     * @param context
     * @param pressType   MyCmd.KeyPressType
     * @param keyCode     MyCmd.Keycode
     * @param isNeedDelay 是否需要收到广播后延时处理.
     */

    public static void sendKey(Context context, byte pressType, int keyCode, boolean isNeedDelay) {
        Intent it = new Intent(MyCmd.ACTION_KEY_PRESS);
        it.putExtra(MyCmd.EXTRAS_KEY_PRESS_TYPE, pressType);
        it.putExtra(MyCmd.EXTRAS_KEY_CODE, keyCode);
        it.putExtra(MyCmd.EXTRAS_IS_NEED_DELAY, isNeedDelay);
        context.sendBroadcast(it);
    }

    public static void sendKey(Context context, int keyCode) {
        Intent it = new Intent(MyCmd.ACTION_KEY_PRESS);
        it.putExtra(MyCmd.EXTRAS_KEY_PRESS_TYPE, MyCmd.KeyPressType.SHORT_PRESS);
        it.putExtra(MyCmd.EXTRAS_KEY_CODE, keyCode);
        context.sendBroadcast(it);
    }

    public static void sendKey(Context context, int keyCode, byte target) {
        Intent it = new Intent(MyCmd.ACTION_KEY_PRESS);
        it.putExtra(MyCmd.EXTRA_COMMON_DATA, target);
        it.putExtra(MyCmd.EXTRAS_KEY_CODE, keyCode);
        context.sendBroadcast(it);
    }

    public static void sendKey(Context context, String packageName, int keyCode) {
        Intent it = new Intent(MyCmd.ACTION_KEY_PRESS);
        it.putExtra(MyCmd.EXTRAS_KEY_PRESS_TYPE, MyCmd.KeyPressType.SHORT_PRESS);
        it.putExtra(MyCmd.EXTRAS_KEY_CODE, keyCode);
        if (packageName != null) {
            it.setPackage(packageName);
        }
        context.sendBroadcast(it);
    }

    public static void sendToCarService(Context context, Intent it) {
        it.setPackage(AppConfig.getCarServicePackageName(context));
        context.sendBroadcast(it);
    }

    public static void sendToCarServiceCmd(Context context, int cmd) {
        Intent it;
        it = new Intent(MyCmd.BROADCAST_CMD_TO_CAR_SERVICE);
        it.putExtra(MyCmd.EXTRA_COMMON_CMD, cmd);
        it.setPackage(AppConfig.getCarServicePackageName(context));
        sendToCarService(context, it);
    }


    public final static void sendToCarService(Context context, int cmd, int data) {
        Intent it;
        it = new Intent(MyCmd.BROADCAST_CMD_TO_CAR_SERVICE);
        it.putExtra(MyCmd.EXTRA_COMMON_CMD, cmd);
        it.putExtra(MyCmd.EXTRA_COMMON_DATA, data);
        it.setPackage(AppConfig.getCarServicePackageName(context));
        sendToCarService(context, it);
    }

    public static void sendToCarService(Context context, int cmd, int data, int data2) {
        Intent it;
        it = new Intent(MyCmd.BROADCAST_CMD_TO_CAR_SERVICE);
        it.putExtra(MyCmd.EXTRA_COMMON_CMD, cmd);
        it.putExtra(MyCmd.EXTRA_COMMON_DATA, data);
        it.putExtra(MyCmd.EXTRA_COMMON_DATA2, data2);
        it.setPackage(AppConfig.getCarServicePackageName(context));
        sendToCarService(context, it);
    }

    public static void sendToCarService(Context context, int cmd, int data, int data2, int data3) {
        Intent it;
        it = new Intent(MyCmd.BROADCAST_CMD_TO_CAR_SERVICE);
        it.putExtra(MyCmd.EXTRA_COMMON_CMD, cmd);
        it.putExtra(MyCmd.EXTRA_COMMON_DATA, data);
        it.putExtra(MyCmd.EXTRA_COMMON_DATA2, data2);
        it.putExtra(MyCmd.EXTRA_COMMON_DATA3, data3);
        it.setPackage(AppConfig.getCarServicePackageName(context));
        sendToCarService(context, it);
    }

    /* mcu data start */
    public static void sendToCarServiceSetSource(Context context, int source) {
        sendToCarService(context, MyCmd.Cmd.SET_SOURCE, source);
    }

    /* mcu data end */

    public static void sendToCarServiceMcuRadio(Context context, int subId, int param1) {
        sendToCarService(context, MyCmd.Cmd.MCU_RADIO_SEND_CMD, subId, param1);
    }

    public static void sendToCarServiceMcuRadio(Context context, int subId, int param1, int param2) {
        sendToCarService(context, MyCmd.Cmd.MCU_RADIO_SEND_CMD, subId, param1, param2);

    }

    public static void sendToCarServiceMcuRds(Context context, int subId, int param1) {
        sendToCarService(context, MyCmd.Cmd.MCU_RDS_SEND_CMD, subId, param1);
    }

    public static void sendToCarServiceMcuRds(Context context, int subId, int param1, int param2) {
        sendToCarService(context, MyCmd.Cmd.MCU_RDS_SEND_CMD, subId, param1, param2);

    }

    public static void sendToCarServiceMcuEQ(Context context, int subId, int param1) {
        sendToCarService(context, MyCmd.Cmd.MCU_AUDIO_SEND_CMD, subId, param1);
    }

    public static void sendToCarServiceMcuEQ(Context context, int subId, int param1, int param2) {
        sendToCarService(context, MyCmd.Cmd.MCU_AUDIO_SEND_CMD, subId, param1, param2);
    }

    public static void sendToCarServiceMcuDVD(Context context, int subId, int param1, int param2) {
        sendToCarService(context, MyCmd.Cmd.MCU_DVD_SEND_CMD, subId, param1, param2);
    }

    public static void sendToCarServiceMcuDVD(Context context, int subId, int param1) {
        sendToCarService(context, MyCmd.Cmd.MCU_DVD_SEND_CMD, subId, param1);
    }

    public static void sendCanboxInfo(Context context, String name, int value1, int value2, int value3) {
        Intent i = new Intent(name);
        i.putExtra("value1", value1);
        i.putExtra("value2", value2);
        i.putExtra("value3", value3);
        sendToCarService(context, i);
    }

    public static void sendCanboxInfo(Context context, byte[] buf) {
        Intent i = new Intent(MyCmd.BROADCAST_SEND_TO_CAN);
        i.putExtra("buf", buf);
        sendToCarService(context, i);
    }

    // only car service send this.
    public static void sendByCarService(Context context, String packageName, int cmd) {
        Intent it = new Intent(MyCmd.BROADCAST_CAR_SERVICE_SEND);
        it.putExtra(MyCmd.EXTRA_COMMON_CMD, cmd);
        if (packageName != null) {
            it.setPackage(packageName);
        }
        context.sendBroadcast(it);
    }

    public static void sendByCarService(Context context, String packageName, int cmd, String s) {
        Intent it = new Intent(MyCmd.BROADCAST_CAR_SERVICE_SEND);
        it.putExtra(MyCmd.EXTRA_COMMON_CMD, cmd);
        it.putExtra(MyCmd.EXTRA_COMMON_DATA, s);
        if (packageName != null) {
            it.setPackage(packageName);
        }
        context.sendBroadcast(it);
    }

    public static void sendByCarService(Context context, String packageName, int cmd, byte[] buf) {
        Intent it = new Intent(MyCmd.BROADCAST_CAR_SERVICE_SEND);
        it.putExtra(MyCmd.EXTRA_COMMON_CMD, cmd);
        it.putExtra(MyCmd.EXTRA_COMMON_DATA, buf);
        if (packageName != null) {
            it.setPackage(packageName);
        }
        context.sendBroadcast(it);
    }

    public static void sendByCarService(Context context, String packageName, int cmd, int buf) {
        Intent it = new Intent(MyCmd.BROADCAST_CAR_SERVICE_SEND);
        it.putExtra(MyCmd.EXTRA_COMMON_CMD, cmd);
        it.putExtra(MyCmd.EXTRA_COMMON_DATA, buf);
        if (packageName != null) {
            it.setPackage(packageName);
        }
        context.sendBroadcast(it);
    }

    public static void sendByCarService(Context context, String[] packageName, int cmd, byte[] buf) {
        Intent it = new Intent(MyCmd.BROADCAST_CAR_SERVICE_SEND);
        it.putExtra(MyCmd.EXTRA_COMMON_CMD, cmd);
        it.putExtra(MyCmd.EXTRA_COMMON_DATA, buf);
        for (String s : packageName) {
            it.setPackage(s);
            context.sendBroadcast(it);
        }
    }

    public static void sendByCarServicePermission(Context context, String permission, int cmd, byte[] buf) {
        Intent it = new Intent(MyCmd.BROADCAST_CAR_SERVICE_SEND);
        it.putExtra(MyCmd.EXTRA_COMMON_CMD, cmd);
        it.putExtra(MyCmd.EXTRA_COMMON_DATA, buf);
        context.sendBroadcast(it, permission);
    }

    public static void sendByCarService(Context context, int cmd, int data) {
        Intent it = new Intent(MyCmd.BROADCAST_CAR_SERVICE_SEND);
        it.putExtra(MyCmd.EXTRA_COMMON_CMD, cmd);
        it.putExtra(MyCmd.EXTRA_COMMON_DATA, data);
        context.sendBroadcast(it);
    }

    public static void sendByCarService(Context context, int cmd, int data, int data2) {
        Intent it = new Intent(MyCmd.BROADCAST_CAR_SERVICE_SEND);
        it.putExtra(MyCmd.EXTRA_COMMON_CMD, cmd);
        it.putExtra(MyCmd.EXTRA_COMMON_DATA, data);
        it.putExtra(MyCmd.EXTRA_COMMON_DATA2, data2);
        context.sendBroadcast(it);
    }

    public static void sendByCarService(Context context, int cmd, int data, Object obj) {
        Intent it = new Intent(MyCmd.BROADCAST_CAR_SERVICE_SEND);
        it.putExtra(MyCmd.EXTRA_COMMON_CMD, cmd);
        it.putExtra(MyCmd.EXTRA_COMMON_DATA, data);
        context.sendBroadcast(it);
    }

    // // //////will del
    // /**
    // * 发送App源变化时通知
    // *
    // * @param context
    // * @param id_change
    // * 即将要切换到的源
    // * @param id_current
    // * 当前源，也就是即将被切换掉的源
    // */
    // public static void notifyAppSourceChange(Context context, int id_change,
    // int id_current) {
    // Intent versionIntent = new Intent(
    // MyCmd.ACTION_APP_SOURCE_ID_CHANGE);
    // versionIntent.putExtra(MyCmd.EXTRAS_APP_SOURCE_ID, id_change);
    // versionIntent.putExtra(MyCmd.EXTRAS_APP_SOURCE_ID_CURRENT,
    // id_current);
    // context.sendBroadcast(versionIntent);
    // }
    //
    // /**
    // * 有设备的有无状态发生了改变
    // *
    // * @param context
    // * @param deviceState
    // * 当前各种设备列表的有无状态
    // * @see MyCmd.DeviceType
    // */
    // public static void notifyDeviceStateChange(Context context, int
    // deviceState) {
    // Intent deviceStateIntent = new Intent(
    // MyCmd.ACTION_DEVICE_STATE_CHANGE);
    // deviceStateIntent.putExtra(MyCmd.EXTRAS_DEVICE_STATE,
    // deviceState);
    // context.sendBroadcast(deviceStateIntent);
    // }
    //
    // /**
    // * 导航软件设置发生了改变
    // *
    // * @param context
    // * @param pkgName
    // * 包名
    // * @param clzName
    // * 类名
    // */
    // public static void notifyNavigationChange(Context context, String
    // pkgName,
    // String clzName) {
    // Intent deviceStateIntent = new Intent(
    // MyCmd.ACTION_NAVIGATION_CHANGE);
    // deviceStateIntent.putExtra(MyCmd.EXTRAS_PACKAGE_NAME, pkgName);
    // deviceStateIntent.putExtra(MyCmd.EXTRAS_CLAZZ_NAME, clzName);
    // context.sendBroadcast(deviceStateIntent);
    // }
    //
    // /**
    // * 倒车状态 改变
    // *
    // * @param context
    // * @param isReverse
    // * 是否在倒车
    // * @param isReverseMte
    // * 是否在倒车静音
    // */
    // public static void notifyReverseChange(Context context, boolean
    // isReverse,
    // boolean isReverseMute) {
    // Intent reverseStatusIntent = new Intent(
    // MyCmd.ACTION_REVERSE_STATUS_CHANGE);
    // reverseStatusIntent.putExtra(MyCmd.EXTRAS_IS_REVERSE,
    // isReverse);
    // reverseStatusIntent.putExtra(MyCmd.EXTRAS_IS_REVERSE_MUTE,
    // isReverseMute);
    // context.sendBroadcast(reverseStatusIntent);
    // }
    //
    // /**
    // * 音量设置
    // *
    // * @param context
    // * @param type
    // * 1：加减 2：具体值 3:静音/觖静音
    // * @param value
    // * -4：静音 -3：解静音 -2：减 -1：加 其他：音量值
    // */
    // public static void setVolume(Context context, int type, int value) {
    // Intent setVolumeIntent = new Intent(MyCmd.ACTION_SET_VOLUME);
    // setVolumeIntent.putExtra(MyCmd.EXTRAS_SET_VOLUME_TYPE, type);
    // setVolumeIntent.putExtra(MyCmd.EXTRAS_SET_VOLUME_VALUE, value);
    // context.sendBroadcast(setVolumeIntent);
    // }
    //
    // /**
    // * 音量控制显示/隐藏
    // *
    // * @param context
    // * @param isShow
    // */
    // public static void showVolumeBar(Context context, boolean isShow) {
    // Intent controlVolumeBarIntent = new Intent(
    // MyCmd.ACTION_VOLUME_BAR_CONTROL);
    // controlVolumeBarIntent.putExtra(MyCmd.EXTRAS_IS_SHOW, isShow);
    // context.sendBroadcast(controlVolumeBarIntent);
    // }
    //
    // // ---------------以上是公共部分广播---------------
    //
    // // ---------------以下是蓝牙部分相关广播---------------
    // /**
    // * 发送蓝牙下载信息改变通知
    // *
    // * @param context
    // * @param downloadType
    // * 下载类型， 1：电话本， 2：所有通话记录， 3：未接电话记录， 4:呼入电话 记录, 5:呼出电话记录
    // * @param status
    // * 0： 下载异常， 1：开始下载， 2：下载结束
    // */
    // public static void notifyBtDownloadChange(Context context,
    // byte downloadType, byte status) {
    // Intent btDownloadIntent = new Intent(MyCmd.ACTION_BT_DOWNLOAD);
    // btDownloadIntent.putExtra(MyCmd.EXTRAS_BT_DOWNLOAD_TYPE,
    // downloadType);
    // btDownloadIntent.putExtra(MyCmd.EXTRAS_BT_DOWNLOAD_STATUS,
    // status);
    // context.sendBroadcast(btDownloadIntent);
    // }
    //
    // /**
    // * 发送App源变化时通知
    // *
    // * @param context
    // * @param downloadType
    // * 下载类型， 1：电话本， 2：所有通话记录， 3：未接电话记录， 4:呼入电话 记录, 5:呼出电话记录
    // * @param status
    // * true:成功 false：失败
    // */
    // public static void notifyBtDownloadChange(Context context,
    // byte downloadType, boolean isSyncSuccess) {
    // Intent btDownloadSuccessIntent = new Intent(
    // MyCmd.ACTION_BT_DOWNLOAD_SUCCESS);
    // btDownloadSuccessIntent.putExtra(
    // MyCmd.EXTRAS_BT_DOWNLOAD_TYPE, downloadType);
    // btDownloadSuccessIntent.putExtra(
    // MyCmd.EXTRAS_BT_DOWNLOAD_IS_SUCCESS, isSyncSuccess);
    // context.sendBroadcast(btDownloadSuccessIntent);
    // }
    //
    // /**
    // * 广播通知当前蓝牙电话状态发生改变
    // *
    // * @param mContext
    // * @param mPhoneState
    // * 蓝牙电话当前所处状态：1：正常待机， 2：来电铃声， 3：去电， 4:通话中
    // */
    // public static void notifyBtPhoneStateChange(Context context, byte
    // phoneState) {
    // Intent btPhoneStateChangeIntent = new Intent(
    // MyCmd.ACTION_BT_PHONE_STATE_CHANGE);
    // btPhoneStateChangeIntent.putExtra(MyCmd.EXTRAS_BT_PHONE_STATE,
    // phoneState);
    // context.sendBroadcast(btPhoneStateChangeIntent);
    // }
    //
    // /**
    // *
    // * @param mContext
    // * @param mHfpState
    // * 蓝牙电话HFP状态：0:初启化 1：未连接， 2：正在连接， 3：已连接， 4：拨出状态 5:来电状态 6:通话状态
    // */
    // public static void notifyBtHfpStateChange(Context context, byte hfpState)
    // {
    // Intent btHfpStateChangeIntent = new Intent(
    // MyCmd.ACTION_BT_HFP_STATE_CHANGE);
    // btHfpStateChangeIntent.putExtra(MyCmd.EXTRAS_BT_HFP_STATE,
    // hfpState);
    // context.sendBroadcast(btHfpStateChangeIntent);
    // }
    //
    // /**
    // * 当前蓝牙电话声音通道改变
    // *
    // * @param mContext
    // * @param isChannelAtCar
    // * true:在车机 false:在手机
    // */
    // public static void notifyBtVoiceChannelChange(Context context,
    // boolean isChannelAtCar) {
    // Intent btVoiceChannelChangeIntent = new Intent(
    // MyCmd.ACTION_BT_VOICE_CHANNEL_CHANGE);
    // btVoiceChannelChangeIntent.putExtra(
    // MyCmd.EXTRAS_BT_VOICE_CHANNEL_AT_CAR, isChannelAtCar);
    // context.sendBroadcast(btVoiceChannelChangeIntent);
    // }
    //
    // /**
    // * 当前通话中的信息
    // *
    // * @param mContext
    // * @param phoneName
    // * 通话中姓名
    // * @param phoneNumber
    // * 通话号码
    // */
    // public static void notifyBtPhoneNameAndNumber(Context context,
    // String phoneName, String phoneNumber) {
    // Intent btCallOnlineInfoIntent = new Intent(
    // MyCmd.ACTION_BT_CALL_ONLINE_INFO);
    // btCallOnlineInfoIntent.putExtra(
    // MyCmd.EXTRAS_BT_CALL_ONLINE_NAME, phoneName);
    // btCallOnlineInfoIntent.putExtra(
    // MyCmd.EXTRAS_BT_CALL_ONLINE_NUMBER, phoneNumber);
    // context.sendBroadcast(btCallOnlineInfoIntent);
    // }
    //
    // /**
    // * 蓝牙speek和mic音量改变
    // *
    // * @param mContext
    // * @param spkVol
    // * 蓝牙speek音量
    // * @param micVol
    // * 蓝牙mic音量
    // */
    // public static void notifyBtSpkMicVolumeChange(Context context, int
    // spkVol,
    // int micVol) {
    // Intent btSkpMicVolumeChangeIntent = new Intent(
    // MyCmd.ACTION_BT_SPEEK_MIC_VOLUME_CHANGE);
    // btSkpMicVolumeChangeIntent.putExtra(
    // MyCmd.EXTRAS_BT_SPEEK_VOLUME, spkVol);
    // btSkpMicVolumeChangeIntent.putExtra(
    // MyCmd.EXTRAS_BT_MIC_VOLUME, micVol);
    // context.sendBroadcast(btSkpMicVolumeChangeIntent);
    // }
    //
    // /**
    // * 蓝牙Mic状态改变
    // *
    // * @param mContext
    // * @param isOpen
    // * true:打开 false:关闭
    // */
    // public static void notifyBtMicStateChange(Context context, boolean
    // isOpen) {
    // Intent btMicStateChangeIntent = new Intent(
    // MyCmd.ACTION_BT_MIC_STATE_CHANGE);
    // btMicStateChangeIntent.putExtra(MyCmd.EXTRAS_BT_MIC_STATE,
    // isOpen);
    // context.sendBroadcast(btMicStateChangeIntent);
    // }
    // // ---------------以上是蓝牙部分相关广播---------------

}
