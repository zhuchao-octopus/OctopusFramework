package com.zhuchao.android.session;

import static com.zhuchao.android.fileutils.FileUtils.NotEmptyString;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Build;
import android.view.KeyEvent;

import com.zhuchao.android.TPlatform;
import com.zhuchao.android.callbackevent.TaskCallback;
import com.zhuchao.android.fileutils.MMLog;
import com.zhuchao.android.fileutils.TAppUtils;
import com.zhuchao.android.net.NetworkInformation;
import com.zhuchao.android.net.TNetUtils;

import org.json.JSONException;
import org.json.JSONObject;

public class TSunshineEvent implements TNetUtils.NetworkStatusListener {
    private final static String TAG = "TSunshineEvent";
    private final static String Action_SystemShutdown = "action.uniwin.shutdown";
    private final static String Action_SystemShutdown1 = "android.intent.action.ACTION_REQUEST_SHUTDOWN";
    private final static String Action_SystemReboot1 = "android.intent.action.ACTION_REQUEST_REBOOT";
    private final static String Action_SystemReboot = "action.uniwin.reboot.receiver";
    private final static String Action_SystemAdjustTouchTscal = "org.zeroxlab.util.tscal";
    private final static String Action_SystemAdjustTouchCalibration = "org.zeroxlab.util.tscal.TSCalibration";
    private final static String Action_SystemResolution = "action.ktv.settings.receiver";
    private final static String Action_SystemEthertConfig = "action.ktv.net.receiver";
    private final static String Action_SystemDeviceUUID = "action.device.uuid.receiver";
    private final static String Action_SilentInstallAction = "android.intent.action.SILENT_INSTALL_PACKAGE";
    private  Context mContext = null;
    private  TNetUtils tNetUtils = null;
    private  TTaskManager tTaskManager = null;

    public TSunshineEvent(Context context) {
        mContext = context;
        registerSunshineEventReceiver();
        //tNetUtils = new TNetUtils(mContext);
        //tTaskManager = new TTaskManager(mContext);
        ///tTaskManager.setReDownload(false);
    }

    private void registerSunshineEventReceiver() {
        try {
            IntentFilter intentFilter = new IntentFilter();
            intentFilter.addAction(Action_SystemShutdown);//关机
            intentFilter.addAction(Action_SystemShutdown1);//关机
            intentFilter.addAction(Action_SystemReboot);//重启
            intentFilter.addAction(Action_SystemReboot1);//重启
            intentFilter.addAction(Action_SystemAdjustTouchTscal);//触摸屏校准
            intentFilter.addAction(Action_SystemAdjustTouchCalibration);//触摸屏校准
            intentFilter.addAction(Action_SystemResolution);//设置分辨率
            intentFilter.addAction(Action_SystemEthertConfig);//以太网设置(有线网络)
            intentFilter.addAction(Action_SystemDeviceUUID);//获取设备序列号
            intentFilter.addAction(Action_SilentInstallAction);//获取设备序列号
            mContext.registerReceiver(SunshineEventReceiver, intentFilter);
            MMLog.d(TAG, "register SunshineEventReceiver successfully !!!!!!");
        } catch (Exception e) {
            MMLog.e(TAG, "register SunshineEventReceiver failed!" + e.toString());
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
            if(intent == null) return;
            final String action = intent.getAction();
            MMLog.log(TAG, "SunshineEvent intent action = " + action);

            switch (action) {
                case Action_SystemShutdown:
                case Action_SystemShutdown1:
                    Action_SystemShutdown();
                    break;
                case Action_SystemReboot:
                case Action_SystemReboot1:
                    Action_SystemReboot();
                    break;
                case Action_SystemAdjustTouchTscal:
                    Action_SystemAdjustTouchTscal();
                    break;
                case Action_SystemAdjustTouchCalibration:
                    Action_SystemAdjustTouchCalibration();
                    break;
                case Action_SystemResolution:
                    Action_SystemResolution();
                    break;
                case Action_SystemEthertConfig:
                    Action_SystemEthertConfig();
                    break;
                case Action_SystemDeviceUUID:
                    Action_SystemGetDeviceUUID();
                    break;
                case Action_SilentInstallAction:
                    if(intent.getExtras()!=null) {
                        String apkFilePath = intent.getExtras().getString("apkFilePath");
                        boolean autostart = intent.getExtras().getBoolean("autostart");
                        Action_SilentInstallAction(apkFilePath,autostart);
                    }
                    break;
                default:break;
            }
        }
    };

    public void Action_SystemShutdown() {
        //TPlatform.sendKeyCode(KeyEvent.KEYCODE_F9);
        //TPlatform.sendKeyEvent(KeyEvent.KEYCODE_F9);
        TPlatform.ExecConsoleCommand("reboot -p");
    }

    public void Action_SystemReboot() {
        TPlatform.ExecConsoleCommand("reboot");
    }

    public void Action_SystemAdjustTouchTscal() {

    }

    public void Action_SystemAdjustTouchCalibration() {

    }

    public void Action_SystemResolution() {

    }

    public void Action_SystemEthertConfig() {

    }

    public String Action_SystemGetDeviceUUID() {
        return TPlatform.GetCPUSerialCode();
    }

    public void Action_SilentInstallAction(String filePath,boolean autostart)
    {
        TAppUtils.installSilent(mContext,filePath);
    }

    private static String getFWVersionName()
    {
        String version= android.os.Build.MODEL + ","
                + Build.VERSION.SDK_INT + ","
                + android.os.Build.VERSION.RELEASE;
        return version;
    }

    private static String getRequestJSON(String mac,String ip,String region) {
        JSONObject jsonObj = new JSONObject();
        try {
            jsonObj.put("name", "A40I");
            jsonObj.put("mac", mac);
            jsonObj.put("ip", ip);
            jsonObj.put("region", region);
            jsonObj.put("brand", "SunShine");
            jsonObj.put("customer", "TianPu");
            //jsonObj.put("appVersion",getFWVersionName());
            jsonObj.put("fwVersion", getFWVersionName());
        } catch (JSONException e) {
            //e.printStackTrace();
        }
        //MMLog.d(TAG,"getRequestJSON = "+jsonObj.toString());
        return jsonObj.toString();
    }


    @Override
    public void onNetStatusChanged(NetworkInformation networkInformation) {
        if (networkInformation.isConnected() &&
                NotEmptyString(networkInformation.getInternetIP()) &&
                NotEmptyString(networkInformation.getLocalIP()))
        {
          String json = getRequestJSON(networkInformation.getMAC(),networkInformation.getInternetIP(),networkInformation.regionToString());
          if(tTaskManager !=null)
             tTaskManager.testRequest(json);
        }
    }
}
