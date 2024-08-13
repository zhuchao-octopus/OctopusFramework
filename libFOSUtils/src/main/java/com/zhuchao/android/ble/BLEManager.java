package com.zhuchao.android.ble;

import android.Manifest;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCallback;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattDescriptor;
import android.bluetooth.BluetoothGattService;
import android.bluetooth.BluetoothManager;
import android.bluetooth.le.ScanCallback;
import android.bluetooth.le.ScanResult;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Handler;
import android.util.Log;

import androidx.core.app.ActivityCompat;

import com.zhuchao.android.fbase.ByteUtils;
import com.zhuchao.android.fbase.ClassUtils;
import com.zhuchao.android.fbase.MMLog;

import java.util.List;


public class BLEManager {
    private static final String TAG = "BLEManager";
    private static final long MAX_CONNECT_TIME = 10000;  //连接超时时间10s
    private Context mContext;
    private BluetoothAdapter mBluetoothAdapter;
    private BluetoothGatt mBluetoothGatt;  //当前连接的gatt
    private BluetoothGattService mBluetoothGattService;   //服务
    private BluetoothGattCharacteristic mReadCharacteristic;  //读特征
    private BluetoothGattCharacteristic mWriteCharacteristic; //写特征
    private BluetoothDevice mCurrentBluetoothDevice;  //当前连接的设备

    private OnDeviceSearchListener onDeviceSearchListener;  //设备扫描结果监听
    private OnBleConnectListener onBleConnectListener;   //连接监听

    private boolean isConnecting = false;  //是否正在连接中
    private String serviceUUID, readUUID, writeUUID;
    private final Handler mHandler = new Handler();

    public BLEManager() {
    }

    public boolean initBle(Context context) {
        mContext = context;
        return checkBle(context);
    }

    private boolean checkBle(Context context) {
        //API 18 Android 4.3
        BluetoothManager bluetoothManager = (BluetoothManager) context.getSystemService(Context.BLUETOOTH_SERVICE);
        if (bluetoothManager == null) {
            return false;
        }
        mBluetoothAdapter = bluetoothManager.getAdapter();  //BLUETOOTH权限
        return mBluetoothAdapter != null;
    }

    private final ScanCallback mScanCallback = new ScanCallback() {
        @Override
        public void onScanResult(int callbackType, ScanResult result) {
            super.onScanResult(callbackType, result);
            if (onDeviceSearchListener != null) {
                onDeviceSearchListener.onDeviceFound(result);  //扫描到设备回调
            }
        }

        @Override
        public void onBatchScanResults(List<ScanResult> results) {
            super.onBatchScanResults(results);
            for (ScanResult result : results) {
                if (onDeviceSearchListener != null) {
                    onDeviceSearchListener.onDeviceFound(result);  //扫描到设备回调
                }
            }
        }

        @Override
        public void onScanFailed(int errorCode) {
            super.onScanFailed(errorCode);
        }
    };

    public void startDiscoveryDevice(OnDeviceSearchListener onDeviceSearchListener, long scanTime) {
        this.onDeviceSearchListener = onDeviceSearchListener;
        MMLog.d(TAG, "Start to scan ble devices");
        if (ActivityCompat.checkSelfPermission(mContext, Manifest.permission.BLUETOOTH_SCAN) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            //return;
        }

        ///mBluetoothAdapter.startLeScan(leScanCallback);
        mBluetoothAdapter.getBluetoothLeScanner().startScan(mScanCallback);
        //设定最长扫描时间
        mHandler.postDelayed(stopScanRunnable, scanTime);
    }

    private final Runnable stopScanRunnable = new Runnable() {
        @Override
        public void run() {
            if (onDeviceSearchListener != null) {
                onDeviceSearchListener.onDiscoveryOutTime();  //扫描超时回调
            }
            stopDiscoveryDevice();
        }
    };

    public void stopDiscoveryDevice() {
        mHandler.removeCallbacks(stopScanRunnable);
        if (mBluetoothAdapter == null) return;
        MMLog.d(TAG, "Stop scanning");
        ///bluetooth4Adapter.stopLeScan(leScanCallback);
        if (ActivityCompat.checkSelfPermission(mContext, Manifest.permission.BLUETOOTH_SCAN) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            //return;
        }
        mBluetoothAdapter.getBluetoothLeScanner().stopScan(mScanCallback);
    }

