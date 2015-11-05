package jp.ac.oit.elc.mail.ibeaconlocationsystem.classification;

import android.content.AsyncTaskLoader;
import android.content.Context;

import weka.classifiers.Classifier;

/**
 * Created by yuuki on 11/4/15.
 */
public class ClassifierLoader extends AsyncTaskLoader<LocationClassifier> {
    public ClassifierLoader(Context context){
        super(context);
    }

    @Override
    public LocationClassifier loadInBackground() {
        return null;
    }
}
