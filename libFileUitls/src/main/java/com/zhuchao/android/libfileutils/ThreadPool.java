package com.zhuchao.android.libfileutils;

import static com.zhuchao.android.libfileutils.FilesManager.md5;

import android.text.TextUtils;

public class ThreadPool extends ObjectList {
    private int maxThreadCount = 100;
    private int minThreadCount = 0;
    private int taskCount = 0;

    public ThreadPool() {
        super();

    }

    public TTask createTask(String Name) {
        String id = makeKey(Name);
        TTask tTask = new TTask(id, null, this);
        if (TextUtils.isEmpty(Name)) return tTask;
        if (!exist(id))
            add(id, tTask);
        return tTask;
    }

    public TTask getTask(int Index) {
        return (TTask) getObject(Index);
    }

    public TTask getTask(String Name) {
        return (TTask) getObject(makeKey(Name));
    }

    private String makeKey(String keyName) {
        return md5(keyName);
    }

}

