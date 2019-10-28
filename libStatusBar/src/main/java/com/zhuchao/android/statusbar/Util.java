package com.zhuchao.android.statusbar;

import android.app.Activity;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.support.annotation.ColorInt;

import android.support.v4.content.ContextCompat;
import com.github.zackratos.ultimatebar.UltimateBar;


public class Util {


    public static void setStatusBarNavigateBarTransparent2(Activity activity)
    {
        @ColorInt
        int color1 = 0x0000BFFF;
        int color2 = 0xFFFFFFFF;

        Drawable drawableSB = new ColorDrawable(color1);
        Drawable drawableNB = new ColorDrawable(color2);

        UltimateBar.Companion.with(activity)
                .statusDark(false)                  // 状态栏灰色模式(Android 6.0+)，默认 flase
                .statusDrawable(drawableSB)           // 状态栏背景，默认 null
                .statusDrawable2(drawableSB)         // Android 6.0 以下状态栏灰色模式时状态栏颜色
                .applyNavigation(false)              // 应用到导航栏，默认 flase
                //.navigationDark(false)              // 导航栏灰色模式(Android 8.0+)，默认 false
                //.navigationDrawable(drawableNB)       // 导航栏背景，默认 null
                //.navigationDrawable2(drawableNB)     // Android 8.0 以下导航栏灰色模式时导航栏颜色
                .create()
                .transparentBar();
    }

    public static void setStatusBarNavigateBarTransparentBySNColor(Activity activity,@ColorInt int SColor, @ColorInt int NColor)
    {
        @ColorInt
        int color1 = SColor;//ContextCompat.getColor(activity, SBId);
        int color2 = NColor;//ContextCompat.getColor(activity,NBId);

        Drawable drawableSB = new ColorDrawable(color1);
        Drawable drawableNB = new ColorDrawable(color2);

        UltimateBar.Companion.with(activity)
                .statusDark(false)                  // 状态栏灰色模式(Android 6.0+)，默认 flase
                .statusDrawable(drawableSB)           // 状态栏背景，默认 null
                .statusDrawable2(drawableSB)         // Android 6.0 以下状态栏灰色模式时状态栏颜色
                .applyNavigation(true)              // 应用到导航栏，默认 flase
                .navigationDark(false)              // 导航栏灰色模式(Android 8.0+)，默认 false
                .navigationDrawable(drawableNB)       // 导航栏背景，默认 null
                .navigationDrawable2(drawableNB)     // Android 8.0 以下导航栏灰色模式时导航栏颜色
                .create()
                .transparentBar();
    }

    public static void setStatusBarNavigateBarTransparentBySID(Activity activity, int SBId,int NBId)
    {
        @ColorInt
        int color1 = ContextCompat.getColor(activity,SBId);
        int color2 = ContextCompat.getColor(activity,NBId);

        Drawable drawableSB = new ColorDrawable(color1);
        Drawable drawableNB = new ColorDrawable(color2);

        UltimateBar.Companion.with(activity)
                .statusDark(false)                  // 状态栏灰色模式(Android 6.0+)，默认 flase
                .statusDrawable(drawableSB)           // 状态栏背景，默认 null
                .statusDrawable2(drawableSB)         // Android 6.0 以下状态栏灰色模式时状态栏颜色
                .applyNavigation(true)              // 应用到导航栏，默认 flase
                .navigationDark(true)              // 导航栏灰色模式(Android 8.0+)，默认 false
                .navigationDrawable(drawableNB)       // 导航栏背景，默认 null
                .navigationDrawable2(drawableNB)     // Android 8.0 以下导航栏灰色模式时导航栏颜色
                .create()
                .transparentBar();
    }

    public static void setStatusBarNavigateBarTransparentByColor(Activity activity, @ColorInt int SColor, @ColorInt int NColor)
    {
        @ColorInt
        int color1 = SColor;//ContextCompat.getColor(activity, SBId);
        int color2 = NColor;//ContextCompat.getColor(activity,NBId);

        Drawable drawableSB = new ColorDrawable(color1);
        Drawable drawableNB = new ColorDrawable(color2);

        UltimateBar.Companion.with(activity)
                .statusDark(false)                  // 状态栏灰色模式(Android 6.0+)，默认 flase
                .statusDrawable(drawableSB)           // 状态栏背景，默认 null
                .statusDrawable2(drawableSB)         // Android 6.0 以下状态栏灰色模式时状态栏颜色
                .applyNavigation(true)              // 应用到导航栏，默认 flase
                .navigationDark(false)              // 导航栏灰色模式(Android 8.0+)，默认 false
                .navigationDrawable(drawableNB)       // 导航栏背景，默认 null
                .navigationDrawable2(drawableNB)     // Android 8.0 以下导航栏灰色模式时导航栏颜色
                .create()
                .transparentBar();
    }


}
