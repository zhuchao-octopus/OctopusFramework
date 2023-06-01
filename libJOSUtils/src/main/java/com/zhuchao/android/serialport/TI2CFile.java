package com.zhuchao.android.serialport;

import com.google.android.things.pio.I2cDevice;
import com.google.android.things.pio.PeripheralManager;

import com.zhuchao.android.fbase.DataID;
import com.zhuchao.android.fbase.EventCourier;
import com.zhuchao.android.fbase.EventCourierInterface;
import com.zhuchao.android.fbase.MMLog;
import com.zhuchao.android.fbase.TCourierEventListener;
import com.zhuchao.android.fbase.TTask;
import com.zhuchao.android.fbase.eventinterface.InvokeInterface;

import java.io.IOException;

public class TI2CFile extends TDevice implements TCourierEventListener, InvokeInterface {
    private final String TAG = "TI2CFile";
    private PeripheralManager peripheralManager = null;
    private I2cDevice i2cDevice = null;
    private final TTask tTask;
    private TCourierEventListener deviceReadingEventListener = null;

    public TI2CFile() {
        peripheralManager = PeripheralManager.getInstance();
        tTask = new TTask(TAG, this);
        setDeviceType("TI2CFile");
    }

    @Override
    public void closeDevice() {
        super.closeDevice();
        try {
            if (i2cDevice != null)
                i2cDevice.close();
            i2cDevice = null;
        } catch (IOException e) {
            //e.printStackTrace();
            MMLog.log(TAG, e.toString());
        }
    }

    @Override
    public void openDevice(String FileName, int Address) {
        try {
            i2cDevice = peripheralManager.openI2cDevice(FileName, Address);
            setDevicePath(TAG + i2cDevice.getName());
        } catch (IOException e) {
            //e.printStackTrace();
            MMLog.log(TAG, e.toString());
        }
    }

    public void startPollingRead() {
        if (tTask.isWorking()) return;
        tTask.start();
    }

    public void writeFile(byte[] var1, int var2) {
        if (i2cDevice == null) {
            MMLog.log(TAG, "no device to be write");
            return;
        }
        try {
            i2cDevice.write(var1, var2);
        } catch (IOException e) {
            MMLog.log(TAG, e.toString());
        }
    }

    public void writeByte(int Address, Byte Data) {
        if (i2cDevice == null) {
            MMLog.log(TAG, "no device to be write");
            return;
        }
        try {
            i2cDevice.writeRegByte(Address, (byte) Data);
        } catch (IOException e) {
            MMLog.log(TAG, e.toString());
        }
    }

    public void writeWord(int Address, short Data) {
        if (i2cDevice == null) {
            MMLog.log(TAG, "no device to be write");
            return;
        }
        try {
            i2cDevice.writeRegWord(Address, Data);
        } catch (IOException e) {
            MMLog.log(TAG, e.toString());
        }
    }

    public void writeBuffer(int var1, byte[] var2, int var3) {
        if (i2cDevice == null) {
            MMLog.log(TAG, "no device to be write");
            return;
        }
        try {
            i2cDevice.writeRegBuffer(var1, var2, var3);
        } catch (IOException e) {
            MMLog.log(TAG, e.toString());
        }
    }

    public void readFile(byte[] var1, int var2) {
        if (i2cDevice == null) {
            MMLog.log(TAG, "no device to be write");
            return;
        }
        try {
            i2cDevice.read(var1, var2);
        } catch (IOException e) {
            MMLog.log(TAG, e.toString());
        }
    }

    public byte readByte(int Address, Byte Data) {
        if (i2cDevice == null) {
            MMLog.log(TAG, "no device to be write");
            return 0;
        }
        try {
            return i2cDevice.readRegByte(Address);
        } catch (IOException e) {
            MMLog.log(TAG, e.toString());
        }
        return 0;
    }

    public void readBuffer(int var1, byte[] var2, int var3) {
        if (i2cDevice == null) {
            MMLog.log(TAG, "no device to be write");
            return;
        }
        try {
            i2cDevice.readRegBuffer(var1, var2, var3);
        } catch (IOException e) {
            MMLog.log(TAG, e.toString());
        }
    }

    @Override
    public void CALLTODO(String tag) {
        byte[] bytes = new byte[2];
        while (tTask.isKeeping() && i2cDevice != null) {
            readBuffer(0, bytes, bytes.length);
            dispatchCourier(new EventCourier(getDevicePath(), DataID.DEVICE_EVENT_I2C_READ, bytes));
            try {
                Thread.sleep(2000);
            } catch (InterruptedException e) {
                //e.printStackTrace();
                MMLog.log(TAG, e.toString());
            }
        }
    }

    @Override
    public boolean onCourierEvent(EventCourierInterface eventCourier) {
        if (i2cDevice == null) return false;
        switch (eventCourier.getId()) {
            case DataID.DEVICE_EVENT_I2C_WRITE:
            case DataID.DEVICE_EVENT_WRITE:
                if (eventCourier.getTag().equals(getDevicePath()))
                {
                    if (eventCourier.getDatas() != null) {
                        writeFile(eventCourier.getDatas(),eventCourier.getDatas().length);
                    }
                    return true;
                }
        }
        return false;
    }

    public void callback(TCourierEventListener deviceEventListener) {
        this.deviceReadingEventListener = deviceEventListener;
    }

    public void registerReadingCallback(TCourierEventListener deviceEventListener) {
        this.deviceReadingEventListener = deviceEventListener;
    }

    public void registerReceivedCallback(TCourierEventListener deviceEventListener) {
        this.deviceReadingEventListener = deviceEventListener;
    }

    private void dispatchCourier(EventCourier eventCourier) {
        if (deviceReadingEventListener != null) {//处理一帧数据
            deviceReadingEventListener.onCourierEvent(eventCourier);
            //MMLog.log(TAG, "deviceReadingEventListener = "+deviceReadingEventListener.toString()+", eventCourier = "+eventCourier.toString());
        } else {
            MMLog.log(TAG, "deviceReadingEventListener = null, eventCourier = " + eventCourier.toString());
        }
    }
}
