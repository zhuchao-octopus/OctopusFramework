package com.zhuchao.android.session;

import static com.zhuchao.android.fbase.FileUtils.EmptyString;
import static com.zhuchao.android.fbase.FileUtils.NotEmptyString;

import com.zhuchao.android.fbase.MMLog;
import com.zhuchao.android.fbase.ObjectList;
import com.zhuchao.android.serialport.SerialPortFinder;
import com.zhuchao.android.serialport.TDevice;
import com.zhuchao.android.serialport.TUartFile;

public class TDeviceManager {
    private final String TAG = "TDeviceManager";
    private ObjectList deviceList = null;
    private SerialPortFinder uartFinder = null;

    public TDeviceManager() {
        this.deviceList = new ObjectList();

        this.uartFinder = new SerialPortFinder();
    }

    public synchronized TUartFile getDevice(String devicePath, int baudRate) {
        TUartFile tUartFile = (TUartFile) deviceList.getObject(devicePath);
        if (tUartFile == null) {
            tUartFile = new TUartFile(devicePath, baudRate);
            deviceList.addItem(devicePath, tUartFile);
        }
        return tUartFile;
    }

    public synchronized TUartFile getDevice(String devicePath) {
        TUartFile tUartFile = null;
        if (deviceList.getCount() > 0 && NotEmptyString(devicePath)) tUartFile = (TUartFile) deviceList.getObject(devicePath);
        return tUartFile;
    }

    public TUartFile startUart(String devicePath, int baudrate) {
        if (EmptyString(devicePath) || baudrate <= 0) {
            MMLog.log(TAG, "invalid device information parameter " + devicePath);
            return null;
        }
        TUartFile tUartFile = getDevice(devicePath, baudrate);
        if (tUartFile != null) tUartFile.startPollingRead();
        else MMLog.log(TAG, "get device failed " + devicePath);
        return tUartFile;
    }

    public String[] getAllDevices() {
        return uartFinder.getAllDevicesPath();
    }

    public void printAllDevice() {
        String[] devices = uartFinder.getAllDevicesPath();
        for (String str : devices)
            MMLog.log(TAG, str);
    }

    public void close(String devicePath) {
        TUartFile tUartFile = (TUartFile) deviceList.getObject(devicePath);
        if (tUartFile != null) {
            deviceList.delete(devicePath);
            tUartFile.closeDevice();
        }
    }

    public void closeAllUartDevice() {
        for (Object obj : deviceList.getAllObject()) {
            TDevice device = ((TDevice) obj);
            if (device.getDeviceType().contains("UART")) {
                device.closeDevice();
                deviceList.delete(device.getDevicePath());
            }
        }
    }
}
