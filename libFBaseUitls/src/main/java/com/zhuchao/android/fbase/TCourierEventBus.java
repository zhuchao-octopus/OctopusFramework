package com.zhuchao.android.fbase;

import com.zhuchao.android.eventinterface.InvokeInterface;

import java.util.ArrayList;
import java.util.concurrent.locks.LockSupport;

public class TCourierEventBus implements InvokeInterface {
    private final String TAG = "TCourierEventBus";
    private ObjectList InvokerList = null;
    private ArrayList<EventCourier> CourierEvents_A = null;
    private ArrayList<EventCourier> CourierEvents_B = null;
    private ArrayList<EventCourier> CourierEvents_MainA = null;
    private ArrayList<EventCourier> CourierEvents_MainB = null;
    private TTask tTask = null;
    private InvokeInterface invokeInterface = null;
    private boolean keepDoing = true;
    private boolean couriersLock_A = false;
    private boolean couriersLock_M = false;

    public TCourierEventBus() {
        InvokerList = new ObjectList();
        CourierEvents_A = new ArrayList<EventCourier>();
        CourierEvents_B = new ArrayList<EventCourier>();
        CourierEvents_MainA = new ArrayList<EventCourier>();
        CourierEvents_MainB = new ArrayList<EventCourier>();
        invokeInterface = this;
        keepDoing = true;
        tTask = new TTask(TAG, invokeInterface);
    }

    public void registerEventObserver(String tag, TCourierEventListener courierEventListener) {
        InvokerList.addItem(tag, courierEventListener);
    }

    public void postMain(EventCourier eventCourier) {
        if (CourierEvents_MainA == null || CourierEvents_MainB == null)
            return;
        try {
            if (couriersLock_M)
                CourierEvents_MainB.add(eventCourier);
            else
                CourierEvents_MainA.add(eventCourier);

            if (tTask != null && CourierEvents_MainA.size() > 0 && !tTask.isAlive()) {
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
        if (CourierEvents_A == null || CourierEvents_B == null)
            return;
        try {
            if (couriersLock_A)
                CourierEvents_B.add(eventCourier);
            else
                CourierEvents_A.add(eventCourier);

            if (tTask != null && CourierEvents_A.size() > 0 && !tTask.isAlive()) {
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
        while (keepDoing) {
            if (CourierEvents_A != null && CourierEvents_B != null) {
                couriersLock_A = true;
                if (CourierEvents_A.size() > 0) {
                    poolingAB(CourierEvents_A);
                    CourierEvents_A.clear();
                }
                couriersLock_A = false;
                if (CourierEvents_B.size() > 0) {
                    poolingAB(CourierEvents_B);
                    CourierEvents_B.clear();
                }
            }

            if (CourierEvents_MainA != null && CourierEvents_MainB != null) {
                couriersLock_M = true;
                if (CourierEvents_MainA.size() > 0) {
                    poolingABM(CourierEvents_MainA);
                    CourierEvents_MainA.clear();
                }
                couriersLock_M = false;
                if (CourierEvents_MainB.size() > 0) {
                    poolingABM(CourierEvents_MainB);
                    CourierEvents_MainB.clear();
                }
            }

            //MMLog.log(TAG,"A:"+couriers_A.size()+",B:"+couriers_B.size()+"AM:"+couriers_MA.size()+"BM:"+couriers_MB.size());
            if ((CourierEvents_A.size() <= 0) && (CourierEvents_B.size() <= 0) && (CourierEvents_MainA.size() <= 0) && (CourierEvents_MainB.size() <= 0)) {
                try {
                    //MMLog.log(TAG,"suspend!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!1");
                    //tTask.wait();
                    LockSupport.park();
                    //MMLog.log(TAG,"suspend!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!2");
                } catch (Exception e) {
                    //e.printStackTrace();
                    MMLog.log(TAG, e.toString());
                }
            }

        }//while (keepDoing)
    }

    private void poolingAB(ArrayList<EventCourier> couriers) {
        try {
            for (int i = 0; i < couriers.size(); i++) {
                EventCourier eventCourier = couriers.get(i);
                if (eventCourier == null) {
                    continue; //couriers.remove(i);//丢弃
                }
                TCourierEventListener courierEventListener = getCourierEventListener(eventCourier);
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
            for (int i = 0; i < couriers.size(); i++) {
                EventCourier eventCourier = couriers.get(i);
                if (eventCourier == null) {
                    continue; //couriers.remove(i);//丢弃
                }
                TCourierEventListener courierEventListener = getCourierEventListener(eventCourier);
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
            MMLog.e(TAG, "poolingABM FAILED " + e.toString());
        }
    }

    private TCourierEventListener getCourierEventListener(EventCourier eventCourier) {
        TCourierEventListener courierEventListener = (TCourierEventListener) InvokerList.getObject(eventCourier.getTag());
        return courierEventListener;
    }

    public void free() {
        keepDoing = false;
        tTask.freeFree();
    }
}
