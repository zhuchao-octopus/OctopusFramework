package com.zhuchao.android.fbase;

public class EC extends EventCourier{
    public EC(String tag, int id) {
        super(tag, id);
    }

    public EC(String tag, int id, byte value) {
        super(tag, id, value);
    }

    public EC(String tag, int id, int value) {
        super(tag, id, value);
    }

    public EC(String tag, int id, byte[] datas) {
        super(tag, id, datas);
    }

    public EC(String tag, int id, Object obj) {
        super(tag, id, obj);
    }

    public EC(String tag, int id, byte[] datas, Object obj) {
        super(tag, id, datas, obj);
    }

    public EC(int id) {
        super(id);
    }

    public EC(int id, byte value) {
        super(id, value);
    }

    public EC(int id, int value) {
        super(id, value);
    }

    public EC(int id, byte[] datas) {
        super(id, datas);
    }

    public EC(int id, Object obj) {
        super(id, obj);
    }

    public EC(int id, byte[] datas, Object obj) {
        super(id, datas, obj);
    }

    public EC(Object obj) {
        super(obj);
    }
}
