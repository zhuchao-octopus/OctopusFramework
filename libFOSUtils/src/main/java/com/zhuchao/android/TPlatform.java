package com.zhuchao.android;

import static com.zhuchao.android.fbase.FileUtils.EmptyString;
import static com.zhuchao.android.fbase.FileUtils.NotEmptyString;

import android.annotation.SuppressLint;
import android.app.ActivityManager;
import android.app.Instrumentation;
import android.content.Context;

import com.zhuchao.android.fbase.MMLog;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.List;

public class TPlatform {
    private static final String TAG = "TPlatform";
    private static Method methodGetProperty = null;
    private static Method methodSetProperty = null;

    ///////////////////////////////////////////////////////////////////////////////////////
    ///////////////////////////////////////////////////////////////////////////////////////
    public static final String OUTPUT_HDMI = "hdmi";
    public static final String OUTPUT_I2S = "i2s";
    public static final String OUTPUT_USB = "usb";
    public static final String OUTPUT_BT = "bt";
    public static final String OUTPUT_SPDIF = "spdif";
    public static final String OUTPUT_CODEC = "codec";
    public static final String OUTPUT_HDMI_CODEC = "hdmicodec";
    public static final String OUTPUT_ALL = "all";

    ///////////////////////////////////////////////////////////////////////////////////////
    ///////////////////////////////////////////////////////////////////////////////////////
    public static final String INPUT_HDMI = "hdmi";
    public static final String INPUT_I2S = "i2s";
    public static final String INPUT_USB = "usb";
    public static final String INPUT_BT = "bt";
    public static final String INPUT_CODEC = "codec";
    public static final String INPUT_BUILD_IN_MIC = "build.in.mic";
    public static final String INPUT_USB_MIC = "usb.mic";
    public static final String INPUT_BT_MIC = "bt.mic";

    public static String systemPropertiesGet(String key) {
        String result = "";
        try {
            @SuppressLint("PrivateApi") Class<?> c = Class.forName("android.os.SystemProperties");
            Method get = c.getMethod("get", String.class);
            result = (String) get.invoke(c, key);
        } catch (ClassNotFoundException | InvocationTargetException | IllegalArgumentException | IllegalAccessException | NoSuchMethodException e) {
            //e.printStackTrace();
            MMLog.e(TAG, "Exception:" + e);
        }
        return result;
    }

    public static void systemPropertiesSet(String key, String val) {
        try {
            @SuppressLint("PrivateApi") Class<?> c = Class.forName("android.os.SystemProperties");
            Method set = c.getMethod("set", String.class, String.class);
            set.invoke(c, key, val);
        } catch (ClassNotFoundException | IllegalAccessException | IllegalArgumentException | InvocationTargetException | NoSuchMethodException e) {
            //e.printStackTrace();
            MMLog.e(TAG, "Exception:" + e);
        }
    }

    public static String getSystemProperty(String key) {
        return systemPropertiesGet(key);
    }

    public static void setSystemProperty(String key, String val) {
        systemPropertiesSet(key, val);
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
        //return com.zhuchao.android.TGOS.get(key);
        return null;
    }

    public static void SetSystemProperty(String key, String val) {
        //com.zhuchao.android.TGOS.set(key, val);

    }

    public static void SetAudioOutputPolicy(String policyName) {
        //com.zhuchao.android.TGOS.SetAudioOutputPolicy(policyName);
    }

    public static void SetAudioInputPolicy(String policyName) {
        //com.zhuchao.android.TGOS.SetAudioInputPolicy(policyName);
    }

    public static String GetAudioOutputPolicy() {
        //return com.zhuchao.android.TGOS.GetAudioOutputPolicy();
        return null;
    }

