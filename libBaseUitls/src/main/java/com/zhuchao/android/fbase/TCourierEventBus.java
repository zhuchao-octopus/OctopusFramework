package com.zhuchao.android.fbase;

import com.zhuchao.android.fbase.eventinterface.EventCourierInterface;
import com.zhuchao.android.fbase.eventinterface.InvokeInterface;
import com.zhuchao.android.fbase.eventinterface.TCourierEventBusInterface;
import com.zhuchao.android.fbase.eventinterface.TCourierEventListener;

import org.jetbrains.annotations.NotNull;

import java.lang.annotation.Annotation;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.locks.LockSupport;

/*////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
TCourierEventBus 是一个用于事件通信的类。在软件开发中，事件总线是一种用于简化组件间通信的模式。TCourierEventBus 提供了一种简单而强大的方式来处理事件的发布和订阅。
通过 TCourierEventBus，组件可以发布事件，而其他组件则可以订阅这些事件以接收通知并采取相应的行动。这种松散耦合的设计使得组件之间的通信更加灵活和可扩展。

TCourierEventBus 主要包含以下功能和特点：

事件发布与订阅: 组件可以通过 TCourierEventBus 发布各种类型的事件，如消息、状态变化等。其他组件可以注册并订阅这些事件，以便在事件发生时收到通知。
线程安全: TCourierEventBus 提供了线程安全的事件发布和订阅机制，可以确保多线程环境下的安全操作。
灵活的事件类型: TCourierEventBus 不限制事件类型，您可以定义任何您需要的事件类型，并在应用程序中使用它们进行通信。
轻量级: TCourierEventBus 被设计为轻量级的事件总线，以确保其在应用程序中的性能和内存开销都尽可能地小。
易于使用: 使用 TCourierEventBus 极为简单，您只需在需要发布或订阅事件的地方调用相应的方法即可。

总的来说，TCourierEventBus 是一个方便而强大的工具，可帮助您在应用程序中实现组件之间的松散耦合通信，提高代码的可维护性和可扩展性。
////////////////////////////////////////////////////////////////////////////////////////////////////////////////////*/
public class TCourierEventBus implements TCourierEventBusInterface,InvokeInterface {
    private final String TAG = "TCourierEventBus";
    ///private final String DEFAULT_EVENT_METHOD_NAME = "onCourierEvent";
    ///private final ObjectList EventTypeList = new ObjectList();
    //private InvokeInterface invokeInterface = null;
    private final ObjectList mInvokerList = new ObjectList();
    private final ArrayList<Object> mCourierEventsQueueA = new ArrayList<Object>();
    private final ArrayList<Object> mCourierEventsQueueB = new ArrayList<Object>();
    private final ArrayList<Object> mCourierEventsQueueMainA = new ArrayList<Object>();
    private final ArrayList<Object> mCourierEventsQueueMainB = new ArrayList<Object>();
    private boolean mCouriersLockQueueA = false;
    private boolean mCouriersLockQueueMA = false;
    private boolean mKeepDoing = true;
    private final TTask tTask;

    public TCourierEventBus() {
        ///InvokerList = new ObjectList();
        ///CourierEventsQueueA = new ArrayList<EventCourier>();
        ///CourierEventsQueueB = new ArrayList<EventCourier>();
        ///CourierEventsQueueMainA = new ArrayList<EventCourier>();
        ///CourierEventsQueueMainB = new ArrayList<EventCourier>();
        ///invokeInterface = this;
        mKeepDoing = true;
        tTask = new TTask(TAG, this);
    }

    /////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    //注册默认接口
    //@Deprecated
    public void registerEventObserver(@NotNull String tag, @NotNull TCourierEventListener courierEventListener) {
        ///if (InvokerList.containsTag(tagName)) return;
        ///TCourierEventListenerBundleManager tCourierEventListenerBundleManager = new TCourierEventListenerBundleManager((Object) courierEventListener, null, null, null);
        ///String tagName = tag+DEFAULT_EVENT_METHOD_NAME;
        ///InvokerList.addItem(tagName, tCourierEventListenerBundleManager);//显示注册默认接口

        findCourierEventTypeSubscriber(tag, courierEventListener);//分析当前类有无订阅者
        ///MMLog.d(TAG, "registerEventObserver -> " + courierEventListener.getClass().getName() + " tag = " + tag);
        //printAllEventListener();
    }

