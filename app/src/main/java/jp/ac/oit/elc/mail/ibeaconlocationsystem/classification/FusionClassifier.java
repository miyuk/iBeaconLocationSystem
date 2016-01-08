package jp.ac.oit.elc.mail.ibeaconlocationsystem.classification;

import android.graphics.Point;

import org.w3c.dom.Attr;

import java.util.ArrayList;
import java.util.List;

import jp.ac.oit.elc.mail.ibeaconlocationsystem.BeaconList;
import jp.ac.oit.elc.mail.ibeaconlocationsystem.LocationDB;
import jp.ac.oit.elc.mail.ibeaconlocationsystem.Sample;
import jp.ac.oit.elc.mail.ibeaconlocationsystem.SampleList;
import jp.ac.oit.elc.mail.ibeaconlocationsystem.bluetooth.BluetoothBeacon;
import jp.ac.oit.elc.mail.ibeaconlocationsystem.wifi.WifiBeacon;
import weka.classifiers.functions.MultilayerPerceptron;
import weka.core.Attribute;
import weka.core.DenseInstance;
import weka.core.Instance;
import weka.core.Instances;
import weka.core.Utils;

/**
 * Created by yuuki on 11/12/15.
 */
public class FusionClassifier extends MultilayerPerceptron {
    private static final String TAG = FusionClassifier.class.getSimpleName();
    private static final int INSTANCES_CAPACITY = 1000;
    public Instances m_Instances;
    private BtClassifier mBtClassifier;
    private WifiClassifier mWifiClassifier;

    public void buildClassifier(SampleList trainingData, BtClassifier btClassifier, WifiClassifier wifiClassifier) throws Exception {
        mBtClassifier = btClassifier;
        mWifiClassifier = wifiClassifier;
        initInstance(trainingData);
        buildClassifier(m_Instances);
    }

    private void initInstance(SampleList trainingData) {
        ArrayList<Attribute> attrs = extractAttributes(trainingData);
        m_Instances = new Instances("FUSION", attrs, INSTANCES_CAPACITY);
        m_Instances.setClass(m_Instances.attribute("CLASS"));
        for (Sample sample : trainingData) {
            Instance instance = makeInstance(sample);
            m_Instances.add(instance);
        }
    }

    private ArrayList<Attribute> extractAttributes(SampleList trainingData) {
        ArrayList<Attribute> result = new ArrayList<>();
        ArrayList<String> positions = new ArrayList<>();

        result.add(new Attribute("BT_X"));
        result.add(new Attribute("BT_Y"));
        result.add(new Attribute("WIFI_X"));
        result.add(new Attribute("WIFI_Y"));

        for (Sample sample : trainingData) {
            String cat = formatPosition(sample.getPosition());
            if (!positions.contains(cat)) {
                positions.add(cat);
            }
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

    protected Instance makeInstance(Sample sample) {
        return makeInstance(sample.getBtBeaconList(), sample.getWifiBeaconList(), sample.getPosition());
    }

    protected Instance makeInstance(BeaconList<BluetoothBeacon> btBeacons, BeaconList<WifiBeacon> wifiBeacons, Point position) {
        double[] values = new double[m_Instances.numAttributes()];
        Point btPos = null;
        Point wifiPos = null;
        try {
            btPos = mBtClassifier.calcExpectedPosition(btBeacons, wifiBeacons);
            wifiPos = mWifiClassifier.calcExpectedPosition(btBeacons, wifiBeacons);
        } catch (Exception e) {
            e.printStackTrace();
        }
        values[m_Instances.attribute("BT_X").index()] = btPos.x;
        values[m_Instances.attribute("BT_Y").index()] = btPos.y;
        values[m_Instances.attribute("WIFI_X").index()] = wifiPos.x;
        values[m_Instances.attribute("WIFI_Y").index()] = wifiPos.y;
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
