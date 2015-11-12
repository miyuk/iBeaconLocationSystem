package jp.ac.oit.elc.mail.ibeaconlocationsystem.classification;

import android.graphics.Point;

import jp.ac.oit.elc.mail.ibeaconlocationsystem.SampleList;
import weka.classifiers.bayes.NaiveBayes;
import weka.classifiers.functions.MultilayerPerceptron;
import weka.core.Instance;
import weka.core.Instances;

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

    protected static double mapRssiValue(double rssi) {
//        if (rssi < OUT_OF_RANGE_RSSI) {
//            return 0.0;
//        }
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
