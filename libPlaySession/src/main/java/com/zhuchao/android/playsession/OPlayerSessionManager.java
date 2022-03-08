package com.zhuchao.android.playsession;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.hardware.usb.UsbManager;
import android.net.Uri;
import android.os.Handler;
import android.os.Message;
import android.util.Log;

import com.zhuchao.android.libfileutils.FilesManager;
import com.zhuchao.android.netutil.NetUtils;
import com.zhuchao.android.video.OMedia;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;


public class OPlayerSessionManager implements SessionCompleteCallback {
    private final String TAG = "OPlayerSessionManager->";
    private static Context mContext = null;
    private int mobileSessionId = Data.SESSION_TYPE_LOCALMEDIA;
    private Map<String, String> mobileDiscs = new HashMap<String, String>();
    private SessionCompleteCallback userSessionCallback = null;
    private static Map<Integer, OPlayerSession> sessions;
    private static OPlayerSession categorySession;// = new OPlayerSession(ScheduleVideoBean.SESSION_TYPE_MANAGER, this);
    private OPlayerSession localSession = new OPlayerSession(null);
    private OPlayerSession mobileSession = new OPlayerSession(null);
    private OPlayerSession mobileTFSession = new OPlayerSession(null);

    private USBReceiver usbReceiver = new USBReceiver();
    private boolean mThreadLock1 = false;
    private boolean mThreadLock2 = false;
    private boolean mThreadLock3 = false;
    private int initType = 0;//0 从网络， 1 //从本地, >=3 已经初始化完成

