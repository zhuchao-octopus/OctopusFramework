package com.rockchip.car.recorder.model;

import android.graphics.SurfaceTexture;
import android.media.CamcorderProfile;
import android.media.MediaRecorder;

import com.rockchip.car.recorder.camera2.CameraManager;
import com.rockchip.car.recorder.service.CameraService;
import com.rockchip.car.recorder.service.IService;

/**
 * Created by Administrator on 2016/7/15.
 */
public class CameraInfo {
    private CameraManager.CameraProxy mCamera;
    private MediaRecorder mMediaRecorder;
    private CamcorderProfile mProfile;
    private int mCameraId;
    private String mCameraState;
    private int mCameraStateId;
    private String mCameraType;
    private int mFacing;

    private boolean mIsRecording;

    private CameraService.CameraPreviewCallback mCameraPreviewCallback;

    private SurfaceTexture mSurfaceTexture;

    public CameraInfo(CameraManager.CameraProxy camera, int cameraId) {
        this.mCamera = camera;
        this.mCameraId = cameraId;
        mSurfaceTexture = new SurfaceTexture(1000 + cameraId);
    }

    public void setCameraStatus(String status) {
        this.mCameraState = status;
        switch (status) {
            case IService.CAMERA_ERROR:
                this.mCameraStateId = IService.I_CAMERA_ERROR;
                break;
            case IService.CAMERA_IDLE:
                this.mCameraStateId = IService.I_CAMERA_IDLE;
                break;
            case IService.CAMERA_OPENED:
                this.mCameraStateId = IService.I_CAMERA_OPENED;
                break;
            case IService.CAMERA_PREVIEWING:
                this.mCameraStateId = IService.I_CAMERA_PREVIEWING;
                break;
            case IService.CAMERA_RECORDING:
                this.mCameraStateId = IService.I_CAMERA_RECORDING;
                break;
            default:
                break;
        }
    }

    public String getCameraState() {
        return mCameraState;
    }

    public int getCameraStateId() {
        return mCameraStateId;
    }

    public void setCameraType(String cameraType) {
        this.mCameraType = cameraType;
    }

    public String getCameraType() {
        return mCameraType;
    }

    public void setCameraFacing(int facing) {
        this.mFacing = facing;
    }

    public int getCameraFacing() {
        return mFacing;
    }

    public void setRecording(boolean recording) {
        this.mIsRecording = recording;
    }

    public boolean isRecording() {
        return mIsRecording;
    }

    public CameraManager.CameraProxy getCameraManager() {
        return mCamera;
    }

    public void setMediaRecorder(MediaRecorder recorder) {
        if (this.mMediaRecorder != null) {
            this.mMediaRecorder.release();
            this.mMediaRecorder = null;
        }
        this.mMediaRecorder = recorder;
    }

    public MediaRecorder getMediaRecorder() {
        if (mMediaRecorder == null) {
            mMediaRecorder = new MediaRecorder();
        }
        return mMediaRecorder;
    }

    public void startRecord() {
        getMediaRecorder().start();
        this.mIsRecording = true;
    }

    public void stopRecord() {
        getMediaRecorder().stop();
        this.mIsRecording = false;
    }

    public void setProfile(CamcorderProfile profile) {
        this.mProfile = profile;
    }

    public int getCameraId() {
        return mCameraId;
    }

    public SurfaceTexture getSurfaceTexture() {
        return mSurfaceTexture;
    }

    public void setSurfaceTexture(SurfaceTexture texture) {
        this.mSurfaceTexture = texture;
    }

    public CameraService.CameraPreviewCallback getCameraPreviewCallback() {
        return mCameraPreviewCallback;
    }

    public void setCameraPreviewCallback(CameraService.CameraPreviewCallback mCameraPreviewCallback) {
        this.mCameraPreviewCallback = mCameraPreviewCallback;
    }
}
