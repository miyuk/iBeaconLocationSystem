package jp.ac.oit.elc.mail.ibeaconlocationsystem.classification;

import android.graphics.Point;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import jp.ac.oit.elc.mail.ibeaconlocationsystem.BeaconList;
import jp.ac.oit.elc.mail.ibeaconlocationsystem.LocationDB;
import jp.ac.oit.elc.mail.ibeaconlocationsystem.bluetooth.BluetoothBeacon;

/**
 * Created by yuuki on 11/9/15.
 */
public class Triangulation {
    public static Point calc(BeaconList<BluetoothBeacon> beacons){
        Map<Double, Point> posMap = new HashMap<>();
        double sum_rss = 0.0;
        for (BluetoothBeacon beacon : beacons){
            Point pos = LocationDB.get(beacon.getMacAddress());
            if(pos != null){
                double rss = dbmToMillWatt(beacon.getRssi());
                posMap.put(rss, pos);
                sum_rss += rss;
            }
        }
        double x = 0.0, y = 0.0;
        for (Map.Entry<Double, Point> entry : posMap.entrySet()){
            x += entry.getKey() * entry.getValue().x / sum_rss;
            y += entry.getKey() * entry.getValue().y / sum_rss;
        }
        return new Point((int)x, (int)y);
    }

    private static double dbmToMillWatt(double dbm) {
        return Math.pow(10.0, dbm / 10.0);
    }
//    private Point triangle(BeaconList<BluetoothBeacon> beacons){
//        Map<BluetoothBeacon, Point> map = new HashMap<>();
//        for (BluetoothBeacon beacon : beacons){
//            Point loc = LocationDB.get(beacon.getMacAddress());
//            if (loc != null){
//                map.put(beacon, loc);
//            }
//        }
//        Log.d(TAG, "points: " + String.valueOf(map.size()));
//        if(map.size() < 3){
//            return null;
//        }
//        while(map.size() > 3){
//            BluetoothBeacon min = new BluetoothBeacon("", 0);
//            for (BluetoothBeacon beacon : map.keySet()){
//                if(beacon.getRssi() <= min.getRssi()){
//                    min = beacon;
//                }
//            }
//            map.remove(min);
//        }
//        double[] rssis = new double[3];
//        Point[] points = new Point[3];
//        int i = 0;
//        for (Map.Entry<BluetoothBeacon, Point> entry : map.entrySet()){
//            rssis[i] = (double)mClassifier.mapRssiValue(entry.getKey().getRssi());
//            points[i] = entry.getValue();
//            i++;
//        }
//        double[][] a = new double[2][2];
//        double[][] b = new double[2][1];
//        a[0][0] = points[1].x - points[0].x;
//        a[0][1] = points[1].y - points[0].y;
//        a[1][0] = points[2].x - points[0].x;
//        a[1][1] = points[2].y - points[0].y;
//        b[0][0] = Math.pow(points[1].x, 2) - Math.pow(points[0].x, 2) + Math.pow(points[1].y, 2) - Math.pow(points[0].y, 2) - Math.pow(rssis[1], 2) + Math.pow(rssis[0], 2);
//        b[1][0] = Math.pow(points[2].x, 2) - Math.pow(points[0].x, 2) + Math.pow(points[2].y, 2) - Math.pow(points[0].y, 2) - Math.pow(rssis[2], 2) + Math.pow(rssis[0], 2);
//        Matrix mA = new Matrix(a);
//        Matrix mB = new Matrix(b);
//        mA = mA.times(2);
//        mA = mA.inverse();
//        Matrix mRes = mA.times(mB);
//        int x = (int)mRes.get(0, 0);
//        int y = (int)mRes.get(0, 1);
//        return new Point(x, y);
//    }
}
