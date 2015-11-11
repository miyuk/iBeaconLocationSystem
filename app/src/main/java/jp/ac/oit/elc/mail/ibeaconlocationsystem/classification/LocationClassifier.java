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
import weka.classifiers.bayes.NaiveBayes;
import weka.core.Attribute;
import weka.core.DenseInstance;
import weka.core.Instance;
import weka.core.Instances;
import weka.core.Utils;

import static jp.ac.oit.elc.mail.ibeaconlocationsystem.Environment.WEKA_HOME;

/**
 * Created by yuuki on 10/20/15.
 */
public abstract class LocationClassifier extends NaiveBayes{
    protected static final int INSTANCES_CAPACITY = 1000;
    protected static final double OUT_OF_RANGE_RSSI = -80;
    protected static final double LOWER_RSSI = -100;
    protected static final double UPPER_RSSI = -0;

    private static final String TAG = LocationClassifier.class.getSimpleName();

    public Point estimatePosition(Instance instance) throws Exception{
        double[] pValues = distributionForInstance(instance);
        double x = 0, y = 0;
        for (int i = 0; i < pValues.length; i++){
            Point pos = parsePosition(instance.classAttribute().value(i));
            x += pValues[i] * pos.x;
            y += pValues[i] * pos.y;
        }
        return new Point((int)Math.round(x), (int)Math.round(y));
    }
    protected static double mapRssiValue(double rssi) {
        if (rssi < OUT_OF_RANGE_RSSI) {
            return 0.0;
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
