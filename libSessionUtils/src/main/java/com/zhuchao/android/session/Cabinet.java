package com.zhuchao.android.session;

import android.annotation.SuppressLint;
import android.content.Context;

import com.zhuchao.android.fbase.DataID;
import com.zhuchao.android.fbase.MMLog;
import com.zhuchao.android.fbase.TAppUtils;
import com.zhuchao.android.fbase.TCourierEventBus;
import com.zhuchao.android.net.TNetUtils;
import com.zhuchao.android.persist.TPersistent;

import org.jetbrains.annotations.NotNull;

public class Cabinet {
    private static final String TAG = "CABINET";
    @SuppressLint("StaticFieldLeak")
    private static TPlayManager tPlayManager = null;
    @SuppressLint("StaticFieldLeak")
    private static TTaskManager tTaskManager = null;
    @SuppressLint("StaticFieldLeak")
    private static TNetUtils tNetUtils = null;
    @SuppressLint("StaticFieldLeak")
    private static TAppUtils tAppUtils = null;
    @SuppressLint("StaticFieldLeak")
    private static TDeviceManager tDeviceManager = null;
    @SuppressLint("StaticFieldLeak")
    private static TPersistent tPersistent = null;
    private static TCourierEventBus tCourierEventBus = null;


    public synchronized static TPlayManager getPlayManager(Context context) {
        if (tPlayManager == null && context != null) {
            tPlayManager = TPlayManager.getInstance(context);
            tPlayManager.setPlayOrder(DataID.PLAY_MANAGER_PLAY_ORDER2);//循环顺序播放
            tPlayManager.setAutoPlaySource(DataID.SESSION_SOURCE_FAVORITELIST);//自动播放源列表
        }
        return tPlayManager;
    }

    public synchronized static TPlayManager getPlayManager() {
        if (tPlayManager == null) {
            tPlayManager = TPlayManager.getInstance(MApplication.getAppContext());
            tPlayManager.setPlayOrder(DataID.PLAY_MANAGER_PLAY_ORDER2);//循环顺序播放
            tPlayManager.setAutoPlaySource(DataID.SESSION_SOURCE_FAVORITELIST);//自动播放源列表
        }
        return tPlayManager;
    }

    public synchronized TPersistent getPersistent(Context context) {
        if (tPersistent == null) return new TPersistent(context, context.getPackageName());
        else return tPersistent;
    }

    public static synchronized TCourierEventBus getEventBus() {
        if (tCourierEventBus == null) tCourierEventBus = new TCourierEventBus();
        return tCourierEventBus;
    }

    public static synchronized TTaskManager gettTaskManager(Context context) {
        if (tTaskManager == null) {
            tTaskManager = new TTaskManager(context);
        }
        return tTaskManager;
    }

    public static synchronized void InitialModules(@NotNull Context context) {
        try {
            getEventBus();
            getPlayManager(context);
            gettTaskManager(context);
        } catch (Exception e) {
            MMLog.e(TAG, e.toString());
        }
    }

    public static synchronized void FreeModules(@NotNull Context context) {
        try {
            if (tPlayManager != null) tPlayManager.free();
            if (tTaskManager != null) tTaskManager.free();
            if (tNetUtils != null) tNetUtils.free();
            if (tDeviceManager != null) tDeviceManager.closeAllUartDevice();
            if (tCourierEventBus != null) tCourierEventBus.free();
            if (tPersistent != null) tPersistent.commit();//持久化数据
            if (tAppUtils != null) tAppUtils.free();
        } catch (Exception e) {
            //e.printStackTrace();
        }
    }
}
