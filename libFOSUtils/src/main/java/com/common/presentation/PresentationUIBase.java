package com.common.presentation;

import android.app.Activity;
import android.app.Presentation;
import android.content.Context;
import android.view.Display;
import android.view.WindowManager;

public class PresentationUIBase extends Presentation {

    private final static String TAG = "PresentationUIBase";

    public static final int SCREEN0 = 0;
    public static final int SCREEN1 = 1;

    protected Context mContext;

    public boolean mPause = false;

    protected int mDisplayIndex; // 0 is main screen, 1 is second screen

    public PresentationUIBase(Context context, Display display, int style) {
        super(context, display, style);
        mContext = context;
        if (!(context instanceof Activity)) {
            getWindow().setType((WindowManager.LayoutParams.TYPE_SYSTEM_ALERT));
        }

    }

    @Override
    public void show() {
        super.show();
        mPause = false;
    }

    public void onPause() {
        mPause = true;
    }

    public void onResume() {
        mPause = false;
    }


}
