package com.zhuchao.android.libfilemanager.devices;

import android.content.Context;
import android.util.Log;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentLinkedQueue;

import JNIAPI.JniSerialPort;

import static com.zhuchao.android.libfilemanager.devices.TypeTool.ByteArrToHex;
import static com.zhuchao.android.libfilemanager.devices.TypeTool.ByteArrToHexStr;


public class MySerialPort {
    private final static int SLEEP_TIMEOUT = 30;
    private final static int STATE_PRODUCT_CODE = 1;
    private final static int STATE_MSG_ID = 2;
    private final static int STATE_ANSWER = 3;
    private final static int STATE_LENGTH = 4;
    private final static int STATE_DATA = 5;
    private final static int STATE_CHECKSUM = 6;
    private final static int STATE_END = 7;
    private final static int STATE_RESTART = 8;
    private final String TAG = "MySerialPort";
    //add by cvte start >>>
    private final ConcurrentLinkedQueue<Byte> mDataQueue = new ConcurrentLinkedQueue<>();
    public OnDataReceiveListener onDataReceiveListener = null;
    private String DevicePath = "/dev/ttyS0";
    private int Baudrate = 9600;
    private Context mContext;

    private boolean threadStatus = false; //线程状态，为了安全终止线程
    private JniSerialPort serialPort = null;
    private InputStream inputStream = null;
    private OutputStream outputStream = null;
    private int mState = STATE_PRODUCT_CODE;
    private boolean IsDecode = false;

    public MySerialPort(Context context) {
        this.mContext = context;
    }

    /**
     * byte数组中取int数值，本方法适用于(低位在前，高位在后)的顺序。
     *
     * @param ary    byte数组
     * @param offset 从数组的第offset位开始
     * @return int数值
     */
    public static int bytesToInt(byte[] ary, int offset) {
        int value;
        value = (ary[offset] & 0xFF)
                | ((ary[offset + 1] << 8) & 0xFF00)
                | ((ary[offset + 2] << 16) & 0xFF0000)
                | ((ary[offset + 3] << 24) & 0xFF000000);

        return value;
    }

    public boolean openPort(String DevicePath, int Baudrate, boolean IsDecode) {
        this.DevicePath = DevicePath;
        this.Baudrate = Baudrate;
        this.IsDecode = IsDecode;

        try {
            serialPort = new JniSerialPort(new File(this.DevicePath), this.Baudrate, 0);
        } catch (IOException e) {
            //PortStatus = false;
            Log.e(TAG, "串口打开失败>>>>>>>JniSerialPort fail:" + e.toString());
            return false;
        }

        if (serialPort.isDeviceReady()) {
            inputStream = serialPort.getInputStream();
            outputStream = serialPort.getOutputStream();

            if (IsDecode)
                new DeccodeThread().start();

            new ReadThread().start();
        } else {
            Log.e(TAG, "串口无法工作>>>>>");
            return false;
        }

        Log.d(TAG, "成功打开串口>>>>>>>>>"+this.DevicePath+",Baudrate:"+this.Baudrate);
        threadStatus = true; //线程状态
        return true;
    }

    /**
     * 关闭串口
     */
    public void closePort() {
        try {
            inputStream.close();
            outputStream.close();

            this.threadStatus = false; //线程状态
            this.IsDecode = false;
            serialPort.close();
        } catch (IOException e) {
            //Log.e(TAG, "closePort: 关闭串口异常：" + e.toString());
            return;
        }
    }

    public void sendString(String data) {
        //Log.d(TAG, "sendSerialPort: 发送数据");
        try {
            byte[] sendData = data.getBytes(); //string转byte[]
            //this.data_ = new String(sendData); //byte[]转string
            if (sendData.length > 0) {
                outputStream.write(sendData);
                outputStream.write('\n');
                //outputStream.write('\r'+'\n');
                outputStream.flush();
                //Log.d(TAG, "sendSerialPort: 串口数据发送成功");
            }
        } catch (IOException e) {
            Log.e(TAG, "sendString fail：" + e.toString());
        }

    }

