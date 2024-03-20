/*
 * Copyright (C) 2013 The Android Open Source Project
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


import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.graphics.SurfaceTexture;
import android.hardware.Camera;
import android.hardware.Camera.AutoFocusCallback;
import android.hardware.Camera.AutoFocusMoveCallback;
import android.hardware.Camera.ErrorCallback;
import android.hardware.Camera.FaceDetectionListener;
import android.hardware.Camera.OnZoomChangeListener;
import android.hardware.Camera.Parameters;
import android.hardware.Camera.PictureCallback;
import android.hardware.Camera.PreviewCallback;
import android.hardware.Camera.ShutterCallback;
import android.os.Build;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;
import android.os.Message;
import android.util.Log;
import android.view.SurfaceHolder;


import com.rockchip.car.recorder.utils.SLog;

import java.io.IOException;

/**
 * A class to implement {@link CameraManager} of the Android camera_surfaceview framework.
 */
@SuppressLint("NewApi")
class AndroidCameraManagerImpl implements CameraManager {
    private static final String TAG = "CAM_" +AndroidCameraManagerImpl.class.getSimpleName();

    private Parameters mParameters;
    private boolean mParametersIsDirty;
    private IOException mReconnectIOException;

    /* Messages used in CameraHandler. */
    // Camera initialization/finalization
    private static final int OPEN_CAMERA = 1;
    private static final int RELEASE =     2;
    private static final int RECONNECT =   3;
    private static final int UNLOCK =      4;
    private static final int LOCK =        5;
    // Preview
    private static final int SET_PREVIEW_TEXTURE_ASYNC =        101;
    private static final int START_PREVIEW_ASYNC =              102;
    private static final int STOP_PREVIEW =                     103;
    private static final int SET_PREVIEW_DATA_CALLBACK_WITH_BUFFER = 104;
    private static final int ADD_CALLBACK_BUFFER =              105;
    private static final int SET_PREVIEW_DISPLAY_ASYNC =        106;
    private static final int SET_PREVIEW_DATA_CALLBACK =             107;
    private static final int SET_PREVIEW_CALLBACK_WITH_BUFFER =             108;
    // Parameters
    private static final int SET_PARAMETERS =     201;
    private static final int GET_PARAMETERS =     202;
    private static final int REFRESH_PARAMETERS = 203;
    // Focus, Zoom
    private static final int AUTO_FOCUS =                   301;
    private static final int CANCEL_AUTO_FOCUS =            302;
    private static final int SET_AUTO_FOCUS_MOVE_CALLBACK = 303;
    private static final int SET_ZOOM_CHANGE_LISTENER =     304;
    // Face detection
    private static final int SET_FACE_DETECTION_LISTENER = 461;
    private static final int START_FACE_DETECTION =        462;
    private static final int STOP_FACE_DETECTION =         463;
    private static final int SET_ERROR_CALLBACK =          464;
    // Presentation
    private static final int ENABLE_SHUTTER_SOUND =    501;
    private static final int SET_DISPLAY_ORIENTATION = 502;

    private CameraHandler mCameraHandler;
    private Camera mCamera;

    private Object mSurface;

    // Used to retain a copy of Parameters for setting parameters.
    private Parameters mParamsToSet;

    private int mCameraId;

    AndroidCameraManagerImpl(int id) {
        HandlerThread ht = new HandlerThread("Camera Handler Thread");
        ht.start();
        mCameraHandler = new CameraHandler(ht.getLooper());
        this.mCameraId = id;
    }

    private class CameraHandler extends Handler {
        CameraHandler(Looper looper) {
            super(looper);
        }

        private void startFaceDetection() {
            if (mCamera != null) {
                mCamera.startFaceDetection();
            }
        }

        private void stopFaceDetection() {
            if (mCamera != null) {
                mCamera.stopFaceDetection();
            }
        }

        private void setFaceDetectionListener(FaceDetectionListener listener) {
            if (mCamera != null) {
                mCamera.setFaceDetectionListener(listener);
            }
        }

        private void setPreviewTexture(Object surfaceTexture) {
            try {
                if (mCamera != null) {
                    mCamera.setPreviewTexture((SurfaceTexture) surfaceTexture);
                }
            } catch (IOException e) {
                Log.e(TAG, "Could not set preview texture", e);
            }
        }

