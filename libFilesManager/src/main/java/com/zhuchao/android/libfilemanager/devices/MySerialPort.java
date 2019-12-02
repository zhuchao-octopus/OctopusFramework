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

import static com.zhuchao.android.libfilemanager.devices.TypeTool.Byte2Hex;
import static com.zhuchao.android.libfilemanager.devices.TypeTool.ByteArrToHex;
import static com.zhuchao.android.libfilemanager.devices.TypeTool.ByteArrToHexStr;


public class MySerialPort {

    private final String TAG = "MySerialPort";
    //add by cvte start >>>
    private final ConcurrentLinkedQueue<Byte> mDataQueue = new ConcurrentLinkedQueue<>();
    public OnDataReceiveListener onDataReceiveCallback = null;
    private String DevicePath = "/dev/ttyS0";
    private int Baudrate = 9600;
    private Context mContext;

    private boolean ThreadRunning = false; //线程状态，为了安全终止线程
    private JniSerialPort serialPort = null;
    private InputStream inputStream = null;
    private OutputStream outputStream = null;

    private boolean Decoding = false;

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
        this.Decoding = IsDecode;
        ThreadRunning = true; //线程状态

        try {
            serialPort = new JniSerialPort(new File(this.DevicePath), this.Baudrate, 0);
        } catch (IOException e) {
            Log.e(TAG, "串口打开失败>>>>>>>JniSerialPort fail:" + e.toString());
            return false;
        }

        if (serialPort.isDeviceReady()) {
            inputStream = serialPort.getInputStream();
            outputStream = serialPort.getOutputStream();

            if (Decoding)
                new DeccodeThread().start();

            new ReadThread().start();
        } else {
            Log.e(TAG, "串口设备还没有准备好！！！！");
            return false;
        }

        Log.d(TAG, "成功打开串口>>>>>>>>> " + this.DevicePath + ",Baudrate:" + this.Baudrate);

