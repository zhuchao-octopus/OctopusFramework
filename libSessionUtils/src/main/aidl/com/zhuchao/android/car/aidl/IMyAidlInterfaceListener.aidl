// IMyAidlInterfaceListener.aidl
package com.zhuchao.android.car.aidl;

import com.zhuchao.android.car.aidl.PEventCourier;
import com.zhuchao.android.car.aidl.PMovie;
// Declare any non-default types here with import statements

interface IMyAidlInterfaceListener {
    /**
     * Demonstrates some basic types that you can use as parameters
     * and return values in AIDL.
     */
    //void basicTypes(int anInt, long aLong, boolean aBoolean, float aFloat, double aDouble, String aString);
    void onMessageAidlInterface(in PEventCourier msg);
    void onMessageMusic(int MsgId,int status,long timeChanged,long length,in PMovie pMovie);
}