package jp.ac.oit.elc.mail.ibeaconlocationsystem.classification;

import android.graphics.Point;

import java.util.ArrayList;

import jp.ac.oit.elc.mail.ibeaconlocationsystem.BeaconList;
import jp.ac.oit.elc.mail.ibeaconlocationsystem.Sample;
import jp.ac.oit.elc.mail.ibeaconlocationsystem.SampleList;
import jp.ac.oit.elc.mail.ibeaconlocationsystem.wifi.WifiBeacon;
import weka.core.Attribute;
import weka.core.DenseInstance;
import weka.core.Instance;
import weka.core.Instances;
import weka.core.Utils;

/**
 * Created by yuuki on 11/11/15.
 */
public class WifiLocationClassifier extends LocationClassifier {
    private static final String TAG = WifiLocationClassifier.class.getSimpleName();

    @Override
    public void buildClassifier(SampleList trainingData) throws Exception {
        initInstances(trainingData);
        System.out.println(m_Instances);
        buildClassifier(m_Instances);
    }

    private void initInstances(SampleList trainingData) {
        ArrayList<Attribute> attrs = extractAttributes(trainingData);
        m_Instances = new Instances("Wifi", attrs, INSTANCES_CAPACITY);
        m_Instances.setClass(m_Instances.attribute("Class"));

        for (Sample sample : trainingData) {
            Instance instance = makeInstance(sample.getWifiBeaconList(), sample.getPosition());
            m_Instances.add(instance);
        }
    }

    private ArrayList<Attribute> extractAttributes(SampleList trainingData) {
        ArrayList<Attribute> result = new ArrayList<>();
        for (Sample sample : trainingData) {
            for (WifiBeacon beacon : sample.getWifiBeaconList()) {
                Attribute attr = new Attribute("Wifi:" + beacon.getMacAddress());
                if (!result.contains(attr)) {
                    result.add(attr);
                }
            }
        }
        ArrayList<String> positions = new ArrayList<>();
        for (Point pos : trainingData.getPositions()) {
            String cat = formatPosition(pos);
            if (!positions.contains(cat)) {
                positions.add(cat);
            }
        }
        Attribute classAttr = new Attribute("Class", positions);
        result.add(classAttr);
        return result;
    }

    public Instance makeInstance(BeaconList<WifiBeacon> beacons, Point position) {
        double[] values = new double[m_Instances.numAttributes()];
        for (int i = 0; i < values.length; i++) {
            values[i] = 0.0;
        }
        for (WifiBeacon beacon : beacons) {
            Attribute attr = m_Instances.attribute("Wifi:" + beacon.getMacAddress());
            if (attr != null) {
                values[attr.index()] = mapRssiValue(beacon.getRssi());
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

    public Point estimatePosition(BeaconList<WifiBeacon> beacons) throws Exception {
        Instance instance = makeInstance(beacons, null);
//        Log.d(TAG, "estimate Instance: " + instance.toString());
        return estimatePosition(instance);
    }
}
