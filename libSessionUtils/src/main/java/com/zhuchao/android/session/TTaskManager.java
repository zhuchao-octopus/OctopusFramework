package com.zhuchao.android.session;

import static com.zhuchao.android.libfileutils.FileUtils.EmptyString;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;

import com.zhuchao.android.callbackevent.HttpCallback;
import com.zhuchao.android.callbackevent.InvokeInterface;
import com.zhuchao.android.libfileutils.DataID;
import com.zhuchao.android.libfileutils.FileUtils;
import com.zhuchao.android.libfileutils.MMLog;
import com.zhuchao.android.libfileutils.ObjectList;
import com.zhuchao.android.libfileutils.TTask;
import com.zhuchao.android.libfileutils.TTaskThreadPool;
import com.zhuchao.android.netutil.HttpUtils;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;


public class TTaskManager {
    private final String TAG = "TTaskManager";
    private final String D_EXT_NAME = ".downloading.temp";
    private Context mContext = null;
    private TTaskThreadPool tTaskThreadPool = null;
    private boolean stopContinue = true;
    private boolean reDownload = true;

    private Handler TimerTaskHandler = new Handler(Looper.myLooper()) {
        public void handleMessage(Message msg) {
            TransactionProcessing(msg.obj);
        }
    };

    private Handler taskMainLooperHandler = new Handler(Looper.getMainLooper()) {
        public void handleMessage(Message msg) {
            TransactionProcessing(msg.obj);
        }
    };

    private synchronized void TransactionProcessing(Object obj)
    {
        TTask tTask = (TTask) (obj);
        if ((tTask != null) && tTask.getCallBackHandler() != null)
        {
            tTask.getCallBackHandler().onEventTask(//在主线程中调用前端回调函数更新UI
                    tTask,
                    tTask.getProperties().getInt("status")
            );
        }
    }

    public TTaskManager(Context context) {
        mContext = context;
        tTaskThreadPool = new TTaskThreadPool(100);
    }

    public int getTaskCount() {
        return tTaskThreadPool.getCount();
    }

    public TTask getTaskByTag(String tag) {
        return tTaskThreadPool.getTaskByTag(tag);
    }

    public TTask getTaskByName(String tag) {
        return tTaskThreadPool.getTaskByName(tag);
    }

    public void delTask(TTask tTask) {
        tTask.freeFree();
        tTaskThreadPool.delTask(tTask.getTTag());
    }
    public void deleteTask(TTask tTask) {
        tTask.freeFree();
        tTaskThreadPool.delTask(tTask.getTTag());
    }
    public List<TTask> getAllTask() {
        List<TTask> allTasks = new ArrayList();//(tTaskThreadPool.getAllObject());
        Collection<Object> objects = tTaskThreadPool.getAllObject();
        for (Object o : objects)
            allTasks.add((TTask) o);
        return allTasks;
    }

    public void free()
    {

    }
    ////////////////////////////////////////////////////////////////////////////////////////////////
    ////////////////////////////////////////////////////////////////////////////////////////////////
    //timer
    public TTask timer(long period, String tag) {
        TTask tTask = tTaskThreadPool.createTask(tag);
        tTask.invoke(new InvokeInterface() {
            @Override
            public void CALLTODO(String tag) {
                long lStartTick = System.currentTimeMillis();
                int lCounter = 0;
                while (tTask.getCallBackHandler() != null)
                {
                    if ((System.currentTimeMillis() - lStartTick) >= period) {
                        Message msg = taskMainLooperHandler.obtainMessage();
                        lCounter++;
                        msg.obj = tTask;
                        tTask.getProperties().putInt("status", lCounter);
                        TimerTaskHandler.sendMessage(msg);
                        lStartTick = System.currentTimeMillis();
                    }
                    try {
                        Thread.sleep(period);
                    } catch (InterruptedException e) {
                        //e.printStackTrace();
                    }
                }
            }
        });
        return tTask;
    }

