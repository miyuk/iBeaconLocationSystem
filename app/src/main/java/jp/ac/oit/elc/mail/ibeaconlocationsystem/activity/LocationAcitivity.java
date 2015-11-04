package jp.ac.oit.elc.mail.ibeaconlocationsystem.activity;

import android.graphics.Canvas;
import android.graphics.Point;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.widget.TextView;

import java.util.Date;
import java.util.Map;

import jp.ac.oit.elc.mail.ibeaconlocationsystem.BeaconList;
import jp.ac.oit.elc.mail.ibeaconlocationsystem.R;
import jp.ac.oit.elc.mail.ibeaconlocationsystem.SampleList;
import jp.ac.oit.elc.mail.ibeaconlocationsystem.bluetooth.BluetoothBeacon;
import jp.ac.oit.elc.mail.ibeaconlocationsystem.bluetooth.BluetoothBeaconScanner;
import jp.ac.oit.elc.mail.ibeaconlocationsystem.classification.LocationClassifier;
import jp.ac.oit.elc.mail.ibeaconlocationsystem.view.IntensityMapView;
import jp.ac.oit.elc.mail.ibeaconlocationsystem.wifi.WifiBeacon;
import jp.ac.oit.elc.mail.ibeaconlocationsystem.wifi.WifiBeaconScanner;

import static jp.ac.oit.elc.mail.ibeaconlocationsystem.Environment.BT_TRAINING_CSV;
import static jp.ac.oit.elc.mail.ibeaconlocationsystem.Environment.WIFI_TRAINING_CSV;

public class LocationAcitivity extends AppCompatActivity {
    private static final String TAG = LocationAcitivity.class.getSimpleName();
    private IntensityMapView mIntensityMapView;
    private TextView mTextStatus;
    private BluetoothBeaconScanner mBtScanner;
    private WifiBeaconScanner mWifiScanner;
    private LocationClassifier mClassifier;
    private BeaconList<BluetoothBeacon> mBtBeacons;
    private BeaconList<WifiBeacon> mWifiBeacons;
    private Date mBtUpdatedTime;
    private Date mWifiUpdatedTime;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_location);
        initViews();
        mBtBeacons = new BeaconList<>();
        mWifiBeacons = new BeaconList<>();
        mBtScanner = new BluetoothBeaconScanner(this);
        mWifiScanner = new WifiBeaconScanner(this);
        mBtScanner.setOnScanListener(mBtScanListener);
        mWifiScanner.setOnScanListener(mWifiScanListener);
        SampleList trainingData = SampleList.loadFromCsv(BT_TRAINING_CSV, WIFI_TRAINING_CSV);
        mClassifier = new LocationClassifier("IntensityMap", trainingData);
        mClassifier.build();
        startScan();
    }

    private void initViews() {
        mIntensityMapView = (IntensityMapView) findViewById(R.id.intensityMapView);
        mTextStatus = (TextView) findViewById(R.id.textStatus);
        mIntensityMapView.setImageResource(R.mipmap.floor_map);
        mIntensityMapView.setOnDrawListener(mDrawMapListener);
        mIntensityMapView.setEnabledPin(true);
    }

    private void startScan() {
        AsyncTask<Void, Point, Void> asyncTask = new AsyncTask<Void, Point, Void>() {
            @Override
            protected void onProgressUpdate(Point... values) {
                super.onProgressUpdate(values);
                mIntensityMapView.setPinImageCoordPosition(values[0].x, values[0].y);
            }

            @Override
            protected void onPreExecute() {
                super.onPreExecute();
                mBtScanner.startScan();
                mWifiScanner.startScan();
            }

            @Override
            protected Void doInBackground(Void... params) {
                while (true) {
                    try {
                        Thread.sleep(500);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                        break;
                    }
                    Date limit = new Date();
                    limit.setTime(limit.getTime() - 1000);
                    Map<Point, Double> recognized = mClassifier.recognize(null, mBtBeacons.getLatestBeacons(limit), mWifiBeacons.getLatestBeacons(mWifiUpdatedTime));
                    Point p = calcPositions(recognized);
                    Log.d(TAG, "recognized result" + p.toString());
                    publishProgress(p);
                }
                return null;
            }
        };
        asyncTask.execute();
    }

    private Point calcPositions(Map<Point, Double> prob) {
        double x = 0, y = 0;
        for (Map.Entry<Point, Double> entry : prob.entrySet()) {
            x += entry.getValue() * entry.getKey().x;
            y += entry.getValue() * entry.getKey().y;
        }
        return new Point((int) Math.round(x), (int) Math.round(y));
    }

    private IntensityMapView.OnDrawListener mDrawMapListener = new IntensityMapView.OnDrawListener() {
        @Override
        public void onDraw(Canvas canvas) {

        }
    };

    private BluetoothBeaconScanner.OnScanListener mBtScanListener = new BluetoothBeaconScanner.OnScanListener() {
        @Override
        public void onStartScan() {
            mBtBeacons = new BeaconList<>();
        }

        @Override
        public void onScan(BluetoothBeacon beacon) {
            mBtUpdatedTime = new Date();
            mBtBeacons.put(beacon);
        }
    };

    private WifiBeaconScanner.OnScanListener mWifiScanListener = new WifiBeaconScanner.OnScanListener() {
        @Override
        public void onStartScan() {
            mWifiBeacons = new BeaconList<>();
        }

        @Override
        public void onScan(BeaconList<WifiBeacon> beaconList) {
            mWifiUpdatedTime = new Date();
            for (WifiBeacon beacon : beaconList) {
                mWifiBeacons.put(beacon);
            }
        }
    };
}
