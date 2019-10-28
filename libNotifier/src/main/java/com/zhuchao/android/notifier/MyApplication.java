package com.zhuchao.android.notifier;

import android.app.Activity;
import android.app.Application;
import android.content.pm.ActivityInfo;
import android.os.Bundle;


public class MyApplication extends Application {
    public static Activity mCurrentActivity;

    @Override
    public void onCreate() {
        super.onCreate();

        registerActivityLifecycleCallbacks(new ActivityLifecycleCallbacks() {
            @Override
            public void onActivityCreated(Activity activity, Bundle savedInstanceState) {
                mCurrentActivity = activity;
                activity.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);

                //Util.setStatusBarNavigateBarTransparentByColor(mCurrentActivity,0x0000BFFF,0xFFffffff);
                //Util.setStatusBarNavigateBarTransparentBySID(mCurrentActivity, R.color.DeepSkyBlue,  R.color.White);
                //SNBarUtil.init(mCurrentActivity);
                //SNBarUtil.setStatusBarColor(mCurrentActivity,0x00400005,0xFFffffff);
                //SNBarUtil.setNavBarColor(mCurrentActivity,0x0ffffffff);
                StatusBarUtil.mTransparentStatusBar(mCurrentActivity);
            }

            @Override
            public void onActivityStarted(Activity activity) {
                mCurrentActivity = activity;
                //SNBarUtil.setStatusBarColor(mCurrentActivity,0x00805985,0xFFffffff);
                //SNBarUtil.setNavBarColor(mCurrentActivity,0x0ffffffff);
                //Util.setStatusBarNavigateBarTransparentBySID(mCurrentActivity, R.color.DeepSkyBlue,  R.color.White);
                //Util.setStatusBarNavigateBarTransparentByColor(mCurrentActivity,0x0000BFFF,0xFFffffff);
                StatusBarUtil.mTransparentStatusBar(mCurrentActivity);
            }

            @Override
            public void onActivityResumed(Activity activity) {
                mCurrentActivity = activity;
                StatusBarUtil.mTransparentStatusBar(mCurrentActivity);
            }

            @Override
            public void onActivityPaused(Activity activity) {

            }

            @Override
            public void onActivityStopped(Activity activity) {

            }

            @Override
            public void onActivitySaveInstanceState(Activity activity, Bundle outState) {
               // SNBarUtil.setStatusBarColor(mCurrentActivity,0x00005985,0xFFffffff);
            }

            @Override
            public void onActivityDestroyed(Activity activity) {

            }
        });
    }//public void onCreate()

    @Override
    public void onTerminate() {
        super.onTerminate();
    }

    @Override
    public void onLowMemory() {
        super.onLowMemory();
    }

    @Override
    public void onTrimMemory(int level) {
        super.onTrimMemory(level);
    }

    @Override
    public void registerActivityLifecycleCallbacks(ActivityLifecycleCallbacks callback) {
        super.registerActivityLifecycleCallbacks(callback);
    }

    @Override
    public void unregisterActivityLifecycleCallbacks(ActivityLifecycleCallbacks callback) {
        super.unregisterActivityLifecycleCallbacks(callback);
    }
}
