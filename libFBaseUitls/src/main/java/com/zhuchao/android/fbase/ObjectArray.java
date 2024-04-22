package com.zhuchao.android.fbase;

import java.util.ArrayList;


public class ObjectArray<E> extends ArrayList<E> {
    private final String TAG = "ObjectArray";

    public ObjectArray() {
    }

    public void print() {
        for (int i = 0; i < this.size() - 1; i++) {
            if (i < 100) MMLog.i(TAG, String.format("%-2s", i) + ":" + this.get(i).toString());
            else if (i < 1000) MMLog.i(TAG, String.format("%-3s", i) + ":" + this.get(i).toString());
            else if (i < 10000) MMLog.i(TAG, String.format("%-4s", i) + ":" + this.get(i).toString());
            else if (i < 100000) MMLog.i(TAG, String.format("%-5s", i) + ":" + this.get(i).toString());
            else if (i < 1000000) MMLog.i(TAG, String.format("%-6s", i) + ":" + this.get(i).toString());
            else if (i < 10000000) MMLog.i(TAG, String.format("%-7s", i) + ":" + this.get(i).toString());
            else if (i < 100000000) MMLog.i(TAG, String.format("%-8s", i) + ":" + this.get(i).toString());
            else if (i < 1000000000) MMLog.i(TAG, String.format("%-9s", i) + ":" + this.get(i).toString());
            else MMLog.i(TAG, i + this.get(i).toString());
        }
    }
}