    public TTask timerOnUIMain(long period, String tag) {
        TTask tTask = tTaskThreadPool.createTask(tag);
            tTask.invoke(new InvokeInterface() {
            @Override
            public void CALLTODO(String tag) {
                long lStartTick = System.currentTimeMillis();
                int lCounter = 0;
                while (tTask.getCallBackHandler() != null)
                {
                    if ((System.currentTimeMillis() - lStartTick) >= period) {
                        Message msg = taskMainLooperHandler.obtainMessage();
                        lCounter++;
                        msg.obj = tTask;
                        tTask.getProperties().putInt("status", lCounter);
                        taskMainLooperHandler.sendMessage(msg);
                        lStartTick = System.currentTimeMillis();
                    }
                    try {
                        Thread.sleep(period);
                    } catch (InterruptedException e) {
                        //e.printStackTrace();
                    }
                }
            }
        });
        return tTask;
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////
    //request task
    public TTask request(final String fromUrl) {
        TTask tTask = tTaskThreadPool.createTask(fromUrl);
        if(EmptyString(fromUrl)) {
            tTask.free();//释放无效的任务
            delTask(tTask);
            return tTask;
        }

        tTask.getProperties().putString("fromUrl", fromUrl);
        tTask.invoke(new InvokeInterface() {
            @Override
            public void CALLTODO(String tag) {
                HttpUtils.requestGet(tag, fromUrl, new HttpCallback() {
                    @Override
                    public void onEventHttpRequest(String tag, String fromUrl, String toUrl, long progress, long total, String result, int status) {
                        if (tTask.getCallBackHandler() != null) {
                            Message msg = taskMainLooperHandler.obtainMessage();
                            msg.obj = tTask;
                            tTask.getProperties().putString("tag", tag);
                            tTask.getProperties().putString("fromUrl", fromUrl);
                            tTask.getProperties().putString("toUrl", toUrl);
                            tTask.getProperties().putLong("progress", progress);
                            tTask.getProperties().putLong("total", total);
                            tTask.getProperties().putString("result", result);
                            tTask.getProperties().putInt("status", status);
                            taskMainLooperHandler.sendMessage(msg);
                        }
                        if (status == DataID.TASK_STATUS_ERROR) {
                            MMLog.e(TAG, "requestGet " + result);
                        }
                        tTask.free();
                    }
                });
            }
        });
        return tTask;
    }

    public TTask requestPost(final String fromUrl, ObjectList bodyParams) {
        TTask tTask = tTaskThreadPool.createTask(fromUrl);
        if(EmptyString(fromUrl)) {
            tTask.free();//释放无效的任务
            delTask(tTask);
            return tTask;
        }
        tTask.getProperties().putString("fromUrl", fromUrl);
        tTask.invoke(new InvokeInterface() {
            @Override
            public void CALLTODO(String tag) {
                HttpUtils.requestPost(tag, fromUrl, bodyParams, new HttpCallback() {
                    @Override
                    public void onEventHttpRequest(String tag, String fromUrl, String toUrl, long progress, long total, String result, int status) {
                        if (tTask.getCallBackHandler() != null) {
                            Message msg = taskMainLooperHandler.obtainMessage();
                            msg.obj = tTask;
                            tTask.getProperties().putString("tag", tag);
                            tTask.getProperties().putString("fromUrl", fromUrl);
                            tTask.getProperties().putString("toUrl", toUrl);
                            tTask.getProperties().putLong("progress", progress);
                            tTask.getProperties().putLong("total", total);
                            tTask.getProperties().putString("result", result);
                            tTask.getProperties().putInt("status", status);
                            taskMainLooperHandler.sendMessage(msg);
                        }
                        if (status == DataID.TASK_STATUS_ERROR) {
                            MMLog.e(TAG, "requestPost " + result);
                        }
                        tTask.free();
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

    //toPath 必须是绝对路劲下的目录
    public TTask dl(final String fromUrl, final String toPath) {
        TTask tTask = tTaskThreadPool.createTask(fromUrl);
        if(EmptyString(fromUrl))  {
            tTask.free();//释放无效的任务
            delTask(tTask);
            return tTask;
        }
        tTask.getProperties().putString("fromUrl", fromUrl);
        tTask.getProperties().putString("toPath", toPath);
        tTask.invoke(new InvokeInterface() {
            @Override
            public void CALLTODO(String tag) {
                download(tag, fromUrl, toPath);
            }
        });
        return tTask;
    }

    private void download(String tag, String fromUrl, String toPath) {
        MMLog.log(TAG, "download tTask.tag = " + tag);
        TTask tTask = tTaskThreadPool.getTaskByTag(tag);
        if (tTask == null) {
            MMLog.log(TAG, "download stop,get tTask failed  interrupted!!!");
            tTask.free();
            return;
        }
        String fileName = null;
        String downloadingPathFileName = null;
        String localPathFileName = null;
        if(EmptyString(toPath)) {
            fileName = FileUtils.getFileName(fromUrl);
            if(EmptyString(fileName))
                fileName = tag;
            downloadingPathFileName = FileUtils.getDownloadDir(null) + fileName + D_EXT_NAME;
            localPathFileName = FileUtils.getDownloadDir(null) + fileName;
        } else {
            String toToPath = FileUtils.getDownloadDir(toPath + "/");//确保目录被创建
            if (!FileUtils.existDirectory(toToPath))//不存在目录
            {
                MMLog.log(TAG, "download stop,create directory failed,toPath = " + toPath);
                tTask.free();
                return;
            }
            fileName = FileUtils.getFileName(fromUrl);
            if(EmptyString(fileName))
                fileName = tag;
            downloadingPathFileName = toToPath + "/" + fileName + D_EXT_NAME;
            localPathFileName = toToPath + "/" + fileName;
        }

        tTask.getProperties().putString("downloadingPathFileName", downloadingPathFileName);
        tTask.getProperties().putString("localPathFileName", localPathFileName);
        if (!this.reDownload) {
            if (FileUtils.existFile(localPathFileName)) {
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
        MMLog.log(TAG, "download file from " + fromUrl + " to " + downloadingPathFileName);
        //MMLog.log(TAG, "download file to " + downloadingPathFileName);
        try {
            HttpUtils.download(tag, fromUrl, downloadingPathFileName, new HttpCallback() {

                @Override
                public void onEventHttpRequest(String tag, String fromUrl, String toUrl, long progress, long total, String result, int status) {
                    String f1 = tTask.getProperties().getString("downloadingPathFileName");
                    String f2 = tTask.getProperties().getString("localPathFileName");
                    //String f2 = toUrl.substring(0, toUrl.length() - D_EXT_NAME.length());
                    switch (status) {
                        case DataID.TASK_STATUS_ERROR:
                            MMLog.log(TAG, "download file failed, from " + fromUrl);
                            tTask.free();//下载完成，释放任务
                            //break;
                        case DataID.TASK_STATUS_PROGRESSING:
                        case DataID.TASK_STATUS_SUCCESS:
                            if ((progress == total) && (progress > 0) && (status == DataID.TASK_STATUS_SUCCESS)) {
                                //MMLog.log(TAG, "download complete, from " + fromUrl + ", total size = " + total);
                                if (FileUtils.renameFile(f1, f2))
                                    MMLog.log(TAG, "download complete save file to " + f2 + ", total size = " + total);
                                else
                                    MMLog.log(TAG, "download save file failed " + f2 + ", total size = " + total);
                                tTask.free();//下载完成，释放任务等待模式
                            }

                            if (tTask.getCallBackHandler() != null) {
                                Message msg = taskMainLooperHandler.obtainMessage();
                                msg.obj = tTask;
                                tTask.getProperties().putString("tag", tag);
                                tTask.getProperties().putString("fromUrl", fromUrl);
                                tTask.getProperties().putString("toUrl", toUrl);
                                tTask.getProperties().putLong("progress", progress);
                                tTask.getProperties().putLong("total", total);
                                tTask.getProperties().putString("result", result);
                                tTask.getProperties().putInt("status", status);
                                taskMainLooperHandler.sendMessage(msg);
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
