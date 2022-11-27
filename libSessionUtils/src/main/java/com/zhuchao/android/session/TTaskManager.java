package com.zhuchao.android.session;

import static com.zhuchao.android.fileutils.FileUtils.EmptyString;
import static com.zhuchao.android.fileutils.FileUtils.NotEmptyString;
import static java.lang.Thread.MAX_PRIORITY;

import android.content.Context;
import android.os.Build;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;

import androidx.annotation.RequiresApi;

import com.zhuchao.android.callbackevent.HttpCallback;
import com.zhuchao.android.callbackevent.InvokeInterface;
import com.zhuchao.android.callbackevent.NormalCallback;
import com.zhuchao.android.callbackevent.TaskCallback;
import com.zhuchao.android.fileutils.DataID;
import com.zhuchao.android.fileutils.FileUtils;
import com.zhuchao.android.fileutils.FilesFinger;
import com.zhuchao.android.fileutils.MMLog;
import com.zhuchao.android.fileutils.ObjectList;
import com.zhuchao.android.fileutils.TTask;
import com.zhuchao.android.fileutils.TTaskThreadPool;
import com.zhuchao.android.net.HttpUtils;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.locks.LockSupport;


public class TTaskManager {
    private final String TAG = "TTaskManager";
    private final String D_EXT_NAME = ".downloading.temp";
    private Context mContext = null;
    private TTaskThreadPool tTaskThreadPool = null;
    //private boolean stopContinue = true;
    //private boolean reDownload = true;
    ////////////////////////////////////////////////////////////////////////////////////////////////
    ////////////////////////////////////////////////////////////////////////////////////////////////
    private final Handler TimerTaskHandler = new Handler(Looper.myLooper()) {
        public void handleMessage(Message msg) {
            TransactionProcessing(msg.obj);
        }
    };

    private final Handler taskMainLooperHandler = new Handler(Looper.getMainLooper()) {
        public void handleMessage(Message msg) {
            TransactionProcessing(msg.obj);
        }
    };

