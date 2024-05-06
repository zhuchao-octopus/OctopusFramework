package com.zhuchao.android.fbase.eventinterface;

public interface EventCourierInterface {
    abstract String getTarget();

    abstract void setTarget(String target);

    abstract int getId();

    abstract void setId(int id);

    abstract void setDatas(byte[] datas);

    abstract byte[] getDatas();

    abstract Object getObj();

    abstract void setObj(Object obj);

    abstract Class<?>  getFromClass();

    abstract void setFromClass(Class<?>  fromClass);

    abstract byte getByte();

    abstract int getValue();

    abstract String toStr();

}
