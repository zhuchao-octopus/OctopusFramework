package com.zhuchao.android.camera.camera2;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.pm.PackageManager;
import android.hardware.camera2.CameraAccessException;
import android.hardware.camera2.CameraCaptureSession;
import android.hardware.camera2.CameraCharacteristics;
import android.hardware.camera2.CameraDevice;
import android.hardware.camera2.CameraManager;
import android.hardware.camera2.CameraMetadata;
import android.hardware.camera2.CaptureRequest;
import android.hardware.camera2.params.OutputConfiguration;
import android.hardware.camera2.params.SessionConfiguration;
import android.media.AudioManager;
import android.media.CamcorderProfile;
import android.media.MediaCodec;
import android.media.MediaRecorder;
import android.os.Build;
import android.os.Environment;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.StatFs;
import android.text.TextUtils;
import android.util.Size;
import android.view.Surface;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;

import com.zhuchao.android.fbase.MMLog;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Objects;
import java.util.concurrent.Executor;
import java.util.concurrent.locks.ReentrantLock;

public class Camera2Recorder {
    private final String TAG = getClass().getSimpleName();
    private static final boolean DEBUG = true;
    private static final String DATA_FORMAT_PATTERN = "yyyyMMdd_HHmmss";
    private static final long RECORD_LOW_STORAGE_THRESHOLD = 50 * 1024 * 1024L; //50M
    private static final long RECORD_LIMIT_SIZE = 1024 * 1024 * 1024L; //1G
    private static final long MIN_REMAIN_AVAILABLE_SPACE = 1024L; //1kb
    private static final int MEDIA_RECORDER_ENCODER_ERROR = -1103;

    private final int CAMERA_STATE_OPENING = 0x001;
    private final int CAMERA_STATE_OPENED = 0x002;
    private final int CAMERA_STATE_CLOSE = 0x003;
    private final int CAMERA_STATE_ERROR = 0x004;
    private final int CAMERA_STATE_PREVIEW = 0x005;
    private final CameraManager mCameraManager;
    private final ReentrantLock mCameraStateLock = new ReentrantLock();
    private CameraCharacteristics mCameraCharacteristics;
    private CameraDevice mCameraDevice;
    private CameraCaptureSession mCameraSession;
    private CaptureRequest.Builder mRequestBuilder;
    private Handler mCameraHandler;
    private HandlerThread mCameraThread;

    private Surface mPreviewSurface;  // the surface for display
    private Surface mRecordSurface;
    private MediaRecorder mMediaRecorder;
    private boolean mIsRecording = false;
    private int mCameraState = CAMERA_STATE_CLOSE;

    // Clockwise angle through which the output image needs to be rotated to be upright on the device screen.
    // Range of valid values: 0, 90, 180, 270
    // for set video orientation
    private int mDisplayRotation;
    private final Camera2Config mCamera2Config;
    private final Size mPreviewSize;
    private final AudioManager mAudioManager;
    private boolean mStreamSystemMute = false;
    private final Handler mHandler;
    private final Context mContext;

    public Size getPreviewSize() {
        return mPreviewSize;
    }

    public void setPreviewSurface(Surface previewSurface) {
        mPreviewSurface = previewSurface;
    }

    public Camera2Recorder(Context context, @NonNull Camera2Config config) {
        mContext = context;
        mCameraManager = (CameraManager) mContext.getSystemService(Context.CAMERA_SERVICE);
        mCamera2Config = config;
        mPreviewSize = config.getPreviewSize();
        mAudioManager = (AudioManager) mContext.getSystemService(Context.AUDIO_SERVICE);
        mStreamSystemMute = mAudioManager.isStreamMute(AudioManager.STREAM_SYSTEM);
        mHandler = new Handler(mContext.getMainLooper());
        openCamera2();
    }

