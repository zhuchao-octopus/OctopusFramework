package com.zhuchao.android.fbase.eventinterface;

import com.zhuchao.android.fbase.TCourierEventBus;

import org.jetbrains.annotations.NotNull;

public interface TCourierEventBusInterface {
    public void registerEventObserver(@NotNull Object context);
    public void registerEventObserver(@NotNull String tag, @NotNull TCourierEventListener courierEventListener);
    public void unRegisterEventObserver(@NotNull String tag);
    public void unRegisterEventObserver(@NotNull Object courierEventListener);


    public void postMain(EventCourierInterface eventCourier);
    public void post(EventCourierInterface eventCourier);
    public void post(Object eventCourier);
    public void postDelay(Object eventCourier, long millis);
    public void printAllEventListener();
    public String getEventListeners();
    public void free();
}
