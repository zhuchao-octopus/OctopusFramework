package com.zhuchao.android.session;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;

import com.zhuchao.android.car.aidl.PEventCourier;
import com.zhuchao.android.fbase.DataID;
import com.zhuchao.android.fbase.FileUtils;
import com.zhuchao.android.fbase.MMLog;
import com.zhuchao.android.fbase.MessageEvent;
import com.zhuchao.android.fbase.MethodThreadMode;
import com.zhuchao.android.fbase.ObjectList;
import com.zhuchao.android.fbase.TCourierSubscribe;
import com.zhuchao.android.fbase.TTask;
import com.zhuchao.android.fbase.eventinterface.EventCourierInterface;
import com.zhuchao.android.fbase.eventinterface.SessionCallback;
import com.zhuchao.android.video.OMedia;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


public class TMediaManager implements SessionCallback {
    private final String TAG = "TMediaManager";
    @SuppressLint("StaticFieldLeak")
    private static Context mContext = null;
    private SessionCallback mUserSessionCallback = null;
    private final ObjectList mAllSessions = new ObjectList();//存储所有的分类
    private final LiveVideoSession mNetCategorySession = new LiveVideoSession(DataID.SESSION_SOURCE_NONE, null);//网络会话
    private final LiveVideoSession mLocalVideoSession = new LiveVideoSession(null);
    private final LiveVideoSession mLocalAudioSession = new LiveVideoSession(null);
    ///private final LiveVideoSession mMobileUSBVideoSession = new LiveVideoSession(null);
    ///private final LiveVideoSession mMobileUSBAudioSession = new LiveVideoSession(null);
    private final LiveVideoSession mSDVideoSession = new LiveVideoSession(null);
    private final LiveVideoSession mSDAudioSession = new LiveVideoSession(null);
    private final LiveVideoSession mFileVideoSession = new LiveVideoSession(null);
    private final LiveVideoSession mFileAudioSession = new LiveVideoSession(null);

    ///private int mMobileSessionId = DataID.SESSION_SOURCE_LOCAL_INTERNAL;
    private Map<String, String> mMobileUSBDiscs = new HashMap<String, String>();
    ///private GlobalBroadcastReceiver mUSBBroadcastReceiver = null;//new USBReceiver();
    ///private MyBroadcastReceiver mFileReceiver = null;//new USBReceiver();
    private boolean mThreadLock1 = false;
    private boolean mThreadLock2 = false;
    private boolean mThreadLock3 = false;
    ///private int initStage = 0;//0 从网络， 1 //从本地, >=3 已经初始化完成
    private TMediaManager tMediaManager = null;

    public synchronized TMediaManager getInstance(Context context) {
        if (tMediaManager == null) tMediaManager = new TMediaManager(context, null);
        return tMediaManager;
    }

    @SuppressLint("SdCardPath")
    public void updateMedias() {
        //setUserSessionCallback(sessionCallback);
        initSessionFromLocal();
        initSessionFromMobileDisc();//usb
        ///initSDSessionFromPath();
    }

    public TMediaManager(Context context, SessionCallback sessionCallback) {
        this.mUserSessionCallback = sessionCallback;
        mContext = context;
        Cabinet.getEventBus().registerEventObserver(this); //this!=mContext
        ///GlobalBroadcastReceiver.registerGlobalBroadcastReceiver(mContext);
        ///registerUSBBroadcastReceiver();
        ///Data.setOplayerSessionRootUrl(hostPath);
        ///mCategorySession = new LiveVideoSession(DataID.SESSION_SOURCE_NONE, this);
        mNetCategorySession.setUserSessionCallback(this);
    }

    public void setUserSessionCallback(SessionCallback mUserSessionCallback) {
        this.mUserSessionCallback = mUserSessionCallback;
    }

    public TMediaManager Callback(SessionCallback userSessionCallback) {
        this.mUserSessionCallback = userSessionCallback;
        return this;
    }

    public ObjectList getSessions() {
        return mAllSessions;
    }

    public LiveVideoSession getCategorySession() {
        return mNetCategorySession;
    }

    public LiveVideoSession getLocalVideoSession() {
        return mLocalVideoSession;
    }

