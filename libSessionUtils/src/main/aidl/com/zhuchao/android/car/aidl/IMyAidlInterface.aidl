// IMyAidlInterface.aidl
package com.zhuchao.android.car.aidl;

// Declare any non-default types here with import statements

import com.zhuchao.android.car.aidl.PEventCourier;
import com.zhuchao.android.car.aidl.PMovie;
import com.zhuchao.android.car.aidl.IMyAidlInterfaceListener;
interface IMyAidlInterface {
    /**
     * Demonstrates some basic types that you can use as parameters
     * and return values in AIDL.
     */
    //void basicTypes(int anInt, long aLong, boolean aBoolean, float aFloat,double aDouble, String aString);
    void registerListener(IMyAidlInterfaceListener iMyAidlInterfaceListener);
    void unregisterListener(IMyAidlInterfaceListener iMyAidlInterfaceListener);
    void sendMessage(in PEventCourier Msg);
}