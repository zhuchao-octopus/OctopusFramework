package com.zhuchao.android.playsession;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.hardware.usb.UsbManager;
import android.net.Uri;
import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;
import android.util.Log;

import com.zhuchao.android.libfilemanager.FilesManager;
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
    private static Map<Integer, OPlayerSession> mSessions;// = new HashMap<Integer, OPlayerSession>();//Layout classification 版面分类
    private static OPlayerSession mTopSession;// = new OPlayerSession(ScheduleVideoBean.SESSION_TYPE_MANAGER, this);
    //private static OPlayerSession mLocalSession;// = new OPlayerSession(ScheduleVideoBean.SESSION_TYPE_LOCALMEDIA, this);
    //private OPlayerSession mMobileSession = new OPlayerSession(ScheduleVideoBean.SESSION_TYPE_MOBILEMEDIA, this);
    private int mMoBileSessionId = Data.SESSION_TYPE_LOCALMEDIA;
    private Map<String, String> MobileDiscs = new HashMap<String, String>();

    private int mIniType = 0;//0 从网络， 1 //从本地, >=3 已经初始化完成
    private SessionCompleteCallback userSessionCallback = null;

    private OPlayerSession mLocalSession = new OPlayerSession(null);
    private OPlayerSession mMobileSession = new OPlayerSession(null);
    private OPlayerSession mMomileTFSession = new OPlayerSession(null);
    private USBReceiver usbReceiver = new USBReceiver();
    private boolean mThreadLock1 = false;
    private boolean mThreadLock2 = false;
    private boolean mThreadLock3 = false;

    public OPlayerSessionManager(Context context, String hostPath, SessionCompleteCallback SessionCallback) {
        this.userSessionCallback = SessionCallback;
        mContext = context;
        registBroadcast();
        Data.setOplayerSessionRootUrl(hostPath);
        mTopSession = new OPlayerSession(Data.SESSION_TYPE_MANAGER, this);

        mSessions = new TreeMap<Integer, OPlayerSession>(new Comparator<Integer>() {
            @Override
            public int compare(Integer o1, Integer o2) {
                return o2.compareTo(o1);
            }
        });


        try {
            //initSessionsFromInternet();
            //initLocalSessionContent();
            //initSessionFromMobileDisc();//usb
            //initSessionFromMobileTFDisc("/storage/");
        } catch (Exception e) {
            e.printStackTrace();
        }


    }
    private void  registBroadcast()
    {
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
        mIniType = 0;
        new Thread() {
            public void run() {
                try {
                    while (true) {
                        if (NetUtils.isInternetOk()) {
                            initTopSessionContent(); //  初始化线程
                            break;
                        }
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }.start();
    }

    private void initTopSessionContent() {
        Log.d(TAG, "initTopSessionContent mIniType = " + mIniType);
        if (mIniType == 0) {
            mTopSession.doUpdateSession(Data.SESSION_TYPE_GET_MOVIE_CATEGORY);
            mTopSession.doUpdateSession(Data.SESSION_TYPE_GET_MOVIE_TYPE);
        } else {
        }
    }

    private void initLocalSessionContent() {
        new Thread() {
            public void run() {
                mThreadLock1 = true;
                Log.d(TAG, "initLocalSessionContent 本地媒体库 mIniType= " + mIniType);
                OPlayerSession LocalSession = null;

                LocalSession = new OPlayerSession(OPlayerSessionManager.this);
                LocalSession.initMediasFromLocal(mContext, Data.MEDIA_SOURCE_ID_VIDEO);
                if (LocalSession.getVideos().size() > 0)
                    addOtherSeesionToSessions(mMoBileSessionId, "本地视频", LocalSession);
                mLocalSession.addVideos(LocalSession.getVideos());

                LocalSession = new OPlayerSession(OPlayerSessionManager.this);
                LocalSession.initMediasFromLocal(mContext, Data.MEDIA_SOURCE_ID_AUDIO);
                if (LocalSession.getVideos().size() > 0)
                    addOtherSeesionToSessions(mMoBileSessionId, "本地音乐", LocalSession);
                mLocalSession.addVideos(LocalSession.getVideos());

                //LocalSession = new OPlayerSession(OPlayerSessionManager.this);
                //LocalSession.initMediasFromLocal(mContext, Data.MEDIA_SOURCE_ID_PIC);
                //if (LocalSession.getVideos().size() > 0)
                //    addOtherSeesionToSessions(mMoBileSessionId, "本地图片", LocalSession);
                //mLocalSession.addVideos(LocalSession.getVideos());


                //mTopSession.printCategory();//mLocalSession.printCategory();
                //printSessionsInManager();
                if( mLocalSession.getVideos().size()<=0) return;
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

                MobileDiscs = FilesManager.getUDiscName(mContext);
                if (!MobileDiscs.isEmpty()) {
                    //for (Map.Entry<Integer, OPlayerSession> entry : mSessions.entrySet())
                    //{
                    //    if(entry.getKey() > 0 && entry.getKey() < Data.SESSION_TYPE_LOCALMEDIA)
                    //        mSessions.remove(entry.getKey());
                    //}

                    Iterator it = mSessions.entrySet().iterator();
                    while (it.hasNext()) {
                        Map.Entry<Integer, OPlayerSession> entry = (Map.Entry<Integer, OPlayerSession>) it.next();
                        if (entry.getKey() < Data.SESSION_TYPE_LOCALMEDIA)
                            it.remove();
                    }

                    for (Map.Entry<String, String> entry : MobileDiscs.entrySet()) {
                        initMobileSessionContent(entry.getKey(), entry.getValue());
                    }
                } else {
                    Log.d(TAG, " no device found ");
                    return;
                }

                if( mMobileSession.getVideos().size()<=0) return;
                //printSessionsInManager();
                if (userSessionCallback != null)
                    userSessionCallback.OnSessionComplete(Data.SESSION_TYPE_MOBILEMEDIA9, MobileDiscs.toString());
                mThreadLock2 = false;
            }
        }.start();
    }

    public void initSessionFromMobileTFDisc(final String TFPath) {

        new Thread() {
            public void run() {
                mThreadLock3 = true;
                if (TFPath != null) {
                    mMomileTFSession.getVideos().clear();
                    mMomileTFSession.initMediasFromPath(mContext, TFPath, Data.MEDIA_SOURCE_ID_AllMEDIA);

                    if( mMomileTFSession.getVideos().size()<=0) return;

                    if (userSessionCallback != null)
                        userSessionCallback.OnSessionComplete(Data.MEDIA_SOURCE_ID_AllMEDIA, MobileDiscs.toString());
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
        mMobileSession.getVideos().clear();

        MoBileSession = new OPlayerSession(OPlayerSessionManager.this);
        MoBileSession.initMediasFromPath(mContext, DevicePath, Data.MEDIA_SOURCE_ID_VIDEO);

        if (MoBileSession.getVideos().size() > 0)
            addOtherSeesionToSessions(mMoBileSessionId, DeviceName + "视频", MoBileSession);

        if (!DevicePath.contains("SD") && !DevicePath.contains("TF"))
            mMobileSession.addVideos(MoBileSession.getVideos());

        MoBileSession = new OPlayerSession(OPlayerSessionManager.this);
        MoBileSession.initMediasFromPath(mContext, DevicePath, Data.MEDIA_SOURCE_ID_AUDIO);

        if (MoBileSession.getVideos().size() > 0)
            addOtherSeesionToSessions(mMoBileSessionId, DeviceName + "音乐", MoBileSession);

        if (!DevicePath.contains("SD") && !DevicePath.contains("TF"))
            mMobileSession.addVideos(MoBileSession.getVideos());

        //MoBileSession = new OPlayerSession(OPlayerSessionManager.this);
        //MoBileSession.initMediasFromPath(mContext, DevicePath, Data.MEDIA_SOURCE_ID_PIC);
        //if (MoBileSession.getVideos().size() > 0)
        //    addOtherSeesionToSessions(mMoBileSessionId, DeviceName + "图片", MoBileSession);

        //if (!DevicePath.contains("SD") && !DevicePath.contains("TF"))
        //    mMobileSession.addVideos(MoBileSession.getVideos());
    }

    private void initPlaySessionContent() {
        try {
            for (Map.Entry<Integer, OPlayerSession> entry : mSessions.entrySet()) {

                if (entry.getKey() <= Data.SESSION_TYPE_LOCALMEDIA) continue; //本地媒体ID

                Log.d(TAG, "initPlaySessionContent = " + entry.getKey());
                entry.getValue().doUpdateSession(entry.getKey());
            }
            mIniType = 3;
            //Log.d(TAG, "initPlaySessionContent mIniType = " + mIniType);
        } catch (Exception e) {
            //Log.d(TAG, "initPlaySessionContent fail mIniType = " + mIniType + ":" + e.toString());
            //e.printStackTrace();
        }
    }

    private void addOtherSeesionToSessions(int SessionID, String name, OPlayerSession Session) {
        Session.getmVideoCategoryNameList().put(SessionID, name);

        mTopSession.getmVideoCategoryNameList().remove(SessionID);

        mTopSession.getmVideoCategoryNameList().put(SessionID, name);

        mSessions.remove(SessionID);

        mSessions.put(SessionID, Session);
        mMoBileSessionId--;
    }

    private void initOPlayerSessions() {
        for (Map.Entry<Integer, String> entry : mTopSession.getmVideoCategoryNameList().entrySet()) {
            //String cName = entry.getValue();
            if (entry.getKey() > Data.SESSION_TYPE_LOCALMEDIA) {
                OPlayerSession oPlayerSession = new OPlayerSession(this);
                oPlayerSession.getmVideoCategoryNameList().put(entry.getKey(), entry.getValue());
                mSessions.put(entry.getKey(), oPlayerSession);
            }
        }
    }

    public boolean isInitComplete() {
        return mIniType >= 3;
    }

    public OPlayerSession getmTopSession() {
        return mTopSession;
    }

    public OPlayerSession getLocalSession() {
        if (mLocalSession.getmVideoList().getMovieCount() <= 0 && mThreadLock1 == false)
            initLocalSessionContent();
           return mLocalSession;
    }

    public OPlayerSession getMobileSession() {
        if (mMobileSession.getmVideoList().getMovieCount() <= 0 && mThreadLock2 == false) {
            Log.d(TAG, "reinitialize MobileSession -----> ");
            initSessionFromMobileDisc();
        }
        return mMobileSession;
    }

    public OPlayerSession getMomileTFSession() {
        if (mMomileTFSession.getmVideoList().getMovieCount() <= 0 && mThreadLock2 == false) {
            //Log.d(TAG, "reinitialize MobileSession -----> ");
            initSessionFromMobileTFDisc("/storage/");
        }
        return mMomileTFSession;
    }

    public Map<Integer, OPlayerSession> getmSessions() {
        return mSessions;
    }

    public String getCategoryName(int categoryId) {
        return mSessions.get(categoryId).getmVideoCategoryNameList().get(categoryId);
    }

    public void setUserSessionCallback(SessionCompleteCallback userSessionCallback) {
        this.userSessionCallback = userSessionCallback;
    }

    public void printSessionsInManager() {
        String str = "";
        Log.d(TAG, "printSessionsInManager mIniType = " + mIniType);
        for (Map.Entry<Integer, OPlayerSession> entry : mSessions.entrySet()) {
            //Log.d(TAG, entry.getKey() + " : " + entry.getValue().getmVideoCategoryNameList().get(entry.getKey()));
            str = str + entry.getKey() + ":" + entry.getValue().getmVideoCategoryNameList().get(entry.getKey()) + ", ";
        }
        Log.d(TAG, "printSessionsInManager:" + str);
    }

    public void printSessionsVideoList(int categoryId) {
        Log.d(TAG, "printSessionsVideoList categoryId = " + categoryId + " LVideo Count = " + mSessions.get(categoryId).getVideos().size());
        mSessions.get(categoryId).printMovies();
        /*
        for (Map.Entry<Integer, OPlayerSession> entry : mSessions.entrySet())
        {
            Log.d(TAG, "printSessionsVideoList " + entry.getKey() + " : " + entry.getValue().getmVideoCategoryNameList().get(entry.getKey())
                    + " Movies Count =" + entry.getValue().getVideos().size());
            entry.getValue().printMovies();
        }*/
    }

    public List<OMedia> getAllVideoList() {
        List<OMedia> allOMedia = new ArrayList<>();
        for (Map.Entry<Integer, OPlayerSession> entry : mSessions.entrySet()) {
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
                msg.what = sessionId;
                myHandler.sendMessage(msg);
                break;
            case Data.SESSION_TYPE_GET_MOVIELIST_VID:
            case Data.SESSION_TYPE_GET_MOVIELIST_VNAME:
            case Data.SESSION_TYPE_GET_MOVIELIST_AREA:
            case Data.SESSION_TYPE_GET_MOVIELIST_YEAR:
            case Data.SESSION_TYPE_GET_MOVIELIST_ACTOR:
            case Data.SESSION_TYPE_GET_MOVIELIST_VIP:
            case Data.SESSION_TYPE_GET_MOVIE_TYPE:
                break;
            case Data.SESSION_TYPE_GET_MOVIE_CATEGORY:
                if (mTopSession.getmVideoCategoryNameList() != null)
                    if (mTopSession.getmVideoCategoryNameList().size() > 0) {
                        initOPlayerSessions(); //根据类别信息初始化会话对象数组
                        mIniType = 2;//初始化分类信息完成
                        msg.what = sessionId;
                        myHandler.sendMessage(msg);//通知初始化分类信息完成
                        return;
                    } else {
                        mIniType = 1;//从本地初始化
                    }
                break;

            default:
                break;
        }
        //Log.d(TAG, "OnSessionComplete mIniType = " + mIniType + ",  sessionId=" + sessionId);
    }

    public void free() {
        //if (!isInitComplete()) return;
        try {
            mContext.unregisterReceiver(usbReceiver);
            this.mIniType = 0;
            this.mLocalSession.getVideos().clear();
            this.mMobileSession.getVideos().clear();
            this.mMomileTFSession.getVideos().clear();
            this.MobileDiscs.clear();
            mTopSession.getVideos().clear();
            mSessions.clear();
        } catch (Exception e) {
            e.printStackTrace();
        }

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
                case Data.SESSION_TYPE_GET_MOVIELIST_VID:
                case Data.SESSION_TYPE_GET_MOVIELIST_VNAME:
                case Data.SESSION_TYPE_GET_MOVIELIST_AREA:
                case Data.SESSION_TYPE_GET_MOVIELIST_YEAR:
                case Data.SESSION_TYPE_GET_MOVIELIST_ACTOR:
                case Data.SESSION_TYPE_GET_MOVIELIST_VIP:
                    break;
                case Data.SESSION_TYPE_GET_MOVIE_CATEGORY:
                    initPlaySessionContent();
                    return;
                case Data.SESSION_TYPE_GET_MOVIE_TYPE:
                    break;
                default://默认尝试转化成视频列表
                    break;
            }

            Log.d(TAG, "Handler mIniType = " + mIniType + ",  sessionId=" + msg.what + "userSessionCallback=" + userSessionCallback.toString());
            if (userSessionCallback != null) {
                //Log.d(TAG, "Handler mIniType = " + mIniType + ",  sessionId=" + msg.what);
                userSessionCallback.OnSessionComplete(msg.what, null);
            }
        }
    };

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
}
