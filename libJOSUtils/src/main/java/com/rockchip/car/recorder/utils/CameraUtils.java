package com.rockchip.car.recorder.utils;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.ContextWrapper;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.UserHandle;


import java.util.HashMap;

/**
 * Created by Administrator on 2016/8/5.
 */
public class CameraUtils {
    private static final String TAG = "CameraUtils";
//    public static ServiceImpl.CameraBinder sService = null;
    private static HashMap<Context, ServiceBinder> sConnectionMap = new HashMap<Context, ServiceBinder>();
    public static ServiceToken bindToService(Activity context, ServiceConnection callback, Class cla) {
        Activity realActivity = context.getParent();
        if (realActivity == null) {
            realActivity = context;
        }
        ContextWrapper cw = new ContextWrapper(realActivity);
        cw.startService(new Intent(cw, cla));
//        cw.startServiceAsUser(new Intent(cw, cla), UserHandle.CURRENT);
        ServiceBinder sb = new ServiceBinder(callback);
        if (cw.bindService((new Intent()).setClass(cw, cla), sb, 0)) {
            sConnectionMap.put(cw, sb);
            return new ServiceToken(cw);
        }
        return null;
    }

    public static void unbindFromService(ServiceToken token) {
        if (token == null) {
            SLog.e(TAG, "Trying to unbind with null token");
            return;
        }
        ContextWrapper cw = token.mWrappedContext;
        ServiceBinder sb = sConnectionMap.remove(cw);
        if (sb == null) {
            SLog.e(TAG, "Trying to unbind for unknown Context");
            return;
        }
        cw.unbindService(sb);
//        if (sConnectionMap.isEmpty()) {
//            sService = null;
//        }
    }

    public static class ServiceToken {
        ContextWrapper mWrappedContext;

        ServiceToken(ContextWrapper context) {
            mWrappedContext = context;
        }
    }

    private static class ServiceBinder implements ServiceConnection {
        ServiceConnection mCallback;

        ServiceBinder(ServiceConnection callback) {
            mCallback = callback;
        }

        public void onServiceConnected(ComponentName className,
                                       android.os.IBinder service) {
            if (mCallback != null) {
                mCallback.onServiceConnected(className, service);
            }
        }

        public void onServiceDisconnected(ComponentName className) {
            if (mCallback != null) {
                mCallback.onServiceDisconnected(className);
            }
//            sService = null;
        }
    }
}
