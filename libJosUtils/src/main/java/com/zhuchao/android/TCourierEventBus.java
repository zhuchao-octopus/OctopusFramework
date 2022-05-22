package com.zhuchao.android;

import com.zhuchao.android.callbackevent.CourierEventListener;
import com.zhuchao.android.callbackevent.EventCourier;
import com.zhuchao.android.callbackevent.InvokeInterface;
import com.zhuchao.android.libfileutils.MMLog;
import com.zhuchao.android.libfileutils.ObjectList;
import com.zhuchao.android.libfileutils.TTask;
import com.zhuchao.android.libfileutils.ThreadUtils;

import java.util.ArrayList;
import java.util.concurrent.locks.LockSupport;

public class TCourierEventBus implements InvokeInterface {
    private final String TAG = "TCourierEventBus";
    private ObjectList InvokerList = null;
    private ArrayList<EventCourier> couriers_A = null;
    private ArrayList<EventCourier> couriers_B = null;
    private ArrayList<EventCourier> couriers_MA = null;
    private ArrayList<EventCourier> couriers_MB = null;
    private TTask tTask = null;
    private InvokeInterface invokeInterface = null;
    private boolean keepDoing = true;
    private boolean busy_A = false;
    private boolean busy_M = false;

    public TCourierEventBus() {
        InvokerList = new ObjectList();
        couriers_A = new ArrayList<EventCourier>();
        couriers_B = new ArrayList<EventCourier>();
        couriers_MA = new ArrayList<EventCourier>();
        couriers_MB = new ArrayList<EventCourier>();
        invokeInterface = this;
        keepDoing = true;
        tTask = new TTask(TAG, invokeInterface);
    }

    public void registerEventObserver(String tag, CourierEventListener courierEventListener) {
        InvokerList.addItem(tag, courierEventListener);
    }

    public void postMainThread(EventCourier eventCourier) {
        if (couriers_MA == null || couriers_MB == null)
            return;
        try {
            if (busy_M)
                couriers_MB.add(eventCourier);
            else
                couriers_MA.add(eventCourier);

            if (tTask != null && couriers_MA.size() > 0 && !tTask.isAlive()) {
                keepDoing = true;
                tTask.start();
                MMLog.log(TAG, "CourierEventBus start...");
            }
            if (tTask != null)
                //tTask.notifyAll();
                LockSupport.unpark(tTask);
        } catch (Exception e) {
            MMLog.log(TAG, "postMain event failed," + e.toString());
        }
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
            if (tTask != null)
                //tTask.notifyAll();
                LockSupport.unpark(tTask);
        } catch (Exception e) {
            MMLog.log(TAG, "post event failed," + e.toString());
        }
    }

    @Override
    public void CALLTODO(String tag) {
        //MMLog.log(TAG,"CALLTODO "+ keepDoing);
        while (keepDoing)
        {
            if (couriers_A != null && couriers_B != null)
            {
                busy_A = true;
                if (couriers_A.size() > 0) {
                    poolingAB(couriers_A);
                    couriers_A.clear();
                }
                busy_A = false;
                if (couriers_B.size() > 0) {
                    poolingAB(couriers_B);
                    couriers_B.clear();
                }
            }

            if (couriers_MA != null && couriers_MB != null)
            {
                busy_M = true;
                if (couriers_MA.size() > 0) {
                    poolingABM(couriers_MA);
                    couriers_MA.clear();
                }
                busy_M = false;
                if (couriers_MB.size() > 0) {
                    poolingABM(couriers_MB);
                    couriers_MB.clear();
                }
            }

            //MMLog.log(TAG,"A:"+couriers_A.size()+",B:"+couriers_B.size()+"AM:"+couriers_MA.size()+"BM:"+couriers_MB.size());
            if ((couriers_A.size() <= 0) && (couriers_B.size() <= 0) && (couriers_MA.size() <= 0) && (couriers_MB.size() <= 0))
            {
                try
                {
                    //MMLog.log(TAG,"suspend!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!1");
                    //tTask.wait();
                    LockSupport.park();
                    //MMLog.log(TAG,"suspend!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!2");
                }
                catch (Exception e)
                {
                    //e.printStackTrace();
                    MMLog.log(TAG,e.toString());
                }
            }

        }//while (keepDoing)
    }

    private void poolingAB(ArrayList<EventCourier> couriers) {
        try {
            for (int i = 0; i < couriers.size(); i++)
            {
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

    private void poolingABM(ArrayList<EventCourier> couriers) {
        try {
            for (int i = 0; i < couriers.size(); i++)
            {
                EventCourier eventCourier = couriers.get(i);
                if (eventCourier == null) {
                    continue; //couriers.remove(i);//丢弃
                }
                CourierEventListener courierEventListener = getCourierEventListener(eventCourier);
                if (courierEventListener != null) {
                    ThreadUtils.runOnMainUiThread(new Runnable() {
                        @Override
                        public void run() {
                            courierEventListener.onCourierEvent(eventCourier);
                        }
                    });
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
        tTask.freeFree();
    }
}
