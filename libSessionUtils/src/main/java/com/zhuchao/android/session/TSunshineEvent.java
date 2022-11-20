package com.zhuchao.android.session;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.view.KeyEvent;

import com.zhuchao.android.TPlatform;
import com.zhuchao.android.fileutils.MMLog;

public class TSunshineEvent {
    private final static String TAG = "TSunshineEvent";
    private Context mContext;
    private final static String SystemShutdown = "action.uniwin.shutdown";
    private final static String SystemReboot = "action.uniwin.reboot.receiver";
    private final static String SystemAdjustTouchTscal = "org.zeroxlab.util.tscal";
    private final static String SystemAdjustTouchCalibration = "org.zeroxlab.util.tscal.TSCalibration";
    private final static String SystemResolution = "action.ktv.settings.receiver";
    private final static String SystemEthertConfig = "action.ktv.net.receiver";
    private final static String SystemDeviceUUID = "action.device.uuid.receiver";

    public TSunshineEvent(Context context) {
        mContext = context;
        registerSunshineEventReceiver();
    }

    private void registerSunshineEventReceiver() {
        try {
            IntentFilter intentFilter = new IntentFilter();
            intentFilter.addAction(SystemShutdown);//关机
            intentFilter.addAction(SystemReboot);//重启
            intentFilter.addAction(SystemAdjustTouchTscal);//触摸屏校准
            intentFilter.addAction(SystemAdjustTouchCalibration);//触摸屏校准
            intentFilter.addAction(SystemResolution);//设置分辨率
            intentFilter.addAction(SystemEthertConfig);//以太网设置(有线网络)
            intentFilter.addAction(SystemDeviceUUID);//获取设备序列号
            mContext.registerReceiver(SunshineEventReceiver, intentFilter);
        } catch (Exception e) {
            MMLog.e(TAG, "register SunshineEventReceiver failed " + e.toString());
        }
    }

    public void unRegisterSunshineEventReceiver() {
        try {
            mContext.unregisterReceiver(SunshineEventReceiver);
        } catch (Exception e) {
            //e.printStackTrace();
        }
    }

    public void free() {
        unRegisterSunshineEventReceiver();
    }

    private final BroadcastReceiver SunshineEventReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            final String action = intent.getAction();
            MMLog.log(TAG, "SunshineEvent intent action = " + action);
            switch (action) {
                case SystemShutdown:
                    systemShutdown();
                    break;
                case SystemReboot:
                    systemReboot();
                    break;
                case SystemAdjustTouchTscal:
                    systemAdjustTouchTscal();
                    break;
                case SystemAdjustTouchCalibration:
                    systemAdjustTouchCalibration();
                    break;
                case SystemResolution:
                    systemResolution();
                    break;
                case SystemEthertConfig:
                    systemEthertConfig();
                    break;
                case SystemDeviceUUID:
                    systemGetDeviceUUID();
                    break;
                //default:break;
            }
        }
    };

    public void systemShutdown() {
        //TPlatform.sendKeyCode(KeyEvent.KEYCODE_F9);
        //TPlatform.sendKeyEvent(KeyEvent.KEYCODE_F9);
        TPlatform.sendConsoleCommand("reboot -p");
    }

    public void systemReboot() {
        TPlatform.sendConsoleCommand("reboot");
    }

    public void systemAdjustTouchTscal() {

    }

    public void systemAdjustTouchCalibration() {

    }

    public void systemResolution() {

    }

    public void systemEthertConfig() {

    }

    public String systemGetDeviceUUID() {
        return TPlatform.getCPUSerialCode();
    }
}
