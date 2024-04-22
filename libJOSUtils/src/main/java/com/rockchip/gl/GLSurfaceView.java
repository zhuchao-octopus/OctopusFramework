package com.rockchip.gl;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Color;
import android.os.Build;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Surface;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.LinearLayout;

import java.io.DataOutputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Locale;

//import com.rockchip.car.recorder.activity.VideoUI;
//import com.rockchip.car.recorder.render.GLFrameSurface;

public class GLSurfaceView {
    private final Context mContext;
    //VideoUI mVideoUI;

    private final ViewGroup mMain;
    //private GLFrameSurface gl;
    private int mCameraIndex = 0;

    private static final int MSG_REFRESH_ADCAMERA_UI = 100;

    private static int screenWidth = 1024;
    private static int screenHeight = 600;
    private static int screenX = 0;
    private static int screenY = 0;

    private final static String DEV_CAMERA_MIRROR = "/sys/class/ak/source/cam_rot_mir";

    private final Handler mHandler = new Handler(Looper.myLooper()) {
        public void handleMessage(Message msg) {
            if (msg.what == MSG_REFRESH_ADCAMERA_UI) {
                if (mMain != null && isAndroidR()) {
                    mMain.setBackgroundColor(Color.TRANSPARENT);
                }
            }
        }
    };

    @SuppressLint("StaticFieldLeak")
    private static Button mADCameraSelfRefresh = null;

