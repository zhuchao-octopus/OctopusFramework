package com.zhuchao.android.netutil;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.text.TextUtils;

import com.zhuchao.android.callbackevent.CallbackFunction;
import com.zhuchao.android.callbackevent.HttpCallBack;
import com.zhuchao.android.libfileutils.FilesManager;
import com.zhuchao.android.libfileutils.MMLog;
import com.zhuchao.android.libfileutils.TTask;
import com.zhuchao.android.libfileutils.TaskThreadPool;


public class MDownloadManager {
    private final String TAG = "DownloadManager";
    private final String D_EXT_NAME = ".d.temp";
    private Context mContext = null;
    private TaskThreadPool downloadTaskPool = null;

    private Handler taskHandler = new Handler(Looper.getMainLooper()) {
        public void handleMessage(Message msg) {
            TTask tTask = (TTask) (msg.obj);
            if ((tTask !=null) && tTask.getCallBackHandler() != null)
            {
                tTask.getCallBackHandler().onRequestHandler(
                        tTask.getProperties().getString("tag"),
                        tTask.getProperties().getString("url"),
                        tTask.getProperties().getString("lrl"),
                        tTask.getProperties().getLong("progress"),
                        tTask.getProperties().getLong("total")
                );
            }
        }
    };

    public MDownloadManager(Context context) {
        mContext = context;
        downloadTaskPool = new TaskThreadPool(100);
    }

    public TTask dl(final String fromUrl, final String toPath) {
        if (TextUtils.isEmpty(fromUrl)) {
            MMLog.log(TAG, "fromUrl = " + fromUrl);
            return null;
        }
        TTask tTask = downloadTaskPool.createTask(fromUrl);
        tTask.getProperties().putString("fromUrl", fromUrl);
        tTask.getProperties().putString("toPath", toPath);

        tTask.call(new CallbackFunction() {
            @Override
            public void call(String tag) {
                download(tag, fromUrl, toPath);
            }
        });
        return tTask;
    }

    private void download(String tag, String fromUrl, String toPath) {
        MMLog.log(TAG, "download TTask.tTask.tag = " + tag);
        TTask tTask = downloadTaskPool.getTaskByTag(tag);
        if (tTask == null) {
            MMLog.log(TAG, "download stop,get tTask failed  interrupted!!!");
            tTask.free();
            return;
        }
        String fileName = null;
        String downloadingPathFileName = null;
        String localPathFileName = null;
        if (TextUtils.isEmpty(toPath)) {
            fileName = FilesManager.getFileName(fromUrl);
            if (TextUtils.isEmpty(fileName))
                fileName = tag;
            downloadingPathFileName = FilesManager.getDownloadDir(null) + fileName + D_EXT_NAME;
            localPathFileName = FilesManager.getDownloadDir(null) + fileName;
        } else {
            fileName = FilesManager.getFileName(toPath);
            if (TextUtils.isEmpty(fileName))
                fileName = FilesManager.getFileName(fromUrl);
            if (TextUtils.isEmpty(fileName))
                fileName = tag;
            downloadingPathFileName = toPath + "/" + fileName + D_EXT_NAME;
            localPathFileName = toPath + "/" + fileName;
        }
        tTask.getProperties().putString("downloadingPathFileName", downloadingPathFileName);
        tTask.getProperties().putString("localPathFileName", localPathFileName);

        if (FilesManager.isExists(localPathFileName)) {
            MMLog.log(TAG, "download stop,file already exist --> " + localPathFileName);
            tTask.free();
            return;//已经完成下载，不再重复下载
        }
        if (FilesManager.isExists(downloadingPathFileName)) { //删除之前的未下载完的临时文件
            if (FilesManager.deleteFile(downloadingPathFileName)) {
                MMLog.log(TAG, "download delete temp file successfully " + downloadingPathFileName);
            }
            else
            {
                MMLog.log(TAG, "download stop, delete exist file failed " + downloadingPathFileName);
                tTask.free();
                return;
            }
        }
        MMLog.log(TAG, "downloading file from " + fromUrl);
        MMLog.log(TAG, "downloading file to " + downloadingPathFileName);
        try {
            OkHttpUtils.Download(tag, fromUrl, downloadingPathFileName, new HttpCallBack() {
                @Override
                public void onHttpRequestProgress(String tag, String url, String lrl, long progress, long total) {
                    if (tTask.getCallBackHandler() != null) {
                        Message msg = taskHandler.obtainMessage();
                        msg.obj = tTask;
                        tTask.getProperties().putString("tag", tag);
                        tTask.getProperties().putString("url", url);
                        tTask.getProperties().putString("lrl", lrl);
                        tTask.getProperties().putLong("progress", progress);
                        tTask.getProperties().putLong("total", total);
                        taskHandler.sendMessage(msg);
                    }
                }

                @Override
                public void onHttpRequestProgress(int tag, String url, String lrl, long progress, long total) {

                }

                @Override
                public void onHttpRequestComplete(String tag, String url, String lrl, long progress, long total)
                {
                    //String f1 = tTask.getProperties().getString("downloadingPathFileName");
                    String f2 = lrl.substring(0, lrl.length() - D_EXT_NAME.length());//tTask.getProperties().getString("localPathFileName");
                    try {
                        if((progress == total) && (progress > 0))
                        {
                            MMLog.log(TAG, "download complete, from " + url);
                            if (FilesManager.renameFile(lrl, f2))
                                MMLog.log(TAG, "download save file complete, to " + f2);
                            else
                                MMLog.log(TAG, "download save file failed, to " + f2);
                        }
                        else
                        {
                            MMLog.log(TAG, "download file failed, from " + url);
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    tTask.free();//下载完成，释放任务
                }
            });
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