        @TargetApi(Build.VERSION_CODES.JELLY_BEAN_MR1)
        private void enableShutterSound(boolean enable) {
            if (mCamera == null) {
                mCamera.enableShutterSound(enable);
            }
        }

        @TargetApi(Build.VERSION_CODES.JELLY_BEAN)
        private void setAutoFocusMoveCallback(Camera camera, Object cb) {
            camera.setAutoFocusMoveCallback((AutoFocusMoveCallback) cb);
        }

        public void requestTakePicture(final ShutterCallback shutter, final PictureCallback raw, final PictureCallback postView, final PictureCallback jpeg, final int id) {
            post(new Runnable() {
                @Override
                public void run() {
                    try {
                        if (mCamera != null) {
                            mCamera.takePicture(shutter, raw, postView, jpeg);
                        }
                    } catch (RuntimeException e) {
                        Log.e(TAG, "take picture failed.");
                        throw e;
                    }
                }
            });
        }

        /**
         * Waits for all the {@code Message} and {@code Runnable} currently in the queue
         * are processed.
         *
         * @return {@code false} if the wait was interrupted, {@code true} otherwise.
         */
        public boolean waitDone() {
            final Object waitDoneLock = new Object();
            final Runnable unlockRunnable = new Runnable() {
                @Override
                public void run() {
                    synchronized (waitDoneLock) {
                        waitDoneLock.notifyAll();
                    }
                }
            };

            synchronized (waitDoneLock) {
                mCameraHandler.post(unlockRunnable);
                try {
                    waitDoneLock.wait();
                } catch (InterruptedException ex) {
                    Log.d(TAG, "waitDone interrupted");
                    return false;
                }
            }
            return true;
        }

