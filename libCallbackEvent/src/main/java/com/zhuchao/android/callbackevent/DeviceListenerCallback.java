package com.zhuchao.android.callbackevent;

public interface DeviceListenerCallback {
    void onDeviceEvent(Object obj, int deviceType,int eventType,byte[] bytes);
}
