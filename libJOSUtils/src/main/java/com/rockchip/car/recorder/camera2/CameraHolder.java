/*
 * Copyright (C) 2009 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.rockchip.car.recorder.camera2;

import static com.rockchip.car.recorder.camera2.CameraManager.CameraOpenCallback;
import static com.rockchip.car.recorder.camera2.CameraManager.CameraProxy;

import android.annotation.SuppressLint;
import android.hardware.Camera;
import android.hardware.Camera.CameraInfo;
import android.hardware.Camera.Parameters;
import android.hardware.Camera.Size;
import android.os.Handler;
import android.util.Log;
import android.view.SurfaceHolder;

import com.rockchip.car.recorder.utils.SLog;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;


/**
 * The class is used to hold an {@code android.hardware.Camera} instance.
 *
 * <p>The {@code open()} and {@code release()} calls are similar to the ones
 * in {@code android.hardware.Camera}. The difference is if {@code keep()} is
 * called before {@code release()}, CameraHolder will try to hold the {@code
 * android.hardware.Camera} instance for a while, so if {@code open()} is
 * called soon after, we can avoid the cost of {@code open()} in {@code
 * android.hardware.Camera}.
 *
 * <p>This is used in switching between different modules.
 */
@SuppressLint("NewApi")
public class CameraHolder {
    private static final String TAG = "CAM_CameraHolder";
    private static final int KEEP_CAMERA_TIMEOUT = 3000; // 3 seconds
    private CameraProxy[] mCameraDevice;
    private boolean[] mCameraOpened;  // true if camera_surfaceview is opened
    private int mNumberOfCameras;
    private int[] mCameraId;  // current camera_surfaceview id
    private int mBackCameraId = -1;
    private int mFrontCameraId = -1;
    private final CameraInfo[] mInfo;
    private static CameraProxy mMockCamera[];
    private static CameraInfo mMockCameraInfo[];
    private SurfaceHolder[] mSurfaceHolder = new SurfaceHolder[CameraSettings.MAX_SUPPORT_CAMERAS];

    public SurfaceHolder getHolder(int cameraId) {
        if (cameraId > -1 && cameraId < mSurfaceHolder.length) {
            return mSurfaceHolder[cameraId];
        } else {
            return null;
        }
    }

    public void setHolder(SurfaceHolder holder, int cameraId) {
        if (cameraId > -1 && cameraId < mSurfaceHolder.length) mSurfaceHolder[cameraId] = holder;
    }

    /* Debug double-open issue */
    private static final boolean DEBUG_OPEN_RELEASE = true;

    private static class OpenReleaseState {
        long time;
        int id;
        String device;
        String[] stack;
    }

    private static ArrayList<OpenReleaseState> sOpenReleaseStates = new ArrayList<OpenReleaseState>();
    private static SimpleDateFormat sDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS");

    private static synchronized void collectState(int id, CameraProxy device) {
        OpenReleaseState s = new OpenReleaseState();
        s.time = System.currentTimeMillis();
        s.id = id;
        if (device == null) {
            s.device = "(null)";
        } else {
            s.device = device.toString();
        }

        StackTraceElement[] stack = Thread.currentThread().getStackTrace();
        String[] lines = new String[stack.length];
        for (int i = 0; i < stack.length; i++) {
            lines[i] = stack[i].toString();
        }
        s.stack = lines;

        if (sOpenReleaseStates.size() > 10) {
            sOpenReleaseStates.remove(0);
        }
        sOpenReleaseStates.add(s);
    }

    private static synchronized void dumpStates() {
        for (int i = sOpenReleaseStates.size() - 1; i >= 0; i--) {
            OpenReleaseState s = sOpenReleaseStates.get(i);
            String date = sDateFormat.format(new Date(s.time));
            Log.d(TAG, "State " + i + " at " + date);
            Log.d(TAG, "mCameraId = " + s.id + ", mCameraDevice = " + s.device);
            Log.d(TAG, "Stack:");
            for (int j = 0; j < s.stack.length; j++) {
                Log.d(TAG, "  " + s.stack[j]);
            }
        }
    }

    public static List<String> mBackCameraSupportWhiteBalances = new ArrayList<String>();
    public static List<String> mBackCameraSupportColorEffects = new ArrayList<String>();
    public static List<Size> mBackCameraSupportVideoSizes = new ArrayList<Size>();
    public static List<Size> mBackCameraSupportPictureSizes = new ArrayList<Size>();
    public static int mBackCameraMaxExposure;
    public static int mBackCameraMinExposure;
    public static float mBackCameraExposureStep;

    // We store the camera_surfaceview parameters when we actually open the device,
    // so we can restore them in the subsequent open() requests by the user.
    // This prevents the parameters set by PhotoModule used by VideoModule
    // inadvertently.
    private Parameters[] mParameters;

