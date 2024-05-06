package com.zhuchao.android.fbase;


import com.zhuchao.android.fbase.eventinterface.EventCourierInterface;

import java.util.Arrays;

public class EventCourier implements EventCourierInterface {
    private Class<?> from;
    ///private String from;
    private String target;
    private int id;
    private byte[] datas;
    private Object obj = null;


    public EventCourier(int id) {
        this.target = null;
        this.id = id;
        this.datas = new byte[1];
        this.obj = null;
        //this.fromClass = getCallerClass();
    }

    public EventCourier(Object obj) {
        this.target = null;
        this.id = -1;
        this.datas = new byte[1];
        this.obj = obj;
        //this.fromClass = getCallerClass();
    }

    public EventCourier(Class<?> fromClass, int id) {
        this.target = null;
        this.id = id;
        this.datas = new byte[1];
        this.obj = null;
        this.from = fromClass;
    }

    public EventCourier(String target, int id) {
        this.target = target;
        this.id = id;
        this.datas = new byte[1];
        this.obj = null;
        //this.fromClass = getCallerClass();
    }

    public EventCourier(int id, byte value) {
        this.target = null;
        this.id = id;
        this.datas = new byte[1];
        this.datas[0] = value;
        this.obj = null;
        //this.fromClass = getCallerClass();
    }

    public EventCourier(int id, int value) {
        this.target = null;
        this.id = id;
        this.datas = ByteUtils.intToBytes(value);
        this.obj = null;
        //this.fromClass = getCallerClass();
    }

    public EventCourier(int id, byte[] datas) {
        this.target = null;
        this.id = id;
        this.datas = datas;
        this.obj = null;
        //this.fromClass = getCallerClass();
    }

    public EventCourier(int id, Object obj) {
        this.target = null;
        this.id = id;
        this.datas = new byte[1];
        this.obj = obj;
        //this.fromClass = getCallerClass();
    }

    public EventCourier(Class<?> fromClass, String target, int id) {
        this.target = target;
        this.id = id;
        this.datas = new byte[1];
        this.obj = null;
        this.from = fromClass;
    }

    public EventCourier(String target, int id, boolean value) {
        this.target = target;
        this.id = id;
        this.datas = new byte[1];
        this.datas[0] = (byte) (value ? 1 : 0);
        this.obj = null;
        //this.fromClass = getCallerClass();
    }

    public EventCourier(String target, int id, byte value) {
        this.target = target;
        this.id = id;
        this.datas = new byte[1];
        this.datas[0] = value;
        this.obj = null;
        //this.fromClass = getCallerClass();
    }

    public EventCourier(String target, int id, int value) {
        this.target = target;
        this.id = id;
        this.datas = ByteUtils.intToBytes(value);
        this.obj = null;
        //this.fromClass = getCallerClass();
    }

    public EventCourier(String target, int id, byte[] datas) {
        this.target = target;
        this.id = id;
        this.datas = datas;
        this.obj = null;
        //this.fromClass = getCallerClass();
    }

    public EventCourier(String target, int id, Object obj) {
        this.target = target;
        this.id = id;
        this.datas = new byte[1];
        this.obj = obj;
        //this.fromClass = getCallerClass();
    }

    public EventCourier(int id, byte[] datas, Object obj) {
        this.target = null;
        this.id = id;
        this.datas = datas;
        this.obj = obj;
        //this.fromClass = getCallerClass();
    }

    public EventCourier(String target, int id, byte[] datas, Object obj) {
        this.target = target;
        this.id = id;
        this.datas = datas;
        this.obj = obj;
        //this.fromClass = getCallerClass();
    }

    public EventCourier(Class<?> fromClass, String target, int id, boolean value) {
        this.target = target;
        this.id = id;
        this.datas = new byte[1];
        this.datas[0] = (byte) (value ? 1 : 0);
        this.obj = null;
        this.from = fromClass;
    }