    public LiveVideoSession getLocalAudioSession() {
        return mLocalAudioSession;
    }

    public LiveVideoSession getUSBVideoSession() {
        LiveVideoSession liveVideoSession = new LiveVideoSession(null);
        if (mMobileUSBDiscs.size() > 1) {
            for (Map.Entry<String, String> entry : mMobileUSBDiscs.entrySet())
            {
                Object obj = mAllSessions.get(makeSessionName(entry.getKey(), DataID.MEDIA_TYPE_ID_VIDEO));
                if (obj != null) {
                    LiveVideoSession lvs = (LiveVideoSession) obj;
                    liveVideoSession.addVideos(lvs.getAllVideos());
                }
            }
        }
        else
        {
            for (Map.Entry<String, String> entry : mMobileUSBDiscs.entrySet())
            {
                Object obj = mAllSessions.get(makeSessionName(entry.getKey(), DataID.MEDIA_TYPE_ID_VIDEO));
                if (obj != null) liveVideoSession = (LiveVideoSession) obj;
            }
        }
        return liveVideoSession;
    }

    public LiveVideoSession getUSBAudioSession() {
        LiveVideoSession liveVideoSession = new LiveVideoSession(null);
        if (mMobileUSBDiscs.size() > 1) {
            for (Map.Entry<String, String> entry : mMobileUSBDiscs.entrySet())
            {
                Object obj = mAllSessions.get(makeSessionName(entry.getKey(), DataID.MEDIA_TYPE_ID_AUDIO));
                if (obj != null) {
                    LiveVideoSession lvs = (LiveVideoSession) obj;
                    liveVideoSession.addVideos(lvs.getAllVideos());
                }
            }
        }
        else
        {
            for (Map.Entry<String, String> entry : mMobileUSBDiscs.entrySet()) {
                Object obj = mAllSessions.get(makeSessionName(entry.getKey(), DataID.MEDIA_TYPE_ID_AUDIO));
                if (obj != null) liveVideoSession = (LiveVideoSession) obj;
            }
        }
        return liveVideoSession;
    }

    public LiveVideoSession getSDVideoSession() {
        return mSDVideoSession;
    }

    public LiveVideoSession getSDAudioSession() {
        return mSDAudioSession;
    }

    public LiveVideoSession getFileVideoSession() {
        return mFileVideoSession;
    }

    public LiveVideoSession getFileAudioSession() {
        return mFileAudioSession;
    }

    public Map<String, String> getMobileUSBDiscs() {
        return mMobileUSBDiscs;
    }

    /////////////////////////////////////////////////////////////////////////////////////
    /////////////////////////////////////////////////////////////////////////////////////

    public void initSessionFromLocal() {
        if (mThreadLock1) return;
        new Thread() {
            public void run() {
                mThreadLock1 = true;
                //MMLog.log(TAG, "init Local SessionContent 本地媒体库");
                LiveVideoSession lSession = null;

                ///lSession = new LiveVideoSession(TMediaManager.this);
                mLocalVideoSession.initMediasFromLocal(mContext, DataID.MEDIA_TYPE_ID_VIDEO);
                addLocalSessionToSessions("本地视频", mLocalVideoSession);
                userSessionCallback(mLocalVideoSession, MessageEvent.MESSAGE_EVENT_LOCAL_VIDEO, "本地视频");

                ///lSession = new LiveVideoSession(TMediaManager.this);
                mLocalAudioSession.initMediasFromLocal(mContext, DataID.MEDIA_TYPE_ID_AUDIO);
                addLocalSessionToSessions("本地音乐", mLocalAudioSession);
                userSessionCallback(mLocalAudioSession, MessageEvent.MESSAGE_EVENT_LOCAL_AUDIO, "本地音乐");

                mThreadLock1 = false;
            }
        }.start();
    }

