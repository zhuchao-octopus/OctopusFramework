package com.zhuchao.android.session;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.text.TextUtils;

import com.zhuchao.android.callbackevent.InvokeFunction;
import com.zhuchao.android.callbackevent.HttpCallback;
import com.zhuchao.android.libfileutils.DataID;
import com.zhuchao.android.libfileutils.FileUtils;
import com.zhuchao.android.libfileutils.TTask;
import com.zhuchao.android.libfileutils.TTaskThreadPool;
import com.zhuchao.android.netutil.HttpUtils;
import com.zhuchao.android.utils.MMLog;


public class TTaskManager {
    private final String TAG = "TTaskManager";
    private final String D_EXT_NAME = ".downloading.temp";
    private Context mContext = null;
    private TTaskThreadPool tTaskThreadPool = null;
    private boolean stopContinue = true;
    private boolean reDownload = true;
    private Handler taskHandler = new Handler(Looper.getMainLooper()) {
        public void handleMessage(Message msg) {
            TTask tTask = (TTask) (msg.obj);
            if ((tTask != null) && tTask.getCallBackHandler() != null) {
                tTask.getCallBackHandler().onEventTask(//调用前端回调函数更新UI
                        tTask,
                        tTask.getProperties().getInt("status")
                );
            }
        }
    };

    public TTaskManager(Context context) {
        mContext = context;
        tTaskThreadPool = new TTaskThreadPool(100);
    }

