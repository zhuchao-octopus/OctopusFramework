/*
 * Copyright (C) 2012 The Android Open Source Project
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

import android.annotation.TargetApi;
import android.graphics.SurfaceTexture;
import android.hardware.Camera;
import android.hardware.Camera.ErrorCallback;
import android.hardware.Camera.OnZoomChangeListener;
import android.hardware.Camera.Parameters;
import android.os.Build;
import android.os.Handler;
import android.view.SurfaceHolder;

/**
 * An interface which provides possible camera_surfaceview device operations.
 *
 * The client should call {@code CameraManager.cameraOpen} to get an instance
 * of {@link CameraProxy} to control the camera_surfaceview. Classes
 * implementing this interface should have its own one unique {@code Thread}
 * other than the main thread for camera_surfaceview operations. Camera device callbacks
 * are wrapped since the client should not deal with
 * {@code android.hardware.Camera} directly.
 *
 * TODO: provide callback interfaces for:
 * {@code android.hardware.Camera.ErrorCallback},
 * {@code android.hardware.Camera.OnZoomChangeListener}, and
 * {@code android.hardware.Camera.Parameters}.
 */
public interface CameraManager {
    /**
     * An interface which wraps
     * {@link Camera.AutoFocusCallback}.
     */
    public interface CameraAFCallback {
        public void onAutoFocus(boolean focused, CameraProxy camera);
    }

    /**
     * An interface which wraps
     * {@link Camera.AutoFocusMoveCallback}.
     */
    public interface CameraAFMoveCallback {
        public void onAutoFocusMoving(boolean moving, CameraProxy camera);
    }

    /**
     * An interface which wraps
     * {@link Camera.ShutterCallback}.
     */
    public interface CameraShutterCallback {
        public void onShutter(CameraProxy camera);
    }

    /**
     * An interface which wraps
     * {@link Camera.PictureCallback}.
     */
    public interface CameraPictureCallback {
        public void onPictureTaken(byte[] data, CameraProxy camera);
    }

    /**
     * An interface which wraps
     * {@link Camera.PreviewCallback}.
     */
    public interface CameraPreviewDataCallback {
        public void onPreviewFrame(byte[] data, CameraProxy camera);
    }

    
    
    /**
     * wo jiad 
     * @author Administrator
     *
     */
    public interface CameraStartPreviewCallback {
        public void onSuccess(int cameraId, boolean record);

        public void onFailure(int cameraId, int reason);
    }

    
    /**
     * An interface which wraps
     * {@link Camera.FaceDetectionListener}.
     */
    public interface CameraFaceDetectionCallback {
        /**
         * Callback for face detection.
         *
         * @param faces   Recognized face in the preview.
         * @param camera  The camera_surfaceview which the preview image comes from.
         */
        public void onFaceDetection(Camera.Face[] faces, CameraProxy camera);
    }

    /**
     * An interface to be called for any exception caught when opening the
     * camera_surfaceview device. This error callback is different from the one defined
     * in the framework, {@link ErrorCallback}, which
     * is used after the camera_surfaceview is opened.
     */
    public interface CameraOpenCallback {
        /**
         * Callback when {@link com.android.camera.CameraDisabledException} is
         * caught.
         *
         * @param cameraId The disabled camera_surfaceview.
         */
        public <T> void onSuccess(int cameraId, boolean preview, boolean record, T t);

        /**
         * Callback when {@link com.android.camera.CameraHardwareException} is
         * caught.
         *
         * @param cameraId The camera_surfaceview with the hardware failure.
         */
        public void onFailure(int cameraId, int reason);
    }
    /**
     * Opens the camera_surfaceview of the specified ID synchronously.
     *
     * @param handler The {@link Handler} in which the callback
     *                was handled.
     * @param callback The callback when any error happens.
     * @param cameraId The camera_surfaceview ID to open.
     * @return   An instance of {@link CameraProxy} on success. null on failure.
     */
    public CameraProxy cameraOpen(Handler handler, CameraOpenCallback callback);

    public <T> CameraProxy cameraOpen(Handler handler, CameraOpenCallback callback, boolean preview, boolean record, T t);
    

    /**
     * An interface that takes camera_surfaceview operation requests and post messages to the
     * camera_surfaceview handler thread. All camera_surfaceview operations made through this interface is
     * asynchronous by default except those mentioned specifically.
     */
    public interface CameraProxy {

        /**
         * Returns the underlying {@link Camera} object used
         * by this proxy. This method should only be used when handing the
         * camera_surfaceview device over to {@link android.media.MediaRecorder} for
         * recording.
         */
        public Camera getCamera();

        /**
         * Releases the camera_surfaceview device synchronously.
         * This function must be synchronous so the caller knows exactly when the camera_surfaceview
         * is released and can continue on.
         */
        public void release();

        /**
         * Reconnects to the camera_surfaceview device.
         * @see Camera#reconnect()
         *
         * @param handler The {@link Handler} in which the callback
         *                was handled.
         * @param cb The callback when any error happens.
         * @return {@code false} on errors.
         */
        public boolean reconnect(Handler handler, CameraOpenCallback cb);

        /**
         * Unlocks the camera_surfaceview device.
         *
         * @see Camera#unlock()
         */
        public void unlock();

