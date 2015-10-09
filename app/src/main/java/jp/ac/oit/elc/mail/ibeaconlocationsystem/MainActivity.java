package jp.ac.oit.elc.mail.ibeaconlocationsystem;

import android.app.ProgressDialog;
import android.graphics.Point;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
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
    private CheckBox mCheckLock;
    private TextView mTextStatus;
    private ImageView mImageLocationPin;
    private IntensityMapView mIntensityMap;
    private ProgressDialog mProgDialog;
    private Point mPinPoint;
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
        mCheckLock = (CheckBox) findViewById(R.id.checkLock);
        mTextStatus = (TextView) findViewById(R.id.textStatus);
        mIntensityMap = (IntensityMapView) findViewById(R.id.intensityMapView);
        mImageLocationPin = (ImageView)findViewById(R.id.imageLocationPin);
        mButtonStartStop.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mBeaconScanner.scan(beaconScanCallback);
                //TODO: Set the walking route
            }
        });
        mButtonStep.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // TODO: Step and Correct data
            }
        });
        mCheckLock.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                // TODO: if true, lock the map
            }
        });
        mIntensityMap.setOnTouchListener(new View.OnTouchListener() {
            Handler handler;
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                mPinPoint = new Point((int)event.getX(), (int)event.getY());
                switch (event.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                        handler = new Handler();
                        Log.d(TAG, "down");
                        final float x = event.getX();
                        final float y = event.getY();
                        handler.postDelayed(new Runnable() {
                            @Override
                            public void run() {
                                mTextStatus.setText(String.format("Long Touch (%.0f, %.0f)", x, y));
                                mImageLocationPin.setX(x - mImageLocationPin.getWidth() / 2);
                                mImageLocationPin.setY(y- mImageLocationPin.getHeight());
                                mImageLocationPin.setVisibility(View.VISIBLE);
                            }
                        }, 1000);
                        return true;
                    case MotionEvent.ACTION_UP:
                        Log.d(TAG, "up");
                        handler.removeCallbacksAndMessages(null);
                        return true;
                    default:
                        return true;
                }            }
        });

    }

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
            mIntensityMap.sample(mPinPoint.x, mPinPoint.y, btBeaconList, wifiBeaconList);
        }

        @Override
        public void onScanFailed() {
            Log.e(TAG, "error scan");
        }
    };

}
