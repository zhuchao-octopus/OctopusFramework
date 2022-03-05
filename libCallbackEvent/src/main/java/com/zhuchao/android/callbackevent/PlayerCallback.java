package com.zhuchao.android.callbackevent;

public interface PlayerCallback {
    void OnEventCallBack(int EventType, long TimeChanged, long LengthChanged, float PositionChanged,
                                 int OutCount, int ChangedType, int ChangedID, float Buffering, long Length);
}

