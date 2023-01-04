package com.zhuchao.android.fbase;


import com.zhuchao.android.eventinterface.InvokeInterface;
import org.jetbrains.annotations.NotNull;
import java.lang.annotation.Annotation;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.locks.LockSupport;

public class TCourierEventBus implements InvokeInterface {
    private final String TAG = "TCourierEventBus";
    //private final String DEFAULT_EVENT_METHOD_NAME = "onCourierEvent";
    private final ObjectList InvokerList = new ObjectList();
    //private final ObjectList EventTypeList = new ObjectList();
    private final ArrayList<Object> CourierEventsQueueA = new ArrayList<Object>();
    private final ArrayList<Object> CourierEventsQueueB = new ArrayList<Object>();
    private final ArrayList<Object> CourierEventsQueueMainA = new ArrayList<Object>();
    private final ArrayList<Object> CourierEventsQueueMainB = new ArrayList<Object>();
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
        //if (InvokerList.containsTag(tagName)) return;
        //TCourierEventListenerBundleManager tCourierEventListenerBundleManager = new TCourierEventListenerBundleManager((Object) courierEventListener, null, null, null);
        //String tagName = tag+DEFAULT_EVENT_METHOD_NAME;
        //InvokerList.addItem(tagName, tCourierEventListenerBundleManager);//显示注册默认接口