        /**
         * This method does not deal with the API level check.  Everyone should
         * check first for supported operations before sending message to this handler.
         */
        @Override
        public void handleMessage(final Message msg) {
            try {
                switch (msg.what) {
                    case OPEN_CAMERA:
                        try {
                            SLog.v(TAG, "AndroidCameraManagerImpl::OPEN_CAMERA " + mCameraId + " start");
//                            if(mCameraId == 0){
                            	mCamera = Camera.open(mCameraId);
//                            }
                            SLog.v(TAG, "AndroidCameraManagerImpl::OPEN_CAMERA " + mCameraId + " end");
                            if (mCamera != null) {
                                mParametersIsDirty = true;

                                // Get a instance of Camera.Parameters for later use.
                                if (mParamsToSet == null) {
                                    mParamsToSet = mCamera.getParameters();
                                }
                                if (msg.obj != null) {
                                    boolean preview = false;
                                    boolean record = false;
                                    if (msg.arg1 == 1) {
                                        preview = true;
                                    }
                                    if (msg.arg2 == 1) {
                                        record = true;
                                    }
                                    ((CameraOpenCallback) msg.obj).onSuccess(mCameraId, preview, record, mSurface);
                                    mSurface = null;
                                }
                            } else {
                                if (msg.obj != null) {
                                    ((CameraOpenCallback) msg.obj).onFailure(mCameraId, -1);
                                }
                            }
                        } catch (RuntimeException e) {
                            SLog.e(TAG, "AndroidCameraManagerImpl::OPEN_CAMERA " + mCameraId +" failed.", e);
                            e.printStackTrace();
                            mCamera = null;
                            mParamsToSet = null;
                        }
                        return;

                    case RELEASE:
                        if (mCamera != null) {
                            mCamera.release();
                            mCamera = null;
                        }
                        return;

                    case RECONNECT:
                        mReconnectIOException = null;
                        try {
                            if (mCamera != null) {
                                mCamera.reconnect();
                            }
                        } catch (IOException ex) {
                            mReconnectIOException = ex;
                        }
                        return;

                    case UNLOCK:
                        if (mCamera != null) {
                            SLog.v(TAG, "AndroidCameraManagerImpl::handleMessage. Camera " + mCameraId + " UNLOCK start");
                            mCamera.unlock();
                            SLog.v(TAG, "AndroidCameraManagerImpl::handleMessage. Camera " + mCameraId + " UNLOCK end");
                        }
                        return;

                    case LOCK:
                        if (mCamera != null) {
                            SLog.v(TAG, "AndroidCameraManagerImpl::handleMessage. Camera " + mCameraId + " LOCK start");
                            mCamera.lock();
                            SLog.v(TAG, "AndroidCameraManagerImpl::handleMessage. Camera " + mCameraId + " LOCK end");
                        }
                        return;

                    case SET_PREVIEW_TEXTURE_ASYNC:
                        setPreviewTexture(msg.obj);
                        return;

                    case SET_PREVIEW_DISPLAY_ASYNC:
                        try {
                            mCamera.setPreviewDisplay((SurfaceHolder) msg.obj);
                        } catch (IOException e) {
                            throw new RuntimeException(e);
                        }
                        return;

                    case START_PREVIEW_ASYNC:
                        if (mCamera != null) {
                        	 try {
                        		mCamera.startPreview();
                             } catch (Exception e) {
                            	 Log.d("allen", "START_PREVIEW_ASYNC fail!!!!!!!!!!!!!!");                                
                             }
                          
                        }
                        if (msg.obj != null) {
                            boolean record = false;
                            if (msg.arg1 == 1) {
                                record = true;
                            }
                            ((CameraStartPreviewCallback) msg.obj).onSuccess(mCameraId, record);
                        }
                        return;

                    case STOP_PREVIEW:
                        if (mCamera != null) {
                            mCamera.stopPreview();
                        }
                        return;

                    case SET_PREVIEW_DATA_CALLBACK_WITH_BUFFER:
                        if (mCamera != null) {
                            mCamera.setPreviewCallbackWithBuffer((PreviewCallback) msg.obj);
                        }
                        return;

                    case SET_PREVIEW_CALLBACK_WITH_BUFFER:
                        if (mCamera != null) {
                            mCamera.setPreviewCallbackWithBuffer((PreviewCallback) msg.obj);
                        }
                        return;

                    case ADD_CALLBACK_BUFFER:
                        if (mCamera != null) {
                            mCamera.addCallbackBuffer((byte[]) msg.obj);
                        }
                        return;

                    case AUTO_FOCUS:
                        if (mCamera != null) {
                            mCamera.autoFocus((AutoFocusCallback) msg.obj);
                        }
                        return;

                    case CANCEL_AUTO_FOCUS:
                        if (mCamera != null) {
                            mCamera.cancelAutoFocus();
                        }
                        return;

                    case SET_AUTO_FOCUS_MOVE_CALLBACK:
                        if (mCamera != null) {
                            setAutoFocusMoveCallback(mCamera, msg.obj);
                        }
                        return;

                    case SET_DISPLAY_ORIENTATION:
                        try {
                            if (mCamera != null) {
                                mCamera.setDisplayOrientation(msg.arg1);
                            }
                        } catch (RuntimeException e) {
                        }
                        return;

                    case SET_ZOOM_CHANGE_LISTENER:
                        if (mCamera != null) {
                            mCamera.setZoomChangeListener((OnZoomChangeListener) msg.obj);
                        }
                        return;

                    case SET_FACE_DETECTION_LISTENER:
                        setFaceDetectionListener((FaceDetectionListener) msg.obj);
                        return;

                    case START_FACE_DETECTION:
                        startFaceDetection();
                        return;

                    case STOP_FACE_DETECTION:
                        stopFaceDetection();
                        return;

                    case SET_ERROR_CALLBACK:
                        try {
                            if (mCamera != null) {
                                mCamera.setErrorCallback((ErrorCallback) msg.obj);
                            }
                        } catch (RuntimeException e) {
                        }
                        return;

                    case SET_PARAMETERS:
                        try {
                            if (mCamera != null) {
                                mParametersIsDirty = true;
                                mParamsToSet.unflatten((String) msg.obj);
                                mCamera.setParameters(mParamsToSet);
                            }
                        } catch (RuntimeException e) {
                            Log.w(TAG, "camera_surfaceview " + mCameraId + " setParameters occur RuntimeException");
                        }
                        return;

                    case GET_PARAMETERS:
                        if (mParametersIsDirty) {
                            try {
                                if (mCamera != null) {
                                    mParameters = mCamera.getParameters();
                                    mParametersIsDirty = false;
                                }
                            } catch (RuntimeException e) {
                                Log.w(TAG, "camera_surfaceview " + mCameraId + " getParameters occur RuntimeException");
                            }
                        }
                        return;

                    case SET_PREVIEW_DATA_CALLBACK:
                        if (mCamera != null) {
                            mCamera.setPreviewCallback((PreviewCallback) msg.obj);
                        }
                        return;

                    case ENABLE_SHUTTER_SOUND:
                        enableShutterSound((msg.arg1 == 1) ? true : false);
                        return;

                    case REFRESH_PARAMETERS:
                        mParametersIsDirty = true;
                        return;

                    default:
                        throw new RuntimeException("Invalid CameraProxy message=" + msg.what);
                }
            } catch (RuntimeException e) {
                if (msg.what != RELEASE && mCamera != null) {
                    try {
                        mCamera.release();
                    } catch (Exception ex) {
                        Log.e(TAG, "Fail to release the camera_surfaceview.");
                    }
                    mCamera = null;
                } else if (mCamera == null) {
                    Log.w(TAG, "Cannot handle message, mCamera is null.");
                }
                throw e;
            }
        }
    }

