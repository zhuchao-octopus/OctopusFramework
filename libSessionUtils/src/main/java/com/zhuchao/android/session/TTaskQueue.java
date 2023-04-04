package com.zhuchao.android.session;

import static java.lang.Thread.MAX_PRIORITY;

import com.zhuchao.android.eventinterface.InvokeInterface;
import com.zhuchao.android.eventinterface.TaskCallback;
import com.zhuchao.android.fbase.DataID;
import com.zhuchao.android.fbase.MMLog;
import com.zhuchao.android.fbase.ObjectArray;
import com.zhuchao.android.fbase.TTask;

import java.util.concurrent.ConcurrentLinkedQueue;


public class TTaskQueue  {
    private final String TAG = "TTaskQueue";
    private final ConcurrentLinkedQueue<TTask> concurrentLinkedQueue = new ConcurrentLinkedQueue<TTask>();
    private final TTask tTaskQueue = new TTask("TTaskQueue");
    private TaskCallback tTaskQueueCallback = null;
    private long delayedMillis = 0;
    private long dotTaskMillis = 1000;
    private int maxConcurrencyCount = 1;//并发任务后，1表示一个一个执行
    private final ObjectArray<TTask> concurrencyObjectArray = new ObjectArray<TTask>();
    private int priority = MAX_PRIORITY;



    public TTaskQueue() {
        concurrencyObjectArray.clear();
    }

    public TTaskQueue(long delayedMillis, long dotTaskMillis, int maxConcurrencyCount) {
        this.delayedMillis = delayedMillis;
        this.dotTaskMillis = dotTaskMillis;
        this.maxConcurrencyCount = maxConcurrencyCount;
        concurrencyObjectArray.clear();
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

    public void setConcurrencyCount(int concurrencyCount) {
        if (concurrencyCount <= 0)
            this.maxConcurrencyCount = 1;
        else
            this.maxConcurrencyCount = concurrencyCount;
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
        if (dotTaskMillis <= 0)
            this.dotTaskMillis = 500;
        else
            this.dotTaskMillis = dotTaskMillis;
    }

    public int getQueueCount() {
        return concurrentLinkedQueue.size();
    }

    public boolean isEmpty() {
        return concurrentLinkedQueue.isEmpty();
    }
    public boolean isWorking()
    {
        return tTaskQueue.isWorking();
    }
    public TTaskQueue addTTask(TTask task) {
        if (!concurrentLinkedQueue.contains(task))
            concurrentLinkedQueue.add(task);
        return this;
    }

    //public TTask getTTask() {
    //    return concurrentLinkedQueue.poll();
    //}

    private final InvokeInterface invokeInterface = new InvokeInterface() {
        TTask doTTask = null;

        @Override
        public void CALLTODO(String tag) {
            while (!concurrentLinkedQueue.isEmpty()) {
                doTTask = concurrentLinkedQueue.poll();
                if (doTTask == null) {
                    continue;
                    //break;
                }
                concurrencyObjectArray.add(doTTask);
                doTTask.setPriority(priority);
                doTTask.setKeep(false);
                doTTask.callbackHandler(new TaskCallback() {
                    @Override
                    public void onEventTask(Object obj, int status) {
                        if (status == DataID.TASK_STATUS_FINISHED_STOP) {
                            //tTaskQueue.unPark();
                            doTTask.freeFree();
                            concurrencyObjectArray.remove(doTTask);
                        }
                    }
                });

                if (!doTTask.isWorking()) {
                    doTTask.start();
                } else {
                    MMLog.e(TAG, "startQueueTTask() wrong!!!!!!!!");
                    //continue;
                }

                //tTaskQueue.pack();//等待任务完成
                while (true) {//hold住线程，等待异步任务完成
                    try {
                        if (concurrencyObjectArray.size() >= maxConcurrencyCount) {
                            Thread.sleep(dotTaskMillis);//等待任务完成
                        } else {
                            break;
                        }
                    } catch (InterruptedException e) {
                        MMLog.e(TAG, "startQueueTTask() " + e.getMessage());
                    }
                }//while
            }//while
        }
    };

    public void startWork() {
        if (!concurrentLinkedQueue.isEmpty())
            startQueueTTask();
    }

    private void startQueueTTask() {
        if (tTaskQueue.isWorking()) {
            return;//列队已经在工作
        }
        if (tTaskQueue.getInvokeInterface() == null)
            tTaskQueue.invoke(invokeInterface);//指派队列管理接口

        tTaskQueue.callbackHandler(new TaskCallback() {
            @Override
            public void onEventTask(Object obj, int status) {
                tTaskQueue.reset();
                if (status == DataID.TASK_STATUS_FINISHED_STOP) {
                    if (tTaskQueueCallback != null)
                        tTaskQueueCallback.onEventTask(obj, status);
                }
            }
        });
        tTaskQueue.setKeep(false);
        tTaskQueue.reset();//需要反复启动的任务,必须在启动之前调用reset
        tTaskQueue.startDelayed(delayedMillis);
    }

    public void free() {
        concurrentLinkedQueue.clear();
        tTaskQueue.freeFree();
        concurrencyObjectArray.clear();
    }

}
