package com.zhuchao.android.fbase;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Random;
import java.util.Set;
import java.util.Vector;

//内部以链表作为底层实现的集合在执行插入，删除操作时有较好的性能
public class ObjectList {
    private final String TAG = "ObjectList";
    private HashMap<String, Object> mFHashMap = new HashMap<String, Object>();

    public ObjectList() {
        //this.FHashMap = ;
    }

    public int getCount() {
        return mFHashMap.size();
    }

    public void addItem(String Name, Object Obj) {
        mFHashMap.remove(Name);
        mFHashMap.put(Name, Obj);
    }

    public void addObject(String Name, Object Obj) {
        mFHashMap.remove(Name);
        mFHashMap.put(Name, Obj);
    }

    public void remove(String Name) {
        mFHashMap.remove(Name);
    }

    public void delete(String key, Object Obj) {
        mFHashMap.remove(key, Obj);
    }

    public void delete(String Name) {
        mFHashMap.remove(Name);
    }

    public void clear() {
        mFHashMap.clear();
    }

    public void removeObjectsLike(String keyLike) {
        List<String> list = new Vector<>();
        for (Map.Entry<String, Object> entity : mFHashMap.entrySet()) {
            if (entity.getKey().contains(keyLike)) {
                //FHashMap.remove(entity.getKey());
                list.add(entity.getKey());
            }
        }
        for (String key : list) {
            mFHashMap.remove(key);
        }
    }

    public Object get(String keyName) {
        return mFHashMap.get(keyName);
    }

    @SuppressWarnings("unchecked")
    public <T> T getValue(String keyName) {
        return (T) mFHashMap.get(keyName);
    }

    public Object getObject(String Name) {
        return mFHashMap.get(Name);
    }

    public Object getObject(int Index) {
        if (Index < 0 || Index >= mFHashMap.size()) return null;
        Object[] array = mFHashMap.values().toArray();
        return array[Index];
    }

    public String getName(int Index) {
        if (Index < 0 || Index >= mFHashMap.size()) return null;
        String[] array = (String[]) mFHashMap.keySet().toArray();
        if (array == null) return null;
        return array[Index];
    }

    public Object getRandom() {
        Random generator = new Random();
        Object[] values = mFHashMap.values().toArray();
        return values[generator.nextInt(values.length)];
    }

    public String getStringKeyByValue(String value) {
        for (Map.Entry<String, Object> entity : mFHashMap.entrySet()) {
            if (entity.getValue().equals(value)) {
                return entity.getKey();
            }
        }
        return null;
    }

    public boolean exist(String Name) {
        return mFHashMap.containsKey(Name);
    }

    public boolean existObject(String Name) {
        return mFHashMap.containsKey(Name);
    }

    public boolean containsTag(String Name) {
        return mFHashMap.containsKey(Name);
    }

    public boolean containsObject(Object obj) {
        return mFHashMap.containsValue(obj);
    }

    public HashMap<String, Object> getAll() {
        return mFHashMap;
    }

    public Collection<Object> getAllObject() {
        return mFHashMap.values();
    }

    public List<Object> getObjectsLike(String keyLike) {
        ///List<Object> list = new Vector<>();
        List<Object> list = new ArrayList<>();
        for (Map.Entry<String, Object> entity : mFHashMap.entrySet()) {
            if (entity.getKey().contains(keyLike)) {
                list.add(entity.getValue());
            }
        }
        return list;
    }

    public void putString(String key, String value) {
        mFHashMap.put(key, value);
    }

    public void putLong(String key, Long value) {
        mFHashMap.put(key, value);
    }

    public void putInt(String key, int value) {
        mFHashMap.put(key, value);
    }

    public void putFloat(String key, float value) {
        mFHashMap.put(key, value);
    }

    public void putBoolean(String key, boolean value) {
        mFHashMap.put(key, value);
    }

    public void putObject(String key, Object value) {
        mFHashMap.put(key, value);
    }

    public String getString(String key) {
        Object o = mFHashMap.get(key);
        if (o == null) return null;
        try {
            return (String) o;
        } catch (Exception e) {
            return null;
        }
    }

    public String get(String key, String defaultValue) {
        Object o = mFHashMap.get(key);
        if (o == null) return defaultValue;
        try {
            return (String) o;
        } catch (Exception e) {
            return defaultValue;
        }
    }

    public String getString(String key, String defaultValue) {
        Object o = mFHashMap.get(key);
        if (o == null) return defaultValue;
        try {
            return (String) o;
        } catch (Exception e) {
            return defaultValue;
        }
    }

