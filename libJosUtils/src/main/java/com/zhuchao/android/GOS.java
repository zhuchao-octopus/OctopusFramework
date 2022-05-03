package com.zhuchao.android;

import com.zhuchao.android.libfileutils.MMLog;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

public class GOS {
    private static final String TAG = "GOS";
    private static Method methodGetProperty = null;
    private static Method methodSetProperty = null;
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

    public static void setAudioOutputPolicy(String policyName,String val) {
        setSystemProperty(policyName,val);
    }

    public static void setAudioInputPolicy(String policyName,String val) {
        setSystemProperty(policyName,val);
    }

    public static String getAudioOutputPolicy(String policyName) {
        return getSystemProperty(policyName);
    }

    public static String getAudioInputPolicy(String policyName) {
        return getSystemProperty(policyName);
    }

    public static String t507GetSystemProperty(String key) {
        return com.zhuchao.android.T507HGOS.get(key);
    }

    public static void t507SetSystemProperty(String key, String val) {
        com.zhuchao.android.T507HGOS.set(key,val);
    }

    public static void t507SetAudioOutputPolicy(String policyName) {
        com.zhuchao.android.T507HGOS.SetAudioOutputPolicy(policyName);
    }

    public static void t507SetAudioInputPolicy(String policyName) {
        com.zhuchao.android.T507HGOS.SetAudioInputPolicy(policyName);
    }

    public static String t507GetAudioOutputPolicy() {
        return com.zhuchao.android.T507HGOS.GetAudioOutputPolicy();
    }

    public static String t507GetAudioInputPolicy() {
        return com.zhuchao.android.T507HGOS.GetAudioInputPolicy();
    }
}
