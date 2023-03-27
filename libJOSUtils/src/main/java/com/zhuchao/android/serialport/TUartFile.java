package com.zhuchao.android.serialport;

import static com.zhuchao.android.fbase.FileUtils.EmptyString;

import com.zhuchao.android.fbase.DataID;
import com.zhuchao.android.fbase.EventCourier;
import com.zhuchao.android.fbase.EventCourierInterface;
import com.zhuchao.android.fbase.MMLog;
import com.zhuchao.android.fbase.TCourierEventListener;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Queue;
import java.util.concurrent.LinkedBlockingQueue;

//实现总线接口，接收异步总线写入
public class TUartFile extends TDevice implements TCourierEventListener {
    private final String TAG = "TUartFile";
    private SerialPort serialPort = null;
    private ReadThread readThread = null;
    private ParserThread parserThread = null;
    private final Queue<Byte> queueDatas = new LinkedBlockingQueue<>();
    private int frame_size = 9;//当前串口协议一个数据包至少9个字节
    private String frame_end_hex = "7E";
    private String f_separator = " ";
    private TCourierEventListener deviceReadingEventListener = null;
    private long writeDelayTime_millis = 0;

    public long getWriteDelayTime() {
        return writeDelayTime_millis;
    }

    public void setWriteDelayTime(long millis) {
        this.writeDelayTime_millis = millis;
    }

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
            MMLog.log(TAG, "device open successfully " + toDeviceString());
        } catch (IOException e) {
            //e.printStackTrace();
            MMLog.e(TAG, "getSerialPort() returns null " + e.toString() + "," + devicePath + " " + baudrate);
            serialPort = null;
        }
        setDevicePath(devicePath);
        setDeviceType("UART");
    }

    public void startPollingRead() {
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

    @Override
    public void closeDevice() {
        if (serialPort != null) {
            serialPort.tryClose();
            serialPort = null;
        }
    }

    private void dispatchCourier(EventCourier eventCourier) {
        if (deviceReadingEventListener != null) {//处理一帧数据
            TProtocol_Package tProtocol_package = new TProtocol_Package();
            tProtocol_package.parse(eventCourier.getDatas());
            if(tProtocol_package.getMsgHead() == tProtocol_package.DEFAULT_HEAD &&
                    tProtocol_package.getMsg_End() == tProtocol_package.DEFAULT_END) {
                eventCourier.setObj(tProtocol_package);
            }

            deviceReadingEventListener.onCourierEvent(eventCourier);
            //MMLog.log(TAG, "deviceReadingEventListener = "+deviceReadingEventListener.toString()+", eventCourier = "+eventCourier.toString());
        }
        else
        {
            MMLog.log(TAG, "deviceReadingEventListener = null, eventCourier = "+eventCourier.toString());
        }
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

    public boolean isReadyPooling() {
        if (serialPort == null) {
            MMLog.log(TAG, "serialPort = false");
            return false;
        } else return serialPort.isDeviceReady();
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

    public int getFrame_size() {
        return frame_size;
    }

    public void setFrame_size(int frame_size) {
        this.frame_size = frame_size;
    }

    public String getFrame_end_hex() {
        return frame_end_hex;
    }

    public void setFrame_end_hex(String frame_end_hex) {
        this.frame_end_hex = frame_end_hex;
    }

    public String getF_separator() {
        return f_separator;
    }

    public void setF_separator(String f_separator) {
        this.f_separator = f_separator;
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

    private void writeBytes(byte[] bytes) {
        if (!isReadyPooling()) {
            MMLog.d(TAG, "device does not work ");
            return;
        }
        if (bytes.length <= 0) {
            MMLog.d(TAG, "no data to write ");
            return;
        }

        try {
            if (serialPort.getOutputStream() != null) {
                serialPort.getOutputStream().write(bytes);
                serialPort.getOutputStream().flush();
                MMLog.d(TAG, "uart write data: " + BufferToHexStr(bytes, " ") + " " + toDeviceString());
            }
        } catch (IOException e) {
            MMLog.d(TAG, "device write failed " + e.toString());
        }
    }

    public void writeBytesWait(byte[] bytes) {
        //写入后休眠等待外部设备,
        while (readThread != null && !readThread.getState().toString().equals("RUNNABLE")) {
            writeBytes(bytes);
            break;
        }
        if (writeDelayTime_millis > 0) {
            try {
                Thread.sleep(writeDelayTime_millis);
            } catch (InterruptedException e) {
                //e.printStackTrace();
            }
        }
    }

    public void writeBytesWait2(byte[] bytes) {
        writeBytes(bytes);
        if (writeDelayTime_millis > 0) {
            try {
                Thread.sleep(writeDelayTime_millis);
            } catch (InterruptedException e) {
                //e.printStackTrace();
            }
        }
    }

    @Override//接收外部事件消息
    public synchronized boolean onCourierEvent(EventCourierInterface eventCourier) {
        if (serialPort == null) return false;
        switch (eventCourier.getId()) {
            case DataID.DEVICE_EVENT_UART_WRITE:
            case DataID.DEVICE_EVENT_WRITE:
                if (eventCourier.getTag().equals(serialPort.getDevice().getAbsolutePath())) {
                    if (eventCourier.getDatas() != null) {
                        //writeBytesWait(eventCourier.getDatas());
                        //writeBytes(eventCourier.getDatas());
                        writeBytesWait2(eventCourier.getDatas());
                    }
                    return true;
                }
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
                        List<Byte> newByteListData = new ArrayList<>();
                        Collections.addAll(newByteListData, new Byte[byteArrayList.size()]);//复制容量
                        Collections.copy(newByteListData, byteArrayList);//复制内容
                        Byte[] byteData = newByteListData.toArray(new Byte[newByteListData.size()]);//列表转成数组类（引用）

                        //将类引用转换成字节数组
                        byte[] bytes = new byte[newByteListData.size()];
                        for (int i = 0; i < byteData.length; i++) {
                            bytes[i] = byteData[i];
                        }
                        byteArrayList.clear();
                        //处理一帧数据
                        //MMLog.log(TAG,"DEBUG READ UART:"+BufferToHexStr(bytes,f_separator));
                        dispatchCourier(new EventCourier(serialPort.getDevice().getAbsolutePath(), DataID.DEVICE_EVENT_UART_READ, bytes));
                    } else if (frame_end_hex.equals("FF")) {
                        MMLog.log(TAG, "RT READ UART:" + strEndFrame);
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
            strBuilder.append(ByteToHexStr(valueOf));
            strBuilder.append(separatorChars);
        }
        return strBuilder.toString();
    }

    //1字节转2个Hex字符
    public String ByteToHexStr(Byte inByte) {
        return String.format("%02x", inByte).toUpperCase();
    }
}
