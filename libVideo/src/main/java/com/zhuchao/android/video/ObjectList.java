package com.zhuchao.android.video;

import android.text.TextUtils;

import com.zhuchao.android.libfileutils.FilesManager;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Random;

public class ObjectList {
    protected HashMap<String, Object> FHashMap;

    public ObjectList() {
        this.FHashMap = new HashMap();
    }

    public void add(String Key, Object Obj) {
        if (TextUtils.isEmpty(Key))
            return;
        FHashMap.put(Key, Obj);
    }

    public void delete(String Key) {
        FHashMap.remove(Key);
    }

    public void delete(Object Obj) {
        FHashMap.remove(Obj);
    }

    public void delete(int Index) {
        Object Obj = getObject(Index);
        if (Obj != null)
            FHashMap.remove(Obj);
    }

    public void clear() {
        FHashMap.clear();
    }

    public Object getObject(String Key) {
        return FHashMap.get(Key);
    }

    public Object getObject(int Index) {
        if (Index < 0 || Index >= FHashMap.size()) return null;
        Object[] array = FHashMap.values().toArray();
        if (array == null) return null;
        return array[Index];
    }

    public Object getRandom() {
        Random generator = new Random();
        Object[] values = FHashMap.values().toArray();
        Object randomValue = values[generator.nextInt(values.length)];
        return randomValue;
    }

    public int getCount() {
        return FHashMap.size();
    }

}
