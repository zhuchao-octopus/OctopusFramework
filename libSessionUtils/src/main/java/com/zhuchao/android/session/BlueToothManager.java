package com.zhuchao.android.session;

import android.annotation.SuppressLint;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.Context;

import com.zhuchao.android.fbase.MMLog;

import java.lang.reflect.Method;
import java.util.HashSet;
import java.util.Set;

public class BlueToothManager {
    private static final String TAG = "BlueToothManager";
    public static final int STATUS_OFF = 0;
    public static final int STATUS_ON = 1;
    public static final int STATUS_CONNECT = 2;

    private static BluetoothAdapter mBluetoothAdapter;
    //获取扫描列表
    //Collection<CachedBluetoothDevice> cachedDevices =null;
    //已连接/绑定设备列表
    public static Set<BluetoothDevice> mBondedDevices = new HashSet<>();

    private static void getBluetoothAdapter() {
        if (mBluetoothAdapter == null) mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        ///LocalBluetoothManager mBluetoothManager = LocalBluetoothManager.getInstance(null, null);
        ///已连接+绑定设备列表
        ///Set<BluetoothDevice> bondedDevices = mBluetoothManager.getBluetoothAdapter().getBondedDevices();
    }

    @SuppressLint("MissingPermission")
    public static void OnOff(Context mContext) {
        getBluetoothAdapter();
        if (mBluetoothAdapter.isEnabled()) {
            // 如果蓝牙已打开，关闭蓝牙
            mBluetoothAdapter.disable();
            ///Intent intent = new Intent(Intent.ACTION_MAIN);
            ///intent.addCategory(Intent.CATEGORY_HOME);
            ///mContext.startActivity(intent);
        } else if (mBluetoothAdapter.getState() == BluetoothAdapter.STATE_OFF) mBluetoothAdapter.enable();
    }

    @SuppressLint("MissingPermission")
    public static int getBlueToothStatus() {
        getBluetoothAdapter();
        if (mBluetoothAdapter.getState() == BluetoothAdapter.STATE_OFF || mBluetoothAdapter.getState() == BluetoothAdapter.STATE_TURNING_OFF) {
            return STATUS_OFF;
        }
        mBondedDevices = mBluetoothAdapter.getBondedDevices();
        for (BluetoothDevice device : mBondedDevices) {
            MMLog.d(TAG, "BT:" + device.toString());
        }
        if (mBondedDevices == null || mBondedDevices.size() == 0) {
            return STATUS_ON;
        } else if (bluetoothIsConnect2()) {
            return STATUS_CONNECT;
        } else {
            return STATUS_ON;
        }

    }

    // 设置蓝牙可见性
    public void requestDiscoverable() {
        getBluetoothAdapter();
        ///mBluetoothAdapter.g.setDiscoverableTimeout(300); //注意单位是秒哦
        ///可发现和可连接状态
        ///mBluetoothAdapter.setScanMode(BluetoothAdapter.SCAN_MODE_CONNECTABLE_DISCOVERABLE);

    }


    @SuppressLint("DiscouragedPrivateApi")
    private static boolean bluetoothIsConnect() {
        Class<BluetoothAdapter> bluetoothAdapterClass = BluetoothAdapter.class;
        try {
            Method method = bluetoothAdapterClass.getDeclaredMethod("getConnectionState");
            int state = (int) method.invoke(mBluetoothAdapter);
            if (state == BluetoothAdapter.STATE_CONNECTED || state == BluetoothAdapter.STATE_CONNECTING) {
                return true;
            }
        } catch (Exception e) {
            e.printStackTrace();
            ///throw new RuntimeException("无法引用");
        }
        return false;
    }

    @SuppressLint("MissingPermission")
    private static boolean bluetoothIsConnect2() {
        getBluetoothAdapter();
        Set<BluetoothDevice> bluetoothDevices = mBluetoothAdapter.getBondedDevices();
        for (BluetoothDevice device : bluetoothDevices) {
            if (isBtConnectedDeviceByMac(device.getAddress())) {
                return true;
            }
        }
        return false;
    }

    @SuppressLint("MissingPermission")
    public static boolean isBtConnectedDeviceByMac(String btMac) {
        getBluetoothAdapter();
        Set<BluetoothDevice> set = mBluetoothAdapter.getBondedDevices();
        BluetoothDevice device = null;
        for (BluetoothDevice dev : set) {
            if (dev.getAddress().equalsIgnoreCase(btMac)) {
                device = dev;
                break;
            }
        }
        if (device == null) {
            return false;
        }
        //得到BluetoothDevice的Class对象
        Class<BluetoothDevice> bluetoothDeviceClass = BluetoothDevice.class;
        try {//得到连接状态的方法
            Method method = bluetoothDeviceClass.getDeclaredMethod("isConnected", (Class[]) null);
            //打开权限
            method.setAccessible(true);
            return (boolean) method.invoke(device, (Object[]) null);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }
}
