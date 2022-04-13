package com.zhuchao.android.netutil;

import android.text.TextUtils;

import com.zhuchao.android.callbackevent.NormalRequestCallback;
import com.zhuchao.android.libfileutils.FilesManager;
import com.zhuchao.android.libfileutils.ThreadPool;

import java.util.ArrayList;
import java.util.List;


public class DownloadManager {
    private static final String TAG = "DownloadManager";
    private static ThreadPool threadPool = new ThreadPool();
    private static DownLoadThread downLoadThread = null;

    public static void batDownload(String fromPathName, String toPath) {
        if (threadPool.exist(fromPathName)) return;//任务已经在进行中
        if (TextUtils.isEmpty(fromPathName)) return;
        if (!FilesManager.isExists(fromPathName)) return;

        downLoadThread = new DownLoadThread(fromPathName,toPath);
        threadPool.add(fromPathName,downLoadThread);
        downLoadThread.start();
    }

    private static void download(String fromPathName) {
        final String fileName = FilesManager.getFileName(fromPathName);
        final String dlPath = FilesManager.getDownloadDir(null) + fileName + ".d";
        final String lPath = FilesManager.getDownloadDir(null) + fileName;
        if (FilesManager.isExists(lPath))
            return;//已经完成下载，不再重复下载
        try {
            if (FilesManager.isExists(dlPath)) {
                FilesManager.deleteFile(dlPath);//删除之前的未下载完的临时文件
            }
            OkHttpUtils.Download(fromPathName, dlPath, fileName, new NormalRequestCallback() {
                @Override
                public void onRequestComplete(String result, int resultIndex) {
                    if (resultIndex >= 0) {
                        FilesManager.renameFile(dlPath, lPath);
                        threadPool.delete(fromPathName);
                    }
                }
            });
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static void download(String fromPathName, String toPath) {
        final String fileName = FilesManager.getFileName(fromPathName);
        final String dlPath = toPath + "/" + fileName + ".d";
        final String lPath = toPath + "/" + fileName;
        if (FilesManager.isExists(lPath))
            return;
        try {
            if (FilesManager.isExists(dlPath)) {
                FilesManager.deleteFile(dlPath);
            }
            OkHttpUtils.Download(fromPathName, dlPath, fileName, new NormalRequestCallback() {
                @Override
                public void onRequestComplete(String result, int resultIndex) {
                    if (resultIndex >= 0) {
                        FilesManager.renameFile(dlPath, lPath);
                        threadPool.delete(fromPathName);
                    }
                }
            });
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static class DownLoadThread extends Thread {
        String tag = null;
        String fromPath = null;
        String toPath = null;
        DownLoadThread(String fromPathName, String toPath)
        {
            this.fromPath = fromPathName;
            this.toPath = toPath;
        }
        @Override
        public void run() {
            super.run();
            if (TextUtils.isEmpty(fromPath)) return;
            //if(TextUtils.isEmpty(toPath)) return;
            if (TextUtils.isEmpty(toPath))
                download(fromPath);
            else
                download(fromPath, toPath);
        }
    }

}