    public CameraProxy cameraOpen(Handler handler, CameraOpenCallback callback) {
        return cameraOpen(handler, callback, false, false, null);
    }

    @Override
    public <T> CameraProxy cameraOpen(Handler handler, CameraOpenCallback callback, boolean preview, boolean record, T t) {
        Log.d(TAG, "AndroidCameraManagerImpl::cameraOpen. preview:" + preview);
        if (preview) {
            mSurface = t;
        }
        mCameraHandler.obtainMessage(OPEN_CAMERA, preview ? 1 : 0, record ? 1 : 0, CameraOpenErrorCallbackForward.getNewInstance(handler, callback)).sendToTarget();
        mCameraHandler.waitDone();
        if (mCamera != null) {
            return new AndroidCameraProxyImpl(mCameraId);
        } else {
            return null;
        }
    }

    /**
     * A class which implements {@link CameraProxy} and
     * camera_surfaceview handler thread.
     * TODO: Save the handler for the callback here to avoid passing the same
     * handler multiple times.
     */
    public class AndroidCameraProxyImpl implements CameraProxy {
        public int mId = -1;
        private AndroidCameraProxyImpl(int id) {
            mId = id;
        }

        @Override
        public Camera getCamera() {
            return mCamera;
        }

        @Override
        public void release() {
            // release() must be synchronous so we know exactly when the camera_surfaceview
            // is released and can continue on.
            //mCameraHandler.sendEmptyMessage(RELEASE);
            mCameraHandler.obtainMessage(RELEASE).sendToTarget();
            mCameraHandler.waitDone();
        }

        @Override
        public boolean reconnect(Handler handler, CameraOpenCallback cb) {
            //mCameraHandler.sendEmptyMessage(RECONNECT);
            mCameraHandler.obtainMessage(RECONNECT).sendToTarget();
            mCameraHandler.waitDone();
            CameraOpenCallback cbforward = CameraOpenErrorCallbackForward.getNewInstance(handler, cb);
            if (mReconnectIOException != null) {
                if (cbforward != null) {
                    cbforward.onFailure(mCameraId, -1);
                }
                return false;
            }
            return true;
        }

        @Override
        public void unlock() {
            //mCameraHandler.sendEmptyMessage(UNLOCK);
            mCameraHandler.obtainMessage(UNLOCK).sendToTarget();
            mCameraHandler.waitDone();
        }

        @Override
        public void lock() {
            mCameraHandler.obtainMessage(LOCK).sendToTarget();
            mCameraHandler.waitDone();
            //mCameraHandler.sendEmptyMessage(LOCK);
        }

        @Override
        public void setPreviewTexture(SurfaceTexture surfaceTexture) {
            mCameraHandler.obtainMessage(SET_PREVIEW_TEXTURE_ASYNC, surfaceTexture).sendToTarget();
            mCameraHandler.waitDone();
        }

        @Override
        public void setPreviewDisplay(SurfaceHolder surfaceHolder) {
            mCameraHandler.obtainMessage(SET_PREVIEW_DISPLAY_ASYNC, surfaceHolder).sendToTarget();
            mCameraHandler.waitDone();
        }

