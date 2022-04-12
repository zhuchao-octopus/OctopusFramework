package com.zhuchao.android.libfileutils;

import android.text.TextUtils;

import com.zhuchao.android.libfileutils.FilesManager;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
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

    public Object getRandomObject() {
        Random generator = new Random();
        Object[] values = FHashMap.values().toArray();
        Object randomValue = values[generator.nextInt(values.length)];
        return randomValue;
    }

    public String getKey(int Index) {
        if (Index < 0 || Index >= FHashMap.size()) return null;
        String[] array = (String[]) FHashMap.keySet().toArray();
        if (array == null) return null;
        return array[Index];
    }

    public int getCount() {
        return FHashMap.size();
    }

    public HashMap<String, Object> getAllMap() {
        return FHashMap;
    }

    public void saveObject(String filePath) {
        FileOutputStream outStream = null;
        try {
            outStream = new FileOutputStream(filePath);
            ObjectOutputStream objectOutputStream = new ObjectOutputStream(outStream);
            objectOutputStream.writeObject(FHashMap);
            outStream.close();
        } catch (Exception ex) {
            ex.printStackTrace();
        } finally {
        }
    }
    public void readObject(String filePath)
    {
        FileInputStream inputStream;
        try {
            inputStream = new FileInputStream(filePath);
            ObjectInputStream objectInputStream = new ObjectInputStream(inputStream);
            FHashMap=(HashMap<String, Object>)objectInputStream.readObject();
        } catch (FileNotFoundException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (ClassNotFoundException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }
}
