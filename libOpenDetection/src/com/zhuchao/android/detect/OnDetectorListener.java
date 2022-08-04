package com.zhuchao.android.detect;

import org.opencv.core.Mat;
import org.opencv.core.Rect;

public interface OnDetectorListener {
    // 检测到一个人脸的回调
    void onFace(Mat mat, Rect rect);
}
