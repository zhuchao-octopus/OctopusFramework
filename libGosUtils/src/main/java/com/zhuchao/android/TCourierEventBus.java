package com.zhuchao.android;

import com.zhuchao.android.callbackevent.EventCourier;
import com.zhuchao.android.callbackevent.CourierEventListener;
import com.zhuchao.android.callbackevent.InvokeInterface;
import com.zhuchao.android.libfileutils.MMLog;
import com.zhuchao.android.libfileutils.ObjectList;
import com.zhuchao.android.libfileutils.TTask;

import java.util.ArrayList;

public class TCourierEventBus implements InvokeInterface {
    private final String TAG = "TCourierEventBus";
    private ObjectList InvokerList = null;
    private ArrayList<EventCourier> couriers = null;
    private TTask tTask = null;
    private InvokeInterface invokeInterface = null;
    private boolean keepDoing = true;

    public TCourierEventBus() {
        InvokerList = new ObjectList();
        couriers = new ArrayList();
        invokeInterface = this;
        keepDoing = true;
        tTask = new TTask(TAG, invokeInterface);
    }

    public void registerEventObserver(String tag, CourierEventListener courierEventListener) {
        InvokerList.addItem(tag, courierEventListener);
    }

    @Override
    public void CALLTODO(String tag) {
        boolean ret = false;
        while (keepDoing) {
            for (EventCourier eventCourier : couriers)
            {
                CourierEventListener courierEventListener = getCourierEventListener(eventCourier);
                if (courierEventListener != null)
                {
                    ret = courierEventListener.onCourierEvent(eventCourier);
                    if (ret) {
                        couriers.remove(eventCourier);
                        //MMLog.log(TAG, "call event handler success data:" + eventCourier.ToHexStr());
                    }
                }
                else
                {
                    MMLog.log(TAG,"can not find EventObserver courier tag = "+ eventCourier.getTag());
                    couriers.remove(eventCourier);//丢弃
                }
                ret = false;
            }
        }
    }

    public void post(EventCourier eventCourier)
    {
        try {
            couriers.add(eventCourier);
            if(tTask != null && couriers.size() > 0 && !tTask.isAlive()) {
                keepDoing = true;
                tTask.start();
                MMLog.log(TAG,"CourierEventBus start...");
            }
        } catch (Exception e) {
            MMLog.log(TAG, "post event failed," + e.toString());
        }
    }
    private CourierEventListener getCourierEventListener(EventCourier eventCourier) {
        CourierEventListener courierEventListener = (CourierEventListener) InvokerList.getObject(eventCourier.getTag());
        return courierEventListener;
    }

    public void free() {
        keepDoing = false;
        tTask.free();
    }
}
