package com.zhuchao.android.session.recorder;

import static com.zhuchao.android.fbase.MessageEvent.MESSAGE_EVENT_OCTOPUS_ACTION_RECORDER;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import com.zhuchao.android.fbase.MMLog;

public class RecordVideoBootReceiver extends BroadcastReceiver {
    private static final String TAG = "RecordVideoBootReceiver";

    @Override
    public void onReceive(Context context, Intent intent) {
        String action = intent.getAction();
        MMLog.d(TAG, "onReceive action : " + action);

        Intent mIntent = new Intent(context, RecordVideoService.class);
        mIntent.setAction(MESSAGE_EVENT_OCTOPUS_ACTION_RECORDER);
        context.startService(mIntent);
        Log.d(TAG, "start recorder service");
    }
}
