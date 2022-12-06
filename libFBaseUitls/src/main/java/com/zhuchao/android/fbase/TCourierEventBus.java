package com.zhuchao.android.fbase;

import com.zhuchao.android.eventinterface.InvokeInterface;

import java.util.ArrayList;
import java.util.concurrent.locks.LockSupport;

public class TCourierEventBus implements InvokeInterface {
    private final String TAG = "TCourierEventBus";
    private ObjectList InvokerList = null;
    private final ArrayList<EventCourier> CourierEventsQueueA = new ArrayList<EventCourier>();
    private final ArrayList<EventCourier> CourierEventsQueueB = new ArrayList<EventCourier>();
    private final ArrayList<EventCourier> CourierEventsQueueMainA = new ArrayList<EventCourier>();
    private final ArrayList<EventCourier> CourierEventsQueueMainB = new ArrayList<EventCourier>();
    private TTask tTask;
    //private InvokeInterface invokeInterface = null;
    private boolean keepDoing = true;
    private boolean couriersLockQueueA = false;
    private boolean couriersLockQueueMA = false;

    public TCourierEventBus() {
        InvokerList = new ObjectList();
        //CourierEventsQueueA = new ArrayList<EventCourier>();
        //CourierEventsQueueB = new ArrayList<EventCourier>();
        //CourierEventsQueueMainA = new ArrayList<EventCourier>();
        //CourierEventsQueueMainB = new ArrayList<EventCourier>();
        //invokeInterface = this;
        keepDoing = true;
        tTask = new TTask(TAG, this);
    }

    public void registerEventObserver(String tag, TCourierEventListener courierEventListener) {
        InvokerList.addItem(tag, courierEventListener);
    }

    public void postMain(EventCourier eventCourier) {
        try {
            if (couriersLockQueueMA)
                CourierEventsQueueMainB.add(eventCourier);
            else
                CourierEventsQueueMainA.add(eventCourier);

            if (tTask != null && CourierEventsQueueMainA.size() > 0 && !tTask.isAlive()) {
                keepDoing = true;
                tTask.start();
                MMLog.log(TAG, "CourierEventBus start...");
            }
            if (tTask != null) {
                //tTask.notifyAll();
                LockSupport.unpark(tTask);
            }
        } catch (Exception e) {
            MMLog.log(TAG, "postMain event failed," + e.toString());
        }
    }

    public void post(EventCourier eventCourier) {
        try {
            if (couriersLockQueueA)
                CourierEventsQueueB.add(eventCourier);
            else
                CourierEventsQueueA.add(eventCourier);

            if (tTask != null && CourierEventsQueueA.size() > 0 && !tTask.isAlive()) {
                keepDoing = true;
                tTask.start();
                MMLog.log(TAG, "CourierEventBus start...");
            }
            if (tTask != null) {
                //tTask.notifyAll();
                LockSupport.unpark(tTask);
            }
        } catch (Exception e) {
            MMLog.log(TAG, "post event failed," + e.toString());
        }
    }

    public void postDelay(EventCourier eventCourier,long millis)
    {
        try {
            Thread.sleep(millis);
        } catch (InterruptedException e) {
            //e.printStackTrace();
        }
        post(eventCourier);
    }
    @Override
    public void CALLTODO(String tag) {
        //MMLog.log(TAG,"CALLTODO "+ keepDoing);
        while (keepDoing) {
            //if (CourierEventsQueueA != null && CourierEventsQueueB != null)
            {
                couriersLockQueueA = true;
                if (CourierEventsQueueA.size() > 0) {
                    poolingAB(CourierEventsQueueA);
                    CourierEventsQueueA.clear();
                }
                couriersLockQueueA = false;
                if (CourierEventsQueueB.size() > 0) {
                    poolingAB(CourierEventsQueueB);
                    CourierEventsQueueB.clear();
                }
            }

            //if (CourierEventsQueueMainA != null && CourierEventsQueueMainB != null)
            {
                couriersLockQueueMA = true;
                if (CourierEventsQueueMainA.size() > 0) {
                    poolingABM(CourierEventsQueueMainA);
                    CourierEventsQueueMainA.clear();
                }
                couriersLockQueueMA = false;
                if (CourierEventsQueueMainB.size() > 0) {
                    poolingABM(CourierEventsQueueMainB);
                    CourierEventsQueueMainB.clear();
                }
            }

            //MMLog.log(TAG,"A:"+couriers_A.size()+",B:"+couriers_B.size()+"AM:"+couriers_MA.size()+"BM:"+couriers_MB.size());
            if ((CourierEventsQueueA.size() <= 0) &&
                (CourierEventsQueueB.size() <= 0) &&
                (CourierEventsQueueMainA.size() <= 0) &&
                (CourierEventsQueueMainB.size() <= 0)) //A已经解锁，B内容始终为空
            {
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
