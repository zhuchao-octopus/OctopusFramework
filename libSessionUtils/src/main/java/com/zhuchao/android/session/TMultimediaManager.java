package com.zhuchao.android.session;

import android.annotation.SuppressLint;
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

import com.zhuchao.android.fbase.DataID;
import com.zhuchao.android.fbase.FileUtils;
import com.zhuchao.android.fbase.MMLog;
import com.zhuchao.android.fbase.MessageEvent;
import com.zhuchao.android.fbase.eventinterface.SessionCallback;
import com.zhuchao.android.net.TNetUtils;
import com.zhuchao.android.video.OMedia;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.TreeMap;


public class TMultimediaManager implements SessionCallback {
    private final String TAG = "TMultimediaManager";
    @SuppressLint("StaticFieldLeak")
    private static Context mContext = null;
    private SessionCallback userSessionCallback = null;
    private static Map<Integer, LiveVideoSession> sessions;
    private static LiveVideoSession categorySession;// = new OPlayerSession(ScheduleVideoBean.SESSION_TYPE_MANAGER, this);
    private final LiveVideoSession localVideoSession = new LiveVideoSession(null);
    private final LiveVideoSession localAudioSession = new LiveVideoSession(null);
    private final LiveVideoSession mobileUSBVideoSession = new LiveVideoSession(null);
    private final LiveVideoSession mobileUSBAudioSession = new LiveVideoSession(null);
    private final LiveVideoSession fileVideoSession = new LiveVideoSession(null);
    private final LiveVideoSession fileAudioSession = new LiveVideoSession(null);

    private int mobileSessionId = DataID.SESSION_SOURCE_LOCAL_INTERNAL;
    private Map<String, String> mobileDiscs = new HashMap<String, String>();
    private MyBroadcastReceiver mUsbReceiver = null;//new USBReceiver();
    private MyBroadcastReceiver mFileReceiver = null;//new USBReceiver();
    private boolean mThreadLock1 = false;
    private boolean mThreadLock2 = false;
    private boolean mThreadLock3 = false;
    private int initStage = 0;//0 从网络， 1 //从本地, >=3 已经初始化完成
    private TMultimediaManager tMultimediaManager = null;

    public synchronized TMultimediaManager getInstance(Context context) {
        if (tMultimediaManager == null) tMultimediaManager = new TMultimediaManager(context, null);
        return tMultimediaManager;
    }

    @SuppressLint("SdCardPath")
    public void updateMedias() {
        //setUserSessionCallback(sessionCallback);
        initSessionFromLocal();
        initSessionFromMobileDisc();//usb
        initSessionFromPath("/sdcard/");
    }

    public TMultimediaManager(Context context, SessionCallback sessionCallback) {
        this.userSessionCallback = sessionCallback;
        mContext = context;
        registerUSBBroadcastReceiver();
        //Data.setOplayerSessionRootUrl(hostPath);
        categorySession = new LiveVideoSession(DataID.SESSION_SOURCE_NONE, this);
        sessions = new TreeMap<Integer, LiveVideoSession>(new Comparator<Integer>() {
            @Override
            public int compare(Integer o1, Integer o2) {
                return o2.compareTo(o1);
            }
        });
        updateMedias();
    }

    public void setUserSessionCallback(SessionCallback userSessionCallback) {
        this.userSessionCallback = userSessionCallback;
    }

    public TMultimediaManager Callback(SessionCallback userSessionCallback) {
        this.userSessionCallback = userSessionCallback;
        return this;
    }

