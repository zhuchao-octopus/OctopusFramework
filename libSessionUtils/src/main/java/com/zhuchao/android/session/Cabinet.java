package com.zhuchao.android.session;

import static android.content.Context.BIND_AUTO_CREATE;

import static com.zhuchao.android.fbase.MessageEvent.MESSAGE_EVENT_AIDL_PACKAGE_NAME;
import static com.zhuchao.android.fbase.MessageEvent.MESSAGE_EVENT_OCTOPUS_ACTION_CAR_CLIENT;
import static com.zhuchao.android.fbase.MessageEvent.MESSAGE_EVENT_OCTOPUS_ACTION_CAR_SERVICE;
import static com.zhuchao.android.fbase.MessageEvent.MESSAGE_EVENT_OCTOPUS_CAR_CLIENT;
import static com.zhuchao.android.fbase.MessageEvent.MESSAGE_EVENT_OCTOPUS_CAR_SERVICE;

import android.annotation.SuppressLint;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.IBinder;
import android.os.RemoteException;

import com.zhuchao.android.car.IMyCarAidlInterface;
import com.zhuchao.android.car.IMyCarAidlInterfaceListener;
import com.zhuchao.android.car.PEventCourier;
import com.zhuchao.android.fbase.DataID;
import com.zhuchao.android.fbase.EventCourier;
import com.zhuchao.android.fbase.MMLog;
import com.zhuchao.android.fbase.MessageEvent;
import com.zhuchao.android.fbase.Msg;
import com.zhuchao.android.fbase.TAppProcessUtils;
import com.zhuchao.android.fbase.TAppUtils;
import com.zhuchao.android.fbase.TCourierEventBus;
import com.zhuchao.android.fbase.eventinterface.TCourierEventBusInterface;
import com.zhuchao.android.net.TNetUtils;
import com.zhuchao.android.persist.TPersistent;

import org.jetbrains.annotations.NotNull;

public class Cabinet {
    private static final String TAG = "Cabinet";
    @SuppressLint("StaticFieldLeak")
    private static TPlayManager tPlayManager = null;
    @SuppressLint("StaticFieldLeak")
    private static TTaskManager tTaskManager = null;
    @SuppressLint("StaticFieldLeak")
    private static TNetUtils tNetUtils = null;
    @SuppressLint("StaticFieldLeak")
    private static TAppUtils tAppUtils = null;
    @SuppressLint("StaticFieldLeak")
    private static TDeviceManager tDeviceManager = null;
    @SuppressLint("StaticFieldLeak")
    private static TPersistent tPersistent = null;
    private static TCourierEventBusInterface tCourierEventBus = null;

    private static IMyCarAidlInterface tIMyCarAidlInterface = null;

    public static synchronized void initialEventBus() {
        if (tCourierEventBus == null) tCourierEventBus = new TCourierEventBus();
    }

    public synchronized static void initialPlayManager(Context context) {
        tPlayManager = TPlayManager.getInstance(context);
        tPlayManager.setPlayOrder(DataID.PLAY_MANAGER_PLAY_ORDER2);//循环顺序播放
        tPlayManager.setAutoPlaySource(DataID.SESSION_SOURCE_FAVORITELIST);//自动播放源列表
        tPlayManager.updateMediaLibrary();
    }

    public static synchronized void initialTaskManager(Context context) {
        if (tTaskManager == null) {
            tTaskManager = new TTaskManager(context);
        }
    }

    public synchronized static TPlayManager getPlayManager() {
        tPlayManager = TPlayManager.getInstance(MApplication.getAppContext());
        tPlayManager.setPlayOrder(DataID.PLAY_MANAGER_PLAY_ORDER2);//循环顺序播放
        tPlayManager.setAutoPlaySource(DataID.SESSION_SOURCE_FAVORITELIST);//自动播放源列表
        return tPlayManager;
    }

    public synchronized TPersistent getPersistent(Context context) {
        if (tPersistent == null) return new TPersistent(context, context.getPackageName());
        else return tPersistent;
    }

    public static synchronized TCourierEventBusInterface getEventBus() {
        if (tCourierEventBus == null) tCourierEventBus = new TCourierEventBus();
        return tCourierEventBus;
    }

    public static synchronized TTaskManager getTaskManager(Context context) {
        if (tTaskManager == null) {
            tTaskManager = new TTaskManager(context);
        }
        return tTaskManager;
    }

