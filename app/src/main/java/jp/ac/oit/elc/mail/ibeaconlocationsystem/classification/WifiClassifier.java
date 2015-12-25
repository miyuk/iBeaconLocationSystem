package jp.ac.oit.elc.mail.ibeaconlocationsystem.classification;

import java.util.ArrayList;

import jp.ac.oit.elc.mail.ibeaconlocationsystem.Sample;
import jp.ac.oit.elc.mail.ibeaconlocationsystem.SampleList;
import weka.core.Attribute;
import weka.core.Instance;
import weka.core.Instances;

/**
 * Created by yuuki on 11/11/15.
 */
public class WifiClassifier extends LocationClassifier {
    private static final String TAG = WifiClassifier.class.getSimpleName();

    @Override
    public void buildClassifier(SampleList trainingData) throws Exception {
        initInstances(trainingData);
//        System.out.println(m_Instances);
        buildClassifier(m_Instances);
    }

    private void initInstances(SampleList trainingData) {
        ArrayList<Attribute> attrs = extractAttributes(trainingData, false, true);
        m_Instances = new Instances("WIFI", attrs, INSTANCES_CAPACITY);
        m_Instances.setClass(m_Instances.attribute("CLASS"));
        for (Sample sample : trainingData) {
            Instance instance = makeInstance(null, sample.getWifiBeaconList(), sample.getPosition());
            m_Instances.add(instance);
        }
    }
}