    // Use a singleton.
    private static CameraHolder sHolder;

    public static synchronized CameraHolder instance() {
        if (sHolder == null) {
            sHolder = new CameraHolder(CameraSettings.MAX_SUPPORT_CAMERAS);
        }
        return sHolder;
    }

    private static final int RELEASE_CAMERA = 1;

    public static void injectMockCamera(CameraInfo[] info, CameraProxy[] camera) {
        mMockCameraInfo = info;
        mMockCamera = camera;
        sHolder = new CameraHolder(CameraSettings.MAX_SUPPORT_CAMERAS);
    }

    private CameraHolder() {
        if (mMockCameraInfo != null) {
            mNumberOfCameras = mMockCameraInfo.length;
            mInfo = mMockCameraInfo;
            mCameraDevice = new CameraProxy[mNumberOfCameras];
            mCameraId = new int[mNumberOfCameras];
            mCameraOpened = new boolean[mNumberOfCameras];
            mParameters = new Parameters[mNumberOfCameras];
            for (int i = 0; i < mNumberOfCameras; i++) {
                mCameraId[i] = -1;
            }
        } else {
            mNumberOfCameras = android.hardware.Camera.getNumberOfCameras();
            mInfo = new CameraInfo[mNumberOfCameras];
            mCameraDevice = new CameraProxy[mNumberOfCameras];
            mCameraId = new int[mNumberOfCameras];
            mCameraOpened = new boolean[mNumberOfCameras];
            mParameters = new Parameters[mNumberOfCameras];
            for (int i = 0; i < mNumberOfCameras; i++) {
                mInfo[i] = new CameraInfo();
                android.hardware.Camera.getCameraInfo(i, mInfo[i]);
                mCameraId[i] = -1;
            }
        }

        // get the first (smallest) back and first front camera_surfaceview id
        for (int i = 0; i < mNumberOfCameras; i++) {
            Log.d(TAG, "camera(" + i + ") facing:" + mInfo[i].facing);
            if (mBackCameraId == -1 && (mInfo[i].facing == CameraInfo.CAMERA_FACING_BACK || mInfo[i].facing == 128)) {
                mBackCameraId = i;
            } else if (mFrontCameraId == -1 && mInfo[i].facing == CameraInfo.CAMERA_FACING_FRONT) {
                mFrontCameraId = i;
            }
        }
    }

    private CameraHolder(int numbers) {
        if (mMockCameraInfo != null) {
            mNumberOfCameras = mMockCameraInfo.length;
            mInfo = mMockCameraInfo;
            mCameraDevice = new CameraProxy[numbers];
            mCameraId = new int[numbers];
            mCameraOpened = new boolean[numbers];
            mParameters = new Parameters[numbers];
            for (int i = 0; i < numbers; i++) {
                mCameraId[i] = -1;
            }
        } else {
            mNumberOfCameras = getNumberOfCameras();
            mInfo = new CameraInfo[numbers];
            mCameraDevice = new CameraProxy[numbers];
            mCameraId = new int[numbers];
            mCameraOpened = new boolean[numbers];
            mParameters = new Parameters[numbers];
            for (int i = 0; i < mNumberOfCameras && i < CameraSettings.MAX_SUPPORT_CAMERAS; i++) {
                mInfo[i] = new CameraInfo();
                try {
                    android.hardware.Camera.getCameraInfo(i, mInfo[i]);
                } catch (RuntimeException e) {
                    Log.w(TAG, "camera_surfaceview " + i + " maybe doesn't exist");
                }
                mCameraId[i] = -1;
            }
        }

        // get the first (smallest) back and first front camera_surfaceview id
        for (int i = 0; i < mNumberOfCameras && i < CameraSettings.MAX_SUPPORT_CAMERAS; i++) {
            if (mBackCameraId == -1 && mInfo[i].facing == CameraInfo.CAMERA_FACING_BACK) {
                mBackCameraId = i;
            } else if (mFrontCameraId == -1 && mInfo[i].facing == CameraInfo.CAMERA_FACING_FRONT) {
                mFrontCameraId = i;
            }
        }
    }

    public int getNumberOfCameras() {
        int number = android.hardware.Camera.getNumberOfCameras();
        try {
            if (number != mNumberOfCameras) {
                SLog.e(TAG, "CameraHolder::getNumberOfCameras. Numbers:" + number);
                mNumberOfCameras = number;
                if (mInfo != null) {
                    for (int i = 0; i < mNumberOfCameras; i++) {
                        mInfo[i] = new CameraInfo();
                        android.hardware.Camera.getCameraInfo(i, mInfo[i]);
                        mCameraId[i] = -1;
                    }
                    // get the first (smallest) back and first front camera_surfaceview id
                    for (int i = 0; i < mNumberOfCameras; i++) {
                        if (mBackCameraId == -1 && mInfo[i].facing == CameraInfo.CAMERA_FACING_BACK) {
                            mBackCameraId = i;
                        } else if (mFrontCameraId == -1 && mInfo[i].facing == CameraInfo.CAMERA_FACING_FRONT) {
                            mFrontCameraId = i;
                        }
                    }
                }
            }
            return mNumberOfCameras;
        } catch (RuntimeException e) {
            SLog.e(TAG, "CameraHolder::getNumberOfCameras error! " + e.toString());
        }
        return number;
    }