    public int getInt(String key) {
        Object o = mFHashMap.get(key);
        if (o == null) return 0;
        try {
            return (int) o;
        } catch (Exception e) {
            return 0;
        }
    }

    public int getInt(String key, int defaultValue) {
        Object o = mFHashMap.get(key);
        if (o == null) return defaultValue;
        try {
            return (int) o;
        } catch (Exception e) {
            return defaultValue;
        }
    }

    public Long getLong(String key) {
        Object o = mFHashMap.get(key);
        if (o == null) return 0L;
        try {
            return (Long) o;
        } catch (Exception e) {
            return 0L;
        }
    }

    public Long getLong(String key, Long defaultValue) {
        Object o = mFHashMap.get(key);
        if (o == null) return defaultValue;
        try {
            return (Long) o;
        } catch (Exception e) {
            return defaultValue;
        }
    }

    public Float getFloat(String key) {
        Object o = mFHashMap.get(key);
        if (o == null) return 0.00f;
        try {
            return (Float) o;
        } catch (Exception e) {
            return 0.00f;
        }
    }

    public Float getLong(String key, Float defaultValue) {
        Object o = mFHashMap.get(key);
        if (o == null) return defaultValue;
        try {
            return (Float) o;
        } catch (Exception e) {
            return defaultValue;
        }
    }

    public boolean getBoolean(String key) {
        Object o = mFHashMap.get(key);
        if (o == null) return false;
        try {
            return (boolean) o;
        } catch (Exception e) {
            return false;
        }
    }

    public boolean getBoolean(String key, boolean defaultValue) {
        Object o = mFHashMap.get(key);
        if (o == null) return defaultValue;
        try {
            return (boolean) o;
        } catch (Exception e) {
            return defaultValue;
        }
    }

    public Object[] toArray() {
        return mFHashMap.values().toArray();
    }

    @SuppressWarnings("unchecked")
    public <T> List<T> toList() {
        List<T> list = new ArrayList<T>();
        for (Map.Entry<String, Object> entity : mFHashMap.entrySet()) {
            list.add((T) entity.getValue());
        }
        return list;
    }

    @SuppressWarnings("unchecked")
    public <T> List<T> toListLike(String likedName) {
        List<T> list = new ArrayList<T>();
        for (Map.Entry<String, Object> entity : mFHashMap.entrySet()) {
            if (entity.getKey().contains(likedName)) list.add((T) entity.getValue());
        }
        return list;
    }

    /////////////////////////////////////////////////////////////////////////////////////////////////
    public void printAll() {
        int i = 0;
        MMLog.d(TAG, "Print all count:" + getCount());
        for (HashMap.Entry<String, Object> entry : mFHashMap.entrySet()) {
            if (entry.getValue() == null) entry.setValue("null");
            MMLog.log(TAG, i + ":" + entry.getKey() + ":" + entry.getValue().toString());
            i++;
        }
    }

    public void saveObject(String filePath) {
        FileOutputStream outStream = null;
        try {
            outStream = new FileOutputStream(filePath);
            ObjectOutputStream objectOutputStream = new ObjectOutputStream(outStream);
            objectOutputStream.writeObject(mFHashMap);
            outStream.close();
        } catch (Exception ex) {
            MMLog.e(TAG, ex.getMessage()); //e.printStackTrace();
        }
    }

    public void readObject(String filePath) {
        FileInputStream inputStream;
        try {
            inputStream = new FileInputStream(filePath);
            ObjectInputStream objectInputStream = new ObjectInputStream(inputStream);
            mFHashMap = (HashMap<String, Object>) objectInputStream.readObject();
        } catch (IOException | ClassNotFoundException e) {
            // TODO Auto-generated catch block
            MMLog.e(TAG, e.getMessage()); //e.printStackTrace();
        }
    }

    public void saveToFile(String filePathName) {
        try {
            String parentDir = FileUtils.getFilePathFromPathName(filePathName);
            FileUtils.MakeDirsExists(Objects.requireNonNull(parentDir));
            String line = System.getProperty("line.separator");
            StringBuilder stringBuffer = new StringBuilder();
            FileWriter fw = new FileWriter(filePathName);

            Set<Map.Entry<String, Object>> set = mFHashMap.entrySet();
            for (Map.Entry<String, Object> stringObjectEntry : set) {
                stringBuffer.append(((Map.Entry<?, ?>) stringObjectEntry).getKey()).append(" : ").append(((Map.Entry<?, ?>) stringObjectEntry).getValue()).append(line);
            }
            fw.write(stringBuffer.toString());
            fw.close();
        } catch (IOException e) {
            MMLog.e(TAG, e.getMessage()); //e.printStackTrace();
        }
    }

