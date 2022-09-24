package com.zhuchao.android.session;

import java.io.Serializable;

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

    protected void doAction() {

    }
}
