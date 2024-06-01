package com.zhuchao.android.fbase;

import static com.zhuchao.android.fbase.FileUtils.EmptyString;
import static com.zhuchao.android.fbase.FileUtils.NotEmptyString;

import com.zhuchao.android.fbase.eventinterface.FileFingerCallback;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Objects;

public class FilesFinger extends ObjectList {
    private final String TAG = "FilesFinger";
    private FileFingerCallback RequestCallBack = null;
    private int count = 0;
    private boolean stopScan = false;
    private int sleepTime = -1;
    private final List<String> dirList = new ArrayList<String>();
    private final List<ScanThread> threadPool = new ArrayList<ScanThread>();
    private final List<String> fileTypes = new ArrayList<String>();
    //private long lStart = 0;
    private boolean bNeedProgress = true;
    private boolean bMultiThread = true;
    private long totalSize = 0;


    public FilesFinger() {
        super();
        count = 0;
        RequestCallBack = null;
        //dirList = new ArrayList<String>();
        //threadPool = new ArrayList<ScanThread>();
        //fileTypes = new ArrayList<String>();
    }

    public FilesFinger(FileFingerCallback fileFingerCallback) {
        super();
        count = 0;
        RequestCallBack = fileFingerCallback;
        //dirList = new ArrayList<String>();
        //threadPool = new ArrayList<ScanThread>();
        //fileTypes = new ArrayList<String>();
    }

    public void setMultiThread(boolean bMultiThread) {
        this.bMultiThread = bMultiThread;
    }

    public FilesFinger callBack(FileFingerCallback fileFingerCallback) {
        RequestCallBack = fileFingerCallback;
        return this;
    }

    public void setNeedProgress(boolean bNeedProgress) {
        this.bNeedProgress = bNeedProgress;
    }

    public long getTotalSize() {
        return totalSize;
    }

    public void addFile(String filePathName) {
        File file = new File(filePathName);
        if (file.exists()) addItem(filePathName, file);
    }

    public void addFile(String fileKey, File file) {
        addItem(fileKey, file);
    }

    public void addFile(File file) {
        addItem(file.getAbsolutePath(), file);
    }

    public File getFile(String fileKey) {
        Object obj = getObject(fileKey);
        if (obj != null) return (File) obj;
        return null;
    }

    public File getFile(int Index) {
        Object obj = getObject(Index);
        if (obj != null) return (File) obj;
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

    private synchronized void fileCounter() {
        this.count++;
    }

    public void clearAll() {
        stopScan();
        dirList.clear();
        threadPool.clear();
        clear();
        fileTypes.clear();
    }

    public void free() {
        clearAll();
    }

    private boolean fileTypesMatch(String fileName) {
        if (fileTypes.size() <= 0) return true;
        if (EmptyString(fileName)) return false;

        for (String ext : fileTypes) {
            if (ext.equals(".*")) return true;
            if (fileName.endsWith(ext)) return true;
        }
        return false;
    }

    public void fingerFromDir(String dirPath) {
        if (dirList.contains(dirPath)) return;
        //lStart = System.currentTimeMillis();
        searchDir(dirPath);
    }

    private void searchDir(String dirPath) {
        if (!FileUtils.existDirectory(dirPath)) {
            //MMLog.log(TAG,"do not exists dir "+dirPath);
            return;
        }
        if (!dirList.contains(dirPath)) {
            ScanThread scanThread = new ScanThread();
            synchronized (dirList) {
                dirList.add(dirPath);
            }
            synchronized (threadPool) {
                threadPool.add(scanThread);
            }
            scanThread.tag = dirPath;
            stopScan = false;
            scanThread.start();
        }
    }

    private void getFiles(String FilePath) {
        File file = new File(FilePath);
        if (!file.exists()) return;
        File[] files = file.listFiles();
        if (files != null) getFileList(files);
    }

    private void getFileList(File[] files) {
        if (files == null) return;
        for (File file : files) {
            if (stopScan) break;
            try {
                if (sleepTime > 0) {
                    Thread.sleep(sleepTime);
                    sleepTime = 0;
                }
            } catch (InterruptedException e) {
                //e.printStackTrace();
            }
            if (file.isDirectory()) {
                if (bMultiThread) searchDir(file.getAbsolutePath());
                else getFileList(Objects.requireNonNull(file.listFiles()));
            } else //if(file.isFile())
            {
                totalSize = totalSize + file.length();
                String filePathName = file.getAbsolutePath();// file.getPath();
                if (fileTypesMatch(filePathName)) {
                    addFile(filePathName, file);
                    if (bNeedProgress) {
                        fileCounter();
                        if (RequestCallBack != null) {
                            RequestCallBack.onFileCallback(filePathName, file.length(), count);
                        }
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
            if (NotEmptyString(tag)) {
                //MMLog.log(TAG,"start scan dir "+tag);
                getFiles(tag);
            }
            ////////////////////////////////////////////////////////////////////////////////////////
            synchronized (threadPool) {
                if (threadPool.contains(this)) {
                    threadPool.remove(this);
                    //MMLog.log(TAG,"return "+getCount()+ ":" + tag);
                }
            }
            if (RequestCallBack != null && threadPool.isEmpty()) {
                RequestCallBack.onFileCallback(null, totalSize, getCount());
            }
        }
    }

}
