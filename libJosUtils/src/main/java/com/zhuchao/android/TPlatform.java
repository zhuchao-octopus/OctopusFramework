package com.zhuchao.android;

import static com.zhuchao.android.libfileutils.FileUtils.EmptyString;
import static com.zhuchao.android.libfileutils.FileUtils.NotEmptyString;

import com.zhuchao.android.libfileutils.MMLog;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Locale;

public class TPlatform {
    private static final String TAG = "GOS";
    private static Method methodGetProperty = null;
    private static Method methodSetProperty = null;
    public static final String OUTPUT_HDMI = "hdmi";
    public static final String OUTPUT_I2S = "i2s";
    public static final String OUTPUT_USB = "usb";
    public static final String OUTPUT_BT = "bt";
    public static final String OUTPUT_CODEC = "codec";
    public static final String INPUT_HDMI = "hdmi";
    public static final String INPUT_I2S = "i2s";
    public static final String INPUT_USB = "usb";
    public static final String INPUT_BT = "bt";
    public static final String INPUT_CODEC = "codec";
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

    public static void setAudioOutputPolicy(String policyName, String val) {
        setSystemProperty(policyName, val);
    }

    public static void setAudioInputPolicy(String policyName, String val) {
        setSystemProperty(policyName, val);
    }

    public static String getAudioOutputPolicy(String policyName) {
        return getSystemProperty(policyName);
    }

    public static String getAudioInputPolicy(String policyName) {
        return getSystemProperty(policyName);
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////
    ////////////////////////////////////////////////////////////////////////////////////////////////
    ////////////////////////////////////////////////////////////////////////////////////////////////
    public static String GetSystemProperty(String key) {
        return com.zhuchao.android.TGOS.get(key);
    }

    public static void SetSystemProperty(String key, String val) {
        com.zhuchao.android.TGOS.set(key, val);
    }

    public static void SetAudioOutputPolicy(String policyName) {
        com.zhuchao.android.TGOS.SetAudioOutputPolicy(policyName);
    }

    public static void SetAudioInputPolicy(String policyName) {
        com.zhuchao.android.TGOS.SetAudioInputPolicy(policyName);
    }

    public static String GetAudioOutputPolicy() {
        return com.zhuchao.android.TGOS.GetAudioOutputPolicy();
    }

    public static String GetAudioInputPolicy() {
        return com.zhuchao.android.TGOS.GetAudioInputPolicy();
    }
    ////////////////////////////////////////////////////////////////////////////////////////////////
    //t507
    public static boolean t507IsI2SMicAudioInput() {
        String str = GetAudioInputPolicy();
        if (EmptyString(str)) return false;
        if (str.startsWith(INPUT_I2S) || str.startsWith(INPUT_BUILD_IN_MIC))
            return true;
        else
            return false;
    }

    public static boolean t507IsUSBMicAudioInput() {
        String str = GetAudioInputPolicy();
        if (EmptyString(str)) return false;
        if (str.startsWith(INPUT_USB))
            return true;
        else
            return false;
    }

    public static boolean t507IsCodecMicAudioInput() {
        String str = GetAudioInputPolicy();
        if (EmptyString(str)) return false;
        if (str.startsWith(INPUT_CODEC))
            return true;
        else
            return false;
    }

    public static void t507SetUSBMiCAudioInput() {
        SetAudioInputPolicy(INPUT_USB);
    }

    public static void t507SetI2SMiCAudioInput() {
        SetAudioInputPolicy(INPUT_I2S);
    }

    public static void t507SetCodecMicAudioInput() {
        SetAudioInputPolicy(INPUT_CODEC);
    }

    public static void t507SetHDMIAudioOutput() {
        SetAudioOutputPolicy(OUTPUT_HDMI);
    }

    public static void t507SetI2sAudioOutput() {
        SetAudioOutputPolicy(OUTPUT_I2S);
    }

    public static void t507SetCodecAudioOutput() {
        SetAudioOutputPolicy(OUTPUT_CODEC);
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////
    ////////////////////////////////////////////////////////////////////////////////////////////////
    //通用
    public static boolean IsI2SMicAudioInput() {
        String str = GetAudioInputPolicy();
        if (EmptyString(str)) return false;
        if (str.startsWith(INPUT_I2S) || str.startsWith(INPUT_BUILD_IN_MIC))
            return true;
        else
            return false;
    }

    public static boolean IsUSBMicAudioInput() {
        String str = GetAudioInputPolicy();
        if (EmptyString(str)) return false;
        if (str.startsWith(INPUT_USB))
            return true;
        else
            return false;
    }

    public static boolean IsCodecMicAudioInput() {
        String str = GetAudioInputPolicy();
        if (EmptyString(str)) return false;
        if (str.startsWith(INPUT_CODEC))
            return true;
        else
            return false;
    }

    public static void SetUSBMiCAudioInput() {
        SetAudioInputPolicy(INPUT_USB);
    }

    public static void SetI2SMiCAudioInput() {
        SetAudioInputPolicy(INPUT_I2S);
    }

    public static void SetCodecMicAudioInput() {
        SetAudioInputPolicy(INPUT_CODEC);
    }

    public static void SetHDMIAudioOutput() {
        SetAudioOutputPolicy(OUTPUT_HDMI);
    }

    public static void SetI2sAudioOutput() {
        SetAudioOutputPolicy(OUTPUT_I2S);
    }

    public static void SetCodecAudioOutput() {
        SetAudioOutputPolicy(OUTPUT_CODEC);
    }


    ////////////////////////////////////////////////////////////////////////////////////////////////
    public static void resetDebugLogOnOffByProperty()
    {
      String  sOnOff =  GetSystemProperty(MMLog.LOG_SYSTEM_PROPERTY);
      if (NotEmptyString(sOnOff) && sOnOff.toLowerCase(Locale.ROOT).contains("false"))
      {
        MMLog.setDebugOnOff(false);
      }
      else
      {
        MMLog.setDebugOnOff(true);
      }
    }
}
