package com.zhuchao.android.fbase;

import com.zhuchao.android.eventinterface.InvokeInterface;
import org.jetbrains.annotations.NotNull;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.concurrent.locks.LockSupport;

public class TCourierEventBus implements InvokeInterface {
    private final String TAG = "TCourierEventBus";
    private final String DEFAULT_EVENT_METHOD_NAME = "onCourierEvent";
    private final ObjectList InvokerList = new ObjectList();
    //private final ObjectList EventTypeList = new ObjectList();
    private final ArrayList<EventCourierInterface> CourierEventsQueueA = new ArrayList<EventCourierInterface>();
    private final ArrayList<EventCourierInterface> CourierEventsQueueB = new ArrayList<EventCourierInterface>();
    private final ArrayList<EventCourierInterface> CourierEventsQueueMainA = new ArrayList<EventCourierInterface>();
    private final ArrayList<EventCourierInterface> CourierEventsQueueMainB = new ArrayList<EventCourierInterface>();
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
    //注册默认接口
    //@Deprecated
    public void registerEventObserver(@NotNull String tag, @NotNull TCourierEventListener courierEventListener) {
        String tagName = tag+DEFAULT_EVENT_METHOD_NAME;
        if (InvokerList.containsTag(tagName)) return;
        //findCourierSubscribeEventType(courierEventListener);
        TCourierEventListenerBundleManager tCourierEventListenerBundleManager = new TCourierEventListenerBundleManager((Object) courierEventListener, null, null, null);
        InvokerList.addItem(tagName, tCourierEventListenerBundleManager);
        //EventTypeList.addItem(courierEventListener.getClass().getName(), eventType);
        //MMLog.i(TAG,"registerEventObserver -> " +tag);
        //assert eventType != null;
        MMLog.i(TAG, "registerEventObserver -> " + courierEventListener.getClass().getName()+",tag = "+tag);
    }
    //观察订阅
    public void registerEventObserver(@NotNull Object context) {
        String tag = context.getClass().getName();
        String tagName = tag+DEFAULT_EVENT_METHOD_NAME;
        if (InvokerList.containsTag(tag)) return;
        if(TCourierEventListener.class.isAssignableFrom(context.getClass()))
        {
            TCourierEventListenerBundleManager tCourierEventListenerBundleManager = new TCourierEventListenerBundleManager(context, null, null, null);
            InvokerList.addItem(tagName, tCourierEventListenerBundleManager);
        }
        findCourierSubscribeEventType(context);
        //MMLog.i(TAG, "registerEventObserver -> " + courierEventListener.getClass().getName() + ",eventType:" + eventType.getName());
    }

    //@Deprecated
    public void unRegisterEventObserver(@NotNull String tag) {
        InvokerList.remove(tag+DEFAULT_EVENT_METHOD_NAME);//移除默认观察者
        //EventTypeList.remove(tag);
    }

    public void unRegisterEventObserver(@NotNull Object courierEventListener) {
        //if(InvokerList.containsTag(tag))
        //InvokerList.remove(courierEventListener.getClass().getName());
        //EventTypeList.remove(courierEventListener.getClass().getName());
        InvokerList.removeObjectsLike(courierEventListener.getClass().getName());
    }

    public ObjectList getInvokerList() {
        return InvokerList;
    }

