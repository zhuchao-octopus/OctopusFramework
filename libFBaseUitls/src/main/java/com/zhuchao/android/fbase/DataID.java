package com.zhuchao.android.fbase;


import java.util.Iterator;
import java.util.Map;

public class DataID {
    public static final int DB_VERSION = 1;
    //public static final String DB_DIRECTORY = CommonValues.application.getFilesDir().getPath();
    public static final int SESSION_TYPE_GET_MOVIELIST_DEFAULT_PAGESIZE = 100;
    public static final String SESSION_SESSION_ROOT_JHZ = "http://test.jhzdesign.cn:8005/";
    public static final String SESSION_SOURCE_TEST_URL = "http://47.106.172.94:8090/zhuchao/devices/test";
    public static final String SESSION_UPDATE_TEST_INIT = SESSION_SOURCE_TEST_URL;
    public static final String SESSION_UPDATE_JHZ_TEST_UPDATE_NAME = "SESSION_UPDATE_JHZ_ROOT_TEST";
    public static final String TASK_STATUS_INTERNAL_ = "_status_";
    //////////////////////////////////////////////////////////////////////
    public static final int SESSION_SOURCE_NONE = -1; //
    public static final int SESSION_SOURCE_ALL = 10; //
    public static final int SESSION_SOURCE_PATH = 11; //
    public static final int SESSION_SOURCE_LOCAL_INTERNAL = 12; //本地媒体
    public static final int SESSION_SOURCE_MOBILE_USB = 13;
    public static final int SESSION_SOURCE_EXTERNAL = 14;
    public static final int SESSION_SOURCE_PLAYLIST = 15;
    public static final int SESSION_SOURCE_FAVORITELIST = 16;

    //获取分类的ID
    public static final int SESSION_TYPE_GET_MOVIE_CATEGORY = 20;//版面分类
    public static final int SESSION_TYPE_GET_MOVIE_TYPE = SESSION_TYPE_GET_MOVIE_CATEGORY + 1;//视屏分类
    //获取视频列表的ID
    public static final int SESSION_TYPE_GET_MOVIELIST_ALL = SESSION_TYPE_GET_MOVIE_TYPE + 1;//所有视频列表
    public static final int SESSION_TYPE_GET_MOVIELIST_VID = SESSION_TYPE_GET_MOVIELIST_ALL + 1;
    public static final int SESSION_TYPE_GET_MOVIELIST_VNAME = SESSION_TYPE_GET_MOVIELIST_VID + 1;
    public static final int SESSION_TYPE_GET_MOVIELIST_AREA = SESSION_TYPE_GET_MOVIELIST_VNAME + 1;
    public static final int SESSION_TYPE_GET_MOVIELIST_YEAR = SESSION_TYPE_GET_MOVIELIST_AREA + 1;
    public static final int SESSION_TYPE_GET_MOVIELIST_ACTOR = SESSION_TYPE_GET_MOVIELIST_YEAR + 1;
    public static final int SESSION_TYPE_GET_MOVIELIST_VIP = SESSION_TYPE_GET_MOVIELIST_ACTOR + 1;
    public static final int SESSION_TYPE_GET_MOVIELIST_SOURCE = SESSION_TYPE_GET_MOVIELIST_VIP + 1;
    //板块分类视频列表的ID
    public static final int SESSION_TYPE_GET_MOVIELIST_ALLTV = SESSION_TYPE_GET_MOVIELIST_SOURCE + 1;//30;//获取直播/电视列表
    public static final int SESSION_TYPE_GET_MOVIELIST_ALLMOVIE = SESSION_TYPE_GET_MOVIELIST_ALLTV + 1;//电影
    public static final int SESSION_TYPE_GET_MOVIELIST_ALLMOVIE2 = SESSION_TYPE_GET_MOVIELIST_ALLMOVIE + 1;//电视剧

    public static final int MEDIA_TYPE_ID_AllFILE = 100;
    public static final int MEDIA_TYPE_ID_AllMEDIA = MEDIA_TYPE_ID_AllFILE + 1;
    public static final int MEDIA_TYPE_ID_PIC = MEDIA_TYPE_ID_AllMEDIA + 1;
    public static final int MEDIA_TYPE_ID_AUDIO = MEDIA_TYPE_ID_PIC + 1;
    public static final int MEDIA_TYPE_ID_VIDEO = MEDIA_TYPE_ID_AUDIO + 1;
    public static final int MEDIA_TYPE_ID_AUDIO_VIDEO = MEDIA_TYPE_ID_VIDEO + 1;
    public static final int MEDIA_TYPE_ID_OTHERS = MEDIA_TYPE_ID_AUDIO_VIDEO + 1;


    public static final int PLAY_MANAGER_PLAY_ORDER0 = 200;
    public static final int PLAY_MANAGER_PLAY_ORDER1 = PLAY_MANAGER_PLAY_ORDER0 + 1;
    public static final int PLAY_MANAGER_PLAY_ORDER2 = PLAY_MANAGER_PLAY_ORDER1 + 1;
    public static final int PLAY_MANAGER_PLAY_ORDER3 = PLAY_MANAGER_PLAY_ORDER2 + 1;
    public static final int PLAY_MANAGER_PLAY_ORDER4 = PLAY_MANAGER_PLAY_ORDER3 + 1;
    public static final int PLAY_MANAGER_PLAY_ORDER5 = PLAY_MANAGER_PLAY_ORDER4 + 1;
    public static final int PLAY_MANAGER_PLAY_ORDER6 = PLAY_MANAGER_PLAY_ORDER5 + 1;