        public void setPreviewDisplay() {
            mCameraHandler.obtainMessage(SET_PREVIEW_DISPLAY_ASYNC, CameraHolder.instance().getHolder(mId)).sendToTarget();
            mCameraHandler.waitDone();
        }
        @Override
        public void startPreview(Handler handler, CameraStartPreviewCallback cb) {
            mCameraHandler.obtainMessage(START_PREVIEW_ASYNC, CameraStartPreviewCallbackForward.getNewInstance(mCameraHandler, cb)).sendToTarget();
            mCameraHandler.waitDone();
        }

        @Override
        public void startPreview(Handler handler, CameraStartPreviewCallback cb, boolean record) {
            mCameraHandler.obtainMessage(START_PREVIEW_ASYNC, record ? 1 : 0, -1, CameraStartPreviewCallbackForward.getNewInstance(mCameraHandler, cb)).sendToTarget();
            mCameraHandler.waitDone();
        }

        public void startPreview() {

        }

        @Override
        public void stopPreview() {
            mCameraHandler.obtainMessage(STOP_PREVIEW).sendToTarget();
            //mCameraHandler.sendEmptyMessage(STOP_PREVIEW);
            mCameraHandler.waitDone();
        }

        @Override
        public void setPreviewDataCallback(
                Handler handler, CameraPreviewDataCallback cb) {
            mCameraHandler.obtainMessage(SET_PREVIEW_DATA_CALLBACK, PreviewCallbackForward.getNewInstance(handler, this, cb)).sendToTarget();
            mCameraHandler.waitDone();
        }

        @Override
        public void setPreviewCallbackWithBuffer(Handler handler, PreviewCallback cb) {
            mCameraHandler.obtainMessage(SET_PREVIEW_CALLBACK_WITH_BUFFER, cb).sendToTarget();
            mCameraHandler.waitDone();
        }

        @Override
        public void setPreviewDataCallbackWithBuffer(
                Handler handler, CameraPreviewDataCallback cb) {
            mCameraHandler.obtainMessage(SET_PREVIEW_DATA_CALLBACK_WITH_BUFFER, PreviewCallbackForward.getNewInstance(handler, this, cb)).sendToTarget();
            mCameraHandler.waitDone();
        }

        @Override
        public void addCallbackBuffer(byte[] callbackBuffer) {
            mCameraHandler.obtainMessage(ADD_CALLBACK_BUFFER, callbackBuffer).sendToTarget();
            mCameraHandler.waitDone();
        }

        @Override
        public void autoFocus(Handler handler, CameraAFCallback cb) {
            mCameraHandler.obtainMessage(AUTO_FOCUS, AFCallbackForward.getNewInstance(handler, this, cb)).sendToTarget();
            mCameraHandler.waitDone();
        }

        @Override
        public void cancelAutoFocus() {
            mCameraHandler.removeMessages(AUTO_FOCUS);
            mCameraHandler.obtainMessage(CANCEL_AUTO_FOCUS).sendToTarget();
            mCameraHandler.waitDone();
            //mCameraHandler.sendEmptyMessage(CANCEL_AUTO_FOCUS);
        }

        @TargetApi(Build.VERSION_CODES.JELLY_BEAN)
        @Override
        public void setAutoFocusMoveCallback(Handler handler, CameraAFMoveCallback cb) {
            mCameraHandler.obtainMessage(SET_AUTO_FOCUS_MOVE_CALLBACK, AFMoveCallbackForward.getNewInstance(handler, this, cb)).sendToTarget();
            mCameraHandler.waitDone();
        }

        @Override
        public void takePicture(
                Handler handler,
                CameraShutterCallback shutter,
                CameraPictureCallback raw,
                CameraPictureCallback post,
                CameraPictureCallback jpeg) {
            mCameraHandler.requestTakePicture(
                    ShutterCallbackForward.getNewInstance(handler, this, shutter),
                    PictureCallbackForward.getNewInstance(handler, this, raw),
                    PictureCallbackForward.getNewInstance(handler, this, post),
                    PictureCallbackForward.getNewInstance(handler, this, jpeg),
                    mId);
        }

        @Override
        public void setDisplayOrientation(int degrees) {
            mCameraHandler.obtainMessage(SET_DISPLAY_ORIENTATION, degrees, 0).sendToTarget();
            mCameraHandler.waitDone();
        }

