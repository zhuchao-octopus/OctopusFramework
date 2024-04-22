package com.zhuchao.android.detect;

import static android.Manifest.permission.CAMERA;

import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageManager;
import android.util.AttributeSet;

import com.zhuchao.android.fbase.MMLog;

import org.opencv.android.BaseLoaderCallback;
import org.opencv.android.JavaCamera2View;
import org.opencv.android.LoaderCallbackInterface;
import org.opencv.android.OpenCVLoader;

public class AndroidCamera2View extends JavaCamera2View {
    private final String TAG = "Camera2View";
    public static final int CAMERA_PERMISSION_REQUEST_CODE = 200;
    //public CameraControllerManager2 cameraControllerManager2;

    public AndroidCamera2View(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    private void onCameraPermissionGranted() {
        this.setCameraPermissionGranted();
    }

    public void requestForPermissions(Context context) {
        boolean havePermission = true;
        if (context.checkSelfPermission(CAMERA) != PackageManager.PERMISSION_GRANTED) {
            ((Activity) context).requestPermissions(new String[]{CAMERA}, CAMERA_PERMISSION_REQUEST_CODE);
            havePermission = false;
        }
        if (havePermission) {
            //如果OPEN CV LOADER 没有初始化那么先初始化。
            tryInitializeOCVLoader(context);
            onCameraPermissionGranted();
        }
    }

    public void startPreview() {
        requestForPermissions(getContext());
    }

    public void stopPreview() {
        disableView();
    }

    public void setDetection(CvCameraViewListener2 detection) {
        setCvCameraViewListener(detection);
    }

    private void tryInitializeOCVLoader(Context context) {
        if (!OpenCVLoader.initDebug()) {
            MMLog.d(TAG, "Internal OpenCV library not found. Using OpenCV Manager for initialization");
            OpenCVLoader.initAsync(OpenCVLoader.OPENCV_VERSION_3_0_0, context, mLoaderCallback);
        } else {
            MMLog.d(TAG, "OpenCV library found inside package. Using it!");
            mLoaderCallback.onManagerConnected(LoaderCallbackInterface.SUCCESS);
        }

    }

    private BaseLoaderCallback mLoaderCallback = new BaseLoaderCallback(getContext()) {
        @Override
        public void onManagerConnected(int status) {
            switch (status) {
                case LoaderCallbackInterface.SUCCESS: {
                    MMLog.i(TAG, "OpenCV loaded successfully");
                    // Load native library after(!) OpenCV initialization
                    enableView();
                }
                break;
                default: {
                    super.onManagerConnected(status);
                }
                break;
            }
        }
    };
}
