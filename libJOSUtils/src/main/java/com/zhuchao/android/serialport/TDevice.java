package com.zhuchao.android.serialport;

public class TDevice {
    protected String deviceType = "TDevice" ;
    protected String devicePath;
    public TDevice() {
    }

    public String getDevicePath() {
        return devicePath;
    }

    public void setDevicePath(String devicePath) {
        this.devicePath = devicePath;
    }

    public String getDeviceType() {
        return deviceType;
    }

    public void setDeviceType(String deviceType) {
        this.deviceType = deviceType;
    }

    public void closeDevice() {

    }

}
