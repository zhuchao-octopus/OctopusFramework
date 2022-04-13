package com.zhuchao.android.playsession;


public class SessionID {
    public static final int DB_VERSION = 1;
    //public static final String DB_DIRECTORY = CommonValues.application.getFilesDir().getPath();
    public static final int SESSION_TYPE_GET_MOVIELIST_DEFAULT_PAGESIZE = 100;
    public static String OPLAYER_SESSION_ROOT_URL = "http://test.jhzdesign.cn:8005/";
    public static final int DEFAULT_PREVIEW_WIDTH = 800;
    public static final int DEFAULT_PREVIEW_HEIGHT = 654;

    //////////////////////////////////////////////////////////////////////
    public static final int SESSION_SOURCE_NONE = -1; //
    public static final int SESSION_SOURCE_ALL = 10; //
    public static final int SESSION_SOURCE_PATH = 11; //
    public static final int SESSION_SOURCE_LOCAL_INTERNAL = 12; //本地媒体
    public static final int SESSION_SOURCE_MOBILE_USB = 13;
    public static final int SESSION_SOURCE_EXTERNAL = 14;
    public static final int SESSION_SOURCE_PLAYLIST = 15;

    //获取分类的ID
    public static final int SESSION_TYPE_GET_MOVIE_CATEGORY = 20;//版面分类
    public static final int SESSION_TYPE_GET_MOVIE_TYPE = SESSION_TYPE_GET_MOVIE_CATEGORY+1;//视屏分类
    //获取视频列表的ID
    public static final int SESSION_TYPE_GET_MOVIELIST_ALL = SESSION_TYPE_GET_MOVIE_TYPE+1;//所有视频列表
    public static final int SESSION_TYPE_GET_MOVIELIST_VID = SESSION_TYPE_GET_MOVIELIST_ALL+1;
    public static final int SESSION_TYPE_GET_MOVIELIST_VNAME = SESSION_TYPE_GET_MOVIELIST_VID+1;
    public static final int SESSION_TYPE_GET_MOVIELIST_AREA = SESSION_TYPE_GET_MOVIELIST_VNAME + 1;
    public static final int SESSION_TYPE_GET_MOVIELIST_YEAR = SESSION_TYPE_GET_MOVIELIST_AREA + 1;
    public static final int SESSION_TYPE_GET_MOVIELIST_ACTOR = SESSION_TYPE_GET_MOVIELIST_YEAR + 1;
    public static final int SESSION_TYPE_GET_MOVIELIST_VIP = SESSION_TYPE_GET_MOVIELIST_ACTOR + 1;
    public static final int SESSION_TYPE_GET_MOVIELIST_SOURCE = SESSION_TYPE_GET_MOVIELIST_VIP + 1;
    //板块分类视频列表的ID
    public static final int SESSION_TYPE_GET_MOVIELIST_ALLTV = SESSION_TYPE_GET_MOVIELIST_SOURCE+1;//30;//获取直播/电视列表
    public static final int SESSION_TYPE_GET_MOVIELIST_ALLMOVIE = SESSION_TYPE_GET_MOVIELIST_ALLTV+1;//电影
    public static final int SESSION_TYPE_GET_MOVIELIST_ALLMOVIE2 = SESSION_TYPE_GET_MOVIELIST_ALLMOVIE+1;//电视剧

    public static final int MEDIA_TYPE_ID_AllFILE = 99;
    public static final int MEDIA_TYPE_ID_AllMEDIA = 100;
    public static final int MEDIA_TYPE_ID_PIC = 101;
    public static final int MEDIA_TYPE_ID_AUDIO = 102;
    public static final int MEDIA_TYPE_ID_VIDEO = 103;
    public static final int MEDIA_TYPE_ID_AUDIO_VIDEO = 104;
    public static final int MEDIA_TYPE_ID_OTHERS = MEDIA_TYPE_ID_AUDIO_VIDEO+1;


    public static final int PLAY_MANAGER_PLAY_ORDER0 = 200;
    public static final int PLAY_MANAGER_PLAY_ORDER1 = PLAY_MANAGER_PLAY_ORDER0+1;
    public static final int PLAY_MANAGER_PLAY_ORDER2 = PLAY_MANAGER_PLAY_ORDER1+1;
    public static final int PLAY_MANAGER_PLAY_ORDER3 = PLAY_MANAGER_PLAY_ORDER2+1;
    public static final int PLAY_MANAGER_PLAY_ORDER4 = PLAY_MANAGER_PLAY_ORDER3+1;
    public static final int PLAY_MANAGER_PLAY_ORDER5 = PLAY_MANAGER_PLAY_ORDER4+1;
    public static final int PLAY_MANAGER_PLAY_ORDER6 = PLAY_MANAGER_PLAY_ORDER5+1;

