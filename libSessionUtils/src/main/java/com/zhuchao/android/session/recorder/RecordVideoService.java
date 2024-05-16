package com.zhuchao.android.session.recorder;

import static com.zhuchao.android.fbase.MessageEvent.MESSAGE_EVENT_OCTOPUS_ACTION_RECORDER_START;
import static com.zhuchao.android.fbase.MessageEvent.MESSAGE_EVENT_OCTOPUS_ACTION_RECORDER_STOP;

import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Build;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.os.Message;
import android.os.RemoteException;
import android.util.Log;
import android.view.Gravity;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.WindowManager;
import android.widget.LinearLayout;

import androidx.annotation.RequiresApi;

import com.zhuchao.android.car.aidl.IRecordVideoService;
import com.zhuchao.android.fbase.MMLog;

public class RecordVideoService extends Service implements SurfaceHolder.Callback {
    private static final String TAG = "RecordVideoService";
    private static final boolean DEBUG = true;
    private static final String ACTION_RECORD_START = MESSAGE_EVENT_OCTOPUS_ACTION_RECORDER_START;
    private static final String ACTION_RECORD_STOP = MESSAGE_EVENT_OCTOPUS_ACTION_RECORDER_STOP;
    private Context mContext;
    private RecordVideoServiceImp mRecordVideoServiceImp;
    private RecordVideoMode mRecordVideoMode;
    private LinearLayout mLinearLayout;
    private WindowManager mWindowManager;
    private SurfaceView mSurfaceView;
    private SurfaceHolder mSurfaceHolder;
    private static final int MSG_START_RECORD = 1;
    private static final int MSG_STOP_RECORD = 1 << 1;

    private final Handler mHandler = new Handler(Looper.myLooper()) {
        @Override
        public void handleMessage(Message msg) {
            int what = msg.what;
            if (DEBUG) Log.d(TAG, "handleMessage what : " + what);
            switch (what) {
                case MSG_START_RECORD:
                    startRecordVideo(true);
                    break;
                case MSG_STOP_RECORD:
                    startRecordVideo(false);
                    break;
                default:
                    break;
            }
        }
    };

