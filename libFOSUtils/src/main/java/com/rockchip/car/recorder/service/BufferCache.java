package com.rockchip.car.recorder.service;

import java.util.LinkedList;
import java.util.Queue;

/**
 * Created by Administrator on 2016/9/8.
 */
public class BufferCache {
    private int mId;
    private Queue<byte[]> mBuffers;

    BufferCache(int id) {
        this.mId = id;
        mBuffers = new LinkedList<byte[]>();
    }

    public synchronized void push(byte[] bytes) {
        if (bytes != null) mBuffers.offer(bytes);
    }

    public synchronized byte[] pull() {
        return mBuffers.size() > 0 ? mBuffers.poll() : null;
    }

    public synchronized int size() {
        return mBuffers.size();
    }

    public synchronized Queue<byte[]> getBuffers() {
        return mBuffers;
    }

    public synchronized void clear() {
        mBuffers.clear();
    }
}