    public static final int SESSION_TYPE_SCHEDULEPLAYBACK = 2019;
    //登录的ID

    public static void setOplayerSessionRootUrl(String oplayerSessionRootUrl) {
        if (oplayerSessionRootUrl != null)
           OPLAYER_SESSION_ROOT_URL = oplayerSessionRootUrl;
    }

    public static String getActionUrl(int sessionId,String categoryName, int pageIndexOrVid) {
        StringBuilder builder = new StringBuilder();
        builder.append(OPLAYER_SESSION_ROOT_URL);
        switch (sessionId) {
            case SESSION_TYPE_GET_MOVIELIST_VNAME:
                builder.append("/getVideoList?movieName="+categoryName+"&limit=" + SESSION_TYPE_GET_MOVIELIST_DEFAULT_PAGESIZE);
                builder.append("&page=" + pageIndexOrVid);
                return builder.toString();
            case SESSION_TYPE_GET_MOVIELIST_AREA:
                builder.append("/getVideoList?movieType="+categoryName+"&limit=" + SESSION_TYPE_GET_MOVIELIST_DEFAULT_PAGESIZE);
                builder.append("&page=" + pageIndexOrVid);
                return builder.toString();
            case SESSION_TYPE_GET_MOVIELIST_YEAR:
                builder.append("/getVideoList?year="+categoryName+"&limit=" + SESSION_TYPE_GET_MOVIELIST_DEFAULT_PAGESIZE);
                builder.append("&page=" + pageIndexOrVid);
                return builder.toString();
            case SESSION_TYPE_GET_MOVIELIST_ACTOR:
                builder.append("/getVideoList?actor="+categoryName+"&limit=" + SESSION_TYPE_GET_MOVIELIST_DEFAULT_PAGESIZE);
                builder.append("&page=" + pageIndexOrVid);
                return builder.toString();
            case SESSION_TYPE_GET_MOVIELIST_VIP:
                builder.append("/getVideoList?movieName="+categoryName+"&limit=" + SESSION_TYPE_GET_MOVIELIST_DEFAULT_PAGESIZE);
                builder.append("&page=" + pageIndexOrVid);
                return builder.toString();
            case SESSION_TYPE_GET_MOVIELIST_SOURCE:
            case SESSION_TYPE_GET_MOVIELIST_ALL: {
                builder.append("/getVideoList?limit=" + SESSION_TYPE_GET_MOVIELIST_DEFAULT_PAGESIZE);
                builder.append("&page=" + pageIndexOrVid);
                return builder.toString();
            }
            case SESSION_TYPE_GET_MOVIELIST_VID:{
                builder.append("/getVideoList?movieId=" + pageIndexOrVid);
                return builder.toString();
            }
            case SESSION_TYPE_GET_MOVIE_CATEGORY: {
                builder.append("/getCategoryList?");
                //builder.append("&page=" + pageIndexOrVid);
                return builder.toString();
            }

            case SESSION_TYPE_GET_MOVIE_TYPE: {
                builder.append("/getTypeList?");
                //builder.append("&page=" + pageIndexOrVid);
                return builder.toString();
            }
            case SESSION_TYPE_GET_MOVIELIST_ALLTV: {
                builder.append("/getVideoList?category="+categoryName+"&limit=" + SESSION_TYPE_GET_MOVIELIST_DEFAULT_PAGESIZE);
                builder.append("&page=" + pageIndexOrVid);
                return builder.toString();
            }
            case SESSION_TYPE_GET_MOVIELIST_ALLMOVIE: {
                builder.append("/getVideoList?category="+categoryName+"&limit=" + SESSION_TYPE_GET_MOVIELIST_DEFAULT_PAGESIZE);
                builder.append("&page=" + pageIndexOrVid);
                return builder.toString();
            }
            case SESSION_TYPE_GET_MOVIELIST_ALLMOVIE2: {
                builder.append("/getVideoList?category="+categoryName+"&limit=" + SESSION_TYPE_GET_MOVIELIST_DEFAULT_PAGESIZE);
                builder.append("&page=" + pageIndexOrVid);
                return builder.toString();
            }
            default:{
                builder.append("/getVideoList?category="+categoryName+"&limit=" + SESSION_TYPE_GET_MOVIELIST_DEFAULT_PAGESIZE);
                builder.append("&page=" + pageIndexOrVid);
                //return builder.toString();
                break;
            }
        }
        return builder.toString();
    }
}
