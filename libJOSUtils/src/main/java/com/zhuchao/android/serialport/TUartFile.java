package com.zhuchao.android.serialport;

import static com.zhuchao.android.fbase.FileUtils.EmptyString;

import com.zhuchao.android.fbase.ByteUtils;
import com.zhuchao.android.fbase.DataID;
import com.zhuchao.android.fbase.EventCourier;
import com.zhuchao.android.fbase.eventinterface.EventCourierInterface;
import com.zhuchao.android.fbase.MMLog;
import com.zhuchao.android.fbase.ObjectList;
import com.zhuchao.android.fbase.eventinterface.TCourierEventListener;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Queue;
import java.util.concurrent.LinkedBlockingQueue;

//实现总线接口，接收异步总线写入
public class TUartFile extends TDevice implements TCourierEventListener {
    private final String TAG = "TUartFile";
    private final int defaultStartCode0 = 0x0101;
    private final int defaultStartCode1 = 0xAF55;
    private final int defaultStartCode2 = 0x55AF;

    private SerialPort serialPort = null;
    private ReadThread readThread = null;
    private ParserThread parserThread = null;
    private final Queue<Byte> queueDatas = new LinkedBlockingQueue<>();
    private int frame_size = 9;//当前串口协议一个数据包至少9个字节

    private final List<Integer> frameHeadCodeList = new ArrayList<>();
    private final List<Integer> frameEndCodeList = new ArrayList<>();
    private String f_separator = " ";
    //private TCourierEventListener deviceReadingEventListener = null;
    private final ObjectList deviceOnReceiveEventListenerList = new ObjectList();
    private long writeTimeDelay_millis = 0;
    private long readTimeout_millis = 50;

    public long getWriteDelayTime() {
        return writeTimeDelay_millis;
    }

