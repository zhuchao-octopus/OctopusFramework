package com.zhuchao.android.playsession;

import android.content.Context;
import android.text.TextUtils;
import android.util.Log;

import com.zhuchao.android.libfileutils.FilesManager;
import com.zhuchao.android.libfileutils.MediaFile;
import com.zhuchao.android.libfileutils.bean.LMusic;
import com.zhuchao.android.libfileutils.bean.LVideo;
import com.zhuchao.android.playsession.PaserBean.MovieListBean;
import com.zhuchao.android.video.Movie;
import com.zhuchao.android.video.OMedia;
import com.zhuchao.android.video.VideoList;

import java.io.File;
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
public class OPlayerSession implements SessionCompleteCallback {
    public final String TAG = "OPlayerSession ---> ";
    protected int mSessionId = Data.SESSION_TYPE_MANAGER;//会话ID
    private SessionCompleteCallback userSessionCallback = null;//会话回调
    private ImplementProxy Ilpr = null;//new ImplementProxy();执行代理
    private VideoList mVideoList = new VideoList();//会话内容
    private Map<Integer, String> mVideoCategoryNameList = new TreeMap<Integer, String>(new Comparator<Integer>() {
        @Override
        public int compare(Integer o1, Integer o2) {
            return o2.compareTo(o1);
        }
    });

    private Map<Integer, String> mVideoTypeNameList = new TreeMap<Integer, String>();
    private int mPageIndexOrVid = 1;
    private int mTotalPages = 1;

    public OPlayerSession(SessionCompleteCallback callback) {
        userSessionCallback = callback;
        Ilpr = new ImplementProxy(this);
    }

    public OPlayerSession(int sessionId, SessionCompleteCallback callback) {
        userSessionCallback = callback;
        this.mSessionId = sessionId;
        Ilpr = new ImplementProxy(this);
    }

