// IMyCarAidlInterface.aidl
package com.zhuchao.android.car;

// Declare any non-default types here with import statements

import com.zhuchao.android.car.PEventCourier;
import com.zhuchao.android.car.IMyCarAidlInterfaceListener;
interface IMyCarAidlInterface {
    /**
     * Demonstrates some basic types that you can use as parameters
     * and return values in AIDL.
     */
    //void basicTypes(int anInt, long aLong, boolean aBoolean, float aFloat,double aDouble, String aString);
    void registerListener(IMyCarAidlInterfaceListener iMyCarAidlInterfaceListener);
    void unregisterListener(IMyCarAidlInterfaceListener iMyCarAidlInterfaceListener);
    void sendMessage(in PEventCourier Msg);
    String getHelloData();

}