        findCourierEventTypeSubscriber(tag,courierEventListener);//分析当前类有无订阅者
        MMLog.i(TAG, "registerEventObserver -> " + courierEventListener.getClass().getName()+",tag = "+tag);
        //printAllEventListener();
    }
    //观察订阅
    public void registerEventObserver(@NotNull Object context) {
        //if (InvokerList.containsTag(tag)) return;
        //if(TCourierEventListener.class.isAssignableFrom(context.getClass()))//隐式注册默认接口
        //{
        //    String tagName = context.getClass().getName()+DEFAULT_EVENT_METHOD_NAME;
        //    TCourierEventListenerBundleManager tCourierEventListenerBundleManager = new TCourierEventListenerBundleManager(context, null, null, null);
        //    InvokerList.addItem(tagName, tCourierEventListenerBundleManager);
        //}
        findCourierEventTypeSubscriber(null,context);//分析当前类有无订阅者
        MMLog.i(TAG, "registerEventObserver -> " + context.getClass().getName());
        //printAllEventListener();
    }

    //@Deprecated
    public void unRegisterEventObserver(@NotNull String tag) {
        try {
            synchronized (InvokerList){ InvokerList.removeObjectsLike(tag);}
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void unRegisterEventObserver(@NotNull Object courierEventListener) {
        try {
           synchronized (InvokerList){ InvokerList.removeObjectsLike(courierEventListener.getClass().getName());}
        } catch (Exception e) {
            e.printStackTrace();
        }
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
        startBus();
    }

    public void post(Object eventCourier) {
        if (couriersLockQueueA)
            CourierEventsQueueB.add(eventCourier);
        else
            CourierEventsQueueA.add(eventCourier);
        startBus();
    }

    public void postDelay(Object eventCourier, long millis) {
        try {
            Thread.sleep(millis);
        } catch (InterruptedException e) {
            //e.printStackTrace();
        }
        post(eventCourier);
    }

    private void startBus()
    {
        try {
            if (tTask != null && !tTask.isAlive()) {
                keepDoing = true;
                tTask.start();
                //MMLog.log(TAG, "CourierEventBus start...");
            }
        } catch (Exception e) {
            MMLog.log(TAG, "start event bus failed," + e.toString());
        }
        try {
            if (tTask != null) {
                //tTask.notifyAll();
                LockSupport.unpark(tTask);
            }
        } catch (Exception e) {
            MMLog.log(TAG, "start event bus failed," + e.toString());
        }
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
        MMLog.log(TAG, "Courier Event Bus stopped...");
    }

    private void poolingEventInvokerList_ab(ArrayList<Object> couriers) {
        //try {
            for (int i = 0; i < couriers.size(); i++)
            {
                String postToTag = null;
                Object eventCourier = couriers.get(i);
                if (eventCourier == null) continue; //couriers.remove(i);//丢弃
                if(EventCourierInterface.class.isAssignableFrom(eventCourier.getClass()))
                    postToTag = ((EventCourierInterface)eventCourier).getTag();

                if (postToTag != null)
                {
                    List<Object> tCourierEventListenerBundleManagers = getDefaultCourierEventListeners(postToTag);
                    for (Object obj : tCourierEventListenerBundleManagers)
                    {
                        handleSingleMatchedEventType((TCourierEventListenerBundle)obj, eventCourier);
                    }
                }
                else//如果为空，调用所有接口似广播
                {
                    for (Object obj : InvokerList.getAllObject()) {
                        handleSingleMatchedEventType((TCourierEventListenerBundle)obj, eventCourier);
                    }
                }
            }
            couriers.clear();
        //} catch (Exception e) {
            //couriers.clear();
       //     MMLog.e(TAG, "poolingEventInvokerList_ab FAILED " + e.toString());
       // }
    }

    private void poolingEventInvokerList_abm(ArrayList<Object> couriers) {
        //try {
            for (int i = 0; i < couriers.size(); i++) {
                String postToTag = null;
                Object eventCourier = couriers.get(i);
                if (eventCourier == null) continue; //couriers.remove(i);//丢弃
                if(EventCourierInterface.class.isAssignableFrom(eventCourier.getClass()))
                    postToTag = ((EventCourierInterface)eventCourier).getTag();

                if (postToTag == null) { //如果为空，调用所有接口，类似广播
                    for (Object obj : InvokerList.getAllObject()) {
                        ThreadUtils.runOnMainUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    handleSingleMatchedEventType_M((TCourierEventListenerBundle) obj, eventCourier);
                                }
                        });
                    }
                }
                else
                {
                    List<Object> tCourierEventListenerBundleManagers = getDefaultCourierEventListeners(postToTag);
                    for (Object obj : tCourierEventListenerBundleManagers)
                    {
                        ThreadUtils.runOnMainUiThread(new Runnable() {
                            @Override
                            public void run() {
                                handleSingleMatchedEventType_M((TCourierEventListenerBundle)obj, eventCourier);
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

    private List<Object> getDefaultCourierEventListeners(String tag) {
        return InvokerList.getObjectsLike(tag);
    }
    //查找订阅的方法
    private void findCourierEventTypeSubscriber(String tag,Object courierEventListener) {
        Method[] methods = courierEventListener.getClass().getMethods();
        //Method[] methods = courierEventListener.getClass().getDeclaredMethods();
        for (int i = 0; i < methods.length - 1; i++) {
            TCourierSubscribe tCourierSubscribe = methods[i].getAnnotation(TCourierSubscribe.class);
            Class<?>[] classes = methods[i].getParameterTypes();
            if(classes.length != 1) continue;

            String pName = classes[0].getSimpleName();
            pName = pName+courierEventListener.hashCode();

            if (tCourierSubscribe != null && (Modifier.toString(methods[i].getModifiers()).contains("public"))) //显示的订阅了总线接口 && courierInterfaceParameterTypes.length == 1
            {//显示订阅
                methods[i].setAccessible(true);
                TCourierEventListenerBundle tCourierEventListenerBundle = new TCourierEventListenerBundle(courierEventListener, methods[i], tCourierSubscribe, classes);
                if(FileUtils.NotEmptyString(tag))
                    InvokerList.addItem(tag+methods[i].getName()+pName, tCourierEventListenerBundle);
                else
                    InvokerList.addItem(courierEventListener.getClass().getName()+methods[i].getName()+pName, tCourierEventListenerBundle);
                //MMLog.i(TAG, tCourierEventListenerBundleManager.toToString());
            }
            //else if(methods[i].getName().equals(DEFAULT_EVENT_METHOD_NAME))//默认方法名特殊处理,自动匹配订阅(隐式订阅)
            //对参数实现EventCourierInterface接口的方法进行隐式订阅
            else if(EventCourierInterface.class.isAssignableFrom(classes[0])
                    && (Modifier.toString(methods[i].getModifiers()).contains("public")))
            {
                methods[i].setAccessible(true);
                TCourierEventListenerBundle listenerBundleManager = new TCourierEventListenerBundle(courierEventListener, methods[i], new defaultSubscriber(), classes);
                if(FileUtils.NotEmptyString(tag))//自动增加匹配接口默认方法，无需显示申明接口
                    InvokerList.addItem(tag+methods[i].getName()+pName, listenerBundleManager);
                else
                    InvokerList.addItem(courierEventListener.getClass().getName()+methods[i].getName()+pName, listenerBundleManager);
            }
        }
    }

    private void handleSingleEventType(@NotNull TCourierEventListenerBundle tCourierEventListenerBundle, @NotNull Object event) {
        //Class<?>[] listenerParameterTypes = tCourierEventListenerBundleManager.parameterTypes;
        try {
            Object context = tCourierEventListenerBundle.getCourierEventListener();
            if(context != null)
            {
                if(tCourierEventListenerBundle.getMethod() != null) {//优先考虑订阅方法,等效if(tCourierSubscribe != null)//订阅的消息接口
                    printEventLog("subscriber "+ tCourierEventListenerBundle.getMethod().getName(), tCourierEventListenerBundle,event);
                    tCourierEventListenerBundle.getMethod().invoke(context, event);//呼叫指定订阅方法
                }
                else if(TCourierEventListener.class.isAssignableFrom(context.getClass())) {//呼叫默认接口
                    printEventLog("default onCourierEvent", tCourierEventListenerBundle,event);
                    ((TCourierEventListener) context).onCourierEvent((EventCourierInterface)event);//call 默认接口onCourierEvent(event);
                }
            }
        } catch (IllegalAccessException | InvocationTargetException e) {
            //e.printStackTrace();
            MMLog.e(TAG,tCourierEventListenerBundle.toToString());
            MMLog.e(TAG, e.getMessage() + ":"+event.getClass().getSimpleName());
        }
    }

    private void handleSingleMatchedEventType(@NotNull TCourierEventListenerBundle tCourierEventListenerBundle, @NotNull Object event) {
        //Class<?>[] listenerParameterTypes = tCourierEventListenerBundleManager.parameterTypes;
        TCourierSubscribe tCourierSubscribe = tCourierEventListenerBundle.getCourierSubscribe();
        if(tCourierSubscribe != null )//处理显示订阅的消息接口&&tCourierEventListenerBundleManager.getMethod() != null
        {
            switch (tCourierSubscribe.threadMode()) {
                case MAIN:
                case MAIN_ORDERED:
                    if(tCourierEventListenerBundle.isParameterTypesMatched(event))
                       ThreadUtils.runOnMainUiThread(new Runnable() {
                        @Override
                        public void run() {
                            handleSingleEventType(tCourierEventListenerBundle, event);
                        }
                      });
                    break;
                case BACKGROUND:
                case POSTING:
                case ASYNC:
                    if(tCourierEventListenerBundle.isParameterTypesMatched(event))
                       handleSingleEventType(tCourierEventListenerBundle, event);
                    break;
            }
        }
        else if(tCourierEventListenerBundle.getMethod() == null) //默认处理接口,默认接口只处理EventCourierInterface消息
        {
            if(EventCourierInterface.class.isAssignableFrom(event.getClass()))
               handleSingleEventType(tCourierEventListenerBundle, event);//显示、隐式注册的默认接口（显示实现了总线接口）
        }
    }

    private void handleSingleMatchedEventType_M(@NotNull TCourierEventListenerBundle tCourierEventListenerBundle, @NotNull Object event) {
        TCourierSubscribe tCourierSubscribe = tCourierEventListenerBundle.getCourierSubscribe();
        if(tCourierSubscribe != null)
        {
            switch (tCourierSubscribe.threadMode()) {
                case MAIN:
                case MAIN_ORDERED:
                case BACKGROUND:
                case POSTING:
                case ASYNC:
                    if(tCourierEventListenerBundle.isParameterTypesMatched(event))
                       handleSingleEventType(tCourierEventListenerBundle, event);
                    break;
            }
        }
        else if(tCourierEventListenerBundle.getMethod() == null) //默认处理接口
        {
            if(EventCourierInterface.class.isAssignableFrom(event.getClass()))
               handleSingleEventType(tCourierEventListenerBundle, event);
        }
    }

    static class TCourierEventListenerBundle {
        private final Object courierEventListener;
        private final Method method;
        private final TCourierSubscribe tCourierSubscribe;
        private final Class<?>[] parameterTypes;

        public TCourierEventListenerBundle(Object courierEventListener, Method method, TCourierSubscribe tCourierSubscribe, Class<?>[] parameterTypes) {
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
            return parameterTypes != null
                    && parameterTypes[0] != null
                    && parameterTypes.length == 1
                    && parameterTypes[0].isAssignableFrom(event.getClass());
        }

        public String toToString()
        {
            String str = this.courierEventListener.getClass().getName();
            if(method != null) {
                if(parameterTypes != null) {
                    if(parameterTypes.length > 0)
                    str = str + " " + this.method.getName().toString() + "(" +
                            (parameterTypes[0].getSimpleName()) + ")";
                    else
                        str = str + " " + this.method.getName().toString() + "(" +
                                Arrays.toString(parameterTypes) + ")";
                }
                else
                    str = str + " " + this.method.getName().toString() + "(null)";
            }
            else
                str = str + "(null)";
            if(tCourierSubscribe != null)
                str = str+ "," + this.tCourierSubscribe.threadMode().toString();
            return str;
        }
    }

    @SuppressWarnings("ClassExplicitlyAnnotation")
    static class defaultSubscriber implements TCourierSubscribe {

        @Override
        public MethodThreadMode.threadMode threadMode() {
            return MethodThreadMode.threadMode.BACKGROUND;
        }

        @Override
        public Class<? extends Annotation> annotationType() {
            return null;
        }
    }

    public void free() {
        keepDoing = false;
        tTask.freeFree();
        InvokerList.clear();
    }

    private void printEventLog(String tag, @NotNull TCourierEventListenerBundle tCourierEventListenerBundle, @NotNull Object event)
    {
        //MMLog.d(TAG,tag+" ev:"+event.getClass().getSimpleName() +" -> EventListener:"+tCourierEventListenerBundleManager.toToString());
    }

    public void printAllEventListener()
    {
        for (Object obj : InvokerList.getAllObject()) {
            TCourierEventListenerBundle listenerBundle = (TCourierEventListenerBundle)obj;
            if(listenerBundle.getMethod() != null)
            MMLog.d(TAG,"EventListener:"+Modifier.toString(listenerBundle.getMethod().getModifiers())+" "+listenerBundle.toToString());
            else
                MMLog.d(TAG,"EventListener:？"+listenerBundle.toToString());
        }
    }
}
