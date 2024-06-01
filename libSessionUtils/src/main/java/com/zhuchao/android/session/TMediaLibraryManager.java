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

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


public class TMediaLibraryManager implements SessionCallback {
    private static final String TAG = "TMediaLibraryManager";
    public static final String ACTION_MEDIA_SCANNER_SCAN_DIR = "android.intent.action.MEDIA_SCANNER_SCAN_DIR";
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
    private final TMediaMetadataManager tTMediaMetadataManager = new TMediaMetadataManager();
    private Map<String, String> mMobileUSBDiscs = new HashMap<String, String>();
    ///private GlobalBroadcastReceiver mUSBBroadcastReceiver = null;//new USBReceiver();
    ///private MyBroadcastReceiver mFileReceiver = null;//new USBReceiver();
    ///private boolean mThreadLock1 = false;
    ///private boolean mThreadLock2 = false;
    ///private boolean mThreadLock3 = false;
    ///private int initStage = 0;//0 从网络， 1 //从本地, >=3 已经初始化完成
    private TMediaLibraryManager tMediaLibraryManager = null;

    public synchronized TMediaLibraryManager getInstance(Context context) {
        if (tMediaLibraryManager == null) {
            if (context == null) tMediaLibraryManager = new TMediaLibraryManager();
            else tMediaLibraryManager = new TMediaLibraryManager(context, null);
        }
        if (mContext == null) mContext = context;
        return tMediaLibraryManager;
    }

    public TMediaLibraryManager() {
    }

    public TMediaLibraryManager(Context context, SessionCallback sessionCallback) {
        this.mUserSessionCallback = sessionCallback;
        Cabinet.getEventBus().registerEventObserver(this);
        mContext = context;
        MMLog.d(TAG, "Initial TMediaManager from local!");
    }

    @SuppressLint("SdCardPath")
    public void updateLocalMedias() {
        //setUserSessionCallback(sessionCallback);
        initSessionFromLocal();
        initSessionFromMobileDisc();//usb
        initSDSessionFromPath();
    }

    public void setUserSessionCallback(SessionCallback mUserSessionCallback) {
        this.mUserSessionCallback = mUserSessionCallback;
    }

