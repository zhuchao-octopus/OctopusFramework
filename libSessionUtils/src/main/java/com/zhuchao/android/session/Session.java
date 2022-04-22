package com.zhuchao.android.session;

import java.io.Serializable;


//{"sourceId":"1000","vedioID":"7895"，"list":[]}

class Session implements Serializable {

    protected int SId;

    public Session(int sId) {
        this.SId = sId;
    }

    public int getSId() {
        return SId;
    }

    public void setSId(int SId) {
        this.SId = SId;
    }

    protected void doStart()
    {

    }
}
