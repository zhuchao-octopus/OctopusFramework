package com.zhuchao.android.session;

import android.content.Context;
import android.text.TextUtils;

import com.zhuchao.android.libfileutils.FileUtils;
import com.zhuchao.android.libfileutils.MediaFile;
import com.zhuchao.android.libfileutils.DataID;
import com.zhuchao.android.libfileutils.bean.LMusic;
import com.zhuchao.android.libfileutils.bean.LVideo;
import com.zhuchao.android.session.PaserBean.MovieListBean;
import com.zhuchao.android.utils.MMLog;
import com.zhuchao.android.video.Movie;
import com.zhuchao.android.video.OMedia;
import com.zhuchao.android.video.VideoList;

import java.io.File;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

/*
Map<Integer, Integer> map = new HashMap<Integer, Integer>();
for (Map.Entry<Integer, Integer> entry : map.entrySet()) {
  System.out.println("Key = " + entry.getKey() + ", Value = " + entry.getValue());

Map<Integer, Integer> map = new HashMap<Integer, Integer>();
for (Integer key : map.keySet())
        System.out.println("Key = " + key);

for (Integer value : map.values())
        System.out.println("Value = "+value);
*/

//会话
public class LiveVideoSession implements SessionCallback {
    public final String TAG = "OPlayerSession ---> ";
    protected int sessionId = DataID.SESSION_SOURCE_NONE;//会话ID
    private SessionCallback userSessionCallback = null;//会话回调
    private ImplementProxy Ilpr = null;//new ImplementProxy();执行代理
    private VideoList videoList = new VideoList(null);//会话内容
    private Map<Integer, String> videoTypeNameList = new TreeMap<Integer, String>();
    private int mPageIndexOrVid = 1;
    private int mTotalPages = 1;


    private Map<Integer, String> videoCategoryNameList = new TreeMap<Integer, String>(new Comparator<Integer>()
    {
        @Override
        public int compare(Integer o1, Integer o2) {
            return o2.compareTo(o1);
        }
    });

    public LiveVideoSession(SessionCallback callback) {
        userSessionCallback = callback;
        //Ilpr = new ImplementProxy(this);
    }

    public LiveVideoSession(int sessionId, SessionCallback callback) {
        userSessionCallback = callback;
        this.sessionId = sessionId;
        //Ilpr = new ImplementProxy(this);
    }

    protected void doStart()//处理内置session,Id存储在内部类 网络请求，通过代理实现
    {
        if (sessionId <= DataID.SESSION_SOURCE_LOCAL_INTERNAL) return;
        switch (sessionId) {
            case DataID.SESSION_TYPE_GET_MOVIELIST_ALLTV:
            case DataID.SESSION_TYPE_GET_MOVIELIST_ALLMOVIE:
            case DataID.SESSION_TYPE_GET_MOVIELIST_ALLMOVIE2:
                Ilpr.performanceUrl(sessionId, DataID.getActionUrl(sessionId, this.getSessionName(), mPageIndexOrVid));
                break;
            case DataID.SESSION_TYPE_GET_MOVIE_CATEGORY:
            case DataID.SESSION_TYPE_GET_MOVIE_TYPE:
            default:
                Ilpr.performanceUrl(sessionId, DataID.getActionUrl(sessionId, this.getSessionName(), 1));
                break;
        }
    }

    private void doUpdate() {
        doStart();
    }

    private void doUpdate(int pageIndex) {
        if (pageIndex < 1)
            mPageIndexOrVid = 1;
        if (pageIndex > mTotalPages)
            mPageIndexOrVid = mTotalPages;
        else
            mPageIndexOrVid = pageIndex;
        doUpdate();
    }

    //用于第一种构造方法
    public void doUpdateSession(int SessionID) {
        this.sessionId = SessionID;
        doUpdate();
    }

    //用户自定义session
    public void doUpdateSession(int SessionID, String url) {
        this.sessionId = SessionID;
        Ilpr.performanceUrl(SessionID, url);
    }

    //测试
    public void doUpdateTest(int sessionID, String url) {
        Ilpr.performanceUrl(sessionID, url);
    }

    public void searchVideoById(int videoId) {
        Ilpr.performanceUrl(DataID.SESSION_TYPE_GET_MOVIELIST_VID, DataID.getActionUrl(DataID.SESSION_TYPE_GET_MOVIELIST_VID, null, videoId));
    }

    private String getSessionName() {
        for (Map.Entry<Integer, String> entry : videoCategoryNameList.entrySet()) {
            return entry.getValue();
            //break;
        }
        return null;
    }

    public void setUserSessionCallback(SessionCallback userSessionCallback) {
        this.userSessionCallback = userSessionCallback;
    }

    public Map<Integer, String> getVideoCategoryNameList() {
        return videoCategoryNameList;
    }

    public Map<Integer, String> getVideoTypeNameList() {
        return videoTypeNameList;
    }

    void setVideoCategoryNameList(Map<Integer, String> mVideoCategory) {
        if (mVideoCategory != null) {
            for (Map.Entry<Integer, String> entry : mVideoCategory.entrySet())
                if (!this.videoCategoryNameList.containsKey(entry.getKey()))
                    this.videoCategoryNameList.put(entry.getKey(), entry.getValue());
            //this.mVideoCategoryNameList = mVideoCategory;
        }
    }

    void setVideoTypeNameList(Map<Integer, String> videoTypeNameList) {
        if (videoTypeNameList != null)
            this.videoTypeNameList = videoTypeNameList;
    }

    public VideoList getVideoList() {
        return videoList;
    }

    void setVideoList(VideoList videoList) {
        if (videoList != null)
            this.videoList = videoList;
    }

    public OMedia getVideoByIndex(int index) {
        OMedia movie = videoList.findByIndex(index);
        return movie;
    }

