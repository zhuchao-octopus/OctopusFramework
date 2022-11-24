package com.zhuchao.android.watchmanservice;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Build;

import com.zhuchao.android.fileutils.MMLog;
import com.zhuchao.android.session.WatchManService;

public class BootBroadCastReceiver extends BroadcastReceiver {
    private static final String TAG = "BootBroadcastReceiver";
    private static final String ACTION = "android.intent.action.BOOT_COMPLETED";

    @Override
    public void onReceive(Context context, Intent intent) {
        // TODO: This method is called when the BroadcastReceiver is receiving
        // an Intent broadcast.
        //throw new UnsupportedOperationException("Not yet implemented");
        MMLog.d(TAG,intent.getAction().toString());
        if (intent.getAction().equals(ACTION)) {
            Intent intent1 = new Intent(context, WatchManService.class);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                //android8.0以上通过startForegroundService启动service
                context.startForegroundService(intent1);
            } else {
                context.startService(intent1);
            }
        }

    }
}