    //第一次重置式初始化
    public void initSessionFromMobileDisc() {
        if (mThreadLock2) return;
        ///if (mUSBBroadcastReceiver == null) registerUSBBroadcastReceiver();
        new Thread() {
            public void run() {
                mThreadLock2 = true;
                mMobileUSBDiscs = FileUtils.getUDiscName(mContext);
                if (mMobileUSBDiscs.isEmpty()) {
                    ///MMLog.log(TAG, "No usb memory was found!");
                    ///mMobileUSBVideoSession.getAllVideos().clear();
                    ///mMobileUSBAudioSession.getAllVideos().clear();
                    ///userSessionCallback(mMobileUSBVideoSession, MessageEvent.MESSAGE_EVENT_USB_VIDEO, null);
                    ///userSessionCallback(mMobileUSBAudioSession, MessageEvent.MESSAGE_EVENT_USB_AUDIO, null);
                    mThreadLock2 = false;
                    return;
                }
                ///移除所有本地移动分类
                /*
                Iterator<Map.Entry<Integer, LiveVideoSession>> it = mAllSessions.entrySet().iterator();
                while (it.hasNext()) {
                    Map.Entry<Integer, LiveVideoSession> entry = (Map.Entry<Integer, LiveVideoSession>) it.next();
                    if (entry.getKey() < DataID.SESSION_SOURCE_LOCAL_INTERNAL) it.remove();
                }*/
                ///mMobileUSBVideoSession.getAllVideos().clear();
                ///mMobileUSBAudioSession.getAllVideos().clear();
                for (Map.Entry<String, String> entry : mMobileUSBDiscs.entrySet()) {
                    singleTaskSearchMobileDisc(entry.getKey(), entry.getValue());
                }
                mThreadLock2 = false;
            }
        }.start();
    }

    @SuppressLint("SdCardPath")
    private void initSDSessionFromPath() {//"/sdcard/"
        if (mThreadLock3) return;
        String path = "/sdcard/";
        new Thread() {
            public void run() {
                mThreadLock3 = true;
                mSDVideoSession.getAllVideos().clear();
                mSDVideoSession.initMediasFromPath(mContext, path, DataID.MEDIA_TYPE_ID_VIDEO);
                userSessionCallback(mSDVideoSession, MessageEvent.MESSAGE_EVENT_SD_VIDEO, path);
                mSDAudioSession.getAllVideos().clear();
                mSDAudioSession.initMediasFromPath(mContext, path, DataID.MEDIA_TYPE_ID_AUDIO);
                userSessionCallback(mSDAudioSession, MessageEvent.MESSAGE_EVENT_SD_AUDIO, path);
                mThreadLock3 = false;
            }
        }.start();
    }

    private void initSessionFromPath(final String path) {
        if (mThreadLock3) return;
        if (path == null) return;
        new Thread() {
            public void run() {
                mThreadLock3 = true;
                mFileVideoSession.clear();
                mFileVideoSession.initMediasFromPath(mContext, path, DataID.MEDIA_TYPE_ID_AUDIO_VIDEO);
                userSessionCallback(mFileVideoSession, MessageEvent.MESSAGE_EVENT_FILES, path);
                mThreadLock3 = false;
            }
        }.start();
    }

    public void scanFilesFromPath(Context context, String filePath, List<String> FileList) {
        if (mThreadLock3) return;
        if (filePath == null) return;
        //if (FileList == null) return;
        if (!FileUtils.existDirectory(filePath)) return;
        //registerFileBroadcastReceiver();
        mFileVideoSession.clear();
        new Thread() {
            public void run() {
                mThreadLock3 = true;
                mFileVideoSession.initFilesFromPath(mContext, filePath, FileList);
                userSessionCallback(mFileVideoSession, MessageEvent.MESSAGE_EVENT_FILES, filePath);
                mThreadLock3 = false;
            }
        }.start();
    }

