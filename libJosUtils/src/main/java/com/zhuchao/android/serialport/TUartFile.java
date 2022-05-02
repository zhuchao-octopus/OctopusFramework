package com.zhuchao.android.serialport;

import static com.zhuchao.android.libfileutils.FileUtils.EmptyString;

import com.zhuchao.android.callbackevent.DeviceCourierEventListener;
import com.zhuchao.android.callbackevent.EventCourier;
import com.zhuchao.android.libfileutils.DataID;
import com.zhuchao.android.libfileutils.MMLog;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Queue;
import java.util.concurrent.LinkedBlockingQueue;

public class TUartFile implements DeviceCourierEventListener {
    private final String TAG = "TUartFile";
    private SerialPort serialPort = null;
    private ReadThread readThread = null;
    private ParserThread parserThread = null;
    private static final Queue<Byte> queueDatas = new LinkedBlockingQueue<>();
    private static int frame_size = 9;
    private static String frame_end_hex = "7E";
    private static String f_separator = " ";
    private DeviceCourierEventListener deviceEventListener = null;

    public TUartFile(String devicePath, int baudrate) {
        if (EmptyString(devicePath) || (baudrate <= 0)) {
            MMLog.e(TAG, "invalid device parameter can not to open device," + devicePath + " " + baudrate);
            return;
        }
        try {
            serialPort = SerialPort.newBuilder(devicePath, baudrate) // 串口地址地址，波特率
                    .dataBits(8) // 数据位,默认8；可选值为5~8
                    .stopBits(1) // 停止位，默认1；1:1位停止位；2:2位停止位
                    .parity(0) // 校验位；0:无校验位(NONE，默认)；1:奇校验位(ODD);2:偶校验位(EVEN)
                    .build();
            MMLog.log(TAG, "device open successfully," + toDeviceString());
        } catch (IOException e) {
            //e.printStackTrace();
            MMLog.e(TAG, "getSerialPort() returns null " + e.toString() + "," + devicePath + " " + baudrate);
            serialPort = null;
        }
    }

    public void startPoolingRead() {
        if (!isReadyPooling()) {
            MMLog.log(TAG, "device can not start pooling,is not ready");
            return;
        }
        try {
            if (readThread == null) {
                readThread = new ReadThread();
                readThread.start();
            }
            if (parserThread == null) {
                parserThread = new ParserThread();
                parserThread.start();
            }
            MMLog.log(TAG, "device access successfully start pooling," + toDeviceString());
        } catch (Exception e) {
            MMLog.log(TAG, "device access failed do not start " + e.toString() + "," + toDeviceString());
        }
    }

    public void closeDevice() {
        if (serialPort != null) {
            serialPort.tryClose();
            serialPort = null;
        }
    }

    public void writeBytes(byte[] bytes) {
        if (!isReadyPooling()) {
            MMLog.d(TAG, "device can not work ");
            return;
        }

        try {
            if (serialPort.getOutputStream() != null) {
                serialPort.getOutputStream().write(bytes);
                serialPort.getOutputStream().flush();
                MMLog.d(TAG, "device write data " + BufferToHexStr(bytes," "));
            }
        } catch (IOException e) {
            MMLog.d(TAG, "device write failed " + e.toString());
        }
    }

    private void dispatchCourier(EventCourier eventCourier) {
        if (deviceEventListener != null) {//处理一帧数据
            deviceEventListener.onCourierEvent(eventCourier);
        }
    }

    public void callback(DeviceCourierEventListener deviceEventListener) {
        this.deviceEventListener = deviceEventListener;
    }

    public boolean isReadyPooling() {
        if (serialPort != null && serialPort.isDeviceReady())
            return true;
        else
            return false;
    }

    public boolean isPooling() {
        if (readThread == null || parserThread == null || serialPort == null) {
            return false;
        }
        if (serialPort.isDeviceReady() && readThread.isAlive() && parserThread.isAlive())
            return true;
        else
            return false;
    }

    public static int getFrame_size() {
        return frame_size;
    }

