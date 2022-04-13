package com.zhuchao.android.playsession;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.hardware.usb.UsbManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;

import com.zhuchao.android.libfileutils.FilesManager;
import com.zhuchao.android.libfileutils.MLog;
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
    private final String TAG = "OPlayerSessionManager";
    private static Context mContext = null;
    private SessionCompleteCallback userSessionCallback = null;
    private static Map<Integer, OPlayerSession> sessions;
    private static OPlayerSession categorySession;// = new OPlayerSession(ScheduleVideoBean.SESSION_TYPE_MANAGER, this);

    private OPlayerSession localSession = new OPlayerSession(null);
    private OPlayerSession mobileUSBSession = new OPlayerSession(null);
    private OPlayerSession fileSession = new OPlayerSession(null);

    private int mobileSessionId = SessionID.SESSION_SOURCE_LOCAL_INTERNAL;
    private Map<String, String> mobileDiscs = new HashMap<String, String>();

    private MyBroadcastReceiver usbReceiver = null;//new USBReceiver();
    private MyBroadcastReceiver fileReceiver = null;//new USBReceiver();
    private boolean mThreadLock1 = false;
    private boolean mThreadLock2 = false;
    private boolean mThreadLock3 = false;
    private int initStage = 0;//0 从网络， 1 //从本地, >=3 已经初始化完成

    public OPlayerSessionManager(Context context, SessionCompleteCallback SessionCallback) {
        this.userSessionCallback = SessionCallback;
        mContext = context;
        //Data.setOplayerSessionRootUrl(hostPath);
        categorySession = new OPlayerSession(SessionID.SESSION_SOURCE_NONE, this);
        sessions = new TreeMap<Integer, OPlayerSession>(new Comparator<Integer>() {
            @Override
            public int compare(Integer o1, Integer o2) {
                return o2.compareTo(o1);
            }
        });
        //try {
        //initSessionsFromInternet();
        //initSessionFromLocal();
        //initSessionFromMobileDisc();//usb
        //initSessionFromMobileTFDisc("/storage/");
        //} catch (Exception e) {
        //   e.printStackTrace();
        //}
    }

    public void setUserSessionCallback(SessionCompleteCallback userSessionCallback) {
        this.userSessionCallback = userSessionCallback;
    }

    public OPlayerSessionManager Callback(SessionCompleteCallback userSessionCallback) {
        this.userSessionCallback = userSessionCallback;
        return this;
    }

    private void initSessionsFromInternet() {
        initStage = 0;
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

    public void initSessionFromLocal() {
        if (mThreadLock1) return;
        new Thread() {
            public void run() {
                mThreadLock1 = true;
                MLog.log(TAG, "initLocalSessionContent 本地媒体库");
                OPlayerSession lSession = null;

                lSession = new OPlayerSession(OPlayerSessionManager.this);
                lSession.initMediasFromLocal(mContext, SessionID.MEDIA_TYPE_ID_VIDEO);
                if (lSession.getVideos().getCount() > 0)
                    addSessionToSessions(mobileSessionId, "本地视频", lSession);
                localSession.addVideos(lSession.getVideos());
                if (userSessionCallback != null)
                    userSessionCallback.OnSessionComplete(SessionID.MEDIA_TYPE_ID_VIDEO, "本地视频");

                lSession = new OPlayerSession(OPlayerSessionManager.this);
                lSession.initMediasFromLocal(mContext, SessionID.MEDIA_TYPE_ID_AUDIO);
                if (lSession.getVideos().getCount() > 0)
                    addSessionToSessions(mobileSessionId, "本地音乐", lSession);
                localSession.addVideos(lSession.getVideos());
                if (userSessionCallback != null)
                    userSessionCallback.OnSessionComplete(SessionID.MEDIA_TYPE_ID_AUDIO, "本地音乐");

                lSession = new OPlayerSession(OPlayerSessionManager.this);
                lSession.initMediasFromLocal(mContext, SessionID.MEDIA_TYPE_ID_PIC);
                if (lSession.getVideos().getCount() > 0)
                    addSessionToSessions(mobileSessionId, "本地图片", lSession);
                localSession.addVideos(lSession.getVideos());
                if (userSessionCallback != null)
                    userSessionCallback.OnSessionComplete(SessionID.MEDIA_TYPE_ID_PIC, "本地图片");

                mThreadLock1 = false;
            }
        }.start();
    }

    public void initSessionFromMobileDisc() {
        if (mThreadLock2) return;
        if (userSessionCallback != null && usbReceiver == null)
            registerUSBBroadcastReceiver();

        new Thread() {
            public void run() {
                mThreadLock2 = true;
                mobileDiscs = FilesManager.getUDiscName(mContext);
                if (mobileDiscs.isEmpty()) {
                    MLog.log(TAG, " no device found ");
                    mThreadLock2 = false;
                    return;
                }
                Iterator it = sessions.entrySet().iterator();
                while (it.hasNext()) {
                    Map.Entry<Integer, OPlayerSession> entry = (Map.Entry<Integer, OPlayerSession>) it.next();
                    if (entry.getKey() < SessionID.SESSION_SOURCE_LOCAL_INTERNAL)
                        it.remove();
                }
                mobileUSBSession.getVideos().clear();
                for (Map.Entry<String, String> entry : mobileDiscs.entrySet()) {
                    initMobileSessionContent(entry.getKey(), entry.getValue());
                    if (userSessionCallback != null) {
                        userSessionCallback.OnSessionComplete(SessionID.SESSION_SOURCE_MOBILE_USB, entry.getKey() + ":" + entry.getValue());
                    }
                }
                mThreadLock2 = false;
            }
        }.start();
    }

    private void initSessionFromPath(final String Path) {
        if (mThreadLock3) return;
        if (Path == null) return;
        new Thread() {
            public void run() {
                mThreadLock3 = true;
                fileSession.getVideos().clear();
                fileSession.initMediasFromPath(mContext, Path, SessionID.MEDIA_TYPE_ID_AUDIO_VIDEO);
                if (userSessionCallback != null)
                    userSessionCallback.OnSessionComplete(SessionID.SESSION_SOURCE_PATH, Path);
                mThreadLock3 = false;
            }
        }.start();
    }

    public void scanFilesFromPath(Context context, String filePath, List<String> FileList) {
        if (mThreadLock3) return;
        if (filePath == null) return;
        //if (FileList == null) return;
        if (!FilesManager.isExists(filePath)) return;
        registerFileBroadcastReceiver();
        fileSession.getVideos().clear();
        new Thread() {
            public void run() {
                mThreadLock3 = true;
                fileSession.initFilesFromPath(mContext, filePath, FileList);
                if (userSessionCallback != null)
                    userSessionCallback.OnSessionComplete(SessionID.SESSION_SOURCE_PATH, filePath);
                mThreadLock3 = false;
            }
        }.start();
    }

    private void initMobileSessionContent(final String DeviceName, final String DevicePath) {
        MLog.log(TAG, "initMobileSessionContent  " +DeviceName+":"+ DevicePath);
        if (DeviceName == null) return;
        OPlayerSession mSession = null;

        mSession = new OPlayerSession(OPlayerSessionManager.this);
        mSession.initMediasFromPath(mContext, DevicePath, SessionID.MEDIA_TYPE_ID_AllMEDIA);
        if (mSession.getVideos().getCount() > 0)
            addSessionToSessions(mobileSessionId, DeviceName, mSession);
        mobileUSBSession.addVideos(mSession.getVideos());//USB总共的媒体文件

        /*mSession = new OPlayerSession(OPlayerSessionManager.this);
        mSession.initMediasFromPath(mContext, DevicePath, Data.MEDIA_TYPE_ID_AUDIO);
        if (mSession.getVideos().size() > 0)
            addSessionToSessions(mobileSessionId, DeviceName, mSession);
        mobileUSBSession.addVideos(mSession.getVideos());//USB总共的媒体文件

        mSession = new OPlayerSession(OPlayerSessionManager.this);
        mSession.initMediasFromPath(mContext, DevicePath, Data.MEDIA_TYPE_ID_PIC);
        if (mSession.getVideos().size() > 0)
            addSessionToSessions(mobileSessionId, DeviceName, mSession);
        mobileUSBSession.addVideos(mSession.getVideos());//USB总共的媒体文件*/
    }

    //初始化非本地分类
    private void initInternetCategorySessions() {
        for (Map.Entry<Integer, String> entry : categorySession.getVideoCategoryNameList().entrySet()) {
            if (entry.getKey() > SessionID.SESSION_SOURCE_LOCAL_INTERNAL)//初始化非本地分类
            {
                OPlayerSession oPlayerSession = new OPlayerSession(this);
                oPlayerSession.getVideoCategoryNameList().put(entry.getKey(), entry.getValue());
                sessions.put(entry.getKey(), oPlayerSession);
            }
        }
    }

    private void updateCategorySession() { //顶层分类信息，顶层分类会话
        MLog.log(TAG, "updateTopSession mIniType = " + initStage);
        if (initStage == 0) {
            categorySession.doUpdateSession(SessionID.SESSION_TYPE_GET_MOVIE_CATEGORY);
            categorySession.doUpdateSession(SessionID.SESSION_TYPE_GET_MOVIE_TYPE);
        } else {
        }
    }

    private void updateCategorySessionContent() {//获取分类下面的内容
        try {
            for (Map.Entry<Integer, OPlayerSession> entry : sessions.entrySet()) {
                if (entry.getKey() <= SessionID.SESSION_SOURCE_LOCAL_INTERNAL) continue; //本地媒体ID
                MLog.log(TAG, "updateCategorySessionContent = " + entry.getKey());
                entry.getValue().doUpdateSession(entry.getKey());
            }
            initStage = 3;
            //MLog.logTAG, "initPlaySessionContent mIniType = " + mIniType);
        } catch (Exception e) {
            //MLog.logTAG, "initPlaySessionContent fail mIniType = " + mIniType + ":" + e.toString());
            e.printStackTrace();
        }
    }

    private void addSessionToSessions(int SessionID, String name, OPlayerSession Session) {
        Session.getVideoCategoryNameList().put(SessionID, name);//备注名称
        categorySession.getVideoCategoryNameList().remove(SessionID);
        categorySession.getVideoCategoryNameList().put(SessionID, name);
        sessions.remove(SessionID);
        sessions.put(SessionID, Session);
        mobileSessionId--;
    }

    //public boolean isInitComplete() {
    //    return initType >= 3;
    //}

    public OPlayerSession getCategorySession() {
        return categorySession;
    }

    public Map<Integer, OPlayerSession> getSessions() {
        return sessions;
    }

    public OPlayerSession getLocalSession() {
        return localSession;
    }

    public OPlayerSession getMobileSession() {
        return mobileUSBSession;
    }

    public OPlayerSession getFileSession() {
        return fileSession;
    }

    public String getCategoryName(int categoryId) {
        return sessions.get(categoryId).getVideoCategoryNameList().get(categoryId);
    }

    public List<OMedia> getAllMedias() {
        List<OMedia> allOMedia = new ArrayList<>();
        for (Map.Entry<Integer, OPlayerSession> entry : sessions.entrySet()) {
            /*
            MLog.logTAG, "printSessionsVideoList " + entry.getKey() + " : " + entry.getValue().getVideoCategoryNameList().get(entry.getKey())
                    + " Movies Count =" + entry.getValue().getVideos().size());
            */
            for (HashMap.Entry<String, Object> m : entry.getValue().getVideos().getMap().entrySet())
                allOMedia.add((OMedia) m.getValue());
        }
        return allOMedia;
    }

    @Override
    public void OnSessionComplete(int sessionId, String result) {
        Message msg = Message.obtain();
        switch (sessionId) {
            case SessionID.SESSION_TYPE_GET_MOVIELIST_ALLTV:
            case SessionID.SESSION_TYPE_GET_MOVIELIST_ALLMOVIE:
            case SessionID.SESSION_TYPE_GET_MOVIELIST_ALLMOVIE2:
                msg.what = sessionId;//返回的是分类下面的内容，内容实体已经在相应会话中被处理
                myHandler.sendMessage(msg);
                break;
            case SessionID.SESSION_TYPE_GET_MOVIELIST_VID:
            case SessionID.SESSION_TYPE_GET_MOVIELIST_VNAME:
            case SessionID.SESSION_TYPE_GET_MOVIELIST_AREA:
            case SessionID.SESSION_TYPE_GET_MOVIELIST_YEAR:
            case SessionID.SESSION_TYPE_GET_MOVIELIST_ACTOR:
            case SessionID.SESSION_TYPE_GET_MOVIELIST_VIP:
            case SessionID.SESSION_TYPE_GET_MOVIE_TYPE:
            default:
                break;
            case SessionID.SESSION_TYPE_GET_MOVIE_CATEGORY://返回的是顶层分类信息
                if (categorySession.getVideoCategoryNameList() != null) {
                    if (categorySession.getVideoCategoryNameList().size() > 0) {
                        initInternetCategorySessions(); //根据类别信息初始化会话对象数组
                        initStage = 2;//初始化分类信息完成
                        msg.what = sessionId;
                        myHandler.sendMessage(msg);//通知初始化分类信息完成，下一部初始化分类下面的内容
                        return;
                    }
                } else {
                    initStage = 1;//从本地初始化
                }
                break;
        }
        //MLog.logTAG, "OnSessionComplete mIniType = " + mIniType + ",  sessionId=" + sessionId);
    }

    private Handler myHandler = new Handler(Looper.getMainLooper()) {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.what) {
                case SessionID.SESSION_TYPE_GET_MOVIELIST_ALLTV:
                case SessionID.SESSION_TYPE_GET_MOVIELIST_ALLMOVIE:
                case SessionID.SESSION_TYPE_GET_MOVIELIST_ALLMOVIE2:
                    printSessionContent(msg.what);
                    break;
                case SessionID.SESSION_TYPE_GET_MOVIE_CATEGORY:
                    updateCategorySessionContent();//更新分类内容
                    return;
                case SessionID.SESSION_TYPE_GET_MOVIE_TYPE:
                case SessionID.SESSION_TYPE_GET_MOVIELIST_VID:
                case SessionID.SESSION_TYPE_GET_MOVIELIST_VNAME:
                case SessionID.SESSION_TYPE_GET_MOVIELIST_AREA:
                case SessionID.SESSION_TYPE_GET_MOVIELIST_YEAR:
                case SessionID.SESSION_TYPE_GET_MOVIELIST_ACTOR:
                case SessionID.SESSION_TYPE_GET_MOVIELIST_VIP:
                default://默认尝试转化成视频列表
                    break;
            }

            MLog.log(TAG, "Handler mIniType = " + initStage + ",  sessionId=" + msg.what + "userSessionCallback=" + userSessionCallback.toString());
            if (userSessionCallback != null) {
                //MLog.logTAG, "Handler mIniType = " + mIniType + ",  sessionId=" + msg.what);
                userSessionCallback.OnSessionComplete(msg.what, null);
            }
        }
    };

    public void printSessions() {
        String str = "";
        //MLog.logTAG, "printSessions InManager");
        for (Map.Entry<Integer, OPlayerSession> entry : sessions.entrySet()) {
            //MLog.logTAG, entry.getKey() + " : " + entry.getValue().getmVideoCategoryNameList().get(entry.getKey()));
            str = str + entry.getKey() + ":" + entry.getValue().getVideoCategoryNameList().get(entry.getKey()) + ", ";
        }
        MLog.log(TAG, "printSessions InManager:" + str);
    }

    public void printSessionContent(int categoryId) {
        MLog.log(TAG, "printSessionsVideoList categoryId = " + categoryId + " LVideo Count = " + sessions.get(categoryId).getVideos().getCount());
        sessions.get(categoryId).printMovies();
        /*
        for (Map.Entry<Integer, OPlayerSession> entry : mSessions.entrySet())
        {
            MLog.logTAG, "printSessionsVideoList " + entry.getKey() + " : " + entry.getValue().getmVideoCategoryNameList().get(entry.getKey())
                    + " Movies Count =" + entry.getValue().getVideos().size());
            entry.getValue().printMovies();
        }*/
    }

    private void registerUSBBroadcastReceiver() {
        if (usbReceiver == null)
            usbReceiver = new MyBroadcastReceiver();
        else
            return;
        try {
            IntentFilter filter = new IntentFilter();
            filter.addAction("android.intent.action.MEDIA_MOUNTED");
            filter.addAction("android.intent.action.MEDIA_UNMOUNTED");
            filter.addAction("android.intent.action.MEDIA_REMOVED");
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

    private void registerFileBroadcastReceiver() {
        if (fileReceiver == null)
            fileReceiver = new MyBroadcastReceiver();
        else
            return;
        try {
            IntentFilter filter = new IntentFilter();
            filter.addAction("com.zhuchao.android.MEDIAFILE_SCAN_ACTION");
            filter.addAction("com.zhuchao.android.FILE_SCAN_ACTION");
            mContext.registerReceiver(fileReceiver, filter);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    class MyBroadcastReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {  //mStorageManager = (StorageManager) context.getSystemService(Activity.STORAGE_SERVICE);
            Bundle bundle = intent.getExtras();
            Uri data = intent.getData();
            try {
                switch (intent.getAction()) {
                    case Intent.ACTION_MEDIA_MOUNTED:
                    case UsbManager.ACTION_USB_DEVICE_ATTACHED:
                        if (bundle != null) {
                            for (String key : bundle.keySet())
                                MLog.log(TAG, "USB device attached, " + key + ":" + bundle.toString());
                        } else
                            MLog.log(TAG, "USB device attached, " + intent.toString() + "," + data);
                        if (data != null)
                            MLog.log(TAG, "USB device Url = " + data);
                        initSessionFromMobileDisc();
                        break;
                    case UsbManager.ACTION_USB_DEVICE_DETACHED:
                    case UsbManager.ACTION_USB_ACCESSORY_DETACHED:
                    case Intent.ACTION_MEDIA_UNMOUNTED:
                        if (bundle != null) {
                            for (String key : bundle.keySet())
                                MLog.log(TAG, "USB device detached, " + key + ":" + bundle.toString());
                        }
                        if (userSessionCallback != null)
                            userSessionCallback.OnSessionComplete(-1, data+"");
                        break;
                    case "com.zhuchao.android.MEDIAFILE_SCAN_ACTION":
                        if (userSessionCallback != null && bundle != null)
                            userSessionCallback.OnSessionComplete(SessionID.MEDIA_TYPE_ID_AllMEDIA, bundle.getString("FileName"));
                        break;
                    case "com.zhuchao.android.FILE_SCAN_ACTION":
                        if (userSessionCallback != null && bundle != null)
                            userSessionCallback.OnSessionComplete(SessionID.MEDIA_TYPE_ID_AllFILE, bundle.getString("FileName"));
                        break;
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    public void freeFree() {
        try {
            this.initStage = 0;
            this.localSession.getVideos().clear();
            this.mobileUSBSession.getVideos().clear();
            this.fileSession.getVideos().clear();
            this.mobileDiscs.clear();
            categorySession.getVideos().clear();
            sessions.clear();
            if (usbReceiver != null)
                mContext.unregisterReceiver(usbReceiver);
            if (fileReceiver != null)
                mContext.unregisterReceiver(fileReceiver);
            fileReceiver = null;
            usbReceiver = null;
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
