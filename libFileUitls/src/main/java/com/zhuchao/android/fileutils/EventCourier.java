package com.zhuchao.android.callbackevent;

public class EventCourier {
    private String tag;
    private int id;
    private byte[] datas;
    private Object obj;

    public EventCourier(String tag, int id) {
        this.tag = tag;
        this.id = id;
        this.datas = null;
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

    public void setIntValue(int value)
    {
        datas = new
    }
    //字节数组转转hex字符串
    public String dataToHexStr() {
        StringBuilder strBuilder = new StringBuilder();
        for (byte valueOf : datas) {
            strBuilder.append(toHexStr(Byte.valueOf(valueOf)));
            strBuilder.append(" ");
        }
        return strBuilder.toString();
    }

    //1字节转2个Hex字符
    private String toHexStr(Byte inByte) {
        return String.format("%02x", inByte).toUpperCase();
    }
}
