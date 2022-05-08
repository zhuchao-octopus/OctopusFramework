package com.zhuchao.android;

import com.zhuchao.android.callbackevent.CourierEventListener;
import com.zhuchao.android.callbackevent.EventCourier;
import com.zhuchao.android.callbackevent.InvokeInterface;
import com.zhuchao.android.libfileutils.MMLog;
import com.zhuchao.android.libfileutils.ObjectList;
import com.zhuchao.android.libfileutils.TTask;

import java.util.ArrayList;

public class TCourierEventBus implements InvokeInterface {
    private final String TAG = "TCourierEventBus";
    private ObjectList InvokerList = null;
    private ArrayList<EventCourier> couriers_A = null;
    private ArrayList<EventCourier> couriers_B = null;
    private TTask tTask = null;
    private InvokeInterface invokeInterface = null;
    private boolean keepDoing = true;
    private boolean busy_A = false;

    public TCourierEventBus() {
        InvokerList = new ObjectList();
        couriers_A = new ArrayList();
        couriers_B = new ArrayList();
        invokeInterface = this;
        keepDoing = true;
        tTask = new TTask(TAG, invokeInterface);
    }

    public void registerEventObserver(String tag, CourierEventListener courierEventListener) {
        InvokerList.addItem(tag, courierEventListener);
    }

    public void post(EventCourier eventCourier) {
        if (couriers_A == null || couriers_B == null)
            return;
        try {
            if (busy_A)
                couriers_B.add(eventCourier);
            else
                couriers_A.add(eventCourier);

            if (tTask != null && couriers_A.size() > 0 && !tTask.isAlive()) {
                keepDoing = true;
                tTask.start();
                MMLog.log(TAG, "CourierEventBus start...");
            }
        } catch (Exception e) {
            MMLog.log(TAG, "post event failed," + e.toString());
        }
    }

    @Override
    public void CALLTODO(String tag) {
        //boolean ret = false;
        while (keepDoing) {
            if (couriers_A != null && couriers_B != null) {
                busy_A = true;
                poolingAB(couriers_A);
                busy_A = false;
                poolingAB(couriers_B);
            }
        }
    }

    private void poolingAB(ArrayList<EventCourier> couriers) {
        try {
            for (int i = 0; i < couriers.size(); i++) {
                EventCourier eventCourier = couriers.get(i);
                if (eventCourier == null) {
                    continue; //couriers.remove(i);//丢弃
                }
                CourierEventListener courierEventListener = getCourierEventListener(eventCourier);
                if (courierEventListener != null) {
                    courierEventListener.onCourierEvent(eventCourier);
                }
            }
            couriers.clear();
        } catch (Exception e) {
            //couriers.clear();
            MMLog.e(TAG, "poolingAB FAILED " + e.toString());
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
