package com.zhuchao.android.session;

import com.zhuchao.android.eventinterface.InvokeInterface;
import com.zhuchao.android.eventinterface.TaskCallback;
import com.zhuchao.android.fbase.DataID;
import com.zhuchao.android.fbase.MMLog;
import com.zhuchao.android.fbase.TTask;

import java.util.concurrent.ConcurrentLinkedQueue;


public class TTaskQueue {
    private final String TAG = "TTaskQueue";
    private final ConcurrentLinkedQueue<TTask> concurrentLinkedQueue = new ConcurrentLinkedQueue<TTask>();
    private final TTask tTaskQueue = new TTask("TTaskQueue");
    private TaskCallback tTaskQueueCompleteCallback = null;
    private long delayedMillis = 0;
    private long dotTaskMillis = 1000;

    public TaskCallback getTaskQueueCompleteCallback() {
        return tTaskQueueCompleteCallback;
    }

    public void setTaskQueueCompleteCallback(TaskCallback tTaskQueueCompleteCallback) {
        this.tTaskQueueCompleteCallback = tTaskQueueCompleteCallback;
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
        this.dotTaskMillis = dotTaskMillis;
    }

    public int getQueueCount() {
        return concurrentLinkedQueue.size();
    }

    public boolean isEmpty() {
        return concurrentLinkedQueue.isEmpty();
    }

    public TTaskQueue addTTask(TTask task) {
        if (!concurrentLinkedQueue.contains(task))
            concurrentLinkedQueue.add(task);
        return this;
    }

    public TTask getTTask() {
        return concurrentLinkedQueue.poll();
    }

    private final InvokeInterface invokeInterface = new InvokeInterface() {
        TTask doTTask = null;

        @Override
        public void CALLTODO(String tag) {
            while (!concurrentLinkedQueue.isEmpty()) {
                doTTask = concurrentLinkedQueue.poll();
                if (doTTask == null) break;
                doTTask.setKeep(false);
                doTTask.callbackHandler(new TaskCallback() {
                    @Override
                    public void onEventTask(Object obj, int status) {
                        if (status == DataID.TASK_STATUS_FINISHED_STOP) {
                            //tTaskQueue.unPark();
                            doTTask.freeFree();
                        }
                    }
                });

                if (!doTTask.isBusy()) {
                    doTTask.start();
                } else {
                    MMLog.e(TAG, "startQueueTTask() wrong!!!!!!!!");
                    //continue;
                }
                //tTaskQueue.pack();//等待任务完成
                while (true) {//hold住线程，等待异步任务完成
                    try {
                        Thread.sleep(dotTaskMillis);//等待任务完成
                        if (!doTTask.isBusy())
                            break;
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
        if(tTaskQueue.getInvokeInterface() == null)
          tTaskQueue.invoke(invokeInterface);
        tTaskQueue.callbackHandler(new TaskCallback() {
            @Override
            public void onEventTask(Object obj, int status) {
                tTaskQueue.reset();
                if(tTaskQueueCompleteCallback != null)
                    tTaskQueueCompleteCallback.onEventTask(obj,status);
            }
        });
        tTaskQueue.setKeep(false);
        tTaskQueue.reset();//需要反复启动的任务,必须在启动之前调用reset
        tTaskQueue.startDelayed(delayedMillis);
    }

    public void free() {
        concurrentLinkedQueue.clear();
        tTaskQueue.freeFree();
    }

}
