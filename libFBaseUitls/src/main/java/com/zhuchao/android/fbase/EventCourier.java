package com.zhuchao.android.fbase;

public class EventCourier {
    private final String TAG = "EventCourier";
    private String tag;
    private int id;
    private byte[] datas;
    private Object obj;

    public EventCourier(String tag, int id) {
        this.tag = tag;
        this.id = id;
        this.datas = new byte[1];
        this.obj = null;
    }

    public EventCourier(String tag, int id, byte value) {
        this.tag = tag;
        this.id = id;
        this.datas = new byte[1];
        this.datas[0] = value;
        this.obj = null;
    }

    public EventCourier(String tag, int id, int value) {
        this.tag = tag;
        this.id = id;
        this.datas = ByteUtils.intToBytes(value);
        this.obj = null;
    }

    public EventCourier(String tag, int id, byte[] datas) {
        this.tag = tag;
        this.id = id;
        this.datas = datas;
        this.obj = null;
    }

    public EventCourier(String tag, int id, Object obj) {
        this.tag = tag;
        this.id = id;
        this.datas = new byte[1];
        this.obj = obj;
    }

    public EventCourier(String tag, int id, byte[] datas, Object obj) {
        this.tag = tag;
        this.id = id;
        this.datas = datas;
        this.obj = obj;
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

    public byte getByte() {
        if (datas.length > 0)
            return datas[0];
        else
            return 0;
    }

    public int getValue() {
        int ret = 0;
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

    //字节数组转转hex字符串
    public String dataToHexStr() {
        StringBuilder strBuilder = new StringBuilder();
        for (byte valueOf : datas) {
            //strBuilder.append(toHexStr(Byte.valueOf(valueOf)));
            strBuilder.append(toHexStr(valueOf));
            strBuilder.append(" ");
        }
        return strBuilder.toString();
    }

    //1字节转2个Hex字符
    private String toHexStr(Byte inByte) {
        return String.format("%02x", inByte).toUpperCase();
    }
}
