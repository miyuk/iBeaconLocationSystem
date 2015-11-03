package jp.ac.oit.elc.mail.ibeaconlocationsystem.classification;

import android.graphics.Point;
import android.util.Log;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import jp.ac.oit.elc.mail.ibeaconlocationsystem.BeaconList;
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

import static jp.ac.oit.elc.mail.ibeaconlocationsystem.Environment.WEKA_HOME;

/**
 * Created by yuuki on 10/20/15.
 */
public class LocationClassifier {
    private static final int INSTANCES_CAPACITY = 1000;
    private static final double OUT_OF_RANGE_RSSI = -70;
    private static final double LOWER_RSSI = -90;
    private static final double UPPER_RSSI = -20;


    private static final String TAG = LocationClassifier.class.getSimpleName();
    private Classifier mClassifier;
    private Instances mInstances;
    private ArrayList<Attribute> mAttributes;
    private Map<String, Point> mPositionMap;

    public LocationClassifier(String name, SampleList training) {
        weka.core.Environment.getSystemWide().addVariable("WEKA_HOME", WEKA_HOME);
        init(name, training);
        mClassifier = new BayesNet();
    }

    private void init(String name, SampleList samples) {
        mAttributes = new ArrayList<>();
        mPositionMap = new HashMap<>();
        for (Sample sample : samples) {
            for (BluetoothBeacon beacon : sample.getBtBeaconList()) {
                Attribute attr = new Attribute(beacon.getMacAddress());
                if (!mAttributes.contains(attr)) {
                    mAttributes.add(attr);
                }
            }
        }
        for (Point pos : samples.getPositions()) {
            String cat = String.format("%d-%d", pos.x, pos.y);
            if (!mPositionMap.containsKey(cat)) {
                mPositionMap.put(cat, pos);
            }
        }
        Attribute classAttr = new Attribute("class", new ArrayList<>(mPositionMap.keySet()));
        mAttributes.add(classAttr);
        mInstances = new Instances(name, mAttributes, INSTANCES_CAPACITY);
        mInstances.setClass(classAttr);
        for (Sample sample : samples) {
            Instance instance = makeInstance(sample.getPosition(), sample.getBtBeaconList(), sample.getWifiBeaconList());
            mInstances.add(instance);
        }
    }

    //make test data if position == null
    public Instance makeInstance(Point position, BeaconList<BluetoothBeacon> btBeacons, BeaconList<WifiBeacon> wifiBeacons) {
        //if add test data, class attribute is missed
        double[] values = new double[mAttributes.size()];
        // initialize for missing beacons
        for (int i = 0; i < values.length; i++) {
            values[i] = -1.0;
        }
        for (BluetoothBeacon beacon : btBeacons) {
            Attribute attr = mInstances.attribute(beacon.getMacAddress());
            if (attr != null) {
                values[attr.index()] = convertRssiValue(beacon.getRssi());
            }
        }
        Attribute classAttr = mInstances.attribute("class");
        if (position != null && mPositionMap.containsValue(position)) {
            values[classAttr.index()] = classAttr.indexOfValue(String.format("%d-%d", position.x, position.y));
        } else {
            values[classAttr.index()] = Utils.missingValue();
        }
        Instance instance = new DenseInstance(1.0, values);
        return instance;
    }

    public void build() {
        try {
            Log.d(TAG, "Start Classification");
            mClassifier.buildClassifier(mInstances);
            Log.d(TAG, "Stop Classification");
            Log.d(TAG, mClassifier.toString());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public Map<Point, Double> recognize(Sample sample) {
        Instance instance = makeInstance(null, sample.getBtBeaconList(), sample.getWifiBeaconList());
        instance.setDataset(mInstances);
        double[] values;
        try {
            values = mClassifier.distributionForInstance(instance);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
        Map<Point, Double> result = new HashMap<>();
        for (Map.Entry<String, Point> entry : mPositionMap.entrySet()) {
            int index = mInstances.classAttribute().indexOfValue(entry.getKey());
            if (index < 0) {
                Log.e(TAG, String.format("missing class value %s", entry.getKey()));
                continue;
            }
            result.put(entry.getValue(), values[index]);
        }
        return result;
    }

    public Map<String, Point> getPositionMap() {
        return mPositionMap;
    }

    private static double convertRssiValue(double rssi) {
        if (rssi < OUT_OF_RANGE_RSSI) {
            return -1.0;
        }
        return (2 * rssi - (UPPER_RSSI + LOWER_RSSI)) / (UPPER_RSSI - LOWER_RSSI);
    }

}
