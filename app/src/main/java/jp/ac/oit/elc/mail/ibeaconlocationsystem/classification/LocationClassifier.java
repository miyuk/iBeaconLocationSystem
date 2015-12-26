package jp.ac.oit.elc.mail.ibeaconlocationsystem.classification;

import android.graphics.Point;

import java.util.ArrayList;

import jp.ac.oit.elc.mail.ibeaconlocationsystem.BeaconList;
import jp.ac.oit.elc.mail.ibeaconlocationsystem.LocationDB;
import jp.ac.oit.elc.mail.ibeaconlocationsystem.Sample;
import jp.ac.oit.elc.mail.ibeaconlocationsystem.SampleList;
import jp.ac.oit.elc.mail.ibeaconlocationsystem.bluetooth.BluetoothBeacon;
import jp.ac.oit.elc.mail.ibeaconlocationsystem.wifi.WifiBeacon;
import weka.classifiers.bayes.NaiveBayes;
import weka.core.Attribute;
import weka.core.DenseInstance;
import weka.core.Instance;
import weka.core.Instances;
import weka.core.Utils;

/**
 * Created by yuuki on 10/20/15.
 */
public abstract class LocationClassifier extends NaiveBayes {
    private static final String TAG = LocationClassifier.class.getSimpleName();

    protected static final int INSTANCES_CAPACITY = 1000;
    protected static final double OUT_OF_RANGE_RSSI = -80;
    protected static final double LOWER_RSSI = -100;
    protected static final double UPPER_RSSI = -0;

    protected Instances m_Instances;
    protected boolean mEnabledBt = false;
    protected boolean mEnabledWifi = false;
    protected ArrayList<Attribute> extractAttributes(SampleList trainingData) {
        ArrayList<Attribute> result = new ArrayList<>();
        ArrayList<String> positions = new ArrayList<>();
        for (Sample sample : trainingData) {
            if (mEnabledBt) {
                for (BluetoothBeacon beacon : sample.getBtBeaconList()) {
                    if (LocationDB.get(beacon.getMacAddress()) == null) {
                        continue;
                    }
                    Attribute attr = new Attribute("BT:" + beacon.getMacAddress());
                    if (!result.contains(attr)) {
                        result.add(attr);
                    }
                }

            }
            if (mEnabledWifi) {
                for (WifiBeacon beacon : sample.getWifiBeaconList()) {
                    Attribute attr = new Attribute("WIFI:" + beacon.getMacAddress());
                    if (!result.contains(attr)) {
                        result.add(attr);
                    }
                }
            }

            String cat = formatPosition(sample.getPosition());
            if (!positions.contains(cat)) {
                positions.add(cat);
            }
        }
        //ROOM IDs
        if (mEnabledBt) {
            result.add(new Attribute("ROOM", LocationDB.getRoomIds()));
        }
        Attribute classAttr = new Attribute("CLASS", positions);
        result.add(classAttr);
        return result;
    }

    public Point calcExpectedPosition(BeaconList<BluetoothBeacon> btBeacons, BeaconList<WifiBeacon> wifiBeacons) throws Exception {
        Instance instance = makeInstance(btBeacons, wifiBeacons, null);
        return calcExpectedPosition(instance);
    }

    public Point calcExpectedPosition(Instance instance) throws Exception {
        double[] pValues = distributionForInstance(instance);
        double x = 0, y = 0;
        for (int i = 0; i < pValues.length; i++) {
            Point pos = parsePosition(instance.classAttribute().value(i));
            x += pValues[i] * pos.x;
            y += pValues[i] * pos.y;
        }
        return new Point((int) Math.round(x), (int) Math.round(y));
    }

    public Point classifyPosition(BeaconList<BluetoothBeacon> btBeacons, BeaconList<WifiBeacon> wifiBeacons) throws Exception {
        Instance instance = makeInstance(btBeacons, wifiBeacons, null);
        return classifyPosition(instance);
    }

    public Point classifyPosition(Instance instance) throws Exception {
        int valIndex = (int) classifyInstance(instance);
        return parsePosition(m_Instances.classAttribute().value(valIndex));
    }

    protected Instance makeInstance(Sample sample){
        return makeInstance(sample.getBtBeaconList(), sample.getWifiBeaconList(), sample.getPosition());
    }
    protected Instance makeInstance(BeaconList<BluetoothBeacon> btBeacons, BeaconList<WifiBeacon> wifiBeacons, Point position) {
        double[] values = new double[m_Instances.numAttributes()];
        for (int i = 0; i < values.length; i++) {
            values[i] = 0.0;
        }
        if (mEnabledBt) {
            BluetoothBeacon nearestBeacon = null;
            int maxRssi = -100;
            for (BluetoothBeacon beacon : btBeacons) {
                Attribute attr = m_Instances.attribute("BT:" + beacon.getMacAddress());
                if (attr != null) {
                    if (beacon.getPosition() != null && beacon.getRssi() > maxRssi) {
                        nearestBeacon = beacon;
                        maxRssi = beacon.getRssi();
                    }
                    values[attr.index()] = mapRssiValue(beacon.getRssi());
                }
            }
            String roomId;
            if(nearestBeacon != null) {
                roomId = LocationDB.getRoomId(nearestBeacon.getPosition());
            }else{
                roomId = "UNKNOWN";
            }
            Attribute roomAttr = m_Instances.attribute("ROOM");
            values[roomAttr.index()] = roomAttr.indexOfValue(roomId);
        }
        if (mEnabledWifi) {
            for (WifiBeacon beacon : wifiBeacons) {
                Attribute attr = m_Instances.attribute("WIFI:" + beacon.getMacAddress());
                if (attr != null) {
                    values[attr.index()] = mapRssiValue(beacon.getRssi());
                }
            }
        }
        Attribute clsAttr = m_Instances.classAttribute();
        if (position == null) {
            values[clsAttr.index()] = Utils.missingValue();
        } else {
            values[clsAttr.index()] = clsAttr.indexOfValue(formatPosition(position));
        }
        Instance result = new DenseInstance(1.0, values);
        result.setDataset(m_Instances);
        return result;
    }

    protected static double mapRssiValue(double rssi) {
        if (rssi < OUT_OF_RANGE_RSSI) {
            return 0.01;
        }
        return (rssi - LOWER_RSSI) / (UPPER_RSSI - LOWER_RSSI);
    }


    public abstract void buildClassifier(SampleList trainingData) throws Exception;

    protected static Point parsePosition(String str) {
        String[] split = str.split("-");
        int x = Integer.parseInt(split[0]);
        int y = Integer.parseInt(split[1]);
        return new Point(x, y);
    }

    protected static String formatPosition(Point position) {
        return String.format("%d-%d", position.x, position.y);
    }

    protected static double calcDistance(double x1, double y1, double x2, double y2) {
        return Math.sqrt(Math.pow(x1 - x2, 2) + Math.pow(y1 - y2, 2));
    }
}
