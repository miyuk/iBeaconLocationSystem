package jp.ac.oit.elc.mail.ibeaconlocationsystem.classification;

import android.content.AsyncTaskLoader;
import android.content.Context;
import android.util.Log;

import jp.ac.oit.elc.mail.ibeaconlocationsystem.SampleList;
import weka.classifiers.Classifier;

/**
 * Created by yuuki on 11/4/15.
 */
public class ClassifierLoader extends AsyncTaskLoader<Classifier[]> {
    private static final String TAG = ClassifierLoader.class.getSimpleName();
    private String mBtDatasetPath;
    private String mWifiDatasetPath;
    public ClassifierLoader(Context context, String btDatasetPath, String wifiDatasetPath){
        super(context);
        mBtDatasetPath = btDatasetPath;
        mWifiDatasetPath = wifiDatasetPath;
        forceLoad();
    }

    @Override
    public Classifier[] loadInBackground() {
        SampleList samples = SampleList.loadFromCsv(mBtDatasetPath, mWifiDatasetPath);
        Classifier[] result = new Classifier[4];
        result[0] = new BtClassifier();
        result[1] = new WifiClassifier();
        result[2] = new BtAndWifiClassifier();
        result[3] = new FusionClassifier();
        try {
            Log.d(TAG, "Loading BT Classifier");
            ((BtClassifier)result[0]).buildClassifier(samples);
//            System.out.println(result[0].m_Instances);
            Log.d(TAG, "Loading WIFI Classifier");
            ((WifiClassifier)result[1]).buildClassifier(samples);
//            System.out.println(result[1].m_Instances);
            Log.d(TAG, "Loading BT and WIFI Classifier");
            ((BtAndWifiClassifier)result[2]).buildClassifier(samples);
//            System.out.println(result[2].m_Instances);
            ((FusionClassifier)result[3]).buildClassifier(samples, (BtClassifier)result[0], (WifiClassifier)result[1]);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
        return result;
    }
}
