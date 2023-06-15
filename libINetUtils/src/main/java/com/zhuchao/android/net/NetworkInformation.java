package com.zhuchao.android.net;

import static com.zhuchao.android.net.TNetUtils.TAG;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonSyntaxException;
import com.zhuchao.android.fbase.MMLog;

import org.json.JSONException;
import org.json.JSONObject;

public class NetworkInformation {
    public final static int NetworkInformation_onUnknow = 0;
    public final static int NetworkInformation_onAvailable = 1;
    public final static int NetworkInformation_onLosing = 2;
    public final static int NetworkInformation_onLost = 3;
    public final static int NetworkInformation_onUnavailable = 4;
    public final static int NetworkInformation_onRSSI = 5;
    public final static int NetworkInformation_onCONNECTIVITY = 6;
    int action;
    boolean isAvailable;
    boolean isConnected;
    int netType;
    String MAC;
    String wifiMAC;
    String localIP;
    int wifiLevel;
    String country;
    String countryCode;
    String region;
    String regionName;
    String city;
    String zip;
    double lat;
    double lon;
    String timezone;
    String isp;
    String organization;
    String internetIP;


    public NetworkInformation() {
        this.action = NetworkInformation_onUnknow;
        this.isAvailable = false;
        this.isConnected = false;
        this.netType = -1;
        this.internetIP = "";
        this.localIP = "";
        this.MAC = "";
        this.wifiMAC = "";
        this.wifiLevel = 4;
        this.country = "";
        this.countryCode = "";
        this.region = "";
        this.regionName = "";
        this.city = "";
        this.timezone = "";
        lat = 0.00;
        lon = 0.00;
        organization = "";
        isp = "";
        zip = "";
    }

    public int getAction() {
        return action;
    }

    public void setAction(int action) {
        this.action = action;
    }

    public boolean isAvailable() {
        return isAvailable;
    }

    public int getNetType() {
        return netType;
    }

    public String getLocalIP() {
        return localIP;
    }

    public String getMAC() {
        return MAC;
    }

    public String getWifiMAC() {
        return wifiMAC;
    }

    public int getWifiLevel() {
        return wifiLevel;
    }

    public String getCountry() {
        return country;
    }

    public String getCountryCode() {
        return countryCode;
    }

    public String getRegion() {
        return region;
    }

    public String getRegionName() {
        return regionName;
    }

    public String getCity() {
        return city;
    }

    public String getZip() {
        return zip;
    }

    public double getLat() {
        return lat;
    }

    public double getLon() {
        return lon;
    }

    public String getTimezone() {
        return timezone;
    }

    public String getIsp() {
        return isp;
    }

    public String getOrganization() {
        return organization;
    }

    public String getInternetIP() {
        return internetIP;
    }

    public boolean isConnected() {
        return isConnected;
    }

    public String toString() {
        String str = "" + action;
        str += "," + isAvailable;
        str += "," + isConnected;
        str += "," + MAC;
        str += "," + wifiMAC;
        str += "," + wifiLevel;
        str += "," + localIP;
        str += "," + internetIP;
        str += "," + country;
        str += "," + countryCode;
        str += "," + region;
        str += "," + regionName;
        str += "," + timezone;
        str += "," + city;
        str += "," + organization;
        str += "," + isp;
        str += ",(" + lon + " " + lat + ")";
        return str;
    }

    public String regionToString() {
        String str = internetIP;
        str += "," + country;
        //str += "," + countryCode;
        //str += "," + region;
        str += "," + regionName;
        str += "," + timezone;//Asia/Shanghai
        str += "," + city;//当前城市
        str += "," + organization;//什么网络
        str += "," + isp;
        str += "," + lon + " " + lat;
        //if (str.length() > 99)
        //   return str.substring(0, 99);
        //else
        return str;
    }

    public String regionToJson() {
        JSONObject jsonObject = new JSONObject();
        try {
            jsonObject.put("localIP", localIP);
            jsonObject.put("internetIP", internetIP);
            jsonObject.put("country", country);
            jsonObject.put("regionName", regionName);
            jsonObject.put("timezone", timezone);
            jsonObject.put("city", city);
            jsonObject.put("organization", organization);
            jsonObject.put("isp", isp);
            jsonObject.put("lon", lon);
            jsonObject.put("lat", lat);
        } catch (JSONException e) {
            //e.printStackTrace();
        }
        return jsonObject.toString();
    }

    public String toJson() {
        //JSONObject jsonObject = new JSONObject();
        //jsonObject.put()
        try {
            Gson gson = new GsonBuilder().serializeNulls().disableHtmlEscaping().create();
            return gson.toJson(this);
        } catch (JsonSyntaxException e) {
            //e.printStackTrace();
            MMLog.e(TAG, e.getMessage());
        }
        return "null";
    }

}