    public void postMain(EventCourierInterface eventCourier) {
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

    public void post(EventCourierInterface eventCourier) {
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

    public void postDelay(EventCourierInterface eventCourier, long millis) {
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
        //MMLog.log(TAG, "Courier Event Bus start...");
        while (keepDoing) {
            //if (CourierEventsQueueA != null && CourierEventsQueueB != null)
            {
                couriersLockQueueA = true;
                if (CourierEventsQueueA.size() > 0) {
                    poolingEventInvokerList_ab(CourierEventsQueueA);
                    CourierEventsQueueA.clear();
                }
                couriersLockQueueA = false;
                if (CourierEventsQueueB.size() > 0) {
                    poolingEventInvokerList_ab(CourierEventsQueueB);
                    CourierEventsQueueB.clear();
                }
            }

            //if (CourierEventsQueueMainA != null && CourierEventsQueueMainB != null)
            {
                couriersLockQueueMA = true;
                if (CourierEventsQueueMainA.size() > 0) {
                    poolingEventInvokerList_abm(CourierEventsQueueMainA);
                    CourierEventsQueueMainA.clear();
                }
                couriersLockQueueMA = false;
                if (CourierEventsQueueMainB.size() > 0) {
                    poolingEventInvokerList_abm(CourierEventsQueueMainB);
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

    private void poolingEventInvokerList_ab(ArrayList<EventCourierInterface> couriers) {
        //try {
            for (int i = 0; i < couriers.size(); i++) {
                EventCourierInterface eventCourier = couriers.get(i);
                if (eventCourier == null) continue; //couriers.remove(i);//丢弃
                if (eventCourier.getTag() == null)//如果为空，调用所有接口似广播
                {
                    for (Object obj : InvokerList.getAllObject()) {
                         handleSingleMatchedEventType((TCourierEventListenerBundleManager)obj, eventCourier);
                    }
                }
                else
                {
                    TCourierEventListenerBundleManager tCourierEventListenerBundleManager = getDefaultCourierEventListener(eventCourier);
                    if(tCourierEventListenerBundleManager != null)
                       handleSingleMatchedEventType(tCourierEventListenerBundleManager, eventCourier);
                }
            }
            couriers.clear();
        //} catch (Exception e) {
            //couriers.clear();
       //     MMLog.e(TAG, "poolingEventInvokerList_ab FAILED " + e.toString());
       // }
    }

    private void poolingEventInvokerList_abm(ArrayList<EventCourierInterface> couriers) {
        //try {
            for (int i = 0; i < couriers.size(); i++) {
                EventCourierInterface eventCourier = couriers.get(i);
                if (eventCourier == null) continue; //couriers.remove(i);//丢弃
                if (eventCourier.getTag() == null) { //如果为空，调用所有接口，类似广播
                    for (Object obj : InvokerList.getAllObject()) {
                        ThreadUtils.runOnMainUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    handleSingleMatchedEventType_M((TCourierEventListenerBundleManager) obj, eventCourier);
                                }
                        });
                    }
                } else {
                    TCourierEventListenerBundleManager tCourierEventListenerBundleManager = getDefaultCourierEventListener(eventCourier);
                    if (tCourierEventListenerBundleManager != null) {
                        ThreadUtils.runOnMainUiThread(new Runnable() {
                            @Override
                            public void run() {
                                handleSingleMatchedEventType_M(tCourierEventListenerBundleManager, eventCourier);
                            }
                        });
                    }
                }
            }
            couriers.clear();
        //} catch (Exception e) {
            //couriers.clear();
       //     MMLog.e(TAG, "poolingEventInvokerList_abm FAILED " + e.toString());
       // }
    }

    private TCourierEventListenerBundleManager getDefaultCourierEventListener(EventCourierInterface eventCourier) {
        if (eventCourier.getTag() != null) {
            return (TCourierEventListenerBundleManager) InvokerList.getObject(eventCourier.getTag()+DEFAULT_EVENT_METHOD_NAME);
        }
        return null;
    }
    //查找订阅的方法
    private void findCourierSubscribeEventType(Object courierEventListener) {
        Method[] methods = courierEventListener.getClass().getMethods();
        for (int i = 0; i < methods.length - 1; i++) {
            TCourierSubscribe tCourierSubscribe = methods[i].getAnnotation(TCourierSubscribe.class);
            if (tCourierSubscribe != null) {
                Class<?>[] classes = methods[i].getParameterTypes();
                methods[i].setAccessible(true);
                TCourierEventListenerBundleManager tCourierEventListenerBundleManager = new TCourierEventListenerBundleManager(courierEventListener, methods[i], tCourierSubscribe, classes);
                InvokerList.addItem(courierEventListener.getClass().getName()+methods[i].getName(), tCourierEventListenerBundleManager);
                //MMLog.i(TAG, tCourierEventListenerBundleManager.toToString());
            }
        }
    }

    private void handleSingleEventType(@NotNull TCourierEventListenerBundleManager tCourierEventListenerBundleManager, @NotNull EventCourierInterface event) {
        //Class<?>[] listenerParameterTypes = tCourierEventListenerBundleManager.parameterTypes;
        try {
            Object context = tCourierEventListenerBundleManager.getCourierEventListener();
            if(context != null)
            {
                if(tCourierEventListenerBundleManager.getMethod() != null) {//优先考虑订阅方法
                    printEventLog("default onCourierEvent",tCourierEventListenerBundleManager,event);
                    tCourierEventListenerBundleManager.getMethod().invoke(context, event);//呼叫指定方法
                }
                else if(TCourierEventListener.class.isAssignableFrom(context.getClass())) {
                    printEventLog("subscriber onCourierEvent",tCourierEventListenerBundleManager,event);
                    ((TCourierEventListener) context).onCourierEvent(event);//call 默认接口onCourierEvent(event);
                }
            }
        } catch (IllegalAccessException | InvocationTargetException e) {
            //e.printStackTrace();
            MMLog.e(TAG, e.getMessage());
        }
    }

    private void handleSingleMatchedEventType(@NotNull TCourierEventListenerBundleManager tCourierEventListenerBundleManager,@NotNull EventCourierInterface event) {
        //Class<?>[] listenerParameterTypes = tCourierEventListenerBundleManager.parameterTypes;
        TCourierSubscribe tCourierSubscribe = tCourierEventListenerBundleManager.getCourierSubscribe();
        if(tCourierSubscribe != null)
        {
            switch (tCourierSubscribe.threadMode()) {
                case MAIN:
                case MAIN_ORDERED:
                    if(tCourierEventListenerBundleManager.isParameterTypesMatched(event))
                       ThreadUtils.runOnMainUiThread(new Runnable() {
                        @Override
                        public void run() {
                            handleSingleEventType(tCourierEventListenerBundleManager, event);
                        }
                      });
                    break;
                case BACKGROUND:
                case POSTING:
                case ASYNC:
                    if(tCourierEventListenerBundleManager.isParameterTypesMatched(event))
                       handleSingleEventType(tCourierEventListenerBundleManager, event);
                    break;
            }
        }
        else //if(TCourierEventListener.class.isAssignableFrom(tCourierEventListenerBundleManager.getCourierEventListener().getClass()))
        {
            handleSingleEventType(tCourierEventListenerBundleManager, event);
        }
    }

    private void handleSingleMatchedEventType_M(@NotNull TCourierEventListenerBundleManager tCourierEventListenerBundleManager,@NotNull EventCourierInterface event) {
        TCourierSubscribe tCourierSubscribe = tCourierEventListenerBundleManager.getCourierSubscribe();
        if(tCourierSubscribe != null)
        {
            switch (tCourierSubscribe.threadMode()) {
                case MAIN:
                case MAIN_ORDERED:
                case BACKGROUND:
                case POSTING:
                case ASYNC:
                    if(tCourierEventListenerBundleManager.isParameterTypesMatched(event))
                       handleSingleEventType(tCourierEventListenerBundleManager, event);
                    break;
            }
        }
        else //if(TCourierEventListener.class.isAssignableFrom(tCourierEventListenerBundleManager.getCourierEventListener().getClass()))
        {
            handleSingleEventType(tCourierEventListenerBundleManager, event);
        }
    }

    static class TCourierEventListenerBundleManager {
        private final Object courierEventListener;
        private final Method method;
        private final TCourierSubscribe tCourierSubscribe;
        private final Class<?>[] parameterTypes;

        public TCourierEventListenerBundleManager(Object courierEventListener, Method method, TCourierSubscribe tCourierSubscribe, Class<?>[] parameterTypes) {
            this.courierEventListener = courierEventListener;
            this.tCourierSubscribe = tCourierSubscribe;
            this.method = method;
            this.parameterTypes = parameterTypes;
        }

        public Object getCourierEventListener() {
            return courierEventListener;
        }

        public TCourierSubscribe getCourierSubscribe() {
            return tCourierSubscribe;
        }

        public Method getMethod() {
            return method;
        }

        public Class<?>[] getClasses() {
            return parameterTypes;
        }

        public boolean isParameterTypesMatched(Object event)
        {
            //&& listenerParameterTypes != null
            //&& listenerParameterTypes[0] != null
            //&& listenerParameterTypes[0].isAssignableFrom(event.getClass())
            //Class<?>[] listenerParameterTypes = tCourierEventListenerBundleManager.parameterTypes;
            return parameterTypes != null && parameterTypes[0] != null &&
                    parameterTypes[0].isAssignableFrom(event.getClass());
        }

        public String toToString()
        {
            String str = this.courierEventListener.getClass().getName();
            if(method != null)
                str = str + ","+this.method.getName().toString();
            if(parameterTypes != null)
                str = str+ "," + this.parameterTypes[0].getName();
            if(tCourierSubscribe != null)
                str = str+ "," + this.tCourierSubscribe.threadMode().toString();
            return str;
        }
    }

    public void free() {
        keepDoing = false;
        tTask.freeFree();
        InvokerList.clear();
    }

    private void printEventLog(String tag,@NotNull TCourierEventListenerBundleManager tCourierEventListenerBundleManager,@NotNull EventCourierInterface event)
    {
        if(event.getTag() != null) return;
        MMLog.d(TAG,tag+" EventListener:"+tCourierEventListenerBundleManager.toToString()+",ev:"+event.getClass().getName());
        //MMLog.d(TAG,"Courier Event = "+event.toString() + ",class = "+event.getClass().getName());
        //MMLog.d(TAG,"tCourierEventListenerBundleManager = " +tCourierEventListenerBundleManager.toToString());
        //InvokerList.printAll();
    }
    private void printEventLog2(@NotNull TCourierEventListenerBundleManager tCourierEventListenerBundleManager,@NotNull EventCourierInterface event)
    {
        //if(event.getTag() != null) return;
        //MMLog.d(TAG,"Courier Event = "+event.toString() + ",class = "+event.getClass().getName());
        //MMLog.d(TAG,"tCourierEventListenerBundleManager = " +tCourierEventListenerBundleManager.toToString());
    }
}