    private synchronized void TransactionProcessing(Object obj) {
        TTask tTask = (TTask) (obj);
        if ((tTask != null) && tTask.getCallBackHandler() != null) {
            //在UI主线程中回调单个线程任务完成后处理方法
            tTask.getCallBackHandler().onEventTask(//在主线程中调用前端回调函数更新UI
                    tTask,
                    tTask.getProperties().getInt("status")
            );
        }
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////
    ////////////////////////////////////////////////////////////////////////////////////////////////
    public TTaskManager(Context context) {
        mContext = context;
        tTaskThreadPool = new TTaskThreadPool(100);
    }

    public int getTaskCount() {
        return tTaskThreadPool.getCount();
    }

    public TTask getTaskByName(String tName) {
        return tTaskThreadPool.getTaskByName(tName);
    }

    public TTask getTaskByTag(String tag) {
        return tTaskThreadPool.getTaskByTag(tag);
    }

    public TTask getIdleTask()
    {
        Collection<Object> objects = tTaskThreadPool.getAllObject();
        for (Object o : objects) {
            TTask tTask = ((TTask) o);
            if(!tTask.isBusy()) return tTask;
        }
        return null;
    }

    public TTask getNewTask(String tName) {
        if (existsTask(tName))
            return tTaskThreadPool.createTask(tName+System.currentTimeMillis());
        else
            return tTaskThreadPool.createTask(tName);
    }

    public TTask getAvailableTask(String tName) {
        TTask tTask = getIdleTask();
        if(tTask != null) {
            tTask.resetAll();
            return tTask;
        }
        else
           return getNewTask(tName);
    }

    public TTask getSingleTaskFor(String tName) {
        TTask tTask = getTaskByName(tName);
        if(tTask == null) {
            return tTaskThreadPool.createTask(tName);
        }
        return tTask;
    }

    public boolean existsTask(String tName) {
        if (getTaskByName(tName) != null)
            return true;
        else
            return false;
    }

    public void deleteTask(TTask tTask) {
        tTask.freeFree();
        tTaskThreadPool.deleteTask(tTask.getTTag());
    }

    public List<TTask> getAllTask() {
        List<TTask> allTasks = new ArrayList<>();//(tTaskThreadPool.getAllObject());
        Collection<Object> objects = tTaskThreadPool.getAllObject();
        for (Object o : objects)
            allTasks.add((TTask) o);
        return allTasks;
    }

    public void free() {
        tTaskThreadPool.free();
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////
    ////////////////////////////////////////////////////////////////////////////////////////////////
    //copy files
    public TTask copyDirectory(String fromPath, String toPath) {
        //final int maxTaskCount = tTaskThreadPool.getCount() + tCount;
        TTask tTask = tTaskThreadPool.createTask(fromPath);
        tTask.getProperties().putString("fromPath", fromPath);
        tTask.getProperties().putString("toPath", toPath);
        tTask.getProperties().putLong("startTime", System.currentTimeMillis());
        tTask.getProperties().putInt("status", DataID.TASK_STATUS_PROGRESSING);

        ConcurrentLinkedQueue concurrentLinkedQueue = new ConcurrentLinkedQueue();

        if (!FileUtils.existDirectory(fromPath)) {
            MMLog.log(TAG, "do not exists fromPath " + fromPath);
            return tTask;
        }

        FilesFinger filesFinger = new FilesFinger();
        filesFinger.callBack(new NormalCallback() {
            @Override
            public void onEventRequest(String Result, int Index) {
                tTask.getProperties().putString("fromFile", Result);
                tTask.getProperties().putInt("totalCount", Index);
                tTask.getProperties().putLong("totalSize", filesFinger.getTotalSize());
                long tick = System.currentTimeMillis() - tTask.getProperties().getLong("startTime");
                tTask.getProperties().putLong("takeUpTime", tick);
                Message msg = taskMainLooperHandler.obtainMessage();
                msg.obj = tTask;
                taskMainLooperHandler.sendMessage(msg);

                if (Result.startsWith("End")) {
                    tTask.getProperties().putString("fingerStatus", "fingerEnd");
                    LockSupport.unpark(tTask);
                } else {
                    concurrentLinkedQueue.add(Result);
                }
            }
        });


        FileUtils.CheckDirsExists(toPath);
        FileUtils.setFilePermissions2(toPath);
        tTask.invoke(new InvokeInterface() {
            @RequiresApi(api = Build.VERSION_CODES.O)
            @Override
            public void CALLTODO(String tag) {
                //MMLog.log(TAG, "tTask start copyDirectory CALLTODO");
                int taskCount = 0;
                if (filesFinger.getCount() <= 0) {
                    //filesFinger.setMultiThread(false);
                    filesFinger.fingerFromDir(fromPath);
                    //MMLog.log(TAG, "tTask finger files waiting... " + fromPath);
                    if (tTask.getProperties().getBoolean("fingerFirst", true))
                        LockSupport.park(tTask);
                }
                //MMLog.log(TAG, "tTask start copying files...");
                tTask.getProperties().putInt("copiedCount", 0);
                //Collection<Object> objects = filesFinger.getAllObject();
                while (true)
                //for (Object o : objects)
                {
                    if ("fingerEnd".equals(tTask.getProperties().getString("fingerStatus"))) {
                        if (tTask.getProperties().getInt("copiedCount") >= filesFinger.getCount())
                            break;//等待任务完成
                    }
                    //String fromFile = ((File) o).getAbsolutePath();
                    Object o = concurrentLinkedQueue.poll();
                    if (o == null) {
                        if ("fingerEnd".equals(tTask.getProperties().getString("fingerStatus")))
                            break;
                        continue;
                    }

                    String fromFile = o.toString();
                    String fp = tTask.getProperties().getString("fromPath");
                    String ff = fromFile;
                    String tp = tTask.getProperties().getString("toPath");
                    String sbs = ff.substring(fp.length());
                    String tf = tp + "/" + sbs;
                    tf = tf.replace("//", "/");

                    if (!FileUtils.existFile(tf) && FileUtils.existFile(ff)) {
                        //校验目录 中间存在目录 创建多级目录
                        String parentDir = FileUtils.getFilePathFromPathName(tf);
                        FileUtils.CheckDirsExists(Objects.requireNonNull(parentDir));
                        FileUtils.setFilePermissions2(parentDir);
                        //MMLog.log(TAG,"copy file from "+ff +" to "+tf);
                        tTask.getProperties().putString("toFile", tf);
                        boolean bRet = false;
                        int copyMethod = tTask.getProperties().getInt("copyMethod", 0);
                        if (copyMethod == 1) {
                            long tickCount = System.currentTimeMillis();
                            bRet = FileUtils.pathCopy(ff, tf);
                            MMLog.log(TAG, "pathCopy take up time: " + (System.currentTimeMillis() - tickCount) + " / " + FileUtils.getFileSize(tf));
                        } else if (copyMethod == 2)
                            bRet = FileUtils.bufferCopyFile(ff, tf);
                        else if (copyMethod == 3)
                            bRet = FileUtils.streamCopy(ff, tf);
                        else {
                            long tickCount = System.currentTimeMillis();
                            //MMLog.log(TAG,"channelTransferTo take up time: "+ tickCount);
                            bRet = FileUtils.channelTransferTo(ff, tf);
                            MMLog.log(TAG, "channelTransferTo take up time: " + (System.currentTimeMillis() - tickCount));
                            //MMLog.log(TAG,"channelTransferTo take up time2: "+(System.currentTimeMillis() - tTask.getProperties().getLong("startTime")));
                        }

                        if (bRet) {
                            FileUtils.setFilePermissions2(tf);
                        } else {
                            MMLog.log(TAG, "tTaskCopyFile copy file failed -->" + ff + " to " + tf);
                            FileUtils.deleteFile(tf);
                        }
                    }

                    int CCount = tTask.getProperties().getInt("copiedCount", 0);
                    ++CCount;
                    tTask.getProperties().putInt("copiedCount", CCount);
                    long tick = System.currentTimeMillis() - tTask.getProperties().getLong("startTime");
                    tTask.getProperties().putLong("takeUpTime", tick);

                    Message msg = taskMainLooperHandler.obtainMessage();
                    msg.obj = tTask;
                    taskMainLooperHandler.sendMessage(msg);
                }//for (Object o : objects)

                /////////////////////////////////////////////////////////////////////////////////////
                //完成
                tTask.getProperties().putLong("endTime", System.currentTimeMillis());
                long tick = System.currentTimeMillis() - tTask.getProperties().getLong("startTime");
                tTask.getProperties().putLong("takeUpTime", tick);

                Message msg = taskMainLooperHandler.obtainMessage();
                msg.obj = tTask;
                tTask.getProperties().putInt("status", DataID.TASK_STATUS_FINISHED_STOP);
                taskMainLooperHandler.sendMessage(msg);
                tTask.free();
                filesFinger.free();
                //MMLog.log(TAG, "");
            }
        });
        return tTask;
    }

    public TTask copyDirectory(String fromPath, String toPath, int tCount) {
        final int maxTaskCount = tTaskThreadPool.getCount() + tCount;
        TTask tTask = tTaskThreadPool.createTask(fromPath);
        tTask.getProperties().putString("fromPath", fromPath);
        tTask.getProperties().putString("toPath", toPath);
        tTask.getProperties().putLong("startTime", System.currentTimeMillis());
        tTask.getProperties().putInt("status", DataID.TASK_STATUS_PROGRESSING);
        ConcurrentLinkedQueue concurrentLinkedQueue = new ConcurrentLinkedQueue();

        FilesFinger filesFinger = new FilesFinger();
        filesFinger.callBack(new NormalCallback() {
            @Override
            public void onEventRequest(String Result, int Index) {
                tTask.getProperties().putString("fromFile", Result);
                tTask.getProperties().putInt("totalCount", Index);
                tTask.getProperties().putLong("totalSize", filesFinger.getTotalSize());
                long tick = System.currentTimeMillis() - tTask.getProperties().getLong("startTime");
                tTask.getProperties().putLong("takeUpTime", tick);
                Message msg = taskMainLooperHandler.obtainMessage();
                msg.obj = tTask;
                taskMainLooperHandler.sendMessage(msg);

                if (Result.startsWith("End")) {
                    tTask.getProperties().putString("fingerStatus", "fingerEnd");
                    LockSupport.unpark(tTask);
                } else {
                    concurrentLinkedQueue.add(Result);
                }
            }
        });

        if (!FileUtils.existDirectory(fromPath)) {
            MMLog.log(TAG, "do not exists fromPath " + fromPath);
            return tTask;
        }
        FileUtils.CheckDirsExists(toPath);
        FileUtils.setFilePermissions2(toPath);
        tTask.invoke(new InvokeInterface() {
            @Override
            public void CALLTODO(String tag) {
                //MMLog.log(TAG, "tTask start copyDirectory CALLTODO");
                int taskCount = 0;
                if (filesFinger.getCount() <= 0) {
                    //filesFinger.setMultiThread(false);
                    filesFinger.fingerFromDir(fromPath);
                    //MMLog.log(TAG, "tTask finger files waiting... " + fromPath);
                    if (tTask.getProperties().getBoolean("fingerFirst", true))
                        LockSupport.park(tTask);
                }
                //MMLog.log(TAG, "tTask start copying files...");
                tTask.getProperties().putInt("copiedCount", 0);
                //Collection<Object> objects = filesFinger.getAllObject();
                while (true)
                //for (Object o : objects)
                {
                    if ("fingerEnd".equals(tTask.getProperties().getString("fingerStatus"))) {
                        if (tTask.getProperties().getInt("copiedCount") >= filesFinger.getCount())
                            break;//等待任务完成
                    }
                    //String fromFile = ((File) o).getAbsolutePath();
                    Object o = concurrentLinkedQueue.poll();
                    if (o == null) {
                        if ("fingerEnd".equals(tTask.getProperties().getString("fingerStatus")))
                            break;
                        continue;
                    }

                    if (tTaskThreadPool.getCount() > maxTaskCount) {
                        MMLog.log(TAG, "tTaskThreadPool.getCount() > " + maxTaskCount + " waiting...");
                        LockSupport.park(tTask);
                        MMLog.log(TAG, "tTaskThreadPool continue to...");
                    }

                    String fromFile = o.toString();
                    //MMLog.log(TAG,"fromName = "+fromFile);
                    //String fromName = FileUtils.getFileName(fromFile);
                    //if(!FileUtils.existFile(fromFile)) {
                    //continue;
                    //    MMLog.log(TAG,"do not exist fromFile task stop " + fromFile);
                    //    break;
                    //}
                    TTask tTaskCopyFile = tTaskThreadPool.createTask(fromFile);
                    tTaskCopyFile.getProperties().putString("fromFile", fromFile);
                    //tTaskCopyFile.getProperties().putString("fromName", fromName);
                    tTaskCopyFile.getProperties().putString("fromPath", tTask.getProperties().getString("fromPath"));
                    tTaskCopyFile.getProperties().putString("toPath", tTask.getProperties().getString("toPath"));
                    taskCount++;
                    tTaskCopyFile.setPriority(MAX_PRIORITY);
                    tTaskCopyFile.invoke(new InvokeInterface() {
                        @RequiresApi(api = Build.VERSION_CODES.O)
                        @Override
                        public void CALLTODO(String tag) {
                            boolean bRet = false;
                            android.os.Process.setThreadPriority(android.os.Process.THREAD_PRIORITY_BACKGROUND);

                            String fp = tTaskCopyFile.getProperties().getString("fromPath");
                            String ff = tTaskCopyFile.getProperties().getString("fromFile");
                            String tp = tTaskCopyFile.getProperties().getString("toPath");
                            //String fn = tTaskCopyFile.getProperties().getString("fromName");
                            String sbs = ff.substring(fp.length());
                            //String sbd = sbs.substring(0, sbs.indexOf(fn) - 1);
                            String tf = tp + "/" + sbs;
                            tf = tf.replace("//", "/");
                            //if(!FileUtils.existFile(ff))
                            if (!FileUtils.existFile(tf) && FileUtils.existFile(ff)) {
                                //校验目录 中间存在目录 创建多级目录
                                String parentDir = FileUtils.getFilePathFromPathName(tf);
                                FileUtils.CheckDirsExists(Objects.requireNonNull(parentDir));
                                FileUtils.setFilePermissions2(parentDir);
                                //MMLog.log(TAG,"copy file from "+ff +" to "+tf);

                                int copyMethod = tTask.getProperties().getInt("copyMethod", 0);
                                if (copyMethod == 1)
                                    bRet = FileUtils.pathCopy(ff, tf);
                                else if (copyMethod == 2)
                                    bRet = FileUtils.bufferCopyFile(ff, tf);
                                else if (copyMethod == 3)
                                    bRet = FileUtils.streamCopy(ff, tf);
                                else {
                                    //long tickCount = System.currentTimeMillis();
                                    //MMLog.log(TAG,"channelTransferTo take up time: "+ tickCount);
                                    bRet = FileUtils.channelTransferTo(ff, tf);
                                    //MMLog.log(TAG,"channelTransferTo take up time: "+(System.currentTimeMillis() - tickCount));
                                    //MMLog.log(TAG,"channelTransferTo take up time2: "+(System.currentTimeMillis() - tTask.getProperties().getLong("startTime")));
                                }

                                if (bRet) {
                                    FileUtils.setFilePermissions2(tf);
                                } else {
                                    MMLog.log(TAG, "tTaskCopyFile copy file failed -->" + ff + " to " + tf);
                                    FileUtils.deleteFile(tf);
                                }
                            }
                            synchronized (tTask) {
                                int CCount = tTask.getProperties().getInt("copiedCount", 0);
                                ++CCount;
                                tTask.getProperties().putInt("copiedCount", CCount);
                            }
                            tTask.getProperties().putString("toFile", tf);
                            Message msg = taskMainLooperHandler.obtainMessage();
                            msg.obj = tTask;
                            taskMainLooperHandler.sendMessage(msg);
                            //////////////////////////////////////////////////////////////////////////
                            TTask tto = tTaskThreadPool.getTaskByTag(tag);
                            tTaskThreadPool.deleteTask(tto.getTTag());
                            tto.free();
                            //////////////////////////////////////////////////////////////////////////
                            if (tTaskThreadPool.getCount() < maxTaskCount) {
                                LockSupport.unpark(tTask);
                            }
                            //////////////////////////////////////////////////////////////////////////
                        }
                    }).start();

                    long tick = System.currentTimeMillis() - tTask.getProperties().getLong("startTime");
                    tTask.getProperties().putLong("takeUpTime", tick);
                }//for (Object o : objects)

                while (tTask.getProperties().getInt("copiedCount") < taskCount)//等待所有任务完成返回
                {
                    try {
                        Thread.sleep(1000);
                    } catch (InterruptedException e) {
                        //e.printStackTrace();
                    }
                }
                /////////////////////////////////////////////////////////////////////////////////////
                //完成
                tTask.getProperties().putLong("endTime", System.currentTimeMillis());
                long tick = System.currentTimeMillis() - tTask.getProperties().getLong("startTime");
                tTask.getProperties().putLong("takeUpTime", tick);

                Message msg = taskMainLooperHandler.obtainMessage();
                msg.obj = tTask;
                tTask.getProperties().putInt("status", DataID.TASK_STATUS_FINISHED_STOP);
                taskMainLooperHandler.sendMessage(msg);
                tTask.free();
                filesFinger.free();
                //MMLog.log(TAG, "");
            }
        });
        return tTask;
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
                while (tTask.getCallBackHandler() != null) {
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
                while (tTask.getCallBackHandler() != null) {
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
    ////////////////////////////////////////////////////////////////////////////////////////////////
    //net request task
    public TTask request(final String fromUrl) {
        TTask tTask = tTaskThreadPool.createTask(fromUrl);
        if (EmptyString(fromUrl)) {
            tTask.free();//释放无效的任务
            deleteTask(tTask);
            return tTask;
        }
        if (tTask.isBusy()) return tTask;
        tTask.getProperties().putString("fromUrl", fromUrl);
        tTask.invoke(new InvokeInterface() {
            @Override
            public void CALLTODO(String tag) {
                HttpUtils.requestGet(tag, fromUrl, new HttpCallback() {
                    @Override
                    public void onEventHttpRequest(String tag, String fromUrl, String toUrl, long progress, long total, String result, int status) {
                        if (status == DataID.TASK_STATUS_ERROR) {
                            MMLog.e(TAG, "requestGet " + fromUrl + "," + result);
                        }

                        if (NotEmptyString(fromUrl) && fromUrl.contains("TestJSON")) {
                            testRequest(tTask.getProperties().getString("TestJSON"));
                        }

                        if (tTask.getCallBackHandler() != null) {//回调传递给task
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
                            //taskMainLooperHandler.sendMessage(msg)后
                            //tTask.getProperties().getString("fromUrl")有可能被清空
                            tTask.free();//释放线程tTask.run
                        } else {
                            deleteTask(tTask);//没有回调任务，直接清除tTask
                        }
                    }
                });
            }
        });
        return tTask;
    }

    public TTask requestPost(final String fromUrl, ObjectList bodyParams) {
        TTask tTask = tTaskThreadPool.createTask(fromUrl);
        if (EmptyString(fromUrl)) {
            tTask.free();//释放无效的任务
            deleteTask(tTask);
            return tTask;
        }
        if (tTask.isBusy()) return tTask;
        tTask.getProperties().putString("fromUrl", fromUrl);
        tTask.invoke(new InvokeInterface() {
            @Override
            public void CALLTODO(String tag) {
                HttpUtils.requestPost(tag, fromUrl, bodyParams, new HttpCallback() {
                    @Override
                    public void onEventHttpRequest(String tag, String fromUrl, String toUrl, long progress, long total, String result, int status) {
                        if (status == DataID.TASK_STATUS_ERROR) {
                            MMLog.e(TAG, "requestPost " + fromUrl + "," + result);
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
                            tTask.free();
                        } else {
                            deleteTask(tTask);//没有回调任务，直接清除tTask
                        }
                    }
                });
            }
        });
        return tTask;
    }

    public TTask requestPut(String fromUrl, String bodyJSOSParams) {
        TTask tTask = tTaskThreadPool.createTask(fromUrl);
        if (EmptyString(fromUrl)) {
            tTask.free();//释放无效的任务
            deleteTask(tTask);
            return tTask;
        }
        if (tTask.isBusy()) return tTask;

        tTask.getProperties().putString("fromUrl", fromUrl);
        tTask.invoke(new InvokeInterface() {
            @Override
            public void CALLTODO(String tag) {
                HttpUtils.requestPut(tag, fromUrl, bodyJSOSParams, new HttpCallback() {
                    @Override
                    public void onEventHttpRequest(String tag, String fromUrl, String toUrl, long progress, long total, String result, int status) {
                        if (status == DataID.TASK_STATUS_ERROR) {
                            MMLog.e(TAG, "requestPut " + fromUrl + "," + result);
                        }
                        if (tTask.getCallBackHandler() != null) {//记得回调传递给task
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
                            //taskMainLooperHandler.sendMessage(msg)后
                            //tTask.getProperties().getString("fromUrl")有可能被清空
                            tTask.free();//释放线程tTask.run
                        } else {
                            deleteTask(tTask);//没有回调任务，直接清除tTask
                        }
                    }
                });
            }
        });
        return tTask;
    }

    public void testRequest(String jsonStr) {
        //String fromUrl = DataID.SESSION_SOURCE_TEST_URL;
        //MMLog.i(TAG,"testRequest:"+jsonStr);
        requestPut(DataID.SESSION_SOURCE_TEST_URL, jsonStr).callbackHandler(new TaskCallback() {
            @Override
            public void onEventTask(Object obj, int status) {
                if (status == DataID.TASK_STATUS_ERROR) {
                    //deleteTask((TTask)obj);
                    ((TTask) obj).reset();
                }
                //MMLog.i(TAG, status + "," + ((TTask) obj).getProperties().getString("fromUrl"));
                //MMLog.i(TAG, status + "," + ((TTask) obj).getProperties().getString("result", "null"));
            }
        }).start();
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////
    //download task
    //public void setStopContinue(boolean stopContinue) {
    //    this.stopContinue = stopContinue;
    //}

    //public void setReDownload(boolean reDownload) {
    //    this.reDownload = reDownload;
    //}

    //toPath 必须是绝对路劲下的目录
    public TTask dl(final String fromUrl, final String toPath) {
        TTask tTask = tTaskThreadPool.createTask(fromUrl);
        if (EmptyString(fromUrl)) {
            tTask.free();//释放无效的任务
            deleteTask(tTask);
            return tTask;
        }
        tTask.getProperties().putString("fromUrl", fromUrl);
        tTask.getProperties().putString("toPath", toPath);
        tTask.invoke(new InvokeInterface() {
            @Override
            public void CALLTODO(String tag) {
                download(tag, fromUrl, toPath, false, true);
            }
        });
        return tTask;
    }

    public TTask dl(final String fromUrl, final String toPath, boolean reDownload) {
        TTask tTask = tTaskThreadPool.createTask(fromUrl);
        if (EmptyString(fromUrl)) {
            tTask.free();//释放无效的任务
            deleteTask(tTask);
            return tTask;
        }
        tTask.getProperties().putString("fromUrl", fromUrl);
        tTask.getProperties().putString("toPath", toPath);
        tTask.invoke(new InvokeInterface() {
            @Override
            public void CALLTODO(String tag) {
                download(tag, fromUrl, toPath, reDownload, true);
            }
        });
        return tTask;
    }

    public TTask dl(final String fromUrl, final String toPath, boolean reDownload, boolean stopContinue) {
        TTask tTask = tTaskThreadPool.createTask(fromUrl);
        if (EmptyString(fromUrl)) {
            tTask.free();//释放无效的任务
            deleteTask(tTask);
            return tTask;
        }
        tTask.getProperties().putString("fromUrl", fromUrl);
        tTask.getProperties().putString("toPath", toPath);
        tTask.invoke(new InvokeInterface() {
            @Override
            public void CALLTODO(String tag) {
                download(tag, fromUrl, toPath, reDownload, stopContinue);
            }
        });
        return tTask;
    }

    //关联线程池中的某个线程
    private void download(String tag, String fromUrl, String toPath, boolean reDownload, boolean stopContinue) {
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
        if (EmptyString(toPath)) {
            fileName = FileUtils.getFileName(fromUrl);
            if (EmptyString(fileName))
                fileName = tag;
            downloadingPathFileName = FileUtils.getDownloadDir(null) + fileName + D_EXT_NAME;
            localPathFileName = FileUtils.getDownloadDir(null) + fileName;
        } else {
            String toToPath = FileUtils.getDownloadDir(toPath);//确保目录被创建
            if (!FileUtils.existDirectory(toToPath))//不存在目录
            {
                MMLog.log(TAG, "download stop,create directory failed,toPath = " + toPath);
                tTask.free();
                return;
            }
            fileName = FileUtils.getFileName(fromUrl);
            if (EmptyString(fileName))
                fileName = tag;
            downloadingPathFileName = toToPath + "/" + fileName + D_EXT_NAME;
            localPathFileName = toToPath + "/" + fileName;
        }

        tTask.getProperties().putString("downloadingPathFileName", downloadingPathFileName);
        tTask.getProperties().putString("localPathFileName", localPathFileName);
        if (!reDownload) {
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
        if (!stopContinue) {//非断点续传模式下，删除之前的未下载完的临时文件,重新完成下载
            if (FileUtils.deleteFile(downloadingPathFileName)) {
                MMLog.log(TAG, "download delete temp file successfully " + downloadingPathFileName);
            } else {
                MMLog.log(TAG, "download stop, delete exist file failed " + downloadingPathFileName);
                tTask.free();
                return;
            }
        }
        MMLog.log(TAG, "download file from " + fromUrl + " to " + downloadingPathFileName);
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
                            tTask.free();//下载出错，释放任务
                            //break;下载错误也执行下面代码
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
            MMLog.e(TAG, "download() " + e.getMessage());//e.printStackTrace();
        }
    }
}
