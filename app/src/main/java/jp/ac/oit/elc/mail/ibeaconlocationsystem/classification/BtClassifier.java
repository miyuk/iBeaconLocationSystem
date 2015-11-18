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

    @Override
    public void buildClassifier(SampleList trainingData) throws Exception {
        initInstances(trainingData);
//        System.out.println(m_Instances);
        buildClassifier(m_Instances);
    }

    private void initInstances(SampleList trainingData) {
        ArrayList<Attribute> attrs = extractAttributes(trainingData, true, false);
        m_Instances = new Instances("BT", attrs, INSTANCES_CAPACITY);
        m_Instances.setClass(m_Instances.attribute("CLASS"));
        int err = 0;
        for (Sample sample : trainingData) {
            Point triPos = Triangulation.calc(sample.getBtBeaconList());
            if (LocationDB.mapRoom(triPos.x, triPos.y) != LocationDB.mapRoom(sample.getPosition().x, sample.getPosition().y)){
                Log.d(TAG, String.format("False: %s <=> %s", sample.getPosition(), triPos));
                err++;
            }
            Instance instance = makeInstance(sample.getBtBeaconList(), sample.getWifiBeaconList(), sample.getPosition());
            m_Instances.add(instance);
        }
        Log.d(TAG, String.format("error rate is %.1f%%", 100.0 * err / trainingData.size()));
    }
}
