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

package com.rockchip.car.recorder.activity;

import android.graphics.Bitmap;
import android.util.Log;
import android.view.Gravity;
import android.view.SurfaceHolder;
import android.view.View;
import android.widget.FrameLayout;

import com.rockchip.car.recorder.camera2.CameraHolder;
import com.rockchip.car.recorder.render.GLFrameSurface;
import com.rockchip.car.recorder.service.BufferManager;
import com.rockchip.car.recorder.service.CameraService;
import com.rockchip.car.recorder.service.ServiceImpl;
import com.rockchip.car.recorder.utils.SLog;

import java.util.List;

public class VideoUI extends AVideoUI {
    private static final String TAG = "CAM_RVideoUI";

    protected GLFrameSurface[] mSurfaceViews;
    private VideoUI mThis;
    private int mOpenId = 0;

    public VideoUI(View surfaceView, int id) {
        super();
        SLog.d(TAG, "VideoUI::VideoUI()");
        mThis = this;
        mOpenId = id;
        this.mSurfaceViews = new GLFrameSurface[2];
        // initializeSurfaceView(id);
        // SLog.d(TAG, "VideoUI::initializeSurfaceView()");
        mSurfaceViews[id] = (GLFrameSurface) surfaceView;
        mSurfaceViews[id].setId(id, mCallbackRender);
        mSurfaceViews[id].getHolder().addCallback(new SurfaceCallback(id));
        surfaceVisible(id, View.VISIBLE);

        mCameraService = new CameraService();
        mCameraService.registerCallback(mICameraCallback);
        initMap();
        // connect();
    }

    protected ServiceImpl.CameraCallback mICameraCallback = new ServiceImpl.CameraCallback() {

        @Override
        public void setPreviewFormat(int id, int format) {
            mThis.setPreviewFormat(id, format);
        }

        @Override
        public void setRenderSize(int id, int width, int height) {
            mThis.setRenderSize(id, width, height);
        }

        @Override
        public void requestRender(int id, byte[] data) {
            mThis.requestRender(id, data);
        }

        @Override
        public void updateRecordIcon(final boolean recording, final boolean lock) {

        }

        @Override
        public void updateRecordTime(final long start, final long max) {

        }

        @Override
        public void surfaceVisible(final int id, final int visible) {
            Log.d("abcd", "surfaceVisible!!!!!!!!");
            // runOnUiThread(new Runnable() {
            // @Override
            // public void run() {
            // mVideoUI.surfaceVisible(id, visible);
            // }
            // });
        }

        @Override
        public void finishActivity() {

        }

        @Override
        public void usbHotPlugEvent(final int state) {

        }

        @Override
        public void drawAdasResult(Bitmap bitmap) {

        }
    };

    public void disconnect() {

    }

    public void connect() {
        //mCameraService.registerCallback(mICameraCallback);
        initMap();


        //	startRecordingDirect(id, null);


        // }
    }

    private void initMap() {
        // mCameraService.getSurfaceToCamera().clear();
        // mCameraService.getCameraToSurface().clear();
        int camera0 = 0;// getResources().getInteger(R.integer.surface0_to_camera);
        int camera1 = 1;// getResources().getInteger(R.integer.surface1_to_camera);
        if (camera0 >= 0) {
            mCameraService.getSurfaceToCamera().put(0, camera0);
            mCameraService.getCameraToSurface().put(camera0, 0);
        }
        if (camera1 >= 0) {
            mCameraService.getSurfaceToCamera().put(1, camera1);
            mCameraService.getCameraToSurface().put(camera1, 1);
        }
    }

