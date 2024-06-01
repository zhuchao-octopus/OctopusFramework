package com.rockchip.car.recorder.render;

import android.content.Context;
import android.graphics.Point;
import android.opengl.GLSurfaceView;
import android.util.AttributeSet;
import android.view.WindowManager;

import com.rockchip.car.recorder.activity.AVideoUI;
import com.rockchip.car.recorder.service.BufferManager;
import com.rockchip.car.recorder.utils.SLog;

public class GLFrameSurface extends GLSurfaceView {
    private static final String TAG = "CAM_GLFrameSurface";
    private Context mContext;
    private int mId;
    private GLFrameRenderer mRender;


    public GLFrameSurface(Context context) {
        super(context);
        mContext = context;
    }

    public GLFrameSurface(Context context, AttributeSet attrs) {
        super(context, attrs);
        mContext = context;
    }

    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
        setRenderMode(RENDERMODE_WHEN_DIRTY);
    }

    public void setId(final int id, final AVideoUI.ICallbackRender callbackRender) {
        this.mId = id;
        this.setEGLContextClientVersion(2);
        mRender = new GLFrameRenderer(id, null, new IRawDataCallback() {

            @Override
            public byte[] fetchYData() {
                // TODO Auto-generated method stub
                return null;
            }

            @Override
            public byte[] fetchVData() {
                // TODO Auto-generated method stub
                return null;
            }

            @Override
            public byte[] fetchUVData() {
                // TODO Auto-generated method stub
                return null;
            }

            @Override
            public byte[] fetchUData() {
                // TODO Auto-generated method stub
                return null;
            }

            @Override
            public byte[] fetchRawData() {
                // TODO Auto-generated method stub
                byte[] data = BufferManager.getInstance(id).pull();
                if (BufferManager.getInstance(id).size() > 0) {
                    mRender.requestRender();
                }
                return data;
            }

            @Override
            public void notifyTextureUpdated(final byte[] data) {
                // TODO Auto-generated method stub
                if (data == null) {
                    SLog.d(TAG, "data is null");
                    return;
                }
                callbackRender.addCallBackBuffer(id, data);
            }
        }, this, callbackRender);
        setRenderer(mRender);
        SLog.d(TAG, "surface setRenderMode RENDERMODE_WHEN_DIRTY");
    }

    public GLFrameRenderer getRender() {
        return mRender;
    }

    public void setRenderSize(int id, int width, int height) {
        if (mRender != null) {
            SLog.d(TAG, "Camera" + id + " setRenderSize w,h = " + width + "," + height);
            Point outSize = new Point();
            ((WindowManager) mContext.getSystemService(Context.WINDOW_SERVICE)).getDefaultDisplay().getSize(outSize);
            //            int pic_in_pic_w = (int) mContext.getResources().getDimension(R.dimen.pic_in_pic_width);
            //            int pic_in_pic_h = (int) mContext.getResources().getDimension(R.dimen.pic_in_pic_height);
            //            if (id == 0/*CameraHolder.instance().getBackCameraId()*/) {
            //                if (CameraSettings.isPreviewSwitched() && CameraHolder.instance().getNumberOfCameras() > 1
            //                        ) {
            //                    mRender.updateSurfaceSize(pic_in_pic_w, pic_in_pic_h);
            //                } else {
            //                    mRender.updateSurfaceSize(outSize.x, outSize.y);
            //                }
            //            } else if (id == 1/*CameraHolder.instance().getFrontCameraId()*/) {
            //                if (CameraSettings.isPreviewSwitched() && CameraHolder.instance().getNumberOfCameras() > 1
            //                       ) {
            mRender.updateSurfaceSize(outSize.x, outSize.y);
            //                } else {
            //                    mRender.updateSurfaceSize(pic_in_pic_w, pic_in_pic_h);
            //                }
            //            }
            mRender.update(width, height);
        }
    }

    public boolean requestRender(int id, byte[] data) {
        if (mRender == null || data == null || data.length == 0) return false;
        mRender.requestRender();
        return true;
    }

    public void setPreviewFormat(int id, int format) {
        mRender.setFormat(format);
    }
}
