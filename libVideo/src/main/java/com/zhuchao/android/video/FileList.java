package com.zhuchao.android.video;

import com.zhuchao.android.callbackevent.NormalRequestCallback;

import java.io.File;

public class FileList extends ObjectList {
    private NormalRequestCallback RequestCallBack = null;
    private int count = 0;
    private boolean threadLock = false;

    public FileList(NormalRequestCallback requestCallBack) {
        super();
        RequestCallBack = requestCallBack;
        count = 0;
    }

    public void addFile(String filePathName) {
        File file = new File(filePathName);
        if(file.exists())
        add(filePathName, file);
    }

    public void addFile(String fileKey, File file) {
        add(fileKey, file);
    }

    public void addFile(File file) {
        add(file.getAbsolutePath(), file);
    }

    public File getFile(String fileKey) {
        Object obj = getObject(fileKey);
        if (obj != null)
            return (File) obj;
        return null;
    }

    public File getFile(int Index) {
        Object obj = getObject(Index);
        if (obj != null)
            return (File) obj;
        return null;
    }

    public File getRandom() {
        Object obj = getRandom();
        if (obj != null)
            return (File) obj;
        return null;
    }

    public void loadFromDir(String dirPath) {
        if (threadLock) return;
        new Thread() {
            public void run() {
                threadLock = true;
                getFiles(dirPath);
                threadLock = false;
            }
        }.start();
    }

    private void getFiles(String FilePath) {
        File path = new File(FilePath);
        File[] files = path.listFiles();
        getFileList(files);
    }

    private void getFileList(File[] files) {
        if (files == null) return;
        String filePathName = null;
        for (File file : files) {
            if (file.isDirectory()) {
                getFileList(file.listFiles());
            } else {
                filePathName = file.getPath();
                addFile(filePathName, file);
                count++;
                if (RequestCallBack != null) {
                    RequestCallBack.onRequestComplete(filePathName, count);
                }
            }
        }
    }

}
