package com.zhuchao.android.net;

import static com.zhuchao.android.fbase.FileUtils.EmptyString;
import static com.zhuchao.android.fbase.FileUtils.NotEmptyString;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.ConnectivityManager;
import android.net.Network;
import android.net.NetworkCapabilities;
import android.net.NetworkInfo;
import android.net.NetworkRequest;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Handler;
import android.os.Looper;

import androidx.annotation.NonNull;

import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;
import com.zhuchao.android.eventinterface.HttpCallback;
import com.zhuchao.android.eventinterface.InvokeInterface;
import com.zhuchao.android.fbase.DataID;
import com.zhuchao.android.fbase.FileUtils;
import com.zhuchao.android.fbase.MMLog;
import com.zhuchao.android.fbase.TTask;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.util.Enumeration;
import java.util.Locale;
import java.util.regex.Pattern;

public class TNetUtils extends ConnectivityManager.NetworkCallback {
    public static final String TAG = "TNetUtils";
    private Context mContext;
    private final NetworkRequest networkRequest;
    private Handler MainLooperHandler = null;
    private NetworkStatusListener networkStatusListener;
    private final NetworkInformation networkInformation;

    private final TTask tTask_ParseExternalIP = new TTask("getInternetStatus");
    private final TTask tTask_NetworkCallback = new TTask("NetworkCallback ");

    public interface NetworkStatusListener {
        void onNetStatusChanged(NetworkInformation networkInformation);
    }

    @Override
    public void onAvailable(@NonNull Network network) {
        super.onAvailable(network);
        MMLog.log(TAG, "onAvailable()");
    }

    @Override
    public void onLosing(@NonNull Network network, int maxMsToLive) {
        super.onLosing(network, maxMsToLive);
        MMLog.log(TAG, "onLosing()");
    }

    @Override
    public void onLost(@NonNull Network network) {
        super.onLost(network);
        MMLog.log(TAG, "onLost()");
    }

    @Override
    public void onUnavailable() {
        super.onUnavailable();
        MMLog.log(TAG, "onUnavailable()");
    }

    public TNetUtils(Context context) {
        mContext = context;//.getApplicationContext()
        MainLooperHandler = new Handler(Looper.getMainLooper());
        networkInformation = new NetworkInformation();
        networkStatusListener = null;
        networkRequest = new NetworkRequest.Builder()
                .addTransportType(NetworkCapabilities.TRANSPORT_CELLULAR)
                .addTransportType(NetworkCapabilities.TRANSPORT_WIFI)
                .build();
        ConnectivityManager connectivityManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        connectivityManager.registerNetworkCallback(networkRequest, this);

        updateNetStatus();
        registerNetStatusListener();
    }

    public void free() {
        try {
            unRegisterNetReceiver();
            mContext = null;
            tTask_ParseExternalIP.freeFree();
            tTask_NetworkCallback.freeFree();
        } catch (Exception e) {
            MMLog.e(TAG, e.toString());
        }
    }

    public void NetStatusChangedCallBack(NetworkStatusListener networkStatusListener) {
        this.networkStatusListener = networkStatusListener;
        updateNetStatus();
    }

    public void registerNetStatusCallback(NetworkStatusListener networkStatusListener) {
        this.networkStatusListener = networkStatusListener;
        updateNetStatus();
    }

    private void registerNetStatusListener() {
        try {
            IntentFilter intentFilter = new IntentFilter();
            intentFilter.addAction(ConnectivityManager.CONNECTIVITY_ACTION);
            intentFilter.addAction(WifiManager.RSSI_CHANGED_ACTION);
            mContext.registerReceiver(NetworkChangedReceiver, intentFilter);
        } catch (Exception e) {
            MMLog.e(TAG, "registerNetStatusListener failed " + e.toString());
        }
    }

    private void unRegisterNetReceiver() {
        if (NetworkChangedReceiver != null) {
            mContext.unregisterReceiver(NetworkChangedReceiver);
            NetworkChangedReceiver = null;
        }
    }

