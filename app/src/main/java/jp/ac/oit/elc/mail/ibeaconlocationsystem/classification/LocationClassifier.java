package jp.ac.oit.elc.mail.ibeaconlocationsystem.classification;

import android.graphics.Point;
import android.util.Log;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import jp.ac.oit.elc.mail.ibeaconlocationsystem.BeaconList;
import jp.ac.oit.elc.mail.ibeaconlocationsystem.LocationDB;
import jp.ac.oit.elc.mail.ibeaconlocationsystem.Sample;
import jp.ac.oit.elc.mail.ibeaconlocationsystem.SampleList;
import jp.ac.oit.elc.mail.ibeaconlocationsystem.bluetooth.BluetoothBeacon;
import jp.ac.oit.elc.mail.ibeaconlocationsystem.wifi.WifiBeacon;
import weka.classifiers.Classifier;
import weka.classifiers.bayes.BayesNet;
import weka.core.Attribute;
import weka.core.DenseInstance;
import weka.core.Instance;
import weka.core.Instances;
import weka.core.Utils;
import weka.filters.Filter;
import weka.filters.supervised.attribute.Discretize;

import static jp.ac.oit.elc.mail.ibeaconlocationsystem.Environment.WEKA_HOME;

/**
 * Created by yuuki on 10/20/15.
 */
public class LocationClassifier {
    private static final int INSTANCES_CAPACITY = 1000;
    private static final double OUT_OF_RANGE_RSSI = -80;
    private static final double LOWER_RSSI = -100;
    private static final double UPPER_RSSI = -0;


    private static final String TAG = LocationClassifier.class.getSimpleName();
    private Classifier mClassifier;
    private Classifier mBtPreClassifier;
    private Classifier mWifiPreClassifier;
    private Instances mInstances;
    private Instances mBtInstances;
    private Instances mWifiInstances;
    private ArrayList<Attribute> mAttributes;
    private ArrayList<Attribute> mBtAttributes;
    private ArrayList<Attribute> mWifiAttributes;
    private Map<String, Point> mPositionMap;
    private Filter mBtFilter;
    private Filter mWifiFilter;

    public LocationClassifier(String name, SampleList training) {
        weka.core.Environment.getSystemWide().addVariable("WEKA_HOME", WEKA_HOME);
        init(name, training);
//        mClassifier = new BayesNet();
        mBtPreClassifier = new BayesNet();
        mWifiPreClassifier = new BayesNet();
        mBtFilter = new Discretize();
        mWifiFilter = new Discretize();
    }