    public void saveToDir(String dir) {
        if (FileUtils.EmptyString(dir)) return;
        try {
            FileUtils.MakeDirsExists(Objects.requireNonNull(dir));
            String filePathName = dir + "/properties";
            String line = System.getProperty("line.separator");
            StringBuilder stringBuffer = new StringBuilder();
            FileWriter fw = new FileWriter(filePathName);

            Set<Map.Entry<String, Object>> set = mFHashMap.entrySet();
            for (Map.Entry<String, Object> stringObjectEntry : set) {
                stringBuffer.append(((Map.Entry<?, ?>) stringObjectEntry).getKey()).append(" : ").append(((Map.Entry<?, ?>) stringObjectEntry).getValue()).append(line);
            }
            fw.write(stringBuffer.toString());
            fw.close();
        } catch (IOException e) {
            MMLog.e(TAG, e.getMessage()); //e.printStackTrace();
        }
    }

    public void saveToDirAsJson(String dir) {
        if (FileUtils.EmptyString(dir)) return;
        try {
            JSONObject jsonObject = new JSONObject();
            FileUtils.MakeDirsExists(Objects.requireNonNull(dir));
            String filePathName = dir + "/properties";
            //String line = System.getProperty("line.separator");
            StringBuilder stringBuffer = new StringBuilder();
            FileWriter fw = new FileWriter(filePathName);
            Set<Map.Entry<String, Object>> set = mFHashMap.entrySet();

            for (Map.Entry<String, Object> stringObjectEntry : set) {
                //stringBuffer.append(((Map.Entry<?, ?>) stringObjectEntry).getKey()).append(" : ").append(((Map.Entry<?, ?>) stringObjectEntry).getValue()).append(line);
                jsonObject.put(((Map.Entry<?, ?>) stringObjectEntry).getKey().toString(), ((Map.Entry<?, ?>) stringObjectEntry).getValue());
            }
            stringBuffer.append(jsonObject.toString());
            fw.write(stringBuffer.toString());
            fw.close();
        } catch (IOException e) {
            MMLog.e(TAG, e.getMessage()); //e.printStackTrace();
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    public void saveToFile() {
        try {
            String line = System.getProperty("line.separator");
            String filePathName = FileUtils.getDirBaseExternalStorageDirectory(".com.zhuchao") + "/properties";
            String parentDir = FileUtils.getFilePathFromPathName(filePathName);
            FileUtils.MakeDirsExists(Objects.requireNonNull(parentDir));

            StringBuilder stringBuffer = new StringBuilder();
            FileWriter fw = new FileWriter(filePathName);

            Set<Map.Entry<String, Object>> set = mFHashMap.entrySet();
            for (Map.Entry<String, Object> stringObjectEntry : set) {
                stringBuffer.append(((Map.Entry<?, ?>) stringObjectEntry).getKey()).append(" : ").append(((Map.Entry<?, ?>) stringObjectEntry).getValue()).append(line);
            }
            fw.write(stringBuffer.toString());
            fw.close();
        } catch (IOException e) {
            MMLog.e(TAG, e.getMessage()); //e.printStackTrace();
        }
    }

    /*public void saveToCache() {
        try {
            String line = System.getProperty("line.separator");
            //String filePathName = FileUtils.getDirBaseExternalStorageDirectory(".zhuchao") + "/properties";
            String filePathName = FileUtils.getDiskCachePath().getDirBaseExternalStorageDirectory(".zhuchao") + "/properties";
            String parentDir = FileUtils.getFilePathFromPathName(filePathName);
            FileUtils.CheckDirsExists(Objects.requireNonNull(parentDir));

            StringBuilder stringBuffer = new StringBuilder();
            FileWriter fw = new FileWriter(filePathName);

            Set<Map.Entry<String, Object>> set = FHashMap.entrySet();
            for (Map.Entry<String, Object> stringObjectEntry : set) {
                stringBuffer.append(((Map.Entry<?, ?>) stringObjectEntry).getKey()).append(" : ").append(((Map.Entry<?, ?>) stringObjectEntry).getValue()).append(line);
            }
            fw.write(stringBuffer.toString());
            fw.close();
        } catch (IOException e) {
            MMLog.e(TAG, e.getMessage()); //e.printStackTrace();
        }
    }*/
}
