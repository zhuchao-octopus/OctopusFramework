package com.zhuchao.android.netutil;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.text.TextUtils;
import android.util.Log;

import com.google.gson.Gson;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.InetAddress;
import java.net.MalformedURLException;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.net.URL;
import java.net.URLConnection;
import java.nio.charset.StandardCharsets;
import java.util.Enumeration;
import java.util.Locale;
import java.util.regex.Pattern;

public class NetUtils {
    public static final String TAG = "NetUtils";
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
    private NetChangedCallBack mNetChangedCallBack;
    private String MAC, WMAC, IP0, IP1, Location;
    private boolean NetStatus = false;
    private int WifiLevel = 0, NetType = -1;
    private WifiManager mWifiManager;
    private ConnectivityManager mConnectivityManager;
    //private NetUtils netUtils =null;

    public interface NetChangedCallBack {
        void onNetStateChanged(boolean isConnected, int type, String MAC, String WMAC, String IP0, String IP1, String Location);

        void onWifiLevelChanged(int level);
    }

    public NetUtils(Context context, NetChangedCallBack netChangedCallBack) {
        mContext = context;
        mNetChangedCallBack = netChangedCallBack;
        mWifiManager = (WifiManager) mContext.getApplicationContext().getSystemService(Context.WIFI_SERVICE);
        mConnectivityManager = (ConnectivityManager) mContext.getApplicationContext().getSystemService(Context.CONNECTIVITY_SERVICE);
        MAC = "";
        WMAC = "";
        IP1 = "";
        IP0 = "";
        Location = "";
        NetStatus = false;
        NetType = -1;
        registerNetReceiver();
    }

