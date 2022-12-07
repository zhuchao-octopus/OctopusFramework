package com.zhuchao.android.fbase;

import static com.zhuchao.android.fbase.FileUtils.EmptyString;
import static com.zhuchao.android.fbase.FileUtils.MD5;

import java.util.Collection;

public class TTaskThreadPool extends ObjectList {
    private final String TAG = "TaskThreadPool";
    private int maxThreadCount = 5000;
    private int minThreadCount = 0;
    private int taskCounter = 0;
    //private List<PTask> pTaskList_Ok = null;

    public TTaskThreadPool() {
        super();
    }

    public TTaskThreadPool(int maxThreadCount) {
        super();
        this.maxThreadCount = maxThreadCount;
    }

    public PTask createTask(String tName) {
        if (EmptyString(tName)) {
            tName = "default";
            MMLog.log(TAG, "only one default PTask name/tag ,name=" + tName);
        }

        if(this.getCount() > maxThreadCount)
        {
            MMLog.i(TAG,"the current thread count is more than maxThreadCount " +maxThreadCount);
            return null;
        }

        TTask tTask = getTaskByTag(MD5(tName));
        if (tTask != null)//存在直接返回
            return (PTask) tTask;

        PTask pTask = new PTask(tName);
        addItem(pTask.getTTag(), pTask);
        MMLog.log(TAG, "create PTask name = " + pTask.getTName() + ",tag = " + pTask.getTTag());
        return pTask;
    }

    private int getTaskCounter() {
        return taskCounter;
    }

    private void setTaskCounter(int taskCounter) {
        this.taskCounter = taskCounter;
    }

    public TTask getTaskByName(String tName) {
        if (EmptyString(tName))
            return null;
        return (TTask) getObject(MD5(tName));
    }

    public TTask getTaskByTag(String tag) {
        if (EmptyString(tag)) return null;
        return (TTask) getObject(tag);
    }

    public <T> T getObjectByName(String tName) {
        if (EmptyString(tName))
            return null;
        return (T) getObject(MD5(tName));
    }

    public boolean addTask(TTask tTask) {
        String tTag = tTask.tTag;
        if (!existObject(tTag)) {
            addItem(tTag, tTask);
            return true;
        }
        return false;
    }

    public boolean addTaskInterface(TTaskInterface tTask) {
        String tTag = tTask.getTTag();
        if (!existObject(tTag)) {
            addItem(tTag, tTask);
            return true;
        }
        return false;
    }

    public void deleteTaskInterface(TTaskInterface tTask) {
        String tTag = tTask.getTTag();
        delete(tTag);
        TTaskInterface tTaskInterface = getTaskByTag(tTag);
        if (tTaskInterface != null)
            MMLog.log(TAG, "delete task tag = " + tTag + ",invokedCount = " + tTaskInterface.getInvokedCount());
        else
            MMLog.log(TAG, "delete task tag = " + tTag);
    }

    public void deleteTask(String tag) {
        delete(tag);
        TTask tTask = getTaskByTag(tag);
        if (tTask != null)
            MMLog.log(TAG, "delete task tag = " + tag + ",invokedCount = " + tTask.invokedCount);
        else
            MMLog.log(TAG, "delete task tag = " + tag);
    }

    public void deleteTask(TTask tTask) {
        if (tTask == null) return;
        delete(tTask.getTTag());
        MMLog.log(TAG, "delete task tag = " + tTask.getTTag() + ",invokedCount = " + tTask.invokedCount);
    }

    public void free() {
        Collection<Object> objects = getAllObject();
        for (Object o : objects) {
            ((PTask) o).freeFree();
        }
    }

    class PTask extends TTask {
        private final String TAG = "PTask";

        //private TTaskThreadPool TTaskThreadPool = null;
        public PTask(String tName) {
            super(tName, null);
        }

        @Override
        public void run() {
            if (existObject(tTag)) {
                //MMLog.log(TAG, "invoke TTask demon tTag = " + tTag);
                super.run(); //执行父类 TTask
                /*//最终结束任务，从任务池中清除掉
                //TTaskThreadPool.delete(this.tTag);结束不清除，等待
                //MMLog.log(TAG, "PTask complete successfully,remove from task pool tag = " + tTag);
                */
                //isKeeping == false 后到这里
                setTaskCounter(getTaskCounter() + 1);
                MMLog.log(TAG, "PTask complete successfully,tag = " + tTag + ",total:" + getCount() + ",complete:" + taskCounter);
                if (getTaskCounter() == getCount()) {
                    if (taskCallback != null) {
                        taskCallback.onEventTask(this, DataID.TASK_STATUS_FINISHED_ALL);//池中所有任务完成
                    }
                }
            } else {
                MMLog.log(TAG, "not found PTask object in pool,break tag = " + tTag);
            }
        }
    }
}


