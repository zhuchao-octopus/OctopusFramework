package com.zhuchao.android.session;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.hardware.usb.UsbDevice;
import android.hardware.usb.UsbManager;
import android.media.AudioManager;

import com.zhuchao.android.fbase.EC;
import com.zhuchao.android.fbase.MMLog;
import com.zhuchao.android.fbase.MessageEvent;
import com.zhuchao.android.session.Cabinet;

import java.util.Objects;

public class MultimediaBroadcastReceiver extends BroadcastReceiver {
    private static final String TAG = "MultimediaBroadcastReceiver";
    public static final String Action_OCTOPUS_permission = "com.octopus.android.action.OCTOPUS.PERMISSION";
    public static final String ACTION_BOOT_COMPLETED = "android.intent.action.BOOT_COMPLETED";
    public static final String Action_OCTOPUS_HELLO = "com.octopus.android.action.OCTOPUS_HELLO";

    @Override
    public void onReceive(Context context, Intent intent) {
        MMLog.d(TAG, "MultimediaBroadcastReceiver action=" + intent.getAction());

        switch (Objects.requireNonNull(intent.getAction())) {
            case ACTION_BOOT_COMPLETED:
                //Intent intent1 = new Intent(context, MultimediaService.class);
                //context.startService(intent1);
                break;
            case Action_OCTOPUS_HELLO:;//
                break;
            case Intent.ACTION_MEDIA_MOUNTED:
                Cabinet.getEventBus().post(new EC(MessageEvent.MESSAGE_EVENT_USB_MOUNTED));
                break;
            case Intent.ACTION_MEDIA_UNMOUNTED:
                Cabinet.getEventBus().post(new EC(MessageEvent.MESSAGE_EVENT_USB_UNMOUNT));
                break;
            case Intent.ACTION_MEDIA_EJECT:
                break;
            case UsbManager.ACTION_USB_DEVICE_ATTACHED:
            case UsbManager.ACTION_USB_DEVICE_DETACHED:
                 UsbDevice usbDevice = intent.getParcelableExtra(UsbManager.EXTRA_DEVICE);
                 if(usbDevice != null) {
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
