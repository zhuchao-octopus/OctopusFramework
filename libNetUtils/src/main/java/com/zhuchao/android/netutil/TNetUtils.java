package com.zhuchao.android.netutil;

import static com.zhuchao.android.libfileutils.FileUtils.EmptyString;

import android.annotation.SuppressLint;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;

import com.google.gson.Gson;
import com.zhuchao.android.libfileutils.MMLog;

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
    //没有网络连接
    public static final int NETWORN_NONE = 0;
    //wifi连接
    public static final int NETWORN_WIFI = 1;
    public static final int UnCon_WIFI = 7;
    //手机网络数据连接类型
    public static final int NETWORN_2G = 2;
    public static final int NETWORN_3G = 3;
    public static final int NETWORN_4G = 4;
    public static final int NETWORN_MOBILE = 5;
    public static final int NETWORN_ETHERNET = 6;

    private Context mContext;
    private NetStatusCallBack netStatusCallBack;
    private String MAC, WMAC, IP0, IP1, Location;
    private boolean NetStatus = false;
    private int WifiLevel = 0, NetType = -1;
    private WifiManager wifiManager;
    private ConnectivityManager connectivityManager;

    public interface NetStatusCallBack {
        void onNetStateChanged(boolean isConnected, int type, String MAC, String WMAC, String IP0, String IP1, String Location);
        void onWifiLevelChanged(int level);
    }

    public TNetUtils(Context context) {
        mContext = context;
        wifiManager = (WifiManager) mContext.getApplicationContext().getSystemService(Context.WIFI_SERVICE);
        connectivityManager = (ConnectivityManager) mContext.getApplicationContext().getSystemService(Context.CONNECTIVITY_SERVICE);
        MAC = "";
        WMAC = "";
        IP0 = "";
        IP1 = "";
        Location = "";
        NetStatus = false;
        NetType = -1;
        netStatusCallBack = null;
        registerNetReceiver();
    }

    public void free() {
        try {
            unRegisterNetReceiver();
            mContext = null;
        } catch (Exception e) {
            //e.printStackTrace();
            MMLog.e(TAG,e.toString());
        }
    }

    public static String getTAG() {
        return TAG;
    }

    public String getMAC() {
        return MAC;
    }

    public String getWMAC() {
        return WMAC;
    }

    public String getIP0() {
        return IP0;
    }

    public String getIP1() {
        return IP1;
    }

    public String getLocation() {
        return Location;
    }

    public boolean isNetStatus() {
        return NetStatus;
    }

    public int getWifiLevel() {
        return WifiLevel;
    }

    public int getNetType() {
        return NetType;
    }

    public void registerNetReceiver() {
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(ConnectivityManager.CONNECTIVITY_ACTION);
        intentFilter.addAction(WifiManager.RSSI_CHANGED_ACTION);
        mContext.registerReceiver(NetworkChangedReceiver, intentFilter);
    }

    public void unRegisterNetReceiver() {
        if (NetworkChangedReceiver != null) {
            mContext.unregisterReceiver(NetworkChangedReceiver);
            NetworkChangedReceiver = null;
        }
    }

    public boolean isNetCanConnect() {
        if(EmptyString(IP0) || EmptyString(IP1) || EmptyString(MAC)) {
            return isInternetOk();
        }
        return true;
    }

    public void NetStatusCallBack(NetStatusCallBack netStatusCallBack) {
        this.netStatusCallBack = netStatusCallBack;
    }

    private BroadcastReceiver NetworkChangedReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            final String action = intent.getAction();
            new Thread() {
                public void run() {
                    if (action.equals(ConnectivityManager.CONNECTIVITY_ACTION)) {
                        updateNetStatus();
                    } else if (action.equals(WifiManager.RSSI_CHANGED_ACTION)) {
                        updateWiFiStrength();
                    }
                }
            }.start();
        }
    };

    public synchronized void updateNetStatus() {
        NetworkInfo networkInfo = connectivityManager.getActiveNetworkInfo();
        if (networkInfo != null)
        {
            NetStatus = networkInfo.isAvailable();
            NetType = networkInfo.getType();
            if (networkInfo.isAvailable()) {
                //if (isInternetOk())
                IP0 = GetInternetIp();
                IP1 = getLocalIpAddress();
                MAC = getEthernetMacAddress();//this.getLanMac();
                WMAC = getWiFiMacAddress();//this.getWifiMac();
            } else {
                MAC = "";
                WMAC = "";
                IP1 = "";
                IP0 = "";
                Location = "";
            }
        }
        else
        {
            MAC = "";
            WMAC = "";
            IP1 = "";
            IP0 = "";
            Location = "";
        }
        if (netStatusCallBack != null) {
            netStatusCallBack.onNetStateChanged(NetStatus, NetType, MAC, WMAC, IP0, IP1, Location);
        }
    }

    private synchronized void updateWiFiStrength() {
        WifiInfo wifiInfo = wifiManager.getConnectionInfo();
        if (wifiInfo != null && wifiInfo.getBSSID() != null) {
            WifiLevel = WifiManager.calculateSignalLevel(wifiInfo.getRssi(), 4);
            if (netStatusCallBack != null) {
                netStatusCallBack.onWifiLevelChanged(WifiLevel);
            }
        }
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

    public int getConnectedType() {
        NetworkInfo mNetworkInfo = connectivityManager.getActiveNetworkInfo();
        if (mNetworkInfo != null && mNetworkInfo.isAvailable()) {
            return mNetworkInfo.getType();
        }
        return -1;
    }

    //判断是否有外网连接
    private synchronized final static boolean ping() {
        //String result = null;
        String ip = "www.baidu.com";
        try {
            Process p = Runtime.getRuntime().exec("ping -c 3 -w 100 " + ip);// ping网址3次
            InputStream input = p.getInputStream();
            BufferedReader in = new BufferedReader(new InputStreamReader(input));
            StringBuffer stringBuffer = new StringBuffer();
            String content = "";
            while ((content = in.readLine()) != null) {
                stringBuffer.append(content);
            }
            //MMLog.log(TAG, "ping IP:" + ip + " " + stringBuffer.toString());
            // ping的状态
            int status = p.waitFor();
            if (status == 0) {
                //result = "success";
                MMLog.log(TAG, "ping IP:" + ip + " success");
                return true;
            } else {
                //result = "failed";
            }
        } catch (IOException e) {
            MMLog.e(TAG,e.toString());
        } catch (InterruptedException e) {
            MMLog.e(TAG,e.toString());
        } finally {
        }
        //MMLog.log(TAG, "ping IP:" + ip + " " + result);
        return false;
    }

    public static final boolean isInternetOk() {
        return ping();
    }

    public static String getLanMac() {
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
                    if (!matcherMAC(mac)) {
                        mac = "00:00:00:00:00:00";
                    }
                    return mac;
                }
            }
        } catch (Exception e) {
            MMLog.e(TAG,e.toString());
        }
        return "00:00:00:00:00:00";
    }

    public String getWifiMac() {
        boolean wifiInitState = wifiManager.isWifiEnabled();
        String mac = null;
        try {
            if (!wifiInitState) {
                boolean openWifi = wifiManager.setWifiEnabled(true);
            }
            for (int i = 0; i < 10; i++) {
                if (wifiManager.isWifiEnabled()) {
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
                    if (!matcherMAC(mac)) {
                        mac = "00:00:00:00:00:00";
                    }
                    return mac;
                }
            }
        } catch (Exception e) {
            //e.printStackTrace();
            MMLog.log(TAG, "");
        } finally {
            if (!wifiInitState) {
                MMLog.log(TAG, "wifi close");
                wifiManager.setWifiEnabled(false);
            }
        }
        return "00:00:00:00:00:00";
    }

    private static boolean matcherMAC(String mac) {
        if(EmptyString(mac)) {
            return false;
        }
        String patternMac = "^[a-f0-9]{2}(:[a-f0-9]{2}){5}$";
        return Pattern.compile(patternMac).matcher(mac).find();
    }

    // 从系统文件中获取以太网MAC地址
    public static String getEthernetMacAddress() {
        try {
            return loadFileAsString("/sys/class/net/eth0/address").toUpperCase().substring(0, 17);
        } catch (IOException e) {
            MMLog.e(TAG,e.toString());
            return null;
        }
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

    public String GetInternetIp() {
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
                        StringBuilder strber = new StringBuilder();
                        String line = null;
                        while ((line = reader.readLine()) != null)
                            strber.append(line + "\n");
                        inStream.close();
                        //从反馈的结果中提取出IP地址
                        line = strber.toString();
                        //Gson gson = new GsonBuilder().serializeNulls().disableHtmlEscaping().create();
                        final IpBean rBean = new Gson().fromJson(line, IpBean.class);
                        IP0 = rBean.getQuery();
                        Location = rBean.getRegionName();
                        //region = Utils.getTheLanguageOfTheRegion(province);
                    } else {
                        IP0 = "";
                        Location = "";
                    }
                    if (netStatusCallBack != null) {
                        netStatusCallBack.onNetStateChanged(NetStatus, NetType, MAC, WMAC, IP0, IP1, Location);
                    }
                } catch (Exception e) {
                    IP0 = "";
                    Location = "";
                    MMLog.e(TAG, "GetInternetIp" + e.toString());
                }
            }
        }.start();
        return IP0;
    }

    public String getLocalIpAddress() {
        try {
            for (Enumeration<NetworkInterface> en = NetworkInterface.getNetworkInterfaces(); en.hasMoreElements(); ) {
                NetworkInterface networkInterface = en.nextElement();
                for (Enumeration<InetAddress> enumeration = networkInterface
                        .getInetAddresses(); enumeration.hasMoreElements(); ) {
                    InetAddress inetAddress = enumeration.nextElement();
                    if (!inetAddress.isLoopbackAddress()) {
                        return inetAddress.getHostAddress();
                    }
                }
            }
        } catch (SocketException ex) {
            MMLog.e(TAG,ex.toString());
        }
        return "";
    }

    public String getDeviceID() {
        String devID = getLanMac();
        if(EmptyString(devID)) {
            devID = getWifiMac();
        }
        return devID;
    }

    //把拼音的省份改成中文
    public String getChineseRegion(String province) {
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