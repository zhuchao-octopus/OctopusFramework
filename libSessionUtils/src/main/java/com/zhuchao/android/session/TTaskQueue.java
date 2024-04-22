package com.zhuchao.android.session;

import static java.lang.Thread.MAX_PRIORITY;

import com.zhuchao.android.fbase.DataID;
import com.zhuchao.android.fbase.FileUtils;
import com.zhuchao.android.fbase.MMLog;
import com.zhuchao.android.fbase.ObjectArray;
import com.zhuchao.android.fbase.TTask;
import com.zhuchao.android.fbase.eventinterface.InvokeInterface;
import com.zhuchao.android.fbase.eventinterface.TaskCallback;

import java.util.concurrent.ConcurrentLinkedQueue;


public class TTaskQueue {
    private final String TAG = "TTaskQueue";
    //用户任务列表
    private final ConcurrentLinkedQueue<TTask> userLinkedQueue = new ConcurrentLinkedQueue<TTask>();
    //工作任务列表
    private final ObjectArray<TTask> workingObjectArray = new ObjectArray<TTask>();

    private final TTask tTaskQueue = new TTask("TTaskQueue");
    private TaskCallback tTaskQueueCallback = null;
    private long delayedMillis = 0;
    private long dotTaskMillis = 1000;
    private int maxConcurrencyCount = 1;//并发任务后，1表示一个一个执行
    private int priority = MAX_PRIORITY;


    public TTaskQueue() {
        workingObjectArray.clear();
        //tTaskQueue.lock();
    }

    public TTaskQueue(long delayedMillis, long dotTaskMillis, int maxConcurrencyCount) {
        this.delayedMillis = delayedMillis;
        this.dotTaskMillis = dotTaskMillis;
        this.maxConcurrencyCount = maxConcurrencyCount;
        workingObjectArray.clear();
        //tTaskQueue.lock();
    }

    public int getPriority() {
        return priority;
    }

    public void setPriority(int priority) {
        this.priority = priority;
    }

    public TaskCallback getTaskQueueCompleteCallback() {
        return tTaskQueueCallback;
    }

    public void setTaskQueueCompleteCallback(TaskCallback tTaskQueueCompleteCallback) {
        this.tTaskQueueCallback = tTaskQueueCompleteCallback;
    }

    public int getConcurrencyCount() {
        return maxConcurrencyCount;
    }

    public void setMaxConcurrencyCount(int concurrencyCount) {
        if (concurrencyCount <= 0) this.maxConcurrencyCount = 1;
        else this.maxConcurrencyCount = concurrencyCount;
    }

    public long getDelayedMillis() {
        return delayedMillis;
    }

    public void setDelayedMillis(long delayedMillis) {
        this.delayedMillis = delayedMillis;
    }

    public long getDotTaskMillis() {
        return dotTaskMillis;
    }

    public void setDotTaskMillis(long dotTaskMillis) {
        if (dotTaskMillis <= 0) this.dotTaskMillis = 500;
        else this.dotTaskMillis = dotTaskMillis;
    }

    public int getQueueCount() {
        return userLinkedQueue.size();
    }

    public boolean isEmpty() {
        return userLinkedQueue.isEmpty();
    }

    public boolean isWorking() {
        return tTaskQueue.isWorking();
    }

    public TTaskQueue addTTask(TTask task) {
        if (!userLinkedQueue.contains(task)) {
            userLinkedQueue.add(task);
            //MMLog.log(TAG,"TaskQueue added "+task.getTaskName());
        }
        return this;
    }

    private final InvokeInterface invokeInterface = new InvokeInterface() {
        TTask doTTask = null;

        @Override
        public void CALLTODO(String tag) {
            while (!userLinkedQueue.isEmpty()) {
                doTTask = userLinkedQueue.poll();
                if (doTTask == null) {
                    continue;
                }
                /////////////////////////////////////////////////////////////////////////////////
                /////////////////////////////////////////////////////////////////////////////////
                workingObjectArray.add(doTTask);
                doTTask.setPriority(priority);
                doTTask.setKeep(false);
                doTTask.getProperties().putInt("result_status", DataID.TASK_STATUS_SUCCESS);
                doTTask.callbackHandler(new TaskCallback() {
                    @Override
                    public void onEventTask(Object obj, int status) {
                        if (status == DataID.TASK_STATUS_SUCCESS || status == DataID.TASK_STATUS_FINISHED_STOP) {
                            ///主题任务完成了，队列不干预任务的生命周期
                            ///tTaskQueue.unPark();
                            ///doTTask.freeFree();
                            ///doTTask.free();//去除同步等待标记，队列不干预任务的生命周期
                            workingObjectArray.remove(obj);
                        }
                    }
                });
                /////////////////////////////////////////////////////////////////////////////////
                /////////////////////////////////////////////////////////////////////////////////
                if (doTTask.isFinishedStop()) {
                    MMLog.log(TAG, "Ttask isFinishedStop" + doTTask.getTaskName());
                }
                if (!doTTask.isWorking()) doTTask.start();
                /////////////////////////////////////////////////////////////////////////////////
                /////////////////////////////////////////////////////////////////////////////////
                while (workingObjectArray.size() >= maxConcurrencyCount) {//hold住线程，等待异步任务完成
                    FileUtils.WaitingFor(dotTaskMillis);//等待任务完成
                }//while
            }//while
            /////////////////////////////////////////////////////////////////////////////////
            /////////////////////////////////////////////////////////////////////////////////
            while (!workingObjectArray.isEmpty()) {//等待并发任务完成
                FileUtils.WaitingFor(dotTaskMillis);//等待并发任务完成
            }
        }
    };

    public void startWork() {
        if (!userLinkedQueue.isEmpty()) startQueueTTask();
    }

    private void startQueueTTask() {
        if (tTaskQueue.isWorking()) {
            return;//列队已经在工作
        }

        if (tTaskQueue.getInvokeInterface() == null) tTaskQueue.invoke(invokeInterface);//指派队列管理接口

        //队列工作完毕的回调
        tTaskQueue.callbackHandler(new TaskCallback() {
            @Override
            public void onEventTask(Object obj, int status) {
                if (status == DataID.TASK_STATUS_FINISHED_STOP) {
                    if (tTaskQueueCallback != null) tTaskQueueCallback.onEventTask(obj, status);
                }
            }
        });

        tTaskQueue.setKeep(false);
        tTaskQueue.reset();//需要反复启动的任务,必须在启动之前调用reset
        tTaskQueue.startDelayed(delayedMillis);
    }

    public void free() {
        userLinkedQueue.clear();
        tTaskQueue.freeFree();
        workingObjectArray.clear();
    }

    public void clear() {
        userLinkedQueue.clear();
        tTaskQueue.freeFree();
        workingObjectArray.clear();
    }

    public void printQueue() {

        for (TTask task0 : userLinkedQueue) {
            MMLog.log(TAG, "Queue 0:" + task0.getTaskName());
        }

        for (TTask task1 : workingObjectArray) {
            MMLog.log(TAG, "Queue 1:" + task1.getTaskName());
        }
    }
}