    public void sendBuffer(byte[] data) {
        try {
            byte[] sendData = data;  //string转byte[]
            outputStream.write(sendData);
            outputStream.flush();
        } catch (IOException e) {
            Log.e(TAG, "sendBuffer fail：" + e.toString());
        }
    }

    public void setOnDataReceiveListener(OnDataReceiveListener dataReceiveListener) {
        onDataReceiveListener = dataReceiveListener;
    }

    public interface OnDataReceiveListener {
        void onDataReceive(Context context, byte[] buffer, int size);
    }

    /**
     * 单开一线程，来读数据
     */

    private class ReadThread extends Thread
    {
        private byte[] buffer = new byte[512];
        private int size; //读取数据的大小

        @Override
        public void run() {
            super.run();
            //判断进程是否在运行，更安全的结束进程
            while (threadStatus) {
                try {
                    size = inputStream.read(buffer);
                    if (size > 0) {
                        //Log.i(TAG, DevicePath+" Read File:" + size + "|" + ByteArrToHexStr(buffer, 0, size));

                        if (IsDecode) {
                            for (int i = 0; i < size; i++)
                                mDataQueue.add(buffer[i]);
                        } else {
                            onDataReceiveListener.onDataReceive(mContext, buffer, size);
                        }
                    }
                } catch (IOException e) {
                    Log.e(TAG, "数据读取异常：" + e.toString());
                }
            }
        }
    }

