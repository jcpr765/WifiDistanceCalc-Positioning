package com.example.juanki.wifidistancecalculator;

import java.util.Comparator;

/**
 * Created by JUANKI on 6/1/2016.
 */
public class WifiComparator implements Comparator<WifiConnection> {

    @Override
    public  int compare(WifiConnection a, WifiConnection b){
        double ad = Double.parseDouble(a.distance);
        double bd = Double.parseDouble(b.distance);

        if(ad > bd)
            return 1;
        else if(bd > ad)
            return -1;
        else
            return 0;
    }
}