        @Override
        public void setZoomChangeListener(OnZoomChangeListener listener) {
            mCameraHandler.obtainMessage(SET_ZOOM_CHANGE_LISTENER, listener).sendToTarget();
            mCameraHandler.waitDone();
        }

        public void setFaceDetectionCallback(Handler handler, CameraFaceDetectionCallback cb) {
            mCameraHandler.obtainMessage(SET_FACE_DETECTION_LISTENER, FaceDetectionCallbackForward.getNewInstance(handler, this, cb)).sendToTarget();
            mCameraHandler.waitDone();
        }

        @Override
        public void startFaceDetection() {
            mCameraHandler.obtainMessage(START_FACE_DETECTION).sendToTarget();
            mCameraHandler.waitDone();
            //mCameraHandler.sendEmptyMessage(START_FACE_DETECTION);
        }

        @Override
        public void stopFaceDetection() {
            mCameraHandler.obtainMessage(STOP_FACE_DETECTION).sendToTarget();
            mCameraHandler.sendEmptyMessage(STOP_FACE_DETECTION);
            mCameraHandler.waitDone();
        }

        @Override
        public void setErrorCallback(ErrorCallback cb) {
            mCameraHandler.obtainMessage(SET_ERROR_CALLBACK, cb).sendToTarget();
            mCameraHandler.waitDone();
        }

        @Override
        public void setParameters(Parameters params) {
            if (params == null) {
                Log.v(TAG, "null parameters in setParameters()");
                return;
            }
            mCameraHandler.obtainMessage(SET_PARAMETERS, params.flatten()).sendToTarget();
            mCameraHandler.waitDone();
        }

        @Override
        public Parameters getParameters() {
            mCameraHandler.obtainMessage(GET_PARAMETERS).sendToTarget();
            //mCameraHandler.sendEmptyMessage(GET_PARAMETERS);
            mCameraHandler.waitDone();
            return mParameters;
        }

        @Override
        public void refreshParameters() {
            mCameraHandler.obtainMessage(REFRESH_PARAMETERS).sendToTarget();
            mCameraHandler.waitDone();
            //mCameraHandler.sendEmptyMessage(REFRESH_PARAMETERS);
        }

        @Override
        public void enableShutterSound(boolean enable) {
            mCameraHandler.obtainMessage(ENABLE_SHUTTER_SOUND, (enable ? 1 : 0), mId).sendToTarget();
            mCameraHandler.waitDone();
        }
    }

    /**
     * A helper class to forward AutoFocusCallback to another thread.
     */
    private static class AFCallbackForward implements AutoFocusCallback {
        private final Handler mHandler;
        private final CameraProxy mCamera;
        private final CameraAFCallback mCallback;

        /**
         * Returns a new instance of {@link AFCallbackForward}.
         *
         * @param handler The handler in which the callback will be invoked in.
         * @param camera  The {@link CameraProxy} which the callback is from.
         * @param cb      The callback to be invoked.
         * @return        The instance of the {@link AFCallbackForward},
         *                or null if any parameter is null.
         */
        public static AFCallbackForward getNewInstance(Handler handler, CameraProxy camera, CameraAFCallback cb) {
            if (handler == null || camera == null || cb == null) return null;
            return new AFCallbackForward(handler, camera, cb);
        }

        private AFCallbackForward(Handler h, CameraProxy camera, CameraAFCallback cb) {
            mHandler = h;
            mCamera = camera;
            mCallback = cb;
        }

        @Override
        public void onAutoFocus(final boolean b, Camera camera) {
            mHandler.post(new Runnable() {
                @Override
                public void run() {
                    mCallback.onAutoFocus(b, mCamera);
                }
            });
        }
    }

    /** A helper class to forward AutoFocusMoveCallback to another thread. */
    @TargetApi(Build.VERSION_CODES.JELLY_BEAN)
    private static class AFMoveCallbackForward implements AutoFocusMoveCallback {
        private final Handler mHandler;
        private final CameraAFMoveCallback mCallback;
        private final CameraProxy mCamera;

        /**
         * Returns a new instance of {@link AFMoveCallbackForward}.
         *
         * @param handler The handler in which the callback will be invoked in.
         * @param camera  The {@link CameraProxy} which the callback is from.
         * @param cb      The callback to be invoked.
         * @return        The instance of the {@link AFMoveCallbackForward},
         *                or null if any parameter is null.
         */
        public static AFMoveCallbackForward getNewInstance(Handler handler, CameraProxy camera, CameraAFMoveCallback cb) {
            if (handler == null || camera == null || cb == null) return null;
            return new AFMoveCallbackForward(handler, camera, cb);
        }

