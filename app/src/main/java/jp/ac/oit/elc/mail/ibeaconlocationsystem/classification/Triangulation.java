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
        BluetoothBeacon maxBeacon = null;
        double sum_rss = 0.0;
        for (BluetoothBeacon beacon : beacons) {
            if(LocationDB.get(beacon.getMacAddress()) == null){
                continue;
            }
            if (maxBeacon == null || beacon.getRssi() > maxBeacon.getRssi()) {

                maxBeacon = beacon;
            }
            if (beacon.getRssi() < -75) {
                continue;
            }
            Point pos = LocationDB.get(beacon.getMacAddress());
            if (pos != null) {
                double rss = dbmToMillWatt(beacon.getRssi());
                posMap.put(rss, pos);
                sum_rss += rss;
            }
        }
        if (posMap.size() == 0) {
            return LocationDB.get(maxBeacon.getMacAddress());
        }
        double x = 0.0, y = 0.0;
        for (Map.Entry<Double, Point> entry : posMap.entrySet()) {
            x += entry.getKey() * entry.getValue().x / sum_rss;
            y += entry.getKey() * entry.getValue().y / sum_rss;
        }
        Point result = new Point((int) x, (int) y);
        return result;
    }

    private static double dbmToMillWatt(double dbm) {
        return Math.pow(10, dbm / 10);
    }

    private static double millWattToDbm(double millWatt) {
        return 10.0 * Math.log10(millWatt);
    }
}
