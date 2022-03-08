package com.zhuchao.android.databaseutil;

import android.content.Context;

public class SPreference {


    public static void saveSharedPreferences(Context mContext,String name,String key ,String value)
    {
        android.content.SharedPreferences sharedPreferences= mContext.getSharedPreferences(name, Context.MODE_PRIVATE);
        android.content.SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString(key,value);
        editor.commit();
    }
    public static void clearSharedPreferences(Context mContext,String name)
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

    public static void putLong(Context mContext,String name,String key ,long value)
    {
        android.content.SharedPreferences sharedPreferences= mContext.getSharedPreferences(name, Context.MODE_PRIVATE);
        android.content.SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putLong(key,value);
        editor.commit();
    }
    public static long getLong(Context mContext,String name,String key)
    {
        android.content.SharedPreferences sharedPreferences= mContext.getSharedPreferences(name, Context .MODE_PRIVATE);
        return sharedPreferences.getLong(key,0);
    }
}