    private void initMobileSessionContent(final String DeviceName, final String DevicePath) {
        ///MMLog.log(TAG, "initMobileSessionContent  " + DeviceName + ":" + DevicePath);
        if (FileUtils.EmptyString(DeviceName) || !FileUtils.existDirectory(DevicePath)) return;

        LiveVideoSession mSession = new LiveVideoSession(TMediaManager.this);
        LiveVideoSession mMobileUSBVideoSession = new LiveVideoSession(TMediaManager.this);
        LiveVideoSession mMobileUSBAudioSession = new LiveVideoSession(TMediaManager.this);

        mSession.initMediasFromPath(mContext, DevicePath, DataID.MEDIA_TYPE_ID_AUDIO_VIDEO, mMobileUSBVideoSession, mMobileUSBAudioSession);
        addLocalSessionToSessions(makeSessionName(DeviceName,DataID.MEDIA_TYPE_ID_VIDEO), mMobileUSBVideoSession);
        addLocalSessionToSessions(makeSessionName(DeviceName,DataID.MEDIA_TYPE_ID_AUDIO), mMobileUSBAudioSession);

        userSessionCallback(mMobileUSBVideoSession, MessageEvent.MESSAGE_EVENT_USB_VIDEO, DeviceName + ":" + DevicePath);
        userSessionCallback(mMobileUSBAudioSession, MessageEvent.MESSAGE_EVENT_USB_AUDIO, DeviceName + ":" + DevicePath);
    }

    private void userSessionCallback(LiveVideoSession liveVideoSession, int mobileSessionId, String message)//DeviceName + ":" + DevicePath
    {
        if (mUserSessionCallback != null) {///通知本地播放器
            if (liveVideoSession != null)
                mUserSessionCallback.OnSessionComplete(mobileSessionId, message, liveVideoSession.getAllVideos().getCount());
            else mUserSessionCallback.OnSessionComplete(mobileSessionId, message, 0);
        }
        ///通知本地总线
        ///Cabinet.getEventBus().post(new EventCourier(this.getClass(), mobileSessionId));
        Cabinet.getEventBus().post(new PEventCourier(this.getClass(), mobileSessionId));//通知IPC,通知外部客户端
    }

    ///增加本地分类
    private void addNetSessionToSessions(int SessionID, String name, LiveVideoSession Session) {
        Session.getVideoCategoryNameList().put(SessionID, name);//备注名称
        mNetCategorySession.getVideoCategoryNameList().remove(SessionID);
        mNetCategorySession.getVideoCategoryNameList().put(SessionID, name);
        mAllSessions.remove(name);
        mAllSessions.addObject(name, Session);///put(SessionID, Session);
        ///mMobileSessionId--;
    }

    private void addLocalSessionToSessions(String name, LiveVideoSession Session) {
        mAllSessions.remove(name);
        mAllSessions.addObject(name, Session);///put(SessionID, Session);
    }

    //初始化网络分类
    private void initInternetCategorySessions() {
        for (Map.Entry<Integer, String> entry : mNetCategorySession.getVideoCategoryNameList().entrySet()) {
            if (entry.getKey() > DataID.SESSION_SOURCE_LOCAL_INTERNAL)//初始化非本地分类
            {
                LiveVideoSession liveVideoSession = new LiveVideoSession(this);
                liveVideoSession.getVideoCategoryNameList().put(entry.getKey(), entry.getValue());
                mAllSessions.addObject(String.valueOf(entry.getKey()), liveVideoSession);
            }
        }
    }

    private void updateCategorySession() { //顶层分类信息，顶层分类会话

    }

    private void updateCategorySessionContent() {//获取分类下面的内容
        try {
            for (Map.Entry<String, Object> entry : mAllSessions.getAll().entrySet()) {
                MMLog.log(TAG, "updateCategorySessionContent = " + entry.getKey());
                //entry.getValue().doUpdateSession(entry.getKey());
            }
            ///initStage = 3;
            ///MLog.logTAG, "initPlaySessionContent mIniType = " + mIniType);
        } catch (Exception e) {
            //MLog.logTAG, "initPlaySessionContent fail mIniType = " + mIniType + ":" + e.toString());
            e.printStackTrace();
        }
    }

    public List<OMedia> getAllMedias() {
        List<OMedia> allOMedia = new ArrayList<>();
        for (Map.Entry<String, Object> entry : mAllSessions.getAll().entrySet()) {
            /*
            MLog.logTAG, "printSessionsVideoList " + entry.getKey() + " : " + entry.getValue().getVideoCategoryNameList().get(entry.getKey())
                    + " Movies Count =" + entry.getValue().getVideos().size());
            */
            for (HashMap.Entry<String, Object> m : ((LiveVideoSession) entry.getValue()).getAllVideos().getMap().entrySet())
                allOMedia.add((OMedia) m.getValue());
        }
        return allOMedia;
    }

