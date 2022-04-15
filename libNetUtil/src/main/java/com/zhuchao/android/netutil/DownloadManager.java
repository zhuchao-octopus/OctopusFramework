package com.zhuchao.android.netutil;

import android.text.TextUtils;

import com.zhuchao.android.callbackevent.CallbackFunction;
import com.zhuchao.android.callbackevent.NormalRequestCallback;
import com.zhuchao.android.libfileutils.FilesManager;
import com.zhuchao.android.libfileutils.TTask;
import com.zhuchao.android.libfileutils.ThreadPool;

import java.util.ArrayList;
import java.util.List;


public class DownloadManager {
    private static final String TAG = "DownloadManager";
    private static ThreadPool threadPool = new ThreadPool();

    public static void d(String fromPathName, String toPath) {
        if (TextUtils.isEmpty(fromPathName)) return;
        if (!FilesManager.isExists(fromPathName)) return;
        TTask tTask = threadPool.createTask(fromPathName);
        tTask.call(new CallbackFunction() {
            @Override
            public void call() {
                if (TextUtils.isEmpty(toPath))
                    download(fromPathName);
                else
                    download(fromPathName, toPath);
            }
        });
        tTask.start();
       return ;
    }

    public static TTask dl(String fromPathName, String toPath) {
        TTask tTask = threadPool.createTask(fromPathName);
        tTask.call(new CallbackFunction() {
            @Override
            public void call() {
                if (TextUtils.isEmpty(toPath))
                    download(fromPathName);
                else
                    download(fromPathName, toPath);
            }
        });
        return tTask;
    }

    public static void download(String fromPathName) {
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
                    }
                }
            });
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void download(String fromPathName, String toPath) {
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
                    }
                }
            });
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