    // public void initializeSurfaceView(int id) {
    // // super.initializeSurfaceView();
    // SLog.d(TAG, "VideoUI::initializeSurfaceView()");
    // mSurfaceViews[id] = (GLFrameSurface)
    // mRootView.findViewById(R.id.preview_content0);
    // mSurfaceViews[id].setId(0, mCallbackRender);
    // mSurfaceViews[id].getHolder().addCallback(new SurfaceCallback(0));
    // surfaceVisible(id, View.VISIBLE );
    // // mSurfaceViews[1] = (GLFrameSurface)
    // mRootView.findViewById(R.id.preview_content0);
    // // mSurfaceViews[1].setId(1, mCallbackRender);
    // // mSurfaceViews[1].getHolder().addCallback(new SurfaceCallback(1));
    // // mSurfaceViews[1].setZOrderMediaOverlay(true);
    // // surfaceVisible(1, View.VISIBLE );
    // }

    public void scaleSurface(int id) {
        android.widget.FrameLayout.LayoutParams params = (FrameLayout.LayoutParams) mSurfaceViews[id].getLayoutParams();
        params.width = mSurfaceViews[id].getWidth();
        params.height = mSurfaceViews[id].getHeight();
        params.gravity = Gravity.TOP | Gravity.LEFT;

        // int sWidth = (int)
        // mActivity.getResources().getDimension(R.dimen.pic_in_pic_width);
        // int sHeight = (int)
        // mActivity.getResources().getDimension(R.dimen.pic_in_pic_height);
        // if (params.width == sWidth && params.height == sHeight) {
        params.width = params.height = FrameLayout.LayoutParams.MATCH_PARENT;
        // } else {
        // params.width = sWidth;
        // params.height = sHeight;
        // }
        mSurfaceViews[id].setLayoutParams(params);
    }

    @Override
    public void setPreviewFormat(int id, int format) {
        if (mSurfaceViews != null && mSurfaceViews.length > id && mSurfaceViews[id] != null) {
            mSurfaceViews[id].setPreviewFormat(id, format);
        }
    }

    @Override
    public void setRenderSize(int id, int width, int height) {
        if (mCameraService == null || mCameraService.getCameraToSurface() == null) return;
        int surfaceId = (int) mCameraService.getCameraToSurface().get(id);
        SLog.d(TAG, "VideoUI::setRenderSize. id:" + id + "; surfaceId:" + surfaceId + "; width:" + width + "; height:" + height);
        if (mSurfaceViews != null && mSurfaceViews.length > surfaceId && mSurfaceViews[surfaceId] != null) {
            mSurfaceViews[surfaceId].setRenderSize(surfaceId, width, height);
        }
    }

    @Override
    public void requestRender(int id, byte[] data) {
        int surfaceId = (int) mCameraService.getCameraToSurface().get(id);
        if (mSurfaceViews != null && mSurfaceViews.length > surfaceId && mSurfaceViews[surfaceId] != null) {
            BufferManager.getInstance(surfaceId).push(data);
            boolean result = mSurfaceViews[surfaceId].requestRender(id, data);
            if (!result) {
                SLog.d(TAG, "VideoUI::requestRender. id:" + id + "data:" + (data != null ? data.length : null));
                mCameraService.addCallbackBuffer(id, BufferManager.getInstance(id).pull());
            }
        }
    }

    private List<Surfaces> mSurfaceInfos;

