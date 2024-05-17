package com.zhuchao.android.detect;

import android.content.Context;
import android.hardware.camera2.CameraDevice;
import android.hardware.camera2.CameraManager;
import android.util.AttributeSet;
import android.util.Log;

import com.zhuchao.android.fbase.MMLog;

import org.opencv.android.JavaCamera2View;

public class TOpenCVCamera2 extends JavaCamera2View {
    private final String TAG = "OpenCVCamera2";
    private final Context mContext;

    public TOpenCVCamera2(Context context) {
        super(context);
        mContext = context;
    }

    public TOpenCVCamera2(Context context, AttributeSet attrs) {
        super(context, attrs);
        mContext = context;
    }

    public String getCamaraID() {
        return mCameraID;
    }

    public int getCamaraIndex() {
        return mCameraIndex;
    }

    public CameraDevice getCamaraDevice() {
        return mCameraDevice;
    }

    public void setCamaraIndex(int cameraIndex) {
        mCameraIndex = cameraIndex;
    }


}
