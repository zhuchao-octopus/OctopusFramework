package com.zhuchao.android.session;

import android.os.Build;

import com.zhuchao.android.fbase.DataID;
import com.zhuchao.android.fbase.FileUtils;
import com.zhuchao.android.fbase.FilesFinder;
import com.zhuchao.android.fbase.MMLog;
import com.zhuchao.android.fbase.ObjectList;
import com.zhuchao.android.fbase.TTask;
import com.zhuchao.android.fbase.TTaskInterface;
import com.zhuchao.android.fbase.TTaskThreadPool;
import com.zhuchao.android.fbase.eventinterface.FileFingerCallback;
import com.zhuchao.android.fbase.eventinterface.InvokeInterface;
import com.zhuchao.android.fbase.eventinterface.TaskCallback;

import java.util.Objects;
import java.util.concurrent.locks.LockSupport;

public class SessionDirectoryCopy implements TTaskInterface, InvokeInterface {
    private final String TAG = "SessionDirectoryCopy";
    //private String SessionName;
    private int copyMethod = 0;
    private String fromPath = null;
    private String toPath = null;
    private TTask tMainTask = null;//new TTask("SessionDirectoryCopy");
    private final TTaskThreadPool tTaskThreadPool = new TTaskThreadPool(20);

    private final FilesFinder filesFinder = new FilesFinder().callBack(new FileFingerCallback() {
        @Override
        public void onFileCallback(String fileName, long size, int Index) {
            if (FileUtils.NotEmptyString(fileName)) {
                tMainTask.getProperties().putString("file", fileName);
                tMainTask.getProperties().putInt("totalCount", Index);
                tMainTask.getProperties().putLong("totalSize", filesFinder.getTotalSize());
            } else {
                tMainTask.getProperties().putString("finderStatus", "finderEnd");
                tMainTask.unPark();
            }
            if (tMainTask.getCallBackHandler() != null) tMainTask.getCallBackHandler().onEventTaskFinished(tMainTask, Index);
        }
    });

    public SessionDirectoryCopy() {
        tMainTask = new TTask("SessionDirectoryCopy");
        tMainTask.invoke(this);
    }

    public SessionDirectoryCopy(String fromFilePath, String toFilePath) {
        tMainTask = new TTask(fromFilePath + toFilePath);
        tMainTask.invoke(this);
        this.fromPath = fromFilePath;
        this.toPath = toFilePath;
    }

    public String getFromPath() {
        return fromPath;
    }

    public void setFromPath(String fromPath) {
        this.fromPath = fromPath;
    }

    public String getToPath() {
        return toPath;
    }

    public void setToPath(String toPath) {
        this.toPath = toPath;
    }

