package com.example.juanki.wifidistancecalculator;

import java.util.Comparator;

/**
 * Created by JUANKI on 5/26/2016.
 */
public class WifiConnection {

    public String SSID;
    public int rssi;
    public String distance;
    public long roundTripTime;

    public String getDistance() {
        return distance;
    }

    public void setDistance(String distance) {
        this.distance = distance;
    }

    public long getRoundTripTime() {
        return roundTripTime;
    }

    public void setRoundTripTime(long roundTripTime) {
        this.roundTripTime = roundTripTime;
    }

    public WifiConnection(String SSID, int rssi, String distance, long roundTripTime){
        this.SSID = SSID;
        this.rssi = rssi;
        this.distance = distance;
        this.roundTripTime = roundTripTime;

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

