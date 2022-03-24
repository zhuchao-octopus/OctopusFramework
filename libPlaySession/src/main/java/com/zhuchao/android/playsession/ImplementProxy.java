package com.zhuchao.android.playsession;

import android.util.Log;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import com.google.gson.JsonSyntaxException;
import com.google.gson.reflect.TypeToken;
import com.zhuchao.android.netutil.HttpResultCallBack;
import com.zhuchao.android.netutil.MyHttpUtils;
import com.zhuchao.android.playsession.PaserBean.IdNameBean;
import com.zhuchao.android.playsession.PaserBean.MovieListBean;

import java.io.UnsupportedEncodingException;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HashMap;
import java.util.Map;

public class ImplementProxy implements HttpResultCallBack {
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
            Log.d(TAG, "URL 错误为空.");
        } else {
            Log.d(TAG, "performanceUrl = " + url);
            MyHttpUtils.doGetAsyn(url, sessionId,this);
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
    public synchronized void onHttpRequestComplete(String result, int resultIndex) {

        switch (resultIndex) {
            case Data.SESSION_TYPE_GET_MOVIELIST_ALLTV:
            case Data.SESSION_TYPE_GET_MOVIELIST_ALLMOVIE:
            case Data.SESSION_TYPE_GET_MOVIELIST_ALLMOVIE2:
            case Data.SESSION_TYPE_GET_MOVIELIST_VID:
            case Data.SESSION_TYPE_GET_MOVIELIST_VNAME:
            case Data.SESSION_TYPE_GET_MOVIELIST_AREA:
            case Data.SESSION_TYPE_GET_MOVIELIST_YEAR:
            case Data.SESSION_TYPE_GET_MOVIELIST_ACTOR:
            case Data.SESSION_TYPE_GET_MOVIELIST_VIP:
            case Data.SESSION_TYPE_GET_MOVIE_TYPE:
                mMovieListBean = parseJSonToMovieListBean(result);
                break;
            case Data.SESSION_TYPE_GET_MOVIE_CATEGORY:
                mVideoCategory = jsonIdNameArrayToMap(result);
                break;
            case Data.SESSION_TYPE_SCHEDULEPLAYBACK:
                break;
            default://默认尝试转化成视频列表
               // mMovieListBean = parseJSonToMovieListBean(result);
                break;
        }

        if (SessionCallback != null)
            SessionCallback.OnSessionComplete(resultIndex, result);
    }
}