        private AFMoveCallbackForward(Handler h, CameraProxy camera, CameraAFMoveCallback cb) {
            mHandler = h;
            mCamera = camera;
            mCallback = cb;
        }

        @Override
        public void onAutoFocusMoving(final boolean moving, Camera camera) {
            mHandler.post(new Runnable() {
                @Override
                public void run() {
                    mCallback.onAutoFocusMoving(moving, mCamera);
                }
            });
        }
    }

    /**
     * A helper class to forward ShutterCallback to to another thread.
     */
    private static class ShutterCallbackForward implements ShutterCallback {
        private final Handler mHandler;
        private final CameraShutterCallback mCallback;
        private final CameraProxy mCamera;

        /**
         * Returns a new instance of {@link ShutterCallbackForward}.
         *
         * @param handler The handler in which the callback will be invoked in.
         * @param camera  The {@link CameraProxy} which the callback is from.
         * @param cb      The callback to be invoked.
         * @return        The instance of the {@link ShutterCallbackForward},
         *                or null if any parameter is null.
         */
        public static ShutterCallbackForward getNewInstance(Handler handler, CameraProxy camera, CameraShutterCallback cb) {
            if (handler == null || camera == null || cb == null) return null;
            return new ShutterCallbackForward(handler, camera, cb);
        }

        private ShutterCallbackForward(Handler h, CameraProxy camera, CameraShutterCallback cb) {
            mHandler = h;
            mCamera = camera;
            mCallback = cb;
        }

        @Override
        public void onShutter() {
            mHandler.post(new Runnable() {
                @Override
                public void run() {
                    mCallback.onShutter(mCamera);
                }
            });
        }
    }

    /**
     * A helper class to forward PictureCallback to another thread.
     */
    private static class PictureCallbackForward implements PictureCallback {
        private final Handler mHandler;
        private final CameraPictureCallback mCallback;
        private final CameraProxy mCamera;

        /**
         * Returns a new instance of {@link PictureCallbackForward}.
         *
         * @param handler The handler in which the callback will be invoked in.
         * @param camera  The {@link CameraProxy} which the callback is from.
         * @param cb      The callback to be invoked.
         * @return        The instance of the {@link PictureCallbackForward},
         *                or null if any parameters is null.
         */
        public static PictureCallbackForward getNewInstance(Handler handler, CameraProxy camera, CameraPictureCallback cb) {
            if (handler == null || camera == null || cb == null) return null;
            return new PictureCallbackForward(handler, camera, cb);
        }

        private PictureCallbackForward(Handler h, CameraProxy camera, CameraPictureCallback cb) {
            mHandler = h;
            mCamera = camera;
            mCallback = cb;
        }

        @Override
        public void onPictureTaken(final byte[] data, Camera camera) {
            mHandler.post(new Runnable() {
                @Override
                public void run() {
                    mCallback.onPictureTaken(data, mCamera);
                }
            });
        }
    }

    /**
     * A helper class to forward PreviewCallback to another thread.
     */
    private static class PreviewCallbackForward implements PreviewCallback {
        private final Handler mHandler;
        private final CameraPreviewDataCallback mCallback;
        private final CameraProxy mCamera;

        /**
         * Returns a new instance of {@link PreviewCallbackForward}.
         *
         * @param handler The handler in which the callback will be invoked in.
         * @param camera  The {@link CameraProxy} which the callback is from.
         * @param cb      The callback to be invoked.
         * @return        The instance of the {@link PreviewCallbackForward},
         *                or null if any parameters is null.
         */
        public static PreviewCallbackForward getNewInstance(Handler handler, CameraProxy camera, CameraPreviewDataCallback cb) {
            if (handler == null || camera == null || cb == null) return null;
            return new PreviewCallbackForward(handler, camera, cb);
        }

        private PreviewCallbackForward(Handler h, CameraProxy camera, CameraPreviewDataCallback cb) {
            mHandler = h;
            mCamera = camera;
            mCallback = cb;
        }

