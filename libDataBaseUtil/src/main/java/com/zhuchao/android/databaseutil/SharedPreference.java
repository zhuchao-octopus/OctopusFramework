package com.zhuchao.android.databaseutil;

import android.content.Context;

public class SharedPreference {


    public static void saveSharedPreferences(Context mContext,String name,String key ,String value)
    {
        android.content.SharedPreferences sharedPreferences= mContext.getSharedPreferences(name, Context.MODE_PRIVATE);
        android.content.SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString(key,value);
        editor.commit();
    }
    public static void ClearSharedPreferences(Context mContext,String name)
    {
        android.content.SharedPreferences sharedPreferences= mContext.getSharedPreferences(name,Context.MODE_PRIVATE);
        android.content.SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.clear();
        editor.commit();
    }
    public static String getSharedPreferences(Context mContext,String name,String key)
    {
        android.content.SharedPreferences sharedPreferences= mContext.getSharedPreferences(name, Context .MODE_PRIVATE);
        return sharedPreferences.getString(key,null);
    }


}
