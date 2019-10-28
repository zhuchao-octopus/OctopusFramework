package com.zhuchao.android.playsession;

import java.io.Serializable;


//{"sourceId":"1000","vedioID":"7895"，"list":[]}

class Session implements Serializable {

    protected int mSessionId;

    public Session(int mSessionId) {
        this.mSessionId = mSessionId;
    }

    public int getmSessionId() {
        return mSessionId;
    }

    public void setmSessionId(int mSessionId) {
        this.mSessionId = mSessionId;
    }

    protected void doStart()
    {

    }
}