    public VideoList getVideos() {
        return videoList;
    }

    public void addVideos(VideoList vList) {
        videoList.add(vList);
    }

    public void printMovies() {
        //MLog.logTAG,"printMovies mVideoList size =" + mVideoList.getVideos().size());
        //for (OMedia oMedia : videoList.getList()) {
        //    MLog.logTAG, "vidieId = " + oMedia.getMovie().getMovieId() + " : videoName = " + oMedia.getMovie().getMovieName());
        //}
    }

    public void printCategory() {
        for (Map.Entry<Integer, String> entry : videoCategoryNameList.entrySet())
            MMLog.log(TAG, "id = " + entry.getKey() + ", name = " + entry.getValue());
    }

    public void printVideoType() {
        for (Map.Entry<Integer, String> entry : videoTypeNameList.entrySet())
            MMLog.log(TAG, "id = " + entry.getKey() + ", name = " + entry.getValue());
    }

    public String getFileName(String filePath) {
        File file = new File(filePath);
        if (file.exists())
            return file.getName();
        else
            return null;
    }

    public void initMediasFromLocal(Context context, Integer fType) {
        if (fType == DataID.MEDIA_TYPE_ID_VIDEO) {
            List<LVideo> lVideos = FileUtils.getVideos(context);
            for (LVideo lVideo : lVideos) {
                Movie movie = new Movie(lVideo.getPath());
                String filename = getFileName(movie.getsUrl());
                if (!TextUtils.isEmpty(filename))
                    movie.setName(filename);
                OMedia oMedia = new OMedia(movie);
                videoList.add(oMedia);
            }
        } else if (fType == DataID.MEDIA_TYPE_ID_AUDIO) {
            List<LMusic> lMusics = FileUtils.getMusics(context);
            for (LMusic lmusic : lMusics) {
                Movie movie = new Movie(lmusic.getPath());
                String filename = getFileName(movie.getsUrl());
                if (!TextUtils.isEmpty(filename))
                    movie.setName(filename);
                OMedia oMedia = new OMedia(movie);
                videoList.add(oMedia);
            }
        }

        if (fType == DataID.MEDIA_TYPE_ID_PIC) {
            List<String> imgList = FileUtils.getLocalImageList();
            for (String img : imgList) {
                Movie movie = new Movie(img);
                String filename = getFileName(movie.getsUrl());
                if (!TextUtils.isEmpty(filename))
                    movie.setName(filename);
                OMedia oMedia = new OMedia(movie);
                videoList.add(oMedia);
            }
        }
    }

    public void initMediasFromPath(Context context, String FilePath, Integer fType) {
        List<String> FileList = MediaFile.getMediaFiles(FilePath, fType);
        for (int i = 0; i < FileList.size(); i++) {
            Movie movie = new Movie(FileList.get(i));
            String filename = getFileName(movie.getsUrl());
            if (!TextUtils.isEmpty(filename))
                movie.setName(filename);
            OMedia oMedia = new OMedia(movie);
            videoList.add(oMedia);
        }
        FileList.clear();
    }

    public void initFilesFromPath(Context context, String filePath, List<String> FileList) {
        boolean f = false;
        if (FileList == null)
            FileList = new ArrayList<String>();
        else
            f = true;
        FileUtils.getFiles(filePath, FileList);
        if (!f) {
            for (int i = 0; i < FileList.size(); i++) {
                Movie movie = new Movie(FileList.get(i));
                String filename = getFileName(movie.getsUrl());
                if (!TextUtils.isEmpty(filename))
                    movie.setName(filename);
                OMedia oMedia = new OMedia(movie);
                videoList.add(oMedia);
            }
            FileList.clear();
        }
    }

    private void generateAndAppendVideoFromIlpr() {
        MovieListBean movieListBean = Ilpr.getMovieListBean();
        if (movieListBean != null) {
            for (Movie movie : movieListBean.getList()) {
                OMedia oMedia = new OMedia(movie);
                videoList.add(oMedia);
            }
            this.mTotalPages = movieListBean.getPages();
        } else {
            MMLog.log(TAG, "generateAndAppendVideoFromIlpr movieListBean == null");
        }
    }

    @Override
    public synchronized void OnSessionComplete(int sessionId, String result) {
        switch (sessionId) {
            case DataID.SESSION_TYPE_GET_MOVIELIST_ALLTV:
            case DataID.SESSION_TYPE_GET_MOVIELIST_ALLMOVIE:
            case DataID.SESSION_TYPE_GET_MOVIELIST_ALLMOVIE2:
            case DataID.SESSION_TYPE_GET_MOVIELIST_VID:
            case DataID.SESSION_TYPE_GET_MOVIELIST_VNAME:
            case DataID.SESSION_TYPE_GET_MOVIELIST_AREA:
            case DataID.SESSION_TYPE_GET_MOVIELIST_YEAR:
            case DataID.SESSION_TYPE_GET_MOVIELIST_ACTOR:
            case DataID.SESSION_TYPE_GET_MOVIELIST_VIP:
                this.generateAndAppendVideoFromIlpr();
                break;
            case DataID.SESSION_TYPE_GET_MOVIE_CATEGORY:
                this.setVideoCategoryNameList(Ilpr.getVideoCategory());
                break;
            case DataID.SESSION_TYPE_GET_MOVIE_TYPE:
                this.setVideoTypeNameList(Ilpr.getVideoType());
                break;
            default: //用户自定义SessionID,由用户自己解析，//默认尝试转化成视频列表
                //this.generateAndAppendVideoFromIlpr();
                break;
        }

        if (userSessionCallback != null)
            userSessionCallback.OnSessionComplete(sessionId, result);
    }

}
