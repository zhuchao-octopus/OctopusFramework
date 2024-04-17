package com.zhuchao.android.detect;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.opencv.android.BaseLoaderCallback;
import org.opencv.android.CameraActivity;
import org.opencv.android.CameraBridgeViewBase.CvCameraViewFrame;
import org.opencv.android.LoaderCallbackInterface;
import org.opencv.android.OpenCVLoader;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.MatOfRect;
import org.opencv.core.Rect;
import org.opencv.core.Scalar;
import org.opencv.core.Size;
import org.opencv.android.CameraBridgeViewBase;
import org.opencv.android.CameraBridgeViewBase.CvCameraViewListener2;
import org.opencv.imgcodecs.Imgcodecs;
import org.opencv.objdetect.CascadeClassifier;
import org.opencv.imgproc.Imgproc;

import android.content.Context;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.Toast;

import com.zhuchao.android.fbase.MMLog;
import com.zhuchao.android.libOpenDetection.R;


public class FdActivity extends CameraActivity implements CvCameraViewListener2 {
    private static final String TAG = "OCVSample::Activity";
    private static final Scalar FACE_RECT_COLOR = new Scalar(0, 255, 0, 255);
    public static final int JAVA_DETECTOR = 0;
    public static final int NATIVE_DETECTOR = 1;


    private Mat mRgba;
    private Mat mGray;
    private CascadeClassifier mJavaDetector;
    private DetectionBasedTracker mNativeDetector;

    public int mDetectorType = JAVA_DETECTOR;
    public String[] mDetectorName;

    private float mRelativeFaceSize = 0.2f;
    private int mAbsoluteFaceSize = 0;
    private CameraBridgeViewBase mOpenCvCameraView1;
    private CameraBridgeViewBase mOpenCvCameraView2;


    //////////////////////////////////////////////////////////////////////////////////////////////
    public FdActivity() {
        mDetectorName = new String[2];
        mDetectorName[JAVA_DETECTOR] = "Java";
        mDetectorName[NATIVE_DETECTOR] = "Native (tracking)";
        MMLog.i(TAG, "Instantiated new " + this.getClass());
    }

    /**
     * Called when the activity is first created.
     */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        MMLog.i(TAG, "called onCreate()");
        super.onCreate(savedInstanceState);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        setContentView(R.layout.face_detect_surface_view);

        mOpenCvCameraView1 = (CameraBridgeViewBase) findViewById(R.id.fd_activity_surface_view1);
        mOpenCvCameraView1.setVisibility(CameraBridgeViewBase.VISIBLE);
        mOpenCvCameraView1.setCvCameraViewListener(this);