    public CameraInfo[] getCameraInfo() {
        return mInfo;
    }

    public synchronized CameraProxy open(Handler handler, int cameraId, CameraOpenCallback cb) {
        return open(handler, cameraId, cb, false, false, null);
    }

    public synchronized <T> CameraProxy open(Handler handler, int cameraId, CameraOpenCallback cb, boolean preview, boolean record, T t) {
        if (DEBUG_OPEN_RELEASE) {
            collectState(cameraId, mCameraDevice[cameraId]);
            if (mCameraOpened[cameraId]) {
                Log.e(TAG, "double open");
                dumpStates();
            }
        }

        if (mCameraDevice[cameraId] == null) {
            if (mMockCameraInfo == null) {
                SLog.d(TAG, "Open Camera " + cameraId + " now. preview:" + preview);
                mCameraDevice[cameraId] = CameraManagerFactory.getAndroidCameraManager(cameraId).cameraOpen(handler, cb, preview, record, t);
            } else {
                SLog.d(TAG, "mMockCameraInfo is not null");
                if (mMockCamera != null) {
                    mCameraDevice[cameraId] = mMockCamera[cameraId];
                } else {
                    SLog.e(TAG, "MockCameraInfo found, but no MockCamera provided.");
                    mCameraDevice[cameraId] = null;
                }
            }
            if (mCameraDevice[cameraId] == null) {
                SLog.e(TAG, "fail to connect Camera:" + mCameraId + ", aborting.");
                return null;
            }
            mCameraId[cameraId] = cameraId;
            mParameters[cameraId] = mCameraDevice[cameraId].getParameters();
        } else {
            if (!mCameraDevice[cameraId].reconnect(handler, cb)) {
                Log.e(TAG, "fail to reconnect Camera:" + mCameraId + ", aborting.");
                return null;
            }
            mCameraDevice[cameraId].setParameters(mParameters[cameraId]);
        }
        mCameraOpened[cameraId] = true;
        return mCameraDevice[cameraId];
    }

    public Camera getCamera(int id) {
        if (id < 0 || id >= mCameraDevice.length) return null;
        if (mCameraDevice != null && mCameraDevice.length > id && mCameraDevice[id] != null) {
            return mCameraDevice[id].getCamera();
        }
        return null;
    }

    public Camera.Parameters getParameters(int id) {
        if (mCameraDevice != null && mCameraDevice.length > id && mCameraDevice[id] != null) {
            return mCameraDevice[id].getParameters();
        }
        return null;
    }

    public void setParameters(int id, Camera.Parameters parameters) {
        if (mCameraDevice != null && mCameraDevice.length > id && mCameraDevice[id] != null) {
            mCameraDevice[id].setParameters(parameters);
        }
    }

    /**
     * Tries to open the hardware camera_surfaceview. If the camera_surfaceview is being used or
     * unavailable then return {@code null}.
     */
    public synchronized CameraProxy tryOpen(Handler handler, int cameraId, CameraOpenCallback cb) {
        return (!mCameraOpened[cameraId] ? open(handler, cameraId, cb) : null);
    }

    public synchronized void release(int id) {
        if (mCameraDevice == null) return;
        if (id < 0 || id >= CameraSettings.MAX_SUPPORT_CAMERAS) return;
        if (DEBUG_OPEN_RELEASE) {
            collectState(mCameraId[id], mCameraDevice[id]);
        }

        if (mCameraDevice[id] == null) return;

        strongRelease(id);
    }

    public synchronized void strongRelease(int id) {
        if (mCameraDevice == null) return;
        if (id < 0 || id >= CameraSettings.MAX_SUPPORT_CAMERAS) return;
        if (mCameraDevice[id] == null) return;

        if (mCameraOpened[id]) {
            mCameraOpened[id] = false;
            mCameraDevice[id].stopPreview();
        }

        mCameraOpened[id] = false;
        mCameraDevice[id].release();
        mCameraDevice[id] = null;
        // We must set this to null because it has a reference to Camera.
        // Camera has references to the listeners.
        mParameters[id] = null;
        mCameraId[id] = -1;
    }

    public int getBackCameraId() {
        return mBackCameraId;
    }

    public int getFrontCameraId() {
        return mFrontCameraId;
    }
}