        return true;
    }

    /**
     * 关闭串口
     */
    public void closePort() {
        try {
            inputStream.close();
            outputStream.close();

            this.ThreadRunning = false; //线程状态
            this.Decoding = false;
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

    public void setOnDataReceiveCallback(OnDataReceiveListener dataReceiveListener) {
        onDataReceiveCallback = dataReceiveListener;
    }

    public interface OnDataReceiveListener {
        void onDataReceive(Context context, byte[] buffer, int size);
    }

    /**
     * 单开一线程，来读数据
     */

    private class ReadThread extends Thread {
        private byte[] buffer = new byte[512];
        private int size; //读取数据的大小

        @Override
        public void run() {
            super.run();
            Log.i(TAG, "ReadThread >>>>>>> running:" + ThreadRunning);
            //判断进程是否在运行，更安全的结束进程
            while (ThreadRunning) {
                try {
                    size = inputStream.read(buffer);
                    if (size > 0) {
                        //Log.i(TAG, DevicePath + " Read File:" + size + "|" + ByteArrToHexStr(buffer, 0, size));

                        if (Decoding) {
                            for (int i = 0; i < size; i++)
                                mDataQueue.add(buffer[i]);
                        } else if(onDataReceiveCallback != null){
                            onDataReceiveCallback.onDataReceive(mContext, buffer, size);
                        }
                    }
                } catch (IOException e) {
                    Log.e(TAG, "数据读取异常：" + e.toString());
                }
            }
        }
    }

    private class DeccodeThread extends Thread {
        private final static int SLEEP_TIMEOUT = 30;
        private final static int STATE_PRODUCT_CODE = 1;
        private final static int STATE_MSG_ID = 2;
        private final static int STATE_ANSWER = 3;
        private final static int STATE_LENGTH = 4;
        private final static int STATE_DATA = 5;
        private final static int STATE_CHECKSUM = 6;
        private final static int STATE_END = 7;
        private final static int STATE_RESTART = 8;
        private final Object lock = new Object();
        private boolean pause = false;
        //private final int TIMEOUTCOUNT = 10;
        private int mState = STATE_PRODUCT_CODE;
        private List<Byte> byteArrayList = new ArrayList<>();

        void pauseThread() {
            pause = true;
        }

        void resumeThread() {
            pause = false;
            synchronized (lock) {
                lock.notifyAll();
            }
        }

        void onPause() {
            synchronized (lock) {
                try {
                    lock.wait();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }

        //Byte aByte = mDataQueue.peek();// 获取但不移除此队列的头；如果此队列为空，则返回 null
        boolean gotoTheHead() {
            if (mDataQueue.size() >= 2)
            {
                byte b0 = mDataQueue.remove();
                byte b1 = mDataQueue.remove();
                if (
                        ((b0 == 0x01) || (b0 == 0x02)) && (b1 == 0x01)
                    )
                {
                    byteArrayList.clear();
                    byteArrayList.add(b0);
                    byteArrayList.add(b1);
                    return true;
                }
                else
                {
                    Log.e(TAG, "invalid product code: "+Byte2Hex((byte)b0)+"/"+Byte2Hex(b1));
                    return false;
                }
            }

            return false;
        }

        @Override
        public void run() {

            byte mCurrentByte;
            int length = 0;
            Log.i(TAG, "DecodeThread>>>>>>> running:"+Decoding);

            while (Decoding) {

                switch (mState) {
                    case STATE_PRODUCT_CODE: //0x01, 0x01
                        if(gotoTheHead())
                        {
                            length = 0;
                            mState = STATE_MSG_ID;
                        }
                        break;
                    case STATE_MSG_ID://命令ID
                        if (mDataQueue.size() >= 2)
                        {
                            //1st byte
                            mCurrentByte = mDataQueue.remove();
                            byteArrayList.add(mCurrentByte);
                            //2nd byte
                            mCurrentByte = mDataQueue.remove();
                            byteArrayList.add(mCurrentByte);
                            mState = STATE_ANSWER;
                        }
                        break;
                    case STATE_ANSWER:
                        if (mDataQueue.size() >= 1)
                        {
                            mCurrentByte = mDataQueue.remove();
                            byteArrayList.add(mCurrentByte);
                            mState = STATE_LENGTH;
                        }
                        break;
                    case STATE_LENGTH:
                        if (mDataQueue.size() >= 2)
                        {
                            //1st byte
                            mCurrentByte = mDataQueue.remove();
                            byteArrayList.add(mCurrentByte);
                            length |= mCurrentByte;
                            //2nd byte
                            mCurrentByte = mDataQueue.remove();
                            byteArrayList.add(mCurrentByte);
                            length |= (mCurrentByte << 8);
                            mState =STATE_DATA;
                        }
                        break;
                    case STATE_DATA:
                        if (mDataQueue.size() >= length)
                        {
                            for (int k = 0; k < length; k++) {
                                mCurrentByte = mDataQueue.remove();
                                byteArrayList.add(mCurrentByte);
                            }
                            mState = STATE_CHECKSUM;
                        }
                        else if(length > 255)
                        {
                            Log.e(TAG, "unsupport data length: "+Byte2Hex((byte)length));
                            //length = 0;
                            //byteArrayList.clear();
                        }
                        break;
                    case STATE_CHECKSUM:
                        if (mDataQueue.size() >= 1)
                        {
                            int sum = 0;
                            mCurrentByte = mDataQueue.remove();

                            for (Byte b : byteArrayList) {
                                sum += (b & 0xFF);
                            }
                            sum &= 0xFF;

                            if (sum == (mCurrentByte & 0xFF)) {
                                byteArrayList.add(mCurrentByte);
                                mState = STATE_END;
                            }
                            else
                            {
                                Log.e(TAG, "invalid checksum: "+Byte2Hex((byte)sum)+"/"+Byte2Hex(mCurrentByte));
                                length = 0;
                                byteArrayList.clear();
                                mState = STATE_PRODUCT_CODE;
                                break;
                            }
                        }
                        break;
                    case STATE_END:
                        if (mDataQueue.size() >= 1)
                        {
                            mCurrentByte = mDataQueue.remove();
                            if ((mCurrentByte & 0xFF) == 0x7E)
                            {
                                byteArrayList.add(mCurrentByte);
                                int cmd_len = byteArrayList.size();
                                byte[] cmd_pkt = new byte[cmd_len];
                                for (int i = 0; i < cmd_len; i++) {
                                    cmd_pkt[i] = byteArrayList.get(i);
                                }

                                Log.i(TAG, "got data: " + ByteArrToHex(cmd_pkt));
                                if(onDataReceiveCallback != null)
                                   onDataReceiveCallback.onDataReceive(mContext, cmd_pkt, cmd_len);
                            } else {
                                Log.e(TAG, "invalid end flag: "+Byte2Hex((byte)mCurrentByte));
                                length = 0;
                                byteArrayList.clear();
                            }
                            mState = STATE_PRODUCT_CODE;
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
