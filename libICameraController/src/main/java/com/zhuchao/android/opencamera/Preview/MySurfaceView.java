package com.zhuchao.android.opencamera.Preview;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.media.MediaRecorder;
import android.os.Handler;
import android.view.SurfaceView;
import android.view.View;

import com.zhuchao.android.opencamera.control.CameraController;
import com.zhuchao.android.opencamera.control.CameraControllerException;


/**
 * Provides support for the surface used for the preview, using a SurfaceView.
 */
public class MySurfaceView extends SurfaceView implements SurfaceInterface {
    private static final String TAG = "MySurfaceView";

    private final CameraPreview cameraPreview;
    private final int[] measure_spec = new int[2];
    private final Handler handler = new Handler();
    //private final Runnable tick;

    public MySurfaceView(Context context, final CameraPreview cameraPreview) {
        super(context);
        this.cameraPreview = cameraPreview;
        //if( MyDebug.LOG ) {
        //    Log.d(TAG, "new MySurfaceView");
        //}

        // Install a SurfaceHolder.Callback so we get notified when the
        // underlying surface is created and destroyed.
        getHolder().addCallback(cameraPreview);
        // deprecated setting, but required on Android versions prior to 3.0
        //getHolder().setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS); // deprecated

    }

    @Override
    public View getView() {
        return this;
    }

    @Override
    public void setPreviewDisplay(CameraController camera_controller) {
        //if( MyDebug.LOG )
        //    Log.d(TAG, "setPreviewDisplay");
        try {
            camera_controller.setPreviewDisplay(this.getHolder());
        } catch (CameraControllerException e) {
            //if( MyDebug.LOG )
            //    Log.e(TAG, "Failed to set preview display: " + e.getMessage());
            e.printStackTrace();
        }
    }

    @Override
    public void setVideoRecorder(MediaRecorder video_recorder) {
        video_recorder.setPreviewDisplay(this.getHolder().getSurface());
    }

    @Override
    public void onDraw(Canvas canvas) {
        cameraPreview.draw(canvas);
    }

    @Override
    protected void onMeasure(int widthSpec, int heightSpec) {
        //if( MyDebug.LOG )
        //    Log.d(TAG, "onMeasure: " + widthSpec + " x " + heightSpec);
        //preview.getMeasureSpec(measure_spec, widthSpec, heightSpec);
        super.onMeasure(measure_spec[0], measure_spec[1]);
    }

    @Override
    public void setTransform(Matrix matrix) {
        //if( MyDebug.LOG )
        //    Log.d(TAG, "setting transforms not supported for MySurfaceView");
        throw new RuntimeException();
    }

    @Override
    public void onPause() {
        //if( MyDebug.LOG )
        //    Log.d(TAG, "onPause()");
        //handler.removeCallbacks(tick);
    }

    @Override
    public void onResume() {
        //if( MyDebug.LOG )
        //    Log.d(TAG, "onResume()");
        //tick.run();
    }
}
