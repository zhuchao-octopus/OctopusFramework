package com.zhuchao.android.callbackevent;


public abstract interface PlayerCallback {
    public abstract void OnEventCallBack(int EventType,long TimeChanged,long LengthChanged,float PositionChanged,
                                         int outCount,int ChangedType,int ChangedID,float Buffering,long Length);



/*    public long getTimeChanged() {
        return this.arg1;
    }

    public long getLengthChanged() {
        return this.arg1;
    }

    public float getPositionChanged() {
        return this.argf1;
    }

    public int getVoutCount() {
        return (int)this.arg1;
    }

    public int getEsChangedType() {
        return (int)this.arg1;
    }

    public int getEsChangedID() {
        return (int)this.arg2;
    }

    public boolean getPausable() {
        return this.arg1 != 0L;
    }

    public boolean getSeekable() {
        return this.arg1 != 0L;
    }

    public float getBuffering() {
        return this.argf1;
    }*/
}

