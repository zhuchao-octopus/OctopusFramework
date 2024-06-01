package com.rockchip.car.recorder.service;

import android.content.BroadcastReceiver;
import android.content.ContentResolver;
import android.graphics.Bitmap;
import android.graphics.SurfaceTexture;
import android.hardware.Camera;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.location.LocationProvider;
import android.media.CamcorderProfile;
import android.media.MediaRecorder;
import android.os.Binder;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.Process;
import android.view.OrientationEventListener;
import android.view.SurfaceHolder;

import com.rockchip.car.recorder.camera2.CameraHolder;
import com.rockchip.car.recorder.camera2.CameraManager;
import com.rockchip.car.recorder.model.CameraInfo;
import com.rockchip.car.recorder.utils.SLog;

import java.io.File;
import java.io.IOException;
import java.lang.ref.WeakReference;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Created by Administrator on 2016/7/15.
 */
public abstract class ServiceImpl implements IService {

    private static final String TAG = "CAM_ServiceImpl";

    //================ MSG ================
    private static final int MSG_UPDATE_RECORD_TIME = 0x00000001;
    private static final int MSG_UPDATE_RECORD_ICON = 0x00000002;
    private static final int MSG_USB_HOTPLUG_ADD = 0x00000003;
    private static final int MSG_USB_HOTPLUG_REMOVE = 0x00000004;

    private int mOrientation = OrientationEventListener.ORIENTATION_UNKNOWN;
    protected static Map<Integer, CameraInfo> mCameraInfos;

    private CameraBinder mCameraBinder;

    private boolean mAllowedTakePictureOnRecording = true;

    protected CameraCallback mCameraCallback;

    private ContentResolver mContentResolver;

    private LocationManager mLocationManager;

    private Map<Integer, Integer> mSurfaceToCamera;
    private Map<Integer, Integer> mCameraToSurface;

    private boolean mTakePicture;
    private long mStartRecordTime;

    private boolean mNeedScanFile = true;

    public ServiceImpl() {
        if (mCameraInfos == null) {
            mCameraInfos = new ConcurrentHashMap<>();
        }
    }

    public Map<Integer, CameraInfo> getCameraInfos() {
        return mCameraInfos;
    }

    public Camera getCamera(int id) {
        if (mCameraInfos != null && mCameraInfos.get(id) != null && mCameraInfos.get(id).getCameraManager() != null) {
            return mCameraInfos.get(id).getCameraManager().getCamera();
        }
        return null;
    }

    public CameraManager.CameraProxy getCameraManager(int id) {
        if (mCameraInfos != null && mCameraInfos.get(id) != null) {
            return mCameraInfos.get(id).getCameraManager();
        }
        return null;
    }

    public void onCreate(ContentResolver c) {
        mCameraBinder = new CameraBinder(this);
        mContentResolver = c;
        mSurfaceToCamera = new HashMap<>();
        mCameraToSurface = new HashMap<>();
        registerExternalStorageListener();
        //        CameraEvent.UsbHotPlugReceiver.getInstance(this).register();
        initLocationManager();
        //        CameraSettings.setObserver(this);
    }

    //    @Override
    //    public IBinder onBind(Intent intent) {
    //        SLog.d(TAG, "ServiceImpl::onBind.");
    //        return mCameraBinder;
    //    }
    //
    //    @Override
    //    public void onRebind(Intent intent) {
    //        super.onRebind(intent);
    //        SLog.d(TAG, "ServiceImpl::onRebind.");
    //    }