    public void Free() {
        try {
            unRegisterNetReceiver();
            mContext = null;
        } catch (Exception e) {
            e.printStackTrace();
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

        if (TextUtils.isEmpty(IP0) || TextUtils.isEmpty(IP1) || TextUtils.isEmpty(MAC)) {
            return isInternetOk();
        }

        return true;
    }

    public NetUtils setmNetChangedCallBack(NetChangedCallBack mNetChangedCallBack) {
        this.mNetChangedCallBack = mNetChangedCallBack;
        return this;
    }

    private BroadcastReceiver NetworkChangedReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            final String action = intent.getAction();
            Log.d(TAG, "Got Action>>>>>>>:" + action);
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

    private synchronized void updateNetStatus() {
        NetworkInfo networkInfo = mConnectivityManager.getActiveNetworkInfo();
        if (networkInfo != null) {
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

        } else {
            MAC = "";
            WMAC = "";
            IP1 = "";
            IP0 = "";
            Location = "";
        }
        if (mNetChangedCallBack != null) {
            mNetChangedCallBack.onNetStateChanged(NetStatus, NetType, MAC, WMAC, IP0, IP1, Location);
        }
    }

    private synchronized void updateWiFiStrength() {

        WifiInfo wifiInfo = mWifiManager.getConnectionInfo();
        if (wifiInfo != null && wifiInfo.getBSSID() != null) {
            WifiLevel = WifiManager.calculateSignalLevel(wifiInfo.getRssi(), 4);

            if (mNetChangedCallBack != null) {
                mNetChangedCallBack.onWifiLevelChanged(WifiLevel);
            }
        }
    }

    public boolean isLocalNetConnected() {
        NetworkInfo mNetworkInfo = mConnectivityManager.getActiveNetworkInfo();
        if (mNetworkInfo != null) {
            return mNetworkInfo.isConnected();
        }

        return false;
    }

    public boolean isWifiConnected() {
        NetworkInfo mWiFiNetworkInfo = mConnectivityManager.getNetworkInfo(ConnectivityManager.TYPE_WIFI);
        if (mWiFiNetworkInfo != null) {
            return mWiFiNetworkInfo.isConnected();
        }

        return false;
    }

    public boolean isMobileConnected() {

        NetworkInfo mMobileNetworkInfo = mConnectivityManager.getNetworkInfo(ConnectivityManager.TYPE_MOBILE);
        if (mMobileNetworkInfo != null) {
            return mMobileNetworkInfo.isConnected();
        }

        return false;
    }

    public int getConnectedType() {
        NetworkInfo mNetworkInfo = mConnectivityManager.getActiveNetworkInfo();
        if (mNetworkInfo != null && mNetworkInfo.isAvailable()) {
            return mNetworkInfo.getType();
        }

        return -1;
    }


    //判断是否有外网连接

    private synchronized final static boolean ping() {
        String result = null;
        try {
            String ip = "www.baidu.com";
            Process p = Runtime.getRuntime().exec("ping -c 3 -w 100 " + ip);// ping网址3次
            // 读取ping的内容，可以不加
            InputStream input = p.getInputStream();
            BufferedReader in = new BufferedReader(new InputStreamReader(input));
            StringBuffer stringBuffer = new StringBuffer();
            String content = "";
            while ((content = in.readLine()) != null) {
                stringBuffer.append(content);
            }
            Log.d("NetUtils -->", stringBuffer.toString());
            // ping的状态
            int status = p.waitFor();
            if (status == 0) {
                result = "success";
                Log.d("NetUtils -->", result);
                return true;
            } else {
                result = "failed";
            }
        } catch (IOException e) {
            result = "IOException";
        } catch (InterruptedException e) {
            result = "InterruptedException";
        } finally {
            //Log.d("----result---", "result = " + result);
        }
        Log.d("NetUtils -->", result);
        return false;
    }

    public static final boolean isInternetOk() {
        return ping();
    }


    public String getDeviceID() {
        String devID = getLanMac();
        if (TextUtils.isEmpty(devID)) {
            devID = getWifiMac();
        }
        return devID;
    }


    public static String getLanMac() {
        String mac = null;
        try {
            Enumeration<NetworkInterface> interfaces = NetworkInterface.getNetworkInterfaces();
            while (interfaces.hasMoreElements()) {
                NetworkInterface networkInterface = interfaces.nextElement();
                String name = networkInterface.getName();
                byte[] addr = networkInterface.getHardwareAddress();
                if ((addr == null) || (addr.length == 0)) {
                    continue;
                }
                StringBuilder buffer = new StringBuilder();
                for (byte b : addr) {
                    buffer.append(String.format("%02X:", b));
                }
                if (buffer.length() > 0) {
                    buffer.deleteCharAt(buffer.length() - 1);
                }

                mac = buffer.toString().toLowerCase(Locale.ENGLISH);

                if (name.startsWith("eth")) {
                    if (!jyMac(mac)) {
                        mac = "00:00:00:00:00:00";
                    }
                    return mac;
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            Log.e(TAG, "");
        }

        return "00:00:00:00:00:00";
    }

    public String getWifiMac() {

        //WifiManager wifiManager = (WifiManager) context.getApplicationContext().getSystemService(Context.WIFI_SERVICE);

        boolean wifiInitState = mWifiManager.isWifiEnabled();

        String mac = null;

        try {

            if (!wifiInitState) {
                boolean openWifi = mWifiManager.setWifiEnabled(true);
            }

            for (int i = 0; i < 10; i++) {
                if (mWifiManager.isWifiEnabled()) {
                    break;
                }
                Thread.sleep(1000);
            }

            Enumeration<NetworkInterface> interfaces = NetworkInterface.getNetworkInterfaces();
            while (interfaces.hasMoreElements()) {
                NetworkInterface networkInterface = interfaces.nextElement();
                String name = networkInterface.getName();
                byte[] addr = networkInterface.getHardwareAddress();
                if ((addr == null) || (addr.length == 0)) {
                    continue;
                }
                StringBuilder buffer = new StringBuilder();
                for (byte b : addr) {
                    buffer.append(String.format("%02X:", b));
                }
                if (buffer.length() > 0) {
                    buffer.deleteCharAt(buffer.length() - 1);
                }

                mac = buffer.toString().toLowerCase(Locale.ENGLISH);

                if (name.startsWith("wlan")) {
                    if (!jyMac(mac)) {
                        mac = "00:00:00:00:00:00";
                    }
                    return mac;
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            Log.e(TAG, "");
        } finally {
            if (!wifiInitState) {
                Log.d(TAG, "wifi close");
                mWifiManager.setWifiEnabled(false);
            }
        }

        return "00:00:00:00:00:00";
    }

    private static boolean jyMac(String mac) {
        if (TextUtils.isEmpty(mac)) {
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
            e.printStackTrace();
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
    public String getWiFiMacAddress() {
        //WifiManager my_wifiManager = ((WifiManager) context.getSystemService(Context.WIFI_SERVICE));
        WifiInfo wifiInfo = mWifiManager.getConnectionInfo();
        return wifiInfo.getMacAddress();
    }


    /**
     * 获取外网IP
     *
     * @return
     */
    public String GetInternetIp() {
        new Thread() {
            @Override
            public void run() {
                super.run();
                try {
                    URL infoUrl = new URL("http://ip-api.com/json/");
                    URLConnection connection = infoUrl.openConnection();
                    //String cidIP = "";
                    //String province = "";

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
                    if (mNetChangedCallBack != null) {
                        mNetChangedCallBack.onNetStateChanged(NetStatus, NetType, MAC, WMAC, IP0, IP1, Location);
                    }
                }  catch (Exception e)
                {
                    IP0 = "";
                    Location = "";
                    Log.d(TAG,"GetInternetIp" + e.toString());
                }
            }
        }.start();
        return IP0;
    }

    public String getLocalIpAddress() {
        try {
            for (Enumeration<NetworkInterface> en = NetworkInterface.getNetworkInterfaces(); en.hasMoreElements(); ) {
                NetworkInterface intf = en.nextElement();
                for (Enumeration<InetAddress> enumIpAddr = intf
                        .getInetAddresses(); enumIpAddr.hasMoreElements(); ) {
                    InetAddress inetAddress = enumIpAddr.nextElement();
                    if (!inetAddress.isLoopbackAddress()) {
                        return inetAddress.getHostAddress();
                    }
                }
            }
        } catch (SocketException ex) {
            Log.e("Exception", ex.toString());
        }
        return "";
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
