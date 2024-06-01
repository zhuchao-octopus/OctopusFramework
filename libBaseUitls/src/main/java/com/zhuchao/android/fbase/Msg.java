package com.zhuchao.android.fbase;

public class Msg extends EventCourier {
    public Msg(String target, int id) {
        super(target, id);
    }

    public Msg(String target, int id, byte value) {
        super(target, id, value);
    }

    public Msg(String target, int id, int value) {
        super(target, id, value);
    }

    public Msg(String target, int id, byte[] datas) {
        super(target, id, datas);
    }

    public Msg(String target, int id, Object obj) {
        super(target, id, obj);
    }

    public Msg(String target, int id, byte[] datas, Object obj) {
        super(target, id, datas, obj);
    }

    public Msg(int id) {
        super(id);
    }

    public Msg(int id, byte value) {
        super(id, value);
    }

    public Msg(int id, int value) {
        super(id, value);
    }

    public Msg(int id, byte[] datas) {
        super(id, datas);
    }

    public Msg(int id, Object obj) {
        super(id, obj);
    }

    public Msg(int id, byte[] datas, Object obj) {
        super(id, datas, obj);
    }

    public Msg(Object obj) {
        super(obj);
    }

    @Override
    public String toStr() {
        return super.toString();
    }
}