    protected void doStart()//处理内置session,Id存储在内部类 网络请求，通过代理实现
    {
        if (mSessionId <= Data.SESSION_TYPE_LOCALMEDIA) return;
        switch (mSessionId) {
            case Data.SESSION_TYPE_GET_MOVIELIST_ALLTV:
            case Data.SESSION_TYPE_GET_MOVIELIST_ALLMOVIE:
            case Data.SESSION_TYPE_GET_MOVIELIST_ALLMOVIE2:
                Ilpr.performanceUrl(mSessionId, Data.getActionUrl(mSessionId, this.getSessionName(), mPageIndexOrVid));
                break;
            case Data.SESSION_TYPE_GET_MOVIE_CATEGORY:
            case Data.SESSION_TYPE_GET_MOVIE_TYPE:
                Ilpr.performanceUrl(mSessionId, Data.getActionUrl(mSessionId, this.getSessionName(), 1));
                break;
            default:
                Ilpr.performanceUrl(mSessionId, Data.getActionUrl(mSessionId, this.getSessionName(), 1));
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
        this.mSessionId = SessionID;
        doUpdate();
    }

    //用户自定义session
    public void doUpdateSession(int SessionID, String url) {
        this.mSessionId = SessionID;
        Ilpr.performanceUrl(SessionID, url);
    }

    //测试
    public void doUpdateTest(int sessionID, String url) {
        Ilpr.performanceUrl(sessionID, url);
    }

    public void searchVideoById(int videoId) {
        Ilpr.performanceUrl(Data.SESSION_TYPE_GET_MOVIELIST_VID, Data.getActionUrl(Data.SESSION_TYPE_GET_MOVIELIST_VID, null, videoId));
    }

    private String getSessionName() {
        for (Map.Entry<Integer, String> entry : mVideoCategoryNameList.entrySet()) {
            return entry.getValue();
            //break;
        }
        return null;
    }

    public void setUserSessionCallback(SessionCompleteCallback userSessionCallback) {
        this.userSessionCallback = userSessionCallback;
    }

    public Map<Integer, String> getmVideoCategoryNameList() {
        return mVideoCategoryNameList;
    }

    public Map<Integer, String> getmVideoTypeNameList() {
        return mVideoTypeNameList;
    }

    void setmVideoCategoryNameList(Map<Integer, String> mVideoCategory) {
        if (mVideoCategory != null) {
            for (Map.Entry<Integer, String> entry : mVideoCategory.entrySet())
                if (!this.mVideoCategoryNameList.containsKey(entry.getKey()))
                    this.mVideoCategoryNameList.put(entry.getKey(), entry.getValue());
            //this.mVideoCategoryNameList = mVideoCategory;
        }
    }

    void setmVideoTypeNameList(Map<Integer, String> mVideoTypeNameList) {
        if (mVideoTypeNameList != null)
            this.mVideoTypeNameList = mVideoTypeNameList;
    }

    public VideoList getmVideoList() {
        return mVideoList;
    }

    void setmVideoList(VideoList mVideoList) {
        if (mVideoList != null)
            this.mVideoList = mVideoList;
    }

    //movie API
    public OMedia getVideoById(int videoId) {
        OMedia movie = mVideoList.findMoviebyId(videoId);
        return movie;
    }

    public OMedia getVideoByIndex(int index) {
        OMedia movie = mVideoList.findMoviebyIndex(index);
        return movie;
    }

    public List<OMedia> getMovieListByMovieName(String title) {
        return mVideoList.findMoviebyMovieName(title);
    }

    public List<OMedia> getMovieListBySourceId(int sourceId) {
        return mVideoList.findMoviebySourceId(sourceId);
    }

    public List<OMedia> getMovieListByCategory(String categoryName) {
        return mVideoList.findMoviebyCategory(categoryName);
    }

    public List<OMedia> getMovieListByType(String typeName) {
        return mVideoList.findMoviebyTypeName(typeName);
    }

    public List<OMedia> getMovieListByArea(String area) {
        return mVideoList.findMoviebyArea(area);
    }

    public List<OMedia> getMovieListByYear(String year) {
        return mVideoList.findMoviebyYear(year);
    }

    public List<OMedia> getMovieListByActor(String actor) {
        return mVideoList.findMoviebyActor(actor);
    }

    public List<OMedia> getMovieListByVip(int vipId) {
        return mVideoList.findMoviebyVip(vipId);
    }

    public List<OMedia> getVideos() {
        return mVideoList.getVideos();
    }

    public void addVideos(List<OMedia> oMedias) {
        for (OMedia oMedia : oMedias) {
            mVideoList.addVideo(oMedia);
        }
    }

    public void printMovies() {
        //Log.d(TAG,"printMovies mVideoList size =" + mVideoList.getVideos().size());
        for (OMedia oMedia : mVideoList.getVideos()) {
            Log.d(TAG, "vidieId = " + oMedia.getMovie().getMovieId() + " : videoName = " + oMedia.getMovie().getMovieName());
        }
    }

    public void printCategory() {
        for (Map.Entry<Integer, String> entry : mVideoCategoryNameList.entrySet())
            Log.d(TAG, "id = " + entry.getKey() + ", name = " + entry.getValue());
    }

    public void printVideoType() {
        for (Map.Entry<Integer, String> entry : mVideoTypeNameList.entrySet())
            Log.d(TAG, "id = " + entry.getKey() + ", name = " + entry.getValue());
    }

    public String getFileName(String filePath) {
        File file = new File(filePath);
        if(file.exists())
            return file.getName();
        else
            return null;

        //int start = filePath.lastIndexOf("/");
        //int end = filePath.lastIndexOf(".");
        //if (start != -1 && end != -1) {
        //    return filePath.substring(start + 1, end);
        //} else {
        //    return null;
        //}
    }

    public void initMediasFromLocal(Context context, Integer fType) {
        if (fType == Data.MEDIA_SOURCE_ID_VIDEO) {
            List<LVideo> lVideos = FilesManager.getVideos(context);
            for (LVideo lVideo : lVideos) {
                Movie movie = new Movie(lVideo.getPath());
                String finame = getFileName(movie.getSourceUrl());
                if (!TextUtils.isEmpty(finame))
                    movie.setMovieName(finame);
                OMedia oMedia = new OMedia(movie);
                mVideoList.addVideo(oMedia);
            }
        } else if (fType == Data.MEDIA_SOURCE_ID_AUDIO) {
            List<LMusic> lMusics = FilesManager.getMusics(context);
            for (LMusic lmusic : lMusics) {
                Movie movie = new Movie(lmusic.getPath());
                String finame = getFileName(movie.getSourceUrl());
                if (!TextUtils.isEmpty(finame))
                    movie.setMovieName(finame);
                OMedia oMedia = new OMedia(movie);
                mVideoList.addVideo(oMedia);
            }
        }

        if (fType == Data.MEDIA_SOURCE_ID_PIC) {
            List<String> imgList = FilesManager.getLocalImageList();
            for (String img : imgList) {
                Movie movie = new Movie(img);
                String finame = getFileName(movie.getSourceUrl());
                if (!TextUtils.isEmpty(finame))
                    movie.setMovieName(finame);
                OMedia oMedia = new OMedia(movie);
                mVideoList.addVideo(oMedia);
            }
        }

    }

    public void initMediasFromPath(Context context, String FilePath, Integer fType) {
        List<String> FileList = MediaFile.getMediaFiles(context, FilePath, fType);

        for (int i = 0; i < FileList.size(); i++) {
            Movie movie = new Movie(FileList.get(i));
            String finame = getFileName(movie.getSourceUrl());
            if (!TextUtils.isEmpty(finame))
                movie.setMovieName(finame);
            OMedia oMedia = new OMedia(movie);
            mVideoList.addVideo(oMedia);
        }
    }

    private void gernerateAndAppendVideoFromIlpr() {
        MovieListBean movieListBean = Ilpr.getMovieListBean();
        if (movieListBean != null) {
            for (Movie movie : movieListBean.getList()) {
                OMedia oMedia = new OMedia(movie);
                mVideoList.addVideo(oMedia);
            }
            this.mTotalPages = movieListBean.getPages();
        } else {
            Log.d(TAG, "gernerateAndAppendVideoFromIlpr movieListBean == null");
        }
    }

    @Override
    public synchronized void OnSessionComplete(int sessionId, String result) {
        switch (sessionId) {
            case Data.SESSION_TYPE_GET_MOVIELIST_ALLTV:
            case Data.SESSION_TYPE_GET_MOVIELIST_ALLMOVIE:
            case Data.SESSION_TYPE_GET_MOVIELIST_ALLMOVIE2:
            case Data.SESSION_TYPE_GET_MOVIELIST_VID:
            case Data.SESSION_TYPE_GET_MOVIELIST_VNAME:
            case Data.SESSION_TYPE_GET_MOVIELIST_AREA:
            case Data.SESSION_TYPE_GET_MOVIELIST_YEAR:
            case Data.SESSION_TYPE_GET_MOVIELIST_ACTOR:
            case Data.SESSION_TYPE_GET_MOVIELIST_VIP:
                this.gernerateAndAppendVideoFromIlpr();
                break;
            case Data.SESSION_TYPE_GET_MOVIE_CATEGORY:
                this.setmVideoCategoryNameList(Ilpr.getVideoCategory());
                break;
            case Data.SESSION_TYPE_GET_MOVIE_TYPE:
                this.setmVideoTypeNameList(Ilpr.getVideoType());
                break;
            default: //用户自定义SessionID,由用户自己解析，//默认尝试转化成视频列表
                this.gernerateAndAppendVideoFromIlpr();
                break;
        }

        if (userSessionCallback != null)
            userSessionCallback.OnSessionComplete(sessionId, result);
    }

}