    private void initSessionsFromInternet() {
        initStage = 0;
        new Thread() {
            public void run() {
                try {
                    while (true) {
                        if (TNetUtils.isInternetReachable(null)) {
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
                MMLog.log(TAG, "initLocalSessionContent 本地媒体库");
                LiveVideoSession lSession = null;

                lSession = new LiveVideoSession(TMultimediaManager.this);
                lSession.initMediasFromLocal(mContext, DataID.MEDIA_TYPE_ID_VIDEO);
                if (lSession.getVideos().getCount() > 0) addSessionToSessions(mobileSessionId, "本地视频", lSession);
                localVideoSession.addVideos(lSession.getVideos());
                if (userSessionCallback != null)
                    userSessionCallback.OnSessionComplete(MessageEvent.MESSAGE_EVENT_LOCAL_VIDEO, "本地视频", localVideoSession.getAllVideos().getCount());

                lSession = new LiveVideoSession(TMultimediaManager.this);
                lSession.initMediasFromLocal(mContext, DataID.MEDIA_TYPE_ID_AUDIO);
                if (lSession.getVideos().getCount() > 0) addSessionToSessions(mobileSessionId, "本地音乐", lSession);
                localAudioSession.addVideos(lSession.getVideos());
                if (userSessionCallback != null)
                    userSessionCallback.OnSessionComplete(MessageEvent.MESSAGE_EVENT_LOCAL_AUDIO, "本地音乐", localAudioSession.getAllVideos().getCount());

                ///lSession = new LiveVideoSession(TMultimediaManager.this);
                ///lSession.initMediasFromLocal(mContext, DataID.MEDIA_TYPE_ID_PIC);
                ///if (lSession.getVideos().getCount() > 0) addSessionToSessions(mobileSessionId, "本地图片", lSession);
                ///localSession.addVideos(lSession.getVideos());
                ///if (userSessionCallback != null) userSessionCallback.OnSessionComplete(DataID.MEDIA_TYPE_ID_PIC, "本地图片");
                mThreadLock1 = false;
            }
        }.start();
    }

    public void initSessionFromMobileDisc() {
        if (mThreadLock2) return;
        if (userSessionCallback != null && mUsbReceiver == null) registerUSBBroadcastReceiver();

        new Thread() {
            public void run() {
                mThreadLock2 = true;
                mobileDiscs = FileUtils.getUDiscName(mContext);
                if (mobileDiscs.isEmpty()) {
                    MMLog.log(TAG, "No usb device !!!");
                    mThreadLock2 = false;
                    return;
                }
                Iterator<Map.Entry<Integer, LiveVideoSession>> it = sessions.entrySet().iterator();
                while (it.hasNext()) {
                    Map.Entry<Integer, LiveVideoSession> entry = (Map.Entry<Integer, LiveVideoSession>) it.next();
                    if (entry.getKey() < DataID.SESSION_SOURCE_LOCAL_INTERNAL) it.remove();
                }
                mobileUSBVideoSession.getVideos().clear();
                mobileUSBAudioSession.getVideos().clear();
                for (Map.Entry<String, String> entry : mobileDiscs.entrySet()) {
                    initMobileSessionContent(entry.getKey(), entry.getValue());
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
                fileVideoSession.getVideos().clear();
                fileVideoSession.initMediasFromPath(mContext, Path, DataID.MEDIA_TYPE_ID_AUDIO_VIDEO);
                if (userSessionCallback != null)
                    userSessionCallback.OnSessionComplete(MessageEvent.MESSAGE_EVENT_SD_VIDEO, Path, fileVideoSession.getAllVideos().getCount());
                mThreadLock3 = false;
            }
        }.start();
    }

    public void scanFilesFromPath(Context context, String filePath, List<String> FileList) {
        if (mThreadLock3) return;
        if (filePath == null) return;
        //if (FileList == null) return;
        if (!FileUtils.existDirectory(filePath)) return;
        registerFileBroadcastReceiver();
        fileVideoSession.getVideos().clear();
        new Thread() {
            public void run() {
                mThreadLock3 = true;
                fileVideoSession.initFilesFromPath(mContext, filePath, FileList);
                if (userSessionCallback != null)
                    userSessionCallback.OnSessionComplete(DataID.SESSION_SOURCE_PATH, filePath, fileVideoSession.getAllVideos().getCount());
                mThreadLock3 = false;
            }
        }.start();
    }

    private void initMobileSessionContent(final String DeviceName, final String DevicePath) {
        //MMLog.log(TAG, "initMobileSessionContent  " + DeviceName + ":" + DevicePath);
        if (DeviceName == null) return;
        LiveVideoSession mSession = null;

        mSession = new LiveVideoSession(TMultimediaManager.this);
        mSession.initMediasFromPath(mContext, DevicePath, DataID.MEDIA_TYPE_ID_AUDIO_VIDEO);
        if (mSession.getVideos().getCount() > 0) {
            addSessionToSessions(mobileSessionId, DeviceName, mSession);
            mobileUSBVideoSession.addVideos(mSession.getVideos());//USB总共的媒体文件
            mobileUSBAudioSession.addVideos(mSession.getAudios());
        }
        //MMLog.d(TAG,"mobileUSBVideoSession.size="+mobileUSBVideoSession.getAllVideos().getCount());
        //MMLog.d(TAG,"mobileUSBAudioSession.size="+mobileUSBVideoSession.getAllVideos().getCount());
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
        if (userSessionCallback != null) {
            userSessionCallback.OnSessionComplete(MessageEvent.MESSAGE_EVENT_USB_VIDEO, DeviceName + ":" + DevicePath, mobileUSBVideoSession.getAllVideos().getCount());
            userSessionCallback.OnSessionComplete(MessageEvent.MESSAGE_EVENT_USB_AUDIO, DeviceName + ":" + DevicePath, mobileUSBAudioSession.getAllVideos().getCount());
        }
    }

    //初始化非本地分类
    private void initInternetCategorySessions() {
        for (Map.Entry<Integer, String> entry : categorySession.getVideoCategoryNameList().entrySet()) {
            if (entry.getKey() > DataID.SESSION_SOURCE_LOCAL_INTERNAL)//初始化非本地分类
            {
                LiveVideoSession liveVideoSession = new LiveVideoSession(this);
                liveVideoSession.getVideoCategoryNameList().put(entry.getKey(), entry.getValue());
                sessions.put(entry.getKey(), liveVideoSession);
            }
        }
    }

    private void updateCategorySession() { //顶层分类信息，顶层分类会话
        MMLog.log(TAG, "updateTopSession mIniType = " + initStage);
        if (initStage == 0) {
            categorySession.doUpdateSession(DataID.SESSION_TYPE_GET_MOVIE_CATEGORY);
            categorySession.doUpdateSession(DataID.SESSION_TYPE_GET_MOVIE_TYPE);
        }
    }

    private void updateCategorySessionContent() {//获取分类下面的内容
        try {
            for (Map.Entry<Integer, LiveVideoSession> entry : sessions.entrySet()) {
                if (entry.getKey() <= DataID.SESSION_SOURCE_LOCAL_INTERNAL) continue; //本地媒体ID
                MMLog.log(TAG, "updateCategorySessionContent = " + entry.getKey());
                entry.getValue().doUpdateSession(entry.getKey());
            }
            initStage = 3;
            //MLog.logTAG, "initPlaySessionContent mIniType = " + mIniType);
        } catch (Exception e) {
            //MLog.logTAG, "initPlaySessionContent fail mIniType = " + mIniType + ":" + e.toString());
            e.printStackTrace();
        }
    }

    private void addSessionToSessions(int SessionID, String name, LiveVideoSession Session) {
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

    public LiveVideoSession getCategorySession() {
        return categorySession;
    }

    public Map<Integer, LiveVideoSession> getSessions() {
        return sessions;
    }

    public LiveVideoSession getLocalVideoSession() {
        return localVideoSession;
    }

    public LiveVideoSession getUSBVideoSession() {
        return mobileUSBVideoSession;
    }

    public LiveVideoSession getFileVideoSession() {
        return fileVideoSession;
    }

    public LiveVideoSession getLocalAudioSession() {
        return localAudioSession;
    }

    public LiveVideoSession getUSBAudioSession() {
        return mobileUSBAudioSession;
    }

    public LiveVideoSession getFileAudioSession() {
        return fileAudioSession;
    }

    public String getCategoryName(int categoryId) {
        return Objects.requireNonNull(sessions.get(categoryId)).getVideoCategoryNameList().get(categoryId);
    }

    public List<OMedia> getAllMedias() {
        List<OMedia> allOMedia = new ArrayList<>();
        for (Map.Entry<Integer, LiveVideoSession> entry : sessions.entrySet()) {
            /*
            MLog.logTAG, "printSessionsVideoList " + entry.getKey() + " : " + entry.getValue().getVideoCategoryNameList().get(entry.getKey())
                    + " Movies Count =" + entry.getValue().getVideos().size());
            */
            for (HashMap.Entry<String, Object> m : entry.getValue().getVideos().getMap().entrySet())
                allOMedia.add((OMedia) m.getValue());
        }
        return allOMedia;
    }

    public Map<String, String> getMobileDiscs() {
        return mobileDiscs;
    }

    public void printSessions() {
        String str = "";
        //MLog.logTAG, "printSessions InManager");
        for (Map.Entry<Integer, LiveVideoSession> entry : sessions.entrySet()) {
            //MLog.logTAG, entry.getKey() + " : " + entry.getValue().getmVideoCategoryNameList().get(entry.getKey()));
            str = str + entry.getKey() + ":" + entry.getValue().getVideoCategoryNameList().get(entry.getKey()) + ", ";
        }
        MMLog.log(TAG, "printSessions InManager:" + str);
    }

    public void printSessionContent(int categoryId) {
        MMLog.log(TAG, "printSessionsVideoList categoryId = " + categoryId + " LVideo Count = " + sessions.get(categoryId).getVideos().getCount());
        Objects.requireNonNull(sessions.get(categoryId)).printMovies();
        /*
        for (Map.Entry<Integer, OPlayerSession> entry : mSessions.entrySet())
        {
            MLog.logTAG, "printSessionsVideoList " + entry.getKey() + " : " + entry.getValue().getmVideoCategoryNameList().get(entry.getKey())
                    + " Movies Count =" + entry.getValue().getVideos().size());
            entry.getValue().printMovies();
        }*/
    }

    public void printUSBList() {
        for (Map.Entry<String, String> entry : mobileDiscs.entrySet()) {
            MMLog.d(TAG, entry.getKey() + "=" + entry.getValue());
        }
    }

    @Override
    public void OnSessionComplete(int session_id, String result, int count) {
        Message msg = Message.obtain();
        switch (session_id) {
            case DataID.SESSION_TYPE_GET_MOVIELIST_ALLTV:
            case DataID.SESSION_TYPE_GET_MOVIELIST_ALLMOVIE:
            case DataID.SESSION_TYPE_GET_MOVIELIST_ALLMOVIE2:
                msg.what = session_id;//返回的是分类下面的内容，内容实体已经在相应会话中被处理
                myHandler.sendMessage(msg);
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
                if (categorySession.getVideoCategoryNameList() != null) {
                    if (categorySession.getVideoCategoryNameList().size() > 0) {
                        initInternetCategorySessions(); //根据类别信息初始化会话对象数组
                        initStage = 2;//初始化分类信息完成
                        msg.what = session_id;
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

    private final Handler myHandler = new Handler(Looper.myLooper()) {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.what) {
                case DataID.SESSION_TYPE_GET_MOVIELIST_ALLTV:
                case DataID.SESSION_TYPE_GET_MOVIELIST_ALLMOVIE:
                case DataID.SESSION_TYPE_GET_MOVIELIST_ALLMOVIE2:
                    printSessionContent(msg.what);
                    break;
                case DataID.SESSION_TYPE_GET_MOVIE_CATEGORY:
                    updateCategorySessionContent();//更新分类内容
                    return;
                case MessageEvent.MESSAGE_EVENT_USB_MOUNTED:
                    initSessionFromMobileDisc();
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
            if (userSessionCallback != null) {
                //MLog.logTAG, "Handler mIniType = " + mIniType + ",  sessionId=" + msg.what);
                userSessionCallback.OnSessionComplete(msg.what, null, 0);
            }
        }
    };

    private void registerUSBBroadcastReceiver() {
        if (mUsbReceiver == null) mUsbReceiver = new MyBroadcastReceiver();
        else return;
        try {
            IntentFilter intentFilter = new IntentFilter();
            intentFilter.addAction(Intent.ACTION_MEDIA_SHARED); //如果SDCard未安装,并通过USB大容量存储共享返回
            intentFilter.addAction(Intent.ACTION_MEDIA_MOUNTED); //表明sd对象是存在并具有读/写权限
            intentFilter.addAction(Intent.ACTION_MEDIA_UNMOUNTED); //SDCard已卸掉,如果SDCard是存在但没有被安装
            intentFilter.addAction(Intent.ACTION_MEDIA_CHECKING); //表明对象正在磁盘检查
            intentFilter.addAction(Intent.ACTION_MEDIA_EJECT); //物理的拔出 SDCARD
            intentFilter.addAction(Intent.ACTION_MEDIA_REMOVED); //完全拔出
            intentFilter.addDataScheme("file");
            intentFilter.addAction(UsbManager.ACTION_USB_DEVICE_ATTACHED);
            intentFilter.addAction(UsbManager.ACTION_USB_DEVICE_DETACHED);

            //intentFilter.addAction(UsbManager.ACTION_USB_STATE);
            intentFilter.addAction(UsbManager.ACTION_USB_ACCESSORY_ATTACHED);
            intentFilter.addAction(UsbManager.ACTION_USB_ACCESSORY_DETACHED);
            mContext.registerReceiver(mUsbReceiver, intentFilter);
        } catch (Exception e) {
            MMLog.e(TAG, String.valueOf(e));
        }
    }

    private void registerFileBroadcastReceiver() {
        if (mFileReceiver == null) mFileReceiver = new MyBroadcastReceiver();
        else return;
        try {
            IntentFilter filter = new IntentFilter();
            filter.addAction("com.zhuchao.android.MEDIAFILE_SCAN_ACTION");
            filter.addAction("com.zhuchao.android.FILE_SCAN_ACTION");
            mContext.registerReceiver(mFileReceiver, filter);
        } catch (Exception e) {
            MMLog.e(TAG, String.valueOf(e));
        }
    }

    class MyBroadcastReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {  //mStorageManager = (StorageManager) context.getSystemService(Activity.STORAGE_SERVICE);
            Bundle bundle = intent.getExtras();
            Uri data = intent.getData();
            MMLog.d(TAG,TAG +" action="+intent.getAction());
            try {
                switch (intent.getAction()) {
                    //case UsbManager.ACTION_USB_DEVICE_ATTACHED:
                    case Intent.ACTION_MEDIA_MOUNTED:
                        ///if (bundle != null) {
                        ///    for (String key : bundle.keySet())
                        ///        MMLog.log(TAG, "USB attached, " + key + ":" + bundle.toString());
                        ///} else MMLog.log(TAG, "USB attached, " + intent.toString() + "," + data);
                        ///if (data != null) MMLog.log(TAG, "USB Url = " + data);
                        myHandler.sendEmptyMessage(MessageEvent.MESSAGE_EVENT_USB_MOUNTED);
                        //initSessionFromMobileDisc();
                        break;
                    case UsbManager.ACTION_USB_DEVICE_DETACHED:
                    case UsbManager.ACTION_USB_ACCESSORY_DETACHED:
                    case Intent.ACTION_MEDIA_UNMOUNTED:
                        ///if (bundle != null) {
                        ///    for (String key : bundle.keySet())
                        ///        MMLog.log(TAG, "USB device detached, " + key + ":" + bundle.toString());
                        ///}
                        ///if (userSessionCallback != null) userSessionCallback.OnSessionComplete(MessageEvent.MESSAGE_EVENT_USB_UNMOUNT, String.valueOf(data), 0);
                        break;
                    case "com.zhuchao.android.MEDIAFILE_SCAN_ACTION":
                        ///if (userSessionCallback != null && bundle != null)
                        ///    userSessionCallback.OnSessionComplete(DataID.MEDIA_TYPE_ID_AllMEDIA, bundle.getString("FileName"));
                        break;
                    case "com.zhuchao.android.FILE_SCAN_ACTION":
                        ///if (userSessionCallback != null && bundle != null)
                        ///    userSessionCallback.OnSessionComplete(DataID.MEDIA_TYPE_ID_AllFILE, bundle.getString("FileName"));
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
            this.localVideoSession.getVideos().clear();
            this.mobileUSBVideoSession.getVideos().clear();
            this.fileVideoSession.getVideos().clear();
            this.mobileDiscs.clear();
            categorySession.getVideos().clear();
            sessions.clear();
            if (mUsbReceiver != null) mContext.unregisterReceiver(mUsbReceiver);
            if (mFileReceiver != null) mContext.unregisterReceiver(mFileReceiver);
            mFileReceiver = null;
            mUsbReceiver = null;
        } catch (Exception e) {
            //e.printStackTrace();
            MMLog.e(TAG, String.valueOf(e));
        }
    }
}