    public void printSessions() {
        mAllSessions.printAll();
    }

    public void printSessionContent(String SessionName) {
    }

    public void printUSBList() {
        for (Map.Entry<String, String> entry : mMobileUSBDiscs.entrySet()) {
            MMLog.d(TAG, entry.getKey() + "=" + entry.getValue());
        }
    }

    public String getUSBNameByValue(String value) {
        for (Map.Entry<String, String> entry : mMobileUSBDiscs.entrySet()) {
            if (entry.getValue().equals(value)) return entry.getKey();
        }
        return null;
    }

    /*private void registerFileBroadcastReceiver() {
        if (mFileReceiver == null) mFileReceiver = new MyBroadcastReceiver();
        else return;
        try {
            IntentFilter filter = new IntentFilter();
            filter.addAction("com.zhuchao.android.action.MEDIAFILE_SCAN");
            filter.addAction("com.zhuchao.android.action.FILE_SCAN");
            mContext.registerReceiver(mFileReceiver, filter);
        } catch (Exception e) {
            MMLog.e(TAG, String.valueOf(e));
        }
    }*/
    /*
    private void registerUSBBroadcastReceiver() {
        //if (mUSBBroadcastReceiver == null) mUSBBroadcastReceiver = new GlobalBroadcastReceiver();

        if (mUSBBroadcastReceiver == null) mUSBBroadcastReceiver = new MyBroadcastReceiver();
        else return;
        try {
            IntentFilter intentFilter = new IntentFilter();
            intentFilter.addAction(Intent.ACTION_MEDIA_SHARED);     //如果SDCard未安装,并通过USB大容量存储共享返回
            intentFilter.addAction(Intent.ACTION_MEDIA_MOUNTED);    //表明sd对象是存在并具有读/写权限
            intentFilter.addAction(Intent.ACTION_MEDIA_UNMOUNTED);  //SDCard已卸掉,如果SDCard是存在但没有被安装
            intentFilter.addAction(Intent.ACTION_MEDIA_CHECKING);   //表明对象正在磁盘检查
            intentFilter.addAction(Intent.ACTION_MEDIA_EJECT);      //物理的拔出 SDCARD
            intentFilter.addAction(Intent.ACTION_MEDIA_REMOVED);    //完全拔出
            intentFilter.addDataScheme("file");
            intentFilter.addAction(UsbManager.ACTION_USB_DEVICE_ATTACHED);
            intentFilter.addAction(UsbManager.ACTION_USB_DEVICE_DETACHED);

            //intentFilter.addAction(UsbManager.ACTION_USB_STATE);
            intentFilter.addAction(UsbManager.ACTION_USB_ACCESSORY_ATTACHED);
            intentFilter.addAction(UsbManager.ACTION_USB_ACCESSORY_DETACHED);
            mContext.registerReceiver(mUSBBroadcastReceiver, intentFilter);
        } catch (Exception e) {
            MMLog.e(TAG, String.valueOf(e));
        }
    }*/
    /*
    class MyBroadcastReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {  //mStorageManager = (StorageManager) context.getSystemService(Activity.STORAGE_SERVICE);
            Bundle bundle = intent.getExtras();
            Uri data = intent.getData();
            MMLog.d(TAG, TAG + " action=" + intent.getAction());
            try {
                switch (intent.getAction()) {
                    //case UsbManager.ACTION_USB_DEVICE_ATTACHED:
                    case Intent.ACTION_MEDIA_MOUNTED:
                        ///if (bundle != null) {
                        ///    for (String key : bundle.keySet())
                        ///        MMLog.log(TAG, "USB attached, " + key + ":" + bundle.toString());
                        ///} else MMLog.log(TAG, "USB attached, " + intent.toString() + "," + data);
                        ///if (data != null) MMLog.log(TAG, "USB Url = " + data);
                        mMyHandler.sendEmptyMessage(MessageEvent.MESSAGE_EVENT_USB_MOUNTED);
                        //initSessionFromMobileDisc();
                        break;
                    case UsbManager.ACTION_USB_DEVICE_DETACHED:
                    case UsbManager.ACTION_USB_ACCESSORY_DETACHED:
                        ;
                        break;
                    case Intent.ACTION_MEDIA_UNMOUNTED:
                        ///if (bundle != null) {
                        ///    for (String key : bundle.keySet())
                        ///        MMLog.log(TAG, "USB device detached, " + key + ":" + bundle.toString());
                        ///}
                        mMyHandler.sendEmptyMessage(MessageEvent.MESSAGE_EVENT_USB_MOUNTED);
                        ;
                        break;
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }*/