    public EventCourier(Class<?> fromClass, String target, int id, byte value) {
        this.target = target;
        this.id = id;
        this.datas = new byte[1];
        this.datas[0] = value;
        this.obj = null;
        this.from = fromClass;
    }

    public EventCourier(Class<?> fromClass, String target, int id, int value) {
        this.target = target;
        this.id = id;
        this.datas = ByteUtils.intToBytes(value);
        this.obj = null;
        this.from = fromClass;
    }

    public EventCourier(Class<?> fromClass, String target, int id, byte[] datas) {
        this.target = target;
        this.id = id;
        this.datas = datas;
        this.obj = null;
        this.from = fromClass;
    }

    public EventCourier(Class<?> fromClass, String target, int id, Object obj) {
        this.target = target;
        this.id = id;
        this.datas = new byte[1];
        this.obj = obj;
        this.from = fromClass;
    }

    public EventCourier(Class<?> fromClass, String target, int id, byte[] datas, Object obj) {
        this.target = target;
        this.id = id;
        this.datas = datas;
        this.obj = obj;
        this.from = fromClass;
    }

    public String getTarget() {
        return target;
    }

    public void setTarget(String target) {
        this.target = target;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public void setDatas(byte[] datas) {
        this.datas = datas;
    }

    public byte[] getDatas() {
        return datas;
    }

    public Object getObj() {
        return obj;
    }

    public void setObj(Object obj) {
        this.obj = obj;
    }

    @Override
    public Class<?> getFromClass() {
        return from;
    }

    public void setFromClass(Class<?> fromClass) {
        this.from = fromClass;
    }

    public byte getByte() {
        if (datas.length > 0) return datas[0];
        else return 0;
    }

    public int getValue() {
        int ret = 0;
        String TAG = "EventCourier";
        switch (datas.length) {
            case 1:
                ret = datas[0];
                //MMLog.log(TAG, "datas.length = 1,value = " + ret);
                break;
            case 2:
                ret = ByteUtils.DoubleBytesToInt(datas[0], datas[1]);
                //MMLog.log(TAG, "datas.length = 2,value = " + ret);
                break;
            case 3:
                ret = ByteUtils.ThreeBytesToInt(datas[0], datas[1], datas[2]);
                //MMLog.log(TAG, "datas.length = 3,value = " + ret);
                break;
            case 4:
                ret = ByteUtils.FourBytesToInt(datas[0], datas[1], datas[2], datas[3]);
                //MMLog.log(TAG, "datas.length = 4,value = " + ret);
                break;
            default:
                MMLog.d(TAG, "do not support more than 4 bytes convert!!!");
                ret = -1;
                break;
        }
        return ret;
    }

    private String getCallerClass() {
        try {
            return Thread.currentThread().getStackTrace()[1].getClass().getSimpleName();
        } catch (Exception ignored) {
        }
        return null;
    }

    //字节数组转转hex字符串
    @Override
    public String toStr() {
        if (from != null)
            return "EventCourier{" + "target='" + target + '\'' + ",id=" + id + ",datas=" + Arrays.toString(datas) + ",obj=" + obj + ",fromClass='" + String.valueOf(from.getName()) + '\'' + '}';
        else
            return "EventCourier{" + "target='" + target + '\'' + ",id=" + id + ",datas=" + Arrays.toString(datas) + ",obj=" + obj + ",fromClass='" + '\'' + '}';
    }

    //1字节转2个Hex字符
    private String toHexStr(Byte inByte) {
        return String.format("%02x", inByte).toUpperCase();
    }

    private String dataToHexStr() {
        StringBuilder strBuilder = new StringBuilder();
        for (byte valueOf : datas) {
            //strBuilder.append(toHexStr(Byte.valueOf(valueOf)));
            strBuilder.append(toHexStr(valueOf));
            strBuilder.append(" ");
        }
        return strBuilder.toString();
    }

    ///////////////////////////////////////////////////////////////////////////////////////////////////////
    //

}
