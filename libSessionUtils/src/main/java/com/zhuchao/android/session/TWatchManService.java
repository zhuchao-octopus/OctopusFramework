package com.zhuchao.android.session;

import static com.zhuchao.android.fileutils.FileUtils.EmptyString;
import static com.zhuchao.android.fileutils.FileUtils.NotEmptyString;

import android.app.ActivityManager;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageInfo;
import android.os.Build;
import android.os.IBinder;
import android.util.Log;

import com.zhuchao.android.TPlatform;
import com.zhuchao.android.eventinterface.InvokeInterface;
import com.zhuchao.android.fileutils.DataID;
import com.zhuchao.android.fileutils.FileUtils;
import com.zhuchao.android.fileutils.MMLog;
import com.zhuchao.android.fileutils.TAppUtils;
import com.zhuchao.android.fileutils.TTask;
import com.zhuchao.android.fileutils.ThreadUtils;
import com.zhuchao.android.net.NetworkInformation;
import com.zhuchao.android.net.TNetUtils;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Locale;

/*第一种方式：通过StartService启动Service
 通过startService启动后，service会一直无限期运行下去，只有外部调用了stopService()或stopSelf()方法时，该Service才会停止运行并销毁。
 要创建一个这样的Service，你需要让该类继承Service类，然后重写以下方法：
 onCreate()
 1.如果service没被创建过，调用startService()后会执行onCreate()回调；
 2.如果service已处于运行中，调用startService()不会执行onCreate()方法,多次执行startService()不会重复调用onCreate().
 onStartCommand()如果多次执行了Context的startService()方法，那么Service的onStartCommand()方法也会相应的多次调用。
 onBind()Service中的onBind()方法是抽象方法，Service类本身就是抽象类，所以onBind()方法是必须重写的，即使我们用不到。
 onDestory()在销毁的时候会执行Service该方法。
 */

public class TWatchManService extends Service implements TNetUtils.NetworkStatusListener {
    private static final String TAG = "TWatchManService";
    private final static String Action_HELLO = "android.intent.action.ACTION_WATCHMAN_HELLO";
    private final static String Action_UPDATE_NET_STATUS = "android.intent.action.UPDATE_NET_STATUS";
    private final static String Action_GET_RUNNING_TASK = "android.intent.action.GET_RUNNING_TASK";
    private final static String Action_WATCHMAN_SWITCH_ONOFF = "android.intent.action.WATCHMAN_SWITCH_ONOFF";
    private final static String Action_SystemShutdown = "android.intent.action.ACTION_REQUEST_SHUTDOWN";
    private final static String Action_SystemReboot = "android.intent.action.ACTION_REQUEST_REBOOT";
    private final static String Action_SilentInstall = "android.intent.action.SILENT_INSTALL_PACKAGE";
    private final static String Action_SilentInstallComplete = "android.intent.action.SILENT_INSTALL_PACKAGE_COMPLETE";
    private final static String Action_SilentUninstall = "android.intent.action.SILENT_UNINSTALL_PACKAGE";
    private final static String Action_SilentClose = "android.intent.action.SILENT_CLOSE_PACKAGE";
    private final static String Action_SetAudioOutputChannel = "android.intent.action.SET_AUDIO_OUTPUT_CHANNEL";
    private final static String Action_SetAudioInputChannel = "android.intent.action.SET_AUDIO_INPUT_CHANNEL";

    private final static String Action_SystemShutdown1 = "action.uniwin.shutdown";
    private final static String Action_SystemReboot1 = "action.uniwin.reboot.receiver";

    private final static String Action_SystemAdjustTouchTscal = "org.zeroxlab.util.tscal";
    private final static String Action_SystemAdjustTouchCalibration = "org.zeroxlab.util.tscal.TSCalibration";
    private final static String Action_SystemResolution = "action.ktv.settings.receiver";
    private final static String Action_SystemEthertConfig = "action.ktv.net.receiver";
    private final static String Action_SystemDeviceUUID = "action.device.uuid.receiver";

    private final static String Action_SilentInstall1 = "android.intent.action.SILENT_INSTALL_PACKAGE1";
    private final static String Action_SilentInstall2 = "android.intent.action.SILENT_INSTALL_PACKAGE2";

