package com.zhuchao.android.libfileutils;

import static com.zhuchao.android.libfileutils.FilesManager.md5;

import android.text.TextUtils;

import com.zhuchao.android.utils.MMLog;

public class TTaskThreadPool extends ObjectList {
    private final String TAG = "TaskThreadPool";
    private int maxThreadCount = 100;
    private int minThreadCount = 0;
    private int taskCount = 0;

    public TTaskThreadPool() {
        super();
    }

    public TTaskThreadPool(int maxThreadCount) {
        super();
        this.maxThreadCount = maxThreadCount;
    }

    public PTask createTask(final String Name) {
        String tag = disguiseName(Name);
        if (existObject(tag)) //存在 对象
        {
            MMLog.log(TAG, "PTask already exist,tag = " + tag + ",task count = " + getCount());
            return (PTask) getObject(tag);
        }

        PTask pTask = new PTask(tag, this);
        MMLog.log(TAG, "create PTask name = " + Name + " tag = " + tag);
        //抛弃非法 TAG 对象
        if (TextUtils.isEmpty(tag)) {
            MMLog.log(TAG, "invalid PTask name/tag ,tag = " + tag);
            pTask.settTag(null);
            return pTask;
        }
        add(tag, pTask);
        MMLog.log(TAG, "create PTask successfully in pool,tag = " + tag + ",task count = " + getCount());
        return pTask;
    }

    public TTask getTaskByName(String Name) {
        if (TextUtils.isEmpty(Name)) return null;
        return (TTask) getObject(disguiseName(Name));
    }

    public TTask getTaskByTag(String tag) {
        if (TextUtils.isEmpty(tag)) return null;
        return (TTask) getObject(tag);
    }

    private String disguiseName(String Name) {
        return md5(Name);
    }

}

class PTask extends TTask {
    private final String TAG = "PTask";
    private TTaskThreadPool TTaskThreadPool = null;

    public PTask(String tag, TTaskThreadPool TTaskThreadPool) {
        super(tag, null);
        this.TTaskThreadPool = TTaskThreadPool;
    }

    @Override
    public void run() {
        if (TTaskThreadPool == null) {
            MMLog.log(TAG, "not found PTask pool object,break/stop tag = " + tTag);
            return;
        }

        if (TTaskThreadPool.existObject(tTag))
        {

            super.run();

            //最终结束任务，从任务池中清除掉
            TTaskThreadPool.delete(this.tTag);
            MMLog.log(TAG, "PTask successfully completed the task,remove from task pool tag = " + tTag);
        } else {
            MMLog.log(TAG, "not found PTask object in pool,break tag = " + tTag);
        }
    }
}