    public void setWriteDelayTime(long millis) {
        this.writeTimeDelay_millis = millis;
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
        frameHeadCodeList.add(defaultStartCode0);
        frameHeadCodeList.add(defaultStartCode1);
        frameHeadCodeList.add(defaultStartCode2);

        //frameEndCodeList.add(0x7E);
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

    private void dispatchCourier(EventCourier eventCourier) {
        if (deviceOnReceiveEventListenerList.getCount() > 0) {//处理一帧数据
            TProtocol_Package tProtocol_package = new TProtocol_Package();
            tProtocol_package.parse(eventCourier.getDatas());
            if (tProtocol_package.getMsgHead() == tProtocol_package.DEFAULT_HEAD &&
                    tProtocol_package.getMsg_End() == tProtocol_package.DEFAULT_END) {
                eventCourier.setObj(tProtocol_package);
            }

            Collection<Object> objects = deviceOnReceiveEventListenerList.getAllObject();
            for (Object o : objects) {
                TCourierEventListener CourierEventListener = ((TCourierEventListener) o);
                CourierEventListener.onCourierEvent(eventCourier);
                //MMLog.log(TAG, "deviceReadingEventListener = "+deviceReadingEventListener.toString()+", eventCourier = "+eventCourier.toString());
            }
        } else {
            MMLog.log(TAG, "deviceEventListener=null, eventCourier=" + eventCourier.toString());
        }
    }

    public void callback(TCourierEventListener deviceEventListener) {
        //this.deviceReadingEventListener = deviceEventListener;
        deviceOnReceiveEventListenerList.putObject(deviceEventListener.toString(), deviceEventListener);
    }

    public void registerReadingCallback(TCourierEventListener deviceEventListener) {
        //this.deviceReadingEventListener = deviceEventListener;
        callback(deviceEventListener);
    }

    public void registerOnReceivedCallback(TCourierEventListener deviceEventListener) {
        //this.deviceReadingEventListener = deviceEventListener;
        callback(deviceEventListener);
    }

    public void removeCallback(TCourierEventListener deviceEventListener) {
        deviceOnReceiveEventListenerList.delete(deviceEventListener.toString(), deviceEventListener);
    }

    public void unRegisterOnReceivedCallback(TCourierEventListener deviceEventListener) {
        removeCallback(deviceEventListener);
    }

    public void removeAllCallback() {
        deviceOnReceiveEventListenerList.clear();
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

        return serialPort.isDeviceReady() && readThread.isAlive() && parserThread.isAlive();
    }

    public int getFrame_size() {
        return frame_size;
    }

    public void setFrame_size(int frame_size) {
        this.frame_size = frame_size;
    }

    public void addFrameStartCode(int frameStartCode) {
        this.frameHeadCodeList.add(frameStartCode);
    }

    public void clearFrameStartCode() {
        this.frameHeadCodeList.clear();
    }

    public void addFrameEndCode(int frameEndCode) {
        this.frameEndCodeList.add(frameEndCode);
    }
    public void clearFrameEndCode() {
        this.frameEndCodeList.clear();
    }


    public String getF_separator() {
        return f_separator;
    }

    public void setF_separator(String f_separator) {
        this.f_separator = f_separator;
    }

    public long getReadTimeout_millis() {
        return readTimeout_millis;
    }

    public void setReadTimeout_millis(long readTimeout_millis) {
        this.readTimeout_millis = readTimeout_millis;
    }

    public String toDeviceString() {
        String str = serialPort.getDevice().getAbsolutePath();
        str += "," + serialPort.getBaudRate();
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
        if (bytes.length == 0) {
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
        if (writeTimeDelay_millis > 0) {
            try {
                Thread.sleep(writeTimeDelay_millis);
            } catch (InterruptedException e) {
                //e.printStackTrace();
            }
        }
    }

    public void writeBytesWait2(byte[] bytes) {
        writeBytes(bytes);
        if (writeTimeDelay_millis > 0) {
            try {
                Thread.sleep(writeTimeDelay_millis);
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
            while (serialPort.isDeviceReady())
            {
                try {
                    if (serialPort.getInputStream().available() >= frame_size)
                    {
                        byte[] readData = new byte[serialPort.getInputStream().available()];
                        int size = serialPort.getInputStream().read(readData);
                        if (size <= 0) continue;
                        for (int i = 0; i < size; i++) {
                            byte bitData = readData[i];
                            queueDatas.offer(bitData);
                        }
                    }
                    else
                    {
                        Thread.sleep(30);
                    }
                } catch (Exception e) {
                    //e.printStackTrace();
                }
            }// while (serialPort.isDeviceReady())
        }
    }

    //queueDatas --> byteArrayList --> List<Byte> newData --> Byte[] byteData --> bytes
    private class ParserThread extends Thread {
        final int WAITING_TIME_MS = 10;
        List<Byte> byteArrayList = new ArrayList<>();
        int startHeadCode = 0;
        int frameLength = -1;
        int startHeadCodeIndex = 0;
        int timeout = 0;
        boolean ok_going = false;
        private void appendByte(Byte aByte) {
            if (aByte != null) {
                byteArrayList.add(aByte);
            }
        }

        private void reset_clear() {
            byteArrayList.clear();
            timeout = 0;
            startHeadCode = 0;
            frameLength = -1;
            startHeadCodeIndex = 0;
        }

        @Override
        public void run() {
            super.run();
            byte starFrame0 = 0;
            byte starFrame1 = 0;
            int endFrame = 0;

            while (serialPort.isDeviceReady())
            {
                try {
                    Byte aByte = queueDatas.poll();
                    if (aByte == null) {
                        Thread.sleep(WAITING_TIME_MS);
                        timeout = timeout + WAITING_TIME_MS;
                        if (timeout < readTimeout_millis)
                            continue;
                    }

                    ok_going = false;
                    appendByte(aByte);
                    endFrame = byteArrayList.get(byteArrayList.size() - 1);

                    if (startHeadCode <= 0 && frameHeadCodeList.size() > 0) {
                        starFrame0 = starFrame1;
                        starFrame1 = byteArrayList.get(byteArrayList.size() - 1);
                        startHeadCode = ByteUtils.DoubleBytesToInt(starFrame0, starFrame1);
                        startHeadCodeIndex = byteArrayList.size() - 2;
                    }

                    if (frameHeadCodeList.contains(startHeadCode)) {
                        if (startHeadCodeIndex > 0 && startHeadCodeIndex < byteArrayList.size() - 1) {
                            for (int i = 0; i < startHeadCodeIndex; i++)
                                byteArrayList.remove(i);
                            startHeadCodeIndex = 0;
                        }
                        if (startHeadCode == defaultStartCode0) {
                            if (byteArrayList.size() < 7)
                                continue;
                            frameLength = ByteUtils.DoubleBytesToInt(byteArrayList.get(6), byteArrayList.get(5));
                            if (byteArrayList.size() < 7 + frameLength + 2)
                                continue;
                            ok_going = true;
                        } else if (startHeadCode == defaultStartCode1 || startHeadCode == defaultStartCode2) {
                            if (byteArrayList.size() < 3)
                                continue;
                            frameLength = byteArrayList.get(3);
                            if (byteArrayList.size() < frameLength + 2)
                                continue;
                            ok_going = true;
                        }
                    }
                    else if (frameEndCodeList.contains(endFrame))
                    {
                        ok_going = true;
                    }
                    else if (timeout >= readTimeout_millis && frameHeadCodeList.contains(startHeadCode))
                    {
                        ok_going = true;
                    }
                    else if(frameHeadCodeList.size() == 0 && frameEndCodeList.size() == 0)
                    {
                        if(byteArrayList.size() >=10 || timeout >= readTimeout_millis)
                            ok_going = true;
                    }
                    else
                    {
                        startHeadCode = 0;
                    }

                    timeout = 0;
                    if (ok_going) //&& (byteArrayList.size() >= frame_size)
                    {
                        List<Byte> newByteListData = new ArrayList<>();
                        Collections.addAll(newByteListData, new Byte[byteArrayList.size()]);//复制容量
                        Collections.copy(newByteListData, byteArrayList);//复制内容
                        Byte[] byteData = newByteListData.toArray(new Byte[newByteListData.size()]);//列表转成数组类（引用）

                        //将类引用转换成字节数组
                        byte[] bytes = new byte[newByteListData.size()];
                        for (int i = 0; i < byteData.length; i++) {
                            bytes[i] = byteData[i];
                        }
                        //一帧数据接收完毕
                        reset_clear();
                        //分发一帧数据出去
                        //MMLog.log(TAG,"DEBUG READ UART:"+BufferToHexStr(bytes,f_separator));
                        dispatchCourier(new EventCourier(serialPort.getDevice().getAbsolutePath(), DataID.DEVICE_EVENT_UART_READ, bytes));
                    }
                } catch (Exception e) {
                    reset_clear();
                    //MMLog.log(TAG, e.toString());
                }
            }//while (serialPort.isDeviceReady())
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


    @Override
    public void closeDevice() {
        if (serialPort != null) {
            serialPort.tryClose();
            serialPort = null;
        }
    }
}
