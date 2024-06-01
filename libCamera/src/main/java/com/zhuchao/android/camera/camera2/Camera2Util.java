package com.zhuchao.android.camera.camera2;

import android.content.Context;
import android.graphics.ImageFormat;
import android.graphics.SurfaceTexture;
import android.hardware.camera2.CameraAccessException;
import android.hardware.camera2.CameraCharacteristics;
import android.hardware.camera2.CameraManager;
import android.hardware.camera2.params.StreamConfigurationMap;
import android.util.Size;

import com.zhuchao.android.fbase.MMLog;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class Camera2Util {
    private static final String TAG = "Camera2Util";

    public static int getNumberOfCameras(Context context) {
        CameraManager manager = (CameraManager) context.getSystemService(Context.CAMERA_SERVICE);
        try {
            return manager.getCameraIdList().length;
        } catch (Throwable e) {
            MMLog.e(TAG, String.valueOf(e));
        }
        return 0;
    }

    public static String[] getAllCameras(Context context) {
        CameraManager manager = (CameraManager) context.getSystemService(Context.CAMERA_SERVICE);
        try {
            return manager.getCameraIdList();
        } catch (Throwable e) {
            MMLog.e(TAG, String.valueOf(e));
        }
        return null;
    }

    public static String[] printAllCameras(Context context) {
        CameraManager manager = (CameraManager) context.getSystemService(Context.CAMERA_SERVICE);
        try {
            String[] camList = manager.getCameraIdList();
            for (String cameraID : camList) {
                MMLog.d(TAG, "cameraID:" + cameraID + " " + manager.getCameraCharacteristics(cameraID).toString());
            }
        } catch (Throwable e) {
            MMLog.e(TAG, String.valueOf(e));
        }
        return null;
    }

    public static List<Camera2Config> getCameraInfo(Context context) {
        ArrayList<Camera2Config> camera2Configs = new ArrayList<>();
        CameraManager cameraManager = (CameraManager) context.getSystemService(Context.CAMERA_SERVICE);
        try {
            String[] cameraIdList = cameraManager.getCameraIdList();
            for (String cameraId : cameraIdList) {
                CameraCharacteristics cameraCharacteristics = cameraManager.getCameraCharacteristics(cameraId);
                StreamConfigurationMap map = cameraCharacteristics.get(CameraCharacteristics.SCALER_STREAM_CONFIGURATION_MAP);
                Size[] previewSizes = map.getOutputSizes(SurfaceTexture.class);
                Size[] pictureSizes = map.getOutputSizes(ImageFormat.JPEG);
                float[] ratios = {3.0f / 4.0f, 9.0f / 16.0f};  // limit size is 4:3 or 16:9
                Size previewSize = chooseOptimalSize(previewSizes, 1080, ratios);
                Size pictureSize = chooseOptimalSize(pictureSizes, 1080, ratios);
                Camera2Config camera2Config = new Camera2Config.Builder().setCameraId(cameraId).setPreviewSizes(Arrays.asList(previewSizes)).setPreviewSize(previewSize).setPictureSizes(Arrays.asList(pictureSizes)).setPictureSize(pictureSize).build();
                camera2Configs.add(camera2Config);
            }
        } catch (CameraAccessException e) {
            e.printStackTrace();
        }
        return camera2Configs;
    }

    public static Size chooseOptimalSize(Size[] sizes, int dstSize, float[] ratios) {
        if (sizes == null || sizes.length == 0) {
            return null;
        }
        int minDelta = Integer.MAX_VALUE;
        int index = 0;
        for (int i = 0; i < sizes.length; i++) {
            Size size = sizes[i];
            for (float ratio : ratios) {
                if (size.getWidth() * ratio == size.getHeight()) {
                    int delta = Math.abs(dstSize - size.getHeight());
                    if (delta == 0) {
                        return size;
                    }
                    if (minDelta > delta) {
                        minDelta = delta;
                        index = i;
                    }
                }
            }
        }
        return sizes[index];
    }
}
