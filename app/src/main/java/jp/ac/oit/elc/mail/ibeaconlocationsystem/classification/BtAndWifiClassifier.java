package jp.ac.oit.elc.mail.ibeaconlocationsystem.classification;

import android.graphics.Point;
import android.util.Log;

import java.util.ArrayList;

import jp.ac.oit.elc.mail.ibeaconlocationsystem.LocationDB;
import jp.ac.oit.elc.mail.ibeaconlocationsystem.Sample;
import jp.ac.oit.elc.mail.ibeaconlocationsystem.SampleList;
import weka.core.Attribute;
import weka.core.Instance;
import weka.core.Instances;

/**
 * Created by yuuki on 11/17/15.
 */
public class BtAndWifiClassifier extends LocationClassifier {

    private static final String TAG = BtAndWifiClassifier.class.getSimpleName();

    public BtAndWifiClassifier(){
        super();
        mEnabledBt = true;
        mEnabledWifi = true;
    }
    public void buildClassifier(SampleList trainingData) throws Exception {
        initInstances(trainingData);
//        System.out.println(m_Instances);
        buildClassifier(m_Instances);
    }

    private void initInstances(SampleList trainingData) {
        ArrayList<Attribute> attrs = extractAttributes(trainingData);
        m_Instances = new Instances("BT_AND_WIFI", attrs, INSTANCES_CAPACITY);
        m_Instances.setClass(m_Instances.attribute("CLASS"));
        for (Sample sample : trainingData) {
            Instance instance = makeInstance(sample.getBtBeaconList(), sample.getWifiBeaconList(), sample.getPosition());
            m_Instances.add(instance);
        }
    }
}
