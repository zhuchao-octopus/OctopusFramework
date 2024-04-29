package com.zhuchao.android.session;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;

import com.zhuchao.android.fbase.MMLog;
import com.zhuchao.android.fbase.MessageEvent;
import com.zhuchao.android.fbase.MethodThreadMode;
import com.zhuchao.android.fbase.TCourierSubscribe;
import com.zhuchao.android.fbase.eventinterface.EventCourierInterface;

public class MultimediaService extends Service {
    private static final String TAG = "MultimediaService";

    public MultimediaService() {
    }

    @Override
    public void onCreate() {
        super.onCreate();
        MMLog.d(TAG, "MultimediaService onCrete!");
        Cabinet.getEventBus().registerEventObserver(this);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Cabinet.getEventBus().unRegisterEventObserver(this);
        MMLog.d(TAG, "MultimediaService onDestroy!");
    }

    @Override
    public IBinder onBind(Intent intent) {
        // TODO: Return the communication channel to the service.
        //throw new UnsupportedOperationException("Not yet implemented");
        return null;
    }

    @TCourierSubscribe(threadMode = MethodThreadMode.threadMode.BACKGROUND)
    public boolean onTCourierSubscribeEvent(EventCourierInterface courierInterface) {
        switch (courierInterface.getId()) {
            case MessageEvent.MESSAGE_EVENT_USB_MOUNTED:
            case MessageEvent.MESSAGE_EVENT_USB_VIDEO:
            case MessageEvent.MESSAGE_EVENT_LOCAL_VIDEO:
            case MessageEvent.MESSAGE_EVENT_USB_UNMOUNT:
                break;
        }
        return true;
    }
}