    @SuppressLint("SetTextI18n")
    private void addADCameraRefreshButton(Context context, ViewGroup ll) {
        if (ll != null) {
            if (isAndroidR()) {
                //ll.setBackgroundColor(Color.TRANSPARENT);
                ll.setBackgroundColor(Color.BLACK);
            } else {
                ll.setBackgroundColor(Color.rgb(0x03, 0x05, 0x01));
                LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(new LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT));
                layoutParams.leftMargin = -1000;
                layoutParams.topMargin = -1000;
                mADCameraSelfRefresh = new Button(context);
                mADCameraSelfRefresh.setLayoutParams(layoutParams);
                mADCameraSelfRefresh.setAlpha(0.0f);
                mADCameraSelfRefresh.setClickable(false);
                mADCameraSelfRefresh.setText("Refresh");
                ll.addView(mADCameraSelfRefresh);
                Log.d("GLSufaceView", "add refresh button ok");
            }
        }
    }

    public GLSurfaceView(Context c, ViewGroup v) {
        mContext = c;
        if (isAndroidP() || isAndroidQ() || isAndroidR()) {
            addADCameraRefreshButton(c, v);

            WindowManager wm = (WindowManager) (c.getSystemService(Context.WINDOW_SERVICE));
            DisplayMetrics dm = new DisplayMetrics();
            wm.getDefaultDisplay().getRealMetrics(dm);
            screenWidth = dm.widthPixels;
            screenHeight = dm.heightPixels;
            if (screenWidth == 960 && screenHeight == 480) {
                screenX = 32;
                screenY = 60;
            }
            Log.d("GLSufaceView", Build.DISPLAY + ": " + screenWidth + "," + screenHeight + ", " + screenX + ", " + screenY);
        }
        mMain = v;
    }

    public GLSurfaceView(Context c, ViewGroup v, int index) {
        mContext = c;
        if (isAndroidP() || isAndroidQ() || isAndroidR()) {
            addADCameraRefreshButton(c, v);

            WindowManager wm = (WindowManager) (c.getSystemService(Context.WINDOW_SERVICE));
            DisplayMetrics dm = new DisplayMetrics();
            wm.getDefaultDisplay().getMetrics(dm);
            screenWidth = dm.widthPixels;
            screenHeight = dm.heightPixels;
            if (screenWidth == 960 && screenHeight == 480) {
                screenX = 32;
                screenY = 60;
            }
            mCameraIndex = index;
            Log.d("GLSufaceView", Build.DISPLAY + ": " + screenWidth + "," + screenHeight + ", " + screenX + ", " + screenY);
        }/* else {
			mMain = v;
			gl = new GLFrameSurface(c);
			mVideoUI = new VideoUI(gl, index);
			v.addView(gl);
			mCameraIndex = index;
			Log.d("GLSufaceView", ""+v.getChildCount());
		}*/
        mMain = v;
    }

    private boolean setFileValue(String file, String value) {
        try {

            FileOutputStream is = new FileOutputStream(file);
            DataOutputStream dis = new DataOutputStream(is);

            dis.write(value.getBytes());
            dis.close();
            is.close();
            return true;
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
            return false;
        }
    }

    private boolean startPreviewVehicleAD(boolean start, int x, int y, int width, int height) {
        if (isAndroidR()) mHandler.sendEmptyMessageDelayed(MSG_REFRESH_ADCAMERA_UI, 200);
        String cmd;
        if (x == -1 && y == -1 && width == -1 && height == -1)
            cmd = String.format(Locale.ENGLISH, "%s %d %d %d %d", start ? "11" : "10", screenX, screenY, screenWidth, screenHeight);
        else cmd = String.format(Locale.ENGLISH, "%s %d %d %d %d", start ? "11" : "10", x, y, width, height);
        boolean result = setFileValue("/dev/vehicle", cmd);
        Log.d("GLSufaceView", "send:" + cmd + ", result=" + result);
        return result;
    }

    public boolean startPreview() {
        if (isRKSystem()) {
            return startPreviewVehicleAD(true, -1, -1, -1, -1);
        } else {
            //gl.invalidate();
            //return mVideoUI.startPreviewDirect(0, null);
            return false;
        }
    }

    public boolean startPreview(int x, int y, int width, int height) {
        if (isRKSystem()) {
            return startPreviewVehicleAD(true, x, y, width, height);
        } else {
            //gl.invalidate();
            //return mVideoUI.startPreviewDirect(0, null);
            return false;
        }
    }

    public boolean startPreview(int channel) {
        return startPreview();
    }

    public boolean startPreviewEx(int channel) {
        return startPreview();
    }

    public void stoptPreview() {
        if (isRKSystem()) {
            if (isAndroidR()) mHandler.removeMessages(MSG_REFRESH_ADCAMERA_UI);
            startPreviewVehicleAD(false, -1, -1, -1, -1);
        }/* else {
			mVideoUI.close();
		}*/
    }

    public void release() {
        try {
            if (isRKSystem()) {
                if (isAndroidR()) mHandler.removeMessages(MSG_REFRESH_ADCAMERA_UI);
                mADCameraSelfRefresh = null;
            }/* else {
				mMain.removeAllViews();
			}*/
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public int getADRotation(Context context) {
        int rotation = ((WindowManager) context.getSystemService(Context.WINDOW_SERVICE)).getDefaultDisplay().getRotation();
        Log.d("GLSufaceView", "rotation=" + rotation);
        if (rotation == Surface.ROTATION_0) {
            return 0;
        } else if (rotation == Surface.ROTATION_90) {
            return 1;
        } else if (rotation == Surface.ROTATION_180) {
            return 2;
        } else if (rotation == Surface.ROTATION_270) {
            return 4;
        } else {
            return 0;
        }
    }

    public void setMirror(int i) {
        setFileValue(DEV_CAMERA_MIRROR, String.valueOf(i));
    }

    public int getCameraIndex() {
        return mCameraIndex;
    }

    public static boolean isPX5() {
        if (Build.DISPLAY.contains("px5") || Build.DISPLAY.contains("rk3368")) {
            return true;
        }
        return false;
    }

    public static boolean isPX6() {
        if (Build.DISPLAY.contains("px6") || Build.DISPLAY.contains("rk3399")) {
            return true;
        }
        return false;
    }

    public static boolean isPX30() {
        if (Build.DISPLAY.contains("px30") || Build.DISPLAY.contains("rk3326")) {
            return true;
        }
        return false;
    }

    public static boolean isAndroidP() {
        if (Build.VERSION.SDK_INT == 28) {        //Build.VERSION.SDK.contains(Build.P)
            return true;
        }
        return false;
    }

    public static boolean isRK356X() {
        if (Build.DISPLAY.contains("rk356")) {
            return true;
        }
        return false;
    }

    public static boolean isRK3566() {
        if (Build.DISPLAY.contains("rk3566")) {
            return true;
        }
        return false;
    }

    public static boolean isRK3568() {
        if (Build.DISPLAY.contains("rk3568")) {
            return true;
        }
        return false;
    }

    public static boolean isAndroidQ() {
        if (Build.VERSION.SDK_INT == 29) {        //Build.VERSION.SDK.contains(Build.Q)
            return true;
        }
        return false;
    }

    public static boolean isAndroidR() {
        if (Build.VERSION.SDK_INT == 30) {        //Build.VERSION.SDK.contains(Build.R)
            return true;
        }
        return false;
    }

    public static boolean isRKSystem() {
        if (isPX5() || isPX6() || isPX30() || isRK356X() || isAndroidP() || isAndroidQ() || isAndroidR()) {
            return true;
        }
        return false;
    }
}
