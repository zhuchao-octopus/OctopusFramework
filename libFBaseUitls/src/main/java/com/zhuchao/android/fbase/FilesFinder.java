package com.zhuchao.android.fbase;

import static com.zhuchao.android.fbase.FileUtils.EmptyString;
import static com.zhuchao.android.fbase.FileUtils.NotEmptyString;
import com.zhuchao.android.eventinterface.FileFingerCallback;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class FilesFinder extends ObjectArray{
    private final String TAG = "FilesFinder";
    private FileFingerCallback fileFingerCallback = null;
    private int count = 0;
    private boolean stopScan = false;
    private int sleepTime = -1;
    private final List<String> dirList  = new ArrayList<String>();
    private final List<ScanThread> threadPool= new ArrayList<ScanThread>();
    //private long lStart = 0;
    private boolean bNeedProgress = true;
    private boolean bMultiThread = true;
    private long totalSize = 0;
    private final List<String> fileTypes = new ArrayList<String>();

    public FilesFinder() {
        super();
        count = 0;
        //fileFingerCallback = null;
    }

    public FilesFinder(FileFingerCallback fileFingerCallback) {
        super();
        count = 0;
        this.fileFingerCallback = fileFingerCallback;
    }

    public void setMultiThread(boolean bMultiThread) {
        this.bMultiThread = bMultiThread;
    }

    public FilesFinder callBack(FileFingerCallback fileFingerCallback) {
        this.fileFingerCallback = fileFingerCallback;
        return this ;
    }

    public void setNeedProgress(boolean bNeedProgress) {
        this.bNeedProgress = bNeedProgress;
    }

    public long getTotalSize() {
        return totalSize;
    }

    public void addFile(String filePathName) {
        File file = new File(filePathName);
        if (file.exists())
            add(filePathName);
    }

    public String getFileName(int Index) {
        return (String) get(Index);
    }

    public void addType(String extName) {
        fileTypes.add(extName);
    }

    private boolean fileTypesMatch(String fileName) {
        if (fileTypes.size() <= 0) return true;
        if (EmptyString(fileName)) return false;
        for (String ext : fileTypes) {
            if (ext.equals(".*")) return true;
            if (fileName.endsWith(ext))
                return true;
        }
        return false;
    }

    public void fingerFromDir(String dirPath) {
        if (dirList.contains(dirPath))
            return;
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
            dirList.add(dirPath);
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
        if (files != null)
            getFileList(files);
    }

    private void getFileList(File[] files) {
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
                if (bMultiThread)
                    searchDir(file.getAbsolutePath());
                else
                    getFileList(file.listFiles());
            } else //if(file.isFile())
            {
                totalSize = totalSize + file.length();
                String filePathName = file.getAbsolutePath();// file.getPath();
                if (fileTypesMatch(filePathName)) {
                    addFile(filePathName);
                    if (bNeedProgress) {
                        fileCounter();
                        if (fileFingerCallback != null) {
                            fileFingerCallback.onFileCallback(filePathName,file.length(),count);
                        }

                    }
                }
            }
        }
    }

    private synchronized void fileCounter() {
        this.count++;
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
            if (fileFingerCallback != null && threadPool.isEmpty()) {
                fileFingerCallback.onFileCallback(null, totalSize,size());
            }
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
}
