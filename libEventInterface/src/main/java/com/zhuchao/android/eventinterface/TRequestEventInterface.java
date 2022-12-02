package com.zhuchao.android.eventinterface;

public abstract interface TRequestEventInterface {
    public abstract String getRequestParameter();

    public abstract void setRequestParameter(String requestParameter);

    public abstract String getRequestMethod();

    public abstract void setRequestMethod(String requestMethod);

    public abstract String getRequestURL();

    public abstract void setRequestURL(String requestURL);
}
