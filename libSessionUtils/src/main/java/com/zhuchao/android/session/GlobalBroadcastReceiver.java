package com.zhuchao.android.session;

import android.annotation.SuppressLint;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.hardware.usb.UsbDevice;
import android.hardware.usb.UsbManager;
import android.media.AudioManager;

import com.zhuchao.android.fbase.EventCourier;
import com.zhuchao.android.fbase.MMLog;
import com.zhuchao.android.fbase.MessageEvent;
import com.zhuchao.android.fbase.TAppProcessUtils;
import com.zhuchao.android.fbase.TAppUtils;

import java.util.Objects;

public class GlobalBroadcastReceiver extends BroadcastReceiver {
    private static final String TAG = "GlobalBroadcastReceiver";
    //public static final String Action_OCTOPUS_permission = "com.octopus.android.action.OCTOPUS.PERMISSION";
    public static final String ACTION_BOOT_COMPLETED = "android.intent.action.BOOT_COMPLETED";

    public static GlobalBroadcastReceiver mGlobalBroadcastReceiver = null;

    @SuppressLint("UnspecifiedRegisterReceiverFlag")
    public synchronized static void registerGlobalBroadcastReceiver(Context context) {
        if(mGlobalBroadcastReceiver == null) {
            mGlobalBroadcastReceiver = new GlobalBroadcastReceiver();
            IntentFilter intentFilter = new IntentFilter();
            intentFilter.addAction(Intent.ACTION_MEDIA_SHARED); //如果SDCard未安装,并通过USB大容量存储共享返回
            intentFilter.addAction(Intent.ACTION_MEDIA_MOUNTED); //表明sd对象是存在并具有读/写权限
            intentFilter.addAction(Intent.ACTION_MEDIA_UNMOUNTED); //SDCard已卸掉,如果SDCard是存在但没有被安装
            intentFilter.addAction(Intent.ACTION_MEDIA_CHECKING); //表明对象正在磁盘检查
            intentFilter.addAction(Intent.ACTION_MEDIA_EJECT); //物理的拔出 SDCARD
            intentFilter.addAction(Intent.ACTION_MEDIA_REMOVED); //完全拔出
            intentFilter.addDataScheme("file");
            intentFilter.addAction(UsbManager.ACTION_USB_DEVICE_ATTACHED);
            intentFilter.addAction(UsbManager.ACTION_USB_DEVICE_DETACHED);
            //intentFilter.addAction(UsbManager.ACTION_USB_STATE);
            intentFilter.addAction(UsbManager.ACTION_USB_ACCESSORY_ATTACHED);
            intentFilter.addAction(UsbManager.ACTION_USB_ACCESSORY_DETACHED);

            intentFilter.addAction(Intent.ACTION_LOCALE_CHANGED);
            intentFilter.addAction(Intent.ACTION_CONFIGURATION_CHANGED);

            intentFilter.addAction(MessageEvent.MESSAGE_EVENT_OCTOPUS_ACTION_HELLO);
            intentFilter.addAction("com.zhuchao.android.ACTION_TEST");
            context.registerReceiver(mGlobalBroadcastReceiver, intentFilter);
        }
    }

    public synchronized static void unregisterGlobalBroadcastReceiver(Context context) {
        try {
            if (mGlobalBroadcastReceiver != null)
                context.unregisterReceiver(mGlobalBroadcastReceiver);
            mGlobalBroadcastReceiver = null;
        } catch (Exception e) {
            //throw new RuntimeException(e);
        }
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        MMLog.d(TAG, TAG + " action=" + intent.getAction() + " "+context.getPackageName());

        switch (Objects.requireNonNull(intent.getAction())) {
            case ACTION_BOOT_COMPLETED:
                Intent intent1 = new Intent();
                intent1.setComponent(new ComponentName("com.zhuchao.android.car", "com.zhuchao.android.car.service.MMCarService"));
                context.startService(intent1);
                break;
            case MessageEvent.MESSAGE_EVENT_OCTOPUS_ACTION_HELLO:
                MMLog.mm(null);
                MMLog.mm("*Hello "+ TAppProcessUtils.getCurrentProcessNameAndId(context));
                //Cabinet.getEventBus().printAllEventListener();
                MMLog.mm(Cabinet.getEventBus().getEventListeners());
                MMLog.m(TAG);
                break;
            case Intent.ACTION_MEDIA_MOUNTED:
                Cabinet.getEventBus().post(new EventCourier(mGlobalBroadcastReceiver.getClass(),MessageEvent.MESSAGE_EVENT_USB_MOUNTED));
                break;
            case Intent.ACTION_MEDIA_UNMOUNTED:
                Cabinet.getEventBus().post(new EventCourier(mGlobalBroadcastReceiver.getClass(),MessageEvent.MESSAGE_EVENT_USB_UNMOUNT));
                break;
            case Intent.ACTION_MEDIA_EJECT:
                break;
            case UsbManager.ACTION_USB_DEVICE_ATTACHED:
            case UsbManager.ACTION_USB_DEVICE_DETACHED:
                UsbDevice usbDevice = intent.getParcelableExtra(UsbManager.EXTRA_DEVICE);
                if (usbDevice != null) {
                    MMLog.i(TAG, "getProductId=" + usbDevice.getProductId());
                    MMLog.i(TAG, "getVendorId=" + usbDevice.getVendorId());
                    MMLog.i(TAG, "getSerialNumber=" + usbDevice.getSerialNumber());
                }
                break;
        }
    }

    public static void unMute(Context context) {
        AudioManager audioManager = (AudioManager) context.getSystemService(Context.AUDIO_SERVICE);
        int currVolume = audioManager.getStreamVolume(AudioManager.STREAM_MUSIC);
        //if (CABINET.properties.getInt("protocol") == 1570)
        {
            int maxVolume = audioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC);
            MMLog.i(TAG, "GlobalBroadcastReceiver maxVolume = " + maxVolume + " currVolume = " + currVolume);
            //audioManager.adjustStreamVolume(AudioManager.STREAM_MUSIC, AudioManager.ADJUST_UNMUTE, AudioManager.FLAG_SHOW_UI); //取消系统静音
            if (currVolume < maxVolume) {
                audioManager.setStreamVolume(AudioManager.STREAM_MUSIC, maxVolume, AudioManager.FLAG_PLAY_SOUND);
                MMLog.i(TAG, "GlobalBroadcastReceiver currVolume = " + currVolume);
            }
        }
    }

}
