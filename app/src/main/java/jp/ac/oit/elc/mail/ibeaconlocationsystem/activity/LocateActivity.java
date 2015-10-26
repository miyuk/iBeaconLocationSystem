package jp.ac.oit.elc.mail.ibeaconlocationsystem.activity;

import android.graphics.Canvas;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.widget.TextView;

import jp.ac.oit.elc.mail.ibeaconlocationsystem.BeaconList;
import jp.ac.oit.elc.mail.ibeaconlocationsystem.BeaconScanCallback;
import jp.ac.oit.elc.mail.ibeaconlocationsystem.BeaconScanner;
import jp.ac.oit.elc.mail.ibeaconlocationsystem.R;
import jp.ac.oit.elc.mail.ibeaconlocationsystem.bluetooth.BluetoothBeacon;
import jp.ac.oit.elc.mail.ibeaconlocationsystem.classification.LocationClassifier;
import jp.ac.oit.elc.mail.ibeaconlocationsystem.view.IntensityMapView;
import jp.ac.oit.elc.mail.ibeaconlocationsystem.view.OnDrawListener;
import jp.ac.oit.elc.mail.ibeaconlocationsystem.wifi.WifiBeacon;

import static jp.ac.oit.elc.mail.ibeaconlocationsystem.Environment.INTENSITY_MAP_PATH;
import static jp.ac.oit.elc.mail.ibeaconlocationsystem.Environment.WEKA_HOME;

public class LocateActivity extends AppCompatActivity {
    private static final String TAG = LocateActivity.class.getSimpleName();
    private IntensityMapView mIntensityMapView;
    private TextView mTextStatus;
    private LocationClassifier mClassifier;
    private BeaconScanner mBeaconScanner;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_locate);
        initViews();
        mClassifier = new LocationClassifier(WEKA_HOME);
        mClassifier.load(INTENSITY_MAP_PATH);
        mClassifier.build();
        mBeaconScanner = new BeaconScanner(this);
        mBeaconScanner.startScan(beaconScanCallback);
    }

    private void initViews() {
        mIntensityMapView = (IntensityMapView) findViewById(R.id.intensityMapView);
        mTextStatus = (TextView) findViewById(R.id.textStatus);
        mIntensityMapView.setImageResource(R.mipmap.floor_map);
        mIntensityMapView.setOnDrawListener(mapOnDrawListener);
    }
    private OnDrawListener mapOnDrawListener = new OnDrawListener() {
        @Override
        public void onDraw(Canvas canvas) {

        }
    };
    private BeaconScanCallback beaconScanCallback = new BeaconScanCallback() {
        @Override
        public void onStartScan() {
            mTextStatus.setText("Start Scan");
        }

        @Override
        public void onScanTimeout(BeaconList<BluetoothBeacon> btBeacons, BeaconList<WifiBeacon> wifiBeacons) {

        }

    };
}
