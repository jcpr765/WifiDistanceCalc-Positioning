package com.example.juanki.wifidistancecalculator;

import java.util.Comparator;

/**
 * Created by JUANKI on 5/26/2016.
 */
public class WifiConnection {

    public String SSID;
    public int rssi;
    public double distance;
    public String MAC;

    public double getDistance() {
        return distance;
    }

    public void setDistance(double distance){
        this.distance = distance;
    }


    public WifiConnection(String SSID, int rssi, double distance, String MAC){
        this.SSID = SSID;
        this.rssi = rssi;
        this.distance = distance;
        this.MAC = MAC;

    }

    public String getMAC() {
        return MAC;
    }

    public void setMAC(String MAC) {
        this.MAC = MAC;
    }

    public int getRssi() {
        return rssi;
    }

    public void setRssi(int rssi) {
        this.rssi = rssi;
    }

    public String getSSID() {
        return SSID;
    }

    public void setSSID(String SSID) {
        this.SSID = SSID;
    }
}

