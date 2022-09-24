package com.zhuchao.android.detect;

import android.content.Context;
import android.util.Log;

import com.zhuchao.android.callbackevent.NormalCallback;
import com.zhuchao.android.fileutils.MMLog;

import org.opencv.android.BaseLoaderCallback;
import org.opencv.android.CameraBridgeViewBase;
import org.opencv.android.LoaderCallbackInterface;
import org.opencv.android.OpenCVLoader;
import org.opencv.core.Mat;
import org.opencv.objdetect.CascadeClassifier;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

public class Detect implements CameraBridgeViewBase.CvCameraViewListener2 {
    private final String TAG = "Detect";
    private Context mContext;
    //private NormalCallback normalCallback;
    private BaseLoaderCallback mLoaderCallback;
    private OnOpenCVInitListener mOnOpenCVInitListener;
    private boolean isLoadSuccess = false;

    public Detect(Context context, OnOpenCVInitListener Callback) {
        mContext = context;
        mOnOpenCVInitListener = Callback;
        tryInitializeOCVLoader();//如果OPEN CV LOADER 没有初始化那么先初始化。

        mLoaderCallback = new BaseLoaderCallback(mContext) {
            @Override
            public void onManagerConnected(int status) {
                switch (status) {
                    case LoaderCallbackInterface.SUCCESS:
                        MMLog.i(TAG, "onManagerConnected: OpenCV加载成功");
                        if (null != mOnOpenCVInitListener) {
                            mOnOpenCVInitListener.onLoadSuccess();
                        }
                        isLoadSuccess = true;
                        break;
                    case LoaderCallbackInterface.MARKET_ERROR: // OpenCV loader can not start Google Play Market.
                        MMLog.i(TAG, "onManagerConnected: 打开Google Play失败");
                        if (null != mOnOpenCVInitListener) {
                            mOnOpenCVInitListener.onMarketError();
                        }
                        break;
                    case LoaderCallbackInterface.INSTALL_CANCELED: // Package installation has been canceled.
                        MMLog.i(TAG, "onManagerConnected: 安装被取消");
                        if (null != mOnOpenCVInitListener) {
                            mOnOpenCVInitListener.onInstallCanceled();
                        }
                        break;
                    case LoaderCallbackInterface.INCOMPATIBLE_MANAGER_VERSION: // Application is incompatible with this version of OpenCV Manager. Possibly, a service update is required.
                        MMLog.i(TAG, "onManagerConnected: 版本不正确");
                        if (null != mOnOpenCVInitListener) {
                            mOnOpenCVInitListener.onIncompatibleManagerVersion();
                        }
                        break;
                    default: // Other status,
                        MMLog.i(TAG, "onManagerConnected: 其他错误");
                        if (null != mOnOpenCVInitListener) {
                            mOnOpenCVInitListener.onOtherError();
                        }
                        // super.onManagerConnected(status);
                        break;
                }
            }
        };
    }

    public void tryInitializeOCVLoader() {
        if (!OpenCVLoader.initDebug()) {
            MMLog.d(TAG, "Internal OpenCV library not found. Using OpenCV Manager for initialization");
            OpenCVLoader.initAsync(OpenCVLoader.OPENCV_VERSION_3_0_0, mContext, mLoaderCallback);
        } else {
            MMLog.d(TAG, "OpenCV library found inside package. Using it!");
            mLoaderCallback.onManagerConnected(LoaderCallbackInterface.SUCCESS);
        }

    }

    @Override
    public void onCameraViewStarted(int width, int height) {

    }

    @Override
    public void onCameraViewStopped() {

    }

    @Override
    public Mat onCameraFrame(CameraBridgeViewBase.CvCameraViewFrame inputFrame) {
        return null;
    }

    public BaseLoaderCallback getLoaderCallback() {
        return mLoaderCallback;
    }

    public void setLoaderCallback(BaseLoaderCallback mLoaderCallback) {
        this.mLoaderCallback = mLoaderCallback;
    }

    public OnOpenCVInitListener getOnOpenCVInitListener() {
        return mOnOpenCVInitListener;
    }

    public void setOnOpenCVInitListener(OnOpenCVInitListener mOnOpenCVInitListener) {
        this.mOnOpenCVInitListener = mOnOpenCVInitListener;
    }

    public boolean isLoadSuccess() {
        return isLoadSuccess;
    }

}