    //private Context mContext = null;
    private TNetUtils tNetUtils = null;
    private TTaskManager tTaskManager = null;
    private NetworkInformation networkInformation = null;

    private String pName = null;//"A40I";
    private String pModel = null;//"A40I";
    private String pBrand = null;//"TianPu";
    private String pCustomer = null;//"TianPu";

    private boolean installedDeleteFile = false;
    private boolean installedReboot = false;
    private boolean watchManSwitchOnOff = true;
    private String VERSION_NAME = "0.01";

    ////////////////////////////////////////////////////////////////////////////////////////////////
    ////////////////////////////////////////////////////////////////////////////////////////////////
    ////////////////////////////////////////////////////////////////////////////////////////////////
    ////////////////////////////////////////////////////////////////////////////////////////////////
    ////////////////////////////////////////////////////////////////////////////////////////////////

    public void start() {
        ThreadUtils.runThread(new Runnable() {
            @Override
            public void run() {
                try {
                    VERSION_NAME = TAppUtils.getAppVersionName(TWatchManService.this, TWatchManService.this.getPackageName());
                    tTaskManager = new TTaskManager(TWatchManService.this);
                    tNetUtils = new TNetUtils(TWatchManService.this);
                    tNetUtils.registerNetStatusCallback(TWatchManService.this);
                    registerUserEventReceiver();
                    MMLog.i(TAG, "WatchManService version:" + VERSION_NAME + " " + getFWVersionName() + " starting...");//2 first call
                    //TPlatform.SetSystemProperty("WatchMan.Service","true");//导致错误
                } catch (Exception e) {
                    //e.printStackTrace();
                    MMLog.e(TAG, e.getMessage());
                }
            }
        });
    }

    public void start(Context context) {
        ThreadUtils.runThread(new Runnable() {
            @Override
            public void run() {
                try {
                    //VERSION_NAME = TAppUtils.getAppVersionName(TWatchManService.this, TWatchManService.this.getPackageName());
                    tTaskManager = new TTaskManager(context);
                    tNetUtils = new TNetUtils(context);
                    tNetUtils.registerNetStatusCallback(TWatchManService.this);
                    registerUserEventReceiver();
                    MMLog.i(TAG, "WatchManService version:" + VERSION_NAME + " " + getFWVersionName() + " starting...");//2 first call
                    //TPlatform.SetSystemProperty("WatchMan.Service","true");//导致错误
                } catch (Exception e) {
                    //e.printStackTrace();
                    MMLog.e(TAG, e.getMessage());
                }
            }
        });
    }

    public TWatchManService() {
        //MMLog.i(TAG, "TWatchManService construct with no parameters.");//1 first call
    }

    @Override
    public void onCreate() {
        super.onCreate();
        //MMLog.d(TAG, "onCreate()");//2 second call
        start();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        return super.onStartCommand(intent, flags, startId);
        //MMLog.d(TAG, "onStartCommand()");//3 call
    }

    @Override
    public IBinder onBind(Intent intent) {
        // TODO: Return the communication channel to the service.
        //throw new UnsupportedOperationException("Not yet implemented");
        //MMLog.d(TAG, "WatchManService on bind");
        return null;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        //MMLog.d(TAG, "onDestroy()");
        unRegisterUserEventReceiver();
    }

    private void registerUserEventReceiver() {
        try {
            IntentFilter intentFilter = new IntentFilter();
            intentFilter.addAction(Action_HELLO);//测试
            intentFilter.addAction(Action_UPDATE_NET_STATUS);//测试
            intentFilter.addAction(Action_GET_RUNNING_TASK);
            intentFilter.addAction(Action_WATCHMAN_SWITCH_ONOFF);

            intentFilter.addAction(Action_SystemShutdown);//关机
            intentFilter.addAction(Action_SystemReboot);//重启

            intentFilter.addAction(Action_SilentInstall);//静默安装
            intentFilter.addAction(Action_SilentUninstall);//静默反安装
            intentFilter.addAction(Action_SilentClose);//静默结束
            intentFilter.addAction(Action_SetAudioOutputChannel);
            intentFilter.addAction(Action_SetAudioInputChannel);

            intentFilter.addAction(Action_SystemShutdown1);//关机
            intentFilter.addAction(Action_SystemReboot1);//重启
            intentFilter.addAction(Action_SystemAdjustTouchTscal);//触摸屏校准
            intentFilter.addAction(Action_SystemAdjustTouchCalibration);//触摸屏校准
            intentFilter.addAction(Action_SystemResolution);//设置分辨率
            intentFilter.addAction(Action_SystemEthertConfig);//以太网设置(有线网络)
            intentFilter.addAction(Action_SystemDeviceUUID);//获取设备序列号
            intentFilter.addAction(Action_SilentInstall1);//静默安装
            intentFilter.addAction(Action_SilentInstall2);//静默安装
            intentFilter.addAction(Action_SilentInstallComplete);//静默安装后删除文件

            registerReceiver(UserEventReceiver, intentFilter);
            //MMLog.d(TAG, "Register user event listener successfully.");
        } catch (Exception e) {
            MMLog.e(TAG, "Register user event listener failed!" + e.toString());
        }
    }