    private void callBackNetworkStatus() {
        if (networkStatusListener == null) return;
        runOnMainUiThread(new Runnable() {
            @Override
            public void run() {
                networkStatusListener.onNetStatusChanged(networkInformation);
            }
        });
    }

    private synchronized void updateNetStatus() {
        runThreadNotOnMainUIThread(new Runnable() {
            @Override
            public void run() {
                GetNetStatusInformation();
                UpdateWiFiStrength();
            }
        });
    }

    private BroadcastReceiver NetworkChangedReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            final String action = intent.getAction();
            //MMLog.log(TAG, "NetworkChangedReceiver action = " + action.toString());
            if (tTask_NetworkCallback.isBusy()) return;
            tTask_NetworkCallback.invoke(new InvokeInterface() {
                @Override
                public void CALLTODO(String tag) {
                    if (action.equals(ConnectivityManager.CONNECTIVITY_ACTION)) {
                        GetNetStatusInformation();
                    } else if (action.equals(WifiManager.RSSI_CHANGED_ACTION)) {
                        UpdateWiFiStrength();
                    }
                }
            }).startAgain();

            /*runThreadNotOnMainUIThread(new Runnable() {
                @Override
                public void run() {
                    if (action.equals(ConnectivityManager.CONNECTIVITY_ACTION)) {
                        GetNetStatusInformation();
                    } else if (action.equals(WifiManager.RSSI_CHANGED_ACTION)) {
                        UpdateWiFiStrength();
                    }
                }
            });*/
        }
    };

    private synchronized void GetNetStatusInformation() {
        try {
            networkInformation.isAvailable = isAvailable();
            if (networkInformation.isAvailable) {
                networkInformation.isConnected = isLocalNetConnected();
                networkInformation.netType = getConnectType();
                networkInformation.localIP = getLocalIpAddress();
                networkInformation.MAC = getDeviceMAC();
                networkInformation.wifiMAC = getWiFiMacAddress();//this.getWifiMac();
                //if (EmptyString(networkInformation.internetIP)) {
                //    GetInternetIp();
                //}
            }
            callBackNetworkStatus();
        } catch (Exception e) {
            MMLog.e(TAG, e.toString());//e.printStackTrace();
        }
    }

    private synchronized void UpdateWiFiStrength() {
        try {
            WifiManager wifiManager = (WifiManager) mContext.getSystemService(Context.WIFI_SERVICE);
            if (wifiManager == null) return;
            WifiInfo wifiInfo = wifiManager.getConnectionInfo();
            if (wifiInfo != null && wifiInfo.getBSSID() != null) {
                networkInformation.wifiLevel = WifiManager.calculateSignalLevel(wifiInfo.getRssi(), 4);
            }
            callBackNetworkStatus();
        } catch (Exception e) {
            MMLog.e(TAG, e.toString());//e.printStackTrace();
        }
    }

    public boolean isAvailable() {
        ConnectivityManager connectivityManager = (ConnectivityManager) mContext.getSystemService(Context.CONNECTIVITY_SERVICE);
        if (connectivityManager == null) return false;
        NetworkInfo networkInfo = connectivityManager.getActiveNetworkInfo();
        if (networkInfo == null) return false;
        return networkInfo.isAvailable();
    }

    //判断是否有外网连接
    public synchronized static boolean isInternetReachable(String ip) {
        if (EmptyString(ip)) ip = "www.baidu.com";
        return pingInternet(ip);
    }

    public boolean isInternetAvailable() {
        return !EmptyString(networkInformation.internetIP) && !EmptyString(networkInformation.localIP) && !EmptyString(networkInformation.MAC);
    }

    public boolean isInternetConnected() {
        if (EmptyString(networkInformation.internetIP) || EmptyString(networkInformation.localIP) || EmptyString(networkInformation.MAC)) {
            return isInternetReachable(null);
        }
        return true;
    }

    public boolean isLocalNetConnected() {
        ConnectivityManager connectivityManager = (ConnectivityManager) mContext.getSystemService(Context.CONNECTIVITY_SERVICE);
        if (connectivityManager == null) return false;
        NetworkInfo networkInfo = connectivityManager.getActiveNetworkInfo();
        if (networkInfo != null) {
            return networkInfo.isConnected();
        }
        return false;
    }

    public boolean isWifiConnected() {
        ConnectivityManager connectivityManager = (ConnectivityManager) mContext.getSystemService(Context.CONNECTIVITY_SERVICE);
        if (connectivityManager == null) return false;
        NetworkInfo mWiFiNetworkInfo = connectivityManager.getNetworkInfo(ConnectivityManager.TYPE_WIFI);
        if (mWiFiNetworkInfo != null) {
            return mWiFiNetworkInfo.isConnected();
        }
        return false;
    }

    public boolean isMobileConnected() {
        ConnectivityManager connectivityManager = (ConnectivityManager) mContext.getSystemService(Context.CONNECTIVITY_SERVICE);
        if (connectivityManager == null) return false;
        NetworkInfo mMobileNetworkInfo = connectivityManager.getNetworkInfo(ConnectivityManager.TYPE_MOBILE);
        if (mMobileNetworkInfo != null) {
            return mMobileNetworkInfo.isConnected();
        }
        return false;
    }

    public int getConnectType() {
        ConnectivityManager connectivityManager = (ConnectivityManager) mContext.getSystemService(Context.CONNECTIVITY_SERVICE);
        if (connectivityManager == null) return -1;
        NetworkInfo mNetworkInfo = connectivityManager.getActiveNetworkInfo();
        if (mNetworkInfo != null && mNetworkInfo.isAvailable()) {
            return mNetworkInfo.getType();
        }
        return -1;
    }

    public synchronized static boolean pingInternet(String ip) {
        try {
            //String ip = "www.baidu.com";
            Process p = Runtime.getRuntime().exec("ping -c 3 -w 100 " + ip);// ping网址3次
            InputStream input = p.getInputStream();
            BufferedReader in = new BufferedReader(new InputStreamReader(input));
            StringBuilder stringBuffer = new StringBuilder();
            String content = "";
            while ((content = in.readLine()) != null) {
                stringBuffer.append(content);
            }
            int status = p.waitFor();
            if (status == 0) {
                //result = "success";
                //MMLog.log(TAG, "ping IP:" + ip + " success");
                return true;
            }  //result = "failed";

        } catch (IOException | InterruptedException e) {
            MMLog.e(TAG, e.toString());
        }
        //MMLog.log(TAG, "ping IP:" + ip + " " + result);
        return false;
    }

    public static String getDeviceMAC() {
        String mac = getEthernetMacFromFile();
        if (EmptyString(mac)) {
            mac = getEthernetMacFromInterface();
        }
        if (EmptyString(mac))
            return mac;
        else
            return mac.toUpperCase();
    }

    public static String getEthernetMacFromInterface() {
        String mac = null;
        try {
            Enumeration<NetworkInterface> interfaces = NetworkInterface.getNetworkInterfaces();
            while (interfaces.hasMoreElements()) {
                NetworkInterface networkInterface = interfaces.nextElement();
                String name = networkInterface.getName();
                byte[] address = networkInterface.getHardwareAddress();
                if ((address == null) || (address.length == 0)) {
                    continue;
                }
                StringBuilder buffer = new StringBuilder();
                for (byte b : address) {
                    buffer.append(String.format("%02X:", b));
                }
                if (buffer.length() > 0) {
                    buffer.deleteCharAt(buffer.length() - 1);
                }
                mac = buffer.toString().toLowerCase(Locale.ENGLISH);
                if (name.startsWith("eth")) {
                    if (!MatcherMAC(mac)) {
                        mac = "00:00:00:00:00:00";
                    }
                    return mac;
                }
            }
        } catch (Exception e) {
            MMLog.e(TAG, e.toString());
        }
        return "00:00:00:00:00:00";
    }

    // 从系统文件中获取以太网MAC地址
    public static String getEthernetMacFromFile() {
        String sMac = null;
        try {
            sMac = loadFileAsString("/sys/class/net/eth0/address").toUpperCase().substring(0, 17);
        } catch (IOException ignored) {
        }
        if (EmptyString(sMac)) {
            try {
                sMac = loadFileAsString("/sys/class/net/wlan0/address").toUpperCase().substring(0, 17);
            } catch (IOException ignored) {
            }
        }
        return sMac;
    }

    // 读取系统文件
    private static String loadFileAsString(String filePath) throws IOException {
        StringBuilder fileData = new StringBuilder(1000);
        BufferedReader reader = new BufferedReader(new FileReader(filePath));
        char[] buf = new char[1024];
        int numRead = 0;
        while ((numRead = reader.read(buf)) != -1) {
            String readData = String.valueOf(buf, 0, numRead);
            fileData.append(readData);
        }
        reader.close();
        return fileData.toString();
    }

    // 从系统文件中获取WIFI MAC地址
    //@SuppressLint("MissingPermission")
    public String getWiFiMacAddress() {
        WifiManager wifiManager = (WifiManager) mContext.getSystemService(Context.WIFI_SERVICE);
        if (wifiManager == null) return "";
        WifiInfo wifiInfo = wifiManager.getConnectionInfo();
        String wifiMac = wifiInfo.getMacAddress();
        if (NotEmptyString(wifiMac))
            return wifiMac.toUpperCase();
        return wifiInfo.getMacAddress();
    }

    public static String getWifiMac(Context context) {
        WifiManager LWifiManager = (WifiManager) context.getApplicationContext().getSystemService(Context.WIFI_SERVICE);
        if (LWifiManager == null) return "";

        boolean wifiInitState = LWifiManager.isWifiEnabled();
        String mac = null;
        try {
            if (!wifiInitState) {
                // boolean openWifi = LWifiManager.setWifiEnabled(true);
                return "00:00:00:00:00:00";
            }
            for (int i = 0; i < 10; i++) {
                if (LWifiManager.isWifiEnabled()) {
                    break;
                }
                Thread.sleep(1000);
            }
            Enumeration<NetworkInterface> interfaces = NetworkInterface.getNetworkInterfaces();
            while (interfaces.hasMoreElements()) {
                NetworkInterface networkInterface = interfaces.nextElement();
                String name = networkInterface.getName();
                byte[] hardwareAddress = networkInterface.getHardwareAddress();
                if ((hardwareAddress == null) || (hardwareAddress.length == 0)) {
                    continue;
                }
                StringBuilder buffer = new StringBuilder();
                for (byte b : hardwareAddress) {
                    buffer.append(String.format("%02X:", b));
                }
                if (buffer.length() > 0) {
                    buffer.deleteCharAt(buffer.length() - 1);
                }
                mac = buffer.toString().toLowerCase(Locale.ENGLISH);
                if (name.startsWith("wlan")) {
                    if (MatcherMAC(mac)) return mac;
                    //mac = "00:00:00:00:00:00";
                }
            }
        } catch (Exception e) {
            //MMLog.log(TAG, e.toString());
        }
        return "00:00:00:00:00:00";
    }

    public static String getLocalIpAddress() {
        try {
            Enumeration<NetworkInterface> en = NetworkInterface.getNetworkInterfaces();
            if (en == null) return "";
            while (en.hasMoreElements()) {
                NetworkInterface networkInterface = en.nextElement();
                for (Enumeration<InetAddress> enumeration = networkInterface
                        .getInetAddresses(); enumeration.hasMoreElements(); ) {
                    InetAddress inetAddress = enumeration.nextElement();
                    if (!inetAddress.isLoopbackAddress()) {
                        String ip = inetAddress.getHostAddress();
                        if (MatcherIP4(ip)) return ip;
                    }
                }
            }
        } catch (SocketException ex) {
            MMLog.e(TAG, ex.toString());
        }
        return "";
    }

    public void GetInternetIp() {
        if (tTask_ParseExternalIP.isBusy()) return;
        tTask_ParseExternalIP.invoke(new InvokeInterface() {
            @Override
            public void CALLTODO(String tag) {
                try {
                    HttpUtils.requestGet("GetIP", "http://ip-api.com/json", new HttpCallback() {
                        @Override
                        public void onEventHttpRequest(String tag, String fromUrl, String toUrl, long progress, long total, String result, int status) {
                            if (status == DataID.TASK_STATUS_SUCCESS) {
                                final IPDataBean ipDataBean = fromJson(result, IPDataBean.class);
                                if (ipDataBean != null) {
                                    networkInformation.internetIP = ipDataBean.getQuery().trim();
                                    networkInformation.regionName = ipDataBean.getRegionName();
                                    networkInformation.country = ipDataBean.getCountry();
                                    networkInformation.region = ipDataBean.getRegion();
                                    networkInformation.city = ipDataBean.getCity();
                                    networkInformation.timezone = ipDataBean.getTimezone();
                                    networkInformation.organization = ipDataBean.getOrg();
                                    networkInformation.lon = ipDataBean.getLon();
                                    networkInformation.lat = ipDataBean.getLat();
                                    networkInformation.zip = ipDataBean.getZip();
                                    networkInformation.isp = ipDataBean.getIsp();
                                    MMLog.log(TAG, "External IP:" + networkInformation.internetIP);
                                    if (networkStatusListener != null && networkInformation.internetIP.length() > 4)
                                        networkStatusListener.onNetStatusChanged(networkInformation);
                                }
                                tTask_ParseExternalIP.free();
                            }
                        }
                    });
                } catch (Exception e) {
                    MMLog.e(TAG, "GetInternetIp ->" + e.toString());
                }
            }//CALLTODO
        });
        tTask_ParseExternalIP.startAgain();
    }

    public static String getDeviceUUID() {
        //String deviceID = getLanMac();
        //String deviceID = getEthernetMacFromFile();
        String deviceID = getCPUSerialCode();
        if (EmptyString(deviceID) || deviceID.equals("0000000000000000")) {
            deviceID = getEthernetMacFromFile();
            return FileUtils.MD5(deviceID);
        }
        return (deviceID);
    }

    private static boolean MatcherMAC(String mac) {
        if (EmptyString(mac)) {
            return false;
        }
        String patternMac = "^[a-f0-9]{2}(:[a-f0-9]{2}){5}$";
        return Pattern.compile(patternMac).matcher(mac).find();
    }

    private static boolean MatcherIP4(String IP4) {
        if (EmptyString(IP4)) return false;
        String regex = "^(1\\d{2}|2[0-4]\\d|25[0-5]|[1-9]\\d|[1-9])\\." +
                "(1\\d{2}|2[0-4]\\d|25[0-5]|[1-9]\\d|\\d)\\." +
                "(1\\d{2}|2[0-4]\\d|25[0-5]|[1-9]\\d|\\d)\\." +
                "(1\\d{2}|2[0-4]\\d|25[0-5]|[1-9]\\d|\\d)$";
        if (IP4.matches(regex))
            return true;
        else
            return false;
    }

    public static String getCPUSerialCode() {
        String cpuSerial = "0000000000000000";
        String cmd = "cat /proc/cpuinfo";
        try {
            Process p = Runtime.getRuntime().exec(cmd);
            StringBuilder data = new StringBuilder();
            BufferedReader ie = new BufferedReader(new InputStreamReader(p.getErrorStream()));
            BufferedReader in = new BufferedReader(new InputStreamReader(p.getInputStream()));
            String error = null;
            while ((error = ie.readLine()) != null && !error.equals("null")) {
                data.append(error).append("\n");
            }
            String line = null;
            while ((line = in.readLine()) != null && !line.equals("null")) {
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

    public void runOnMainUiThread(final Runnable runnable) {
        if (Looper.myLooper() == Looper.getMainLooper()) {
            runnable.run();
        } else {
            MainLooperHandler.post(runnable);//发送到主线程执行
        }
    }

    public void runThread(final Runnable runnable) {
        runnable.run();//直接执行
    }

    public void runThreadNotOnMainUIThread(final Runnable runnable) {
        runnable.run();
    }

    public static <T> T fromJson(String json, Class<T> classOfT) {
        try {
            return new Gson().fromJson(json, classOfT);
        } catch (JsonSyntaxException e) {
            //MMLog.e(TAG, "fromJson failed!");
            return null;
        }
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////
    public NetworkInformation getNetworkInformation() {
        return networkInformation;
    }

    //把拼音的省份改成中文
    public static String getChineseRegion(String province) {
        province = province.trim();
        String region = province;

        if (province.equalsIgnoreCase("Guangdong")) {
            region = "广东省";
        } else if (province.equalsIgnoreCase("Guangxi")) {
            region = "广西壮族自治区";
        } else if (province.equalsIgnoreCase("Hainan")) {
            region = "海南省";
        } else if (province.equalsIgnoreCase("Beijing")) {
            region = "北京市";
        } else if (province.equalsIgnoreCase("Tianjin")) {
            region = "天津市";
        } else if (province.equalsIgnoreCase("Shanghai")) {
            region = "上海市";
        } else if (province.equalsIgnoreCase("Chongqing")) {
            region = "重庆市";
        } else if (province.equalsIgnoreCase("Hebei")) {
            region = "河北省";
        } else if (province.equalsIgnoreCase("Henan")) {
            region = "河南省";
        } else if (province.equalsIgnoreCase("Yunan")) {
            region = "云南省";
        } else if (province.equalsIgnoreCase("Liaoning")) {
            region = "辽宁省";
        } else if (province.equalsIgnoreCase("Heilongjiang")) {
            region = "黑龙江省";
        } else if (province.equalsIgnoreCase("Hunan")) {
            region = "湖南省";
        } else if (province.equalsIgnoreCase("Anhui")) {
            region = "安徽省";
        } else if (province.equalsIgnoreCase("Shandong")) {
            region = "山东省";
        } else if (province.equalsIgnoreCase("Xinjiang")) {
            region = "新疆维吾尔族自治区";
        } else if (province.equalsIgnoreCase("Jiangsu")) {
            region = "江苏省";
        } else if (province.equalsIgnoreCase("Zhejiang")) {
            region = "浙江省";
        } else if (province.equalsIgnoreCase("Jiangxi")) {
            region = "江西省";
        } else if (province.equalsIgnoreCase("Hubei")) {
            region = "湖北省";
        } else if (province.equalsIgnoreCase("Gansu")) {
            region = "甘肃省";
        } else if (province.equalsIgnoreCase("Shanxi")) {
            region = "山西省";
        } else if (province.equalsIgnoreCase("Shanxi")) {
            region = "陕西省";
        } else if (province.equalsIgnoreCase("Neimenggu")) {
            region = "内蒙古蒙古族自治区";
        } else if (province.equalsIgnoreCase("Jilin")) {
            region = "吉林省";
        } else if (province.equalsIgnoreCase("Fujian")) {
            region = "福建省";
        } else if (province.equalsIgnoreCase("Guizhou")) {
            region = "贵州省";
        } else if (province.equalsIgnoreCase("Qinghai")) {
            region = "青海省";
        } else if (province.equalsIgnoreCase("Sichuan")) {
            region = "四川省";
        } else if (province.equalsIgnoreCase("Xizang")) {
            region = "西藏藏族自治区";
        } else if (province.equalsIgnoreCase("Ningxia")) {
            region = "宁夏回族自治区";
        } else if (province.equalsIgnoreCase("Taiwan")) {
            region = "台湾省";
        } else if (province.equalsIgnoreCase("Hong Kong")) {
            region = "香港特别行政区";
        } else if (province.equalsIgnoreCase("Macao")) {
            region = "澳门特别行政区";
        }
        return region;
    }
}
