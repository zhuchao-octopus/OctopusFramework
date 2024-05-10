package com.zhuchao.android.session;

import static com.zhuchao.android.fbase.FileUtils.NotEmptyString;

import android.content.Context;

import com.zhuchao.android.fbase.DataID;
import com.zhuchao.android.fbase.FileUtils;
import com.zhuchao.android.fbase.MMLog;
import com.zhuchao.android.fbase.MediaFile;
import com.zhuchao.android.fbase.bean.LMusic;
import com.zhuchao.android.fbase.bean.LVideo;
import com.zhuchao.android.fbase.eventinterface.SessionCallback;
import com.zhuchao.android.session.PaserBean.MovieListBean;
import com.zhuchao.android.video.Movie;
import com.zhuchao.android.video.OMedia;
import com.zhuchao.android.video.VideoList;

import java.io.File;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
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
    private final String TAG = "OPlayerSession ---> ";
    private int sessionId = DataID.SESSION_SOURCE_NONE;//会话ID
    private SessionCallback userSessionCallback = null;//会话回调
    private ImplementProxy implementProxy = null;//new ImplementProxy();执行代理
    private VideoList mVideoList = new VideoList(null);//会话内容
    private Map<Integer, String> videoTypeNameList = new TreeMap<Integer, String>();
    private int mPageIndexOrVid = 1;
    private int mTotalPages = 1;


    private final Map<Integer, String> videoCategoryNameList = new TreeMap<Integer, String>(new Comparator<Integer>() {
        @Override
        public int compare(Integer o1, Integer o2) {
            return o2.compareTo(o1);
        }
    });

    public LiveVideoSession(SessionCallback callback) {
        userSessionCallback = callback;
        implementProxy = new ImplementProxy(this);
    }

    public LiveVideoSession(int sessionId, SessionCallback callback) {
        userSessionCallback = callback;
        this.sessionId = sessionId;
        implementProxy = new ImplementProxy(this);
    }

    protected void doStart()//处理内置session,Id存储在内部类 网络请求，通过代理实现
    {
        if (sessionId <= DataID.SESSION_SOURCE_LOCAL_INTERNAL) return;
        switch (sessionId) {
            case DataID.SESSION_TYPE_GET_MOVIELIST_ALLTV:
            case DataID.SESSION_TYPE_GET_MOVIELIST_ALLMOVIE:
            case DataID.SESSION_TYPE_GET_MOVIELIST_ALLMOVIE2:
                implementProxy.performanceUrl(sessionId, DataID.getActionUrl(sessionId, this.getSessionName(), mPageIndexOrVid));
                break;
            case DataID.SESSION_TYPE_GET_MOVIE_CATEGORY:
            case DataID.SESSION_TYPE_GET_MOVIE_TYPE:
            default:
                implementProxy.performanceUrl(sessionId, DataID.getActionUrl(sessionId, this.getSessionName(), 1));
                break;
        }
    }

    private void doUpdate() {
        doStart();
    }

    private void doUpdate(int pageIndex) {
        if (pageIndex < 1) mPageIndexOrVid = 1;
        if (pageIndex > mTotalPages) mPageIndexOrVid = mTotalPages;
        else mPageIndexOrVid = pageIndex;
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
        implementProxy.performanceUrl(SessionID, url);
    }

    //测试
    public void doUpdateTest(int sessionID, String url) {
        implementProxy.performanceUrl(sessionID, url);
    }

    public void searchVideoById(int videoId) {
        implementProxy.performanceUrl(DataID.SESSION_TYPE_GET_MOVIELIST_VID, DataID.getActionUrl(DataID.SESSION_TYPE_GET_MOVIELIST_VID, null, videoId));
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
                if (!this.videoCategoryNameList.containsKey(entry.getKey())) this.videoCategoryNameList.put(entry.getKey(), entry.getValue());
            //this.mVideoCategoryNameList = mVideoCategory;
        }
    }

    void setVideoTypeNameList(Map<Integer, String> videoTypeNameList) {
        if (videoTypeNameList != null) this.videoTypeNameList = videoTypeNameList;
    }

    public OMedia getVideoByIndex(int index) {
        return mVideoList.findByIndex(index);
    }

    void setVideoList(VideoList mVideoList) {
        if (mVideoList != null) this.mVideoList = mVideoList;
    }

    public void clear() {
        mVideoList.clear();
    }

    public VideoList getVideoList() {
        return mVideoList;
    }

    public VideoList getAllVideos() {
        return mVideoList;
    }

    public VideoList getVideos() {
        VideoList videoList1 = new VideoList();
        for (HashMap.Entry<String, Object> oo : mVideoList.getMap().entrySet()) {
            OMedia oMedia = (OMedia) oo.getValue();
            if (oMedia.isVideo()) videoList1.add(oMedia);
        }
        return videoList1;
    }

    public VideoList getAudios() {
        VideoList audioList1 = new VideoList();
        for (HashMap.Entry<String, Object> oo : mVideoList.getMap().entrySet()) {
            OMedia oMedia = (OMedia) oo.getValue();
            if (oMedia.isAudio()) audioList1.add(oMedia);
        }
        return audioList1;
    }

    public void addVideos(VideoList videoList) {
        mVideoList.add(videoList);
        ///MMLog.d(TAG,"videoList.count="+videoList.getCount());
        ///MMLog.d(TAG,"mVideoList.count="+mVideoList.getCount());
    }

    public void initMediasFromLocal(Context context, Integer fType) {
        if (fType == DataID.MEDIA_TYPE_ID_VIDEO) {
            List<LVideo> lVideos = FileUtils.getLocalSystemVideos(context);
            for (LVideo lVideo : lVideos) {
                Movie movie = new Movie(lVideo.getPath());
                ///String fileName = getFileName(movie.getSrcUrl());
                ///if (NotEmptyString(fileName)) movie.setName(fileName);
                movie.setName(lVideo.getName());
                movie.setMovie_id(lVideo.getId());
                OMedia oMedia = new OMedia(movie);
                mVideoList.add(oMedia);
            }
        } else if (fType == DataID.MEDIA_TYPE_ID_AUDIO) {
            List<LMusic> lMusics = FileUtils.getLocalSystemMusics(context);
            for (LMusic lmusic : lMusics) {
                Movie movie = new Movie(lmusic.getPath());
                ///String fileName = getFileName(movie.getSrcUrl());
                ///if (NotEmptyString(fileName)) movie.setName(fileName);
                movie.setName(lmusic.getName());
                movie.setMovie_id(lmusic.getId());
                movie.setSource_id(lmusic.getAlbumId());
                movie.setAlbum(lmusic.getAlbum());
                movie.setArtist(lmusic.getArtist());
                movie.setSize(lmusic.getSize());
                movie.setDuration(lmusic.getDuration());
                OMedia oMedia = new OMedia(movie);
                mVideoList.add(oMedia);
            }
        }

        /*if (fType == DataID.MEDIA_TYPE_ID_PIC) {
            List<String> imgList = FileUtils.getLocalImageList();
            for (String img : imgList) {
                Movie movie = new Movie(img);
                String fileName = getFileName(movie.getsUrl());
                if(NotEmptyString(fileName))
                    movie.setName(fileName);
                OMedia oMedia = new OMedia(movie);
                videoList.add(oMedia);
            }
        }*/
    }

    public void initMediasFromUSB(Context context, String filePathName, Integer fType, LiveVideoSession videoSession, LiveVideoSession audioSession) {
        ///if (fType == DataID.MEDIA_TYPE_ID_AUDIO_VIDEO)
        {
            List<LVideo> lVideos = FileUtils.getLocalSystemVideos(context);
            ///MMLog.d(TAG,"lVideos.count="+lVideos.size());
            for (LVideo lVideo : lVideos) {
                if (!lVideo.getPath().contains(filePathName)) continue;
                Movie movie = new Movie(lVideo.getPath());
                ///String fileName = getFileName(movie.getSrcUrl());
                ///if (NotEmptyString(fileName)) movie.setName(fileName);
                movie.setName(lVideo.getName());
                movie.setMovie_id(lVideo.getId());
                OMedia oMedia = new OMedia(movie);
                videoSession.getVideoList().add(oMedia);
            }
        }
        ///else if (fType == DataID.MEDIA_TYPE_ID_AUDIO)
        {
            List<LMusic> lMusics = FileUtils.getLocalSystemMusics(context);
            ///MMLog.d(TAG,"lMusics.count="+lMusics.size());
            for (LMusic lmusic : lMusics) {
                if (!lmusic.getPath().contains(filePathName)) continue;

                Movie movie = new Movie(lmusic.getPath());
                ///String fileName = getFileName(movie.getSrcUrl());
                ///if (NotEmptyString(fileName)) movie.setName(fileName);
                movie.setName(lmusic.getName());
                movie.setMovie_id(lmusic.getId());
                movie.setSource_id(lmusic.getAlbumId());
                movie.setAlbum(lmusic.getAlbum());
                movie.setArtist(lmusic.getArtist());
                movie.setSize(lmusic.getSize());
                movie.setDuration(lmusic.getDuration());
                OMedia oMedia = new OMedia(movie);
                audioSession.getVideoList().add(oMedia);
            }
        }
    }

    public void initMediasFromPath(Context context, String FilePath, Integer fType, LiveVideoSession videoSession, LiveVideoSession audioSession) {
        List<String> FileList = MediaFile.getMediaFiles(FilePath, fType);
        List<String> list = null;
        for (int i = 0; i < FileList.size(); i++) {
            Movie movie = new Movie(FileList.get(i));
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.Q) {
               list = MediaFile.readMetadataFromMusic(movie.getSrcUrl());
            }
            String fileName = getFileName(movie.getSrcUrl());
            if (NotEmptyString(fileName)) movie.setName(fileName);
            if(list != null)
            {
                movie.setArtist(list.get(0));
                movie.setActor(list.get(1));
                movie.setAlbum(list.get(2));
            }
            OMedia oMedia = new OMedia(movie);
            if (oMedia.isVideo()) videoSession.getVideoList().add(oMedia);
            else if (oMedia.isAudio()) audioSession.getVideoList().add(oMedia);
            //mVideoList.add(oMedia);
        }
        FileList.clear();
    }

    public void initMediasFromPath(Context context, String FilePath, Integer fType) {
        List<String> FileList = MediaFile.getMediaFiles(FilePath, fType);
        for (int i = 0; i < FileList.size(); i++) {
            Movie movie = new Movie(FileList.get(i));
            String fileName = getFileName(movie.getSrcUrl());
            if (NotEmptyString(fileName)) movie.setName(fileName);
            OMedia oMedia = new OMedia(movie);
            mVideoList.add(oMedia);
        }
        FileList.clear();
    }

    public void initFilesFromPath(Context context, String filePath, List<String> FileList) {
        boolean f = false;
        if (FileList == null) FileList = new ArrayList<String>();
        else f = true;
        FileUtils.getFiles(filePath, FileList);
        if (!f) {
            for (int i = 0; i < FileList.size(); i++) {
                Movie movie = new Movie(FileList.get(i));
                String fileName = getFileName(movie.getSrcUrl());
                if (NotEmptyString(fileName)) movie.setName(fileName);
                OMedia oMedia = new OMedia(movie);
                mVideoList.add(oMedia);
            }
            FileList.clear();
        }
    }

    private void generateAndAppendVideoFromIlpr() {
        MovieListBean movieListBean = implementProxy.getMovieListBean();
        if (movieListBean != null) {
            for (Movie movie : movieListBean.getList()) {
                OMedia oMedia = new OMedia(movie);
                mVideoList.add(oMedia);
            }
            this.mTotalPages = movieListBean.getPages();
        } else {
            MMLog.log(TAG, "generateAndAppendVideoFromIlpr movieListBean == null");
        }
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
        if (file.exists()) return file.getName();
        else return null;
    }

    @Override
    public synchronized void OnSessionComplete(int session_id, String result, int count) {
        switch (session_id) {
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
                this.setVideoCategoryNameList(implementProxy.getVideoCategory());
                break;
            case DataID.SESSION_TYPE_GET_MOVIE_TYPE:
                this.setVideoTypeNameList(implementProxy.getVideoType());
                break;
            default: //用户自定义SessionID,由用户自己解析，//默认尝试转化成视频列表
                //this.generateAndAppendVideoFromIlpr();
                break;
        }
        if (userSessionCallback != null) userSessionCallback.OnSessionComplete(session_id, result, count);
    }

}
