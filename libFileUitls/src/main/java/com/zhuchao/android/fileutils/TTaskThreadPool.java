package com.zhuchao.android.fileutils;

import static com.zhuchao.android.fileutils.FileUtils.EmptyString;
import static com.zhuchao.android.fileutils.FileUtils.md5;

import java.util.Collection;

public class TTaskThreadPool extends ObjectList {
    private final String TAG = "TaskThreadPool";
    private int maxThreadCount = 1000;
    private int minThreadCount = 0;
    private int taskCounter = 0;
    //private List<PTask> pTaskList_Ok = null;

    public TTaskThreadPool() {
        super();
    }

    public TTaskThreadPool(int maxThreadCount) {
        super();
        this.maxThreadCount = maxThreadCount;
        //pTaskList_Ok = new ArrayList<>();
    }

    public PTask createTask(String Name) {
        if (EmptyString(Name)) {
            Name = "default";
            MMLog.log(TAG, "only one default PTask name/tag ,name = " + Name);
        }
        String tag = disguiseName(Name);
        if (existObject(tag)) //存在 对象
        {
            MMLog.log(TAG, "PTask already exist in pool,tag = " + tag + ",total count = " + getCount());
            return (PTask) getObject(tag);
        }

        PTask pTask = new PTask(tag);
        pTask.setTName(Name);
        addItem(tag, pTask);
        MMLog.log(TAG, "create PTask name = " + Name + ",tag = " + tag);
        //MMLog.log(TAG, "create PTask successfully in pool,tag = " + tag + ",total count:" + getCount()+"|"+taskCounter);
        return pTask;
    }

    public TTask getTaskByName(String Name) {
        if (EmptyString(Name))
            return (TTask) getObject(disguiseName("default"));
        return (TTask) getObject(disguiseName(Name));
    }

    public TTask getTaskByTag(String tag) {
        if (EmptyString(tag)) return null;
        return (TTask) getObject(tag);
    }

    public void deleteTask(String tag) {
        delete(tag);
        MMLog.log(TAG, "delete task tag = " + tag);
    }

    public void deleteTask(TTask tTask) {
        delete(tTask.getTTag());
        MMLog.log(TAG, "delete task tag = " + tTask.getTTag() + ",name = " + tTask.getTName());
    }

    private String disguiseName(String Name) {
        return md5(Name);
    }

    private int getTaskCounter() {
        return taskCounter;
    }

    private void setTaskCounter(int taskCounter) {
        this.taskCounter = taskCounter;
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
        public PTask(String tag) {
            super(tag, null);
        }

        @Override
        public void run() {
            if (existObject(tTag)) {
                MMLog.log(TAG, "invoke TTask demon tTag = " + tTag);
                super.run(); //执行父类 TTask
                //最终结束任务，从任务池中清除掉
                //TTaskThreadPool.delete(this.tTag);
                //MMLog.log(TAG, "PTask complete successfully,remove from task pool tag = " + tTag);
                MMLog.log(TAG, "PTask complete successfully,tag = " + tTag + ",total count:" + getCount() + "|" + taskCounter);
                properties.putInt(DataID.TASK_STATUS_INTERNAL_, DataID.TASK_STATUS_FINISHED);//内部使用
                setTaskCounter(getTaskCounter() + 1);
                if (getTaskCounter() == getCount()) {
                    if (taskCallback != null) {
                        taskCallback.onEventTask(this, DataID.TASK_STATUS_ALL_FINISHED);
                    }
                }
            } else {
                MMLog.log(TAG, "not found PTask object in pool,break tag = " + tTag);
            }
        }
    }
}

