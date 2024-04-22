package com.common.presentation;

import android.annotation.SuppressLint;
import android.app.Presentation;
import android.content.Context;
import android.hardware.display.DisplayManager;
import android.util.Log;
import android.view.Display;
import android.view.WindowManager;

import com.zhuchao.android.fbase.MMLog;

import java.util.Arrays;

public class PresentationManager {
    private static final String TAG = "PresentationManager";
    private Presentation mPresentation = null;
    private Context mContext;
    private Display[] mDisplay;
    @SuppressLint("StaticFieldLeak")
    private static PresentationManager mPresentationManager;

    public static PresentationManager getInstance(Context context) {
        if (mPresentationManager == null) {
            mPresentationManager = new PresentationManager();
            mPresentationManager.mContext = context;
            mPresentationManager.init();
        }
        return mPresentationManager;
    }

    private void init() {
        DisplayManager displayManager = (DisplayManager) mContext.getSystemService(Context.DISPLAY_SERVICE);
        mDisplay = displayManager.getDisplays();
        MMLog.i(TAG, "get " + mDisplay.length + " displays!");
    }

    public Display getDisplay(int index) {
        if (index < mDisplay.length) {
            return mDisplay[index];
        }
        return null;
    }

    public void updatePresentation(int index, Presentation presentation) {
        if (index < mDisplay.length) {
            Display display = mDisplay[index];

            // Dismiss the current presentation if the display has changed.
            if (mPresentation != null && presentation.getDisplay() != display) {
                Log.w(TAG, "Dismissing presentation because the current route no longer " + "has a presentation display.");
                // if (mListener != null) {
                // mListener.onPresentationChanged(false);
                // }
                // mPresentation.dismiss();
                // mPresentation = null;
            }
            // Show a new presentation if needed.
            if (mPresentation == null && mDisplay != null) {
                Log.w(TAG, "Showing presentation on display: " + Arrays.toString(mDisplay));

                try {
                    WindowManager.LayoutParams l = mPresentation.getWindow().getAttributes();
                    /// l.type = WindowManager.LayoutParams.FIRST_SYSTEM_WINDOW +
                    /// 100;
                    l.type = WindowManager.LayoutParams.TYPE_SYSTEM_ALERT;
                    mPresentation.show();

                } catch (WindowManager.InvalidDisplayException ex) {
                    Log.w(TAG, "Couldn't show presentation!  Display was removed in " + "the meantime.", ex);
                    mPresentation = null;
                }

            }
        }
    }
}
