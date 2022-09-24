package com.zhuchao.android.net;

import static com.zhuchao.android.fileutils.FileUtils.EmptyString;

import android.annotation.SuppressLint;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Handler;
import android.os.Looper;

import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;
import com.zhuchao.android.fileutils.FileUtils;
import com.zhuchao.android.fileutils.MMLog;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.net.URL;
import java.net.URLConnection;
import java.nio.charset.StandardCharsets;
import java.util.Enumeration;
import java.util.Locale;
import java.util.regex.Pattern;

public class TNetUtils {
    public static final String TAG = "TNetUtils";
    public static final int NETWORN_NONE = 0;
    public static final int NETWORN_WIFI = 1;
    public static final int UnCon_WIFI = 7;
    public static final int NETWORN_2G = 2;
    public static final int NETWORN_3G = 3;
    public static final int NETWORN_4G = 4;
    public static final int NETWORN_MOBILE = 5;
    public static final int NETWORN_ETHERNET = 6;
    private Context mContext;
    private Handler MainLooperHandler = null;
    private NetworkStatusListener networkStatusListener;
    private WifiManager wifiManager;
    private ConnectivityManager connectivityManager;
    private NetworkInformation networkInformation;

    public interface NetworkStatusListener {
        void onNetStatusChanged(NetworkInformation networkInformation);
    }

    public TNetUtils(Context context) {
        mContext = context;
        MainLooperHandler = new Handler(Looper.getMainLooper());
        wifiManager = (WifiManager) mContext.getApplicationContext().getSystemService(Context.WIFI_SERVICE);
        connectivityManager = (ConnectivityManager) mContext.getApplicationContext().getSystemService(Context.CONNECTIVITY_SERVICE);
        networkInformation = new NetworkInformation();
        networkStatusListener = null;
        updateStatus();
        registerNetStatusListener();
    }

    public void free() {
        try {
            unRegisterNetReceiver();
            mContext = null;
        } catch (Exception e) {
            MMLog.e(TAG, e.toString());
        }
    }

    public void NetStatusChangedCallBack(NetworkStatusListener networkStatusListener) {
        this.networkStatusListener = networkStatusListener;
        updateStatus();
    }

    public void registerNetStatusCallback(NetworkStatusListener networkStatusListener) {
        this.networkStatusListener = networkStatusListener;
        updateStatus();
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

    private void CallBackStatus() {
        if (networkStatusListener == null) return;
        runOnMainUiThread(new Runnable() {
            @Override
            public void run() {
                networkStatusListener.onNetStatusChanged(networkInformation);
            }
        });
    }

    private synchronized void updateStatus() {
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
            //MMLog.log(TAG, "NetworkChanged " + action.toString());
            runThreadNotOnMainUIThread(new Runnable() {
                @Override
                public void run() {
                    if (action.equals(ConnectivityManager.CONNECTIVITY_ACTION)) {
                        GetNetStatusInformation();
                    } else if (action.equals(WifiManager.RSSI_CHANGED_ACTION)) {
                        UpdateWiFiStrength();
                    }
                }
            });
        }
    };

    private synchronized void GetNetStatusInformation() {
        networkInformation.isAvailable = isAvailable();
        networkInformation.isConnected = isLocalNetConnected();
        networkInformation.netType = getConnectType();
        networkInformation.localIP = getLocalIpAddress();
        networkInformation.MAC = getDeviceMAC();
        networkInformation.wifiMAC = getWiFiMacAddress();//this.getWifiMac();
        if (isAvailable())
            GetInternetIp();
        CallBackStatus();
    }

    private synchronized void UpdateWiFiStrength() {
        WifiInfo wifiInfo = wifiManager.getConnectionInfo();
        if (wifiInfo != null && wifiInfo.getBSSID() != null) {
            networkInformation.wifiLevel = WifiManager.calculateSignalLevel(wifiInfo.getRssi(), 4);
        }
        CallBackStatus();
    }

    public boolean isNetCanConnect() {
        if (EmptyString(networkInformation.internetIP) || EmptyString(networkInformation.localIP) || EmptyString(networkInformation.MAC)) {
            return isInternetOk();
        }
        return true;
    }

    public boolean isLocalNetConnected() {
        NetworkInfo networkInfo = connectivityManager.getActiveNetworkInfo();
        if (networkInfo != null) {
            return networkInfo.isConnected();
        }
        return false;
    }

    public boolean isWifiConnected() {
        NetworkInfo mWiFiNetworkInfo = connectivityManager.getNetworkInfo(ConnectivityManager.TYPE_WIFI);
        if (mWiFiNetworkInfo != null) {
            return mWiFiNetworkInfo.isConnected();
        }
        return false;
    }

    public boolean isMobileConnected() {
        NetworkInfo mMobileNetworkInfo = connectivityManager.getNetworkInfo(ConnectivityManager.TYPE_MOBILE);
        if (mMobileNetworkInfo != null) {
            return mMobileNetworkInfo.isConnected();
        }
        return false;
    }

