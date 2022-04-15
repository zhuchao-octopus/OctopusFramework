package com.zhuchao.android.libfileutils;

import com.zhuchao.android.callbackevent.CallbackFunction;

public class TTask extends Thread {
    private String tag = null;
    private CallbackFunction callbackFunction = null;
    private ThreadPool threadPool;

    public TTask(String tag, CallbackFunction callbackFunction, ThreadPool threadPool) {
        this.tag = tag;
        this.callbackFunction = callbackFunction;
        this.threadPool = threadPool;
    }

    @Override
    public void run() {
        super.run();
        if (callbackFunction != null)
        {
            if(threadPool != null)
            {
                if(!threadPool.exist(this.tag))
                    callbackFunction.call();
            }
            else
            {
                callbackFunction.call();
            }
        }
        if(threadPool != null) {
            threadPool.delete(tag);
        }
    }

    public void call(CallbackFunction callFunction) {
        callbackFunction = callFunction;
        tag = this.getName();
    }

    public String getTag() {
        return tag;
    }

    public void setTag(String tag) {
        this.tag = tag;
    }

    public int getID()
    {
        return this.getID();
    }
}