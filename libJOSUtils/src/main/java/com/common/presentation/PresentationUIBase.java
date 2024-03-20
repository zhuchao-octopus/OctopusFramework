package com.common.presentation;

import com.common.util.BroadcastUtil;
import com.common.util.MyCmd;
import com.common.view.CarUiBase;

import android.widget.VideoView;
import android.util.Log;
import android.hardware.display.DisplayManager;
import android.media.MediaPlayer;
import android.media.MediaRouter.RouteInfo;
import android.media.MediaRouter;
import android.app.Activity;
import android.app.Application;
import android.app.Presentation;
import android.os.Bundle;
import android.view.Display;
import android.view.KeyEvent;
import android.view.View;
import android.content.Context;
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
