package com.zhuchao.android.fbase;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonSyntaxException;
import com.google.gson.reflect.TypeToken;

import java.util.Map;

public class TConverter {
    private final static String TAG = "TConverter";

    public static <T> T fromJson(String json, Class<T> classOfT) {
        try {
            return new Gson().fromJson(json,
                    classOfT);
        } catch (JsonSyntaxException e) {
            MMLog.e(TAG, "fromJson failed " + e.toString() + "," + json);
            return null;
        }
    }

    public static Map<Integer, String> jsonToMap(String JsonStr) {
        Gson gson = new GsonBuilder().serializeNulls().disableHtmlEscaping().create();
        try {
            return gson.fromJson(JsonStr, new TypeToken<Map<Integer, String>>() {
            }.getType());
        } catch (JsonSyntaxException e) {
            e.printStackTrace();
        }
        return null;
    }

    public static String mapToJson(Map<String, Object> map) {
        Gson gson = new GsonBuilder().serializeNulls().disableHtmlEscaping().create();
        try {
            return gson.toJson(map);
        } catch (JsonSyntaxException e) {
            e.printStackTrace();
        }
        return null;
    }
}