    public static String GetAudioInputPolicy() {
        //return com.zhuchao.android.TGOS.GetAudioInputPolicy();
        return null;
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////
    //t507
    public static boolean t507IsI2SMicAudioInput() {
        String str = GetAudioInputPolicy();
        if (EmptyString(str)) return false;
        return str.startsWith(INPUT_I2S) || str.startsWith(INPUT_BUILD_IN_MIC);
    }

    public static boolean t507IsUSBMicAudioInput() {
        String str = GetAudioInputPolicy();
        if (EmptyString(str)) return false;
        if (str.startsWith(INPUT_USB)) return true;
        else return false;
    }

    public static boolean t507IsCodecMicAudioInput() {
        String str = GetAudioInputPolicy();
        if (EmptyString(str)) return false;
        return str.startsWith(INPUT_CODEC);
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
        return str.startsWith(INPUT_I2S) || str.startsWith(INPUT_BUILD_IN_MIC);
    }

    public static boolean IsUSBMicAudioInput() {
        String str = GetAudioInputPolicy();
        if (EmptyString(str)) return false;
        return str.startsWith(INPUT_USB);
    }

    public static boolean IsCodecMicAudioInput() {
        String str = GetAudioInputPolicy();
        if (EmptyString(str)) return false;
        return str.startsWith(INPUT_CODEC);
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

    public static void SetAudioOutput() {
        SetAudioOutputPolicy(OUTPUT_ALL);
    }

    public static void SetAudioHdmiCodecOutput() {
        SetAudioOutputPolicy(OUTPUT_HDMI_CODEC);
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////
    public static void resetDebugLogOnOffByProperty() {
        String sOnOff = GetSystemProperty(MMLog.LOG_SYSTEM_PROPERTY);
        MMLog.setDebugOnOff(!NotEmptyString(sOnOff) || !sOnOff.toLowerCase().contains("false"));
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////
    public static void sendKeyCode(final int keyCode) {
        new Thread() {
            public void run() {
                try {
                    Instrumentation inst = new Instrumentation();
                    inst.sendKeyDownUpSync(keyCode);
                } catch (Exception e) {
                    //e.printStackTrace();
                    MMLog.log(TAG, e.toString());
                }
            }
        }.start();
    }

    public static boolean sendKeyEvent(int keyCode) {
        String cmd = "input keyevent " + keyCode;
        return ExecConsoleCommand(cmd);
    }

    public static boolean ExecConsoleCommand(String cmd) {
        try {
            MMLog.log(TAG, "Exec console command: " + cmd);
            Runtime.getRuntime().exec(cmd);
            return true;
        } catch (Exception e) {
            //e.printStackTrace();
            MMLog.log(TAG, e.toString());
        }
        return false;
    }

    /*
     * m命令可以通过adb在shell中执行，同样，我们可以通过代码来执行
     */
    public static String ExecShellCommand(String... command) {
        Process process = null;
        InputStream errIs = null;
        InputStream inIs = null;
        String result = "";
        try {
            process = new ProcessBuilder().command(command).start();
            ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
            int read = -1;
            errIs = process.getErrorStream();
            while ((read = errIs.read()) != -1) {
                byteArrayOutputStream.write(read);
            }
            inIs = process.getInputStream();
            while ((read = inIs.read()) != -1) {
                byteArrayOutputStream.write(read);
            }
            result = byteArrayOutputStream.toString();
            inIs.close();
            errIs.close();
            process.destroy();
        } catch (IOException e) {
            result = e.getMessage();
        }
        return result;
    }

    public static void ExecShell(String command) {
        final String sSuPath = "/system/bin/su";
        try {
            /* Missing read/write permission, trying to chmod the file */
            Process su;
            su = Runtime.getRuntime().exec(sSuPath);
            String cmd = command + "\n" + "exit\n";
            su.getOutputStream().write(cmd.getBytes());
            if (su.waitFor() != 0) {
                //throw new SecurityException();
                MMLog.e(TAG, "ExecShell native open fail,not allow to read and write ");
            }
        } catch (Exception e) {
            MMLog.e(TAG, "native open returns null " + e.toString());
        }
    }

    public static String GetCPUSerialCode() {
        String cpuSerial = "0000000000000000";
        String cmd = "cat /proc/cpuinfo";
        try {
            Process p = Runtime.getRuntime().exec(cmd);
            StringBuilder data = null;
            BufferedReader ie = new BufferedReader(new InputStreamReader(p.getErrorStream()));
            BufferedReader in = new BufferedReader(new InputStreamReader(p.getInputStream()));
            String error = null;
            while ((error = ie.readLine()) != null && !error.equals("null")) {
                assert false;
                data.append(error).append("\n");
            }
            String line = null;
            while ((line = in.readLine()) != null && !line.equals("null")) {
                assert false;
                data.append(line).append("\n");
                if (line.contains("Serial\t\t:")) {
                    String[] SerialStr = line.split(":");
                    if (SerialStr.length == 2) {
                        String mSerial = SerialStr[1];
                        cpuSerial = mSerial.trim();
                        return cpuSerial;
                    }
                }
            }
        } catch (IOException ioe) {
            MMLog.log(TAG, ioe.toString());
        }
        return cpuSerial;
    }

    public static boolean isServiceRunning(Context mContext, String className) {
        boolean isRunning = false;
        ActivityManager activityManager = (ActivityManager) mContext.getSystemService(Context.ACTIVITY_SERVICE);
        List<ActivityManager.RunningServiceInfo> serviceList = activityManager.getRunningServices(30);
        if (!(serviceList.size() > 0)) {
            return false;
        }
        //Log.e("OnlineService：",className);
        MMLog.i(TAG, "RunningService:" + className);
        for (int i = 0; i < serviceList.size(); i++) {
            //Log.e("serviceName：",serviceList.get(i).service.getClassName());
            if (serviceList.get(i).service.getClassName().contains(className)) {
                isRunning = true;
                break;
            }
        }
        return isRunning;
    }

}
