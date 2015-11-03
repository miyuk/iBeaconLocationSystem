package jp.ac.oit.elc.mail.ibeaconlocationsystem.activity;

import android.graphics.Canvas;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.widget.TextView;

import jp.ac.oit.elc.mail.ibeaconlocationsystem.BeaconList;
import jp.ac.oit.elc.mail.ibeaconlocationsystem.R;
import jp.ac.oit.elc.mail.ibeaconlocationsystem.SampleList;
import jp.ac.oit.elc.mail.ibeaconlocationsystem.bluetooth.BluetoothBeacon;
import jp.ac.oit.elc.mail.ibeaconlocationsystem.bluetooth.BluetoothBeaconScanner;
import jp.ac.oit.elc.mail.ibeaconlocationsystem.classification.LocationClassifier;
import jp.ac.oit.elc.mail.ibeaconlocationsystem.view.IntensityMapView;
import jp.ac.oit.elc.mail.ibeaconlocationsystem.wifi.WifiBeacon;
import jp.ac.oit.elc.mail.ibeaconlocationsystem.wifi.WifiBeaconScanner;

import static jp.ac.oit.elc.mail.ibeaconlocationsystem.Environment.*;

public class LocationAcitivity extends AppCompatActivity {
    private static final String TAG = LocationAcitivity.class.getSimpleName();
    private IntensityMapView mIntensityMapView;
    private TextView mTextStatus;
    private BluetoothBeaconScanner mBtScanner;
    private WifiBeaconScanner mWifiScanner;
    private LocationClassifier mClassifier;
    private BeaconList<BluetoothBeacon> btBeacons;
    private BeaconList<WifiBeacon> wifiBeacons;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_location);
        initViews();
        btBeacons = new BeaconList<>();
        wifiBeacons = new BeaconList<>();
        mBtScanner = new BluetoothBeaconScanner(this);
        mWifiScanner = new WifiBeaconScanner(this);
        mBtScanner.setOnScanListener(mBtScanListener);
        mWifiScanner.setOnScanListener(mWifiScanListener);
        SampleList trainingData = SampleList.loadFromCsv(BT_TRAINING_CSV, WIFI_TRAINING_CSV);
        mClassifier = new LocationClassifier("IntensityMap", trainingData);
        mClassifier.build();

    }

    private void initViews() {
        mIntensityMapView = (IntensityMapView) findViewById(R.id.intensityMapView);
        mTextStatus = (TextView) findViewById(R.id.textStatus);
        mIntensityMapView.setImageResource(R.mipmap.floor_map);
        mIntensityMapView.setOnDrawListener(mDrawMapListener);
    }

    private IntensityMapView.OnDrawListener mDrawMapListener = new IntensityMapView.OnDrawListener() {
        @Override
        public void onDraw(Canvas canvas) {

        }
    };

    private BluetoothBeaconScanner.OnScanListener mBtScanListener = new BluetoothBeaconScanner.OnScanListener() {
        @Override
        public void onStartScan() {

        }

        @Override
        public void onScan(BluetoothBeacon beacon) {

        }
    };

    private WifiBeaconScanner.OnScanListener mWifiScanListener = new WifiBeaconScanner.OnScanListener() {
        @Override
        public void onStartScan() {

        }

        @Override
        public void onScan(BeaconList<WifiBeacon> beaconList) {

        }
    };
}