    /////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    //观察订阅
    public void registerEventObserver(@NotNull Object context) {
        ///if (InvokerList.containsTag(tag)) return;
        ///if(TCourierEventListener.class.isAssignableFrom(context.getClass()))//隐式注册默认接口
        ///{
        ///    String tagName = context.getClass().getName()+DEFAULT_EVENT_METHOD_NAME;
        ///    TCourierEventListenerBundleManager tCourierEventListenerBundleManager = new TCourierEventListenerBundleManager(context, null, null, null);
        ///    InvokerList.addItem(tagName, tCourierEventListenerBundleManager);
        ///}
        findCourierEventTypeSubscriber(null, context);//分析当前类有无订阅者
        ///MMLog.d(TAG, "registerEventObserver -> " + context.getClass().getName());
        ///printAllEventListener();
    }

    //@Deprecated
    public void unRegisterEventObserver(@NotNull String tag) {
        try {
            synchronized (mInvokerList) {
                mInvokerList.removeObjectsLike(tag);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void unRegisterEventObserver(@NotNull Object courierEventListener) {
        try {
            synchronized (mInvokerList) {
                mInvokerList.removeObjectsLike(courierEventListener.getClass().getName());
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public ObjectList getInvokerList() {
        return mInvokerList;
    }

    /////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    /////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    public void postMain(EventCourierInterface eventCourier) {
        if (mCouriersLockQueueMA) mCourierEventsQueueMainB.add(eventCourier);
        else mCourierEventsQueueMainA.add(eventCourier);
        try {
            if (tTask != null && !tTask.isBusy()) {
                mKeepDoing = true;
                tTask.start();
                //MMLog.log(TAG, "CourierEventBus main start...");
            }
        } catch (Exception e) {
            MMLog.log(TAG, "postMain event failed," + e.toString());
        }
        try {
            if (tTask != null) {
                //LockSupport.unpark(tTask);
                tTask.unPark();
            }
        } catch (Exception e) {
            MMLog.log(TAG, "postMain event failed," + e.toString());
        }
    }

    public void post(EventCourierInterface eventCourier) {
        if (mCouriersLockQueueA) mCourierEventsQueueB.add(eventCourier);
        else mCourierEventsQueueA.add(eventCourier);
        startBus();
    }

    public void post(Object eventCourier) {
        if (mCouriersLockQueueA) mCourierEventsQueueB.add(eventCourier);
        else mCourierEventsQueueA.add(eventCourier);
        startBus();
    }

    public void postDelay(Object eventCourier, long millis) {
        ThreadUtils.runThreadNotOnMainUIThread(new Runnable() {
            @Override
            public void run() {
                try {
                    Thread.sleep(millis);
                } catch (InterruptedException e) {
                    //e.printStackTrace();
                }
                post(eventCourier);
            }
        });
    }

    private void startBus() {
        try {
            if (tTask != null && !tTask.isBusy()) {
                mKeepDoing = true;
                tTask.start();
                //MMLog.log(TAG, "CourierEventBus start...");
            }
        } catch (Exception e) {
            MMLog.log(TAG, "start event bus failed," + e.toString());
        }
        try {
            if (tTask != null) {
                ///tTask.notifyAll();
                ///LockSupport.unpack(tTask);
                tTask.unPark();
            }
        } catch (Exception e) {
            MMLog.log(TAG, "start event bus failed," + e.toString());
        }
    }

    /////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    /////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    @Override
    public void CALLTODO(String tag) {
        ///MMLog.log(TAG,"CALLTODO "+ keepDoing);
        ///MMLog.log(TAG, "Courier Event Bus start...");
        while (mKeepDoing) {
            //if (CourierEventsQueueA != null && CourierEventsQueueB != null)
            {
                mCouriersLockQueueA = true;
                if (mCourierEventsQueueA.size() > 0) {
                    poolingEventInvokerList_ab(mCourierEventsQueueA);
                    mCourierEventsQueueA.clear();
                }
                mCouriersLockQueueA = false;
                if (mCourierEventsQueueB.size() > 0) {
                    poolingEventInvokerList_ab(mCourierEventsQueueB);
                    mCourierEventsQueueB.clear();
                }
            }

            //if (CourierEventsQueueMainA != null && CourierEventsQueueMainB != null)
            {
                mCouriersLockQueueMA = true;
                if (mCourierEventsQueueMainA.size() > 0) {
                    poolingEventInvokerList_abm(mCourierEventsQueueMainA);
                    mCourierEventsQueueMainA.clear();
                }
                mCouriersLockQueueMA = false;
                if (mCourierEventsQueueMainB.size() > 0) {
                    poolingEventInvokerList_abm(mCourierEventsQueueMainB);
                    mCourierEventsQueueMainB.clear();
                }
            }

            //MMLog.log(TAG,"A:"+couriers_A.size()+",B:"+couriers_B.size()+"AM:"+couriers_MA.size()+"BM:"+couriers_MB.size());
            if ((mCourierEventsQueueA.size() == 0) &&
                (mCourierEventsQueueB.size() == 0) &&
                (mCourierEventsQueueMainA.size() == 0) &&
                (mCourierEventsQueueMainB.size() <= 0)) //A已经解锁，B内容始终为空
            {
                try {
                    LockSupport.park();
                    //MMLog.log(TAG,"suspend!2");
                } catch (Exception e) {
                    //e.printStackTrace();
                    MMLog.log(TAG, e.toString());
                }
            }

        }//while (keepDoing)
        MMLog.log(TAG, "Courier Event Bus stopped...");
    }

    private void poolingEventInvokerList_ab(ArrayList<Object> couriers) {
        for (int i = 0; i < couriers.size(); i++) {
            String postToTag = null;
            Object eventCourier = couriers.get(i);
            if (eventCourier == null) continue; //couriers.remove(i);//丢弃
            if (EventCourierInterface.class.isAssignableFrom(eventCourier.getClass())) postToTag = ((EventCourierInterface) eventCourier).getTarget();

            if (postToTag != null) {
                List<Object> tCourierEventListenerBundleManagers = getDefaultCourierEventListeners(postToTag);
                for (Object obj : tCourierEventListenerBundleManagers) {
                    handleSingleMatchedEventType((TCourierEventListenerBundle) obj, eventCourier);
                }
            }
            else//如果为空，调用所有接口似广播
            {
                for (Object obj : mInvokerList.getAllObject()) {
                    handleSingleMatchedEventType((TCourierEventListenerBundle) obj, eventCourier);
                }
            }
        }
        couriers.clear();
    }

    private void poolingEventInvokerList_abm(ArrayList<Object> couriers) {
        for (int i = 0; i < couriers.size(); i++) {
            String postToTag = null;
            Object eventCourier = couriers.get(i);
            if (eventCourier == null) continue; //couriers.remove(i);//丢弃
            if (EventCourierInterface.class.isAssignableFrom(eventCourier.getClass())) postToTag = ((EventCourierInterface) eventCourier).getTarget();

            if (postToTag == null) { //如果为空，调用所有接口，类似广播
                for (Object obj : mInvokerList.getAllObject()) {
                    ThreadUtils.runOnMainUiThread(new Runnable() {
                        @Override
                        public void run() {
                            handleSingleMatchedEventType_M((TCourierEventListenerBundle) obj, eventCourier);
                        }
                    });
                }
            } else {
                List<Object> tCourierEventListenerBundleManagers = getDefaultCourierEventListeners(postToTag);
                for (Object obj : tCourierEventListenerBundleManagers) {
                    ThreadUtils.runOnMainUiThread(new Runnable() {
                        @Override
                        public void run() {
                            handleSingleMatchedEventType_M((TCourierEventListenerBundle) obj, eventCourier);
                        }
                    });
                }
            }
        }
        couriers.clear();
    }

    private List<Object> getDefaultCourierEventListeners(String tag) {
        return mInvokerList.getObjectsLike(tag);
    }

    /////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    //查找订阅的方法
    private void findCourierEventTypeSubscriber(String tag, Object courierEventListener) {
        Method[] methods = courierEventListener.getClass().getMethods();
        //Method[] methods = courierEventListener.getClass().getDeclaredMethods();
        for (int i = 0; i < methods.length - 1; i++) {
            TCourierSubscribe tCourierSubscribe = methods[i].getAnnotation(TCourierSubscribe.class);
            Class<?>[] classes = methods[i].getParameterTypes();
            if (classes.length != 1) continue;

            String pName = classes[0].getSimpleName();
            pName = pName +"."+ courierEventListener.hashCode();

            if (tCourierSubscribe != null && (Modifier.toString(methods[i].getModifiers()).contains("public"))) //显示的订阅了总线接口 && courierInterfaceParameterTypes.length == 1
            {//显示订阅
                methods[i].setAccessible(true);
                TCourierEventListenerBundle tCourierEventListenerBundle = new TCourierEventListenerBundle(courierEventListener, methods[i], tCourierSubscribe, classes);
                if (FileUtils.NotEmptyString(tag))
                    mInvokerList.addItem(tag +"."+ methods[i].getName()  +"."+ pName, tCourierEventListenerBundle);
                else
                    mInvokerList.addItem(courierEventListener.getClass().getName() + methods[i].getName() + pName, tCourierEventListenerBundle);
                //MMLog.i(TAG, tCourierEventListenerBundleManager.toToString());
            }
            //else if(methods[i].getName().equals(DEFAULT_EVENT_METHOD_NAME))//默认方法名特殊处理,自动匹配订阅(隐式订阅)
            //对参数实现EventCourierInterface接口的方法进行隐式订阅
            else if (EventCourierInterface.class.isAssignableFrom(classes[0]) && (Modifier.toString(methods[i].getModifiers()).contains("public"))) {
                methods[i].setAccessible(true);
                TCourierEventListenerBundle listenerBundleManager = new TCourierEventListenerBundle(courierEventListener, methods[i], new defaultSubscriber(), classes);
                if (FileUtils.NotEmptyString(tag))//自动增加匹配接口默认方法，无需显示申明接口
                    mInvokerList.addItem(tag +"."+ methods[i].getName()  +"."+ pName, listenerBundleManager);
                else
                    mInvokerList.addItem(courierEventListener.getClass().getName() + methods[i].getName() + pName, listenerBundleManager);
            }
        }
    }

    private void handleSingleEventType(@NotNull TCourierEventListenerBundle tCourierEventListenerBundle, @NotNull Object event) {
        //Class<?>[] listenerParameterTypes = tCourierEventListenerBundleManager.parameterTypes;
        try {
            Object context = tCourierEventListenerBundle.getCourierEventListener();
            if (context != null) {
                if (tCourierEventListenerBundle.getMethod() != null) {//优先考虑订阅方法,等效if(tCourierSubscribe != null)//订阅的消息接口
                    printEventLog("subscriber " + tCourierEventListenerBundle.getMethod().getName(), tCourierEventListenerBundle, event);
                    tCourierEventListenerBundle.getMethod().invoke(context, event);//呼叫指定订阅方法
                } else if (TCourierEventListener.class.isAssignableFrom(context.getClass())) {//呼叫默认接口
                    printEventLog("default onCourierEvent", tCourierEventListenerBundle, event);
                    ((TCourierEventListener) context).onCourierEvent((EventCourierInterface) event);//call 默认接口onCourierEvent(event);
                }
            }
        } catch (IllegalAccessException | InvocationTargetException e) {
            e.printStackTrace();
            ///MMLog.e(TAG, tCourierEventListenerBundle.toToString());
            ///MMLog.e(TAG,  event.getClass().getSimpleName()+","+e.toString());
        }
    }

    private void handleSingleMatchedEventType(@NotNull TCourierEventListenerBundle tCourierEventListenerBundle, @NotNull Object event) {
        //Class<?>[] listenerParameterTypes = tCourierEventListenerBundleManager.parameterTypes;
        TCourierSubscribe tCourierSubscribe = tCourierEventListenerBundle.getCourierSubscribe();
        if (tCourierSubscribe != null)//处理显示订阅的消息接口&&tCourierEventListenerBundleManager.getMethod() != null
        {
            switch (tCourierSubscribe.threadMode()) {
                case MAIN:
                case MAIN_ORDERED:
                    if (tCourierEventListenerBundle.isParameterTypesMatched(event)) ThreadUtils.runOnMainUiThread(new Runnable() {
                        @Override
                        public void run() {
                            handleSingleEventType(tCourierEventListenerBundle, event);
                        }
                    });
                    break;
                case BACKGROUND:
                    if (tCourierEventListenerBundle.isParameterTypesMatched(event)) ThreadUtils.runThreadNotOnMainUIThread(new Runnable() {
                        @Override
                        public void run() {
                            handleSingleEventType(tCourierEventListenerBundle, event);
                        }
                    });
                    break;
                case POSTING:
                case ASYNC:
                    if (tCourierEventListenerBundle.isParameterTypesMatched(event)) handleSingleEventType(tCourierEventListenerBundle, event);
                    break;
            }
        } else if (tCourierEventListenerBundle.getMethod() == null) //默认处理接口,默认接口只处理EventCourierInterface消息
        {
            if (EventCourierInterface.class.isAssignableFrom(event.getClass()))
                handleSingleEventType(tCourierEventListenerBundle, event);//显示、隐式注册的默认接口（显示实现了总线接口）
        }
    }

    private void handleSingleMatchedEventType_M(@NotNull TCourierEventListenerBundle tCourierEventListenerBundle, @NotNull Object event) {
        TCourierSubscribe tCourierSubscribe = tCourierEventListenerBundle.getCourierSubscribe();
        if (tCourierSubscribe != null) {
            switch (tCourierSubscribe.threadMode()) {
                case MAIN:
                case MAIN_ORDERED:
                case BACKGROUND:
                case POSTING:
                case ASYNC:
                    if (tCourierEventListenerBundle.isParameterTypesMatched(event)) handleSingleEventType(tCourierEventListenerBundle, event);
                    break;
            }
        } else if (tCourierEventListenerBundle.getMethod() == null) //默认处理接口
        {
            if (EventCourierInterface.class.isAssignableFrom(event.getClass())) handleSingleEventType(tCourierEventListenerBundle, event);
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

        public boolean isParameterTypesMatched(Object event) {
            //&& listenerParameterTypes != null
            //&& listenerParameterTypes[0] != null
            //&& listenerParameterTypes[0].isAssignableFrom(event.getClass())
            //Class<?>[] listenerParameterTypes = tCourierEventListenerBundleManager.parameterTypes;
            return parameterTypes != null && parameterTypes[0] != null && parameterTypes.length == 1 && parameterTypes[0].isAssignableFrom(event.getClass());
        }

        public String toToString() {
            String str = this.courierEventListener.getClass().getName();
            if (method != null) {
                if (parameterTypes != null) {
                    if (parameterTypes.length > 0)
                        str = str + " " + this.method.getName() + "(" + (parameterTypes[0].getSimpleName()) + ")";
                    else str = str + " " + this.method.getName() + "(" + Arrays.toString(parameterTypes) + ")";
                } else str = str + " " + this.method.getName() + "(null)";
            } else str = str + "(null)";
            if (tCourierSubscribe != null) str = str + "," + this.tCourierSubscribe.threadMode().toString();
            return str;
        }
    }

    /////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
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
        mKeepDoing = false;
        tTask.freeFree();
        mInvokerList.clear();
    }

    private void printEventLog(String tag, @NotNull TCourierEventListenerBundle tCourierEventListenerBundle, @NotNull Object event) {
        //MMLog.d(TAG,tag+" ev:"+event.getClass().getSimpleName() +" -> EventListener:"+tCourierEventListenerBundleManager.toToString());
    }
    public void printInvokerList() {
        mInvokerList.printAll();
    }
    public void printEventListener() {
        for (Object obj : mInvokerList.getAllObject()) {
            TCourierEventListenerBundle listenerBundle = (TCourierEventListenerBundle) obj;
            if (listenerBundle.getMethod() != null)
                MMLog.d(TAG, "EventListener:" + Modifier.toString(listenerBundle.getMethod().getModifiers()) + " " + listenerBundle.toToString());
            else
                MMLog.d(TAG, "EventListener:？" + listenerBundle.toToString());
        }
    }
    public String getEventListeners()
    {
        StringBuilder stringBuffer = new StringBuilder();
        String line = System.getProperty("line.separator");
        for (Object obj : mInvokerList.getAllObject()) {
            TCourierEventListenerBundle listenerBundle = (TCourierEventListenerBundle) obj;
            if (listenerBundle.getMethod() != null)
                stringBuffer.append("*EventListener:").append(Modifier.toString(listenerBundle.getMethod().getModifiers())).append(" ").append(listenerBundle.toToString()).append(line);
            else
                stringBuffer.append("*EventListener:？").append(listenerBundle.toToString());
        }
        return stringBuffer.toString();
    }
}
