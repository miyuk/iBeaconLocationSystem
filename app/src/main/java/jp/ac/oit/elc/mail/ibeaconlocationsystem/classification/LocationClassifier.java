package jp.ac.oit.elc.mail.ibeaconlocationsystem.classification;

import android.os.Environment;
import android.util.Log;

import java.io.File;
import java.util.ArrayList;
import java.util.Dictionary;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import jp.ac.oit.elc.mail.ibeaconlocationsystem.Sample;
import jp.ac.oit.elc.mail.ibeaconlocationsystem.SampleList;
import jp.ac.oit.elc.mail.ibeaconlocationsystem.bluetooth.BluetoothBeacon;
import weka.classifiers.functions.MultilayerPerceptron;
import weka.core.Attribute;
import weka.core.DenseInstance;
import weka.core.FastVector;
import weka.core.Instance;
import weka.core.Instances;
import weka.core.WekaPackageManager;

/**
 * Created by yuuki on 10/20/15.
 */
public class LocationClassifier {

    private static final String TAG = LocationClassifier.class.getSimpleName();
    private LocationInstances mInstances;
    private MultilayerPerceptron mBackProp;

    public LocationClassifier() {
        mInstances = new LocationInstances();
        mBackProp = new MultilayerPerceptron();
    }

    public boolean load(String path) {
        SampleList samples = SampleList.load(path);
        if (samples == null) {
            return false;
        }
        return mInstances.load(samples);
    }


    public void build() {
        try {
            mBackProp.setHiddenLayers("1");
            mBackProp.setTrainingTime(100);
            mBackProp.setLearningRate(0.1);
            mBackProp.buildClassifier(mInstances);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public Instances getInstances() {
        return mInstances;
    }


}
