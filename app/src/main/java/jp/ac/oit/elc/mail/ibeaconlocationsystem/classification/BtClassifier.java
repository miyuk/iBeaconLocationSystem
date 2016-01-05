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
 * Created by yuuki on 11/11/15.
 */
public class BtClassifier extends LocationClassifier {
    private static final String TAG = BtClassifier.class.getSimpleName();

    public BtClassifier(){
        super();
        mEnabledBt = true;
        mEnabledWifi = false;
    }
    @Override
    public void buildClassifier(SampleList trainingData) throws Exception {
        initInstances(trainingData);
//        System.out.println(m_Instances);
        buildClassifier(m_Instances);
    }

    private void initInstances(SampleList trainingData) {
        ArrayList<Attribute> attrs = extractAttributes(trainingData);
        m_Instances = new Instances("BT", attrs, INSTANCES_CAPACITY);
        m_Instances.setClass(m_Instances.attribute("CLASS"));
        for (Sample sample : trainingData) {
            Instance instance = makeInstance(sample);
            m_Instances.add(instance);
        }
    }
}