    @RequiresApi(api = Build.VERSION_CODES.TIRAMISU)
    @Override
    public void onCreate() {
        super.onCreate();
        mContext = this;
        mRecordVideoServiceImp = new RecordVideoServiceImp();
        publish();
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(Intent.ACTION_SHUTDOWN);
        intentFilter.addAction(ACTION_RECORD_START);
        intentFilter.addAction(ACTION_RECORD_STOP);
        mContext.registerReceiver(mReceiver, intentFilter, Context.RECEIVER_EXPORTED);
        initView();
        mRecordVideoMode = new RecordVideoMode(mContext, mSurfaceHolder);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    public IBinder onBind(Intent intent) {
        return mRecordVideoServiceImp;
    }

    @Override
    public boolean onUnbind(Intent intent) {
        return super.onUnbind(intent);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        unregisterReceiver(mReceiver);
        mHandler.removeMessages(MSG_START_RECORD);
        mHandler.removeMessages(MSG_STOP_RECORD);
        mHandler.removeCallbacks(mStartRecordRunnable);
        mHandler.removeCallbacks(mStopRecordRunnable);
        if (null != mRecordVideoMode) mRecordVideoMode.releaseVideoRecorder();
        releaseView();
    }

    private void publish() {
        ///if (DEBUG) Log.d(TAG, "publish: " + mRecordVideoServiceImp);
        ///ServiceManager.addService(Context.RECORDVIDEO_SERVICE, mRecordVideoServiceImp);
    }


    private final class RecordVideoServiceImp extends IRecordVideoService.Stub {
        @Override
        public void startRecordVideo() throws RemoteException {
            if (DEBUG) MMLog.w(TAG, "startRecordVideo");
            mHandler.sendEmptyMessage(MSG_START_RECORD);
        }

        @Override
        public void stopRecordVideo() throws RemoteException {
            if (DEBUG) MMLog.w(TAG, "stopRecordVideo");
            mHandler.sendEmptyMessage(MSG_STOP_RECORD);
        }
    }

    private BroadcastReceiver mReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (DEBUG) MMLog.w(TAG, "BroadcastReceiver action : " + action);
            if (Intent.ACTION_SHUTDOWN.equals(action)) {
                mHandler.sendEmptyMessage(MSG_STOP_RECORD);
            } else if (ACTION_RECORD_START.equals(action)) {
                mHandler.sendEmptyMessage(MSG_START_RECORD);
            } else if (ACTION_RECORD_STOP.equals(action)) {
                mHandler.sendEmptyMessage(MSG_STOP_RECORD);
            }
        }
    };

    private void initView() {
        if (DEBUG) MMLog.d(TAG, "InitView");

        mWindowManager = (WindowManager) getSystemService(WINDOW_SERVICE);
        WindowManager.LayoutParams mLayoutParams = new WindowManager.LayoutParams();
        mLayoutParams.type = WindowManager.LayoutParams.TYPE_PHONE;
        // 设置图片格式，效果为背景透明 //wmParams.format = PixelFormat.RGBA_8888;
        mLayoutParams.format = 1;
        mLayoutParams.flags = WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL | WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE;
        mLayoutParams.width = WindowManager.LayoutParams.WRAP_CONTENT;
        mLayoutParams.height = WindowManager.LayoutParams.WRAP_CONTENT;
        mLayoutParams.gravity = Gravity.CENTER_HORIZONTAL | Gravity.CENTER;
        // 以屏幕左上角为原点，设置x、y初始值
        mLayoutParams.x = 0;
        mLayoutParams.y = 0;

        mSurfaceView = new SurfaceView(this);
        mSurfaceHolder = mSurfaceView.getHolder();
        WindowManager.LayoutParams mLayoutParamsSur = new WindowManager.LayoutParams();
        mLayoutParamsSur.width = 1;
        mLayoutParamsSur.height = 1;
        mLayoutParamsSur.alpha = 255;
        mSurfaceView.setLayoutParams(mLayoutParamsSur);
        mSurfaceView.getHolder().setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
        mSurfaceView.getHolder().addCallback(this);

        mLinearLayout = new LinearLayout(this);
        WindowManager.LayoutParams mLayoutParamsLin = new WindowManager.LayoutParams();
        mLayoutParamsLin.width = WindowManager.LayoutParams.WRAP_CONTENT;
        mLayoutParamsLin.height = WindowManager.LayoutParams.WRAP_CONTENT;
        mLinearLayout.setLayoutParams(mLayoutParamsLin);
        mLinearLayout.addView(mSurfaceView);

        mWindowManager.addView(mLinearLayout, mLayoutParams); // 创建View
    }

    private void releaseView() {
        if (mWindowManager != null) {
            mWindowManager.removeView(mLinearLayout);
        }
    }

    @Override
    public void surfaceCreated(SurfaceHolder holder) {
        if (DEBUG) MMLog.d(TAG, "surfaceCreated");
        mSurfaceHolder = holder;
        if (null == mRecordVideoMode) {
            mRecordVideoMode = new RecordVideoMode(mContext, mSurfaceHolder);
        }
        mRecordVideoMode.updateSurfaceHolder(mSurfaceHolder);
        mHandler.removeMessages(MSG_START_RECORD);
        mHandler.sendEmptyMessageDelayed(MSG_START_RECORD, 3000);

    }

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
        if (DEBUG) MMLog.d(TAG, "surfaceChanged");
        mSurfaceHolder = holder;
        if (null == mRecordVideoMode) {
            mRecordVideoMode = new RecordVideoMode(mContext, mSurfaceHolder);
        }
        mRecordVideoMode.updateSurfaceHolder(mSurfaceHolder);
    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {
        if (DEBUG) MMLog.d(TAG, "surfaceDestroyed");
        holder.removeCallback(this);
        if (null != mRecordVideoMode) mRecordVideoMode.releaseVideoRecorder();
    }

    private final Runnable mStartRecordRunnable = new Runnable() {
        @Override
        public void run() {
            if (null != mRecordVideoMode) {
                mRecordVideoMode.startVideoRecording();
            }
        }
    };

    private final Runnable mStopRecordRunnable = new Runnable() {
        @Override
        public void run() {
            if (null != mRecordVideoMode) {
                mRecordVideoMode.releaseVideoRecorder();
            }
        }
    };

    private void startRecordVideo(boolean enable) {
        if (DEBUG) MMLog.d(TAG, "startRecordVideo enable : " + enable);
        if (enable) {
            mHandler.removeCallbacks(mStartRecordRunnable);
            mHandler.post(mStartRecordRunnable);
        } else {
            mHandler.removeCallbacks(mStartRecordRunnable);
            mHandler.removeCallbacks(mStopRecordRunnable);
            mHandler.post(mStopRecordRunnable);
        }
    }
}
