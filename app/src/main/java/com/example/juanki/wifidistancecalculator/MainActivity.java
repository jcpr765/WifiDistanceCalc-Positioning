package com.example.juanki.wifidistancecalculator;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.BroadcastReceiver;

import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;

import android.database.sqlite.SQLiteDatabase;
import android.graphics.Color;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.os.Build;
import android.Manifest;
import android.content.pm.PackageManager;


import android.view.CollapsibleActionView;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Timer;
import java.util.concurrent.Exchanger;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ThreadFactory;

public class MainActivity extends Activity {
    TextView tv;
    WifiManager wifiManager;
    StringBuilder sb;
    List<ScanResult> scanList;
    final int PERMISSIONS_REQUEST_CODE_ACCESS_COARSE_LOCATION = 1;
    ArrayList<WifiConnection> connectionList = new ArrayList<>();
    WifiConnectionAdapter adapter;
    HashMap<String, WifiConnection> connectionMap = new HashMap<>();
    final int REFRESH_RATE_IN_MILLIS = 500;
    long sysTime;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        try {

            SQLiteDatabase myDB = this.openOrCreateDatabase("Wifi Data", MODE_PRIVATE, null);

            myDB.execSQL("CREATE TABLE IF NOT EXISTS scans ()");
        }
        catch (Exception e){
            e.printStackTrace();
        }

        tv = (TextView) findViewById(R.id.textView);
        ListView lv = (ListView) findViewById(R.id.listView);
        adapter = new WifiConnectionAdapter(this, connectionList);

        lv.setAdapter(adapter);

        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && checkSelfPermission(Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED){
            requestPermissions(new String[]{Manifest.permission.ACCESS_COARSE_LOCATION},
                    PERMISSIONS_REQUEST_CODE_ACCESS_COARSE_LOCATION);

        }
        else
            getWifiScanList();



    }

    private void getWifiScanList() {

        IntentFilter filter = new IntentFilter();
        filter.addAction(WifiManager.SCAN_RESULTS_AVAILABLE_ACTION);
        final WifiManager wifiManager =
                (WifiManager) getApplicationContext().getSystemService(Context.WIFI_SERVICE);


        registerReceiver(new BroadcastReceiver() {


            @SuppressLint("UseValueOf")
            @Override
            public void onReceive(Context context, Intent intent) {
                scanList = wifiManager.getScanResults();
                for (int i = 0; i < scanList.size(); i++) {
                    long rtt = scanList.get(i).timestamp - sysTime;

                    double exp = (27.55 - (20 * Math.log10(scanList.get(i).frequency)) + Math.abs(scanList.get(i).level)) / 20.0;

                    Math.pow(10.0, exp);

                    double distance = (Math.pow(10.0, exp) * 100.0) / 100.0;
                    DecimalFormat df = new DecimalFormat("###.##");



                    WifiConnection wifiObj = new WifiConnection(scanList.get(i).SSID, scanList.get(i).level, df.format(distance), rtt);
                    if(connectionMap.containsKey(wifiObj.SSID)) {
                        connectionMap.get(wifiObj.SSID).setRssi(wifiObj.rssi);
                        connectionMap.get(wifiObj.SSID).setDistance(wifiObj.distance);
                        connectionMap.get(wifiObj.SSID).setRoundTripTime(wifiObj.roundTripTime);
                    }
                    else {
                        connectionMap.put(wifiObj.SSID, wifiObj);
                        connectionList.add(wifiObj);

                    }
                    Collections.sort(connectionList, new WifiComparator());
                }
                tv.setText("Number Of Wifi Connections :" + " " + connectionMap.size());
                adapter.notifyDataSetChanged();

            }

        }, filter);

            Runnable r = new Runnable() {
                @Override
                public void run() {
                    while(true){
                        synchronized (this){
                            try{
                                sysTime = System.currentTimeMillis()*1000;
                                wifiManager.startScan();
                                wait(REFRESH_RATE_IN_MILLIS);
                            }catch (Exception e){}
                        }
                    }
                }
            };

            Thread scanThread = new Thread(r);
            scanThread.start();



    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions,
                                           int[] grantResults) {
        if (requestCode == PERMISSIONS_REQUEST_CODE_ACCESS_COARSE_LOCATION
                && grantResults[0] == PackageManager.PERMISSION_GRANTED) {

            getWifiScanList();
        }
    }
}