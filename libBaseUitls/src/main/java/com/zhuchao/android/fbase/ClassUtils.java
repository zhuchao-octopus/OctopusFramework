package com.zhuchao.android.fbase;

import android.annotation.SuppressLint;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.util.Log;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

public class ClassUtils {
    private final static String TAG = "ClassUtils";
    public static BluetoothDevice remoteDevice = null;

    /**
     * 与设备配对 参考源码：platform/packages/apps/Settings.git
     * /Settings/src/com/android/settings/bluetooth/CachedBluetoothDevice.java
     */
    @SuppressWarnings("unchecked")
    static public boolean createBond(@SuppressWarnings("rawtypes") Class btClass, BluetoothDevice btDevice) throws Exception {
        Method createBondMethod = btClass.getMethod("createBond");
        Boolean returnValue = (Boolean) createBondMethod.invoke(btDevice);
        assert returnValue != null;
        return returnValue;
    }

    //自动配对设置Pin值
    static public boolean autoBond(Class btClass, BluetoothDevice device, String strPin) throws Exception {
        Method autoBondMethod = btClass.getMethod("setPin", new Class[]{byte[].class});
        return (Boolean) autoBondMethod.invoke(device, new Object[]{strPin.getBytes()});
    }

    @SuppressLint("SoonBlockedPrivateApi")
    static public void setPairingConfirmation(BluetoothDevice device) {
        try {
            Field field = device.getClass().getDeclaredField("sService");
            field.setAccessible(true);
            Object service = field.get(device);
            assert service != null;
            Method method = service.getClass().getDeclaredMethod("setPairingConfirmation", BluetoothDevice.class, boolean.class);
            method.setAccessible(true);
            method.invoke(service, device, true);

        } catch (NoSuchFieldException | NoSuchMethodException | InvocationTargetException | IllegalAccessException e) {
            MMLog.e(TAG, String.valueOf(e));
        }
    }

    /**
     * 与设备解除配对 参考源码：platform/packages/apps/Settings.git
     * /Settings/src/com/android/settings/bluetooth/CachedBluetoothDevice.java
     */
    @SuppressWarnings("unchecked")
    static public boolean removeBond(Class btClass, BluetoothDevice btDevice) throws Exception {
        Method removeBondMethod = btClass.getMethod("removeBond");
        Boolean returnValue = (Boolean) removeBondMethod.invoke(btDevice);
        assert returnValue != null;
        return returnValue;
    }

    @SuppressWarnings("unchecked")
    static public boolean setPin(Class btClass, BluetoothDevice btDevice, String str) throws Exception {
        try {
            Method removeBondMethod = btClass.getDeclaredMethod("setPin", new Class[]{byte[].class});
            Boolean returnValue = (Boolean) removeBondMethod.invoke(btDevice, new Object[]{str.getBytes()});
        } catch (Exception e) {
            MMLog.e(TAG, String.valueOf(e));
        }
        return true;

    }

    @SuppressWarnings("unchecked")
    static public boolean cancelPairingUserInput(Class btClass, BluetoothDevice device) throws Exception {
        Method createBondMethod = btClass.getMethod("cancelPairingUserInput");
        // cancelBondProcess()
        Boolean returnValue = (Boolean) createBondMethod.invoke(device);
        assert returnValue != null;
        return returnValue;
    }

    @SuppressWarnings("unchecked")
    static public boolean cancelBondProcess(Class btClass, BluetoothDevice device) throws Exception {
        Method createBondMethod = btClass.getMethod("cancelBondProcess");
        Boolean returnValue = (Boolean) createBondMethod.invoke(device);
        assert returnValue != null;
        return returnValue;
    }

    @SuppressWarnings("unchecked")
    static public void printAllInform(Class clsShow) {
        try {
            Method[] hideMethod = clsShow.getMethods();
            int i = 0;
            for (; i < hideMethod.length; i++) {
            }
            // 取得所有常量
            Field[] allFields = clsShow.getFields();
            for (i = 0; i < allFields.length; i++) {
            }
        } catch (Exception e) {
            MMLog.e(TAG, String.valueOf(e));
        }
    }

    /**
     * Clears the internal cache and forces a refresh of the services from the	 * remote device.
     */
    public static boolean refreshDeviceCache(BluetoothGatt mBluetoothGatt) {
        if (mBluetoothGatt != null) {
            try {
                Method localMethod = mBluetoothGatt.getClass().getMethod("refresh", new Class[0]);
                return (boolean) (Boolean) localMethod.invoke(mBluetoothGatt, new Object[0]);
            } catch (Exception localException) {
                MMLog.i(TAG, "An exception occurred while refreshing device");
            }
        }
        return false;
    }
}
