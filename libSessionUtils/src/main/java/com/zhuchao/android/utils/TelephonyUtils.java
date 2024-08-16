package com.zhuchao.android.utils;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.pm.PackageManager;
import android.telephony.TelephonyManager;

import androidx.core.content.ContextCompat;

public class TelephonyUtils {
    private final String TAG="TelephonyManager";
    @SuppressLint({"ServiceCast", "HardwareIds"})
    public static String getIMEI(Context context) {
        if (ContextCompat.checkSelfPermission(context, Manifest.permission.READ_PHONE_STATE) == PackageManager.PERMISSION_GRANTED) {
            TelephonyManager telephonyManager = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
            if (telephonyManager != null) {
                if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.P) {
                    // API level 28 and above
                    return telephonyManager.getImei();
                } else {
                    // API level 27 and below
                    return telephonyManager.getDeviceId();
                }
            }
        }
        return "Permission Denied";
    }
    @SuppressLint({"ServiceCast", "HardwareIds"})
    public static String getICCID(Context context) {
        if (ContextCompat.checkSelfPermission(context, Manifest.permission.READ_PHONE_STATE) == PackageManager.PERMISSION_GRANTED) {
            TelephonyManager telephonyManager = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
            if (telephonyManager != null) {
                return telephonyManager.getSimSerialNumber();
            }
        }
        return "Permission Denied";
    }
}
