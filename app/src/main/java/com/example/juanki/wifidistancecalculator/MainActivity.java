package com.example.juanki.wifidistancecalculator;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.BroadcastReceiver;

import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Color;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.os.Build;
import android.Manifest;
import android.content.pm.PackageManager;


import android.os.Environment;
import android.util.Log;
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

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
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
    final int PERMISSIONS_REQUEST_EXTERNAL_STORAGE = 2;
    ArrayList<WifiConnection> connectionList = new ArrayList<>();
    WifiConnectionAdapter adapter;
    HashMap<String, WifiConnection> connectionMap = new HashMap<>();
    boolean scanRunning = false;
    EditText position_input;
    final int TEST_CASES = 25;
    int testCase;
    SQLiteDatabase myDB;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        position_input = (EditText) findViewById(R.id.position_input);

        try {

            myDB = this.openOrCreateDatabase("WifiData", MODE_PRIVATE, null);

            myDB.execSQL("CREATE TABLE IF NOT EXISTS accesspoints (mac VARCHAR NOT NULL, ssid VARCHAR NOT NULL, PRIMARY KEY (mac))");

            myDB.execSQL("CREATE TABLE IF NOT EXISTS scans (rssi INT NOT NULL, distance DECIMAL(3,3) NOT NULL, position INT(3) NOT NULL," +
                    " macfk VARCHAR NOT NULL, testnum INT(2) NOT NULL, FOREIGN KEY (macfk) REFERENCES accesspoints(mac))");

         //   myDB.execSQL("DELETE FROM accesspoints");
            myDB.execSQL("DELETE FROM scans");


        }
        catch (Exception e){
            e.printStackTrace();
        }

        tv = (TextView) findViewById(R.id.textView);
        ListView lv = (ListView) findViewById(R.id.listView);
        adapter = new WifiConnectionAdapter(this, connectionList);

        lv.setAdapter(adapter);



    }

    private void getWifiScanList() {

        IntentFilter filter = new IntentFilter();
        filter.addAction(WifiManager.SCAN_RESULTS_AVAILABLE_ACTION);
        wifiManager = (WifiManager) getApplicationContext().getSystemService(Context.WIFI_SERVICE);


        registerReceiver(new BroadcastReceiver() {


            @SuppressLint("UseValueOf")
            @Override
            public void onReceive(Context context, Intent intent) {
                scanList = wifiManager.getScanResults();
                for (int i = 0; i < scanList.size(); i++) {

                    int position = Integer.parseInt(position_input.getText().toString());

                    //double exp = (27.55 - (20 * Math.log10(scanList.get(i).frequency)) + Math.abs(scanList.get(i).level)) / 20.0;

                    double exp = ((Math.abs(scanList.get(i).level) + 27.55)/20) - Math.log10(scanList.get(i).frequency);


                    Math.pow(10.0, exp);

                    double distance = Math.pow(10.0, exp); // for value in feet

                    DecimalFormat df = new DecimalFormat("###.##");


                    WifiConnection wifiObj = new WifiConnection(scanList.get(i).SSID, scanList.get(i).level, Double.parseDouble(df.format(distance)), scanList.get(i).BSSID);
                    //if (wifiObj.SSID == "Android" || wifiObj.SSID == "Android5") {
                        if (connectionMap.containsKey(wifiObj.MAC)) {

                            connectionMap.get(wifiObj.MAC).setRssi(wifiObj.rssi);
                            connectionMap.get(wifiObj.MAC).setDistance(wifiObj.distance);
                            myDB.execSQL("INSERT INTO scans VALUES( " + wifiObj.rssi + ", " + Double.parseDouble(df.format(distance)) + ", " + position + ", '" + wifiObj.MAC
                                    + "', " + testCase + ")");

                        } else {
                            connectionMap.put(wifiObj.MAC, wifiObj);
                            connectionList.add(wifiObj);
                            myDB.execSQL("INSERT OR IGNORE INTO accesspoints VALUES( '" + wifiObj.MAC + "', '" + wifiObj.SSID.replace("'", "") + "')");
                            myDB.execSQL("INSERT INTO scans VALUES( " + wifiObj.rssi + ", " + Double.parseDouble(df.format(distance)) + ", " + position + ", '" + wifiObj.MAC
                                    + "', " + testCase + ")");
                        }
                        Collections.sort(connectionList, new WifiComparator());
                    //}
                }
                tv.setText("Number Of Wifi Connections :" + " " + connectionMap.size());
                adapter.notifyDataSetChanged();

                    Log.i("# of scans - ", Integer.toString(testCase));

                if(testCase++ < TEST_CASES)
                    wifiManager.startScan();
                else {
                    scanRunning = false;
                    unregisterReceiver(this);
                    Toast.makeText(getBaseContext(), "Scan finished.", Toast.LENGTH_SHORT).show();
                }


            }

        }, filter);


    }

    public void scanBegin(View view){

        if(!position_input.getText().toString().isEmpty()) {
            int num = Integer.parseInt(position_input.getText().toString());
            if (num >= 1 && num <= 15) {

                if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && checkSelfPermission(Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED){
                    requestPermissions(new String[]{Manifest.permission.ACCESS_COARSE_LOCATION},
                            PERMISSIONS_REQUEST_CODE_ACCESS_COARSE_LOCATION);

                }
                else
                    getWifiScanList();

                if (!scanRunning) {
                    testCase = 1;
                    scanRunning = true;
                        wifiManager.startScan();
                }
                else {

                    Toast.makeText(this, "A scan is currently running. Please try again after the notification.", Toast.LENGTH_SHORT).show();
                }
            } else
                Toast.makeText(this, "The position must be between 1 and 15", Toast.LENGTH_SHORT).show();
        }
        else
            Toast.makeText(this, "A number between 1 and 15 must be entered", Toast.LENGTH_SHORT).show();
    }

    public void exportCSV(View view){
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},
                    PERMISSIONS_REQUEST_EXTERNAL_STORAGE);
        }
        else {
            saveCSV("accesspoints", "Accesspoints.csv");
            saveCSV("scans", "Scans.csv");
            Log.d("Status: ", "Databases exported");
            }
        }

    public void saveCSV(String table, String filename){

        try {
            Cursor c = myDB.rawQuery("SELECT * FROM " + table, null);
            File path = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DCIM);

            File dir = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DCIM), "Test Directory");
            dir.mkdirs();
            File saveFile = new File(dir, filename);

            //saveFile.createNewFile();

            FileWriter fw = new FileWriter(saveFile, false);


            BufferedWriter bw = new BufferedWriter(fw);
            int rowcount = c.getCount();
            int colcount = c.getColumnCount();
            if (rowcount > 0) {
                c.moveToFirst();

                for (int i = 0; i < colcount; i++) {
                    if (i != colcount - 1) {

                        bw.write(c.getColumnName(i) + ",");

                    } else {

                        bw.write(c.getColumnName(i));

                    }
                }
                bw.newLine();

                for (int i = 0; i < rowcount; i++) {
                    c.moveToPosition(i);

                    for (int j = 0; j < colcount; j++) {
                        if (j != colcount - 1)
                            bw.write(c.getString(j) + ",");
                        else
                            bw.write(c.getString(j));
                    }
                    bw.newLine();
                }
                //bw.flush();
                bw.close();
            }
        } catch (Exception ex) {
            if (myDB.isOpen()) {
                myDB.close();
                Log.d("ERROR - ", ex.getMessage().toString());
            }
        } finally {

        }
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