    @Override
    public void OnSessionComplete(int session_id, String result, int count) {
        Message msg = Message.obtain();
        switch (session_id) {
            case DataID.SESSION_TYPE_GET_MOVIELIST_ALLTV:
            case DataID.SESSION_TYPE_GET_MOVIELIST_ALLMOVIE:
            case DataID.SESSION_TYPE_GET_MOVIELIST_ALLMOVIE2:
                msg.what = session_id;//返回的是分类下面的内容，内容实体已经在相应会话中被处理
                mMyHandler.sendMessage(msg);
                break;
            case DataID.SESSION_TYPE_GET_MOVIELIST_VID:
            case DataID.SESSION_TYPE_GET_MOVIELIST_VNAME:
            case DataID.SESSION_TYPE_GET_MOVIELIST_AREA:
            case DataID.SESSION_TYPE_GET_MOVIELIST_YEAR:
            case DataID.SESSION_TYPE_GET_MOVIELIST_ACTOR:
            case DataID.SESSION_TYPE_GET_MOVIELIST_VIP:
            case DataID.SESSION_TYPE_GET_MOVIE_TYPE:
            default:
                break;
            case DataID.SESSION_TYPE_GET_MOVIE_CATEGORY://返回的是顶层分类信息
                if (mNetCategorySession.getVideoCategoryNameList() != null) {
                    if (mNetCategorySession.getVideoCategoryNameList().size() > 0) {
                        initInternetCategorySessions(); //根据类别信息初始化会话对象数组
                        msg.what = session_id;
                        mMyHandler.sendMessage(msg);//通知初始化分类信息完成，下一部初始化分类下面的内容
                        return;
                    }
                }
                break;
        }
        //MLog.logTAG, "OnSessionComplete mIniType = " + mIniType + ",  sessionId=" + sessionId);
    }

