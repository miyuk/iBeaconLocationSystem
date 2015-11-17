package jp.ac.oit.elc.mail.ibeaconlocationsystem.classification;

import android.content.AsyncTaskLoader;
import android.content.Context;
import android.util.Log;

import jp.ac.oit.elc.mail.ibeaconlocationsystem.SampleList;

/**
 * Created by yuuki on 11/4/15.
 */
public class ClassifierLoader extends AsyncTaskLoader<LocationClassifier[]> {
    private static final String TAG = ClassifierLoader.class.getSimpleName();
    private String mBtDatasetPath;
    private String mWifiDataSetPath;
    public ClassifierLoader(Context context, String btDatasetPath, String wifiDatasetPath){
        super(context);
        mBtDatasetPath = btDatasetPath;
        mWifiDataSetPath = wifiDatasetPath;
        forceLoad();
    }

    @Override
    public LocationClassifier[] loadInBackground() {
        SampleList samples = SampleList.loadFromCsv(mBtDatasetPath, mWifiDataSetPath);
        LocationClassifier[] result = new LocationClassifier[3];
        result[0] = new BtClassifier();
        result[1] = new WifiClassifier();
        result[2] = new BtAndWifiClassifier();
        try {
            Log.d(TAG, "Loading BT Classifier");
            result[0].buildClassifier(samples);
            Log.d(TAG, "Loading WIFI Classifier");
            result[1].buildClassifier(samples);
            Log.d(TAG, "Loading BT and WIFI Classifier");
            result[2].buildClassifier(samples);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
        return result;
    }
}