    private class DeccodeThread extends Thread {
        private final int TIMEOUTCOUNT = 10;
        private byte mCurrentByte;
        private int len = 0, timeout = 0;
        private List<Byte> byteArrayList = new ArrayList<>();
        @Override
        public void run() {

            while (true) {

                switch (mState) {
                    case STATE_PRODUCT_CODE: //0x01, 0x01
                        if (mDataQueue.size() >= 2) {
                            byteArrayList.clear();
                            len = 0;
                            mCurrentByte = mDataQueue.remove();
                            if ((mCurrentByte == 0x01) || (mCurrentByte == 0x02)) {
                                byteArrayList.add(mCurrentByte);
                                Byte aByte = mDataQueue.peek();
                                if (aByte != null) {
                                    if (aByte == 0x01) {
                                        mState++;
                                        mDataQueue.remove();
                                        byteArrayList.add(aByte);
                                    } else {
                                        break;
                                    }
                                } else {
                                    break;
                                }
                            }
                        } else {
                            if (timeout >= TIMEOUTCOUNT) {
                                timeout = 0;
                                mState = STATE_PRODUCT_CODE;
                                mDataQueue.clear();
                                byteArrayList.clear();
                                break;
                            }
                            try {
                                sleep(SLEEP_TIMEOUT);
                                timeout++;
                            } catch (InterruptedException e) {
                                e.printStackTrace();
                            }
                        }
                        break;
                    case STATE_MSG_ID:
                        if (mDataQueue.size() >= 2) {
                            //1st byte
                            mCurrentByte = mDataQueue.remove();
                            byteArrayList.add(mCurrentByte);
                            //2nd byte
                            mCurrentByte = mDataQueue.remove();
                            byteArrayList.add(mCurrentByte);
                            mState++;
                        } else {
                            try {
                                if (timeout >= TIMEOUTCOUNT) {
                                    timeout = 0;
                                    mState = STATE_PRODUCT_CODE;
                                    mDataQueue.clear();
                                    byteArrayList.clear();
                                    break;
                                }
                                sleep(SLEEP_TIMEOUT);
                                timeout++;
                            } catch (InterruptedException e) {
                                e.printStackTrace();
                            }
                        }
                        break;
                    case STATE_ANSWER:
                        if (mDataQueue.size() >= 1) {
                            mCurrentByte = mDataQueue.remove();
                            byteArrayList.add(mCurrentByte);
                            mState++;
                        } else {
                            try {
                                if (timeout >= TIMEOUTCOUNT) {
                                    timeout = 0;
                                    mState = STATE_PRODUCT_CODE;
                                    mDataQueue.clear();
                                    byteArrayList.clear();
                                    break;
                                }
                                sleep(SLEEP_TIMEOUT);
                                timeout++;
                            } catch (InterruptedException e) {
                                e.printStackTrace();
                            }
                        }
                        break;
                    case STATE_LENGTH:
                        if (mDataQueue.size() >= 2) {
                            //1st byte
                            mCurrentByte = mDataQueue.remove();
                            byteArrayList.add(mCurrentByte);
                            len |= mCurrentByte;
                            //2nd byte
                            mCurrentByte = mDataQueue.remove();
                            byteArrayList.add(mCurrentByte);
                            len |= (mCurrentByte << 8);
                            mState++;
                        } else {
                            try {
                                if (timeout >= TIMEOUTCOUNT) {
                                    timeout = 0;
                                    mState = STATE_PRODUCT_CODE;
                                    mDataQueue.clear();
                                    byteArrayList.clear();
                                    break;
                                }
                                sleep(SLEEP_TIMEOUT);
                                timeout++;
                            } catch (InterruptedException e) {
                                e.printStackTrace();
                            }
                        }
                        break;
                    case STATE_DATA:
                        if (mDataQueue.size() >= len) {
                            for (int k = 0; k < len; k++) {
                                mCurrentByte = mDataQueue.remove();
                                byteArrayList.add(mCurrentByte);
                            }
                            mState++;
                        } else {
                            try {
                                if (timeout >= TIMEOUTCOUNT) {
                                    timeout = 0;
                                    mState = STATE_PRODUCT_CODE;
                                    mDataQueue.clear();
                                    byteArrayList.clear();
                                    break;
                                }
                                sleep(SLEEP_TIMEOUT);
                                timeout++;
                            } catch (InterruptedException e) {
                                e.printStackTrace();
                            }
                        }
                        break;
                    case STATE_CHECKSUM:
                        if (mDataQueue.size() >= 1) {
                            int sum = 0;
                            for (Byte b : byteArrayList) {
                                sum += (b & 0xFF);
                            }
                            sum &= 0xFF;
                            mCurrentByte = mDataQueue.remove();
                            if (sum == (mCurrentByte & 0xFF)) {
                                byteArrayList.add(mCurrentByte);
                                mState++;
                            } else {
                                len = 0;
                                byteArrayList.clear();
                                mState = STATE_PRODUCT_CODE;
                                break;
                            }
                        } else {
                            try {
                                if (timeout >= TIMEOUTCOUNT) {
                                    timeout = 0;
                                    mState = STATE_PRODUCT_CODE;
                                    mDataQueue.clear();
                                    byteArrayList.clear();
                                    break;
                                }
                                sleep(SLEEP_TIMEOUT);
                                timeout++;
                            } catch (InterruptedException e) {
                                e.printStackTrace();
                            }
                        }
                    case STATE_END:
                        if (mDataQueue.size() >= 1) {
                            mCurrentByte = mDataQueue.remove();
                            if ((mCurrentByte & 0xFF) == 0x7E) {
                                byteArrayList.add(mCurrentByte);
                                int cmd_len = byteArrayList.size();
                                byte[] cmd_pkt = new byte[cmd_len];
                                for (int i = 0; i < cmd_len; i++) {
                                    cmd_pkt[i] = byteArrayList.get(i);
                                }

                                Log.i(TAG, "Got Data: " + ByteArrToHex(cmd_pkt));
                                onDataReceiveListener.onDataReceive(mContext, cmd_pkt, cmd_len);
                            } else {
                                len = 0;
                                byteArrayList.clear();
                            }
                            mState = STATE_PRODUCT_CODE;
                        } else {
                            try {
                                if (timeout >= TIMEOUTCOUNT) {
                                    timeout = 0;
                                    mState = STATE_PRODUCT_CODE;
                                    mDataQueue.clear();
                                    byteArrayList.clear();
                                    //Log.i(TAG, "Got Data: " + ByteArrToHex(cmd_pkt));
                                    break;
                                }
                                sleep(SLEEP_TIMEOUT);
                                timeout++;
                            } catch (InterruptedException e) {
                                e.printStackTrace();
                            }
                        }
                        break;
                }
            }
        }
    }

    public String getDevicePath() {
        return DevicePath;
    }

    public int getBaudrate() {
        return Baudrate;
    }
}