    public void unRegisterUserEventReceiver() {
        try {
            unregisterReceiver(UserEventReceiver);
        } catch (Exception e) {
            //e.printStackTrace();
        }
    }

    private final BroadcastReceiver UserEventReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent == null) return;
            final String action = intent.getAction();
            MMLog.i(TAG, "User event intent.Action = " + action);
            switch (action) {
                case Action_HELLO:
                    MMLog.log(TAG, "Hello it is ready! version:" + VERSION_NAME);
                    if (networkInformation != null)
                        MMLog.d(TAG, "HOST:" + networkInformation.toString());
                    if (intent.getExtras() != null) {
                        pName = intent.getExtras().getString("pName", null);
                        pModel = intent.getExtras().getString("pModel", null);
                        pBrand = intent.getExtras().getString("pBrand", null);
                        pCustomer = intent.getExtras().getString("pCustomer", null);
                    }
                    break;
                case Action_UPDATE_NET_STATUS://
                    checkAndUpdateDevice(true);
                    break;
                case Action_GET_RUNNING_TASK:
                    Action_GETRUNNINGTASK();
                    break;
                case Action_WATCHMAN_SWITCH_ONOFF:
                    //Action_WATCHMAN_SWITCH_ONOFF();
                    watchManSwitchOnOff = !watchManSwitchOnOff;
                    MMLog.i(TAG, "watchManSwitchOnOff = " + watchManSwitchOnOff);
                    break;
            }
            if (!watchManSwitchOnOff) return;
            ////////////////////////////////////////////////////////////////////////////////////////
            ////////////////////////////////////////////////////////////////////////////////////////
            ////////////////////////////////////////////////////////////////////////////////////////
            switch (action) {
                case Action_SystemShutdown:
                case Action_SystemShutdown1:
                    Action_SystemShutdown();
                    break;
                case Action_SystemReboot:
                case Action_SystemReboot1:
                    Action_SystemReboot();
                    break;
                case Action_SystemAdjustTouchTscal:
                case Action_SystemAdjustTouchCalibration:
                case Action_SystemResolution:
                case Action_SystemEthertConfig:
                case Action_SystemDeviceUUID:
                    break;
                case Action_SilentInstall:
                    if (intent.getExtras() != null) {
                        String apkFilePath = intent.getExtras().getString("apkFilePath");
                        boolean autostart = intent.getExtras().getBoolean("autostart", false);
                        installedDeleteFile = intent.getExtras().getBoolean("installedDeleteFile", false);
                        installedReboot = intent.getExtras().getBoolean("installedReboot", false);

                        TTask tTask = tTaskManager.getSingleTaskFor("Silent to install " + apkFilePath);
                        if (tTask.isBusy()) {
                            MMLog.d(TAG, "The tTask is on working! " + tTask.getTName());
                            break;
                        }
                        tTask.invoke(new InvokeInterface() {
                            @Override
                            public void CALLTODO(String tag) {
                                Action_SilentInstallAction(apkFilePath, autostart);
                            }
                        });
                        tTask.start();
                    }
                    break;
                case Action_SilentInstall1:
                    if (intent.getExtras() != null) {
                        String apkFilePath = intent.getExtras().getString("apkFilePath");
                        boolean autostart = intent.getExtras().getBoolean("autostart");
                        Action_SilentInstallAction1(apkFilePath, autostart);
                    }
                    break;
                case Action_SilentInstall2:
                    if (intent.getExtras() != null) {
                        String apkFilePath = intent.getExtras().getString("apkFilePath");
                        boolean autostart = intent.getExtras().getBoolean("autostart");
                        Action_SilentInstallAction2(apkFilePath, autostart);
                    }
                    break;
                case Action_SilentInstallComplete:
                    if (intent.getExtras() != null) {
                        String apkFilePath = intent.getExtras().getString("apkFilePath");
                        Action_SilentInstallComplete(apkFilePath);
                    }
                    break;
                case Action_SilentUninstall:
                    if (intent.getExtras() != null) {
                        String packageName = intent.getExtras().getString("uninstall_pkg");
                        Action_SilentUnInstallAction(packageName);
                    }
                    break;
                case Action_SilentClose:
                    if (intent.getExtras() != null) {
                        String packageName = intent.getExtras().getString("close_pkg");
                        Action_SilentCLOSEAction(packageName);
                    }
                    break;
                case Action_SetAudioOutputChannel:
                    if (intent.getExtras() != null) {
                        String channel = intent.getExtras().getString("channel");
                        Action_SetAudioOutputChannel(channel);
                    }
                    break;
                case Action_SetAudioInputChannel:
                    if (intent.getExtras() != null) {
                        String channel = intent.getExtras().getString("channel");
                        Action_SetAudioInputChannel(channel);
                    }
                    break;
                default:
                    ;
                    break;
            }
        }
    };

    private void Action_GETRUNNINGTASK() {
        TAppUtils.getRunningProcess(this).print();
    }

    private void Action_SilentInstallComplete(String apkFilePath) {
        if (installedDeleteFile) {
            boolean b = FileUtils.deleteFile(apkFilePath);
            if (b)
                MMLog.i(TAG, "delete file successfully! ---> " + apkFilePath);
            else
                MMLog.i(TAG, "delete file failed! ---> " + apkFilePath);
        }
        if (installedReboot)
            Action_SystemReboot();
    }

    private void Action_SetAudioOutputChannel(String channel) {
        TPlatform.setAudioOutputPolicy("device.audio.output.policy", channel);
        MMLog.log(TAG, "AudioOutputPolicy--->" + TPlatform.GetAudioOutputPolicy());
    }

    private void Action_SetAudioInputChannel(String channel) {
        TPlatform.setAudioInputPolicy("device.audio.input.policy", channel);
        MMLog.log(TAG, "AudioInputPolicy--->" + TPlatform.GetAudioInputPolicy());
    }

    private void Action_SilentUnInstallAction(String packageName) {
        //Uninstall(packageName);
        TAppUtils.uninstallApk(packageName);
    }

    private void Action_SilentCLOSEAction(String packageName) {
        //killAppProcess(packageName);
        //killAssignPkg(packageName);
        TAppUtils.killApplication(this, packageName);
    }

    private synchronized void killAssignPkg(String packageName) {
        ActivityManager mActivityManager = (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);
        Method method = null;
        try {
            method = Class.forName("android.app.ActivityManager").getMethod("forceStopPackage", String.class);
            method.invoke(mActivityManager, packageName);
        } catch (NoSuchMethodException | ClassNotFoundException | IllegalAccessException | InvocationTargetException e) {
            //e.printStackTrace();
            MMLog.e(TAG, e.toString());
        }
    }

    private void Action_SystemShutdown() {
        //TPlatform.sendKeyCode(KeyEvent.KEYCODE_F9);
        //TPlatform.sendKeyEvent(KeyEvent.KEYCODE_F9);
        TPlatform.ExecConsoleCommand("reboot -p");
    }

    private void Action_SystemReboot() {
        TPlatform.ExecConsoleCommand("reboot");
    }

    private String Action_SystemGetDeviceUUID() {
        return TPlatform.GetCPUSerialCode();
    }

    private synchronized void Action_SilentInstallAction(String filePath, boolean autostart) {
        if (EmptyString(filePath)) {
            return;
        }
        if (!FileUtils.existFile(filePath)) {
            MMLog.log(TAG, "file does not exists! --->" + filePath);
            return;
        }
        if (!filePath.toLowerCase(Locale.ROOT).endsWith(".apk")) {
            MMLog.log(TAG, "file is not a valid apk file! --->" + filePath);
            return;
        }
        boolean b = TAppUtils.installSilent(this, filePath);
        if (b)
            MMLog.log(TAG, "installSilent successfully! ->" + filePath);
        else
            MMLog.log(TAG, "installSilent failed! ->" + filePath);
        if (autostart) {
            PackageInfo packageInfo = TAppUtils.getPackageInfo(this, filePath);
            if (packageInfo != null)
                TAppUtils.startApp(this, packageInfo.packageName);
        }
    }

    private synchronized void Action_SilentInstallAction1(String filePath, boolean autostart) {
        TPlatform.ExecConsoleCommand("pm install -r " + filePath);
        if (autostart) {
            PackageInfo packageInfo = TAppUtils.getPackageInfo(this, filePath);
            if (packageInfo != null)
                TAppUtils.startApp(this, packageInfo.packageName);
        }
    }

    private synchronized void Action_SilentInstallAction2(String filePath, boolean autostart) {
        String ret = TPlatform.ExecShellCommand("pm", "install", "-f", filePath);
        MMLog.log(TAG, ret);
        if (autostart) {
            PackageInfo packageInfo = TAppUtils.getPackageInfo(this, filePath);
            if (packageInfo != null)
                TAppUtils.startApp(this, packageInfo.packageName);
        }
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////
    ////////////////////////////////////////////////////////////////////////////////////////////////
    private synchronized static void install(File apkFile) {
        String cmd = "";
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT_WATCH) {
            cmd = "pm install -r -d " + apkFile.getAbsolutePath();
        } else {
            cmd = "pm install -r -d -i packageName --user 0 " + apkFile.getAbsolutePath();
        }
        Runtime runtime = Runtime.getRuntime();
        try {
            Process process = runtime.exec(cmd);
            InputStream errorInput = process.getErrorStream();
            InputStream inputStream = process.getInputStream();
            BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(inputStream));
            String error = "";
            String result = "";
            String line = "";
            while ((line = bufferedReader.readLine()) != null) {
                result += line;
            }
            bufferedReader = new BufferedReader(new InputStreamReader(errorInput));
            while ((line = bufferedReader.readLine()) != null) {
                error += line;
            }
            if (result.equals("Success")) {
                Log.i(TAG, "install: Success");
            } else {
                Log.i(TAG, "install: error" + error);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private synchronized static boolean installApk(String apkPath) {
        //String [ ] args = { "pm" , "install" , "-i" , "com.example", apkPath } ;//7.0用这个，参考的博客说要加 --user，但是我发现使用了反而不成功。
        String[] args = {"pm", "install", "-r", apkPath};
        ProcessBuilder processBuilder = new ProcessBuilder(args);
        Process process = null;
        BufferedReader successResult = null;
        BufferedReader errorResult = null;
        StringBuilder successMsg = new StringBuilder();
        StringBuilder errorMsg = new StringBuilder();
        try {
            process = processBuilder.start();
            successResult = new BufferedReader(new InputStreamReader(process.getInputStream()));
            errorResult = new BufferedReader(new InputStreamReader(process.getErrorStream()));
            String s;
            while ((s = successResult.readLine()) != null) {
                successMsg.append(s);
            }
            while ((s = errorResult.readLine()) != null) {
                errorMsg.append(s);
            }
            return process.waitFor() == 0 || successMsg.toString().contains("Success");
        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
        } finally {
            try {
                if (successResult != null) {
                    successResult.close();
                }
                if (errorResult != null) {
                    errorResult.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
            if (process != null) {
                process.destroy();
            }
        }
        return false;
    }

    private synchronized static void installApk(Context context, String apkPath) {
        try {
            PackageInfo info = TAppUtils.getPackageInfo(context, apkPath);
            String[] args = {"pm", "install", "-i", info.packageName, "--user", "0", "-r", apkPath};
            ProcessBuilder processBuilder = new ProcessBuilder(args);
            Process process = null;
            BufferedReader successResult = null;
            BufferedReader errorResult = null;
            StringBuilder successMsg = new StringBuilder();
            StringBuilder errorMsg = new StringBuilder();
            try {
                process = processBuilder.start();
                successResult = new BufferedReader(new InputStreamReader(process.getInputStream()));
                errorResult = new BufferedReader(new InputStreamReader(process.getErrorStream()));
                String line;

                while ((line = successResult.readLine()) != null) {
                    successMsg.append(line);
                }

                while ((line = errorResult.readLine()) != null) {
                    errorMsg.append(line);
                }
                MMLog.i(TAG, "install success_msg " + successMsg.toString());
                MMLog.i(TAG, "install error_msg " + errorMsg.toString());
            } catch (Exception e) {
                MMLog.e(TAG, e.toString());
            } finally {
                try {
                    if (successResult != null) {
                        successResult.close();
                    }
                } catch (IOException e) {
                    MMLog.e(TAG, e.toString());
                }
                try {
                    if (errorResult != null) {
                        errorResult.close();
                    }
                } catch (IOException e) {
                    MMLog.e(TAG, e.toString());
                }
                try {
                    if (process != null) {
                        process.destroy();
                    }
                } catch (Exception e) {
                    MMLog.e(TAG, e.toString());
                }
            }
        } catch (Exception e) {
            MMLog.e(TAG, e.toString());
        }
    }

    private synchronized static boolean uninstall(String packageName) {
        Process process = null;
        BufferedReader successResult = null;
        BufferedReader errorResult = null;
        StringBuilder successMsg = new StringBuilder();
        StringBuilder errorMsg = new StringBuilder();
        try {
            process = new ProcessBuilder("pm", "uninstall", packageName).start();
            successResult = new BufferedReader(new InputStreamReader(process.getInputStream()));
            errorResult = new BufferedReader(new InputStreamReader(process.getErrorStream()));
            String s;
            while ((s = successResult.readLine()) != null) {
                successMsg.append(s);
            }
            while ((s = errorResult.readLine()) != null) {
                errorMsg.append(s);
            }
        } catch (Exception e) {
            MMLog.d(TAG, e.toString());
        } finally {
            try {
                if (successResult != null) {
                    successResult.close();
                }
                if (errorResult != null) {
                    errorResult.close();
                }
            } catch (Exception e) {
                MMLog.d(TAG, e.toString());
            }
            if (process != null) {
                process.destroy();
            }
        }
        //如果含有"success"单词则认为卸载成功
        MMLog.log(TAG, successMsg.toString());
        MMLog.log(TAG, errorMsg.toString());
        return successMsg.toString().equalsIgnoreCase("success");
    }

    private static String getFWVersionName() {
        String version = Build.MODEL + ","
                + Build.VERSION.SDK_INT + ","
                + Build.VERSION.RELEASE;
        return version;
    }

    private String getRequestJSON() {
        JSONObject jsonObj = new JSONObject();
        //networkInformation.getMAC(), networkInformation.getInternetIP(), networkInformation.regionToJson()
        try {
            jsonObj.put("name", pName); //不推送pName
            jsonObj.put("brand", pBrand);
            jsonObj.put("customer", pCustomer);
            if (networkInformation != null) {
                jsonObj.put("mac", networkInformation.getMAC());
                jsonObj.put("ip", networkInformation.getInternetIP());
                jsonObj.put("region", networkInformation.regionToJson());
            }
            jsonObj.put("appVersion", null);//VERSION_NAME
            jsonObj.put("fwVersion", getFWVersionName());
        } catch (JSONException e) {
            //e.printStackTrace();
        }
        return jsonObj.toString();
    }

    private void checkAndUpdateDevice(boolean startAgainFlag) {
        TTask tTask = tTaskManager.getTaskByName(DataID.SESSION_UPDATE_JHZ_TEST_UPDATE_NAME);
        if (tTask == null) {
            MMLog.i(TAG, "NOT FOUND TASK SESSION_UPDATE_JHZ_TEST_UPDATE_NAME!!");
            return;
        }

        if (!tTask.isBusy()) {
            ((TNetTask) (tTask)).setRequestParameter(getRequestJSON());
            if (startAgainFlag)
                ((TNetTask) (tTask)).startAgain();
            else
                ((TNetTask) (tTask)).start();
        }
    }

    private void onNetStatusChanged() {
        if (tTaskManager != null && networkInformation != null) {
            checkAndUpdateDevice(false);
        }
    }

    @Override
    public void onNetStatusChanged(NetworkInformation networkInformation) {
        if (networkInformation.isConnected() &&
                NotEmptyString(networkInformation.getInternetIP()) &&
                NotEmptyString(networkInformation.getLocalIP())) {
            this.networkInformation = networkInformation;
            onNetStatusChanged();
        }
    }

}