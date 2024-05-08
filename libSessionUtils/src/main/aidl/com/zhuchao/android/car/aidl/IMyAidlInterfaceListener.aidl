// IMyAidlInterfaceListener.aidl
package com.zhuchao.android.car.aidl;

import com.zhuchao.android.car.aidl.PEventCourier;
// Declare any non-default types here with import statements

interface IMyAidlInterfaceListener {
    /**
     * Demonstrates some basic types that you can use as parameters
     * and return values in AIDL.
     */
    //void basicTypes(int anInt, long aLong, boolean aBoolean, float aFloat, double aDouble, String aString);
    void onMessageAidlInterface(in PEventCourier msg);
    void onMessageMusice(int MsgId,int status,long timeChanged,long length,String filePathName);
}