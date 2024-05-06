// IMyCarAidlInterfaceListener.aidl
package com.zhuchao.android.car;

import com.zhuchao.android.car.PEventCourier;
// Declare any non-default types here with import statements

interface IMyCarAidlInterfaceListener {
    /**
     * Demonstrates some basic types that you can use as parameters
     * and return values in AIDL.
     */
    //void basicTypes(int anInt, long aLong, boolean aBoolean, float aFloat, double aDouble, String aString);
    void onMessageCarAidlInterface(in PEventCourier msg);
    void onMessageMusice(int MsgId,int status,long timeChanged,long length,String filePathName);
}