        @Override
        public void onPreviewFrame(final byte[] data, Camera camera) {
            mHandler.post(new Runnable() {
                @Override
                public void run() {
                    mCallback.onPreviewFrame(data, mCamera);
                }
            });
        }
    }
    
    
    
    private static class FaceDetectionCallbackForward implements FaceDetectionListener {
        private final Handler mHandler;
        private final CameraFaceDetectionCallback mCallback;
        private final CameraProxy mCamera;

        /**
         * Returns a new instance of {@link FaceDetectionCallbackForward}.
         *
         * @param handler The handler in which the callback will be invoked in.
         * @param camera  The {@link CameraProxy} which the callback is from.
         * @param cb      The callback to be invoked.
         * @return        The instance of the {@link FaceDetectionCallbackForward},
         *                or null if any parameter is null.
         */
        public static FaceDetectionCallbackForward getNewInstance(Handler handler, CameraProxy camera, CameraFaceDetectionCallback cb) {
            if (handler == null || camera == null || cb == null) return null;
            return new FaceDetectionCallbackForward(handler, camera, cb);
        }

        private FaceDetectionCallbackForward(Handler h, CameraProxy camera, CameraFaceDetectionCallback cb) {
            mHandler = h;
            mCamera = camera;
            mCallback = cb;
        }

        @Override
        public void onFaceDetection(final Camera.Face[] faces, Camera camera) {
            mHandler.post(new Runnable() {
                @Override
                public void run() {
                    mCallback.onFaceDetection(faces, mCamera);
                }
            });
        }
    }

    /**
     * A callback helps to invoke the original callback on another
     * {@link Handler}.
     */
    private static class CameraOpenErrorCallbackForward implements CameraOpenCallback {
        private final Handler mHandler;
        private final CameraOpenCallback mCallback;

        /**
         * Returns a new instance of {@link FaceDetectionCallbackForward}.
         *
         * @param handler The handler in which the callback will be invoked in.
         * @param cb The callback to be invoked.
         * @return The instance of the {@link FaceDetectionCallbackForward}, or
         *         null if any parameter is null.
         */
        public static CameraOpenErrorCallbackForward getNewInstance(Handler handler, CameraOpenCallback cb) {
            if (handler == null || cb == null) {
                return null;
            }
            return new CameraOpenErrorCallbackForward(handler, cb);
        }

        private CameraOpenErrorCallbackForward(Handler h, CameraOpenCallback cb) {
            mHandler = h;
            mCallback = cb;
        }

        @Override
        public <T> void onSuccess(final int cameraId, final boolean preview, final boolean record, final T t) {
            mHandler.post(new Runnable() {
                @Override
                public void run() {
                    mCallback.onSuccess(cameraId, preview, record, t);
                }
            });
        }

        @Override
        public void onFailure(final int cameraId, final int what) {
            mHandler.post(new Runnable() {
                @Override
                public void run() {
                    mCallback.onFailure(cameraId, what);
                }
            });
        }
    }

    private static class CameraStartPreviewCallbackForward implements CameraStartPreviewCallback {
        private final Handler mHandler;
        private final CameraStartPreviewCallback mCallback;

        /**
         * Returns a new instance of {@link FaceDetectionCallbackForward}.
         *
         * @param handler The handler in which the callback will be invoked in.
         * @param cb The callback to be invoked.
         * @return The instance of the {@link FaceDetectionCallbackForward}, or
         *         null if any parameter is null.
         */
        public static CameraStartPreviewCallbackForward getNewInstance(Handler handler, CameraStartPreviewCallback cb) {
            if (handler == null || cb == null) {
                return null;
            }
            return new CameraStartPreviewCallbackForward(handler, cb);
        }

        private CameraStartPreviewCallbackForward(Handler h, CameraStartPreviewCallback cb) {
            mHandler = h;
            mCallback = cb;
        }

        @Override
        public void onSuccess(final int cameraId, final boolean record) {
            // TODO Auto-generated method stub
            mHandler.post(new Runnable() {
                @Override
                public void run() {
                    mCallback.onSuccess(cameraId, record);
                }
            });
        }

        @Override
        public void onFailure(final  int cameraId, final int reason) {
            // TODO Auto-generated method stub
            mHandler.post(new Runnable() {
                @Override
                public void run() {
                    mCallback.onFailure(cameraId, reason);
                }
            });
        }
    }
}