    public int getConnectType() {
        NetworkInfo mNetworkInfo = connectivityManager.getActiveNetworkInfo();
        if (mNetworkInfo != null && mNetworkInfo.isAvailable()) {
            return mNetworkInfo.getType();
        }
        return -1;
    }

    public boolean isAvailable() {
        NetworkInfo networkInfo = connectivityManager.getActiveNetworkInfo();
        if (networkInfo == null)
            return false;
        else
            return networkInfo.isAvailable();
    }

    //判断是否有外网连接
    public synchronized static final boolean isInternetOk() {
        return checkInternet();
    }

    private synchronized static final boolean checkInternet() {
        try {
            String ip = "www.baidu.com";
            Process p = Runtime.getRuntime().exec("ping -c 3 -w 100 " + ip);// ping网址3次
            InputStream input = p.getInputStream();
            BufferedReader in = new BufferedReader(new InputStreamReader(input));
            StringBuffer stringBuffer = new StringBuffer();
            String content = "";
            while ((content = in.readLine()) != null) {
                stringBuffer.append(content);
            }
            int status = p.waitFor();
            if (status == 0) {
                //result = "success";
                //MMLog.log(TAG, "ping IP:" + ip + " success");
                return true;
            } else {
                //result = "failed";
            }
        } catch (IOException e) {
            MMLog.e(TAG, e.toString());
        } catch (InterruptedException e) {
            MMLog.e(TAG, e.toString());
        } finally {
        }
        //MMLog.log(TAG, "ping IP:" + ip + " " + result);
        return false;
    }

    public static String getDeviceMAC() {
        String mac = getEthernetMacFromFile();
        if (EmptyString(mac)) {
            mac = getEthernetMacFromInterface();
        }
        return mac;
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
        } catch (IOException e) {
        }
        if (EmptyString(sMac)) {
            try {
                sMac = loadFileAsString("/sys/class/net/wlan0/address").toUpperCase().substring(0, 17);
            } catch (IOException e) {
            }
        }
        return sMac;
    }

    // 读取系统文件
    private static String loadFileAsString(String filePath) throws IOException {
        StringBuffer fileData = new StringBuffer(1000);
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
    @SuppressLint("MissingPermission")
    public String getWiFiMacAddress() {
        WifiInfo wifiInfo = wifiManager.getConnectionInfo();
        return wifiInfo.getMacAddress();
    }

    public static String getWifiMac(Context context) {
        WifiManager LWifiManager = (WifiManager) context.getApplicationContext().getSystemService(Context.WIFI_SERVICE);
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
                    if (!MatcherMAC(mac)) {
                        mac = "00:00:00:00:00:00";
                    }
                    return mac;
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
            if (en == null) return null;
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
        return null;
    }

    public void GetInternetIp() {
        new Thread() {
            @Override
            public void run() {
                super.run();
                try {
                    URL infoUrl = new URL("http://ip-api.com/json/");
                    URLConnection connection = infoUrl.openConnection();
                    HttpURLConnection httpConnection = (HttpURLConnection) connection;
                    int responseCode = httpConnection.getResponseCode();
                    if (responseCode == HttpURLConnection.HTTP_OK) {
                        InputStream inStream = httpConnection.getInputStream();
                        BufferedReader reader = new BufferedReader(new InputStreamReader(inStream, StandardCharsets.UTF_8));
                        StringBuilder stringBuilder = new StringBuilder();
                        String line = null;
                        while ((line = reader.readLine()) != null)
                            stringBuilder.append(line + "\n");
                        inStream.close();
                        //从反馈的结果中提取出IP地址
                        line = stringBuilder.toString();
                        //Gson gson = new GsonBuilder().serializeNulls().disableHtmlEscaping().create();
                        final IPDataBean ipDataBean = fromJson(line, IPDataBean.class);
                        if (ipDataBean != null) {
                            networkInformation.internetIP = ipDataBean.getQuery();
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
                            CallBackStatus();
                        } else {
                            // MMLog.e(TAG, "fromJson = null");
                        }
                    }
                } catch (Exception e) {
                    MMLog.e(TAG, "GetInternetIp" + e.toString());
                }
            }
        }.start();
    }

    public static String getDeviceUUID() {
        //String deviceID = getLanMac();
        String deviceID = getEthernetMacFromFile();
        if (EmptyString(deviceID)) {
            //deviceID = getLanMac();
            MMLog.e(TAG, "getDeviceUUID() failed error");
            return deviceID;
        }
        return FileUtils.md5(deviceID);
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
            Object object = new Gson().fromJson(json, classOfT);
            return (T) object;
        } catch (JsonSyntaxException e) {
            MMLog.e(TAG, "fromJson failed " + e.toString() + "," + json);
            return null;
        }
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////
    public NetworkInformation getNetworkInformation() {
        return networkInformation;
    }
}
