package com.zhuchao.android.libfileutils;

public class EventObject {
    private int id;
    private byte[] datas;

    public EventObject(int id, byte[] datas) {
        this.id = id;
        this.datas = datas;
    }

    public int getId() {
        return id;
    }

    public byte[] getDatas() {
        return datas;
    }

    //字节数组转转hex字符串
    public  String ToHexStr(byte[] bytes,String separatorChars) {
        StringBuilder strBuilder = new StringBuilder();
        for (byte valueOf : bytes) {
            strBuilder.append(toHexStr(Byte.valueOf(valueOf)));
            strBuilder.append(separatorChars);
        }
        return strBuilder.toString();
    }
    //1字节转2个Hex字符
    private String toHexStr(Byte inByte) {
        return String.format("%02x", inByte).toUpperCase();
    }
}