    public void setCopyMethod(int copyMethod) {
        this.copyMethod = copyMethod;
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////
    ////////////////////////////////////////////////////////////////////////////////////////////////
    @Override
    public TTask invoke(InvokeInterface callFunction) {
        return tMainTask.invoke(callFunction);
    }

    @Override
    public TTask callbackHandler(TaskCallback taskCallback) {
        return tMainTask.callbackHandler(taskCallback);
    }

    @Override
    public InvokeInterface getInvokeInterface() {
        return tMainTask.getInvokeInterface();
    }

    @Override
    public TaskCallback getCallBackHandler() {
        return tMainTask.getCallBackHandler();
    }

    @Override
    public String getTaskTag() {
        return tMainTask.getTaskTag();
    }

    @Override
    public long getTaskID() {
        return tMainTask.getTaskID();
    }

    @Override
    public void setTaskTag(String tTag) {
        tMainTask.setTaskTag(tTag);
    }

    @Override
    public String getTaskName() {
        return tMainTask.getTaskName();
    }

    @Override
    public void setTaskName(String tName) {
        tMainTask.setTaskName(tName);
    }

    @Override
    public ObjectList getProperties() {
        return tMainTask.getProperties();
    }

    @Override
    public boolean isKeeping() {
        return tMainTask.isKeeping();
    }

    @Override
    public void setKeep(boolean keeping) {
        tMainTask.setKeep(keeping);
    }

    @Override
    public void lock() {
        tMainTask.lock();
    }

    @Override
    public void unLock() {
        tMainTask.unLock();
    }

    @Override
    public int getInvokedCount() {
        return tMainTask.getInvokedCount();
    }

    @Override
    public boolean isBusy() {
        return tMainTask.isBusy();
    }

    @Override
    public boolean isWorking() {
        return isBusy();
    }

    @Override
    public void free() {
        tMainTask.free();
    }

    @Override
    public void freeFree() {
        try {
            tMainTask.freeFree();
            tTaskThreadPool.free();
            filesFinder.free();
        } finally {
        }
    }

    @Override
    public TTask reset() {
        tMainTask.reset();
        return tMainTask;
    }

    @Override
    public TTask resetAll() {
        return tMainTask.resetAll();
    }

    @Override
    public void forceResetAll() {
        tMainTask.forceResetAll();
    }

    @Override
    public void start() {
        tMainTask.start();
    }

    @Override
    public void startAgain() {
        tMainTask.startAgain();
    }

    @Override
    public void startWait() {
        tMainTask.startWait();
    }

    @Override
    public void startDelayed(long millis) {
        tMainTask.startDelayed(millis);
    }

    @Override
    public void startAgainDelayed(long millis) {
        tMainTask.startAgainDelayed(millis);
    }

    @Override
    public void unPark() {

    }

    @Override
    public void pack() {

    }

    @Override
    public TTask setPriority(int newPriority) {
        tMainTask.setPriority(newPriority);
        return tMainTask;
    }

    @Override
    public long getStartTick() {
        return tMainTask.getStartTick();
    }

    @Override
    public boolean isTimeOut(long timeOutMillis) {
        return tMainTask.isTimeOut(timeOutMillis);
    }

    @Override
    public void CALLTODO(String tag) {
        if (FileUtils.existFile(fromPath)) //文件复制
        {
            return;
        } else {//目录复制
            MMLog.i(TAG, "start fingerFromDir " + fromPath);
            filesFinder.fingerFromDir(fromPath);//搜索目录
        }
        tMainTask.pack();
        //LockSupport.park(tMainTask);
        MMLog.i(TAG, "start to copy files ...");
        startCopyDirectory();

        if (tMainTask.getCallBackHandler() != null) tMainTask.getCallBackHandler().onEventTaskFinished(tMainTask, DataID.TASK_STATUS_FINISHED_ALL);
    }

    private void startCopyDirectory() {
        String fromFile = null;
        FileUtils.MakeDirsExists(toPath);//目录不存在创建新的目录
        //FileUtils.setFilePermissions(toFilePath);//设置目录权限
        for (int i = 0; i < filesFinder.size() - 1; i++) {
            fromFile = filesFinder.get(i).toString();
            if (!FileUtils.existFile(fromFile)) continue;

            String sbs = fromFile.substring(fromPath.length());
            String tf = toPath + "/" + sbs;
            tf = tf.replace("//", "/");
            if (FileUtils.existFile(tf)) continue;

            String parentDir = FileUtils.getFilePathFromPathName(tf);
            FileUtils.MakeDirsExists(Objects.requireNonNull(parentDir));
            FileUtils.setFilePermissions(parentDir);

            while ((tTaskThreadPool.getCount() > 20)) {
                LockSupport.park();
            }
            TTask fTask = tTaskThreadPool.createTask(tf);
            fTask.getProperties().putString("fromPath", fromPath);
            fTask.getProperties().putString("fromFile", fromFile);
            fTask.getProperties().putString("toPath", toPath);
            fTask.getProperties().putString("toFile", tf);
            fTask.invoke(new InvokeInterface() {
                @Override
                public void CALLTODO(String tag) {
                    boolean bRet = false;
                    String ff = fTask.getProperties().getString("fromFile");
                    String tf = fTask.getProperties().getString("toFile");
                    fTask.getProperties().putLong("startTick", System.currentTimeMillis());
                    if (copyMethod == 1) {
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) bRet = FileUtils.pathCopy(ff, tf);
                    } else if (copyMethod == 2) bRet = FileUtils.bufferCopyFile(ff, tf);
                    else if (copyMethod == 3) bRet = FileUtils.streamCopy(ff, tf);
                    else bRet = FileUtils.channelTransferTo(ff, tf);

                    if (!bRet) {
                        MMLog.log(TAG, "tTaskCopyFile copy file failed -->" + ff + " to " + tf);
                        FileUtils.deleteFile(tf);
                    }
                    MMLog.log(TAG, "tTaskCopyFile copy file successfully -->" + ff + " to " + tf);
                    fTask.getProperties().putLong("endTick", System.currentTimeMillis());
                }
            });
            fTask.callbackHandler(new TaskCallback() {
                @Override
                public void onEventTaskFinished(Object obj, int status) {
                    synchronized (tTaskThreadPool) {
                        if (tMainTask.getCallBackHandler() != null) tMainTask.getCallBackHandler().onEventTaskFinished(obj, status);
                        tTaskThreadPool.deleteTask((TTask) obj);
                    }
                    ///LockSupport.unpark(tMainTask);
                }
            });
            fTask.start();
        }
    }
}
