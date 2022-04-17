package com.zhuchao.android.playsession;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import com.google.gson.JsonSyntaxException;
import com.google.gson.reflect.TypeToken;
import com.zhuchao.android.callbackevent.HttpCallBack;
import com.zhuchao.android.libfileutils.MMLog;
import com.zhuchao.android.libfileutils.SessionID;
import com.zhuchao.android.netutil.HttpUtils;
import com.zhuchao.android.playsession.PaserBean.IdNameBean;
import com.zhuchao.android.playsession.PaserBean.MovieListBean;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HashMap;
import java.util.Map;

public class ImplementProxy implements HttpCallBack {
    public final String TAG = "ImplementProxy";
    private SessionCompleteCallback SessionCallback = null;

    private Map<Integer, String> mVideoCategory = null;// = new HashMap<Integer, String>();
    private Map<Integer, String> mVideoType = null;// = new HashMap<Integer, String>();
    private MovieListBean mMovieListBean = null;

    public ImplementProxy(SessionCompleteCallback sessionCompleteCallback) {
        SessionCallback = sessionCompleteCallback;
    }

    public Map<Integer, String> getVideoCategory() {
        return mVideoCategory;
    }

    public Map<Integer, String> getVideoType() {
        return mVideoType;
    }

    public MovieListBean getMovieListBean() {
        return mMovieListBean;
    }

    public Map<Integer, String> JsonToMap(String JsonStr) {
        Gson gson = new GsonBuilder().serializeNulls().disableHtmlEscaping().create();
        try {
            return gson.fromJson(JsonStr, new TypeToken<Map<Integer, String>>() {
            }.getType());
        } catch (JsonSyntaxException e) {
            e.printStackTrace();
        }
        return null;
    }

    public Map<Integer, String> jsonIdNameArrayToMap(String jsonStr) {
        JsonParser parser = new JsonParser();
        JsonArray jsonArray = parser.parse(jsonStr).getAsJsonArray();
        Gson gson = new Gson();

        Map<Integer, String> map = new HashMap<Integer, String>();
        for (JsonElement user : jsonArray) {
            IdNameBean idNameBean = gson.fromJson(user, IdNameBean.class);
            if (idNameBean.getStatus() == 1)
                map.put(idNameBean.getId(), idNameBean.getName());
        }
        return map;
    }

    public String MapToJson(Map<Integer, String> map) {
        Gson gson = new GsonBuilder().serializeNulls().disableHtmlEscaping().create();
        try {
            return gson.toJson(map);
        } catch (JsonSyntaxException e) {
            e.printStackTrace();
        }
        return null;
    }

    public String stringToMD5(String string) {
        byte[] hash;
        try {
            hash = MessageDigest.getInstance("MD5").digest(string.getBytes(StandardCharsets.UTF_8));
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
            return null;
        }
        StringBuilder hex = new StringBuilder(hash.length * 2);
        for (byte b : hash) {
            if ((b & 0xFF) < 0x10)
                hex.append("0");
            hex.append(Integer.toHexString(b & 0xFF));
        }

        return hex.toString();
    }

    public void performanceUrl(int sessionId, String url) {
        if (url == null) {
            MMLog.log(TAG, "URL 错误为空.");
        } else {
            MMLog.log(TAG, "performanceUrl = " + url);
            HttpUtils.asynchronousGet(String.valueOf(sessionId),url, sessionId,this);
        }
    }


    public MovieListBean parseJSonToMovieListBean(String jsonStr) {
        MovieListBean movieListBean = null;
        try {
            movieListBean = new Gson().fromJson(jsonStr, MovieListBean.class);
        } catch (JsonSyntaxException e) {
            e.printStackTrace();
        }
        return movieListBean;
    }

    @Override
    public void onHttpRequestProgress(String tag, String fromUrl, String lrl, long progress, long total) {
        switch ((int) progress) {
            case SessionID.SESSION_TYPE_GET_MOVIELIST_ALLTV:
            case SessionID.SESSION_TYPE_GET_MOVIELIST_ALLMOVIE:
            case SessionID.SESSION_TYPE_GET_MOVIELIST_ALLMOVIE2:
            case SessionID.SESSION_TYPE_GET_MOVIELIST_VID:
            case SessionID.SESSION_TYPE_GET_MOVIELIST_VNAME:
            case SessionID.SESSION_TYPE_GET_MOVIELIST_AREA:
            case SessionID.SESSION_TYPE_GET_MOVIELIST_YEAR:
            case SessionID.SESSION_TYPE_GET_MOVIELIST_ACTOR:
            case SessionID.SESSION_TYPE_GET_MOVIELIST_VIP:
            case SessionID.SESSION_TYPE_GET_MOVIE_TYPE:
                mMovieListBean = parseJSonToMovieListBean(lrl);
                break;
            case SessionID.SESSION_TYPE_GET_MOVIE_CATEGORY:
                mVideoCategory = jsonIdNameArrayToMap(lrl);
                break;
            case SessionID.SESSION_TYPE_SCHEDULEPLAYBACK:
                break;
            default://默认尝试转化成视频列表
                // mMovieListBean = parseJSonToMovieListBean(result);
                break;
        }

        if (SessionCallback != null)
            SessionCallback.OnSessionComplete((int) progress, lrl);
    }

    @Override
    public void onHttpRequestComplete(String tag, String fromUrl, String lrl, long progress, long total) {

    }
}




