// IMyMediaAidlInterface.aidl
package com.zhuchao.android.car.aidl;

// Declare any non-default types here with import statements
import com.zhuchao.android.car.aidl.PEventCourier;
import com.zhuchao.android.car.aidl.PMovie;
import com.zhuchao.android.car.aidl.IMyAidlInterfaceListener;
interface IMyMediaAidlInterface {
    /**
     * Demonstrates some basic types that you can use as parameters
     * and return values in AIDL.
     */
    void registerListener(IMyAidlInterfaceListener iMyAidlInterfaceListener);
    void unregisterListener(IMyAidlInterfaceListener iMyAidlInterfaceListener);
    void sendMessage(in PEventCourier Msg);

    void setMagic(int magic);
    void setTime(long time);
    void pausePlay();
    void playPause();
    void playNext();
    void playPrev();
    void playStop();
    void playStopFree();
    void playStopFreeFree();
    void startPlay(String fileName);
    boolean isPlaying();
    int getPlayerStatus();
    List<PMovie> getMediaList(int MsgID);
}