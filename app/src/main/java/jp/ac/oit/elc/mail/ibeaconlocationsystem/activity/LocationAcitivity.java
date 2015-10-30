package jp.ac.oit.elc.mail.ibeaconlocationsystem.activity;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.widget.TextView;

import jp.ac.oit.elc.mail.ibeaconlocationsystem.R;
import jp.ac.oit.elc.mail.ibeaconlocationsystem.classification.LocationClassifier;
import jp.ac.oit.elc.mail.ibeaconlocationsystem.view.IntensityMapView;

import static jp.ac.oit.elc.mail.ibeaconlocationsystem.Environment.WEKA_HOME;

public class LocationAcitivity extends AppCompatActivity {
    private static final String TAG = LocationAcitivity.class.getSimpleName();
    private IntensityMapView mIntensityMapView;
    private TextView mTextStatus;
    private LocationClassifier mClassifier;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_location);
        initViews();
        mClassifier = new LocationClassifier(WEKA_HOME);
//        mClassifier.load(BT_TRAINING_CSV);
        //SampleList list = SampleList.loadFromCsv(Environment.BT_TRAINING_CSV);
        //mIntensityMapView.setSampleList(list);
        //mClassifier.build();
    }

    private void initViews() {
        mIntensityMapView = (IntensityMapView) findViewById(R.id.intensityMapView);
        mTextStatus = (TextView) findViewById(R.id.textStatus);
        mIntensityMapView.setImageResource(R.mipmap.floor_map);
    }
}
