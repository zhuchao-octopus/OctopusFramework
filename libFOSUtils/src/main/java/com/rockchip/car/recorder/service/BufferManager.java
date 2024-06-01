package com.rockchip.car.recorder.service;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by Administrator on 2016/9/8.
 */
public class BufferManager {
    private static Map<Integer, BufferCache> sBufferManager = new HashMap<>();

    public static synchronized BufferCache getInstance(int id) {
        if (sBufferManager.get(id) == null) {
            sBufferManager.put(id, new BufferCache(id));
        }
        return sBufferManager.get(id);
    }
}
