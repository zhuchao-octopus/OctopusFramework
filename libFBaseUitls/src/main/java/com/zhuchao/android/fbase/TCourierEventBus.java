package com.zhuchao.android.fbase;

import com.zhuchao.android.eventinterface.InvokeInterface;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.concurrent.locks.LockSupport;

public class TCourierEventBus implements InvokeInterface {
    private final String TAG = "TCourierEventBus";
    private final ObjectList InvokerList = new ObjectList();
    private final ObjectList EventTypeList = new ObjectList();
    private final ArrayList<EventCourier> CourierEventsQueueA = new ArrayList<EventCourier>();
    private final ArrayList<EventCourier> CourierEventsQueueB = new ArrayList<EventCourier>();
    private final ArrayList<EventCourier> CourierEventsQueueMainA = new ArrayList<EventCourier>();
    private final ArrayList<EventCourier> CourierEventsQueueMainB = new ArrayList<EventCourier>();
    private final TTask tTask;
    //private InvokeInterface invokeInterface = null;
    private boolean keepDoing = true;
    private boolean couriersLockQueueA = false;
    private boolean couriersLockQueueMA = false;

    public TCourierEventBus() {
        //InvokerList = new ObjectList();
        //CourierEventsQueueA = new ArrayList<EventCourier>();
        //CourierEventsQueueB = new ArrayList<EventCourier>();
        //CourierEventsQueueMainA = new ArrayList<EventCourier>();
        //CourierEventsQueueMainB = new ArrayList<EventCourier>();
        //invokeInterface = this;
        keepDoing = true;
        tTask = new TTask(TAG, this);
    }

    public void registerEventObserver(String tag, TCourierEventListener courierEventListener) {
        String eventType = getEventType(courierEventListener);
        InvokerList.addItem(tag, courierEventListener);
        EventTypeList.addItem(tag,eventType);
        //MMLog.i(TAG,"registerEventObserver -> " +tag);
        MMLog.i(TAG,"registerEventObserver -> " +courierEventListener.getClass().getName()+",eventType:"+eventType);
    }

    public void registerEventObserver(TCourierEventListener courierEventListener) {
        String eventType = getEventType(courierEventListener);
        String tag = courierEventListener.getClass().getName();
        InvokerList.addItem(tag, courierEventListener);
        EventTypeList.addItem(tag,eventType);
        MMLog.i(TAG,"registerEventObserver -> " +courierEventListener.getClass().getName()+",eventType:"+eventType);
    }

    public void unRegisterEventObserver(String tag) {
        InvokerList.remove(tag);
        EventTypeList.remove(tag);
    }

    public void unRegisterEventObserver(TCourierEventListener courierEventListener) {
        //if(InvokerList.containsTag(tag))
        InvokerList.remove(courierEventListener.getClass().getName());
        EventTypeList.remove(courierEventListener.getClass().getName());
    }

    public ObjectList getInvokerList() {
        return InvokerList;
    }

    public void postMain(EventCourier eventCourier) {
        if (couriersLockQueueMA)
            CourierEventsQueueMainB.add(eventCourier);
        else
            CourierEventsQueueMainA.add(eventCourier);
        try {
            if (tTask != null && !tTask.isAlive()) {
                keepDoing = true;
                tTask.start();
                //MMLog.log(TAG, "CourierEventBus main start...");
            }
        } catch (Exception e) {
            MMLog.log(TAG, "postMain event failed," + e.toString());
        }
        try {
            if (tTask != null) {
                LockSupport.unpark(tTask);
            }
        } catch (Exception e) {
            MMLog.log(TAG, "postMain event failed," + e.toString());
        }
    }

