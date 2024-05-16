package com.zhuchao.android.session.recorder;

import android.content.Context;
import android.content.res.Configuration;
import android.hardware.Camera;
import android.media.AudioManager;
import android.media.CamcorderProfile;
import android.media.MediaRecorder;
import android.os.Environment;
import android.os.Handler;
import android.os.StatFs;
import android.os.storage.StorageManager;
import android.text.TextUtils;
import android.util.Log;
import android.view.SurfaceHolder;
import android.widget.Toast;

import com.zhuchao.android.fbase.MMLog;

import java.io.File;
import java.io.FileInputStream;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

public class RecordVideoMode {
    private static final String TAG = "RecordVideoMode";
    private static final boolean DEBUG = true;
    private static final long RECORD_LOW_STORAGE_THRESHOLD = 50 * 1024 * 1024L; //50M
    private static final long RECORD_LIMIT_SIZE = 1024 * 1024 * 1024L; //1G
    private static final long MIN_REMAIN_AVAILABLE_SPACE = 1024L; //1kb
    private static final long UNAVAILABLE = -1L;
    private static final long PREPARING = -2L;
    private static final long UNKNOWN_SIZE = -3L;
    private static final long FULL_SDCARD = -4L;
    private static final String FOLDER_NAME = "/RecordVideo";
    private static final String OUTPUT_FORMAT = ".3gp";
    protected static final int MEDIA_RECORDER_INFO_BITRATE_ADJUSTED = 898;
    protected static final int MEDIA_RECORDER_INFO_RECORDING_SIZE = 895;
    protected static final int MEDIA_RECORDER_INFO_FPS_ADJUSTED = 897;
    protected static final int MEDIA_RECORDER_INFO_START_TIMER = 1998;
    protected static final int MEDIA_RECORDER_INFO_WRITE_SLOW = 899;
    protected static final int MEDIA_RECORDER_INFO_CAMERA_RELEASE = 1999;
    protected static final int MEDIA_RECORDER_ENCODER_ERROR = -1103;
    private static final String RECORDER_INFO_SUFFIX = "media-recorder-info=";
    private static final String DATA_FORMAT_PATTERN = "yyyyMMdd_HHmmss";
    private static String sMountPoint;
    private Context mContext;
    private MediaRecorder mMediaRecorder;
    private Camera mCamera;
    protected boolean mIsRecorderCameraReleased = true;
    private SurfaceHolder mSurfaceHolder;
    private Toast mToast;
    private boolean mRelease = false;
    private boolean mStartRecording = false;
    private int mCameraId = 0;
    private StorageManager mStorageManager;
    private String mFileDir;
    private long mMaxFileSize;
    protected String mRecordFilename;
    protected Handler mHandler;
    private boolean mRecordVideoMode = false;
    private AudioManager mAudioManager;
    private boolean mStreamSystemMute = false;

    public RecordVideoMode(Context context, SurfaceHolder surfaceHolder) {
        mContext = context;
        mSurfaceHolder = surfaceHolder;
        mStorageManager = (StorageManager) mContext.getSystemService(Context.STORAGE_SERVICE);
        sMountPoint = Environment.getExternalStorageDirectory().getPath();
        mFileDir = getFileDirectory();
        mHandler = new Handler(mContext.getMainLooper());
        mAudioManager = (AudioManager) mContext.getSystemService(Context.AUDIO_SERVICE);
        mStreamSystemMute = mAudioManager.isStreamMute(AudioManager.STREAM_SYSTEM);
    }

    public void updateSurfaceHolder(SurfaceHolder surfaceHolder) {
        mSurfaceHolder = surfaceHolder;
    }