    private Handler mMainHandler = new Handler() {

        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case MSG_UPDATE_RECORD_TIME:

                    break;
                case MSG_UPDATE_RECORD_ICON:
                    mCameraCallback.updateRecordIcon(isRecording(0), false);
                    break;

                case MSG_USB_HOTPLUG_ADD:
                    execUsbHotplug(true);
                    break;

                case MSG_USB_HOTPLUG_REMOVE:
                    execUsbHotplug(false);
                    break;
                default:
                    break;
            }
        }
    };

    @Override
    public int getNumberOfCameras() {
        return CameraHolder.instance().getNumberOfCameras();
    }

    public abstract void afterOpenedBeforePreview();

    public abstract void afterPreviewedBeforeRecord();

    @Override
    public boolean[] open() {
        int numberOfCameras = getNumberOfCameras();
        boolean result[] = new boolean[numberOfCameras];
        for (int i = 0; i < numberOfCameras; i++) {
            result[i] = open(i);
        }
        return result;
    }

    @Override
    public boolean open(int id) {
        SLog.d(TAG, "ServiceImpl::open(). id:" + id + "; number:" + getNumberOfCameras());
        if (id >= getNumberOfCameras()) {
            return false;
        }
        if (mCameraInfos.get(id) != null && mCameraInfos.get(id).getCameraStateId() >= I_CAMERA_OPENED) return true;
        CameraManager.CameraProxy proxy = CameraHolder.instance().open(mMainHandler, id, mCameraOpenErrorCallback);
        CameraInfo infos = new CameraInfo(proxy, id);
        if (proxy == null) {
            //            infos.setCameraStatus(CAMERA_ERROR);
            //            mCameraInfos.put(id, infos);
            return false;
        } else {
            try {
                //                proxy.setErrorCallback(new CameraEvent.CameraErrorCallback(id, this));
                infos.setCameraStatus(CAMERA_OPENED);
                infos.setCameraType(proxy.getParameters().get("camera-type"));
                Camera.CameraInfo info = new Camera.CameraInfo();
                Camera.getCameraInfo(id, info);
                infos.setCameraFacing(info.facing);
                SLog.d(TAG, "ServiceImpl::open. Camera " + id + " is " + proxy.getParameters().get("camera-type") + "; FACING is " + info.facing);
                mCameraInfos.put(id, infos);
                return true;
            } catch (RuntimeException e) {
                mIsUsbHotpluging = false;
                return false;
            }
        }
    }

    @Override
    public boolean[] startPreview() {
        if (mCameraInfos == null) {
            throw new RuntimeException("CameraDevices is null");
        } else if (mCameraInfos.size() == 0) {
            throw new RuntimeException("CameraDevices is zero");
        }
        int id;
        boolean result[] = {false, false, false};
        for (CameraInfo info : mCameraInfos.values()) {
            id = info.getCameraId();
            if (info.getCameraStateId() < IService.I_CAMERA_PREVIEWING) {
                result[id] = startPreview(id);
            } else {
                SLog.w(TAG, "Camera " + id + " can't start preview, it is previewing!");
            }
        }
        return result;
    }

    @Override
    public boolean startPreview(int id) {
        return startPreview(id, true, false, null, null);
    }

    public boolean startPreview(int id, boolean render, boolean record, SurfaceTexture texture, SurfaceHolder holder) {

        SLog.d(TAG, "ServiceImpl::startPreview. id:" + id + "; render:" + render + "; recorder:" + record);
        if (mCameraInfos != null && mCameraInfos.get(id) != null && mCameraInfos.get(id).getCameraManager() != null) {
            if (!ParametersSet.initParameters(id)) {
                SLog.w(TAG, "ServiceImpl::startPreview. Camera " + id + " initParameters error!");
                return false;
            }
            if (mCameraInfos.get(id).getCameraStateId() > 1 && !(mTakePicture && mCameraInfos.get(id).getCameraStateId() == 2)) {
                SLog.w(TAG, "ServiceImpl::startPreview(" + id + ", CameraStartPreviewCallback) - it's already Previewing");
                return true;
            }
            Camera.Parameters parameters = mCameraInfos.get(id).getCameraManager().getParameters();
            SLog.d(TAG, "ServiceImpl::startPreview. id:" + id + "; previewSize:" + parameters.getPreviewSize().width + "x" + parameters.getPreviewSize().height);
            if (render) {
                try {
                    if (!mTakePicture) {
                        mCameraInfos.get(id).getCameraManager().setPreviewTexture(mCameraInfos.get(id).getSurfaceTexture());
                    }
                    setPreviewCallback(id);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            } else {
                if (!mTakePicture) {
                    if (texture != null) {
                        mCameraInfos.get(id).getCameraManager().setPreviewTexture(texture);
                        mCameraInfos.get(id).setSurfaceTexture(texture);
                    } else if (holder != null) {
                        mCameraInfos.get(id).getCameraManager().setPreviewDisplay(holder);
                    } else {
                        if (mCameraInfos.get(id).getSurfaceTexture() == null) {
                            throw new RuntimeException("startPreview with null preview window");
                        }
                        mCameraInfos.get(id).getCameraManager().setPreviewTexture(mCameraInfos.get(id).getSurfaceTexture());
                    }
                }
            }

            mCameraInfos.get(id).getCameraManager().startPreview(mMainHandler, mCameraStartPreivewCallback, record);
            /*if (!mTakePicture) {
                mCameraInfos.get(id).setCameraStatus(CAMERA_PREVIEWING);
            }*/
            mTakePicture = false;
            return true;
        }
        SLog.w(TAG, "ServiceImpl::startPreview. mCameraInfos.get(" + id + ") is " + (mCameraInfos.get(id) == null ? "null" : "not null"));
        return false;
    }

    public <T> boolean startPreview(int id, boolean render, boolean record, T t) {
        return startPreview(id, render, record, t instanceof SurfaceTexture ? (SurfaceTexture) t : null, t instanceof SurfaceHolder ? (SurfaceHolder) t : null);
    }

    public abstract void setPreviewCallback(int id);

    @Override
    public <T> boolean startPreviewDirect(int id, T t) {
        SLog.d(TAG, "ServiceImpl::startPreviewAnyway. id:" + id + "; Cameras:" + getNumberOfCameras());
        if (id >= getNumberOfCameras()) {
            SLog.e(TAG, "ServiceImpl::startPreviewAnyway. Unknown camera ID, can't start record!");
            return false;
        }
        if (mCameraInfos.get(id) == null || mCameraInfos.get(id).getCameraStateId() < I_CAMERA_OPENED) {
            CameraManager.CameraProxy proxy = CameraHolder.instance().open(mMainHandler, id, mCameraOpenErrorCallback, true, false, t);
            CameraInfo infos = new CameraInfo(proxy, id);
            if (proxy == null) {
                //                infos.setCameraStatus(CAMERA_ERROR);
                //                mCameraInfos.put(id, infos);
                return false;
            } else {
                try {
                    //                    proxy.setErrorCallback(new CameraEvent.CameraErrorCallback(id, this));
                    infos.setCameraStatus(CAMERA_OPENED);
                    infos.setCameraType(proxy.getParameters().get("camera-type"));
                    Camera.CameraInfo info = new Camera.CameraInfo();
                    Camera.getCameraInfo(id, info);
                    infos.setCameraFacing(info.facing);
                    SLog.d(TAG, "ServiceImpl::open. Camera " + id + " is " + proxy.getParameters().get("camera-type") + "; FACING is " + info.facing);
                    mCameraInfos.put(id, infos);
                    afterOpenedBeforePreview();
                    startPreview(id, t == null ? true : false, false, t);
                    return true;
                } catch (RuntimeException e) {
                    mIsUsbHotpluging = false;
                    return false;
                }
            }
        } else if (mCameraInfos.get(id).getCameraStateId() < I_CAMERA_PREVIEWING) {
            return startPreview(id);
        }
        return true;
    }

    @Override
    public <T> boolean startRecordingDirect(int id, T t) {
        SLog.d(TAG, "ServiceImpl::startRecordingAnyway. id:" + id + "; Thread:" + Process.myTid());
        if (id >= getNumberOfCameras()) {
            SLog.e(TAG, "ServiceImpl::startRecordingDirect. Unknown camera ID, can't start record!");
            return false;
        }
        if (mCameraInfos.get(id) == null || mCameraInfos.get(id).getCameraStateId() < I_CAMERA_OPENED) {
            CameraManager.CameraProxy proxy = CameraHolder.instance().open(mMainHandler, id, mCameraOpenErrorCallback, true, true, t);
            SLog.d(TAG, "ServiceImpl::startRecordingDirect. proxy:" + (proxy == null ? "fail" : "success"));
            CameraInfo infos = new CameraInfo(proxy, id);
            if (proxy == null) {
                return false;
            } else {
                try {
                    //                    proxy.setErrorCallback(new CameraEvent.CameraErrorCallback(id, this));
                    infos.setCameraStatus(CAMERA_OPENED);
                    infos.setCameraType(proxy.getParameters().get("camera-type"));
                    Camera.CameraInfo info = new Camera.CameraInfo();
                    Camera.getCameraInfo(id, info);
                    infos.setCameraFacing(info.facing);
                    SLog.d(TAG, "ServiceImpl::open. Camera " + id + " is " + proxy.getParameters().get("camera-type") + "; FACING is " + info.facing);
                    mCameraInfos.put(id, infos);
                    afterOpenedBeforePreview();
                    startPreview(id, t == null ? true : false, true, t);
                    return true;
                } catch (RuntimeException e) {
                    mIsUsbHotpluging = false;
                }
            }
        } else if (mCameraInfos.get(id).getCameraStateId() < I_CAMERA_PREVIEWING) {
            return startPreview(id, t == null ? true : false, true, t);
        } else if (!isRecording()) {
            startRecord(id);
        }
        return true;
    }

    @Override
    public boolean[] stopPreview() {
        if (mCameraInfos == null) {
            throw new RuntimeException("CameraDevices is null");
        } else if (mCameraInfos.size() == 0) {
            throw new RuntimeException("CameraDevices is zero");
        }
        int id;
        boolean result[] = {false, false, false};
        for (CameraInfo info : mCameraInfos.values()) {
            id = info.getCameraId();
            result[id] = stopPreview(id);
        }
        return result;
    }

    @Override
    public boolean stopPreview(int id) {
        if (mCameraInfos != null && mCameraInfos.get(id) != null) {
            if (mCameraInfos.get(id).getCameraStateId() < IService.I_CAMERA_PREVIEWING) {
                SLog.w(TAG, "ServiceImpl::stopPreview(" + id + ", CameraStartPreviewCallback) - it's not Previewing, no need to stop");
                return true;
            } else if (mCameraInfos.get(id).getCameraStateId() > 2) {
                SLog.w(TAG, "ServiceImpl::stopPreview(" + id + ", CameraStartPreviewCallback) - it's Recording, Can't stop preview");
                return false;
            }
            mCameraInfos.get(id).getCameraManager().stopPreview();
            mCameraInfos.get(id).setCameraStatus(CAMERA_OPENED);
            return true;
        }
        return false;
    }

    @Override
    public boolean[] release() {
        if (mCameraInfos == null) {
            throw new RuntimeException("CameraDevices is null");
        } else if (mCameraInfos.size() == 0) {
            throw new RuntimeException("CameraDevices is zero");
        }
        int id;
        boolean result[] = {false, false, false};
        for (CameraInfo info : mCameraInfos.values()) {
            id = info.getCameraId();
            result[id] = release(id);
        }
        return result;
    }

    @Override
    public boolean release(int id) {
        if (mCameraInfos != null && mCameraInfos.get(id) != null) {
            if (mCameraInfos.get(id).getCameraStateId() < I_CAMERA_OPENED) {
                SLog.w(TAG, "ServiceImpl::release(" + id + ", CameraStartPreviewCallback) - it's not Opened, no need to release");
                return true;
            } else if (mCameraInfos.get(id).getCameraStateId() > I_CAMERA_RECORDING) {
                SLog.w(TAG, "ServiceImpl::release(" + id + ", CameraStartPreviewCallback) - it's Recording, Can't release Camera");
                return false;
            }
            SLog.d(TAG, "release camera " + id);
            mCameraInfos.get(id).getCameraManager().setErrorCallback(null);
            CameraHolder.instance().release(id);
            mCameraInfos.get(id).setCameraStatus(CAMERA_IDLE);
            mCameraInfos.remove(id);
            return true;
        }
        return false;
    }

    @Override
    public void takePicture() {
        if (mCameraInfos == null) {
            throw new RuntimeException("CameraDevices is null");
        } else if (mCameraInfos.size() == 0) {
            throw new RuntimeException("CameraDevices is zero");
        }

        for (CameraInfo info : mCameraInfos.values()) {
            takePicture(info.getCameraId());
        }
    }

    @Override
    public void takePicture(int id) {
        if (mCameraInfos != null && mCameraInfos.get(id) != null && mCameraInfos.get(id).getCameraManager() != null) {
            if (mCameraInfos.get(id).getCameraStateId() < I_CAMERA_PREVIEWING) {  //预览还未开始，不允许拍照
                SLog.w(TAG, "ServiceImpl::takePicture " + id + ". It's not previewing!");
                return;
            } else if (!mAllowedTakePictureOnRecording && mCameraInfos.get(id).getCameraStateId() >= I_CAMERA_RECORDING) { //录像状态，并且录像状态不允许拍照
                SLog.w(TAG, "ServiceImpl::takePicture " + id + ". It's Recording!");
                return;
            }
            mCameraInfos.get(id).getCameraManager().takePicture(mMainHandler, null, null, null, new JpegPictureCallback(null, id));
        }
    }

    @Override
    public boolean[] initRecorder() {
        if (mCameraInfos == null) {
            throw new RuntimeException("CameraDevices is null");
        } else if (mCameraInfos.size() == 0) {
            throw new RuntimeException("CameraDevices is zero");
        }

        int id;
        boolean result[] = {false, false, false};
        for (CameraInfo info : mCameraInfos.values()) {
            id = info.getCameraId();
            result[id] = initRecorder(id);
        }
        return result;
    }

    @Override
    public boolean initRecorder(int id) {
        if (mCameraInfos != null && mCameraInfos.get(id) != null) {
            if (mCameraInfos.get(id).getCameraStateId() < IService.I_CAMERA_PREVIEWING || mCameraInfos.get(id).getCameraStateId() >= IService.I_CAMERA_RECORDING) {
                return false;
            }
            MediaRecorder recorder = mCameraInfos.get(id).getMediaRecorder();
            recorder.setCamera(mCameraInfos.get(id).getCameraManager().getCamera());

            //            if (id == ParametersSet.getMainCameraId() && CameraSettings.isAudioEnable(id)) {
            //                recorder.setAudioSource(MediaRecorder.AudioSource.CAMCORDER);
            //            }
            recorder.setVideoSource(MediaRecorder.VideoSource.CAMERA);

            String videoQuailty = "90";//CameraSettings.getVideoQuailty(id);
            CamcorderProfile profile = ParametersSet.getBestCamcorderProfile(id, Integer.parseInt(videoQuailty));
            if (profile != null) {
                //profile.fileFormat = MediaRecorder.OutputFormat.OUTPUT_FORMAT_MPEG4_FOR_DRIVING;
                profile.fileFormat = 10;
                SLog.d(TAG, "ServiceImpl::initRecorder(). id:" + id + "; videoFrameWidth:" + profile.videoFrameWidth + "; videoFrameHeight:" + profile.videoFrameHeight + "; videoFrameRate:" + profile.videoFrameRate);
                //                if (!ParametersSet.isPreviewFpsSupported(id, profile.videoFrameRate)) {
                //                    SLog.d(TAG, "ServiceImpl::initRecorder(). id:" + id + "; videoFrameRate " + profile.videoFrameRate + " unSupported, reSet " + ParametersSet.getSupportedMaxFps(id));
                //                    profile.videoFrameRate = ParametersSet.getSupportedMaxFps(id);
                //                } else {
                //                    SLog.d(TAG, "ServiceImpl::initRecorder(). id:" + id + "; videoFrameRate " + profile.videoFrameRate + " Supported.");
                //                }
                recorder.setProfile(profile);
            } else {
                //recorder.setOutputFormat(MediaRecorder.OutputFormat.OUTPUT_FORMAT_MPEG4_FOR_DRIVING);
                SLog.d(TAG, "initRecorder id:" + id + " profile is null");
                recorder.setOutputFormat(10);
                recorder.setVideoEncoder(MediaRecorder.VideoEncoder.H264);
                //                if (id == ParametersSet.getMainCameraId() && CameraSettings.isAudioEnable(id)) {
                //                    recorder.setAudioEncoder(MediaRecorder.AudioEncoder.AAC);
                //                }
                recorder.setVideoSize(1280, 720);
                recorder.setVideoFrameRate(30);
            }

            int rotation = 0;
            if (mOrientation != OrientationEventListener.ORIENTATION_UNKNOWN) {
                android.hardware.Camera.CameraInfo info = new android.hardware.Camera.CameraInfo();
                android.hardware.Camera.getCameraInfo(id, info);
                if (info.facing == android.hardware.Camera.CameraInfo.CAMERA_FACING_FRONT) {
                    rotation = (info.orientation - mOrientation + 360) % 360;
                } else {  // back-facing camera_surfaceview
                    rotation = (info.orientation + mOrientation) % 360;
                }
                if (rotation != 0) {
                    SLog.e(TAG, "rotation:" + rotation + "; info.facing:" + info.facing + "; info.orientation:" + info.orientation + "; mOrientation:" + mOrientation);
                    rotation = 0;
                }
            }
            recorder.setOrientationHint(rotation);

            //            Config.mMaxRecordTime = Integer.parseInt(CameraSettings.getRecordDuration());
            recorder.setMaxDuration(Config.mMaxRecordTime);
            recorder.setOutputFile(generateName(id, 0));

            //            recorder.setOnInfoListener(new CameraEvent.MediaRecorderInfoListener(id, Config.mMaxRecordTime, CameraSettings.isAudioEnable(id), this));
            //            recorder.setOnErrorListener(new CameraEvent.MediaRecorderErrorListener(id, this));
            try {
                recorder.setMaxFileSize(-1);
            } catch (RuntimeException e) {

            }
            try {
                recorder.prepare();
            } catch (IOException e) {
                e.printStackTrace();
                throw new RuntimeException(e);
            }
        }
        return true;
    }

    @Override
    public void startRecord() {
        if (mCameraInfos == null) {
            throw new RuntimeException("CameraDevices is null");
        } else if (mCameraInfos.size() == 0) {
            throw new RuntimeException("CameraDevices is zero");
        }
        //        StorageManager.getInstance(this).updateStorageSpaceAndHint(true, new StorageManager.OnStorageUpdateDoneListener() {
        //            @Override
        //            public void onStorageUpdateDone(long bytes, boolean enought) {
        //                if (enought) {
        //                    int size = mCameraInfos.size();
        //                    SLog.d(TAG, "ServiceImpl::startRecord. CameraInfo size is " + size);
        //                    boolean result[] = {false, false, false};
        //                    for (CameraInfo info : mCameraInfos.values()) {
        //                        if (info.getCameraManager() != null) {
        //                            info.getCameraManager().unlock();
        //                            result[info.getCameraId()] = initRecorder(info.getCameraId());
        //                        }
        //                    }
        //                    boolean timeUpdated = false;
        //                    for (CameraInfo info : mCameraInfos.values()) {
        //                        if (result[info.getCameraId()]) {
        //                            info.getMediaRecorder().start();
        //                            info.setRecording(true);
        //                            info.setCameraStatus(CAMERA_RECORDING);
        //                            setPreviewCallback(info.getCameraId());
        //                            setEncordingRate(info.getCameraId());
        //                            if (!timeUpdated && mCameraCallback != null) {
        //                                timeUpdated = true;
        //                                mCameraCallback.updateRecordIcon(true, false);
        //                                mStartRecordTime = SystemClock.uptimeMillis();
        //                                mCameraCallback.updateRecordTime(mStartRecordTime, Config.mMaxRecordTime);
        //                            }
        //                        }
        //                    }
        //                } else {
        //                    SLog.w(TAG, "Can't start record, due to there is no enought memory. it's " + bytes);
        //                    Toast.makeText(getApplicationContext(), R.string.low_memory, Toast.LENGTH_LONG).show();
        //                }
        //            }
        //        }, false);
    }

    @Override
    public void startRecord(final int id) {
        //        StorageManager.getInstance(this).updateStorageSpaceAndHint(true, new StorageManager.OnStorageUpdateDoneListener() {
        //            @Override
        //            public void onStorageUpdateDone(long bytes, boolean enought) {
        //                if (enought) {
        //                    if (mCameraInfos != null && mCameraInfos.get(id) != null && mCameraInfos.get(id).getCameraManager() != null && mCameraInfos.get(id).getMediaRecorder() != null) {
        //                        mCameraInfos.get(id).getCameraManager().unlock();
        //                        if (initRecorder(id)) {
        //                            mCameraInfos.get(id).getMediaRecorder().start();
        //                            mCameraInfos.get(id).setRecording(true);
        //                            mCameraInfos.get(id).setCameraStatus(CAMERA_RECORDING);
        //                            setPreviewCallback(id);
        //                            setEncordingRate(id);
        //                            if (mCameraCallback != null) {
        //                                if (id == ParametersSet.getMainCameraId() || !isDoubleRecording()) {
        //                                    mCameraCallback.updateRecordIcon(true, false);
        //                                    mStartRecordTime = SystemClock.uptimeMillis();
        //                                    mCameraCallback.updateRecordTime(mStartRecordTime, Config.mMaxRecordTime);
        //                                }
        //                            }
        //                        } else {
        //                            SLog.d(TAG, "initRecorder " + id + " failed");
        //                        }
        //                    } else {
        //                        SLog.d(TAG, "startRecord(" + id + ") failed!");
        //                        if (mCameraInfos == null) {
        //                            SLog.d(TAG, "mCameraInfos is null");
        //                        } else if (mCameraInfos.get(id) == null) {
        //                            SLog.d(TAG, "mCameraInfos.get(id) is null");
        //                        } else if (mCameraInfos.get(id).getCameraManager() == null) {
        //                            SLog.d(TAG, "mCameraInfos.get(id).getCameraManager() is null");
        //                        } else if (mCameraInfos.get(id).getMediaRecorder() == null) {
        //                            SLog.d(TAG, "mCameraInfos.get(id).getMediaRecorder() is null");
        //                        }
        //                    }
        //                } else {
        //                    SLog.w(TAG, "Can't start record, due to there is no enought memory. it's " + bytes);
        //                    Toast.makeText(getApplicationContext(), R.string.low_memory, Toast.LENGTH_LONG).show();
        //                }
        //                mIsUsbHotpluging = false;
        //            }
        //        }, false);
    }

    public void startRecordSync(int id) {
        //        boolean enought = StorageManager.getInstance(this).checkStorage(true);
        //        if (enought) {
        //            if (mCameraInfos != null && mCameraInfos.get(id) != null && mCameraInfos.get(id).getCameraManager() != null && mCameraInfos.get(id).getMediaRecorder() != null) {
        //                mCameraInfos.get(id).getCameraManager().unlock();
        //                if (initRecorder(id)) {
        //                    mCameraInfos.get(id).getMediaRecorder().start();
        //                    mCameraInfos.get(id).setRecording(true);
        //                    mCameraInfos.get(id).setCameraStatus(CAMERA_RECORDING);
        //                    setPreviewCallback(id);
        //                    setEncordingRate(id);
        //                    if (mCameraCallback != null) {
        //                        if (id == ParametersSet.getMainCameraId() || !isDoubleRecording()) {
        //                            mCameraCallback.updateRecordIcon(true, false);
        //                            mStartRecordTime = SystemClock.uptimeMillis();
        //                            mCameraCallback.updateRecordTime(mStartRecordTime, Config.mMaxRecordTime);
        //                        }
        //                    }
        //                }
        //            }
        //        } else {
        //            SLog.w(TAG, "Can't start record, due to there is no enought memory!");
        //            Toast.makeText(getApplicationContext(), R.string.low_memory, Toast.LENGTH_LONG).show();
        //        }
    }

    @Override
    public void stopRecord() {
        //        if (mCameraInfos == null) {
        //            throw new RuntimeException("CameraDevices is null");
        //        } else if (mCameraInfos.size() == 0) {
        //            throw new RuntimeException("CameraDevices is zero");
        //        }
        //        int size = mCameraInfos.size();
        //        mNeedScanFile = size > 1 ? false : true;
        //        for (CameraInfo info : mCameraInfos.values()) {
        //            stopRecord(info.getCameraId());
        //        }
        //        if (!mNeedScanFile) {
        //            mNeedScanFile = true;
        //            Utils.scanVideoFile(getApplicationContext());
        //        }
    }

    @Override
    public void stopRecord(int id) {
        //        SLog.d(TAG, "ServiceImpl::stopRecord. id:" + id);
        //        StorageManager.getInstance(this).updateStorageSpaceAndHint(false, null, false);
        //        if (mCameraInfos != null && mCameraInfos.get(id) != null && mCameraInfos.get(id).getCameraManager() != null && mCameraInfos.get(id).getMediaRecorder() != null && isRecording(id)) {
        //            MediaRecorder recorder = mCameraInfos.get(id).getMediaRecorder();
        //            recorder.setOnErrorListener(null);
        //            recorder.setOnInfoListener(null);
        //            mCameraInfos.get(id).stopRecord();
        //            recorder.reset();
        //            recorder = null;
        //            mCameraInfos.get(id).setMediaRecorder(null);
        //
        //            mCameraInfos.get(id).getCameraManager().lock();
        //            mCameraInfos.get(id).setCameraStatus(CAMERA_PREVIEWING);
        //        }
        //        if (mCameraCallback != null) {
        //            mMainHandler.post(new Runnable() {
        //                @Override
        //                public void run() {
        //                    mCameraCallback.updateRecordIcon(isRecording(ParametersSet.getMainCameraId()), false);
        //                }
        //            });
        //        }
        //        if (mNeedScanFile) {
        //            Utils.scanVideoFile(getApplicationContext());
        //        }
    }

    @Override
    public boolean isOpended(int id) {
        if (mCameraInfos == null || mCameraInfos.get(id) == null) return false;
        if (mCameraInfos.get(id).getCameraStateId() >= I_CAMERA_OPENED) {
            return true;
        }
        return false;
    }

    @Override
    public boolean isPreviewing(int id) {
        if (mCameraInfos.get(id) != null && mCameraInfos.get(id).getCameraStateId() >= I_CAMERA_PREVIEWING) {
            return true;
        }
        return false;
    }

    @Override
    public boolean isRecording(int id) {
        //        if (mCameraInfos.get(id).getCameraStateId() >= I_CAMERA_RECORDING) {
        //            return true;
        //        }
        if (mCameraInfos.get(id) != null && mCameraInfos.get(id).isRecording()) {
            return true;
        }
        return false;
    }

    public boolean isRecording() {
        for (CameraInfo info : mCameraInfos.values()) {
            if (info.isRecording()) {
                return true;
            }
        }
        return false;
    }

    public boolean isDoubleRecording() {
        //        if (CameraSettings.isSingleRecord())
        //            return false;
        //        int numbers = getNumberOfCameras();
        //        if (numbers >= 2) {
        //            String mode = CameraSettings.getRecordMode();
        //            String arr[] = ServiceImpl.this.getResources().getStringArray(R.array.pref_camera_record_mode_entry);
        //            if (mode.equals(arr[0])) {
        //                return true;
        //            }
        //        }
        return false;
    }

    public Map getSurfaceToCamera() {
        if (mSurfaceToCamera == null) {
            mSurfaceToCamera = new HashMap<>();
        }
        return mSurfaceToCamera;
    }

    public Map getCameraToSurface() {
        if (mCameraToSurface == null) {
            mCameraToSurface = new HashMap<>();
        }
        return mCameraToSurface;
    }

    public int getCameraId(String type) {
        for (CameraInfo info : mCameraInfos.values()) {
            if (type.equals(info.getCameraType())) {
                return info.getCameraId();
            }
        }
        return -1;
    }

    public int getSocId() {
        return getCameraId("SOC");
    }

    public int getUsbId() {
        return getCameraId("USB");
    }

    private void setEncordingRate(int id) {
        //        try {
        //            String strEncordRate = SharedPreference.getString(ParametersSet.getPrefKey(Config.KEY_ENCORDING_RATE, id), BaseApplication.getInstance().getResources().getString(R.string.pref_encording_rate_default));
        //            long rate = Long.parseLong(strEncordRate) * 1024 * 1024;
        //            SLog.d(TAG, "Camera " + id + " Encording Rate: " + rate);
        //            if (mCameraInfos.get(id) != null && mCameraInfos.get(id).getMediaRecorder() != null) {
        //                //mCameraInfos.get(id).getMediaRecorder().setParametersExtra("video-param-encoder-bitrate=" + rate);
        //            }
        //        } catch(Exception e) {
        //            e.printStackTrace();
        //        }
    }

    public void switchPreview() {
    }

    public void registerCallback(CameraCallback callback) {
        this.mCameraCallback = callback;
    }

    public void unregisterCallback() {
        this.mCameraCallback = null;
    }

    private void setNextRecord(int id) {
        //        SLog.d(TAG, "ServiceImpl::setNextRecord(" + id + ")");
        //        try {
        //            if (mCameraInfos.get(id) != null && mCameraInfos.get(id).getMediaRecorder() != null){
        //                mCameraInfos.get(id).getMediaRecorder().setNextRecorder(generateName(id == getResources().getInteger(R.integer.surface0_to_camera) ? 0 : 1, 0));
        //            }
        //        } catch (/*IO*/Exception e) {
        //            e.printStackTrace();
        //        }
        //        if ((isDoubleRecording() && id == ParametersSet.getMainCameraId()) || !isDoubleRecording()) {
        //            mStartRecordTime = SystemClock.uptimeMillis();
        //            if (mCameraCallback != null) {
        //                mCameraCallback.updateRecordTime(mStartRecordTime, Config.mMaxRecordTime);
        //            }
        //        }
    }

    private String generateName(int id, long time, int type) {
        Date date = new Date(time);
        SimpleDateFormat dateFormat = null;
        String path = "NULL";
        switch (type) {
            //            case 0:
            //                if (id == getResources().getInteger(R.integer.surface0_to_camera)) {
            //                    dateFormat = new SimpleDateFormat(this.getString(R.string.video_file_name_format));
            //                    path = Config.VIDEO_DIRECTORY_A + '/' + dateFormat.format(date) + ".mp4";
            //                } else {
            //                    dateFormat = new SimpleDateFormat(this.getString(R.string.video_usb_file_name_format));
            //                    path = Config.VIDEO_DIRECTORY_B + '/' + dateFormat.format(date) + ".mp4";
            //                }
            //                break;
            //            case 1:
            //                if (id == getResources().getInteger(R.integer.surface0_to_camera)) {
            //                    dateFormat = new SimpleDateFormat(this.getString(R.string.image_file_name_format));
            //                    path = dateFormat.format(date);
            ////                    path = Config.JPEG_DIRECTORY + "/" + dateFormat.format(date) + ".jpg";
            //                } else {
            //                    dateFormat = new SimpleDateFormat(this.getString(R.string.image_usb_file_name_format));
            //                    path = dateFormat.format(date);
            ////                    path = Config.JPEG_DIRECTORY + "/" + dateFormat.format(date) + ".jpg";
            //                }
            //                break;
        }
        String tmp = null;
        tmp = (type == 0 ? path : Config.JPEG_DIRECTORY + "/" + path + ".jpg");
        File file = new File(tmp);
        if (file.exists()) {
            if (file.isFile()) {
                file.delete();
            } else if (file.isDirectory()) {
                file.delete();
            }
        }
        File dir = new File(tmp.substring(0, tmp.lastIndexOf("/")));
        if (!dir.exists()) {
            if (!dir.mkdirs()) {
                throw new RuntimeException("mkdirs " + dir.getPath() + " error");
            }
        }
        return path;
    }

    private String generateName(int id, int type) {
        return generateName(id, System.currentTimeMillis(), type);
    }

    protected CameraManager.CameraOpenCallback mCameraOpenErrorCallback = new CameraManager.CameraOpenCallback() {


        @Override
        public <T> void onSuccess(int cameraId, boolean preview, boolean recorder, T t) {
            SLog.d(TAG, "ServiceImpl.mCameraOpenErrorCallback::onSuccess. cameraId:" + cameraId + "; preview:" + preview + "; recorder:" + recorder);
            //            if (preview) {
            //                afterOpenedBeforePreview();
            //                startPreview(cameraId, t == null ? true : false, recorder, t);
            //            }
        }

        @Override
        public void onFailure(int cameraId, int what) {

        }
    };

    private CameraManager.CameraStartPreviewCallback mCameraStartPreivewCallback = new CameraManager.CameraStartPreviewCallback() {
        @Override
        public void onSuccess(int cameraId, boolean record) {
            SLog.d(TAG, "ServiceImpl.mCameraStartPreivewCallback::onSuccess. cameraId:" + cameraId + "; recorder:" + record);
            if (!mTakePicture) {
                mCameraInfos.get(cameraId).setCameraStatus(CAMERA_PREVIEWING);
            }
            if (record) {
                afterPreviewedBeforeRecord();
                startRecord(cameraId);
            } else {
                mIsUsbHotpluging = false;
            }
        }

        @Override
        public void onFailure(int cameraId, int reason) {

        }
    };

    private class JpegPictureCallback implements CameraManager.CameraPictureCallback {

        Location mLocation;
        int mCameraId;

        public JpegPictureCallback(Location loc, int cameraId) {
            mLocation = loc;
            mCameraId = cameraId;
        }


        @Override
        public void onPictureTaken(byte[] data, CameraManager.CameraProxy camera) {
            SLog.i(TAG, "Camera " + mCameraId + " onPictureTaken. lenght:" + data.length);
            storeImage(data, mLocation, mCameraId);
            mTakePicture = true;
            startPreview(mCameraId);
        }

        private void storeImage(final byte[] data, Location loc, int cameraId) {
            //            long dateTaken = System.currentTimeMillis();
            //            String title = generateName(cameraId, dateTaken, 1);
            //            SLog.d(TAG, "ServiceImpl.JpegPictureCallback::storeImage(data.lenght:" + (data == null ? 0 : data.length) + ", " + (loc == null ? null : loc.toString()) + ", " + cameraId + "). picPath:" + title);
            //            ExifInterface exif = Exif.getExif(data);
            //            int orientation = Exif.getOrientation(exif);
            //
            //            BaseApplication.getInstance().getMediaSaver().addImage(data, title, dateTaken, loc, orientation, exif, new TakePictureOnMediaSavedListener(), mContentResolver);
        }

        //        class TakePictureOnMediaSavedListener implements MediaSaver.OnMediaSavedListener {
        //
        //            @Override
        //            public void onMediaSaved(Uri uri) {
        //                SLog.d(TAG, "ServiceImpl.JpegPictureCallback.TakePictureOnMediaSavedListener::onMediaSaved(" + (uri != null ? uri.toString() : "null") + "). Camera:" + mCameraId);
        //            }
        //        }
    }

    ;

    public class CameraBinder extends Binder {
        WeakReference<ServiceImpl> mWeakService;
        private ServiceImpl mService;

        public CameraBinder(ServiceImpl service) {
            mWeakService = new WeakReference<ServiceImpl>(service);
            mService = mWeakService.get();
        }

        public boolean startPreview(int id) {
            return mService.startPreview(id);
        }

        public boolean startPreview(int id, SurfaceTexture surface, boolean direct) {
            return mService.startPreview(id);
        }

        public boolean[] open() {
            return mService.open();
        }

        public boolean open(int id) {
            return mService.open(id);
        }

        public Camera getCamera(int id) {
            return mService.getCamera(id);
        }

        public Map<Integer, CameraInfo> getCameraInfos() {
            return mService.getCameraInfos();
        }

        public CameraInfo getCameraInfo(int id) {
            if (mService.getCameraInfos() != null) {
                return mService.getCameraInfos().get(id);
            }
            return null;
        }

        public void setPreviewCallback(int id) {
            mService.setPreviewCallback(id);
        }

        public void setPreviewCallback(int id, Camera.PreviewCallback callback) {
            if (mCameraInfos.get(id) != null && mCameraInfos.get(id).getCameraManager() != null) {
                mCameraInfos.get(id).getCameraManager().setPreviewCallbackWithBuffer(null, callback);
            }
        }

        public void addCallbackBuffer(int id, byte[] data) {
            try {
                if (mCameraInfos.get(id) != null && mCameraInfos.get(id).getCameraManager() != null) {
                    mCameraInfos.get(id).getCameraManager().addCallbackBuffer(data);
                }
            } catch (NullPointerException e) {
                e.printStackTrace();
            }
        }

        public <T> boolean startPreview(int id, boolean callback, T texture) {
            return mService.startPreview(id, callback, false, (texture instanceof SurfaceTexture ? (SurfaceTexture) texture : (texture instanceof SurfaceHolder ? (SurfaceHolder) texture : null)));
        }

        public <T> boolean startPreviewDirect(int id, T t) {
            return mService.startPreviewDirect(id, t);
        }

        public <T> boolean startRecordingDirect(int id, T t) {
            //            if (CameraSettings.isSingleRecord() && id != CameraSettings.getSingleRecordCameraId()) {
            //                return mService.startPreviewDirect(id, t);
            //            } else {
            //                return mService.startRecordingDirect(id, t);
            //            }
            return false;
        }

        public boolean stopPreview(int id) {
            return mService.stopPreview(id);
        }

        public boolean[] release() {
            return mService.release();
        }

        public boolean release(int id) {
            return mService.release(id);
        }

        public void startRecord() {
            //            String mode = CameraSettings.getRecordMode();
            //            String arr[] = ServiceImpl.this.getResources().getStringArray(R.array.pref_camera_record_mode_entry);
            //            SLog.d(TAG, "ServiceImpl::startRecord(). mode:" + mode + "; numbers:" + CameraHolder.instance().getNumberOfCameras());
            //            if (mCameraInfos.size() > 1) {
            //                if (mode.equals(arr[0])) {
            //                    mService.startRecord();
            //                } else if (mode.equals(arr[1])) {
            //                    mService.startRecord(getResources().getInteger(R.integer.surface0_to_camera));
            //                } else if (mode.equals(arr[2])) {
            //                    mService.startRecord(getResources().getInteger(R.integer.surface1_to_camera));
            //                }
            //            } else {
            //                mService.startRecord(getResources().getInteger(R.integer.surface0_to_camera));
            //            }
        }

        public void startRecord(int id) {
            SLog.d(TAG, "ServiceImpl.CameraBinder::startRecord(" + id + ")");
            mService.startRecord(id);
        }

        public long getStartRecordTime() {
            return mStartRecordTime;
        }

        public void stopRecord() {
            //            String mode = CameraSettings.getRecordMode();
            //            SLog.d(TAG, "ServiceImpl::stopRecord. mode:" + mode + "; cameras:" + CameraHolder.instance().getNumberOfCameras());
            //            String arr[] = ServiceImpl.this.getResources().getStringArray(R.array.pref_camera_record_mode_entry);
            //            if (CameraHolder.instance().getNumberOfCameras() > 1) {
            //                if (mode.equals(arr[0])) {
            //                    mService.stopRecord();
            //                } else if (mode.equals(arr[1])) {
            //                    mService.stopRecord(0);
            //                } else if (mode.equals(arr[2])) {
            //                    mService.stopRecord(1);
            //                }
            //            } else {
            //                mService.stopRecord(0);
            //            }
        }

        public void stopRecord(int id) {
            mService.stopRecord(id);
        }

        public boolean isRecording(int id) {
            return mService.isRecording(id);
        }

        public boolean isRecording() {
            return mService.isRecording();
        }

        public Map getSurfaceToCamera() {
            return mService.getSurfaceToCamera();
        }

        public Map getCameraToSurface() {
            return mService.getCameraToSurface();
        }

        public int getSocId() {
            return mService.getSocId();
        }

        public int getUsbId() {
            return mService.getUsbId();
        }

        public void takePicture() {
            mService.takePicture();
        }

        public void takePicture(int id) {
            mService.takePicture(id);
        }

        public void switchPreview() {
            mService.switchPreview();
        }

        public int getNumberOfCameras() {
            return CameraHolder.instance().getNumberOfCameras();
        }

        public void registerCallback(CameraCallback callback) {
            mService.registerCallback(callback);
        }

        public void unregisterCallback() {
            mService.unregisterCallback();
        }

        public void activityExisted() {
            mService.activityExisted();
        }

        public boolean isHotPluging() {
            return mIsUsbHotpluging;
        }
    }

    public interface CameraCallback {
        void setPreviewFormat(int id, int format);

        void setRenderSize(int id, int width, int height);

        void requestRender(int id, byte[] data);

        void updateRecordIcon(boolean recording, boolean lock);

        void updateRecordTime(long start, long max);

        void surfaceVisible(int id, int visible);

        void finishActivity();

        void usbHotPlugEvent(int state);

        void drawAdasResult(Bitmap bitmap);
    }

    //    @Override
    //    public void onInfoRecord(int id, int what, int extra, long time) {
    //        SLog.d(TAG, "ServiceImpl::onInfoRecord(" + id + ", " + what + ", " + extra + ", " + time + ")");
    //        switch (what) {
    //            case MediaRecorder.MEDIA_RECORDER_INFO_MAX_DURATION_REACHED:
    //                setNextRecord(id);
    //                break;
    //        }
    //    }
    //
    //    @Override
    //    public void onErrorCamera(int id, int error) {
    //        SLog.d(TAG, "ServiceImpl.onErrorCamera(" + id + ", " + error + ")");
    //        final boolean isPreview = true;
    //        final boolean isRecording = isRecording();
    //
    //        new Thread(new Runnable() {
    //            @Override
    //            public void run() {
    //                //Intent intent = new Intent(Config.ACTION_RECORD_RESTART);
    //                //intent.putExtra(Config.EXTRA_PREVIEWING, isPreview);
    //                //intent.putExtra(Config.EXTRA_RECORDING, isRecording);
    //                //sendBroadcast(intent);
    //                //Utils.killProcess(ServiceImpl.this, ServiceImpl.this.getPackageName());
    //            }
    //        }).start();
    //    }
    //
    //    @Override
    //    public void onErrorMedia(int id, int what, int extra) {
    //        SLog.d(TAG, "ServiceImpl.onErrorMedia(" + id + ", " + what + ", " + extra + ")");
    //    }
    //
    //    private int mLastUsbId = -1;
    //    private Object mUsbHotPlugLock = new Object();
    //    @Override
    //    public void onUsbHotplug(final boolean add) {
    //        mMainHandler.removeMessages(MSG_USB_HOTPLUG_ADD);
    //        mMainHandler.removeMessages(MSG_USB_HOTPLUG_REMOVE);
    //        mMainHandler.sendEmptyMessageDelayed(add ? MSG_USB_HOTPLUG_ADD : MSG_USB_HOTPLUG_REMOVE, 100);
    //    }

    private boolean mIsUsbHotpluging;
    private int mLastUsbStatus = -1;

    public void execUsbHotplug(final boolean add) {
        //        new Thread(new Runnable() {
        //            @Override
        //            public void run() {
        //                synchronized (mUsbHotPlugLock) {
        //                    SLog.d(TAG, "ServiceImpl::onUsbHotplug. state:" + (add ? "ADD" : "REMOVE"));
        //                    if (mIsUsbHotpluging) {
        //                        SLog.w(TAG, "ServiceImpl::onUsbHotplug. It's hotpluging, please wait!");
        //                        mMainHandler.removeMessages(MSG_USB_HOTPLUG_ADD);
        //                        mMainHandler.removeMessages(MSG_USB_HOTPLUG_REMOVE);
        //                        mMainHandler.sendEmptyMessageDelayed(add ? MSG_USB_HOTPLUG_ADD : MSG_USB_HOTPLUG_REMOVE, 100);
        //                        return;
        //                    }
        //                    mIsUsbHotpluging = true;
        //                    if (mCameraCallback != null) {
        //                        mCameraCallback.usbHotPlugEvent(1);
        //                    }
        //                    if (add) {
        //                        if (mLastUsbStatus == 1) {
        //                            SLog.w(TAG, "ServiceImpl::onUsbHotplug. Last Status is this, ignore this!");
        //                            mIsUsbHotpluging = false;
        //                            return;
        //                        }
        //                        mLastUsbStatus = 1;
        //                        if (mLastUsbId == -1) {
        //                            mLastUsbId = getNumberOfCameras() - 1;
        //                            mLastUsbId = (mLastUsbId < 0 ? 0 : mLastUsbId);
        //                        }
        //                        if (mCameraCallback != null ) {
        //                            mCameraCallback.surfaceVisible(1, View.VISIBLE);
        //                        }
        //                        if (isRecording()) {
        //                            SLog.d(TAG, "ServiceImpl::onUsbHotplug. It's Recording!");
        //                            boolean result = startRecordingDirect(mLastUsbId, null);
        //                            if (!result) {
        //                                SLog.d(TAG, "ServiceImpl::onUsbHotplug. Start record direct failed!");
        //                                mIsUsbHotpluging = false;
        //                            }
        //                        } else {
        //                            if (mCameraCallback != null) {
        //                                SLog.d(TAG, "ServiceImpl::onUsbHotplug. It's Previewing!");
        //                                boolean result = startPreviewDirect(mLastUsbId, null);
        //                                if (!result) {
        //                                    SLog.d(TAG, "ServiceImpl::onUsbHotplug. Start preview direct failed!");
        //                                    mIsUsbHotpluging = false;
        //                                }
        //                            } else {
        //                                mIsUsbHotpluging = false;
        //                            }
        //                        }
        //                    } else {
        //                        if (mLastUsbStatus == 0) {
        //                            SLog.w(TAG, "ServiceImpl::onUsbHotplug. Last Status is this, ignore this!");
        //                            mIsUsbHotpluging = false;
        //                            return;
        //                        }
        //                        mLastUsbStatus = 0;
        //                        int usbId = getUsbId();
        //                        mLastUsbId = usbId;
        //                        if (mCameraCallback != null) {
        //                            mCameraCallback.surfaceVisible(1, View.GONE);
        //                        }
        //                        if (isRecording(usbId)) {
        //                            SLog.d(TAG, "ServiceImpl::onUsbHotplug. it is recording");
        //                            stopRecord(usbId);
        //                        }
        //                        if (isPreviewing(usbId)) {
        //                            SLog.d(TAG, "ServiceImpl::onUsbHotplug. it is preview");
        //                            stopPreview(usbId);
        //                        }
        //                        if (isOpended(usbId)) {
        //                            SLog.d(TAG, "ServiceImpl::onUsbHotplug. it is opened");
        //                            release(usbId);
        //                        }
        //                        mIsUsbHotpluging = false;
        //                    }
        //                    SLog.d(TAG, "ServiceImpl::onUsbHotplug. end");
        //                    if (mCameraCallback != null) {
        //                        mCameraCallback.usbHotPlugEvent(0);
        //                    }
        //                }
        //            }
        //        }).start();
    }


    protected boolean mIsChannelChanged = false;
    private Object mChannelLock = new Object();
    //    @Override
    //    public void changedNotify(String key) {
    //        if (key.equals(Config.KEY_CVBS_CHANNEL)) {
    //            new Thread(new Runnable() {
    //                @Override
    //                public void run() {
    //                    synchronized (mChannelLock) {
    //                        int id = getSocId();
    //                        if (id >= 0) {
    //                            mIsChannelChanged = true;
    //                            if (mCameraInfos.get(id) != null) {
    //                                if (mCameraInfos.get(id).getCameraStateId() >= IService.I_CAMERA_PREVIEWING) {
    //                                    stopPreview(id);
    //                                }
    //                                release(id);
    //                                try {
    //                                    //Thread.sleep(1500);
    //                                    open(id);
    //                                    //startPreview(id);
    //                                } catch (/*Interrupted*/Exception e) {
    //                                    e.printStackTrace();
    //                                }
    //                            }
    //                        }
    //                    }
    //                }
    //            }).start();
    //        }
    //    }

    private BroadcastReceiver mUnmountReceiver = null;

    private void registerExternalStorageListener() {
        //        if (mUnmountReceiver == null) {
        //            mUnmountReceiver = new BroadcastReceiver() {
        //                @Override
        //                public void onReceive(Context context, Intent intent) {
        //                    String action = intent.getAction();
        //                    SLog.i(TAG, "onReceive action = " + action);
        //                    if(action.equals(Intent.ACTION_MEDIA_UNMOUNTED)
        //                            && Config.EXTENAL_SD.equals(intent.getData().getPath())){
        //                        if(isRecording(0) || isRecording(1))
        //                            stopRecord();
        //                        if (mCameraCallback != null)
        //                            mCameraCallback.updateRecordIcon(false, false);
        //                        Toast.makeText(getApplicationContext(), R.string.msg_sd_ummounted, Toast.LENGTH_SHORT).show();
        //                        BaseApplication.getInstance().getSDCard();
        //                    } else if(action.equals(Intent.ACTION_MEDIA_UNMOUNTED)) {
        //                        Toast.makeText(getApplicationContext(), R.string.msg_sd_ummounted, Toast.LENGTH_SHORT).show();
        //                        BaseApplication.getInstance().getSDCard();
        //                    } else if(action.equals(Intent.ACTION_MEDIA_MOUNTED)) {
        //                        Toast.makeText(getApplicationContext(), R.string.msg_sd_mounted, Toast.LENGTH_SHORT).show();
        //                        BaseApplication.getInstance().getSDCard();
        //                    }
        //                }
        //            };
        //            IntentFilter iFilter = new IntentFilter();
        //            iFilter.addAction(Intent.ACTION_MEDIA_EJECT);
        //            iFilter.addAction(Intent.ACTION_MEDIA_UNMOUNTED);
        //            iFilter.addAction(Intent.ACTION_MEDIA_MOUNTED);
        //            iFilter.addAction(Intent.ACTION_MEDIA_REMOVED);
        //            iFilter.addDataScheme("file");
        //            registerReceiver(mUnmountReceiver, iFilter);
        //        }
    }

    private LocationListener mLocationListener = new LocationListener() {

        @Override
        public void onStatusChanged(String provider, int status, Bundle extras) {
            // TODO Auto-generated method stub
            switch (status) {
                case LocationProvider.AVAILABLE:
                    SLog.d(TAG, "gps status: available");
                    break;

                case LocationProvider.OUT_OF_SERVICE:
                    SLog.d(TAG, "gps status: out of service");
                    break;

                case LocationProvider.TEMPORARILY_UNAVAILABLE:
                    SLog.d(TAG, "gps status: temporarily unavailable");
                    break;

                default:
                    break;
            }
        }

        @Override
        public void onProviderEnabled(String provider) {
            // TODO Auto-generated method stub
            SLog.d(TAG, "GPS onProviderEnabled");
        }

        @Override
        public void onProviderDisabled(String provider) {
            // TODO Auto-generated method stub
            SLog.d(TAG, "GPS onProviderDisabled");
            updateLocation(null);
        }

        @Override
        public void onLocationChanged(Location location) {
            // TODO Auto-generated method stub
            SLog.d(TAG, "GPS location changed");
            updateLocation(location);
        }
    };

    private void updateLocation(Location location) {
        /*if (location == null) {
            realLocation = false;
            location = new Location(LocationManager.GPS_PROVIDER);
            location.setLongitude(180.0 * Math.random());
            location.setLatitude(90.0 * Math.random());
            location.setSpeed((float) (30.0 * Math.random()));
        }*/
        if (location != null) {
            double Longituded = location.getLongitude(); //jingdu
            double Latituded = location.getLatitude(); //weidu
            double Altituded = location.getAltitude(); //haiba
            float CarSpeed = location.getSpeed() * 3600 / 1000f; //sudu
            SLog.d(TAG, "location = " + Longituded + "," + Latituded + "," + Altituded + "," + CarSpeed);

            DecimalFormat df = new DecimalFormat("#.00");
            String Longitudedstr = df.format(Math.abs(Longituded));
            String Latitudedstr = df.format(Math.abs(Latituded));
            String CarSpeedstr = CarSpeed >= 1.0f ? df.format(CarSpeed) : "0.00";

            if (Longituded < 0) {
                Longitudedstr = "W" + Longitudedstr;
            } else {
                Longitudedstr = "E" + Longitudedstr;
            }
            if (Latituded < 0) {
                Latitudedstr = "S" + Latitudedstr;
            } else {
                Latitudedstr = "N" + Latitudedstr;
            }

            String gpsinfo;
            boolean hasLatLon = (Latituded != 0.0d) || (Longituded != 0.0d);
            if (hasLatLon) {
                gpsinfo = Longitudedstr + " " + Latitudedstr + " " + CarSpeedstr + "km/h";
            } else {
                location = null;
                gpsinfo = "";
            }
            if (isRecording()) {
                for (int i = 0; i < CameraHolder.instance().getNumberOfCameras(); i++) {
                    ParametersSet.setWaterMark(i, gpsinfo);
                }
            }

        } else {
            if (isRecording()) {
                for (int i = 0; i < CameraHolder.instance().getNumberOfCameras(); i++) {
                    ParametersSet.setWaterMark(i, "");
                }
            }
        }
    }

    private Criteria getCriteria() {
        Criteria criteria = new Criteria();
        criteria.setAccuracy(Criteria.ACCURACY_FINE);
        criteria.setSpeedRequired(true);
        criteria.setCostAllowed(false);
        criteria.setBearingRequired(false);
        criteria.setAltitudeRequired(true);
        criteria.setPowerRequirement(Criteria.POWER_LOW);
        return criteria;
    }

    private void initLocationManager() {
        if (mLocationManager == null) {
            //            mLocationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
            //            if (mLocationManager == null) return;
            /*mLocationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER,
                    3000, 0, mLocationListener);
            if (!mLocationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
                SLog.e(TAG, "gps disabled");
                return;
            }
            String bestProvider = mLocationManager.getBestProvider(getCriteria(),
                    true);
            Location location = mLocationManager.getLastKnownLocation(bestProvider);
            SLog.d(TAG, "getLastKnownLocation = " + location);
            if (location != null) {
                location.setSpeed(0.0f);
                updateLocation(location);
            }
            SLog.i(TAG, "gps location init");*/
        }
    }

    private void disconnectLocationManager() {
        if (mLocationManager != null) {
            try {
                mLocationManager.removeUpdates(mLocationListener);
            } catch (Exception ex) {
                SLog.i(TAG, "fail to remove location listners, ignore" + ex);
            }
            mLocationManager = null;
            SLog.i(TAG, "gps location disconnect");
        }
    }

    public void activityExisted() {
        int id;
        for (CameraInfo info : mCameraInfos.values()) {
            id = info.getCameraId();
            if (isRecording(id)) {
                if (info.getCameraManager() != null) {
                    info.getCameraManager().setPreviewCallbackWithBuffer(null, null);
                }
            } else {
                if (info.getCameraStateId() >= IService.I_CAMERA_PREVIEWING) {
                    stopPreview(id);
                }
                release(id);
            }
        }
    }

    public void onDestroy() {


        //        BaseApplication.getInstance().setService(null);
        //        if (mUnmountReceiver != null) {
        //            try {
        //                unregisterReceiver(mUnmountReceiver);
        //            } catch (Exception e) {
        //                SLog.e(TAG, "unregister mUnmountReceiver error:" + e);
        //            }
        //            mUnmountReceiver = null;
        //        }
        //        CameraEvent.UsbHotPlugReceiver.getInstance(this).unregister();
        disconnectLocationManager();
    }
    /////////////

    public void addCallbackBuffer(int id, byte[] data) {
        try {
            if (mCameraInfos.get(id) != null && mCameraInfos.get(id).getCameraManager() != null) {
                mCameraInfos.get(id).getCameraManager().addCallbackBuffer(data);
            }
        } catch (NullPointerException e) {
            e.printStackTrace();
        }
    }


    public CameraInfo getCameraInfo(int id) {

        getCameraInfos().get(id);

        return null;
    }
}