    @Override
    public void reverse(final int state) {
        // final int reverseCamera = CameraSettings.getReverseCamera();
        // SLog.d(TAG, "VideoUI::reverse. state:" + state + "; reverseCamera:" +
        // reverseCamera);
        // if (mCameraService == null ||
        // mCameraService.getCameraInfo(reverseCamera) == null) {
        // SLog.d(TAG, "VideoUI::reverse. state:" + state +
        // ", but reverse camera is not exist");
        // }
        // if (state == 1) {
        // mActivity.runOnUiThread(new Runnable() {
        // @Override
        // public void run() {
        // mSurfaceInfos = new ArrayList<Surfaces>();
        // mSurfaceInfos.add(new Surfaces(reverseCamera,
        // mSurfaceViews[reverseCamera].getVisibility(),
        // mSurfaceViews[reverseCamera].getWidth(),
        // mSurfaceViews[reverseCamera].getHeight(), 0));
        // mSurfaceInfos.add(new Surfaces(reverseCamera == 0 ? 1 : 0,
        // mSurfaceViews[reverseCamera == 0 ? 1 : 0].getVisibility(),
        // mSurfaceViews[reverseCamera == 0 ? 1 : 0].getWidth(),
        // mSurfaceViews[reverseCamera == 0 ? 1 : 0].getHeight(),0));
        //
        // int sur = (int)
        // mCameraService.getCameraToSurface().get(reverseCamera);
        // sur = CameraSettings.isPreviewSwitched() ? (sur == 0 ? 1 : 0) : sur;
        // surfaceVisible(sur == 0 ? 1 : 0, View.GONE);
        // if (View.GONE == mSurfaceViews[sur].getVisibility()) {
        // surfaceVisible(sur, View.VISIBLE);
        // }
        // android.widget.FrameLayout.LayoutParams params =
        // (FrameLayout.LayoutParams) mSurfaceViews[sur].getLayoutParams();
        // params.width = params.height = FrameLayout.LayoutParams.MATCH_PARENT;
        // mSurfaceViews[sur].setLayoutParams(params);
        //
        //
        // }
        // });
        // } else if (state == 0) {
        // mActivity.runOnUiThread(new Runnable() {
        // @Override
        // public void run() {
        // if (mSurfaceInfos == null) {
        // SLog.w(TAG, "mSurfaceInfos is null");
        // return;
        // }
        // for (Surfaces surfaces : mSurfaceInfos) {
        // android.widget.FrameLayout.LayoutParams params =
        // (FrameLayout.LayoutParams)
        // mSurfaceViews[surfaces.getId()].getLayoutParams();
        // params.width = surfaces.getWidth();
        // params.height = surfaces.getHeight();
        // mSurfaceViews[surfaces.getId()].setLayoutParams(params);
        //
        // surfaceVisible(surfaces.getId(), surfaces.getVisible() ==
        // View.VISIBLE ? View.VISIBLE : View.GONE);
        //
        // }
        //
        //
        // }
        // });
        // }
    }

    @Override
    public void surfaceVisible(int id, int visible) {
        mSurfaceViews[id].setVisibility(visible);
        if (mCameraService != null) {
            int cameraId = (int) (mCameraService.getSurfaceToCamera().get(id));
            if (visible == View.VISIBLE) {
                mCameraService.setPreviewCallback(cameraId);
            } else if (visible == View.GONE) {
                mCameraService.setPreviewCallback(cameraId);
            }
        }
    }

    @Override
    public void previewSwitch() {
        if (mCameraService == null || mCameraService.getSurfaceToCamera() == null) return;
        mSurfaceViews[0].getRender().isRenderer(false);
        mSurfaceViews[0].getRender().isRequestRender(false);
        if (false) {
            mSurfaceViews[1].getRender().isRenderer(false);
            mSurfaceViews[1].getRender().isRequestRender(false);
        }

        if (false) {
            // mActivity.onPreviewUIDestroyed((Integer)
            // mCameraService.getSurfaceToCamera().get(0));
            // mActivity.onPreviewUIDestroyed((Integer)
            // mCameraService.getSurfaceToCamera().get(1));
        } else {
            // if (CameraSettings.isPreviewSwitched()) {
            // mActivity.onPreviewUIDestroyed((Integer)
            // mCameraService.getSurfaceToCamera().get(1));
            // } else {
            // mActivity.onPreviewUIDestroyed((Integer)
            // mCameraService.getSurfaceToCamera().get(0));
            // }
        }

        BufferManager.getInstance(0).clear();
        BufferManager.getInstance(1).clear();
        // SharedPreference.putBoolean(Config.KEY_PREVIEW_SWITCH,
        // !CameraSettings.isPreviewSwitched());
        // mSwitchDefault = CameraSettings.isPreviewSwitched();

        // mSurfaceViews[0].getRender().isRenderer(true);
        // mSurfaceViews[0].getRender().isRequestRender(true);
        // if (CameraSettings.isPicInPic()) {
        mSurfaceViews[1].getRender().isRenderer(true);
        mSurfaceViews[1].getRender().isRequestRender(true);
        // }
        // if (CameraSettings.isPicInPic()) {
        // mActivity.onPreviewUIReady((Integer)
        // mCameraService.getSurfaceToCamera().get(0));
        // mActivity.onPreviewUIReady((Integer)
        // mCameraService.getSurfaceToCamera().get(1));
        // } else {
        // if (CameraSettings.isPreviewSwitched()) {
        // mActivity.onPreviewUIReady((Integer)
        // mCameraService.getSurfaceToCamera().get(1));
        // } else {
        // mActivity.onPreviewUIReady((Integer)
        // mCameraService.getSurfaceToCamera().get(0));
        // }
        // }
    }

