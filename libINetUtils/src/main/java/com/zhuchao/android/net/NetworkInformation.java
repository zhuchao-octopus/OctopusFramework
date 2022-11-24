package com.zhuchao.android.net;

public class NetworkInformation {
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
        String str = "" + isAvailable;
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
        str += "," + countryCode;
        str += "," + region;
        str += "," + regionName;
        //str += "," + timezone;Asia/Shanghai
        str += "," + city;
        str += "," + organization;
        str += "," + isp;
        str += "," + lon + " " + lat;
        return str;
    }
}
