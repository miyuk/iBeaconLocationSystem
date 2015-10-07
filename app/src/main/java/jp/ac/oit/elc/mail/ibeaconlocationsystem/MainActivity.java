package jp.ac.oit.elc.mail.ibeaconlocationsystem;

import android.app.ProgressDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

public class MainActivity extends AppCompatActivity {
    private static final String TAG = "MainActivity";
    private BeaconScanner mBeaconScanner;

    private Button mButton;
    private ImageView mImageIntensityMap;
    private ImageView mImageLocationPin;
    private IntensityMapView mIntensityMap;
    private ProgressDialog mProgDialog;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mBeaconScanner = new BeaconScanner(this);
        mButton = (Button) findViewById(R.id.button);
        mButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d(TAG, "onClick");
                mBeaconScanner.scan(beaconScanCallback);
            }
        });
        mIntensityMap = (IntensityMapView)findViewById(R.id.intensityMapView);
        mIntensityMap.setImageResource(R.mipmap.location_pin);
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
            mIntensityMap.sample(0, 1, btBeaconList, wifiBeaconList);
        }

        @Override
        public void onScanFailed() {
            Log.e(TAG, "error scan");
        }
    };

}
