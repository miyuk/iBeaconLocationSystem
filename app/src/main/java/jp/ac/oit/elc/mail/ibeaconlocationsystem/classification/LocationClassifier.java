package jp.ac.oit.elc.mail.ibeaconlocationsystem.classification;

import android.graphics.Point;

import java.util.ArrayList;

import jp.ac.oit.elc.mail.ibeaconlocationsystem.BeaconList;
import jp.ac.oit.elc.mail.ibeaconlocationsystem.LocationDB;
import jp.ac.oit.elc.mail.ibeaconlocationsystem.Sample;
import jp.ac.oit.elc.mail.ibeaconlocationsystem.SampleList;
import jp.ac.oit.elc.mail.ibeaconlocationsystem.bluetooth.BluetoothBeacon;
import jp.ac.oit.elc.mail.ibeaconlocationsystem.wifi.WifiBeacon;
import weka.classifiers.bayes.BayesNet;
import weka.classifiers.bayes.NaiveBayes;
import weka.classifiers.functions.MultilayerPerceptron;
import weka.core.Attribute;
import weka.core.DenseInstance;
import weka.core.Instance;
import weka.core.Instances;
import weka.core.Utils;

/**
 * Created by yuuki on 10/20/15.
 */
public abstract class LocationClassifier extends BayesNet {
    private static final String TAG = LocationClassifier.class.getSimpleName();

    protected static final int INSTANCES_CAPACITY = 1000;
    protected static final double OUT_OF_RANGE_RSSI = -80;
    protected static final double LOWER_RSSI = -100;
    protected static final double UPPER_RSSI = -0;

    protected Instances m_Instances;

    protected ArrayList<Attribute> extractAttributes(SampleList trainingData, boolean enableBt, boolean enableWifi) {
        ArrayList<Attribute> result = new ArrayList<>();
        ArrayList<String> positions = new ArrayList<>();
        for (Sample sample : trainingData) {
            if (enableBt) {
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
            if (enableWifi) {
                for (WifiBeacon beacon : sample.getWifiBeaconList()) {
                    Attribute attr = new Attribute("WIFI:" + beacon.getMacAddress());
                    if (!result.contains(attr)) {
                        result.add(attr);
                    }
                }
            }
//            result.add(new Attribute("X"));
//            result.add(new Attribute("Y"));
            String cat = formatPosition(sample.getPosition());
            if (!positions.contains(cat)) {
                positions.add(cat);
            }
        }
        Attribute classAttr = new Attribute("CLASS", positions);
        result.add(classAttr);
        return result;
    }

    public Point estimatePosition(BeaconList<BluetoothBeacon> btBeacons, BeaconList<WifiBeacon> wifiBeacons) throws Exception{
        Instance instance = makeInstance(btBeacons, wifiBeacons, null);
        return estimatePosition(instance);
    }

    public Point estimatePosition(Instance instance) throws Exception {
        double[] pValues = distributionForInstance(instance);
        double x = 0, y = 0;
        for (int i = 0; i < pValues.length; i++) {
            Point pos = parsePosition(instance.classAttribute().value(i));
            x += pValues[i] * pos.x;
            y += pValues[i] * pos.y;
        }
        return new Point((int) Math.round(x), (int) Math.round(y));
    }

    protected Instance makeInstance(BeaconList<BluetoothBeacon> btBeacons, BeaconList<WifiBeacon> wifiBeacons, Point position) {
        double[] values = new double[m_Instances.numAttributes()];
        for (int i = 0; i < values.length; i++) {
            values[i] = 0.0;
        }
        if(btBeacons != null) {
            for (BluetoothBeacon beacon : btBeacons) {
                Attribute attr = m_Instances.attribute("BT:" + beacon.getMacAddress());
                if (attr != null) {
                    values[attr.index()] = mapRssiValue(beacon.getRssi());
                }
            }
        }
        if(wifiBeacons != null) {
            for (WifiBeacon beacon : wifiBeacons) {
                Attribute attr = m_Instances.attribute("WIFI:" + beacon.getMacAddress());
                if (attr != null) {
                    values[attr.index()] = mapRssiValue(beacon.getRssi());
                }
            }
        }
//        Point pos = Triangulation.calc(btBeacons);
//        values[m_Instances.attribute("X").index()] = pos.x / 1245.0;
//        values[m_Instances.attribute("Y").index()] = pos.y / 1127.0;
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

}