    public TMediaLibraryManager Callback(SessionCallback userSessionCallback) {
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
            for (Map.Entry<String, String> entry : mMobileUSBDiscs.entrySet()) {
                Object obj = mAllSessions.get(makeSessionName(entry.getKey(), DataID.MEDIA_TYPE_ID_VIDEO));
                if (obj != null) {
                    LiveVideoSession lvs = (LiveVideoSession) obj;
                    liveVideoSession.addVideos(lvs.getAllVideos());
                }
            }
        } else {
            for (Map.Entry<String, String> entry : mMobileUSBDiscs.entrySet()) {
                Object obj = mAllSessions.get(makeSessionName(entry.getKey(), DataID.MEDIA_TYPE_ID_VIDEO));
                if (obj != null) liveVideoSession = (LiveVideoSession) obj;
            }
        }
        return liveVideoSession;
    }

    public LiveVideoSession getUSBAudioSession() {
        LiveVideoSession liveVideoSession = new LiveVideoSession(null);
        if (mMobileUSBDiscs.size() > 1) {
            for (Map.Entry<String, String> entry : mMobileUSBDiscs.entrySet()) {
                Object obj = mAllSessions.get(makeSessionName(entry.getKey(), DataID.MEDIA_TYPE_ID_AUDIO));
                if (obj != null) {
                    LiveVideoSession lvs = (LiveVideoSession) obj;
                    liveVideoSession.addVideos(lvs.getAllVideos());
                }
            }
        } else {
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

    public TMediaMetadataManager getMediaMetadataManager() {
        return tTMediaMetadataManager;
    }

    /////////////////////////////////////////////////////////////////////////////////////
    /////////////////////////////////////////////////////////////////////////////////////
    public void startMediaScanning(String scanDir) {
        MMLog.d(TAG, "Start scanning " + scanDir + ":" + Uri.fromFile(new File(scanDir)));
        Intent scanIntent = new Intent(ACTION_MEDIA_SCANNER_SCAN_DIR);
        scanIntent.setData(Uri.fromFile(new File(scanDir)));
        mContext.sendBroadcast(scanIntent);
    }

    public void initSessionFromLocal() {
        singleTaskSearchLocalDisc();
    }

    //第一次重置式初始化
    public void initSessionFromMobileDisc() {
        if(mContext != null) {
            mMobileUSBDiscs = FileUtils.getMobileDiscName(mContext);
            for (Map.Entry<String, String> entry : mMobileUSBDiscs.entrySet()) {
                singleTaskSearchMobileDisc(entry.getKey(), entry.getValue());
            }
        }
        else {
            MMLog.d(TAG,"Init Session From Mobile Disc mContext is null!");
        }
    }

    @SuppressLint("SdCardPath")
    private void initSDSessionFromPath() {//"/sdcard/"
        singleTaskSearchLocalSD();
    }

    private void initMobileSessionContent(final String DeviceName, final String DevicePath) {
        ///MMLog.log(TAG, "initMobileSessionContent  " + DeviceName + ":" + DevicePath);
        if (FileUtils.EmptyString(DeviceName) || !FileUtils.existDirectory(DevicePath)) return;

        LiveVideoSession mSession = new LiveVideoSession(TMediaLibraryManager.this);
        LiveVideoSession mMobileUSBVideoSession = new LiveVideoSession(TMediaLibraryManager.this);
        LiveVideoSession mMobileUSBAudioSession = new LiveVideoSession(TMediaLibraryManager.this);

        ///mSession.initMediasFromUSB(mContext, DevicePath, DataID.MEDIA_TYPE_ID_AUDIO_VIDEO, mMobileUSBVideoSession, mMobileUSBAudioSession);
        ///mSession.initMediasFromPath(mContext, DevicePath, DataID.MEDIA_TYPE_ID_AUDIO_VIDEO, mMobileUSBVideoSession, mMobileUSBAudioSession);
        mSession.synchronizationInitMediasFromPath(mContext, DevicePath, DataID.MEDIA_TYPE_ID_AUDIO_VIDEO, mMobileUSBVideoSession, mMobileUSBAudioSession, mLocalVideoSession, mLocalAudioSession);
        addLocalSessionToSessions(makeSessionName(DeviceName, DataID.MEDIA_TYPE_ID_VIDEO), mMobileUSBVideoSession);
        addLocalSessionToSessions(makeSessionName(DeviceName, DataID.MEDIA_TYPE_ID_AUDIO), mMobileUSBAudioSession);

        tTMediaMetadataManager.updateArtistAndAlbum(mMobileUSBAudioSession.getVideoList());
        ///tTMediaMetadataManager.updateArtistAndAlbum(mMobileUSBVideoSession.getVideoList());

        ///synchronizationMediaLibrary();
        userSessionCallback(mMobileUSBVideoSession, MessageEvent.MESSAGE_EVENT_USB_VIDEO, DeviceName + ":" + DevicePath);
        userSessionCallback(mMobileUSBAudioSession, MessageEvent.MESSAGE_EVENT_USB_AUDIO, DeviceName + ":" + DevicePath);
    }

    private void synchronizationMediaLibrary()///无需同步
    {
        TTask tTask = TTaskManager.getSingleTaskFor(TAG + ".synchronization.MediaLibrary").resetAll();
        if (!tTask.isBusy()) {
            tTask.invoke(tag -> {
                LiveVideoSession mSession;
                for (Map.Entry<String, Object> entry : mAllSessions.getAll().entrySet()) {
                    mSession = ((LiveVideoSession) entry.getValue());
                    for (HashMap.Entry<String, Object> m : mSession.getAllVideos().getMap().entrySet()) {
                        OMedia oMedia = (OMedia) m.getValue();
                        if (oMedia.isAudio()) {
                            OMedia oMedia_a = mLocalAudioSession.getVideoList().findByPath(oMedia.getPathName());
                            if (oMedia_a == null) {
                                mLocalAudioSession.getVideoList().add(oMedia);
                            }
                        } else if (oMedia.isVideo()) {
                            OMedia oMedia_a = mLocalVideoSession.getVideoList().findByPath(oMedia.getPathName());
                            if (oMedia_a == null) {
                                mLocalVideoSession.getVideoList().add(oMedia);
                            }
                        }
                    }
                }
            }).startAgain();
        }
    }

    private void userSessionCallback(LiveVideoSession liveVideoSession, int mobileSessionId, String message)//DeviceName + ":" + DevicePath
    {
        ///先更新本地播放管理
        if (mUserSessionCallback != null) {///通知本地播放器
            if (liveVideoSession != null)
                mUserSessionCallback.OnSessionComplete(mobileSessionId, message, liveVideoSession.getAllVideos().getCount());
            else mUserSessionCallback.OnSessionComplete(mobileSessionId, message, 0);
        }
        ///通知本地总线，通过代理更新客户端
        PEventCourier pEventCourier = new PEventCourier(this.getClass(), mobileSessionId);
        pEventCourier.setTarget(MessageEvent.OCTOPUS_COMPONENT_NAME_MIDDLE_SERVICE);//消息发往代理PROXY
        Cabinet.getEventBus().post(pEventCourier);//通知IPC,通知外部客户端
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

    public void singleTaskSearchLocalDisc() {
        if (mContext == null) return;
        TTask tTask = TTaskManager.getSingleTaskFor(TAG + ".LocalDisc").resetAll();
        if (!tTask.isBusy()) {
            tTask.invoke(tag -> {
                mLocalVideoSession.initMediasFromLocal(mContext, DataID.MEDIA_TYPE_ID_VIDEO);
                ///addLocalSessionToSessions("本地视频", mLocalVideoSession);
                mLocalAudioSession.initMediasFromLocal(mContext, DataID.MEDIA_TYPE_ID_AUDIO);
                ///addLocalSessionToSessions("本地音乐", mLocalAudioSession);
                ///tTMediaMetadataManager.updateArtistAndAlbum(mLocalVideoSession.getVideoList());
                tTMediaMetadataManager.updateArtistAndAlbum(mLocalAudioSession.getVideoList());

                userSessionCallback(mLocalVideoSession, MessageEvent.MESSAGE_EVENT_LOCAL_VIDEO, "本地视频");
                userSessionCallback(mLocalAudioSession, MessageEvent.MESSAGE_EVENT_LOCAL_AUDIO, "本地音乐");
            }).startAgain();
        }
    }

    public void singleTaskSearchMobileDisc(final String finalDeviceName, final String finalDevicePath) {
        ///MMLog.d(TAG,finalDeviceName +" 1 "+finalDevicePath);
        if (FileUtils.EmptyString(finalDeviceName) || !FileUtils.existDirectory(finalDevicePath)) return;
        ///MMLog.d(TAG,finalDeviceName +" 2 "+finalDevicePath);
        TTask tTask = TTaskManager.getSingleTaskFor(TAG + "." + finalDeviceName).resetAll();
        if (!tTask.isBusy()) {
            tTask.invoke(tag -> initMobileSessionContent(finalDeviceName, finalDevicePath)).startAgain();
        }
    }

    public void singleTaskSearchLocalSD() {
        TTask tTask = TTaskManager.getSingleTaskFor(TAG + ".SD").resetAll();
        @SuppressLint("SdCardPath") String sd_path = "/sdcard/";
        if (!tTask.isBusy()) {
            tTask.invoke(tag -> {
                mSDVideoSession.getAllVideos().clear();
                mSDAudioSession.getAllVideos().clear();
                ///mSDVideoSession.initMediasFromPath(mContext, sd_path, DataID.MEDIA_TYPE_ID_AUDIO_VIDEO, mSDVideoSession, mSDAudioSession);
                mSDVideoSession.synchronizationInitMediasFromPath(mContext, sd_path, DataID.MEDIA_TYPE_ID_AUDIO_VIDEO, mSDVideoSession, mSDAudioSession, mLocalVideoSession, mLocalAudioSession);

                ///updateArtistAndAlbum(mSDVideoSession.getVideoList(), mMediaMetadataList);
                ///updateArtistAndAlbum(mSDAudioSession.getVideoList(), mMediaMetadataList);
                userSessionCallback(mSDVideoSession, MessageEvent.MESSAGE_EVENT_SD_VIDEO, sd_path);
                userSessionCallback(mSDAudioSession, MessageEvent.MESSAGE_EVENT_SD_AUDIO, sd_path);
            }).startAgain();
        }
    }

    public String makeSessionName(String name, int id) {
        return name + "." + id;
    }

    public void testScanning() {
        for (Map.Entry<String, String> entry : mMobileUSBDiscs.entrySet()) {
            singleTaskSearchMobileDisc(entry.getKey(), entry.getValue());
        }
    }

    @TCourierSubscribe(threadMode = MethodThreadMode.threadMode.BACKGROUND)
    public boolean onTCourierSubscribeEvent(EventCourierInterface courierInterface) {
        ///MMLog.d(TAG, courierInterface.toStr());
        String usbPath = null;
        Intent intent = null;
        Bundle bundle = null;
        Uri data = null;
        switch (courierInterface.getId()) {
            case MessageEvent.MESSAGE_EVENT_USB_MOUNTED:
                if (mContext == null) break;
                mMobileUSBDiscs = FileUtils.getMobileDiscName(mContext);
                ///printUSBList();

                if (courierInterface.getObj() != null) {
                    intent = (Intent) courierInterface.getObj();
                    bundle = intent.getExtras();
                    data = intent.getData();
                    if (data != null) {
                        usbPath = data.getPath();
                        ///startMediaScanning(usbPath);
                        singleTaskSearchMobileDisc(getUSBNameByValue(usbPath), usbPath);
                    }
                    ///for (String key : bundle.keySet())
                    /// MMLog.log(TAG, "USB:" + key + ":" + bundle.toString() + " " + subName1);
                }
                ///mMyHandler.sendEmptyMessage(MessageEvent.MESSAGE_EVENT_USB_MOUNTED);
                break;

            case MessageEvent.MESSAGE_EVENT_USB_UNMOUNT:
                if (mContext == null) break;
                mMobileUSBDiscs = FileUtils.getMobileDiscName(mContext);
                String subName2 = null;
                String usbName = null;
                if (courierInterface.getObj() != null) {
                    intent = (Intent) courierInterface.getObj();
                    bundle = intent.getExtras();
                    data = intent.getData();
                    if (data != null) subName2 = data.getPath();
                    if (subName2 != null) {
                        usbName = getUSBNameByValue(subName2);
                        if (FileUtils.EmptyString(usbName)) {
                            mAllSessions.delete(makeSessionName(usbName, DataID.MEDIA_TYPE_ID_VIDEO));
                            mAllSessions.delete(makeSessionName(usbName, DataID.MEDIA_TYPE_ID_AUDIO));
                            userSessionCallback(null, MessageEvent.MESSAGE_EVENT_USB_VIDEO, usbName + ":" + subName2);
                            userSessionCallback(null, MessageEvent.MESSAGE_EVENT_USB_AUDIO, usbName + ":" + subName2);
                        }
                    }
                    ///for (String key : bundle.keySet())
                    ///MMLog.log(TAG, "USB:" + key + ":" + bundle.toString() + " " + subName2);
                }
                ///printUSBList();
                ///mMyHandler.sendEmptyMessage(MessageEvent.MESSAGE_EVENT_USB_UNMOUNT);
                break;
            case MessageEvent.MESSAGE_EVENT_USB_SCANNING_FINISHED:
                if (mContext == null) break;
                intent = (Intent) courierInterface.getObj();
                bundle = intent.getExtras();
                data = intent.getData();
                if (data != null) singleTaskSearchLocalDisc();
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
            tTMediaMetadataManager.free();
            Cabinet.getEventBus().unRegisterEventObserver(this);
        } catch (Exception e) {
            ///e.printStackTrace();
            MMLog.e(TAG, String.valueOf(e));
        }
    }

}
