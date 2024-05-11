package com.zhuchao.android.fbase.bean;

import java.util.ArrayList;
import java.util.List;

public class DirBean {
    String name;
    String path;
    List<String> filesList;
    int fileCount;
    long size;

    public DirBean(String name, String path) {
        this.name = name;
        this.path = path;
        filesList = new ArrayList<>();
    }

    public void add(String filePath) {
        filesList.add(filePath);
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public int getSubItemCount() {
        fileCount = filesList.size();
        return fileCount;
    }

    public long getSize() {
        return size;
    }

    public void setSize(long size) {
        this.size = size;
    }

    public List<String> getFileList() {
        return filesList;
    }
}
