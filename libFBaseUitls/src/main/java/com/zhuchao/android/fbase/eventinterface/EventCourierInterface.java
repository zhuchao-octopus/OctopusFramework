package com.zhuchao.android.fbase.eventinterface;

import android.content.Context;

import com.zhuchao.android.fbase.EventCourier;

public interface EventCourierInterface {
    abstract String getTarget();

    abstract void setTarget(String target);

    abstract int getId();

    abstract void setId(int id);

    abstract void setDatas(byte[] datas);

    abstract byte[] getDatas();

    abstract Object getObj();

    abstract void setObj(Object obj);

    abstract String getFromClass();

    //abstract EventCourier with(Context context);

    abstract EventCourier f(String fromClass);

    abstract byte getByte();

    abstract int getValue();

    abstract String toStr();

}
