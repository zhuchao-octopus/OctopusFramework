package com.zhuchao.android.video;

import android.text.TextUtils;

import com.zhuchao.android.libfileutils.FilesManager;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Random;

public class FileList {
    public HashMap<String, Object> hashMap;
    public FileList() {
        this.hashMap = new HashMap();
    }
    public void add(String FilePathName) {
        if(TextUtils.isEmpty(FilePathName))
         return;
        //if (findByPath(FilePathName) != null) return;
        hashMap.put(FilesManager.getFileName(FilePathName), FilePathName);
    }

    public int getCount() {
        return hashMap.size();
    }

    void delete(String FilePathName) {

    }

    public void clear() {
        hashMap.clear();
    }




}
