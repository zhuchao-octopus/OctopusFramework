package com.zhuchao.android.libfileutils;

import static com.zhuchao.android.libfileutils.FileUtils.EmptyString;
import static com.zhuchao.android.libfileutils.FileUtils.NotEmptyString;

import com.zhuchao.android.callbackevent.NormalCallback;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class FilesFinger extends ObjectList {
    private NormalCallback RequestCallBack = null;
    private int count = 0;
    private boolean stopScan = false;
    private int sleepTime = -1;
    private List<String> dirList = null;
    private List<ScanThread> threadPool = null;
    private long lStart = 0;
    private List<String> fileTypes = null;

    public FilesFinger(NormalCallback requestCallBack) {
        super();
        count = 0;
        RequestCallBack = requestCallBack;
        dirList = new ArrayList<String>();
        threadPool = new ArrayList<ScanThread>();
        fileTypes = new ArrayList<String>();
    }

    public void callBack(NormalCallback requestCallBack) {
        RequestCallBack = requestCallBack;
    }

    public void addFile(String filePathName) {
        File file = new File(filePathName);
        if (file.exists())
            addItem(filePathName, file);
    }

    public void addFile(String fileKey, File file) {
        addItem(fileKey, file);
    }

    public void addFile(File file) {
        addItem(file.getAbsolutePath(), file);
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

    public String getFileName(int Index) {
        return getName(Index);
    }

    public HashMap<String, Object> getAllFiles() {
        return getAll();
    }

    public void addType(String extName) {
        fileTypes.add(extName);
    }

    private boolean fileTypesMatch(String fileName) {
        if (EmptyString(fileName)) return false;

        if (fileTypes.size() <= 0) return true;
        for (String ext : fileTypes) {
            if (ext.equals(".*")) return true;
            if (fileName.endsWith(ext))
                return true;
        }
        return false;
    }

    public void fingerFromDir(String dirPath) {
        lStart = System.currentTimeMillis();
        File file = new File(dirPath);
        if (!file.exists()) return;
        if (dirList.indexOf(dirPath) < 0) {
            dirList.add(dirPath);
            ScanThread scanThread = new ScanThread();
            threadPool.add(scanThread);
            scanThread.tag = dirPath;
            stopScan = false;
            scanThread.start();
        }
    }

    private void getFiles(String FilePath) {
        File file = new File(FilePath);
        if (!file.exists()) return;
        File[] files = file.listFiles();
        getFileList(files);
    }

    private void getFileList(File[] files) {
        if (files == null) return;
        String filePathName = null;
        for (File file : files) {
            if (stopScan) break;
            try {
                if (sleepTime > 0) {
                    Thread.sleep(sleepTime);
                    sleepTime = 0;
                }
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            if (file.isDirectory()) {
                getFileList(file.listFiles());
            } else {
                filePathName = file.getPath();
                if (fileTypesMatch(filePathName)) {
                    addFile(filePathName, file);
                    count++;
                    if (RequestCallBack != null) {
                        RequestCallBack.onEventRequest(filePathName, count);
                    }
                }
            }
        }
    }

    private class ScanThread extends Thread {
        String tag = null;

        @Override
        public void run() {
            super.run();
            if(NotEmptyString(tag))
                getFiles(tag);
            if (RequestCallBack != null)
                RequestCallBack.onEventRequest("TimeElapsed:" + (System.currentTimeMillis() - lStart) + "ms-->" + tag, count);
            if (threadPool.indexOf(this) >= 0)
                threadPool.remove(this);
        }
    }

    public void stopScan() {
        stopScan = true;
    }

    public void sleepScan(int time) {
        sleepTime = time;
    }

    public int getStatus() {
        return sleepTime;
    }

    public void saveToFile(String fileName) {
        saveObject(fileName);
    }

    public void readFromFile(String fileName) {
        readObject(fileName);
    }

    public void clearAll() {
        stopScan();
        dirList.clear();
        threadPool.clear();
        clear();
        fileTypes.clear();
    }
}