    public void post(EventCourier eventCourier) {
        if (couriersLockQueueA)
            CourierEventsQueueB.add(eventCourier);
        else
            CourierEventsQueueA.add(eventCourier);
        try {
            if (tTask != null && !tTask.isAlive()) {
                keepDoing = true;
                tTask.start();
                //MMLog.log(TAG, "CourierEventBus start...");
            }
        } catch (Exception e) {
            MMLog.log(TAG, "post event failed," + e.toString());
        }
        try {
            if (tTask != null) {
                //tTask.notifyAll();
                LockSupport.unpark(tTask);
            }
        } catch (Exception e) {
            MMLog.log(TAG, "post event failed," + e.toString());
        }
    }

    public void postDelay(EventCourier eventCourier, long millis) {
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
        MMLog.log(TAG, "Courier Event Bus start...");
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
        MMLog.log(TAG, "Courier Event Bus stop...");
    }

    private void poolingAB(ArrayList<EventCourier> couriers) {
        try {
            for (int i = 0; i < couriers.size(); i++)
            {
                EventCourier eventCourier = couriers.get(i);
                if (eventCourier == null) {
                    continue; //couriers.remove(i);//丢弃
                }
                if (eventCourier.getTag() == null)
                {//如果为空，调用所有接口似广播
                    for (Object obj : InvokerList.getAllObject()) {
                        if (obj == null) break;
                        try {
                            TCourierEventListener tCourierEventListener = (TCourierEventListener) obj;
                            tCourierEventListener.onCourierEvent(eventCourier);
                            //MMLog.i(TAG,eventCourier.getTag()+","+ eventCourier.getFromClass());
                        } catch (Exception e) {
                            //e.printStackTrace();
                            MMLog.e(TAG, e.getMessage());
                        }
                    }
                }
                else
                {
                    TCourierEventListener courierEventListener = getCourierEventListener(eventCourier);
                    if (courierEventListener != null) {//调用指定接口
                        courierEventListener.onCourierEvent(eventCourier);
                        //MMLog.i(TAG,"eventCourier.getClass().getName() = "+ eventCourier.getClass().getName());
                        //MMLog.i(TAG,"eventCourier.getClass().getName() = "+ courierEventListener.getClass().getName());
                    }
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

                if (eventCourier.getTag() == null) { //如果为空，调用所有接口，类似广播
                    for (Object obj : InvokerList.getAllObject()) {
                        if (obj == null) break;
                        try {
                            ThreadUtils.runOnMainUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    TCourierEventListener tCourierEventListener = (TCourierEventListener) obj;
                                    tCourierEventListener.onCourierEvent(eventCourier);
                                }
                            });
                        } catch (Exception e) {
                            //e.printStackTrace();
                            MMLog.e(TAG, e.getMessage());
                        }
                    }
                } else {
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
            }
            couriers.clear();
        } catch (Exception e) {
            //couriers.clear();
            MMLog.e(TAG, "poolingABM FAILED " + e.toString());
        }
    }

    private TCourierEventListener getCourierEventListener(EventCourier eventCourier) {
        if (eventCourier.getTag() != null) {
            return (TCourierEventListener) InvokerList.getObject(eventCourier.getTag());
        }
        return null;
    }

    private String getEventType(TCourierEventListener courierEventListener)
    {
      Method[] methods = courierEventListener.getClass().getMethods();
        for(int i=0 ; i< methods.length-1;i++)
        {
            if(methods[i].getName().equals("onCourierEvent"))
            {
                Class<?>[] classes = methods[i].getParameterTypes();
                return classes[0].getName();
            }
            //MMLog.i(TAG,"method  = " +methods[i].getName()+",getParameterTypes = "+ Arrays.toString(methods[i].getParameterTypes()));
        }
        return null;
    }

    private void handleSingleEventType(TCourierEventListener courierEventListener,Object event)
    {
       String eventType = EventTypeList.get(courierEventListener.getClass().getName(),null);
       if(event.getClass().getName().equals(eventType)) {
           //courierEventListener.onCourierEvent(event);
       }
    }

    public void free() {
        keepDoing = false;
        tTask.freeFree();
    }
}
