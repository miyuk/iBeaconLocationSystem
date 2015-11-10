package jp.ac.oit.elc.mail.ibeaconlocationsystem.activity;

import android.app.LoaderManager;
import android.app.ProgressDialog;
import android.content.Loader;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Point;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.widget.TextView;
import android.widget.Toast;

import java.util.Date;
import java.util.Map;

import jp.ac.oit.elc.mail.ibeaconlocationsystem.BeaconList;
import jp.ac.oit.elc.mail.ibeaconlocationsystem.LocationSender;
import jp.ac.oit.elc.mail.ibeaconlocationsystem.R;
import jp.ac.oit.elc.mail.ibeaconlocationsystem.bluetooth.BluetoothBeaconScanner;
import jp.ac.oit.elc.mail.ibeaconlocationsystem.classification.ClassifierLoader;
import jp.ac.oit.elc.mail.ibeaconlocationsystem.classification.LocationClassifier;
import jp.ac.oit.elc.mail.ibeaconlocationsystem.view.IntensityMapView;
import jp.ac.oit.elc.mail.ibeaconlocationsystem.wifi.WifiBeacon;
import jp.ac.oit.elc.mail.ibeaconlocationsystem.wifi.WifiBeaconScanner;

import static jp.ac.oit.elc.mail.ibeaconlocationsystem.Environment.BT_TRAINING_CSV;
import static jp.ac.oit.elc.mail.ibeaconlocationsystem.Environment.SERVER_URL;
import static jp.ac.oit.elc.mail.ibeaconlocationsystem.Environment.WIFI_TRAINING_CSV;

public class LocationActivity extends AppCompatActivity {
    private static final String TAG = LocationActivity.class.getSimpleName();

    //view
    private IntensityMapView mIntensityMap;
    private TextView mTextStatus;
    //model
    private BluetoothBeaconScanner mBtScanner;
    private WifiBeaconScanner mWifiScanner;
    private LocationClassifier mClassifier;
    private LocationSender mSender;
    private Point mWifiLocatedPosition;
    private Point mBtLocatedPosition;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_location);
        initViews();
        mSender = new LocationSender(SERVER_URL);
        mBtScanner = new BluetoothBeaconScanner(this);
        mWifiScanner = new WifiBeaconScanner(this);
        mWifiScanner.setOnScanListener(mWifiScanListener);
        Bundle bundle = new Bundle();
        bundle.putString("BT_TRAINING_CSV", BT_TRAINING_CSV);
        bundle.putString("WIFI_TRAINING_CSV", WIFI_TRAINING_CSV);
        getLoaderManager().initLoader(0, bundle, mClassifierLoaderCallback);
    }

    private void initViews() {
        mIntensityMap = (IntensityMapView) findViewById(R.id.intensityMapView);
        mTextStatus = (TextView) findViewById(R.id.textStatus);
        mIntensityMap.setImageResource(R.mipmap.floor_map);
        mIntensityMap.setOnDrawListener(mDrawMapListener);
//        mIntensityMap.setEnabledPin(true);
    }

    private void startScan() {
        new AsyncTask<Void, Point, Void>() {
            @Override
            protected void onProgressUpdate(Point... values) {
                super.onProgressUpdate(values);
                mBtLocatedPosition = values[0];
                mIntensityMap.invalidate();
//                mSender.send(values[0]);
            }

            @Override
            protected void onPreExecute() {
                super.onPreExecute();
                mBtScanner.startScan();
                mWifiScanner.startScan();
            }

            @Override
            protected Void doInBackground(Void... params) {
                Date lastUpdateTime = new Date();
                while (!isCancelled()) {
                    try {
                        Thread.sleep(1000);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                        cancel(true);
                    }
                    Map<Point, Double> pValues = mClassifier.recognize(mBtScanner.getBeaconBuffer().getLatestBeacons(lastUpdateTime), true);
                    Point pos = mClassifier.predictPosition(pValues);
                    Log.d(TAG, "recognized result" + pos.toString());
                    lastUpdateTime = new Date();
                    publishProgress(pos);
                }
                return null;
            }
        }.execute();
    }

    LoaderManager.LoaderCallbacks<LocationClassifier> mClassifierLoaderCallback = new LoaderManager.LoaderCallbacks<LocationClassifier>() {
        ProgressDialog dialog;

        @Override
        public Loader<LocationClassifier> onCreateLoader(int id, Bundle args) {
            dialog = new ProgressDialog(LocationActivity.this);
            dialog.setMessage("Load Classifier");
            dialog.setCancelable(false);
            dialog.show();
            String btTrainingCsv = args.getString("BT_TRAINING_CSV");
            String wifiTrainingCsv = args.getString("WIFI_TRAINING_CSV");
            ClassifierLoader loader = new ClassifierLoader(LocationActivity.this, btTrainingCsv, wifiTrainingCsv);
            return loader;
        }

        @Override
        public void onLoadFinished(Loader<LocationClassifier> loader, LocationClassifier data) {
            Toast.makeText(LocationActivity.this, "Loaded Classifier", Toast.LENGTH_SHORT).show();
            dialog.dismiss();
            mClassifier = data;
            startScan();
        }

        @Override
        public void onLoaderReset(Loader<LocationClassifier> loader) {

        }
    };

    private WifiBeaconScanner.OnScanListener mWifiScanListener = new WifiBeaconScanner.OnScanListener() {
        @Override
        public void onStartScan() {

        }

        @Override
        public void onScan(BeaconList<WifiBeacon> beaconList) {
            Map<Point, Double> pValues = mClassifier.recognize(beaconList, false);
            mWifiLocatedPosition = mClassifier.predictPosition(pValues);
            Log.d(TAG, "Wifi estimation " + mWifiLocatedPosition.toString());
            mIntensityMap.invalidate();
        }
    };

    private IntensityMapView.OnDrawListener mDrawMapListener = new IntensityMapView.OnDrawListener() {
        @Override
        public void onDraw(Canvas canvas) {
            if (mWifiLocatedPosition != null) {
                Point pos = mIntensityMap.imageToScreenCoord(mWifiLocatedPosition.x, mWifiLocatedPosition.y);
                Paint paint = new Paint();
                paint.setStyle(Paint.Style.FILL);
                paint.setColor(Color.GRAY);
                canvas.drawCircle(pos.x, pos.y, 10, paint);
            }
            if (mBtLocatedPosition != null) {
                Point pos = mIntensityMap.imageToScreenCoord(mBtLocatedPosition.x, mBtLocatedPosition.y);
                Paint paint = new Paint();
                paint.setStyle(Paint.Style.FILL);
                paint.setColor(Color.GREEN);
                canvas.drawCircle(pos.x, pos.y, 10, paint);
            }
            if (mBtLocatedPosition != null && mWifiLocatedPosition != null) {
                Point pos = mIntensityMap.imageToScreenCoord(
                        (mBtLocatedPosition.x + mWifiLocatedPosition.x) / 2,
                        (mBtLocatedPosition.y + mWifiLocatedPosition.y) / 2);
                Paint paint = new Paint();
                paint.setStyle(Paint.Style.FILL);
                paint.setColor(Color.CYAN);
                canvas.drawCircle(pos.x, pos.y, 20, paint);
            }
        }
    };

}