    public void startVideoRecording() {
        if (DEBUG) MMLog.d(TAG, "startVideoRecording");
        if (mRecordVideoMode) {
            if (DEBUG) MMLog.d(TAG, "startVideoRecording videoRecord has been start");
            return;
        }

        if (mStartRecording) {
            if (DEBUG) MMLog.d(TAG, "startVideoRecording videoRecord is opening");
            showToast("videoRecord is opening");
            return;
        }

        long availableSpace = getRemainAvailableSpace() - RECORD_LOW_STORAGE_THRESHOLD;
        if (DEBUG) MMLog.d(TAG, "startVideoRecording availableSpace : " + availableSpace);
        if (availableSpace < MIN_REMAIN_AVAILABLE_SPACE) {
            mMaxFileSize = delRecordVideoFile();
            if (mMaxFileSize <= 0) {
                showToast("Not enough space");
                return;
            }
        }
        else
        {
            mMaxFileSize = Math.min(availableSpace, RECORD_LIMIT_SIZE);
        }

        mStartRecording = true;
        if (null == mCamera) {
            if (DEBUG) MMLog.d(TAG, "startVideoRecording mCamera open");
            try {
                int numCameras = Camera.getNumberOfCameras();
                if (DEBUG) MMLog.d(TAG, "startVideoRecording numCameras : " + numCameras);
                if (numCameras > 1) {
                    mCamera = Camera.open(Camera.CameraInfo.CAMERA_FACING_BACK);
                } else if (numCameras == 1) {
                    mCamera = Camera.open();
                } else {
                    if (DEBUG) Log.i(TAG, "startVideoRecording numberOfCameras : " + numCameras);
                    showToast("Camera not supported");
                    mStartRecording = false;
                    return;
                }

            } catch (Exception e) {
                Log.e(TAG, "Camera open failed, exception : " + e);
                showToast("Camera open failed");
            }
        }

        if (null == mCamera) {
            if (DEBUG) Log.d(TAG, "startVideoRecording mCamera is null");
            mStartRecording = false;
            return;
        }


        Camera.Parameters parameters = mCamera.getParameters();
        List<String> focusModesList = parameters.getSupportedFocusModes();

        if (focusModesList.contains(Camera.Parameters.FOCUS_MODE_CONTINUOUS_VIDEO)) {
            parameters.setFocusMode(Camera.Parameters.FOCUS_MODE_CONTINUOUS_VIDEO);
        } else if (focusModesList.contains(Camera.Parameters.FOCUS_MODE_AUTO)) {
            parameters.setFocusMode(Camera.Parameters.FOCUS_MODE_AUTO);
        }

        mCamera.setParameters(parameters);
        mCamera.setDisplayOrientation(90);
        if (mMediaRecorder == null) {
            mMediaRecorder = new MediaRecorder();
        } else {
            try {
                mMediaRecorder.reset();
            } catch (Exception e) {
                if (DEBUG) Log.e(TAG, "MediaRecorder reset exception : " + e);
            }
        }
        mCamera.stopPreview();
        mCamera.unlock();
        synchronized (mMediaRecorder) {
            mMediaRecorder.setCamera(mCamera);

            CamcorderProfile mProfile = CamcorderProfile.get(CamcorderProfile.QUALITY_LOW);
            mMediaRecorder.setAudioSource(MediaRecorder.AudioSource.CAMCORDER);
            mMediaRecorder.setVideoSource(MediaRecorder.VideoSource.CAMERA);
            mMediaRecorder.setOutputFormat(mProfile.fileFormat);
            mMediaRecorder.setVideoEncodingBitRate(mProfile.videoBitRate);
            mMediaRecorder.setVideoEncoder(mProfile.videoCodec);
            mMediaRecorder.setVideoSize(mProfile.videoFrameWidth, mProfile.videoFrameHeight);
            mMediaRecorder.setVideoFrameRate(mProfile.videoFrameRate);
            mMediaRecorder.setAudioEncodingBitRate(mProfile.audioBitRate);
            mMediaRecorder.setAudioChannels(mProfile.audioChannels);
            mMediaRecorder.setAudioSamplingRate(mProfile.audioSampleRate);
            mMediaRecorder.setAudioEncoder(mProfile.audioCodec);
            mMediaRecorder.setMaxDuration(0);//disables the duration limit

            try {
                mMediaRecorder.setMaxFileSize(mMaxFileSize);
            } catch (RuntimeException exception) {
                if (DEBUG) Log.e(TAG, "startVideoRecording()", exception);
            }
            mMediaRecorder.setPreviewDisplay(mSurfaceHolder != null ? mSurfaceHolder.getSurface() : null);
            mRecordFilename = getRecordVideoName();
            mMediaRecorder.setOutputFile(mRecordFilename);
            setMediaRecorderParameters(mMediaRecorder);
            int orientation = mContext.getResources().getConfiguration().orientation;
            mMediaRecorder.setOrientationHint(getRecordingRotation(orientation, mCameraId));

            mIsRecorderCameraReleased = false;
            try {
                mMediaRecorder.prepare();
                mMediaRecorder.start();
            } catch (Exception e) {
                if (DEBUG) Log.e(TAG, "startVideoRecording exception " + e);
                showToast("MediaRecorder start failed");
                releaseVideoRecorder();
                mStartRecording = false;
                return;
            }
            mRecordVideoMode = true;
            mMediaRecorder.setOnErrorListener(mRecorderErrorListener);
            mMediaRecorder.setOnInfoListener(mRecorderOnInfoListener);
            ///mMediaRecorder.setOnCameraReleasedListener(mRecorderOnInfoListener);
            mStartRecording = false;
        }
    }