        /**
         * Locks the camera_surfaceview device.
         * @see Camera#lock()
         */
        public void lock();

        /**
         * Sets the {@link SurfaceTexture} for preview.
         *
         * @param surfaceTexture The {@link SurfaceTexture} for preview.
         */
        public void setPreviewTexture(final SurfaceTexture surfaceTexture);

        /**
         * Sets the {@link SurfaceHolder} for preview.
         *
         * @param surfaceHolder The {@link SurfaceHolder} for preview.
         */
        public void setPreviewDisplay(final SurfaceHolder surfaceHolder);
        public void setPreviewDisplay();
        /**
         * Starts the camera_surfaceview preview.
         */
        public void startPreview(Handler handler, CameraStartPreviewCallback cb);

        public void startPreview(Handler handler, CameraStartPreviewCallback cb, boolean recorder);

        public void startPreview();

        /**
         * Stops the camera_surfaceview preview synchronously.
         * {@code stopPreview()} must be synchronous to ensure that the caller can
         * continues to release resources related to camera_surfaceview preview.
         */
        public void stopPreview();

        /**
         * Sets the callback for preview data.
         *
         * @param handler    The {@link Handler} in which the callback was handled.
         * @param cb         The callback to be invoked when the preview data is available.
         * @see  Camera#setPreviewCallback(Camera.PreviewCallback)
         */
        public void setPreviewDataCallback(Handler handler, CameraPreviewDataCallback cb);

        public void setPreviewCallbackWithBuffer(Handler handler, Camera.PreviewCallback cb);

        /**
         * Sets the callback for preview data.
         *
         * @param handler The handler in which the callback will be invoked.
         * @param cb      The callback to be invoked when the preview data is available.
         * @see Camera#setPreviewCallbackWithBuffer(Camera.PreviewCallback)
         */
        public void setPreviewDataCallbackWithBuffer(Handler handler, CameraPreviewDataCallback cb);

        /**
         * Adds buffer for the preview callback.
         *
         * @param callbackBuffer The buffer allocated for the preview data.
         */
        public void addCallbackBuffer(byte[] callbackBuffer);

        /**
         * Starts the auto-focus process. The result will be returned through the callback.
         *
         * @param handler The handler in which the callback will be invoked.
         * @param cb      The auto-focus callback.
         */
        public void autoFocus(Handler handler, CameraAFCallback cb);

        /**
         * Cancels the auto-focus process.
         */
        public void cancelAutoFocus();

        /**
         * Sets the auto-focus callback
         *
         * @param handler The handler in which the callback will be invoked.
         * @param cb      The callback to be invoked when the preview data is available.
         */
        @TargetApi(Build.VERSION_CODES.JELLY_BEAN)
        public void setAutoFocusMoveCallback(Handler handler, CameraAFMoveCallback cb);

        /**
         * Instrument the camera_surfaceview to take a picture.
         *
         * @param handler   The handler in which the callback will be invoked.
         * @param shutter   The callback for shutter action, may be null.
         * @param raw       The callback for uncompressed data, may be null.
         * @param postview  The callback for postview image data, may be null.
         * @param jpeg      The callback for jpeg image data, may be null.
         * @see Camera#takePicture(
         *         Camera.ShutterCallback,
         *         Camera.PictureCallback,
         *         Camera.PictureCallback)
         */
        public void takePicture(Handler handler, CameraShutterCallback shutter, CameraPictureCallback raw, CameraPictureCallback postview, CameraPictureCallback jpeg);

        /**
         * Sets the display orientation for camera_surfaceview to adjust the preview orientation.
         *
         * @param degrees The rotation in degrees. Should be 0, 90, 180 or 270.
         */
        public void setDisplayOrientation(int degrees);

        /**
         * Sets the listener for zoom change.
         *
         * @param listener The listener.
         */
        public void setZoomChangeListener(OnZoomChangeListener listener);

        /**
         * Sets the face detection listener.
         *
         * @param handler  The handler in which the callback will be invoked.
         * @param callback The callback for face detection results.
         */
        public void setFaceDetectionCallback(Handler handler, CameraFaceDetectionCallback callback);

        /**
         * Starts the face detection.
         */
        public void startFaceDetection();

        /**
         * Stops the face detection.
         */
        public void stopFaceDetection();

        /**
         * Registers an error callback.
         *
         * @param cb The error callback.
         * @see Camera#setErrorCallback(ErrorCallback)
         */
        public void setErrorCallback(ErrorCallback cb);

        /**
         * Sets the camera_surfaceview parameters.
         *
         * @param params The camera_surfaceview parameters to use.
         */
        public void setParameters(Parameters params);

        /**
         * Gets the current camera_surfaceview parameters synchronously. This method is
         * synchronous since the caller has to wait for the camera_surfaceview to return
         * the parameters. If the parameters are already cached, it returns
         * immediately.
         */
        public Parameters getParameters();

        /**
         * Forces {@code CameraProxy} to update the cached version of the camera_surfaceview
         * parameters regardless of the dirty bit.
         */
        public void refreshParameters();

        /**
         * Enables/Disables the camera_surfaceview shutter sound.
         *
         * @param enable   {@code true} to enable the shutter sound,
         *                 {@code false} to disable it.
         */
        public void enableShutterSound(boolean enable);
    }
}