    public static final int TASK_STATUS_NONE = 300;
    public static final int TASK_STATUS_START = TASK_STATUS_NONE + 1;
    public static final int TASK_STATUS_PROGRESSING = TASK_STATUS_START + 1;

    public static final int TASK_STATUS_FINISHED_WAITING = TASK_STATUS_PROGRESSING + 1; //内部等待，303
    public static final int TASK_STATUS_SUCCESS = TASK_STATUS_FINISHED_WAITING + 1;//主题任务完成，304
    //（内部使用）任务结束、终止、停止不再需要运行，305
    public static final int TASK_STATUS_FINISHED_STOP = TASK_STATUS_SUCCESS + 1;//305
    //任务池中的所有任务完成 306
    public static final int TASK_STATUS_FINISHED_ALL = TASK_STATUS_FINISHED_STOP + 1;//306
    //任务复位后可以再次启动
    public static final int TASK_STATUS_CAN_RESTART = TASK_STATUS_FINISHED_ALL + 1; //307允许重新启动
    //任务出错 与 TASK_STATUS_SUCCESS 相反
    public static final int TASK_STATUS_ERROR = TASK_STATUS_CAN_RESTART + 1;//308


    public static final int DEVICE_TYPE = 400;
    public static final int DEVICE_TYPE_FILE = DEVICE_TYPE + 1;
    public static final int DEVICE_TYPE_UART = DEVICE_TYPE_FILE + 1;


    public static final int DEVICE_EVENT = 500;
    public static final int DEVICE_EVENT_OPEN = DEVICE_EVENT + 1;
    public static final int DEVICE_EVENT_READ = DEVICE_EVENT_OPEN + 1;

    public static final int DEVICE_EVENT_WRITE = DEVICE_EVENT_READ + 1;
    public static final int DEVICE_EVENT_UART_WRITE = DEVICE_EVENT_WRITE + 1;

    public static final int DEVICE_EVENT_CLOSE = DEVICE_EVENT_UART_WRITE + 1;
    public static final int DEVICE_EVENT_ERROR = DEVICE_EVENT_CLOSE + 1;



    public static String getRequestUrl(String fromUrl, ObjectList requestParams) {
        StringBuilder builder = new StringBuilder();
        builder.append(fromUrl);
        try {
            Iterator<Map.Entry<String, Object>> it = requestParams.getAll().entrySet().iterator();
            while (it.hasNext()) {
                Map.Entry<String, Object> entry = it.next();
                builder.append(entry.getKey() + "=" + entry.getValue().toString() + "&");
            }
        } catch (Exception e) {
            //e.printStackTrace();
        }
        if (builder == null)
            return "Sorry i am can not ";
        return builder.toString();
    }

    public static String getActionUrl(int sessionId, String categoryName, int pageIndexOrVid) {
        StringBuilder builder = new StringBuilder();
        builder.append(SESSION_SESSION_ROOT_JHZ);
        switch (sessionId) {
            case SESSION_TYPE_GET_MOVIELIST_VNAME:
                builder.append("/getVideoList?movieName=" + categoryName + "&limit=" + SESSION_TYPE_GET_MOVIELIST_DEFAULT_PAGESIZE);
                builder.append("&page=" + pageIndexOrVid);
                return builder.toString();
            case SESSION_TYPE_GET_MOVIELIST_AREA:
                builder.append("/getVideoList?movieType=" + categoryName + "&limit=" + SESSION_TYPE_GET_MOVIELIST_DEFAULT_PAGESIZE);
                builder.append("&page=" + pageIndexOrVid);
                return builder.toString();
            case SESSION_TYPE_GET_MOVIELIST_YEAR:
                builder.append("/getVideoList?year=" + categoryName + "&limit=" + SESSION_TYPE_GET_MOVIELIST_DEFAULT_PAGESIZE);
                builder.append("&page=" + pageIndexOrVid);
                return builder.toString();
            case SESSION_TYPE_GET_MOVIELIST_ACTOR:
                builder.append("/getVideoList?actor=" + categoryName + "&limit=" + SESSION_TYPE_GET_MOVIELIST_DEFAULT_PAGESIZE);
                builder.append("&page=" + pageIndexOrVid);
                return builder.toString();
            case SESSION_TYPE_GET_MOVIELIST_SOURCE:
            case SESSION_TYPE_GET_MOVIELIST_ALL: {
                builder.append("/getVideoList?limit=" + SESSION_TYPE_GET_MOVIELIST_DEFAULT_PAGESIZE);
                builder.append("&page=" + pageIndexOrVid);
                return builder.toString();
            }
            case SESSION_TYPE_GET_MOVIELIST_VID: {
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
                builder.append("/getVideoList?category=" + categoryName + "&limit=" + SESSION_TYPE_GET_MOVIELIST_DEFAULT_PAGESIZE);
                builder.append("&page=" + pageIndexOrVid);
                return builder.toString();
            }
            default: {
                builder.append("/getVideoList?category=" + categoryName + "&limit=" + SESSION_TYPE_GET_MOVIELIST_DEFAULT_PAGESIZE);
                builder.append("&page=" + pageIndexOrVid);
                //return builder.toString();
                break;
            }
        }
        return builder.toString();
    }
}
