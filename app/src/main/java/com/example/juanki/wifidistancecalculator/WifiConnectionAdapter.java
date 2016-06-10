package com.example.juanki.wifidistancecalculator;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import org.w3c.dom.Text;

import java.util.ArrayList;

/**
 * Created by JUANKI on 5/26/2016.
 */
public class WifiConnectionAdapter extends ArrayAdapter<WifiConnection>{

    public WifiConnectionAdapter(Context context, ArrayList<WifiConnection> connections){
        super(context, 0, connections);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent){
        WifiConnection connection = getItem(position);

        if(convertView == null)
            convertView = LayoutInflater.from(getContext()).inflate(R.layout.connection_item, parent, false);

        TextView Wifi_SSID = (TextView) convertView.findViewById(R.id.Wifi_SSID);

        TextView Wifi_RSSI = (TextView) convertView.findViewById(R.id.Wifi_RSSI);

        TextView Wifi_Distance = (TextView) convertView.findViewById(R.id.Wifi_Distance);

        TextView Wifi_MAC = (TextView) convertView.findViewById(R.id.Wifi_MAC);

        Wifi_SSID.setText("SSID: " + connection.SSID);

        Wifi_RSSI.setText("RSSI: " + String.valueOf(connection.rssi));

        Wifi_Distance.setText("Distance (FSPL): " + String.valueOf(connection.distance));

        Wifi_MAC.setText("MAC: " + String.valueOf(connection.MAC));

        return convertView;
    }
}