    private final Handler mMyHandler = new Handler(Looper.myLooper()) {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.what) {
                case DataID.SESSION_TYPE_GET_MOVIELIST_ALLTV:
                case DataID.SESSION_TYPE_GET_MOVIELIST_ALLMOVIE:
                case DataID.SESSION_TYPE_GET_MOVIELIST_ALLMOVIE2:
                    printSessionContent(String.valueOf(msg.what));
                    break;
                case DataID.SESSION_TYPE_GET_MOVIE_CATEGORY:
                    updateCategorySessionContent();//更新分类内容
                    return;
                case MessageEvent.MESSAGE_EVENT_USB_UNMOUNT:
                case MessageEvent.MESSAGE_EVENT_USB_MOUNTED:
                    break;
                case DataID.SESSION_TYPE_GET_MOVIE_TYPE:
                case DataID.SESSION_TYPE_GET_MOVIELIST_VID:
                case DataID.SESSION_TYPE_GET_MOVIELIST_VNAME:
                case DataID.SESSION_TYPE_GET_MOVIELIST_AREA:
                case DataID.SESSION_TYPE_GET_MOVIELIST_YEAR:
                case DataID.SESSION_TYPE_GET_MOVIELIST_ACTOR:
                case DataID.SESSION_TYPE_GET_MOVIELIST_VIP:
                default://默认尝试转化成视频列表
                    break;
            }
            //MMLog.log(TAG, "Handler mIniType = " + initStage + ",  sessionId=" + msg.what + "userSessionCallback=" + userSessionCallback.toString());
        }
    };

    public void singleTaskSearchMobileDisc(final String finalDeviceName, final String finalDevicePath) {
        ///MMLog.d(TAG,finalDeviceName +" 1 "+finalDevicePath);
        if (FileUtils.EmptyString(finalDeviceName) || !FileUtils.existDirectory(finalDevicePath)) return;
        ///MMLog.d(TAG,finalDeviceName +" 2 "+finalDevicePath);
        TTask tTask = TTaskManager.getSingleTaskFor(TAG + "." + finalDeviceName).resetAll();
        if (!tTask.isBusy()) {
            tTask.invoke(tag -> initMobileSessionContent(finalDeviceName, finalDevicePath)).startAgain();
        }
        ///else
        ///{
        ///  MMLog.d(TAG,"singleTaskSearchMobileDisc task is busy!");
        ///}
    }

    public String makeSessionName(String name, int id) {
        return name + "." + id;
    }

    @TCourierSubscribe(threadMode = MethodThreadMode.threadMode.BACKGROUND)
    public boolean onTCourierSubscribeEvent(EventCourierInterface courierInterface) {
        ///MMLog.d(TAG, courierInterface.toStr());
        switch (courierInterface.getId()) {
            case MessageEvent.MESSAGE_EVENT_USB_MOUNTED:
                mMobileUSBDiscs = FileUtils.getUDiscName(mContext);
                ///printUSBList();
                String subName1 = null;
                if (courierInterface.getObj() != null) {
                    Intent intent = (Intent) courierInterface.getObj();
                    Bundle bundle = intent.getExtras();
                    Uri data = intent.getData();
                    if (data != null) subName1 = data.getPath();
                    ///for (String key : bundle.keySet())
                    /// MMLog.log(TAG, "USB:" + key + ":" + bundle.toString() + " " + subName1);
                    if (subName1 != null && FileUtils.existDirectory(subName1)) {
                        ///MMLog.d(TAG,subName1+":"+getUSBNameByValue(subName1)+":"+FileUtils.existDirectory(subName1));
                        singleTaskSearchMobileDisc(getUSBNameByValue(subName1), subName1);
                    }
                }
                ///mMyHandler.sendEmptyMessage(MessageEvent.MESSAGE_EVENT_USB_MOUNTED);
                break;
            case MessageEvent.MESSAGE_EVENT_USB_UNMOUNT:
                mMobileUSBDiscs = FileUtils.getUDiscName(mContext);
                String subName2 = null;
                String usbName = null;
                if (courierInterface.getObj() != null) {
                    Intent intent = (Intent) courierInterface.getObj();
                    Bundle bundle = intent.getExtras();
                    Uri data = intent.getData();
                    if (data != null) subName2 = data.getPath();
                    if (subName2 != null) {
                        usbName = getUSBNameByValue(subName2);
                        if (FileUtils.EmptyString(usbName)) {
                            mAllSessions.delete(makeSessionName(usbName, DataID.MEDIA_TYPE_ID_VIDEO));
                            mAllSessions.delete(makeSessionName(usbName, DataID.MEDIA_TYPE_ID_AUDIO));
                        }
                    }
                    userSessionCallback(null, MessageEvent.MESSAGE_EVENT_USB_UNMOUNT, usbName + ":" + subName2);
                    ///for (String key : bundle.keySet())
                    ///    MMLog.log(TAG, "USB:" + key + ":" + bundle.toString() + " " + subName2);
                }
                ///printUSBList();
                ///mMyHandler.sendEmptyMessage(MessageEvent.MESSAGE_EVENT_USB_UNMOUNT);
                break;
        }
        return true;
    }

    public void freeFree() {
        try {
            this.mLocalVideoSession.clear();
            this.mLocalAudioSession.clear();
            this.mFileVideoSession.clear();
            this.mMobileUSBDiscs.clear();
            this.mNetCategorySession.clear();
            this.mAllSessions.clear();
            ///if (mUSBBroadcastReceiver != null) mContext.unregisterReceiver(mUSBBroadcastReceiver);
            ///if (mFileReceiver != null) mContext.unregisterReceiver(mFileReceiver);
            ///mFileReceiver = null;
            ///mUSBBroadcastReceiver = null;
            Cabinet.getEventBus().unRegisterEventObserver(this);
        } catch (Exception e) {
            ///e.printStackTrace();
            MMLog.e(TAG, String.valueOf(e));
        }
    }
}
