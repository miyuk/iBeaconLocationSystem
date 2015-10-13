package jp.ac.oit.elc.mail.ibeaconlocationsystem;

import android.app.ProgressDialog;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import jp.ac.oit.elc.mail.ibeaconlocationsystem.bluetooth.BluetoothBeacon;
import jp.ac.oit.elc.mail.ibeaconlocationsystem.wifi.WifiBeacon;


public class MainActivity extends AppCompatActivity {
    private static final String TAG = "MainActivity";
    private BeaconScanner mBeaconScanner;
    private Button mButtonStartStop;
    private Button mButtonStep;
    private CheckBox mCheckBoxLockMap;
    private TextView mTextStatus;
    private ImageView mImageLocationPin;
    private IntensityMapView mIntensityMap;
    private ProgressDialog mProgDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        setView();
        mBeaconScanner = new BeaconScanner(this);
        mIntensityMap.setImageResource(R.mipmap.floor_map);
    }

    private void setView() {
        mButtonStartStop = (Button) findViewById(R.id.buttonStartStop);
        mButtonStep = (Button) findViewById(R.id.buttonStep);
        mCheckBoxLockMap = (CheckBox) findViewById(R.id.checkLock);
        mTextStatus = (TextView) findViewById(R.id.textStatus);
        mIntensityMap = (IntensityMapView) findViewById(R.id.intensityMapView);
        mButtonStartStop.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mBeaconScanner.scan(beaconScanCallback);
                //TODO: Set the walking route
            }
        });
        mCheckBoxLockMap.setOnCheckedChangeListener(mLockMapToggleChanged);
        mIntensityMap.setOnTouchListener(mMapTouchListener);
    }

    private View.OnTouchListener mMapTouchListener = new View.OnTouchListener() {
        @Override
        public boolean onTouch(View v, MotionEvent event) {
            if (!mCheckBoxLockMap.isChecked()) {
                return false;
            }
            mIntensityMap.setPinPosition(event.getX(), event.getY());
            return false;
//            switch (event.getAction()) {
//                case MotionEvent.ACTION_DOWN:
//                    setPinPosition(event.getX(), event.getY());
//                case MotionEvent.ACTION_MOVE:
//                case MotionEvent.ACTION_UP:
//                    //setPinPosition(event.getX(), event.getY());
//                    invalidate();
//                    return false;
//                case MotionEvent.ACTION_CANCEL:
//                default:
//                    return true;
//            }
        }
    };
    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    protected void onPause() {
        super.onPause();
    }

    private BeaconScanCallback beaconScanCallback = new BeaconScanCallback() {
        @Override
        public void onStartScan() {
            mProgDialog = new ProgressDialog(MainActivity.this);
            mProgDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
            mProgDialog.setMessage("Now Scanning");
            mProgDialog.setCanceledOnTouchOutside(false);
            mProgDialog.setCancelable(true);
            mProgDialog.show();
        }

        @Override
        public void onScanned(BeaconList<BluetoothBeacon> btBeaconList, BeaconList<WifiBeacon> wifiBeaconList) {
            mProgDialog.dismiss();
            int beaconNum = btBeaconList.size() + wifiBeaconList.size();
            Toast.makeText(MainActivity.this, String.format("Scan Complete: BT(%d), Wifi(%d)", btBeaconList.size(), wifiBeaconList.size()), Toast.LENGTH_SHORT).show();
            mIntensityMap.sample(btBeaconList, wifiBeaconList);
        }
        @Override
        public void onScanFailed() {
            Log.e(TAG, "error scan");
        }
    };

    private CompoundButton.OnCheckedChangeListener mLockMapToggleChanged = new CompoundButton.OnCheckedChangeListener() {
        @Override
        public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
            mIntensityMap.setScaleEnabled(!isChecked);
            mIntensityMap.setScrollEnabled(!isChecked);
            mIntensityMap.setDoubleTapEnabled(!isChecked);
        }
    };
}