    public int getTaskCount() {
        return tTaskThreadPool.getCount();
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////
    //request task
    public TTask requestPost(final String fromUrl, String bodyJson) {
        if (TextUtils.isEmpty(fromUrl)) {
            MMLog.log(TAG, "fromUrl = " + fromUrl);
            return null;
        }
        TTask tTask = tTaskThreadPool.createTask(fromUrl);
        tTask.getProperties().putString("fromUrl", fromUrl);
        tTask.call(new InvokeFunction() {
            @Override
            public void call(String tag) {
                HttpUtils.requestPost(tag, fromUrl, bodyJson, new HttpCallback() {
                    @Override
                    public void onEventHttpRequest(String tag, String fromUrl, String toUrl, long progress, long total, String result, int status) {
                        if (tTask.getCallBackHandler() != null) {
                            Message msg = taskHandler.obtainMessage();
                            msg.obj = tTask;
                            tTask.getProperties().putString("tag", tag);
                            tTask.getProperties().putString("fromUrl", fromUrl);
                            tTask.getProperties().putString("toUrl", toUrl);
                            tTask.getProperties().putLong("progress", progress);
                            tTask.getProperties().putLong("total", total);
                            tTask.getProperties().putString("result", result);
                            tTask.getProperties().putInt("status", status);
                            taskHandler.sendMessage(msg);
                        }
                    }
                });
            }
        });
        return tTask;
    }

    public TTask request(final String fromUrl) {
        if (TextUtils.isEmpty(fromUrl)) {
            MMLog.log(TAG, "fromUrl = " + fromUrl);
            return null;
        }
        TTask tTask = tTaskThreadPool.createTask(fromUrl);
        tTask.getProperties().putString("fromUrl", fromUrl);
        tTask.call(new InvokeFunction() {
            @Override
            public void call(String tag) {
                HttpUtils.request(tag, fromUrl, new HttpCallback() {

                    @Override
                    public void onEventHttpRequest(String tag, String fromUrl, String toUrl, long progress, long total, String result, int status) {
                        if (tTask.getCallBackHandler() != null) {
                            Message msg = taskHandler.obtainMessage();
                            msg.obj = tTask;
                            tTask.getProperties().putString("tag", tag);
                            tTask.getProperties().putString("fromUrl", fromUrl);
                            tTask.getProperties().putString("toUrl", toUrl);
                            tTask.getProperties().putLong("progress", progress);
                            tTask.getProperties().putLong("total", total);
                            tTask.getProperties().putString("result", result);
                            tTask.getProperties().putInt("status", status);
                            taskHandler.sendMessage(msg);
                        }
                    }
                });
            }
        });
        return tTask;
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////
    //download task
    public void setStopContinue(boolean stopContinue) {
        this.stopContinue = stopContinue;
    }

    public void setReDownload(boolean reDownload) {
        this.reDownload = reDownload;
    }

    public TTask dl(final String fromUrl, final String toPath) {
        if (TextUtils.isEmpty(fromUrl)) {
            MMLog.log(TAG, "fromUrl = " + fromUrl);
            return null;
        }
        TTask tTask = tTaskThreadPool.createTask(fromUrl);
        tTask.getProperties().putString("fromUrl", fromUrl);
        tTask.getProperties().putString("toPath", toPath);

        tTask.call(new InvokeFunction() {
            @Override
            public void call(String tag) {
                download(tag, fromUrl, toPath);
            }
        });
        return tTask;
    }

    private void download(String tag, String fromUrl, String toPath) {
        MMLog.log(TAG, "download TTask.tTask.tag = " + tag);
        TTask tTask = tTaskThreadPool.getTaskByTag(tag);
        if (tTask == null) {
            MMLog.log(TAG, "download stop,get tTask failed  interrupted!!!");
            tTask.free();
            return;
        }
        String fileName = null;
        String downloadingPathFileName = null;
        String localPathFileName = null;
        if (TextUtils.isEmpty(toPath)) {
            fileName = FileUtils.getFileName(fromUrl);
            if (TextUtils.isEmpty(fileName))
                fileName = tag;
            downloadingPathFileName = FileUtils.getDownloadDir(null) + fileName + D_EXT_NAME;
            localPathFileName = FileUtils.getDownloadDir(null) + fileName;
        } else {
            fileName = FileUtils.getFileName(toPath);
            if (TextUtils.isEmpty(fileName))
                fileName = FileUtils.getFileName(fromUrl);
            if (TextUtils.isEmpty(fileName))
                fileName = tag;
            downloadingPathFileName = toPath + "/" + fileName + D_EXT_NAME;
            localPathFileName = toPath + "/" + fileName;
        }

        tTask.getProperties().putString("downloadingPathFileName", downloadingPathFileName);
        tTask.getProperties().putString("localPathFileName", localPathFileName);
        if (!this.reDownload) {
            if (FileUtils.isExists(localPathFileName)) {
                MMLog.log(TAG, "download stop,file already exist --> " + localPathFileName);
                tTask.free();
                return;//已经完成下载，不再重复下载
            }
        } else {//重新下载
            if (FileUtils.deleteFile(downloadingPathFileName)) {
                MMLog.log(TAG, "download delete exist file successfully " + localPathFileName);
            } else {
                MMLog.log(TAG, "download stop, delete exist file failed " + localPathFileName);
                tTask.free();
                return;
            }
        }
        if (!this.stopContinue) {//非断点续传模式下，删除之前的未下载完的临时文件,重新完成下载
            if (FileUtils.deleteFile(downloadingPathFileName)) {
                MMLog.log(TAG, "download delete temp file successfully " + downloadingPathFileName);
            } else {
                MMLog.log(TAG, "download stop, delete exist file failed " + downloadingPathFileName);
                tTask.free();
                return;
            }
        }//
        MMLog.log(TAG, "download file from " + fromUrl);
        MMLog.log(TAG, "download file to " + downloadingPathFileName);
        try {
            HttpUtils.download(tag, fromUrl, downloadingPathFileName, new HttpCallback() {

                @Override
                public void onEventHttpRequest(String tag, String fromUrl, String toUrl, long progress, long total, String result, int status) {
                    String f1 = tTask.getProperties().getString("downloadingPathFileName");
                    String f2 = tTask.getProperties().getString("localPathFileName");
                    //if(TextUtils.isEmpty(f2))
                    //String f2 = toUrl.substring(0, toUrl.length() - D_EXT_NAME.length());
                    switch (status) {
                        case DataID.TASK_STATUS_ERROR:
                            MMLog.log(TAG, "download file failed, from " + fromUrl);
                            tTask.free();//下载完成，释放任务
                            //break;
                        case DataID.TASK_STATUS_PROGRESSING:
                        case DataID.TASK_STATUS_SUCCESS:
                            if ((progress == total) && (progress > 0) && (status == DataID.TASK_STATUS_SUCCESS))
                            {
                                MMLog.log(TAG, "download complete, from " + fromUrl + " total size = " + total);
                                if (FileUtils.renameFile(f1, f2))
                                    MMLog.log(TAG, "download save file complete, to " + f2);
                                else
                                    MMLog.log(TAG, "download save file failed, to " + f2);
                                tTask.free();//下载完成，释放任务等待模式
                            }

                            if (tTask.getCallBackHandler() != null)
                            {
                                Message msg = taskHandler.obtainMessage();
                                msg.obj = tTask;
                                tTask.getProperties().putString("tag", tag);
                                tTask.getProperties().putString("fromUrl", fromUrl);
                                tTask.getProperties().putString("toUrl", toUrl);
                                tTask.getProperties().putLong("progress", progress);
                                tTask.getProperties().putLong("total", total);
                                tTask.getProperties().putString("result", result);
                                tTask.getProperties().putInt("status", status);
                                taskHandler.sendMessage(msg);
                            }
                            break;
                    }
                }
            });
        } catch (Exception e) {
            //e.printStackTrace();
            MMLog.e(TAG, "download() " + e.getMessage());
        }
    }
}