    public void releaseVideoRecorder() {
        if (DEBUG) Log.d(TAG, "releaseMediaRecorder mIsRecorderCameraReleased : " + mIsRecorderCameraReleased);
        mHandler.removeCallbacks(saveRecordRunnable);
        if (mIsRecorderCameraReleased) {
            if (DEBUG) Log.d(TAG, "releaseMediaRecorder return when camera&mediaRecorder has been released");
            return;
        }

        if (mRelease) {
            if (DEBUG) Log.d(TAG, "releaseMediaRecorder Return when MediaRecorder is being released");
            return;
        }

        if (mMediaRecorder != null) {
            mRelease = true;
            cleanupEmptyFile();
            synchronized (mMediaRecorder) {
                try {
                    if (mRecordVideoMode) {
                        stopRecording();
                    }
                    mMediaRecorder.reset();
                    mMediaRecorder.release();
                    if (null != mCamera) {
                        mCamera.release();
                    }
                } catch (Exception e) {
                    if (DEBUG) Log.e(TAG, "releaseMediaRecorder exception : " + e);
                }
                mIsRecorderCameraReleased = true;
                mMediaRecorder.setOnInfoListener(null);
                mMediaRecorder.setOnErrorListener(null);
                mRelease = false;
                mMediaRecorder = null;
                mCamera = null;
            }
        }
        mStartRecording = false;
    }

    private long getRemainAvailableSpace() {
        String state;
        if (null != mStorageManager) {
            state = "";///mStorageManager.getVolumeState(sMountPoint);
        } else {
            state = null;
        }

        if (TextUtils.isEmpty(state)) {
            return UNAVAILABLE;
        }
        if (Environment.MEDIA_CHECKING.equals(state)) {
            return PREPARING;
        }
        if (!Environment.MEDIA_MOUNTED.equals(state)) {
            return UNAVAILABLE;
        }

        File dir = new File(mFileDir);
        if (!dir.exists()) {
            dir.mkdir();
        }
        boolean isDirectory = dir.isDirectory();
        boolean canWrite = dir.canWrite();
        if (!isDirectory || !canWrite) {
            if (DEBUG) Log.d(TAG, "getAvailableSpace() isDirectory=" + isDirectory + ", canWrite=" + canWrite);
            return FULL_SDCARD;
        }

        try {
            // Here just use one directory to stat fs.
            StatFs stat = new StatFs(mFileDir);
            return stat.getAvailableBlocks() * (long) stat.getBlockSize();
        } catch (Exception e) {
            if (DEBUG) Log.e(TAG, "Fail to access external storage", e);
        }
        return UNKNOWN_SIZE;
    }


    public static String getFileDirectory() {
        if (DEBUG) Log.d(TAG, "getFileDirectory sMountPoint : " + sMountPoint);
        if (TextUtils.isEmpty(sMountPoint)) {
            sMountPoint = "/sdcard";
        }
        String path = sMountPoint + FOLDER_NAME;
        File dir = new File(path);
        if (!dir.exists()) {
            dir.mkdir();
        }
        if (DEBUG) Log.d(TAG, "getFileDirectory path : " + path);
        return path;
    }

