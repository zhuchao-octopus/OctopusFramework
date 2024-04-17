package com.zhuchao.android.fbase;


import android.content.Context;

import com.zhuchao.android.fbase.eventinterface.EventCourierInterface;

import java.util.Arrays;

public class EventCourier implements EventCourierInterface {
    private String tag;
    private int id;
    private byte[] datas;
    private Object obj;
    private Context from;

    public EventCourier(String tag, int id) {
        this.tag = tag;
        this.id = id;
        this.datas = new byte[1];
        this.obj = null;
        //this.fromClass = getCallerClass();
    }

    public EventCourier(Context context, String tag, int id) {
        this.tag = tag;
        this.id = id;
        this.datas = new byte[1];
        this.obj = null;
        this.from = context;
    }

    public EventCourier(String tag, int id, boolean value) {
        this.tag = tag;
        this.id = id;
        this.datas = new byte[1];
        this.datas[0] = (byte) (value ? 1 : 0);
        this.obj = null;
        //this.fromClass = getCallerClass();
    }

    public EventCourier(Context context, String tag, int id, boolean value) {
        this.tag = tag;
        this.id = id;
        this.datas = new byte[1];
        this.datas[0] = (byte) (value ? 1 : 0);
        this.obj = null;
        this.from = context;
    }

    public EventCourier(String tag, int id, byte value) {
        this.tag = tag;
        this.id = id;
        this.datas = new byte[1];
        this.datas[0] = value;
        this.obj = null;
        //this.fromClass = getCallerClass();
    }

    public EventCourier(Context context, String tag, int id, byte value) {
        this.tag = tag;
        this.id = id;
        this.datas = new byte[1];
        this.datas[0] = value;
        this.obj = null;
        this.from = context;
    }

    public EventCourier(String tag, int id, int value) {
        this.tag = tag;
        this.id = id;
        this.datas = ByteUtils.intToBytes(value);
        this.obj = null;
        //this.fromClass = getCallerClass();
    }

    public EventCourier(Context context, String tag, int id, int value) {
        this.tag = tag;
        this.id = id;
        this.datas = ByteUtils.intToBytes(value);
        this.obj = null;
        this.from = context;
    }

    public EventCourier(String tag, int id, byte[] datas) {
        this.tag = tag;
        this.id = id;
        this.datas = datas;
        this.obj = null;
        //this.fromClass = getCallerClass();
    }

    public EventCourier(Context context, String tag, int id, byte[] datas) {
        this.tag = tag;
        this.id = id;
        this.datas = datas;
        this.obj = null;
        this.from = context;
    }

    public EventCourier(String tag, int id, Object obj) {
        this.tag = tag;
        this.id = id;
        this.datas = new byte[1];
        this.obj = obj;
        //this.fromClass = getCallerClass();
    }

    public EventCourier(Context context, String tag, int id, Object obj) {
        this.tag = tag;
        this.id = id;
        this.datas = new byte[1];
        this.obj = obj;
        this.from = context;
    }

    public EventCourier(String tag, int id, byte[] datas, Object obj) {
        this.tag = tag;
        this.id = id;
        this.datas = datas;
        this.obj = obj;
        //this.fromClass = getCallerClass();
    }

    public EventCourier(Context context, String tag, int id, byte[] datas, Object obj) {
        this.tag = tag;
        this.id = id;
        this.datas = datas;
        this.obj = obj;
        this.from = context;
    }

    public EventCourier(int id) {
        this.tag = null;
        this.id = id;
        this.datas = new byte[1];
        this.obj = null;
        //this.fromClass = getCallerClass();
    }

    public EventCourier(Context context, int id) {
        this.tag = null;
        this.id = id;
        this.datas = new byte[1];
        this.obj = null;
        this.from = context;
    }

    public EventCourier(int id, byte value) {
        this.tag = null;
        this.id = id;
        this.datas = new byte[1];
        this.datas[0] = value;
        this.obj = null;
        //this.fromClass = getCallerClass();
    }

    public EventCourier(Context context, int id, byte value) {
        this.tag = null;
        this.id = id;
        this.datas = new byte[1];
        this.datas[0] = value;
        this.obj = null;
        this.from = context;
    }

    public EventCourier(int id, int value) {
        this.tag = null;
        this.id = id;
        this.datas = ByteUtils.intToBytes(value);
        this.obj = null;
        //this.fromClass = getCallerClass();
    }

    public EventCourier(Context context, int id, int value) {
        this.tag = null;
        this.id = id;
        this.datas = ByteUtils.intToBytes(value);
        this.obj = null;
        this.from = context;
    }

    public EventCourier(int id, byte[] datas) {
        this.tag = null;
        this.id = id;
        this.datas = datas;
        this.obj = null;
        //this.fromClass = getCallerClass();
    }

    public EventCourier(Context context, int id, byte[] datas) {
        this.tag = null;
        this.id = id;
        this.datas = datas;
        this.obj = null;
        this.from = context;
    }

    public EventCourier(int id, Object obj) {
        this.tag = null;
        this.id = id;
        this.datas = new byte[1];
        this.obj = obj;
        //this.fromClass = getCallerClass();
    }

    public EventCourier(Context context, int id, Object obj) {
        this.tag = null;
        this.id = id;
        this.datas = new byte[1];
        this.obj = obj;
        this.from = context;
    }

    public EventCourier(int id, byte[] datas, Object obj) {
        this.tag = null;
        this.id = id;
        this.datas = datas;
        this.obj = obj;
        //this.fromClass = getCallerClass();
    }

    public EventCourier(Context context, int id, byte[] datas, Object obj) {
        this.tag = null;
        this.id = id;
        this.datas = datas;
        this.obj = obj;
        this.from = context;
    }

    public EventCourier(Object obj) {
        this.tag = null;
        this.id = -1;
        this.datas = new byte[1];
        this.obj = obj;
        //this.fromClass = getCallerClass();
    }

    public EventCourier(Context context, Object obj) {
        this.tag = null;
        this.id = -1;
        this.datas = new byte[1];
        this.obj = obj;
        this.from = context;
    }

    public String getTag() {
        return tag;
    }

    public void setTag(String tag) {
        this.tag = tag;
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
    public String getFromClass() {
        if (from != null) return from.toString();
        else return "";
    }

    public Context getFrom() {
        return from;
    }

    public void setFromClass(Context context) {
        this.from = context;
    }

    public EventCourier with(Context context) {
        this.from = context;
        return this;
    }

    public EventCourier f(Context context) {
        this.from = context;
        return this;
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
    private String dataToHexStr() {
        StringBuilder strBuilder = new StringBuilder();
        for (byte valueOf : datas) {
            //strBuilder.append(toHexStr(Byte.valueOf(valueOf)));
            strBuilder.append(toHexStr(valueOf));
            strBuilder.append(" ");
        }
        return strBuilder.toString();
    }

    @Override
    public String toString() {
        if (from != null)
            return "EventCourier{" + "tag='" + tag + '\'' + ", id=" + id + ", datas=" + Arrays.toString(datas) + ", obj=" + obj + ", fromClass='" + from.getClass() + '\'' + '}';
        else
            return "EventCourier{" + "tag='" + tag + '\'' + ", id=" + id + ", datas=" + Arrays.toString(datas) + ", obj=" + obj + ", fromClass='" + '\'' + '}';
    }

    //1字节转2个Hex字符
    private String toHexStr(Byte inByte) {
        return String.format("%02x", inByte).toUpperCase();
    }
}