    public void openCamera2() {
        if (mCameraState != CAMERA_STATE_CLOSE) {
            MMLog.e(TAG, "only could open camera when closed");
            return;
        }
        if (ActivityCompat.checkSelfPermission(mContext, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
            MMLog.e(TAG, "Open camera failed! No permission CAMERA.");
            return;
        }
        ///if (mPreviewSurface == null) {
        ///    MMLog.e(TAG, "Open camera failed! No PreviewSurface");
        ///    return;
        ///}

        mCameraStateLock.lock();
        String cameraId = mCamera2Config.getCameraId();
        MMLog.i(TAG, "openCamera --> cameraId: " + cameraId);
        mCameraState = CAMERA_STATE_OPENING;
        startBackgroundThread();
        try {
            String[] cameraIdList = mCameraManager.getCameraIdList();
            boolean isValidCameraId = false;
            for (String s : cameraIdList) {
                if (s.equals(cameraId)) {
                    isValidCameraId = true;
                    break;
                }
            }
            if (isValidCameraId) {
                mCameraCharacteristics = mCameraManager.getCameraCharacteristics(cameraId);
                initDisplayRotation(mCameraCharacteristics, Surface.ROTATION_0);
                mCameraManager.openCamera(cameraId, mCameraOpenCallback, mCameraHandler);
            } else {
                MMLog.e(TAG, "openCamera failed! invalid camera id: " + cameraId);
            }
        } catch (CameraAccessException e) {
            ///e.printStackTrace();
            MMLog.e(TAG, String.valueOf(e));
        } finally {
            mCameraStateLock.unlock();
        }
    }


    private final CameraDevice.StateCallback mCameraOpenCallback = new CameraDevice.StateCallback() {
        @Override
        public void onOpened(@NonNull CameraDevice camera) {
            mCameraState = CAMERA_STATE_OPENED;
            mCameraDevice = camera;
            createVideoSession();  // create session after open camera
        }

        @Override
        public void onDisconnected(@NonNull CameraDevice camera) {
            mCameraState = CAMERA_STATE_ERROR;
            releaseCamera();
        }

        @Override
        public void onError(@NonNull CameraDevice camera, int error) {
            MMLog.e(TAG, "Camera onError: " + error);
            mCameraState = CAMERA_STATE_ERROR;
            releaseCamera();
        }
    };

    private void startBackgroundThread() {
        if (mCameraThread == null || mCameraHandler == null) {
            MMLog.v(TAG, "startBackgroundThread");
            mCameraThread = new HandlerThread("CameraBackground");
            mCameraThread.start();
            mCameraHandler = new Handler(mCameraThread.getLooper());
        }
    }

    private void initDisplayRotation(CameraCharacteristics cameraCharacteristics, int displayRotation) {
        if (cameraCharacteristics == null || mContext == null) {
            return;
        }
        ///int displayRotation = mContext.getWindowManager().getDefaultDisplay().getRotation();
        switch (displayRotation) {
            case Surface.ROTATION_0:
                displayRotation = 90;
                break;
            case Surface.ROTATION_90:
                displayRotation = 0;
                break;
            case Surface.ROTATION_180:
                displayRotation = 270;
                break;
            case Surface.ROTATION_270:
                displayRotation = 180;
                break;
        }
        int sensorOrientation = cameraCharacteristics.get(CameraCharacteristics.SENSOR_ORIENTATION);
        mDisplayRotation = (displayRotation + sensorOrientation + 270) % 360;
        MMLog.d(TAG, "mDisplayRotation: " + mDisplayRotation);
    }

    private void createVideoSession() {
        MMLog.v(TAG, "createVideoSession start...");
        try {
            // video surface
            mRecordSurface = MediaCodec.createPersistentInputSurface();
            mMediaRecorder = createRecorder();

            ArrayList<Surface> sessionSurfaces = new ArrayList<>();
            if (mPreviewSurface != null) sessionSurfaces.add(mPreviewSurface);
            if (mRecordSurface != null) sessionSurfaces.add(mRecordSurface);

            createPreviewRequest();

            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.Q) {
                ArrayList<OutputConfiguration> outputConfigurations = new ArrayList<>();
                for (Surface surface : sessionSurfaces) {
                    if (surface != null) {
                        OutputConfiguration configuration = new OutputConfiguration(surface);
                        outputConfigurations.add(configuration);
                    }
                }
                SessionConfiguration sessionConfiguration = new SessionConfiguration(SessionConfiguration.SESSION_REGULAR, outputConfigurations, new HandlerExecutor(mCameraHandler), mSessionCreateCallback);
                sessionConfiguration.setSessionParameters(mRequestBuilder.build());
                mCameraDevice.createCaptureSession(sessionConfiguration);
            } else {
                mCameraDevice.createCaptureSession(sessionSurfaces, mSessionCreateCallback, mCameraHandler);
            }
        } catch (CameraAccessException e) {
            e.printStackTrace();
        }
    }

