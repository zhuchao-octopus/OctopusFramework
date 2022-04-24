package com.zhuchao.android.libfileutils;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.HashMap;
import java.util.Random;

public class ObjectList {
    private HashMap<String, Object> FHashMap;

    public ObjectList() {
        this.FHashMap = new HashMap();
    }

    public Object buildItem(String Name, Object obj) {
        Object o = getObject(Name);
        if (o != null)
            return o;
        addItem(Name, obj);
        return obj;
    }

    public void addItem(String Name, Object Obj) {
        FHashMap.put(Name, Obj);
    }

    public void delete(String Name) {
        FHashMap.remove(Name);
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

    public boolean existObject(String Name) {
        return FHashMap.containsKey(Name);
    }

    public HashMap<String, Object> getAll() {
        return FHashMap;
    }

    public int getCount() {
        return FHashMap.size();
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