    private final BluetoothGattCallback bluetoothGattCallback = new BluetoothGattCallback() {
        @Override
        public void onPhyUpdate(BluetoothGatt gatt, int txPhy, int rxPhy, int status) {
            super.onPhyUpdate(gatt, txPhy, rxPhy, status);
        }

        @Override
        public void onPhyRead(BluetoothGatt gatt, int txPhy, int rxPhy, int status) {
            super.onPhyRead(gatt, txPhy, rxPhy, status);
        }

        //连接状态回调-连接成功/断开连接
        @Override
        public void onConnectionStateChange(BluetoothGatt gatt, int status, int newState) {
            super.onConnectionStateChange(gatt, status, newState);
            MMLog.d(TAG, "Status:" + status + " " + newState);
            switch (status) {
                case BluetoothGatt.GATT_SUCCESS:
                    MMLog.w(TAG, "BluetoothGatt.GATT_SUCCESS");
                    break;
                case BluetoothGatt.GATT_FAILURE:
                    MMLog.w(TAG, "BluetoothGatt.GATT_FAILURE");
                    break;
                case BluetoothGatt.GATT_CONNECTION_CONGESTED:
                    MMLog.w(TAG, "BluetoothGatt.GATT_CONNECTION_CONGESTED");
                    break;
                case BluetoothGatt.GATT_INSUFFICIENT_AUTHENTICATION:
                    MMLog.w(TAG, "BluetoothGatt.GATT_INSUFFICIENT_AUTHENTICATION");
                    break;
                case BluetoothGatt.GATT_INSUFFICIENT_ENCRYPTION:
                    MMLog.w(TAG, "BluetoothGatt.GATT_INSUFFICIENT_ENCRYPTION");
                    break;
                case BluetoothGatt.GATT_INVALID_OFFSET:
                    MMLog.w(TAG, "BluetoothGatt.GATT_INVALID_OFFSET");
                    break;
                case BluetoothGatt.GATT_READ_NOT_PERMITTED:
                    MMLog.w(TAG, "BluetoothGatt.GATT_READ_NOT_PERMITTED");
                    break;
                case BluetoothGatt.GATT_REQUEST_NOT_SUPPORTED:
                    MMLog.w(TAG, "BluetoothGatt.GATT_REQUEST_NOT_SUPPORTED");
                    break;
            }

            BluetoothDevice bluetoothDevice = gatt.getDevice();
            if (ActivityCompat.checkSelfPermission(mContext, Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED) {
                // TODO: Consider calling
                //    ActivityCompat#requestPermissions
                // here to request the missing permissions, and then overriding
                //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                //                                          int[] grantResults)
                // to handle the case where the user grants the permission. See the documentation
                // for ActivityCompat#requestPermissions for more details.
                //return;
            }
            MMLog.d(TAG, "Connect to ：" + bluetoothDevice.getName() + " " + bluetoothDevice.getAddress());
            isConnecting = false;
            mHandler.removeCallbacks(connectOutTimeRunnable);
            if (newState == BluetoothGatt.STATE_CONNECTED) {
                MMLog.w(TAG, "Connected successfully");
                //连接成功去发现服务
                gatt.discoverServices();
                //设置发现服务超时时间
                mHandler.postDelayed(serviceDiscoverOutTimeRunnable, MAX_CONNECT_TIME);

                if (onBleConnectListener != null) {
                    onBleConnectListener.onConnectSuccess(gatt, bluetoothDevice, status);   //连接成功回调
                }
            } else if (newState == BluetoothGatt.STATE_DISCONNECTED) {
                //清空系统缓存
                ClassUtils.refreshDeviceCache(gatt);
                gatt.close();  //断开连接释放连接

                if (status == 133) {
                    //无法连接
                    if (onBleConnectListener != null) {
                        gatt.close();
                        onBleConnectListener.onConnectFailure(gatt, bluetoothDevice, "连接异常！", status);  //133连接异常 异常断开
                        MMLog.d(TAG, "Status：" + status + "  " + bluetoothDevice.getAddress());
                    }
                } else if (status == 62) {
                    //成功连接没有发现服务断开
                    if (onBleConnectListener != null) {
                        gatt.close();
                        onBleConnectListener.onConnectFailure(gatt, bluetoothDevice, "连接成功服务未发现断开！", status); //62没有发现服务 异常断开
                        MMLog.d(TAG, "Status:" + status);
                    }

                } else if (status == 0) {
                    if (onBleConnectListener != null) {
                        onBleConnectListener.onDisConnectSuccess(gatt, bluetoothDevice, status); //0正常断开 回调
                    }
                } else if (status == 8) {
                    //因为距离远或者电池无法供电断开连接
                    // 已经成功发现服务
                    if (onBleConnectListener != null) {
                        onBleConnectListener.onDisConnectSuccess(gatt, bluetoothDevice, status); //8断电断开  回调
                    }
                } else if (status == 34) {
                    if (onBleConnectListener != null) {
                        onBleConnectListener.onDisConnectSuccess(gatt, bluetoothDevice, status); //34断开
                    }
                } else {
                    //其它断开连接
                    if (onBleConnectListener != null) {
                        onBleConnectListener.onDisConnectSuccess(gatt, bluetoothDevice, status); //其它断开
                    }
                }
            } else if (newState == BluetoothGatt.STATE_CONNECTING) {
                MMLog.d(TAG, "正在连接...");
                if (onBleConnectListener != null) {
                    onBleConnectListener.onConnecting(gatt, bluetoothDevice);  //正在连接回调
                }
            } else if (newState == BluetoothGatt.STATE_DISCONNECTING) {
                MMLog.d(TAG, "正在断开...");
                if (onBleConnectListener != null) {
                    onBleConnectListener.onDisConnecting(gatt, bluetoothDevice); //正在断开回调
                }
            }
        }

        //发现服务
        @Override
        public void onServicesDiscovered(BluetoothGatt gatt, int status) {
            super.onServicesDiscovered(gatt, status);
            //移除发现服务超时
            mHandler.removeCallbacks(serviceDiscoverOutTimeRunnable);
            //配置服务信息
            if (setupService(gatt, serviceUUID, readUUID, writeUUID)) {
                if (onBleConnectListener != null) {
                    onBleConnectListener.onServiceDiscoverySucceed(gatt, gatt.getDevice(), status);  //成功发现服务回调
                }
            } else {
                if (onBleConnectListener != null) {
                    onBleConnectListener.onServiceDiscoveryFailed(gatt, gatt.getDevice(), "获取服务特征异常");  //发现服务失败回调
                }
            }
        }

        @Override
        public void onCharacteristicRead(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic, int status) {
            super.onCharacteristicRead(gatt, characteristic, status);
            MMLog.d(TAG, "CharacteristicRead status: " + status);
        }

        //向蓝牙设备写入数据结果回调
        @Override
        public void onCharacteristicWrite(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic, int status) {
            super.onCharacteristicWrite(gatt, characteristic, status);

            if (characteristic.getValue() == null) {
                Log.e(TAG, "characteristic.getValue() == null");
                return;
            }
            //将收到的字节数组转换成十六进制字符串
            String msg = ByteUtils.BuffToHexStr(characteristic.getValue()); //TypeConversion.bytes2HexString(characteristic.getValue(), characteristic.getValue().length);
            if (status == BluetoothGatt.GATT_SUCCESS) {
                //写入成功
                MMLog.w(TAG, "BluetoothGatt.GATT_SUCCESS " + msg);
                if (onBleConnectListener != null) {
                    onBleConnectListener.onWriteSuccess(gatt, gatt.getDevice(), characteristic.getValue());  //写入成功回调
                }

            } else if (status == BluetoothGatt.GATT_FAILURE) {
                //写入失败
                MMLog.d(TAG, "BluetoothGatt.GATT_FAILURE " + msg);
                if (onBleConnectListener != null) {
                    onBleConnectListener.onWriteFailure(gatt, gatt.getDevice(), characteristic.getValue(), "写入失败");  //写入失败回调
                }
            } else if (status == BluetoothGatt.GATT_WRITE_NOT_PERMITTED) {
                //没有权限
                MMLog.d(TAG, "No BluetoothGatt.GATT_WRITE_NOT_PERMITTED！");
            }
        }

        //读取蓝牙设备发出来的数据回调
        @Override
        public void onCharacteristicChanged(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic) {
            super.onCharacteristicChanged(gatt, characteristic);
            //接收数据
            byte[] bytes = characteristic.getValue();
            ///Log.w("TAG", "收到数据str:" + TypeConversion.bytes2HexString(bytes, bytes.length));
            if (onBleConnectListener != null) {
                onBleConnectListener.onReceiveMessage(gatt, gatt.getDevice(), characteristic, characteristic.getValue());  //接收数据回调
            }
        }

        @Override
        public void onDescriptorRead(BluetoothGatt gatt, BluetoothGattDescriptor descriptor, int status) {
            super.onDescriptorRead(gatt, descriptor, status);
        }

        @Override
        public void onDescriptorWrite(BluetoothGatt gatt, BluetoothGattDescriptor descriptor, int status) {
            super.onDescriptorWrite(gatt, descriptor, status);
        }

        @Override
        public void onReliableWriteCompleted(BluetoothGatt gatt, int status) {
            super.onReliableWriteCompleted(gatt, status);
            MMLog.d(TAG, "onReliableWriteCompleted");
        }

        @Override
        public void onReadRemoteRssi(BluetoothGatt gatt, int rssi, int status) {
            super.onReadRemoteRssi(gatt, rssi, status);
            if (status == BluetoothGatt.GATT_SUCCESS) {
                MMLog.d(TAG, "BluetoothGatt.GATT_SUCCESS RSSI：" + rssi + ",status" + status);
                if (onBleConnectListener != null) {
                    onBleConnectListener.onReadRssi(gatt, rssi, status);  //成功读取连接的信号强度回调
                }
            } else if (status == BluetoothGatt.GATT_FAILURE) {
                MMLog.d(TAG, "BluetoothGatt.GATT_FAILURE status：" + status);
            }
        }

        //修改MTU值结果回调
        @Override
        public void onMtuChanged(BluetoothGatt gatt, int mtu, int status) {
            super.onMtuChanged(gatt, mtu, status);
            ///设置mtu值，即bluetoothGatt.requestMtu()时触发，提示该操作是否成功
            if (status == BluetoothGatt.GATT_SUCCESS) {  //设置MTU成功
                //MTU默认取的是23，当收到 onMtuChanged 后，会根据传递的值修改MTU，注意由于传输用掉3字节，因此传递的值需要减3。
                //mtu - 3
                MMLog.d(TAG, "设置MTU成功，新的MTU值：" + (mtu - 3) + ",status" + status);
                if (onBleConnectListener != null) {
                    onBleConnectListener.onMTUSetSuccess("设置后新的MTU值 = " + (mtu - 3) + "   status = " + status, mtu - 3);  //MTU设置成功
                }

            } else if (status == BluetoothGatt.GATT_FAILURE) {  //设置MTU失败
                MMLog.d(TAG, "设置MTU值失败：" + (mtu - 3) + ",status" + status);
                if (onBleConnectListener != null) {
                    onBleConnectListener.onMTUSetFailure("设置MTU值失败：" + (mtu - 3) + "   status：" + status);  //MTU设置失败
                }
            }
        }
    };

    public BluetoothGatt connectBleDevice(Context context, BluetoothDevice bluetoothDevice, long outTime, String serviceUUID, String readUUID, String writeUUID, OnBleConnectListener onBleConnectListener) {
        if (bluetoothDevice == null) {
            MMLog.d(TAG, "connectBleDevice()-->bluetoothDevice == null");
            return null;
        }
        if (isConnecting) {
            MMLog.d(TAG, "connectBleDevice()-->isConnectIng = true");
            return null;
        }
        this.serviceUUID = serviceUUID;
        this.readUUID = readUUID;
        this.writeUUID = writeUUID;
        this.onBleConnectListener = onBleConnectListener;

        this.mCurrentBluetoothDevice = bluetoothDevice;
        if (ActivityCompat.checkSelfPermission(mContext, Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return null;
        }
        MMLog.d(TAG, "开始准备连接：" + bluetoothDevice.getName() + "-->" + bluetoothDevice.getAddress());
        //出现 BluetoothGatt.android.os.DeadObjectException 蓝牙没有打开
        try {
            if (ActivityCompat.checkSelfPermission(mContext, Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED) {
                // TODO: Consider calling
                //    ActivityCompat#requestPermissions
                // here to request the missing permissions, and then overriding
                //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                //                                          int[] grantResults)
                // to handle the case where the user grants the permission. See the documentation
                // for ActivityCompat#requestPermissions for more details.
                return null;
            }
            mBluetoothGatt = bluetoothDevice.connectGatt(context, false, bluetoothGattCallback);
            mBluetoothGatt.connect();
            isConnecting = true;

        } catch (Exception e) {
            MMLog.e(TAG, "e:" + e.getMessage());
        }

        //设置连接超时时间10s
        mHandler.postDelayed(connectOutTimeRunnable, outTime);

        return mBluetoothGatt;
    }

    //连接超时
    private final Runnable connectOutTimeRunnable = new Runnable() {
        @Override
        public void run() {
            if (mBluetoothGatt == null) {
                MMLog.d(TAG, "connectOutTimeRunnable-->mBluetoothGatt == null");
                return;
            }

            isConnecting = false;
            if (ActivityCompat.checkSelfPermission(mContext, Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED) {
                // TODO: Consider calling
                //    ActivityCompat#requestPermissions
                // here to request the missing permissions, and then overriding
                //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                //                                          int[] grantResults)
                // to handle the case where the user grants the permission. See the documentation
                // for ActivityCompat#requestPermissions for more details.
                return;
            }
            mBluetoothGatt.disconnect();

            //连接超时当作连接失败回调
            if (onBleConnectListener != null) {
                onBleConnectListener.onConnectFailure(mBluetoothGatt, mCurrentBluetoothDevice, "连接超时！", -1);  //连接失败回调
            }
        }
    };

    //发现服务超时
    private final Runnable serviceDiscoverOutTimeRunnable = new Runnable() {
        @Override
        public void run() {
            if (mBluetoothGatt == null) {
                MMLog.d(TAG, "connectOutTimeRunnable-->mBluetoothGatt == null");
                return;
            }

            isConnecting = false;
            if (ActivityCompat.checkSelfPermission(mContext, Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED) {
                // TODO: Consider calling
                //    ActivityCompat#requestPermissions
                // here to request the missing permissions, and then overriding
                //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                //                                          int[] grantResults)
                // to handle the case where the user grants the permission. See the documentation
                // for ActivityCompat#requestPermissions for more details.
                return;
            }
            mBluetoothGatt.disconnect();

            //发现服务超时当作连接失败回调
            if (onBleConnectListener != null) {
                onBleConnectListener.onConnectFailure(mBluetoothGatt, mCurrentBluetoothDevice, "发现服务超时！", -1);  //连接失败回调
            }
        }
    };

    private boolean setupService(BluetoothGatt bluetoothGatt, String serviceUUID, String readUUID, String writeUUID) {
        if (bluetoothGatt == null) {
            MMLog.d(TAG, "setupService()-->bluetoothGatt == null");
            return false;
        }

        if (serviceUUID == null) {
            MMLog.d(TAG, "setupService()-->serviceUUID == null");
            return false;
        }

        for (BluetoothGattService service : bluetoothGatt.getServices()) {
            if (service.getUuid().toString().equals(serviceUUID)) {
                mBluetoothGattService = service;
            }
        }

        if (mBluetoothGattService == null) {
            //找不到该服务就立即断开连接
            MMLog.d(TAG, "setupService()-->bluetoothGattService == null");
            return false;
        }
        MMLog.d(TAG, "setupService()-->bluetoothGattService = " + mBluetoothGattService.toString());

        if (readUUID == null || writeUUID == null) {
            MMLog.d(TAG, "setupService()-->readUUID == null || writeUUID == null");
            return false;
        }

        for (BluetoothGattCharacteristic characteristic : mBluetoothGattService.getCharacteristics()) {
            if (characteristic.getUuid().toString().equals(readUUID)) {  //读特征
                mReadCharacteristic = characteristic;
            } else if (characteristic.getUuid().toString().equals(writeUUID)) {  //写特征
                mWriteCharacteristic = characteristic;
            }
        }
        if (mReadCharacteristic == null) {
            MMLog.d(TAG, "setupService()-->readCharacteristic == null");
            return false;
        }
        if (mWriteCharacteristic == null) {
            MMLog.d(TAG, "setupService()-->writeCharacteristic == null");
            return false;
        }
        //打开读通知
        enableNotification(true, bluetoothGatt, mReadCharacteristic);

        //重点中重点，需要重新设置
        List<BluetoothGattDescriptor> descriptors = mWriteCharacteristic.getDescriptors();
        for (BluetoothGattDescriptor descriptor : descriptors) {
            descriptor.setValue(BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE);
            if (ActivityCompat.checkSelfPermission(mContext, Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED) {
                // TODO: Consider calling
                //    ActivityCompat#requestPermissions
                // here to request the missing permissions, and then overriding
                //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                //                                          int[] grantResults)
                // to handle the case where the user grants the permission. See the documentation
                // for ActivityCompat#requestPermissions for more details.
                return false;
            }
            bluetoothGatt.writeDescriptor(descriptor);
        }

        //延迟2s，保证所有通知都能及时打开
        mHandler.postDelayed(new Runnable() {
            @Override
            public void run() {
            }
        }, 2000);
        return true;
    }

    public void enableNotification(boolean enable, BluetoothGatt gatt, BluetoothGattCharacteristic characteristic) {
        if (gatt == null) {
            MMLog.d(TAG, "enableNotification-->gatt == null");
            return;
        }
        if (characteristic == null) {
            MMLog.d(TAG, "enableNotification-->characteristic == null");
            return;
        }
        //这一步必须要有，否则接收不到通知
        if (ActivityCompat.checkSelfPermission(mContext, Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return;
        }
        gatt.setCharacteristicNotification(characteristic, enable);
    }

    public boolean sendMessage(String msg) {
        if (mWriteCharacteristic == null) {
            MMLog.d(TAG, "sendMessage(byte[])-->writeGattCharacteristic == null");
            return false;
        }

        if (mBluetoothGatt == null) {
            MMLog.d(TAG, "sendMessage(byte[])-->mBluetoothGatt == null");
            return false;
        }

        boolean b = mWriteCharacteristic.setValue(ByteUtils.HexStr2Bytes(msg));
        MMLog.d(TAG, "WriteCharacteristic：" + b);
        if (ActivityCompat.checkSelfPermission(mContext, Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return false;
        }
        return mBluetoothGatt.writeCharacteristic(mWriteCharacteristic);
    }

    public void disConnectDevice() {
        if (mBluetoothGatt == null) return;

        if (ActivityCompat.checkSelfPermission(mContext, Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return;
        }
        mBluetoothGatt.disconnect();
        // close()方法应该放在断开回调处，放在此处，会没有回调信息
        // mBluetoothGatt.close();
    }

    public boolean isEnable() {
        if (mBluetoothAdapter == null) {
            return false;
        }
        return mBluetoothAdapter.isEnabled();
    }

    public void openBluetooth(Context context, boolean isFast) {
        if (!isEnable()) {
            if (isFast) {
                MMLog.d(TAG, "直接打开手机蓝牙");
                if (ActivityCompat.checkSelfPermission(mContext, Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED) {
                    // TODO: Consider calling
                    //    ActivityCompat#requestPermissions
                    // here to request the missing permissions, and then overriding
                    //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                    //                                          int[] grantResults)
                    // to handle the case where the user grants the permission. See the documentation
                    // for ActivityCompat#requestPermissions for more details.
                    return;
                }
                mBluetoothAdapter.enable();  //BLUETOOTH_ADMIN权限
            } else {
                MMLog.d(TAG, "提示用户去打开手机蓝牙");
                Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
                context.startActivity(enableBtIntent);
            }
        } else {
            MMLog.d(TAG, "手机蓝牙状态已开");
        }
    }

    public void closeBluetooth() {
        if (mBluetoothAdapter == null) return;
        if (ActivityCompat.checkSelfPermission(mContext, Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return;
        }
        mBluetoothAdapter.disable();
    }

    public boolean isDiscovery() {
        if (mBluetoothAdapter == null) {
            return false;
        }
        if (ActivityCompat.checkSelfPermission(mContext, Manifest.permission.BLUETOOTH_SCAN) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return false;
        }
        return mBluetoothAdapter.isDiscovering();
    }
}
