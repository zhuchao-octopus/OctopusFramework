package com.zhuchao.android.playsession;

import android.content.Context;
import android.os.Handler;
import android.os.Message;
import android.util.Log;

import com.zhuchao.android.libfilemanager.FileUtils;
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

    private OPlayerSession  mLocalSession = new OPlayerSession(null);
    private OPlayerSession mMobileSession = new OPlayerSession(null);
    private OPlayerSession  mMomileTFSession = new OPlayerSession(null);

    public OPlayerSessionManager(Context context, String hostPath, SessionCompleteCallback userSessionCallback) {
        this.userSessionCallback = userSessionCallback;
        this.mContext = context;
        Data.setOplayerSessionRootUrl(hostPath);

        mTopSession = new OPlayerSession(Data.SESSION_TYPE_MANAGER, this);
        //mLocalSession = new OPlayerSession(ScheduleVideoBean.SESSION_TYPE_LOCALMEDIA, this);
        mSessions = new TreeMap<Integer, OPlayerSession>(new Comparator<Integer>() {
            @Override
            public int compare(Integer o1, Integer o2) {
                return o2.compareTo(o1);
            }
        });

        try {
            initSessionsFromInternet();
            initLocalSessionContent();

            initSessionFromMobileDisc();
            initSessionFromMobileTFDisc("/storage/card/");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void initSessionsFromInternet()
    {
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
                Log.d(TAG, "initLocalSessionContent 本地媒体库 mIniType= " + mIniType);
                OPlayerSession   LocalSession = null;

                LocalSession = new OPlayerSession(OPlayerSessionManager.this);
                LocalSession.initMediasFromLocal(mContext,Data.MEDIA_SOURCE_ID_VIDEO);
                if(LocalSession.getVideos().size() > 0)
                addOtherSeesionToSessions(mMoBileSessionId, "本地视频",LocalSession);
                mLocalSession.addVideos(LocalSession.getVideos());

                LocalSession = new OPlayerSession(OPlayerSessionManager.this);
                LocalSession.initMediasFromLocal(mContext,Data.MEDIA_SOURCE_ID_AUDIO);
                if(LocalSession.getVideos().size() > 0)
                addOtherSeesionToSessions(mMoBileSessionId,"本地音乐",LocalSession);
                mLocalSession.addVideos(LocalSession.getVideos());

                LocalSession = new OPlayerSession(OPlayerSessionManager.this);
                LocalSession.initMediasFromLocal(mContext,Data.MEDIA_SOURCE_ID_PIC);
                if(LocalSession.getVideos().size() > 0)
                addOtherSeesionToSessions(mMoBileSessionId,"本地图片",LocalSession);
                mLocalSession.addVideos(LocalSession.getVideos());
                //mTopSession.printCategory();//mLocalSession.printCategory();
                //printSessionsInManager();
                if (userSessionCallback != null)
                    userSessionCallback.OnSessionComplete(Data.SESSION_TYPE_LOCALMEDIA, LocalSession.toString());
            }
        }.start();
    }

    public void initSessionFromMobileDisc() {

        new Thread() {
            public void run() {
                MobileDiscs = FileUtils.getUDiscName(mContext);

                //for (Map.Entry<Integer, OPlayerSession> entry : mSessions.entrySet())
                //{
                //    if(entry.getKey() > 0 && entry.getKey() < Data.SESSION_TYPE_LOCALMEDIA)
                //        mSessions.remove(entry.getKey());
                //}

                Iterator it = mSessions.entrySet().iterator();
                while(it.hasNext()) {
                    Map.Entry<Integer, OPlayerSession> entry = (Map.Entry<Integer, OPlayerSession>) it.next();
                    if(entry.getKey() > 0 && entry.getKey() < Data.SESSION_TYPE_LOCALMEDIA)
                    it.remove();
                }

                for (Map.Entry<String, String> entry : MobileDiscs.entrySet()) {
                    initMobileSessionContent(entry.getKey(), entry.getValue());
                }

                //printSessionsInManager();
                if (userSessionCallback != null)
                    userSessionCallback.OnSessionComplete(Data.SESSION_TYPE_MOBILEMEDIA9, MobileDiscs.toString());
            }
        }.start();
    }

    public void initSessionFromMobileTFDisc(final String TFPath) {

        new Thread() {
            public void run() {
                if(TFPath != null) {
                    mMomileTFSession.getVideos().clear();
                    mMomileTFSession.initMediasFromPath(TFPath, Data.MEDIA_SOURCE_ID_AllMEDIA);
                }
            }
        }.start();
    }
    private void initMobileSessionContent(final String DeviceName, final String DevicePath) {
        Log.d(TAG, "initMobileSessionContent DeviceName = " + DevicePath);
        if (DeviceName == null) return;
        OPlayerSession   MoBileSession;
        mMobileSession.getVideos().clear();

        MoBileSession = new OPlayerSession(OPlayerSessionManager.this);
        MoBileSession.initMediasFromPath(DevicePath,Data.MEDIA_SOURCE_ID_VIDEO);
        if(MoBileSession.getVideos().size() > 0)
        addOtherSeesionToSessions(mMoBileSessionId,DeviceName+"视频",MoBileSession);

        if(!DevicePath.contains("SD") || !DevicePath.contains("TF"))
        mMobileSession.addVideos(MoBileSession.getVideos());

        MoBileSession = new OPlayerSession(OPlayerSessionManager.this);
        MoBileSession.initMediasFromPath(DevicePath,Data.MEDIA_SOURCE_ID_AUDIO);
        if(MoBileSession.getVideos().size() > 0)
        addOtherSeesionToSessions(mMoBileSessionId,DeviceName+"音乐",MoBileSession);

        if(!DevicePath.contains("SD") || !DevicePath.contains("TF"))
        mMobileSession.addVideos(MoBileSession.getVideos());

        MoBileSession = new OPlayerSession(OPlayerSessionManager.this);
        MoBileSession.initMediasFromPath(DevicePath,Data.MEDIA_SOURCE_ID_PIC);
        if(MoBileSession.getVideos().size() > 0)
        addOtherSeesionToSessions(mMoBileSessionId,DeviceName+"图片",MoBileSession);

        if(!DevicePath.contains("SD") || !DevicePath.contains("TF"))
        mMobileSession.addVideos(MoBileSession.getVideos());

    }

    private void initPlaySessionContent() {
        for (Map.Entry<Integer, OPlayerSession> entry : mSessions.entrySet()) {
            Log.d(TAG, "initPlaySessionContent = " + entry.getKey());
            entry.getValue().doUpdateSession(entry.getKey());
        }
        mIniType = 3;
        Log.d(TAG, "initPlaySessionContent mIniType = " + mIniType);
    }

    private synchronized void addOtherSeesionToSessions(int SessionID,String name,OPlayerSession Session)
    {
        Session.getmVideoCategoryNameList().put(SessionID, name);

        if(mTopSession.getmVideoCategoryNameList().containsKey(SessionID))
            mTopSession.getmVideoCategoryNameList().remove(SessionID);

        mTopSession.getmVideoCategoryNameList().put(SessionID, name);

        if(mSessions.containsKey(SessionID))
            mSessions.remove(SessionID);

        mSessions.put(SessionID, Session);
        mMoBileSessionId--;
    }

    private void initOPlayerSessions() {
        for (Map.Entry<Integer, String> entry : mTopSession.getmVideoCategoryNameList().entrySet()) {
            //String cName = entry.getValue();
            if(entry.getKey() > Data.SESSION_TYPE_LOCALMEDIA) {
                OPlayerSession oPlayerSession = new OPlayerSession(this);
                oPlayerSession.getmVideoCategoryNameList().put(entry.getKey(), entry.getValue());
                mSessions.put(entry.getKey(), oPlayerSession);
            }
        }
    }

    public boolean isInitComplete() {
        if (mIniType >= 3)
            return true;
        else
            return false;
    }

    public OPlayerSession getmTopSession() {
        return mTopSession;
    }

    public OPlayerSession getLocalSession() {
        return mLocalSession;
    }

    public OPlayerSession getMoBileSession() {
        return mMobileSession;
    }

    public OPlayerSession getMomileTFSession() {
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
            case Data.SESSION_TYPE_GET_MOVIE_TYPE:
                break;
            default:
                msg.what = sessionId;
                myHandler.sendMessage(msg);
                break;
        }
        Log.d(TAG, "OnSessionComplete mIniType = " + mIniType + ",  sessionId=" + sessionId);
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

            if (userSessionCallback != null)
                userSessionCallback.OnSessionComplete(msg.what, null);
        }
    };
}