    private String getRecordVideoName() {
        String nameStr;
        SimpleDateFormat mFormat = new SimpleDateFormat(DATA_FORMAT_PATTERN);
        Date date = new Date();
        nameStr = mFileDir + File.separator + mFormat.format(date) + OUTPUT_FORMAT;
        if (DEBUG) Log.d(TAG, "getRecordVideoName name : " + nameStr);
        return nameStr;
    }

    private MediaRecorder.OnErrorListener mRecorderErrorListener = new MediaRecorder.OnErrorListener() {
        @Override
        public void onError(MediaRecorder mr, int what, int extra) {
            if (DEBUG) Log.d(TAG, "OnErrorListener what : " + what + " extra : " + extra);
            if (MediaRecorder.MEDIA_RECORDER_ERROR_UNKNOWN == what || MEDIA_RECORDER_ENCODER_ERROR == extra) {
                // We may have run out of space on the SD card.
                releaseVideoRecorder();
            }
        }
    };

    private MediaRecorder.OnInfoListener mRecorderOnInfoListener = new MediaRecorder.OnInfoListener() {
        @Override
        public void onInfo(MediaRecorder mr, int what, int extra) {
            if (DEBUG) Log.d(TAG, "OnInfoListener what : " + what + " extra : " + extra);
            switch (what) {
                case MediaRecorder.MEDIA_RECORDER_INFO_MAX_DURATION_REACHED:
                    if (DEBUG) Log.d(TAG, "OnInfoListener MAX_DURATION_REACHED");
                    if (!mIsRecorderCameraReleased && !mStartRecording && !mRelease) {
                        mHandler.removeCallbacks(saveRecordRunnable);
                        mHandler.post(saveRecordRunnable);
                    }
                    break;
                case MediaRecorder.MEDIA_RECORDER_INFO_MAX_FILESIZE_REACHED:
                    if (DEBUG) Log.d(TAG, "OnInfoListener MAX_FILESIZE_REACHED");
                    if (!mIsRecorderCameraReleased && !mStartRecording && !mRelease) {
                        mHandler.removeCallbacks(saveRecordRunnable);
                        mHandler.post(saveRecordRunnable);
                    }
                    break;
                case MEDIA_RECORDER_INFO_CAMERA_RELEASE:
                    break;
                case MEDIA_RECORDER_INFO_START_TIMER:
                    break;
                case MEDIA_RECORDER_INFO_FPS_ADJUSTED:
                case MEDIA_RECORDER_INFO_BITRATE_ADJUSTED:
                    break;
                case MEDIA_RECORDER_INFO_WRITE_SLOW:
                    if (DEBUG) Log.d(TAG, "OnInfoListener MEDIA_RECORDER_INFO_WRITE_SLOW");
                    if (!mIsRecorderCameraReleased && !mStartRecording && !mRelease) {
                        mHandler.removeCallbacks(saveRecordRunnable);
                        mHandler.post(saveRecordRunnable);
                    }
                    break;
                case MEDIA_RECORDER_INFO_RECORDING_SIZE:
                    break;
                default:
                    break;
            }
        }
    };

    private void setMediaRecorderParameters(MediaRecorder mediaRecorder) {
        /*try {
            mediaRecorder.setParametersExtra(RECORDER_INFO_SUFFIX + MEDIA_RECORDER_INFO_BITRATE_ADJUSTED);
            mediaRecorder.setParametersExtra(RECORDER_INFO_SUFFIX + MEDIA_RECORDER_INFO_FPS_ADJUSTED);
            mediaRecorder.setParametersExtra(RECORDER_INFO_SUFFIX + MEDIA_RECORDER_INFO_START_TIMER);
            mediaRecorder.setParametersExtra(RECORDER_INFO_SUFFIX + MEDIA_RECORDER_INFO_WRITE_SLOW);
            mediaRecorder.setParametersExtra(RECORDER_INFO_SUFFIX + MEDIA_RECORDER_INFO_CAMERA_RELEASE);
        } catch (Exception ex) {
            ex.printStackTrace();
        }*/
    }

    public void showToast(String text) {
        if (null != mToast) {
            mToast.cancel();
        }
        mToast = Toast.makeText(mContext, text, Toast.LENGTH_SHORT);
        mToast.show();
    }

