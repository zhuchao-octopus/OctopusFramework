package com.zhuchao.android.serialport;

import com.google.android.things.pio.Gpio;
import com.google.android.things.pio.GpioCallback;
import com.google.android.things.pio.PeripheralManager;
import com.zhuchao.android.fbase.DataID;
import com.zhuchao.android.fbase.EventCourier;
import com.zhuchao.android.fbase.MMLog;
import com.zhuchao.android.fbase.eventinterface.EventCourierInterface;
import com.zhuchao.android.fbase.eventinterface.InvokeInterface;
import com.zhuchao.android.fbase.eventinterface.TCourierEventListener;

import java.io.IOException;

public class TGPIOFile extends TDevice implements TCourierEventListener, InvokeInterface {
    private final static String TAG = "TGPIOFile";
    private Gpio mGpio;
    private TCourierEventListener mGpioEventListener = null;
    // Step 4. Register an event callback.
    private final GpioCallback mCallback = new GpioCallback() {
        @Override
        public boolean onGpioEdge(Gpio gpio) {
            MMLog.i(TAG, "GPIO " + gpio.getName() + " status changed");
            if (mGpioEventListener != null)
                mGpioEventListener.onCourierEvent(new EventCourier(getDevicePath(), DataID.DEVICE_EVENT_GPIO_STATUS, getGpioValue()));
            // Step 5. Return true to keep callback active.
            return true;
        }
    };

    public TGPIOFile(String gpioPinName, TCourierEventListener courierEventListener) {
        PeripheralManager manager = PeripheralManager.getInstance();
        try {
            // Step 1. Create GPIO connection.
            mGpio = manager.openGpio(gpioPinName);
            // Step 2. Configure as an input.
            mGpio.setDirection(Gpio.DIRECTION_IN);
            // Step 3. Enable edge trigger events.
            mGpio.setEdgeTriggerType(Gpio.EDGE_FALLING);
            // Step 4. Register an event callback.
            mGpio.registerGpioCallback(mCallback);

            mGpioEventListener = courierEventListener;
            setDevicePath(mGpio.getClass().getSimpleName() + mGpio.getName());
            setDeviceType("GPIO");
        } catch (IOException e) {
            MMLog.e(TAG, "Error on PeripheralIO API " + e);
            mGpio = null;
        }
    }

    public void gpio_Config(int direction, int trigger) {
        try {
            mGpio.setDirection(direction);
            mGpio.setEdgeTriggerType(trigger);
            //gpio.setActiveType(ACTIVE_HIGH);
        } catch (IOException e) {
            MMLog.e(TAG, String.valueOf(e));
        }
    }

    public void setGpioValue(boolean b) {
        try {
            mGpio.setValue(b);
        } catch (IOException e) {
            MMLog.e(TAG, String.valueOf(e));
        }
    }

    public boolean getGpioValue() {
        try {
            return mGpio.getValue();
        } catch (IOException e) {
            MMLog.e(TAG, String.valueOf(e));
        }
        return false;
    }

    @Override
    public void closeDevice() {
        super.closeDevice();
        if (mGpio != null) {
            try {
                mGpio.unregisterGpioCallback(mCallback);
                mGpio.close();
            } catch (IOException e) {
                MMLog.e(TAG, "Error on PeripheralIO API" + e);
            }
        }
    }

    @Override
    public void openDevice(String FileName) {
        super.openDevice(FileName);
    }

    @Override
    public void CALLTODO(String tag) {

    }

    @Override
    public boolean onCourierEvent(EventCourierInterface eventCourier) {
        return false;
    }

}