    public OPlayerSessionManager(Context context, String hostPath, SessionCompleteCallback SessionCallback) {
        this.userSessionCallback = SessionCallback;
        mContext = context;
        registerBroadcast();
        Data.setOplayerSessionRootUrl(hostPath);
        categorySession = new OPlayerSession(Data.SESSION_TYPE_MANAGER, this);
        sessions = new TreeMap<Integer, OPlayerSession>(new Comparator<Integer>() {
            @Override
            public int compare(Integer o1, Integer o2) {
                return o2.compareTo(o1);
            }
        });

        try {
            initSessionsFromInternet();
            initSessionFromLocal();
            initSessionFromMobileDisc();//usb
            initSessionFromMobileTFDisc("/storage/");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void setUserSessionCallback(SessionCompleteCallback userSessionCallback) {
        this.userSessionCallback = userSessionCallback;
    }

    private void registerBroadcast() {
        try {
            IntentFilter filter = new IntentFilter();
            filter.addAction("android.intent.action.MEDIA_MOUNTED");
            filter.addAction("android.intent.action.MEDIA_UNMOUNTED");
            filter.addAction("android.intent.action.MEDIA_REMOVED");
            filter.addAction("com.zhuchao.android.MEDIAFILEA_SCAN_ACTION");
            filter.addAction("android.hardware.usb.action.USB_STATE");
            filter.addAction(Intent.ACTION_MEDIA_MOUNTED);
            filter.addAction(Intent.ACTION_MEDIA_UNMOUNTED);
            filter.addAction(UsbManager.ACTION_USB_ACCESSORY_ATTACHED);
            filter.addAction(UsbManager.ACTION_USB_ACCESSORY_DETACHED);
            filter.addAction(UsbManager.ACTION_USB_DEVICE_ATTACHED);
            filter.addAction(UsbManager.ACTION_USB_DEVICE_DETACHED);
            mContext.registerReceiver(usbReceiver, filter);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void initSessionsFromInternet() {
        initType = 0;
        new Thread() {
            public void run() {
                try {
                    while (true) {
                        if (NetUtils.isInternetOk()) {
                            updateCategorySession(); //  初始化线程
                            break;
                        }
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }.start();
    }

    private void initSessionFromLocal() {
        new Thread() {
            public void run() {
                mThreadLock1 = true;
                Log.d(TAG, "initLocalSessionContent 本地媒体库 mIniType= " + initType);
                OPlayerSession LocalSession = null;

                LocalSession = new OPlayerSession(OPlayerSessionManager.this);
                LocalSession.initMediasFromLocal(mContext, Data.MEDIA_SOURCE_ID_VIDEO);
                if (LocalSession.getVideos().size() > 0)
                    addSessionToSessions(mobileSessionId, "本地视频", LocalSession);
                localSession.addVideos(LocalSession.getVideos());

                LocalSession = new OPlayerSession(OPlayerSessionManager.this);
                LocalSession.initMediasFromLocal(mContext, Data.MEDIA_SOURCE_ID_AUDIO);
                if (LocalSession.getVideos().size() > 0)
                    addSessionToSessions(mobileSessionId, "本地音乐", LocalSession);
                localSession.addVideos(LocalSession.getVideos());

                //LocalSession = new OPlayerSession(OPlayerSessionManager.this);
                //LocalSession.initMediasFromLocal(mContext, Data.MEDIA_SOURCE_ID_PIC);
                //if (LocalSession.getVideos().size() > 0)
                //    addOtherSeesionToSessions(mMoBileSessionId, "本地图片", LocalSession);
                //mLocalSession.addVideos(LocalSession.getVideos());

                //mTopSession.printCategory();//mLocalSession.printCategory();
                //printSessionsInManager();
                if (localSession.getVideos().size() <= 0) return;
                if (userSessionCallback != null)
                    userSessionCallback.OnSessionComplete(Data.SESSION_TYPE_LOCALMEDIA, LocalSession.toString());

                mThreadLock1 = false;
            }
        }.start();
    }

    public void initSessionFromMobileDisc() {
        new Thread() {
            public void run() {
                mThreadLock2 = true;
                mobileDiscs = FilesManager.getUDiscName(mContext);
                if (!mobileDiscs.isEmpty()) {
                    //for (Map.Entry<Integer, OPlayerSession> entry : mSessions.entrySet())
                    //{
                    //    if(entry.getKey() > 0 && entry.getKey() < Data.SESSION_TYPE_LOCALMEDIA)
                    //        mSessions.remove(entry.getKey());
                    //}

                    Iterator it = sessions.entrySet().iterator();
                    while (it.hasNext()) {
                        Map.Entry<Integer, OPlayerSession> entry = (Map.Entry<Integer, OPlayerSession>) it.next();
                        if (entry.getKey() < Data.SESSION_TYPE_LOCALMEDIA)
                            it.remove();
                    }

                    for (Map.Entry<String, String> entry : mobileDiscs.entrySet()) {
                        initMobileSessionContent(entry.getKey(), entry.getValue());
                    }
                } else {
                    Log.d(TAG, " no device found ");
                    return;
                }

                if (mobileSession.getVideos().size() <= 0) return;
                //printSessionsInManager();
                if (userSessionCallback != null)
                    userSessionCallback.OnSessionComplete(Data.SESSION_TYPE_MOBILEMEDIA9, mobileDiscs.toString());
                mThreadLock2 = false;
            }
        }.start();
    }

    public void initSessionFromMobileTFDisc(final String TFPath) {
        new Thread() {
            public void run() {
                mThreadLock3 = true;
                if (TFPath != null) {
                    mobileTFSession.getVideos().clear();
                    mobileTFSession.initMediasFromPath(mContext, TFPath, Data.MEDIA_SOURCE_ID_AllMEDIA);
                    if (mobileTFSession.getVideos().size() <= 0) return;
                    if (userSessionCallback != null)
                        userSessionCallback.OnSessionComplete(Data.MEDIA_SOURCE_ID_AllMEDIA, mobileDiscs.toString());
                }
                mThreadLock3 = false;
            }
        }.start();
    }

    private void initMobileSessionContent(final String DeviceName, final String DevicePath) {
        Log.d(TAG, "initMobileSessionContent DeviceName = " + DevicePath);
        if (DeviceName == null) return;
        //mMobileSession.initMediasFromPath(mContext,DevicePath,Data.MEDIA_SOURCE_ID_AllMEDIA);
        OPlayerSession MoBileSession;
        mobileSession.getVideos().clear();

        MoBileSession = new OPlayerSession(OPlayerSessionManager.this);
        MoBileSession.initMediasFromPath(mContext, DevicePath, Data.MEDIA_SOURCE_ID_VIDEO);

        if (MoBileSession.getVideos().size() > 0)
            addSessionToSessions(mobileSessionId, DeviceName + "视频", MoBileSession);

        if (!DevicePath.contains("SD") && !DevicePath.contains("TF"))
            mobileSession.addVideos(MoBileSession.getVideos());

        MoBileSession = new OPlayerSession(OPlayerSessionManager.this);
        MoBileSession.initMediasFromPath(mContext, DevicePath, Data.MEDIA_SOURCE_ID_AUDIO);

        if (MoBileSession.getVideos().size() > 0)
            addSessionToSessions(mobileSessionId, DeviceName + "音乐", MoBileSession);

        if (!DevicePath.contains("SD") && !DevicePath.contains("TF"))
            mobileSession.addVideos(MoBileSession.getVideos());

        //MoBileSession = new OPlayerSession(OPlayerSessionManager.this);
        //MoBileSession.initMediasFromPath(mContext, DevicePath, Data.MEDIA_SOURCE_ID_PIC);
        //if (MoBileSession.getVideos().size() > 0)
        //    addOtherSeesionToSessions(mMoBileSessionId, DeviceName + "图片", MoBileSession);

        //if (!DevicePath.contains("SD") && !DevicePath.contains("TF"))
        //    mMobileSession.addVideos(MoBileSession.getVideos());
    }

    private void initCategorySessions() {
        for (Map.Entry<Integer, String> entry : categorySession.getmVideoCategoryNameList().entrySet()) {
            if (entry.getKey() > Data.SESSION_TYPE_LOCALMEDIA) {
                OPlayerSession oPlayerSession = new OPlayerSession(this);
                oPlayerSession.getmVideoCategoryNameList().put(entry.getKey(), entry.getValue());
                sessions.put(entry.getKey(), oPlayerSession);
            }
        }
    }

    private void updateCategorySession() { //顶层分类信息，顶层分类会话
        Log.d(TAG, "updateTopSession mIniType = " + initType);
        if (initType == 0) {
            categorySession.doUpdateSession(Data.SESSION_TYPE_GET_MOVIE_CATEGORY);
            categorySession.doUpdateSession(Data.SESSION_TYPE_GET_MOVIE_TYPE);
        } else {
        }
    }

    private void updateCategorySessionContent() {//获取分类下面的内容
        try {
            for (Map.Entry<Integer, OPlayerSession> entry : sessions.entrySet()) {
                if (entry.getKey() <= Data.SESSION_TYPE_LOCALMEDIA) continue; //本地媒体ID
                Log.d(TAG, "updateCategorySessionContent = " + entry.getKey());
                entry.getValue().doUpdateSession(entry.getKey());
            }
            initType = 3;
            //Log.d(TAG, "initPlaySessionContent mIniType = " + mIniType);
        } catch (Exception e) {
            //Log.d(TAG, "initPlaySessionContent fail mIniType = " + mIniType + ":" + e.toString());
            e.printStackTrace();
        }
    }

    private void addSessionToSessions(int SessionID, String name, OPlayerSession Session) {
        Session.getmVideoCategoryNameList().put(SessionID, name);
        categorySession.getmVideoCategoryNameList().remove(SessionID);
        categorySession.getmVideoCategoryNameList().put(SessionID, name);
        sessions.remove(SessionID);
        sessions.put(SessionID, Session);
        mobileSessionId--;
    }

    public boolean isInitComplete() {
        return initType >= 3;
    }

    public OPlayerSession getTopSession() {
        return categorySession;
    }

    public OPlayerSession getLocalSession() {
        if (localSession.getmVideoList().getCount() <= 0 && mThreadLock1 == false)
            initSessionFromLocal();
        return localSession;
    }

    public OPlayerSession getMobileSession() {
        if (mobileSession.getmVideoList().getCount() <= 0 && mThreadLock2 == false) {
            Log.d(TAG, "reinitialize MobileSession -----> ");
            initSessionFromMobileDisc();
        }
        return mobileSession;
    }

    public OPlayerSession getMobileTFSession() {
        if (mobileTFSession.getmVideoList().getCount() <= 0 && mThreadLock2 == false) {
            //Log.d(TAG, "reinitialize MobileSession -----> ");
            initSessionFromMobileTFDisc("/storage/");
        }
        return mobileTFSession;
    }

    public Map<Integer, OPlayerSession> getSessions() {
        return sessions;
    }

    public String getCategoryName(int categoryId) {
        return sessions.get(categoryId).getmVideoCategoryNameList().get(categoryId);
    }

    public List<OMedia> getAllVideoList() {
        List<OMedia> allOMedia = new ArrayList<>();
        for (Map.Entry<Integer, OPlayerSession> entry : sessions.entrySet()) {
            Log.d(TAG, "printSessionsVideoList " + entry.getKey() + " : " + entry.getValue().getmVideoCategoryNameList().get(entry.getKey())
                    + " Movies Count =" + entry.getValue().getVideos().size());
            entry.getValue().getVideos();
            allOMedia.addAll(entry.getValue().getVideos());
        }
        return allOMedia;
    }

    @Override
    public void OnSessionComplete(int sessionId, String result) {
        Message msg = Message.obtain();
        switch (sessionId) {
            case Data.SESSION_TYPE_GET_MOVIELIST_ALLTV:
            case Data.SESSION_TYPE_GET_MOVIELIST_ALLMOVIE:
            case Data.SESSION_TYPE_GET_MOVIELIST_ALLMOVIE2:
                msg.what = sessionId;//返回的是分类下面的内容，内容实体已经在相应会话中被处理
                myHandler.sendMessage(msg);
                break;
            case Data.SESSION_TYPE_GET_MOVIELIST_VID:
            case Data.SESSION_TYPE_GET_MOVIELIST_VNAME:
            case Data.SESSION_TYPE_GET_MOVIELIST_AREA:
            case Data.SESSION_TYPE_GET_MOVIELIST_YEAR:
            case Data.SESSION_TYPE_GET_MOVIELIST_ACTOR:
            case Data.SESSION_TYPE_GET_MOVIELIST_VIP:
            case Data.SESSION_TYPE_GET_MOVIE_TYPE:
            default:
                break;
            case Data.SESSION_TYPE_GET_MOVIE_CATEGORY://返回的是顶层分类信息
                if (categorySession.getmVideoCategoryNameList() != null) {
                    if (categorySession.getmVideoCategoryNameList().size() > 0) {
                        initCategorySessions(); //根据类别信息初始化会话对象数组
                        initType = 2;//初始化分类信息完成
                        msg.what = sessionId;
                        myHandler.sendMessage(msg);//通知初始化分类信息完成，下一部初始化分类下面的内容
                        return;
                    }
                } else {
                    initType = 1;//从本地初始化
                }
                break;
        }
        //Log.d(TAG, "OnSessionComplete mIniType = " + mIniType + ",  sessionId=" + sessionId);
    }

    private Handler myHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.what) {
                case Data.SESSION_TYPE_GET_MOVIELIST_ALLTV:
                case Data.SESSION_TYPE_GET_MOVIELIST_ALLMOVIE:
                case Data.SESSION_TYPE_GET_MOVIELIST_ALLMOVIE2:
                    printSessionsVideoList(msg.what);
                    break;
                case Data.SESSION_TYPE_GET_MOVIE_CATEGORY:
                    updateCategorySessionContent();//更新分类内容
                    return;
                case Data.SESSION_TYPE_GET_MOVIE_TYPE:
                case Data.SESSION_TYPE_GET_MOVIELIST_VID:
                case Data.SESSION_TYPE_GET_MOVIELIST_VNAME:
                case Data.SESSION_TYPE_GET_MOVIELIST_AREA:
                case Data.SESSION_TYPE_GET_MOVIELIST_YEAR:
                case Data.SESSION_TYPE_GET_MOVIELIST_ACTOR:
                case Data.SESSION_TYPE_GET_MOVIELIST_VIP:
                default://默认尝试转化成视频列表
                    break;
            }

            Log.d(TAG, "Handler mIniType = " + initType + ",  sessionId=" + msg.what + "userSessionCallback=" + userSessionCallback.toString());
            if (userSessionCallback != null) {
                //Log.d(TAG, "Handler mIniType = " + mIniType + ",  sessionId=" + msg.what);
                userSessionCallback.OnSessionComplete(msg.what, null);
            }
        }
    };

    public void printSessionsInManager() {
        String str = "";
        Log.d(TAG, "printSessionsInManager mIniType = " + initType);
        for (Map.Entry<Integer, OPlayerSession> entry : sessions.entrySet()) {
            //Log.d(TAG, entry.getKey() + " : " + entry.getValue().getmVideoCategoryNameList().get(entry.getKey()));
            str = str + entry.getKey() + ":" + entry.getValue().getmVideoCategoryNameList().get(entry.getKey()) + ", ";
        }
        Log.d(TAG, "printSessionsInManager:" + str);
    }

    public void printSessionsVideoList(int categoryId) {
        Log.d(TAG, "printSessionsVideoList categoryId = " + categoryId + " LVideo Count = " + sessions.get(categoryId).getVideos().size());
        sessions.get(categoryId).printMovies();
        /*
        for (Map.Entry<Integer, OPlayerSession> entry : mSessions.entrySet())
        {
            Log.d(TAG, "printSessionsVideoList " + entry.getKey() + " : " + entry.getValue().getmVideoCategoryNameList().get(entry.getKey())
                    + " Movies Count =" + entry.getValue().getVideos().size());
            entry.getValue().printMovies();
        }*/
    }

    class USBReceiver extends BroadcastReceiver {
        //private StorageManager mStorageManager;
        @Override
        public void onReceive(Context context, Intent intent) {
            //mStorageManager = (StorageManager) context.getSystemService(Activity.STORAGE_SERVICE);
            switch (intent.getAction()) {
                case Intent.ACTION_MEDIA_MOUNTED:
                case UsbManager.ACTION_USB_DEVICE_ATTACHED:
                    Uri data = intent.getData();
                    Log.d(TAG, "USBReceiver path = " + data);
                    initSessionFromMobileDisc();
                    break;
                case UsbManager.ACTION_USB_DEVICE_DETACHED:
                    //Name of extra for ACTION_USB_DEVICE_ATTACHED and ACTION_USB_DEVICE_DETACHED broadcasts containing the UsbDevice object for the device.
                    //UsbDevice device = intent.getParcelableExtra(UsbManager.EXTRA_DEVICE);
                    break;
            }
        }
    }

    public void free() {
        //if (!isInitComplete()) return;
        try {
            mContext.unregisterReceiver(usbReceiver);
            this.initType = 0;
            this.localSession.getVideos().clear();
            this.mobileSession.getVideos().clear();
            this.mobileTFSession.getVideos().clear();
            this.mobileDiscs.clear();
            categorySession.getVideos().clear();
            sessions.clear();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