        mOpenCvCameraView2 = (CameraBridgeViewBase) findViewById(R.id.fd_activity_surface_view2);
        mOpenCvCameraView2.setVisibility(CameraBridgeViewBase.VISIBLE);
        //mOpenCvCameraView2.setCvCameraViewListener(this);
        Button button1 = (Button) findViewById(R.id.button1);

    }

    @Override
    public void onPause() {
        super.onPause();
        if (mOpenCvCameraView1 != null) mOpenCvCameraView1.disableView();
        if (mOpenCvCameraView2 != null) mOpenCvCameraView2.disableView();
    }

    @Override
    public void onResume() {
        super.onResume();
        if (!OpenCVLoader.initDebug()) {
            MMLog.d(TAG, "Internal OpenCV library not found. Using OpenCV Manager for initialization");
            OpenCVLoader.initAsync(OpenCVLoader.OPENCV_VERSION_3_0_0, this, mLoaderCallback);
        } else {
            MMLog.d(TAG, "OpenCV library found inside package. Using it!");
            mLoaderCallback.onManagerConnected(LoaderCallbackInterface.SUCCESS);
        }
    }

    public void onDestroy() {
        super.onDestroy();
        mOpenCvCameraView1.disableView();
        mOpenCvCameraView2.disableView();
    }

    @Override
    protected List<? extends CameraBridgeViewBase> getCameraViewList() {
        //return Collections.singletonList(mOpenCvCameraView1);
        List<CameraBridgeViewBase> list = new ArrayList<>();
        list.add(mOpenCvCameraView1);
        list.add(mOpenCvCameraView2);
        return list;
    }

    public void onCameraViewStarted(int width, int height) {
        mGray = new Mat();
        mRgba = new Mat();
    }

    public void onCameraViewStopped() {
        if (mGray != null) mGray.release();
        if (mRgba != null) mRgba.release();
    }

    public Mat onCameraFrame(CvCameraViewFrame inputFrame) {
        mRgba = inputFrame.rgba();
        mGray = inputFrame.gray();

        if (mAbsoluteFaceSize == 0) {
            int height = mGray.rows();
            if (Math.round(height * mRelativeFaceSize) > 0) {
                mAbsoluteFaceSize = Math.round(height * mRelativeFaceSize);
            }
            mNativeDetector.setMinFaceSize(mAbsoluteFaceSize);
        }

        MatOfRect faces = new MatOfRect();

        if (mDetectorType == JAVA_DETECTOR) {
            if (mJavaDetector != null) mJavaDetector.detectMultiScale(mGray, faces, 1.1, 2, 2, // TODO: objdetect.CV_HAAR_SCALE_IMAGE
                    new Size(mAbsoluteFaceSize, mAbsoluteFaceSize), new Size());
        } else if (mDetectorType == NATIVE_DETECTOR) {
            if (mNativeDetector != null) mNativeDetector.detect(mGray, faces);
        } else {
            MMLog.e(TAG, "Detection method is not selected!");
        }

        Rect[] facesArray = faces.toArray();
        for (Rect rect : facesArray) Imgproc.rectangle(mRgba, rect.tl(), rect.br(), FACE_RECT_COLOR, 2);

        return mRgba;
    }

    public void setMinFaceSize(float faceSize) {
        mRelativeFaceSize = faceSize;
        mAbsoluteFaceSize = 0;
    }

    public void setDetectorType(int type) {
        if (mDetectorType != type) {
            mDetectorType = type;

            if (type == NATIVE_DETECTOR) {
                MMLog.i(TAG, "Detection Based Tracker enabled");
                mNativeDetector.start();
            } else {
                MMLog.i(TAG, "Cascade detector enabled");
                mNativeDetector.stop();
            }
        }
    }

    private final BaseLoaderCallback mLoaderCallback = new BaseLoaderCallback(this) {
        @Override
        public void onManagerConnected(int status) {
            if (status == LoaderCallbackInterface.SUCCESS) {
                MMLog.i(TAG, "OpenCV loaded successfully");
                // Load native library after(!) OpenCV initialization
                System.loadLibrary("detection_based_tracker");

                try {
                    // load cascade file from application resources
                    InputStream inputStream = getResources().openRawResource(R.raw.lbpcascade_frontalface);
                    File cascadeDir = getDir("cascade", Context.MODE_PRIVATE);
                    File mCascadeFile = new File(cascadeDir, "lbpcascade_frontalface.xml");
                    FileOutputStream fileOutputStream = new FileOutputStream(mCascadeFile);

                    byte[] buffer = new byte[4096];
                    int bytesRead;
                    while ((bytesRead = inputStream.read(buffer)) != -1) {
                        fileOutputStream.write(buffer, 0, bytesRead);
                    }
                    inputStream.close();
                    fileOutputStream.close();

                    mJavaDetector = new CascadeClassifier(mCascadeFile.getAbsolutePath());
                    if (mJavaDetector.empty()) {
                        MMLog.e(TAG, "Failed to load cascade classifier");
                        mJavaDetector = null;
                    } else MMLog.i(TAG, "Loaded cascade classifier from " + mCascadeFile.getAbsolutePath());

                    mNativeDetector = new DetectionBasedTracker(mCascadeFile.getAbsolutePath(), 0);
                    cascadeDir.delete();
                } catch (IOException e) {
                    e.printStackTrace();
                    MMLog.e(TAG, "Failed to load cascade. Exception thrown: " + e);
                }

                mOpenCvCameraView1.enableView();
                mOpenCvCameraView2.enableView();
            } else {
                super.onManagerConnected(status);
            }
        }
    };
}