    private final CameraCaptureSession.StateCallback mSessionCreateCallback = new CameraCaptureSession.StateCallback() {
        @Override
        public void onConfigured(@NonNull CameraCaptureSession session) {
            mCameraSession = session;
            startPreview();
        }

        @Override
        public void onConfigureFailed(@NonNull CameraCaptureSession session) {
            releaseCamera();
        }
    };

    private void createPreviewRequest() {
        CaptureRequest.Builder builder;
        try {
            builder = mCameraDevice.createCaptureRequest(CameraDevice.TEMPLATE_RECORD);
        } catch (CameraAccessException e) {
            e.printStackTrace();
            MMLog.e(TAG, "setUpPreviewRequest, Camera access failed");
            return;
        }
        if (mPreviewSurface != null) builder.addTarget(mPreviewSurface);
        if (mRecordSurface != null) builder.addTarget(mRecordSurface);
        applyCommonSettings(builder);
        mRequestBuilder = builder;
    }

    private void applyCommonSettings(CaptureRequest.Builder builder) {
        if (builder == null) {
            return;
        }
        builder.set(CaptureRequest.CONTROL_MODE, CaptureRequest.CONTROL_MODE_AUTO);
        builder.set(CaptureRequest.CONTROL_AF_MODE, CameraMetadata.CONTROL_AF_MODE_CONTINUOUS_VIDEO);  // set auto focus mode
        builder.set(CaptureRequest.CONTROL_AWB_MODE, CaptureRequest.CONTROL_AWB_MODE_AUTO); // set auto white balance mode
        builder.set(CaptureRequest.CONTROL_AE_MODE, CaptureRequest.CONTROL_AE_MODE_ON_AUTO_FLASH); // set auto exposure mode
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    private MediaRecorder createRecorder() {
        MediaRecorder mediaRecorder = new MediaRecorder();
        try {
            File tmpFile = configRecorder(mediaRecorder);
            // we will create new file when click record button.
            // so need delete this temporary file
            tmpFile.delete();
        } catch (IOException e) {
            ///e.printStackTrace();
            MMLog.e(TAG, "createRecorder error: " + e.getMessage());
        }
        return mediaRecorder;
    }

    private File configRecorder(@NonNull MediaRecorder mediaRecorder) throws IOException {
        CamcorderProfile mProfile = CamcorderProfile.get(CamcorderProfile.QUALITY_LOW);
        mediaRecorder.reset();
        // Sets the video source to be used for recording
        mediaRecorder.setVideoSource(MediaRecorder.VideoSource.SURFACE);
        // Sets the video encoding bit rate for recording
        mediaRecorder.setVideoEncodingBitRate(mPreviewSize.getWidth() * mPreviewSize.getHeight() * 8);
        // Sets the audio source to be used for recording
        mediaRecorder.setAudioSource(MediaRecorder.AudioSource.MIC);
        // Sets the audio encoding bit rate for recording
        mediaRecorder.setAudioEncodingBitRate(96000);
        // Sets the audio sampling rate for recording
        mediaRecorder.setAudioSamplingRate(44100);
        // Set video frame capture rate
        mediaRecorder.setCaptureRate(30);
        // Sets the orientation hint for output video playback. Values: 0, 90, 180, 270
        mediaRecorder.setOrientationHint(mDisplayRotation);

        // Sets the format of the output file produced during recording
        mediaRecorder.setOutputFormat(MediaRecorder.OutputFormat.MPEG_4);
        // Sets the width and height of the video to be captured
        mediaRecorder.setVideoSize(mPreviewSize.getWidth(), mPreviewSize.getHeight());
        // Sets the frame rate of the video to be captured
        mediaRecorder.setVideoFrameRate(30);
        // Sets the video encoder to be used for recording
        mediaRecorder.setVideoEncoder(MediaRecorder.VideoEncoder.H264);
        // Sets the audio encoder to be used for recording
        mediaRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.AAC);

        mediaRecorder.setAudioChannels(mProfile.audioChannels);
        // a persistent input surface created by MediaCodec.createPersistentInputSurface()
        mediaRecorder.setInputSurface(mRecordSurface);
        mediaRecorder.setMaxFileSize(RECORD_LIMIT_SIZE);
        mediaRecorder.setMaxDuration(0);//disables the duration limit

        mediaRecorder.setPreviewDisplay(mPreviewSurface != null ? mPreviewSurface : null);

        File outputFile = getOutputFile();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            mediaRecorder.setOutputFile(outputFile);
        }
        mediaRecorder.setOnInfoListener(mRecorderOnInfoListener);
        mediaRecorder.setOnErrorListener(mRecorderErrorListener);
        mediaRecorder.prepare();
        mIsRecording = false;
        return outputFile;
    }

    @SuppressLint("SimpleDateFormat")
    private File getOutputFile() {
        File saveDirectory = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DCIM), "CameraRecorder");
        saveDirectory.mkdirs();
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat(DATA_FORMAT_PATTERN);
        String fileName = simpleDateFormat.format(new Date(System.currentTimeMillis())) + ".mp4";
        return new File(saveDirectory, fileName);
    }

    private File getOutputDir() {
        File saveDirectory = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DCIM), "CameraRecorder");
        saveDirectory.mkdirs();
        return saveDirectory;
    }

    public boolean isFrontCamera() {
        int cameraId = Integer.parseInt(mCamera2Config.getCameraId());
        return cameraId == CameraMetadata.LENS_FACING_BACK;
    }

    /////////////////////////////////////////////////////////////////////////////////////////////////////////////
    public void startPreview() {///这里不是预览到UI
        MMLog.v(TAG, "startPreview");
        if (mCameraSession == null || mRequestBuilder == null) {
            MMLog.w(TAG, "startPreview failed. mCaptureSession or mCurrentRequestBuilder is null");
            return;
        }
        try {///继续请求数据
            CaptureRequest captureRequest = mRequestBuilder.build();
            mCameraSession.setRepeatingRequest(captureRequest, null, mCameraHandler);
            mCameraState = CAMERA_STATE_PREVIEW;
        } catch (CameraAccessException e) {
            ///e.printStackTrace();
            MMLog.e(TAG, String.valueOf(e));
        }
    }

    public void startVideoRecording() {
        if (DEBUG) MMLog.d(TAG, "startVideoRecording");
        if (mIsRecording) {
            if (DEBUG) MMLog.d(TAG, "startVideoRecording videoRecord has been start");
            return;
        }

        long availableSpace = getRemainAvailableSpace() - RECORD_LOW_STORAGE_THRESHOLD;
        if (DEBUG) MMLog.d(TAG, "startVideoRecording availableSpace : " + availableSpace);
        if (availableSpace < MIN_REMAIN_AVAILABLE_SPACE) {
            deleteRecordVideoFile();
        }  ///mMaxFileSize = Math.min(availableSpace, RECORD_LIMIT_SIZE);
        startRecorder();
    }

    private void startRecorder() {
        if (mCameraState != CAMERA_STATE_PREVIEW || mMediaRecorder == null || mIsRecording) {
            MMLog.e(TAG, "Start Recorder failed!");
            return;
        }
        try {
            configRecorder(mMediaRecorder);
            mMediaRecorder.start();
            mIsRecording = true;
            MMLog.i(TAG, "startRecorder...");
        } catch (IOException e) {
            MMLog.e(TAG, "startRecorder failed! " + e.getMessage());
        }
    }

    public void stopRecorder() {
        if (mMediaRecorder != null && mIsRecording) {
            MMLog.i(TAG, "stopRecorder...");
            mMediaRecorder.stop();
            mIsRecording = false;
        }
    }

    public void stopPreview() {
        MMLog.v(TAG, "stopPreview");
        if (mCameraSession == null) {
            MMLog.w(TAG, "stopPreview: mCaptureSession is null");
            return;
        }
        try {
            mCameraSession.stopRepeating();
            mCameraState = CAMERA_STATE_OPENED;
        } catch (CameraAccessException e) {
            e.printStackTrace();
        }
    }

    private void releaseRecorder() {
        if (mMediaRecorder == null) {
            return;
        }
        stopRecorder();  // stop if is recording
        mMediaRecorder.reset();
        mMediaRecorder.release();
        mMediaRecorder = null;
    }

    private void closeCameraSession() {
        if (mCameraSession != null) {
            mCameraSession.close();
            mCameraSession = null;
        }
    }

    private void closeCameraDevice() {
        if (mCameraDevice != null) {
            mCameraDevice.close();
            mCameraDevice = null;
        }
    }

    private void stopBackgroundThread() {
        MMLog.v(TAG, "stopBackgroundThread");
        if (mCameraThread != null) {
            mCameraThread.quitSafely();
            try {
                mCameraThread.join();
                mCameraThread = null;
                mCameraHandler = null;
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    public void releaseCamera() {
        if (mCameraState == CAMERA_STATE_CLOSE) {
            MMLog.w(TAG, "camera is closed");
            return;
        }
        mCameraStateLock.lock();
        MMLog.v(TAG, "releaseCamera");
        releaseRecorder();
        stopPreview();
        closeCameraSession();
        closeCameraDevice();
        stopBackgroundThread();
        mCameraState = CAMERA_STATE_CLOSE;
        mCameraStateLock.unlock();
    }

    private static class HandlerExecutor implements Executor {
        private final Handler ihandler;

        public HandlerExecutor(Handler handler) {
            ihandler = handler;
        }

        @Override
        public void execute(Runnable runCmd) {
            ihandler.post(runCmd);
        }
    }


    private final MediaRecorder.OnErrorListener mRecorderErrorListener = new MediaRecorder.OnErrorListener() {
        @Override
        public void onError(MediaRecorder mr, int what, int extra) {
            if (DEBUG) MMLog.d(TAG, "OnErrorListener what : " + what + " extra : " + extra);
            if (MediaRecorder.MEDIA_RECORDER_ERROR_UNKNOWN == what || MEDIA_RECORDER_ENCODER_ERROR == extra) {
                // We may have run out of space on the SD card.
                releaseRecorder();
            }
        }
    };

    private final MediaRecorder.OnInfoListener mRecorderOnInfoListener = new MediaRecorder.OnInfoListener() {
        @Override
        public void onInfo(MediaRecorder mr, int what, int extra) {
            if (DEBUG) MMLog.d(TAG, "OnInfoListener what : " + what + " extra : " + extra);
            switch (what) {
                case MediaRecorder.MEDIA_RECORDER_INFO_MAX_DURATION_REACHED:
                    if (DEBUG) MMLog.d(TAG, "OnInfoListener MAX_DURATION_REACHED");

                    break;
                case MediaRecorder.MEDIA_RECORDER_INFO_MAX_FILESIZE_REACHED:
                    if (DEBUG) MMLog.d(TAG, "OnInfoListener MAX_FILESIZE_REACHED");
                    mHandler.removeCallbacks(saveAndRestartRecordRunnable);
                    mHandler.post(saveAndRestartRecordRunnable);
                    break;
                default:
                    break;
            }
        }
    };

    private final Runnable saveAndRestartRecordRunnable = new Runnable() {
        @Override
        public void run() {
            silentSwitch(true);
            stopRecorder();
            startVideoRecording();
            try {
                Thread.sleep(500);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            silentSwitch(false);
        }
    };

    /**
     * @param mute The required mute state: true for mute ON, false for mute OFF
     */
    private void silentSwitch(boolean mute) {
        if (DEBUG) MMLog.d(TAG, "mStreamSystemMute : " + mStreamSystemMute + " mute : " + mute);
        if (!mStreamSystemMute) {
            mAudioManager.setStreamMute(AudioManager.STREAM_SYSTEM, mute);
        }
    }

    private long deleteRecordVideoFile() {
        long fileSize = 0l;
        File fileDir = getOutputDir();
        if (fileDir.exists() && fileDir.isDirectory()) {
            File[] files = fileDir.listFiles();
            String path = null;
            String beforeTime = null;
            for (File file : Objects.requireNonNull(files)) {
                ///if (file.isFile() && file.getName().contains(OUTPUT_FORMAT))
                {
                    String[] time = file.getName().split("\\.");
                    String afterTime = time[0];
                    if (TextUtils.isEmpty(path)) {
                        path = file.getPath();
                        beforeTime = afterTime;
                    } else {
                        if (compareTwoTime(beforeTime, afterTime)) {
                            path = file.getPath();
                            beforeTime = afterTime;
                        }
                    }
                }
                if (file.length() == 0) file.delete();
            }
            if (DEBUG) MMLog.d(TAG, "path : " + path);
            if (null != path) {
                File file = new File(path);
                if (file.exists()) {
                    try {
                        FileInputStream fis = new FileInputStream(file);
                        fileSize = fis.available();
                    } catch (Exception e) {
                        if (DEBUG) MMLog.d(TAG, "delRecordVideoFile exception : " + e);
                    }
                    file.delete();
                } else {
                    if (DEBUG) MMLog.d(TAG, path + " is not exists");
                }
            }
        }
        if (DEBUG) MMLog.d(TAG, "delRecordVideoFile fileSize : " + fileSize);
        return fileSize;
    }

    @SuppressLint("SimpleDateFormat")
    private boolean compareTwoTime(String beforeTime, String afterString) {
        boolean large = false;
        SimpleDateFormat dateFormat = new SimpleDateFormat(DATA_FORMAT_PATTERN);
        try {
            Date bParse = dateFormat.parse(beforeTime);
            Date aParse1 = dateFormat.parse(afterString);
            long diff = bParse.getTime() - aParse1.getTime();
            large = diff >= 0;
        } catch (ParseException e) {
            if (DEBUG) MMLog.e(TAG, "compareTwoTime exception : " + e);
        }
        return large;
    }

    private long getRemainAvailableSpace() {
        File dir = getOutputDir();
        boolean isDirectory = dir.isDirectory();
        boolean canWrite = dir.canWrite();

        if (!isDirectory || !canWrite) {
            if (DEBUG) MMLog.d(TAG, "getAvailableSpace() isDirectory=" + isDirectory + ", canWrite=" + canWrite);
            return 0;
        }

        try {
            // Here just use one directory to stat fs.
            StatFs stat = new StatFs(dir.getAbsolutePath());
            return stat.getAvailableBlocks() * (long) stat.getBlockSize();
        } catch (Exception e) {
            if (DEBUG) MMLog.e(TAG, "Fail to access external storage " + e);
        }
        return 0;
    }
}
