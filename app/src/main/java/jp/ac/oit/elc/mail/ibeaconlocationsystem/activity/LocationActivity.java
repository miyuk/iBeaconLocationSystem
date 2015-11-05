package jp.ac.oit.elc.mail.ibeaconlocationsystem.activity;

import android.app.LoaderManager;
import android.app.ProgressDialog;
import android.content.Loader;
import android.graphics.Point;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.widget.TextView;
import android.widget.Toast;

import java.util.Date;
import java.util.Map;

import jp.ac.oit.elc.mail.ibeaconlocationsystem.LocationSender;
import jp.ac.oit.elc.mail.ibeaconlocationsystem.R;
import jp.ac.oit.elc.mail.ibeaconlocationsystem.bluetooth.BluetoothBeaconScanner;
import jp.ac.oit.elc.mail.ibeaconlocationsystem.classification.ClassifierLoader;
import jp.ac.oit.elc.mail.ibeaconlocationsystem.classification.LocationClassifier;
import jp.ac.oit.elc.mail.ibeaconlocationsystem.view.IntensityMapView;
import jp.ac.oit.elc.mail.ibeaconlocationsystem.wifi.WifiBeaconScanner;

import static jp.ac.oit.elc.mail.ibeaconlocationsystem.Environment.BT_TRAINING_CSV;
import static jp.ac.oit.elc.mail.ibeaconlocationsystem.Environment.SERVER_URL;
import static jp.ac.oit.elc.mail.ibeaconlocationsystem.Environment.WIFI_TRAINING_CSV;

public class LocationActivity extends AppCompatActivity{
    private static final String TAG = LocationActivity.class.getSimpleName();

    private IntensityMapView mIntensityMapView;
    private TextView mTextStatus;
    private BluetoothBeaconScanner mBtScanner;
    private WifiBeaconScanner mWifiScanner;
    private LocationClassifier mClassifier;
    private LocationSender mSender;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_location);
        initViews();
        mSender = new LocationSender(SERVER_URL);
        mBtScanner = new BluetoothBeaconScanner(this);
        mWifiScanner = new WifiBeaconScanner(this);
        Bundle bundle = new Bundle();
        bundle.putString("BT_TRAINING_CSV", BT_TRAINING_CSV);
        bundle.putString("WIFI_TRAINING_CSV", WIFI_TRAINING_CSV);
        getLoaderManager().initLoader(0, bundle, mClassifierLoaderCallback);
    }

    private void initViews() {
        mIntensityMapView = (IntensityMapView) findViewById(R.id.intensityMapView);
        mTextStatus = (TextView) findViewById(R.id.textStatus);
        mIntensityMapView.setImageResource(R.mipmap.floor_map);
        mIntensityMapView.setEnabledPin(true);
    }

    private void startScan() {
        new AsyncTask<Void, Point, Void>() {
            @Override
            protected void onProgressUpdate(Point... values) {
                super.onProgressUpdate(values);
                mIntensityMapView.setPinImageCoordPosition(values[0].x, values[0].y);
                mSender.send(values[0]);
            }

            @Override
            protected void onPreExecute() {
                super.onPreExecute();
                mBtScanner.startScan();
                mWifiScanner.startScan();
            }

            @Override
            protected Void doInBackground(Void... params) {
                while (!isCancelled()) {
                    try {
                        Thread.sleep(1000);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                        cancel(true);
                    }
                    Date btlimit = new Date(new Date().getTime() - 1000);

                    Map<Point, Double> recognized = mClassifier.recognize(null,
                            mBtScanner.getBeaconBuffer().getLatestBeacons(btlimit),
                            mWifiScanner.getBeaconBuffer().getLatestBeacons(mWifiScanner.getUpdateTime()));
                    Point pos = calcPositions(recognized);
                    Log.d(TAG, "recognized result" + pos.toString());
                    publishProgress(pos);
                }
                return null;
            }
        }.execute();
    }


    private Point calcPositions(Map<Point, Double> prob) {
        double x = 0, y = 0;
        for (Map.Entry<Point, Double> entry : prob.entrySet()) {
            x += entry.getValue() * entry.getKey().x;
            y += entry.getValue() * entry.getKey().y;
        }
        return new Point((int) Math.round(x), (int) Math.round(y));
    }

    LoaderManager.LoaderCallbacks<LocationClassifier> mClassifierLoaderCallback = new LoaderManager.LoaderCallbacks<LocationClassifier>() {
        ProgressDialog dialog;
        @Override
        public Loader<LocationClassifier> onCreateLoader(int id, Bundle args) {
            dialog = new ProgressDialog(LocationActivity.this);
            dialog.setMessage("Load Classifier");
            dialog.setCancelable(false);
            dialog.show();
            String btTraingCsv = args.getString("BT_TRAINING_CSV");
            String wifiTrainingCsv = args.getString("WIFI_TRAINING_CSV");
            ClassifierLoader loader = new ClassifierLoader(LocationActivity.this, btTraingCsv, wifiTrainingCsv);
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

}
