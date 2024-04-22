package com.zhuchao.android.session;

import android.app.Application;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.hardware.usb.UsbManager;
import android.os.Build;
import android.view.WindowManager;

public class MApplication extends Application {
    protected static Context appContext = null;//需要使用的上下文对象
    public static WindowManager.LayoutParams LayoutParams = new WindowManager.LayoutParams();

    public static Context getAppContext() {
        return appContext;
    }

    private MultimediaBroadcastReceiver mMultimediaBroadcastReceiver = null;

    @Override
    public void onCreate() {
        super.onCreate();
        appContext = this.getApplicationContext();
        //layoutParams = View;
        /////////////////////////////////////////////////////////////////////////////////
        //初始化各模块组件
        mMultimediaBroadcastReceiver = new MultimediaBroadcastReceiver();
        registerBaseBroadcastReceiver();
        Cabinet.InitialModules(this);
        //////////////////////////////////////////////////////////////////////////////////
    }

    @Override
    public void onLowMemory() {
        super.onLowMemory();
    }

    @Override
    public void onTrimMemory(int level) {
        super.onTrimMemory(level);
    }

    @Override
    public void registerActivityLifecycleCallbacks(ActivityLifecycleCallbacks callback) {
        super.registerActivityLifecycleCallbacks(callback);
    }

    @Override
    public void onTerminate() {
        super.onTerminate();
        if (mMultimediaBroadcastReceiver != null) unregisterReceiver(mMultimediaBroadcastReceiver);
        Cabinet.FreeModules(this);
    }

    private void registerBaseBroadcastReceiver() {
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
        intentFilter.addAction(MultimediaBroadcastReceiver.Action_OCTOPUS_HELLO);
        //context.registerReceiver(mMultimediaBroadcastReceiver, intentFilter);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            registerReceiver(mMultimediaBroadcastReceiver, intentFilter, MultimediaBroadcastReceiver.Action_OCTOPUS_permission, null, RECEIVER_EXPORTED);
        }
    }
}