    private void init(String name, SampleList samples) {
        mAttributes = new ArrayList<>();
        mBtAttributes = extractAttributes(samples, true, false);
        mWifiAttributes = extractAttributes(samples, false, true);
//        mPositionMap = new HashMap<>();
//        for (Point pos : samples.getPositions()) {
//            String cat = String.format("%d-%d", pos.x, pos.y);
//            if (!mPositionMap.containsKey(cat)) {
//                mPositionMap.put(cat, pos);
//            }
//        }
        mBtInstances = new Instances("BT", mBtAttributes, INSTANCES_CAPACITY);
        mWifiInstances = new Instances("Wifi", mWifiAttributes, INSTANCES_CAPACITY);
        for (Sample sample : samples) {
            Instance btInstance = makeInstance(mBtInstances, sample.getBtBeaconList(), sample.getWifiBeaconList(), sample.getPosition());
            Instance wifiInstance = makeInstance(mWifiInstances, sample.getBtBeaconList(), sample.getWifiBeaconList(), sample.getPosition());
            mBtInstances.add(btInstance);
            mWifiInstances.add(wifiInstance);
        }
        mBtInstances.setClass(mBtInstances.attribute("class"));
        mWifiInstances.setClass(mWifiInstances.attribute("class"));
        try {
            mBtInstances = Filter.useFilter(mBtInstances, mBtFilter);
            mWifiInstances = Filter.useFilter(mWifiInstances, mWifiFilter);
            mBtFilter.setInputFormat(mBtInstances);
            mWifiFilter.setInputFormat(mWifiInstances);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private ArrayList<Attribute> extractAttributes(SampleList samples, boolean enableBt, boolean enableWifi) {
        ArrayList<Attribute> result = new ArrayList<>();
        for (Sample sample : samples) {
            if (enableBt) {
                for (BluetoothBeacon beacon : sample.getBtBeaconList()) {
                    Attribute attr = new Attribute("BT:" + beacon.getMacAddress());
                    //test
                    if (LocationDB.get(beacon.getMacAddress()) == null) {
                        continue;
                    }
                    if (!result.contains(attr)) {
                        result.add(attr);
                    }
                }
            }
            if (enableWifi) {
                for (WifiBeacon beacon : sample.getWifiBeaconList()) {
                    Attribute attr = new Attribute("Wifi:" + beacon.getMacAddress());
                    if (!result.contains(attr)) {
                        result.add(attr);
                    }
                }
            }
        }
        ArrayList<String> positions = new ArrayList<>();
        for (Point pos : samples.getPositions()) {
            String cat = String.format("%d-%d", pos.x, pos.y);
            if (!positions.contains(cat)) {
                positions.add(cat);
            }
        }
        Attribute classAttr = new Attribute("class", positions);
        result.add(classAttr);
        return result;
    }

    //make test data if position == null
    private Instance makeInstance(Instances dataset, BeaconList<BluetoothBeacon> btBeacons, BeaconList<WifiBeacon> wifiBeacons, Point position) {
        //if add test data, class attribute is missed
        double[] values = new double[dataset.numAttributes()];
        // initialize for missing beacons
        for (int i = 0; i < values.length; i++) {
            values[i] = 0;
//            values[i] = -1.0;
        }
        if (btBeacons != null) {
            for (BluetoothBeacon beacon : btBeacons) {
                Attribute attr = dataset.attribute("BT:" + beacon.getMacAddress());
                if (attr != null) {
                    values[attr.index()] = convertRssiValue(beacon.getRssi());
                }
            }
        }
        if (wifiBeacons != null) {
            for (WifiBeacon beacon : wifiBeacons) {
                Attribute attr = dataset.attribute("Wifi:" + beacon.getMacAddress());
                if (attr != null) {
                    values[attr.index()] = convertRssiValue(beacon.getRssi());
                }
            }
        }
        Attribute classAttr = dataset.attribute("class");
        if (position == null) {
            values[classAttr.index()] = Utils.missingValue();
        } else {
            values[classAttr.index()] = classAttr.indexOfValue(String.format("%d-%d", position.x, position.y));
        }
        Instance instance = new DenseInstance(1.0, values);
        instance.setDataset(dataset);
        return instance;
    }

    private Instance makeInstance(Instances dataset, BeaconList<BluetoothBeacon> btBeacons, BeaconList<WifiBeacon> wifiBeacons) {
        return makeInstance(dataset, btBeacons, wifiBeacons, null);
    }

    public void build() {
        try {
            Log.d(TAG, "Start Classification");
            mBtPreClassifier.buildClassifier(mBtInstances);
            mWifiPreClassifier.buildClassifier(mWifiInstances);
            Log.d(TAG, "Stop Classification");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private Map<Point, Double> distribute(Classifier classifier, Instance instance) {
        double[] values;
        try {
            values = classifier.distributionForInstance(instance);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
        Map<Point, Double> result = new HashMap<>();
        for (int i = 0; i < values.length; i++) {
            String posStr = instance.classAttribute().value(i);
            Point pos = parsePosition(posStr);
            result.put(pos, values[i]);
        }
        return result;
    }

    public Map<Point, Double> recognize(BeaconList beacons, boolean bt) {
        if (bt) {
            Instance btInstance = makeInstance(mBtInstances, beacons, null);
            try {
                mBtFilter.input(btInstance);
                mBtFilter.batchFinished();
                btInstance = mBtFilter.output();
            } catch (Exception e) {
                e.printStackTrace();
            }
            return distribute(mBtPreClassifier, btInstance);
        } else {
            Instance wifiInstance = makeInstance(mWifiInstances, null, beacons);
            try {
                mWifiFilter.input(wifiInstance);
                mWifiFilter.batchFinished();
                wifiInstance = mWifiFilter.output();
            } catch (Exception e) {
                e.printStackTrace();
            }
            return distribute(mWifiPreClassifier, wifiInstance);
        }
    }

    public static Point predictPosition(Map<Point, Double> pValues) {
        double x = 0, y = 0;
        for (Map.Entry<Point, Double> entry : pValues.entrySet()) {
            x += entry.getValue() * entry.getKey().x;
            y += entry.getValue() * entry.getKey().y;
        }
        return new Point((int) Math.round(x), (int) Math.round(y));
    }

    public static double convertRssiValue(double rssi) {
        if (rssi < OUT_OF_RANGE_RSSI) {
//            return -1.0;
            return 0.0;
        }
//        double result = (2 * rssi - (UPPER_RSSI + LOWER_RSSI)) / (UPPER_RSSI - LOWER_RSSI);
        double result = (rssi - LOWER_RSSI) / (UPPER_RSSI - LOWER_RSSI);
        return result;
    }

    private static Point parsePosition(String str) {
        String[] split = str.split("-");
        int x = Integer.parseInt(split[0]);
        int y = Integer.parseInt(split[1]);
        return new Point(x, y);
    }
}
