package com.zhuchao.android.fileutils;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.Collection;
import java.util.HashMap;
import java.util.Random;

//内部以链表作为底层实现的集合在执行插入，删除操作时有较好的性能
public class ObjectList {
    private final String TAG = "ObjectList";
    private HashMap<String, Object> FHashMap;

    public ObjectList() {
        this.FHashMap = new HashMap();
    }

    public void addItem(String Name, Object Obj) {
        FHashMap.put(Name, Obj);
    }

    public Object buildItem(String Name, Object obj) {
        Object o = getObject(Name);
        if (o != null)
            return o;
        addItem(Name, obj);
        return obj;
    }

    public void delete(String Name) {
        FHashMap.remove(Name);
    }

    public void delete(String key, Object Obj) {
        FHashMap.remove(key, Obj);
    }

    public void clear() {
        FHashMap.clear();
    }

    public Object getObject(String Name) {
        return FHashMap.get(Name);
    }

    public Object getObject(int Index) {
        if (Index < 0 || Index >= FHashMap.size()) return null;
        Object[] array = FHashMap.values().toArray();
        if (array == null) return null;
        return array[Index];
    }

    public String getName(int Index) {
        if (Index < 0 || Index >= FHashMap.size()) return null;
        String[] array = (String[]) FHashMap.keySet().toArray();
        if (array == null) return null;
        return array[Index];
    }

    public Object getRandom() {
        Random generator = new Random();
        Object[] values = FHashMap.values().toArray();
        Object randomValue = values[generator.nextInt(values.length)];
        return randomValue;
    }

    public void putString(String key, String value) {
        FHashMap.put(key, value);
    }

    public void putLong(String key, Long value) {
        FHashMap.put(key, value);
    }

    public void putInt(String key, int value) {
        FHashMap.put(key, value);
    }

    public void putFloat(String key, float value) {
        FHashMap.put(key, value);
    }

    public void putBoolean(String key, boolean value) {
        FHashMap.put(key, value);
    }

    public void putObject(String key, Object value) {
        FHashMap.put(key, value);
    }

    public String getString(String key) {
        Object o = FHashMap.get(key);
        if (o == null) return null;
        try {
            return (String) o;
        } catch (Exception e) {
            return null;
        }
    }

    public String get(String key, String defaultValue) {
        Object o = FHashMap.get(key);
        if (o == null) return defaultValue;
        try {
            return (String) o;
        } catch (Exception e) {
            return defaultValue;
        }
    }

    public String getString(String key, String defaultValue) {
        Object o = FHashMap.get(key);
        if (o == null) return defaultValue;
        try {
            return (String) o;
        } catch (Exception e) {
            return defaultValue;
        }
    }

    public int getInt(String key) {
        Object o = FHashMap.get(key);
        if (o == null) return 0;
        try {
            return (int) o;
        } catch (Exception e) {
            return 0;
        }
    }

    public int getInt(String key, int defaultValue) {
        Object o = FHashMap.get(key);
        if (o == null) return defaultValue;
        try {
            return (int) o;
        } catch (Exception e) {
            return defaultValue;
        }
    }

    public Long getLong(String key) {
        Object o = FHashMap.get(key);
        if (o == null) return 0L;
        try {
            return (Long) o;
        } catch (Exception e) {
            return 0L;
        }
    }

    public Long getLong(String key, Long defaultValue) {
        Object o = FHashMap.get(key);
        if (o == null) return defaultValue;
        try {
            return (Long) o;
        } catch (Exception e) {
            return defaultValue;
        }
    }

    public Float getFloat(String key) {
        Object o = FHashMap.get(key);
        if (o == null) return 0.00f;
        try {
            return (Float) o;
        } catch (Exception e) {
            return 0.00f;
        }
    }

    public Float getLong(String key, Float defaultValue) {
        Object o = FHashMap.get(key);
        if (o == null) return defaultValue;
        try {
            return (Float) o;
        } catch (Exception e) {
            return defaultValue;
        }
    }

    public boolean getBoolean(String key) {
        Object o = FHashMap.get(key);
        if (o == null) return false;
        try {
            return (boolean) o;
        } catch (Exception e) {
            return false;
        }
    }

    public boolean getBoolean(String key, boolean defaultValue) {
        Object o = FHashMap.get(key);
        if (o == null) return defaultValue;
        try {
            return (boolean) o;
        } catch (Exception e) {
            return defaultValue;
        }
    }

    public boolean existObject(String Name) {
        return FHashMap.containsKey(Name);
    }

    public HashMap<String, Object> getAll() {
        return FHashMap;
    }

    public Collection<Object> getAllObject() {
        return FHashMap.values();
    }

    public int getCount() {
        return FHashMap.size();
    }

    public void printAll() {
        for (HashMap.Entry<String, Object> m : FHashMap.entrySet()) {
            MMLog.log(TAG, m.getKey() + ":" + m.getValue().toString());
        }
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

    public void readObject(String filePath) {
        FileInputStream inputStream;
        try {
            inputStream = new FileInputStream(filePath);
            ObjectInputStream objectInputStream = new ObjectInputStream(inputStream);
            FHashMap = (HashMap<String, Object>) objectInputStream.readObject();
        } catch (IOException | ClassNotFoundException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }
}
