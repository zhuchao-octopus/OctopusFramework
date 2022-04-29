package com.zhuchao.android.session;

import static com.zhuchao.android.libfileutils.FileUtils.EmptyString;
import static com.zhuchao.android.libfileutils.FileUtils.NotEmptyString;

import com.zhuchao.android.libfileutils.MMLog;
import com.zhuchao.android.libfileutils.ObjectList;
import com.zhuchao.android.serialport.SerialPortFinder;
import com.zhuchao.android.serialport.TUartFile;

public class TDeviceManager {
    private final String TAG = "TDeviceManager";
    private ObjectList deviceList = null;
    private SerialPortFinder uartFinder = null;

    public TDeviceManager() {
        this.deviceList = new ObjectList();;
        this.uartFinder = new SerialPortFinder();
    }

    public synchronized TUartFile getDevice(String devicePath, int baudrate) {
        TUartFile tUartFile = (TUartFile) deviceList.getObject(devicePath);
        if (tUartFile == null) {
            tUartFile = new TUartFile(devicePath, baudrate);
            if (tUartFile != null)
                deviceList.addItem(devicePath, tUartFile);
            else
                MMLog.log(TAG,"open device failed "+ devicePath);
        }
        return tUartFile;
    }

    public synchronized TUartFile getDevice(String devicePath) {
        TUartFile tUartFile = null;
        if(deviceList.getCount()>0 && NotEmptyString(devicePath))
            tUartFile= (TUartFile) deviceList.getObject(devicePath);
        return tUartFile;
    }

    public TUartFile startUart(String devicePath, int baudrate)
    {
        if(EmptyString(devicePath) || baudrate <= 0)
        {
            MMLog.log(TAG,"invalid device information parameter "+ devicePath);
            return null;
        }
        TUartFile tUartFile = getDevice(devicePath,baudrate);
        if(tUartFile != null) {
            tUartFile.startPoolingRead();
        }
        else
            MMLog.log(TAG,"get device failed "+ devicePath);
        return tUartFile;
    }
    public String[] getAllDevices() {
        String[] devices = uartFinder.getAllDevicesPath();
        return devices;
    }

    public void printAllDevice() {
        String[] devices = uartFinder.getAllDevicesPath();
        for (String str : devices)
            MMLog.log(TAG, str);
    }

    public void close(String devicePath) {
        TUartFile tUartFile = (TUartFile) deviceList.getObject(devicePath);
        if (tUartFile != null) {
            deviceList.delete(tUartFile);
            tUartFile.closeDevice();
        }
    }

    public void closeAllUartFile()
    {
      for(Object obj:deviceList.getAllObject())
      {
          ((TUartFile)obj).closeDevice();
      }
    }
}
