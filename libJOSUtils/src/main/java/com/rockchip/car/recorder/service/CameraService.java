package com.rockchip.car.recorder.service;

import android.content.ContentResolver;
import android.content.Intent;
import android.graphics.ImageFormat;
import android.hardware.Camera;
import android.os.IBinder;
import android.os.SystemClock;

import com.rockchip.car.recorder.camera2.CameraHolder;
import com.rockchip.car.recorder.utils.SLog;
import com.rockchip.car.recorder.utils.SystemProperties;

import java.util.LinkedList;
import java.util.Queue;

/**
 * Created by Administrator on 2016/7/15.
 */
public class CameraService extends ServiceImpl {

    private static final String TAG = "CAM_CameraService2";
    private static CameraService mService;

    public void onCreate(ContentResolver c) {
        super.onCreate(c);
    }

//    public IBinder onBind(Intent intent) {
//        return super.onBind(intent);
//    }

    @Override
    public void afterOpenedBeforePreview() {

    }

    @Override
    public void afterPreviewedBeforeRecord() {

    }

    @Override
    public boolean open(int id) {
        SLog.d(TAG, "CameraService::open. id:" + id);
        boolean res = super.open(id);
        if (mCameraCallback != null) {
            mCameraCallback.setPreviewFormat(id, CameraHolder.instance().getParameters(id).getPreviewFormat());
        }
        return res;
    }

    public static CameraService getService() {
        return mService;
    }

    @Override
    public void setPreviewCallback(int id) {
        SLog.d(TAG, "CameraService::setPreviewCallback. id:" + id);
        if (mCameraInfos != null && mCameraInfos.get(id) != null && mCameraInfos.get(id).getCameraManager() != null) {
            Camera.Parameters parameters = mCameraInfos.get(id).getCameraManager().getParameters();
            if (parameters == null)
                return;
            CameraPreviewCallback cpc = getCameraInfos().get(id).getCameraPreviewCallback();
            if (mCameraCallback != null) {
                mCameraCallback.setRenderSize(false && !false ? (int) getSurfaceToCamera().get((int) getCameraToSurface().get(id) == 0 ? 1 : 0) : id,
                        parameters.getPreviewSize().width, parameters.getPreviewSize().height);
                mCameraCallback.setPreviewFormat(false && !false ? (int) getSurfaceToCamera().get((int) getCameraToSurface().get(id) == 0 ? 1 : 0) : id,
                        false && false ? ImageFormat.RGB_565 : parameters.getPreviewFormat());
            }
            if (cpc == null) {
                Camera.Size size = parameters.getPreviewSize();
                int buffer = size.width * size.height * ImageFormat.getBitsPerPixel(parameters.getPreviewFormat()) / 8;
               
                SLog.d(TAG,"new CameraPreviewCallback " + id + " buffer = " + buffer);
                CameraPreviewCallback callback = new CameraPreviewCallback(id, buffer);
//                getCameraInfos().get(id).setCameraPreviewCallback(callback);
                callback.addAll();
                mCameraInfos.get(id).getCameraManager().setPreviewCallbackWithBuffer(null, callback);//.setPreviewCallbackWithBuffer(callback);//new  CameraPreviewCallback(id, buffer));
            } else {
                cpc.addAll();
                mCameraInfos.get(id).getCameraManager().setPreviewCallbackWithBuffer(null, cpc);
            }
        }
    }

    public class CameraPreviewCallback implements Camera.PreviewCallback {
        private int mId;
        private int mBuffer;
        private Queue<byte[]> mBuffers;
        private byte[][] mBytes;
        private int mCount;
        private int mTimes;

        private byte[] mTmp;
        public  CameraPreviewCallback(int id, int buffer) {
            SLog.d(TAG, "CameraService.CameraPreviewCallback::CameraPreviewCallback. id:" + id + "; buffer:" + buffer);
            this.mId = id;
            this.mBuffer = buffer;
            mBuffers = new LinkedList<byte[]>();
            mBytes = new byte[][]{new byte[mBuffer], new byte[mBuffer],new byte[mBuffer]};
//            mBuffers.offer(mBytes[0]);
//            mBuffers.offer(mBytes[1]);
//            mBuffers.offer(mBytes[2]);
        }

        @Override
        public void onPreviewFrame(byte[] data, Camera camera) {
            if (mCount++ == 30) {
                mCount = 0;
                SLog.v(TAG, "CameraService::onPreviewFrame. " + mId + ", buffer lenght " + (data != null ? data.length : "null"));
            }
            if (data == null || data.length == 0) {
                SLog.d(TAG, "CameraService::onPreviewFrame. " + mId + ", data is null");
                if (mBuffers.size() == 0) {
                    mBuffers.offer(new byte[mBuffer]);
                }
                camera.addCallbackBuffer(mBuffers.poll());
                return;
            }
            frameRate(data);
            if (mIsChannelChanged && mTimes < Integer.parseInt(SystemProperties.get("persist.car.channel.switch", "15"))) {
                mTimes++;
                mBuffers.offer(data);
                camera.addCallbackBuffer(mBuffers.poll());
                return;
            }
            mIsChannelChanged = false;
            mTimes = 0;
            if (mCameraCallback != null) {
                mCameraCallback.requestRender(mId, data);
            } else {
                mBuffers.offer(data);
                camera.addCallbackBuffer(mBuffers.poll());
            }
        }

        public void addCallbackBuffer() {
            if (mCameraInfos.get(mId) == null || mCameraInfos.get(mId).getCameraManager() == null)
                return;
            mTmp = mBuffers.poll();
            if (mTmp != null) {
                mCameraInfos.get(mId).getCameraManager().addCallbackBuffer(mTmp);
            }
        }

        public void addCallbackBuffers() {
            if (mCameraInfos.get(mId) == null || mCameraInfos.get(mId).getCameraManager() == null)
                return;
            int size = mBuffers.size();
            if (size > 0) {
                for (int i = 0; i < size; i++) {
                    mTmp = mBuffers.poll();
                    if (mTmp != null) {
                        mCameraInfos.get(mId).getCameraManager().addCallbackBuffer(mTmp);
                    }
                }
            } else {
                for (byte[] by : mBytes) {
                    if (by != null) {
                        mCameraInfos.get(mId).getCameraManager().addCallbackBuffer(by);
                    }
                }
            }
        }

        public void addAll() {
            mBuffers.clear();
            for (int i=0; i<mBytes.length; i++) {
                mTmp = mBytes[i];
                if (mTmp != null) {
                    mCameraInfos.get(mId).getCameraManager().addCallbackBuffer(mTmp);
                }
            }
        }

        private long mLastPreviewTime;
        private int mPreviewCount;
        private void frameRate(byte[] data) {
            if (mLastPreviewTime == 0) {
                mLastPreviewTime = SystemClock.uptimeMillis();
            } else {
                if (SystemClock.uptimeMillis() - mLastPreviewTime > 3 * 1000) {
                    float frameRate = mPreviewCount * 1.0f / ((SystemClock.uptimeMillis() - mLastPreviewTime) * 1.0f / 1000);
                    mLastPreviewTime = SystemClock.uptimeMillis();
                    mPreviewCount = 0;
                    SLog.d(TAG, "Camera " + mId + " frame rate is " + frameRate + ", buffer lenght is " + data.length);
                } else {
                    mPreviewCount++;
                }
            }
        }
    }
}
