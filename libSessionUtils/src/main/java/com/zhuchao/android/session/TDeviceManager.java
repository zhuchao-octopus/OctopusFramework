package com.zhuchao.android.session;

import android.content.SharedPreferences;

import com.zhuchao.android.libfileutils.ObjectList;
import com.zhuchao.android.serialport.SerialPort;
import com.zhuchao.android.serialport.SerialPortFinder;
import com.zhuchao.android.utils.MMLog;

import java.io.IOException;

public class TDeviceManager {
    private static final String TAG = "TDeviceManager";
    private static ObjectList deviceList = new ObjectList();
    //private static ObjectList deviceAll = new ObjectList();
    public static SerialPortFinder serialPortFinder = new SerialPortFinder();

    public static SerialPort getDevice(String devicePath, int baudrate)
    {
        SerialPort serialPort = (SerialPort)deviceList.getObject(devicePath);
        if(serialPort == null) {
            try {
                serialPort = SerialPort.newBuilder(devicePath, baudrate) // 串口地址地址，波特率
                           .dataBits(8) // 数据位,默认8；可选值为5~8
                           .stopBits(1) // 停止位，默认1；1:1位停止位；2:2位停止位
                           .parity(0) // 校验位；0:无校验位(NONE，默认)；1:奇校验位(ODD);2:偶校验位(EVEN)
                           .build();
                deviceList.add(devicePath,serialPort);
            } catch (IOException e) {
                //e.printStackTrace();
                MMLog.e(TAG, "getSerialPort() returns null " + e.toString());
                serialPort = null;
            }
        }
        return serialPort;
    }

    public static String[] getAllDevices()
    {
        String[] devices =  serialPortFinder.getAllDevicesPath();
        for(String str :devices)
            MMLog.log(TAG,str);
        return devices;
    }

    public static void closeSerialPort(String devicePath)
    {
        SerialPort serialPort = null;
        serialPort = (SerialPort)deviceList.getObject(devicePath);
        if(serialPort != null)
        {
            deviceList.delete(serialPort);
            serialPort.close();
        }
    }
}
