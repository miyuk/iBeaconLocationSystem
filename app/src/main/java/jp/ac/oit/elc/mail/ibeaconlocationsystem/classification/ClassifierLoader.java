package jp.ac.oit.elc.mail.ibeaconlocationsystem.classification;

import android.content.AsyncTaskLoader;
import android.content.Context;

import jp.ac.oit.elc.mail.ibeaconlocationsystem.SampleList;

/**
 * Created by yuuki on 11/4/15.
 */
public class ClassifierLoader extends AsyncTaskLoader<LocationClassifier[]> {
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
        LocationClassifier[] result = new LocationClassifier[2];
        result[0] = new BluetoothLocationClassifier();
        result[1] = new WifiLocationClassifier();
        try {
            result[0].buildClassifier(samples);
            result[1].buildClassifier(samples);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
        return result;
    }
}
