package com.zhuchao.android.fbase.eventinterface;

import com.zhuchao.android.fbase.EventCourier;

public interface EventCourierInterface {
    abstract String getTag();

    abstract void setTag(String tag);

    abstract int getId();

    abstract void setId(int id);

    abstract void setDatas(byte[] datas);

    abstract byte[] getDatas();

    abstract Object getObj();

    abstract void setObj(Object obj);

    abstract String getFromClass();

    abstract void setFromClass(String fromClass);

    abstract EventCourier fromClass(String fromClass);

    abstract EventCourier f(String fromClass);

    abstract EventCourier f(Class classzz);

    abstract byte getByte();

    abstract int getValue();

    abstract String toString();
}
