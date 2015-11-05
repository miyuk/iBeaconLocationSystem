package jp.ac.oit.elc.mail.ibeaconlocationsystem.classification;

import android.content.AsyncTaskLoader;
import android.content.Context;
import android.util.Log;

import jp.ac.oit.elc.mail.ibeaconlocationsystem.SampleList;

/**
 * Created by yuuki on 11/4/15.
 */
public class ClassifierLoader extends AsyncTaskLoader<LocationClassifier> {
    private String mBtTrainingPath;
    private String mWifiTrainingPath;
    public ClassifierLoader(Context context, String btTrainingPath, String wifiTrainingPath){
        super(context);
        mBtTrainingPath = btTrainingPath;
        mWifiTrainingPath = wifiTrainingPath;
        forceLoad();
    }


    @Override
    public LocationClassifier loadInBackground() {
        Log.d("test", "start background");
        SampleList samples = SampleList.loadFromCsv(mBtTrainingPath, mWifiTrainingPath);
        LocationClassifier classifier = new LocationClassifier("IntensityMap", samples);
        classifier.build();
        return classifier;
    }
}
