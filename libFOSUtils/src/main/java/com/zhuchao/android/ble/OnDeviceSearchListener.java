package com.zhuchao.android.ble;

import android.bluetooth.le.ScanResult;

public interface OnDeviceSearchListener {

    void onDeviceFound(ScanResult result);  //搜索到设备
    void onDiscoveryOutTime(); //扫描超时
}