    public static void setFrame_size(int frame_size) {
        TUartFile.frame_size = frame_size;
    }

    public static String getFrame_end_hex() {
        return frame_end_hex;
    }

    public static void setFrame_end_hex(String frame_end_hex) {
        TUartFile.frame_end_hex = frame_end_hex;
    }

    public static String getF_separator() {
        return f_separator;
    }

    public static void setF_separator(String f_separator) {
        TUartFile.f_separator = f_separator;
    }

    public String getTAG() {
        return TAG;
    }

    public String toDeviceString() {
        String str = serialPort.getDevice().getAbsolutePath();
        str += "," + serialPort.getBaudrate();
        str += "," + serialPort.getDataBits();
        str += "," + serialPort.getStopBits();
        str += "," + serialPort.getParity();
        str += "," + serialPort.getFlags();
        return str;
    }

    public String getDeviceTag() {
        if (isReadyPooling())
            return serialPort.getDevice().getAbsolutePath();
        return " ";
    }

    @Override
    public synchronized boolean onCourierEvent(EventCourier eventCourier) {
        if (eventCourier.getId() == DataID.DEVICE_EVENT_WRITE
                && eventCourier.getTag().equals(serialPort.getDevice().getAbsolutePath()))
        {
            writeBytes(eventCourier.getDatas());
            return true;
        }
        return false;
    }

    private class ReadThread extends Thread {
        @Override
        public void run() {
            super.run();
            try {
                while (serialPort.isDeviceReady()) {
                    if (serialPort.getInputStream().available() >= frame_size) {
                        byte[] readData = new byte[serialPort.getInputStream().available()];
                        int size = serialPort.getInputStream().read(readData);
                        if (size <= 0) continue;
                        for (int i = 0; i < size; i++) {
                            byte bitData = readData[i];
                            queueDatas.offer(bitData);
                        }
                    } else {
                        Thread.sleep(50);
                    }
                }
            } catch (Exception e) {
                //e.printStackTrace();
            }
        }
    }

    //queueDatas --> byteArrayList --> List<Byte> newData --> Byte[] byteData --> bytes
    private class ParserThread extends Thread {
        @Override
        public void run() {
            super.run();
            List<Byte> byteArrayList = new ArrayList<>();
            while (serialPort.isDeviceReady())
                try {
                    Byte aByte = queueDatas.poll();
                    if (aByte == null) {
                        Thread.sleep(20);
                        continue;
                    }
                    byteArrayList.add(aByte);
                    String strEndFrame = ByteToHexStr(aByte);
                    if (frame_end_hex.equals(strEndFrame) && byteArrayList.size() >= frame_size) {
                        List<Byte> newData = new ArrayList<>();
                        Collections.addAll(newData, new Byte[byteArrayList.size()]);//复制容量
                        Collections.copy(newData, byteArrayList);//复制内容
                        Byte[] byteData = newData.toArray(new Byte[newData.size()]);//列表转成数组类（引用）

                        //将类引用转换成字节数组
                        byte[] bytes = new byte[newData.size()];
                        for (int i = 0; i < byteData.length; i++) {
                            bytes[i] = byteData[i];
                        }
                        //MMLog.log(TAG,"UART:"+BufferToHexStr(bytes,f_separator));
                        byteArrayList.clear();
                        //处理一帧数据
                        dispatchCourier(new EventCourier(serialPort.getDevice().getAbsolutePath(), DataID.DEVICE_EVENT_READ, bytes));
                    }
                } catch (Exception e) {
                    //e.printStackTrace();
                }
        }
    }

    //字节数组转转hex字符串
    public String BufferToHexStr(byte[] bytes, String separatorChars) {
        StringBuilder strBuilder = new StringBuilder();
        for (byte valueOf : bytes) {
            strBuilder.append(ByteToHexStr(Byte.valueOf(valueOf)));
            strBuilder.append(separatorChars);
        }
        return strBuilder.toString();
    }

    //1字节转2个Hex字符
    public String ByteToHexStr(Byte inByte) {
        return String.format("%02x", inByte).toUpperCase();
    }
}