    @Override
    public void showPicInPic(boolean show) {
        // if (show) {
        // mSurfaceViews[1].setVisibility(View.VISIBLE);
        // android.widget.FrameLayout.LayoutParams params =
        // (FrameLayout.LayoutParams) mSurfaceViews[1].getLayoutParams();
        // params.width = (int)
        // mActivity.getResources().getDimension(R.dimen.pic_in_pic_width);
        // params.height = (int)
        // mActivity.getResources().getDimension(R.dimen.pic_in_pic_height);
        // mSurfaceViews[1].setLayoutParams(params);
        // if (mCameraService != null) {
        // mCameraService.setPreviewCallback(CameraSettings.isPreviewSwitched()
        // ? 0 : 1);
        // }
        // } else {
        // mSurfaceViews[1].setVisibility(View.GONE);
        // if (mCameraService != null) {
        // mCameraService.setPreviewCallback(CameraSettings.isPreviewSwitched()
        // ? 0 : 1, null);
        // }
        // }
    }

    @Override
    public void open() {
        startPreviewDirect(mOpenId, null);
    }

    @Override
    public void close() {
        mCameraService.stopPreview(mOpenId);
        mCameraService.release(mOpenId);

    }

    @Override
    public void checkEvent() {
    }

    @Override
    public void setRender(int id, boolean render) {
        if (id == mSurfaceViews[0].getId()) {
            SLog.d(TAG, "Surface 0 not render");
            mSurfaceViews[0].getRender().isRenderer(render);
        } else if (id == mSurfaceViews[1].getId()) {
            SLog.d(TAG, "Surface 1 not render");
            mSurfaceViews[1].getRender().isRenderer(render);
        }
    }

    public boolean switchAadas() {
        boolean success = false;
        return success;
    }

    private class SurfaceCallback implements SurfaceHolder.Callback {
        private final int mId;

        public SurfaceCallback(int id) {
            this.mId = id;
        }

        @Override
        public void surfaceCreated(SurfaceHolder holder) {
            SLog.d(TAG, "AVideoUI.SurfaceCallback::surfaceCreated. mId:" + mId);
            CameraHolder.instance().setHolder(holder, mId);

        }

        @Override
        public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
            SLog.d(TAG, "AVideoUI.SurfaceCallback::surfaceChanged. mId:" + mId);
            CameraHolder.instance().setHolder(holder, mId);
        }

        @Override
        public void surfaceDestroyed(SurfaceHolder holder) {
            SLog.d(TAG, "AVideoUI.SurfaceCallback::surfaceDestroyed. id:" + mId);
            CameraHolder.instance().setHolder(null, mId);

        }
    }

    AVideoUI.ICallbackRender mCallbackRender = new AVideoUI.ICallbackRender() {

        @Override
        public void addCallBackBuffer(int id, byte[] data) {
            mCameraService.addCallbackBuffer((int) mCameraService.getSurfaceToCamera().get(id), data);
        }
    };
}
