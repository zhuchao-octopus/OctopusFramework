package com.zhuchao.android.persist;

import android.content.Context;
import android.content.SharedPreferences;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public class TPersistent implements SharedPreferences, SharedPreferences.Editor {
    private Context mContext = null;
    private android.content.SharedPreferences sharedPreferences = null;
    private android.content.SharedPreferences.Editor editor = null;

    public TPersistent(Context mContext, String name) {
        this.mContext = mContext;
        sharedPreferences = this.mContext.getSharedPreferences(name, Context.MODE_PRIVATE);
        editor = sharedPreferences.edit();
    }

    @Override
    public Map<String, ?> getAll() {
        return sharedPreferences.getAll();
    }

    @Override
    public String getString(String s, String s1) {
        return sharedPreferences.getString(s, s1);
    }

    @Override
    public Set<String> getStringSet(String s, Set<String> set) {
        return sharedPreferences.getStringSet(s, set);
    }

    @Override
    public int getInt(String s, int i) {
        return sharedPreferences.getInt(s, i);
    }

    @Override
    public long getLong(String s, long l) {
        return sharedPreferences.getLong(s, l);
    }

    @Override
    public float getFloat(String s, float v) {
        return sharedPreferences.getFloat(s, v);
    }

    @Override
    public boolean getBoolean(String s, boolean b) {
        return sharedPreferences.getBoolean(s, b);
    }

    @Override
    public boolean contains(String s) {
        return sharedPreferences.contains(s);
    }

    @Override
    public Editor edit() {
        return sharedPreferences.edit();
    }

    @Override
    public void registerOnSharedPreferenceChangeListener(OnSharedPreferenceChangeListener onSharedPreferenceChangeListener) {

    }

    @Override
    public void unregisterOnSharedPreferenceChangeListener(OnSharedPreferenceChangeListener onSharedPreferenceChangeListener) {

    }

    @Override
    public Editor putString(String s, String s1) {
        return editor.putString(s, s1);
    }

    @Override
    public Editor putStringSet(String s, Set<String> set) {
        return editor.putStringSet(s, set);
    }

    @Override
    public Editor putInt(String s, int i) {
        return editor.putInt(s, i);
    }

    @Override
    public Editor putLong(String s, long l) {
        return editor.putLong(s, l);
    }

    @Override
    public Editor putFloat(String s, float v) {
        return editor.putFloat(s, v);
    }

    @Override
    public Editor putBoolean(String s, boolean b) {
        return editor.putBoolean(s, b);
    }

    @Override
    public Editor remove(String s) {
        return editor.remove(s);
    }

    @Override
    public Editor clear() {
        return editor.clear();
    }

    @Override
    public boolean commit() {
        return editor.commit();
    }

    @Override
    public void apply() {
        editor.apply();
    }

    public void loadFromMap(HashMap<String, Object> HashMap)
    {
        Set<Map.Entry<String, Object>> set = HashMap.entrySet();
        for (Map.Entry<String, Object> stringObjectEntry : set) {
          //((Map.Entry<?, ?>) stringObjectEntry).getKey().toString(),
          //((Map.Entry<?, ?>) stringObjectEntry).getValue()
            try {
                putString(((Map.Entry<?, ?>) stringObjectEntry).getKey().toString(), (String) ((Map.Entry<?, ?>) stringObjectEntry).getValue());
            } catch (Exception e) {
                //e.printStackTrace();
            }
        }
    }
}
