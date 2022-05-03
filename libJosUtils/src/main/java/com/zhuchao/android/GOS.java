package com.zhuchao.android;

import com.zhuchao.android.libfileutils.MMLog;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

public class GOS {
    private static final String TAG = "GOS";
    public static Method methodGetProperty = null;
    public static Method methodSetProperty = null;
    public static final String OUTPUT_HDMI = "hdmi";
    public static final String OUTPUT_I2S = "i2s";
    public static final String OUTPUT_USB = "usb";
    public static final String OUTPUT_BT = "bt";
    public static final String INPUT_HDMI = "hdmi";
    public static final String INPUT_I2S = "i2s";
    public static final String INPUT_USB = "usb";
    public static final String INPUT_BT = "bt";
    public static final String INPUT_BUILD_IN_MIC = "build.in.mic";
    public static final String INPUT_USB_MIC = "usb.mic";
    public static final String INPUT_BT_MIC = "bt.mic";

    private static Class<?> getSystemProperties() {
        Class<?> aClass = null;
        try {
            aClass = Class.forName("android.os.SystemProperties");
        } catch (ClassNotFoundException e) {
            MMLog.log(TAG, e.toString());
        }
        return aClass;
    }

    public static String getSystemProperty(String key) {
        Class<?> aClass = getSystemProperties();
        if (aClass == null) return null;
        Method get = null;
        try {
            get = aClass.getMethod("get", String.class);
        } catch (NoSuchMethodException e) {
            MMLog.log(TAG, e.toString());
            return null;
        }
        try {
            return (String) get.invoke(aClass, key);
        } catch (IllegalAccessException e) {
            MMLog.log(TAG, e.toString());
        } catch (InvocationTargetException e) {
            MMLog.log(TAG, e.toString());
        }
        return null;
    }

    public static boolean setSystemProperty(String key, String val) {
        Class<?> aClass = getSystemProperties();
        if (aClass == null) return false;
        Method set = null;
        try {
            set = aClass.getMethod("set", String.class);
        } catch (NoSuchMethodException e) {
            MMLog.log(TAG, e.toString());
            return false;
        }
        try {
            set.invoke(aClass, key, val);
            return true;
        } catch (IllegalAccessException e) {
            MMLog.log(TAG, e.toString());
        } catch (InvocationTargetException e) {
            MMLog.log(TAG, e.toString());
        }
        return false;
    }

    public static String t507GetSystemProperty(String key) {
         return   com.zhuchao.android.JGOS.get(key);
    }

    public static boolean t507SetSystemProperty(String key, String val) {

    }

    public static void setAudioOutputPolicy(String policyName) {

    }

    public static void setAudioInputPolicy(String policyName) {

    }

    public static String getAudioOutputPolicy() {
        return null;
    }

    public static String getAudioInputPolicy() {
        return null;
    }

    public static void t507SetAudioOutputPolicy(String policyName) {

    }

    public static void t507SetAudioInputPolicy(String policyName) {

    }

    public static String t507GetAudioOutputPolicy() {
        return null;
    }

    public static String t507GetAudioInputPolicy() {
        return null;
    }
}
