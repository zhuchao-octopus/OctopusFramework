package com.zhuchao.android.detect;

//import static org.bytedeco.javacpp.opencv_objdetect.CV_HAAR_DO_CANNY_PRUNING;
//import static org.bytedeco.javacpp.opencv_objdetect.CV_HAAR_DO_ROUGH_SEARCH;
//import static org.bytedeco.javacpp.opencv_objdetect.CV_HAAR_FIND_BIGGEST_OBJECT;
//import static org.bytedeco.javacpp.opencv_objdetect.CV_HAAR_SCALE_IMAGE;

import android.content.Context;
import android.util.Log;

import com.zhuchao.android.fbase.DateTimeUtils;
import com.zhuchao.android.fbase.FileUtils;
import com.zhuchao.android.fbase.MMLog;
import com.zhuchao.android.fbase.eventinterface.NormalCallback;
import com.zhuchao.android.libOpenDetection.R;

import org.opencv.android.CameraBridgeViewBase;
import org.opencv.core.Mat;
import org.opencv.core.MatOfRect;
import org.opencv.core.Rect;
import org.opencv.core.Scalar;
import org.opencv.core.Size;
import org.opencv.imgcodecs.Imgcodecs;
import org.opencv.imgproc.Imgproc;
import org.opencv.objdetect.CascadeClassifier;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

public class FaceDetector extends Detect {
    private final String TAG = "FaceDetector";
    private Context mContext;
    private Mat mRgba;
    private Mat mGray;
    private MatOfRect faces;

    private File mCascadeFile;
    private CascadeClassifier mJavaDetector;
    private DetectionBasedTracker mNativeDetector;

    private static final Scalar FACE_RECT_COLOR = new Scalar(0, 255, 0, 255);
    public static final int JAVA_DETECTOR = 0;
    public static final int NATIVE_DETECTOR = 1;
    public int mDetectorType = JAVA_DETECTOR;
    public String[] mDetectorName;

    private float mRelativeFaceSize = 0.2f;
    private int mAbsoluteFaceSize = 0;

    private OnDetectorListener mOnFaceDetectorListener;
    private String faceFileSavedPath;

    public FaceDetector(Context context) {
        super(context, null);
        mContext = context;
        mDetectorName = new String[2];
        mDetectorName[JAVA_DETECTOR] = "Java";
        mDetectorName[NATIVE_DETECTOR] = "Native (tracking)";
        setMinFaceSize(0.2f);
        setDetectorType(JAVA_DETECTOR);
        mGray = new Mat();
        mRgba = new Mat();
        faces = new MatOfRect();

        faceFileSavedPath = FileUtils.getDirBaseExternalStorageDirectory("com.zhuchao.face");
        //String parentDir = FileUtils.getFilePathFromPathName(filePathName);
        //FileUtils.CheckDirsExists(Objects.requireNonNull(parentDir));
    }

    public static synchronized FaceDetector create(Context context) {
        return new FaceDetector(context);
    }

    public FaceDetector initialize(NormalCallback normalCallback) {
        System.loadLibrary("detection_based_tracker");
        try {
            // load cascade file from application resources
            InputStream inputStream = mContext.getResources().openRawResource(R.raw.lbpcascade_frontalface);
            File cascadeDir = mContext.getDir("cascade", Context.MODE_PRIVATE);
            mCascadeFile = new File(cascadeDir, "lbpcascade_frontalface.xml");
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
            //e.printStackTrace();
            MMLog.e(TAG, "Failed to load cascade. Exception thrown: " + e);
        }

        return this;
    }


    public void setMinFaceSize(float faceSize) {
        mRelativeFaceSize = faceSize;
        mAbsoluteFaceSize = 0;
    }

    public void setDetectorType(int type) {
        if (mDetectorType != type) {
            mDetectorType = type;

            if (type == NATIVE_DETECTOR) {
                Log.i(TAG, "Detection Based Tracker enabled");
                mNativeDetector.start();
            } else {
                Log.i(TAG, "Cascade detector enabled");
                mNativeDetector.stop();
            }
        }
    }

    public void free() {
        mGray.release();
        mRgba.release();
        faces.release();
    }

    @Override
    public void onCameraViewStarted(int width, int height) {

    }

    @Override
    public void onCameraViewStopped() {

    }

    @Override
    public Mat onCameraFrame(CameraBridgeViewBase.CvCameraViewFrame inputFrame) {
        mRgba = inputFrame.rgba();
        mGray = inputFrame.gray();

        if (mAbsoluteFaceSize == 0) {
            int height = mGray.rows();
            if (Math.round(height * mRelativeFaceSize) > 0) {
                mAbsoluteFaceSize = Math.round(height * mRelativeFaceSize);
            }
            mNativeDetector.setMinFaceSize(mAbsoluteFaceSize);
        }

        if ((mDetectorType == JAVA_DETECTOR) && (mJavaDetector != null)) {

            mJavaDetector.detectMultiScale(mGray, faces, 1.1, 2, 2, // TODO: objdetect.CV_HAAR_SCALE_IMAGE
                    new Size(mAbsoluteFaceSize, mAbsoluteFaceSize), new Size());

            /*mJavaDetector.detectMultiScale(mGray, // 要检查的灰度图像
                    faces, // 检测到的人脸
                    1.1, // 表示在前后两次相继的扫描中，搜索窗口的比例系数。默认为1.1即每次搜索窗口依次扩大10%;
                    10, // 默认是3 控制误检测，表示默认几次重叠检测到人脸，才认为人脸存在
                    CV_HAAR_FIND_BIGGEST_OBJECT // 返回一张最大的人脸（无效？）
                            | CV_HAAR_SCALE_IMAGE
                            | CV_HAAR_DO_ROUGH_SEARCH
                            | CV_HAAR_DO_CANNY_PRUNING, //CV_HAAR_DO_CANNY_PRUNING ,// CV_HAAR_SCALE_IMAGE, // TODO: objdetect.CV_HAAR_SCALE_IMAGE
                    new Size(mAbsoluteFaceSize, mAbsoluteFaceSize),
                    new Size(mGray.width(), mGray.height()));
            */
        } else if (mDetectorType == NATIVE_DETECTOR) {
            if (mNativeDetector != null) mNativeDetector.detect(mGray, faces);
        } else {
            Log.e(TAG, "Detection method is not selected!");
        }
        // 检测到人脸
        Rect[] facesArray = faces.toArray();
        for (Rect rect : facesArray) {
            Imgproc.rectangle(mRgba, rect.tl(), rect.br(), FACE_RECT_COLOR, 2);
            if (null != mOnFaceDetectorListener) {
                mOnFaceDetectorListener.onFace(mRgba, rect);
            }
        }
        return mRgba;
    }

    private void saveFace(Mat face) {
        //long millSecs = System.currentTimeMillis();
        String millSecs = DateTimeUtils.getCurrentTime2();
        int temp = (int) (Math.random() * 1000);
        StringBuilder outputImgName = new StringBuilder();
        outputImgName.append(faceFileSavedPath).append("/").append(millSecs).append(temp).append(".jpg");
        if (face != null) {
            Imgcodecs.imwrite(outputImgName.toString(), face);
            //logger.info(">>>>>>write image into->" + outputDir);
        }
    }

}