    public static int getRecordingRotation(int orientation, int cameraId) {
        int rotation = 90;
        if (DEBUG) Log.d(TAG, "getRecordingRotation orientation : " + orientation);
        boolean backCamera = cameraId == Camera.CameraInfo.CAMERA_FACING_BACK;
        if (orientation == Configuration.ORIENTATION_LANDSCAPE) {
            if (backCamera) {
                rotation = 0;
            } else {
                rotation = 180;
            }
        } else if (orientation == Configuration.ORIENTATION_PORTRAIT) {
            if (backCamera) {
                rotation = 90;
            } else {
                rotation = 270;
            }
        }
        if (DEBUG) Log.d(TAG, "getRecordingRotation rotation : " + rotation);
        return rotation;
    }

    private long delRecordVideoFile() {
        long fileSize = 0l;
        File fileDir = new File(mFileDir);
        if (fileDir.exists() && fileDir.isDirectory()) {
            File[] files = fileDir.listFiles();
            String path = null;
            String beforeTime = null;
            for (File file : files) {
                if (file.isFile() && file.getName().contains(OUTPUT_FORMAT)) {
                    String time[] = file.getName().split("\\.");
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
            }
            if (DEBUG) Log.d(TAG, "path : " + path);
            if (null != path) {
                File file = new File(path);
                if (file.exists()) {
                    try {
                        FileInputStream fis = new FileInputStream(file);
                        fileSize = fis.available();
                    } catch (Exception e) {
                        if (DEBUG) Log.d(TAG, "delRecordVideoFile exception : " + e);
                    }
                    file.delete();
                } else {
                    if (DEBUG) Log.d(TAG, path + " is not exists");
                }
            }
        }
        if (DEBUG) Log.d(TAG, "delRecordVideoFile fileSize : " + fileSize);
        return fileSize;
    }

    private boolean compareTwoTime(String beforeTime, String afterString) {
        boolean large = false;
        SimpleDateFormat dateFormat = new SimpleDateFormat(DATA_FORMAT_PATTERN);
        try {
            Date bParse = dateFormat.parse(beforeTime);
            Date aParse1 = dateFormat.parse(afterString);
            long diff = bParse.getTime() - aParse1.getTime();
            if (diff >= 0) {
                large = true;
            } else {
                large = false;
            }
        } catch (ParseException e) {
            if (DEBUG) Log.e(TAG, "compareTwoTime exception : " + e);
        }
        return large;
    }

    private void cleanupEmptyFile() {
        if (mRecordFilename != null) {
            File f = new File(mRecordFilename);
            if (f.length() == 0 && f.delete()) {
                if (DEBUG) Log.d(TAG, "cleanupEmptyFile Empty video file deleted: " + mRecordFilename);
                mRecordFilename = null;
            }
        }
    }

    public void stopRecording() {
        if (DEBUG) Log.i(TAG, "stopRecording");
        if (mStartRecording) {
            if (DEBUG) Log.i(TAG, "stopRecording return because of mStartRecording is true");
            return;
        }
        if (mIsRecorderCameraReleased) {
            if (DEBUG) Log.i(TAG, "stopRecording MediaRecord not init return");
            return;
        }
        if (!mRecordVideoMode) {
            if (DEBUG) Log.i(TAG, "stopRecording return because mRecordVideoMode has false");
            return;
        }
        mRecordVideoMode = false;
        synchronized (mMediaRecorder) {
            mMediaRecorder.setOnInfoListener(null);
            mMediaRecorder.setOnErrorListener(null);
            ///mMediaRecorder.setOnCameraReleasedListener(null);
            try {
                mMediaRecorder.stop();
            } catch (Exception e) {
                if (DEBUG) Log.e(TAG, "stopRecording exception : " + e);
            }
        }
    }

    private Runnable saveRecordRunnable = new Runnable() {
        @Override
        public void run() {
            silentSwitch(true);
            stopRecording();
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
        if (DEBUG) Log.d(TAG, "mStreamSystemMute : " + mStreamSystemMute + " mute : " + mute);
        if (!mStreamSystemMute) {
            mAudioManager.setStreamMute(AudioManager.STREAM_SYSTEM, mute);
        }
    }

}
