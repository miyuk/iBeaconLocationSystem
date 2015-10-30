package jp.ac.oit.elc.mail.ibeaconlocationsystem.classification;

import android.util.Log;

import jp.ac.oit.elc.mail.ibeaconlocationsystem.SampleList;
import weka.classifiers.functions.MultilayerPerceptron;
import weka.core.Instances;

/**
 * Created by yuuki on 10/20/15.
 */
public class LocationClassifier {

    private static final String TAG = LocationClassifier.class.getSimpleName();
    private LocationInstances mInstances;
    private MultilayerPerceptron mBackProp;

    public LocationClassifier(String wekahome) {
        weka.core.Environment.getSystemWide().addVariable("WEKA_HOME", wekahome);
        mInstances = new LocationInstances();
        mBackProp = new MultilayerPerceptron();
    }

//    public boolean load(String path) {
//        SampleList samples = SampleList.loadFromCsv(path);
//        if (samples == null) {
//            return false;
//        }
//        return mInstances.setDataSet(samples);
//    }


    public void build() {
        try {
            Log.d(TAG, "Start Classification");
            //mBackProp.setHiddenLayers("a");
//            mBackProp.setTrainingTime(1000);
//            mBackProp.setLearningRate(0.1);
            mBackProp.buildClassifier(mInstances);
            Log.d(TAG, "Stoop Classification");
            Log.d(TAG, mBackProp.toString());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public Instances getInstances() {
        return mInstances;
    }

    public MultilayerPerceptron getClassifier() {
        return mBackProp;
    }

}
