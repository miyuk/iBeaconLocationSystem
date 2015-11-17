package jp.ac.oit.elc.mail.ibeaconlocationsystem.classification;

import android.graphics.Point;

import java.util.HashMap;
import java.util.Map;

import jp.ac.oit.elc.mail.ibeaconlocationsystem.BeaconList;
import jp.ac.oit.elc.mail.ibeaconlocationsystem.LocationDB;
import jp.ac.oit.elc.mail.ibeaconlocationsystem.bluetooth.BluetoothBeacon;

/**
 * Created by yuuki on 11/9/15.
 */
public class Triangulation {
    public static Point calc(BeaconList<BluetoothBeacon> beacons) {
        Map<Double, Point> posMap = new HashMap<>();
        double sum_rss = 0.0;
        for (BluetoothBeacon beacon : beacons) {
            Point pos = LocationDB.get(beacon.getMacAddress());
            if (pos != null) {
                double rss = rssiToDistance(beacon.getRssi());
                posMap.put(rss, pos);
                sum_rss += rss;
            }
        }
        double x = 0.0, y = 0.0;
        for (Map.Entry<Double, Point> entry : posMap.entrySet()) {
            x += entry.getKey() * entry.getValue().x / sum_rss;
            y += entry.getKey() * entry.getValue().y / sum_rss;
        }
        return new Point((int) x, (int) y);
    }

    private static double rssiToDistance(double rssi) {
        return Math.pow(10, -(rssi + 59.844) / 8.121);
    }
}