    public static synchronized void InitialAllModules(@NotNull Context context) {
        //MMLog.d(TAG, "Initial all modules for " + TAppProcessUtils.getCurrentProcessNameAndId(context) + " ");
        try {
            initialMyCarAidlInterface(context);
            initialEventBus();
            initialTaskManager(context);
            initialPlayManager(context);
        } catch (Exception e) {
            MMLog.e(TAG, e.toString());
        }
    }
    public static synchronized void InitialBaseModules(@NotNull Context context) {
        //MMLog.d(TAG, "Initial few modules for " + TAppProcessUtils.getCurrentProcessNameAndId(context) + " ");
        try {
            initialEventBus();
            initialTaskManager(context);
        } catch (Exception e) {
            MMLog.e(TAG, e.toString());
        }
    }
    public static synchronized void FreeModules(@NotNull Context context) {
        try {
            if (tPlayManager != null) tPlayManager.free();
            if (tTaskManager != null) tTaskManager.free();
            if (tNetUtils != null) tNetUtils.free();
            if (tDeviceManager != null) tDeviceManager.closeAllUartDevice();
            if (tCourierEventBus != null) tCourierEventBus.free();
            if (tPersistent != null) tPersistent.commit();//持久化数据
            if (tAppUtils != null) tAppUtils.free();
            if (tIMyCarAidlInterface != null)disconnectedMyCarAidlService(context);
        } catch (Exception e) {
            //e.printStackTrace();
        }
    }
    ////////////////////////////////////////////////////////////////////////////////////////////////////////
    ////////////////////////////////////////////////////////////////////////////////////////////////////////
    //aidl
    private static final IMyCarAidlInterfaceListener mIMyCarAidlInterfaceListener = new IMyCarAidlInterfaceListener.Stub() {

        @Override
        public void onMessageCarAidlInterface(PEventCourier msg) throws RemoteException {
             MMLog.d(TAG, msg.toStr());
        }
    };
    private static final ServiceConnection mServiceConnection = new ServiceConnection() {

        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            tIMyCarAidlInterface = IMyCarAidlInterface.Stub.asInterface(service);
            try {
                tIMyCarAidlInterface.registerListener(mIMyCarAidlInterfaceListener);
            } catch (RemoteException e) {
                //throw new RuntimeException(e);
            }
            MMLog.d(TAG, "Connect to MMCarService proxy successfully!");
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            tIMyCarAidlInterface = null;
            MMLog.d(TAG, "MMCarService onServiceDisconnected!");
        }
    };
    ////////////////////////////////////////////////////////////////////////////////////////////////////////
    public static void disconnectedMyCarAidlService(Context context)
    {
       if(tIMyCarAidlInterface != null && tIMyCarAidlInterface.asBinder().isBinderAlive())
       {
           try {
               context.unbindService(mServiceConnection);
               tIMyCarAidlInterface.unregisterListener(mIMyCarAidlInterfaceListener);
               tIMyCarAidlInterface = null;
           } catch (RemoteException e) {
               //throw new RuntimeException(e);
           }
       }
    }
    public static void initialMyCarAidlInterface(Context context) {
        ///Intent intent = new Intent(mContext, MMCarService.class);
        //MMLog.d(TAG,"initial MMCarService proxy");
        Intent intent = new Intent("com.zhuchao.android.car.action.MMCarService");
        //intent.setPackage(mContext.getPackageName());
        intent.setComponent(new ComponentName("com.zhuchao.android.car", "com.zhuchao.android.car.service.MMCarService"));
        //Intent newIntent = new Intent(createExplicitFromImplicitIntent(mContext,intent));
        boolean isBound= context.bindService(intent, mServiceConnection, BIND_AUTO_CREATE);
        if(!isBound)
            MMLog.d(TAG,"Connect to MMCarService proxy failed!");
    }

    public static IMyCarAidlInterface getMyCarAidlInterface() {
        return tIMyCarAidlInterface;
    }

    public static <T> T getMyCarAidlInterface(Class<T> classOfT) {
        if (tIMyCarAidlInterface != null) return classOfT.cast(tIMyCarAidlInterface);
        else return null;
    }

    public static void testMyCarAidlInterface()  {
       if(tIMyCarAidlInterface!= null) {
           try {
               MMLog.d(TAG,tIMyCarAidlInterface.getHelloData());
           } catch (RemoteException e) {
               //throw new RuntimeException(e);
               MMLog.d(TAG,"MyCarAidlInterface "+e);
           }
       }
       else
           MMLog.d(TAG,"MyCarAidlInterface is null!");
    }

    public static void IAidlSendMessage(PEventCourier pEventCourier)  {
        if(tIMyCarAidlInterface!= null)
        {
            try {
                tIMyCarAidlInterface.sendMessage(pEventCourier);
            } catch (RemoteException e) {
                //throw new RuntimeException(e);
                MMLog.d(TAG,"ICarAidlSend "+e);
            }
        }
        else {
            MMLog.d(TAG, "ICarAidlSend is null!");
        }
    }

    public static void IAidlSendMessage(EventCourier eventCourier)
    {
        String action = MessageEvent.MESSAGE_EVENT_OCTOPUS_ACTION_HELLO;
        Intent intent = new Intent();
        switch (eventCourier.getId())
        {
            case MESSAGE_EVENT_OCTOPUS_CAR_SERVICE:
                action = MESSAGE_EVENT_OCTOPUS_ACTION_CAR_SERVICE;
                intent.setAction(action);
                intent.setPackage(MESSAGE_EVENT_AIDL_PACKAGE_NAME);
                MApplication.getAppContext().sendBroadcast(intent);
                break;
            case MESSAGE_EVENT_OCTOPUS_CAR_CLIENT:
                action = MESSAGE_EVENT_OCTOPUS_ACTION_CAR_CLIENT;
                intent.setAction(action);
                ///intent.setPackage(MApplication.getAppContext().getPackageName());
                ///intent.setComponent(new ComponentName("com.zhuchao.android.car", "com.zhuchao.android.session.GlobalBroadcastReceiver"));
                MApplication.getAppContext().sendBroadcast(intent);
                break;
        }

    }
}
