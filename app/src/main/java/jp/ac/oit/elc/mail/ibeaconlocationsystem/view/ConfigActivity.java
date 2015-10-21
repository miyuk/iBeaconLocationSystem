package jp.ac.oit.elc.mail.ibeaconlocationsystem.view;

import android.app.ProgressDialog;
import android.os.Bundle;
import android.os.Environment;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.TextView;
import android.widget.Toast;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;

import jp.ac.oit.elc.mail.ibeaconlocationsystem.BeaconList;
import jp.ac.oit.elc.mail.ibeaconlocationsystem.BeaconScanCallback;
import jp.ac.oit.elc.mail.ibeaconlocationsystem.BeaconScanner;
import jp.ac.oit.elc.mail.ibeaconlocationsystem.Sample;
import jp.ac.oit.elc.mail.ibeaconlocationsystem.R;
import jp.ac.oit.elc.mail.ibeaconlocationsystem.bluetooth.BluetoothBeacon;
import jp.ac.oit.elc.mail.ibeaconlocationsystem.wifi.WifiBeacon;


public class ConfigActivity extends AppCompatActivity {
    private static final String TAG = "MainActivity";
    private static final String INTENSITY_MAP_FILE = "/local/intensity_map.csv";
    private BeaconScanner mBeaconScanner;
    private Button mButtonStart;
    private Button mButtonStep;
    private CheckBox mToggleLockMap;
    private TextView mTextStatus;
    private IntensityMapView mIntensityMap;
    private ProgressDialog mProgDialog;

    //    private List<IntensitySample> mMidSampleList;
//    private boolean mIsStart;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_config);
        initViews();
        mBeaconScanner = new BeaconScanner(this);
//        mMidSampleList = new ArrayList<>();
    }

    private void initViews() {
        mButtonStart = (Button) findViewById(R.id.buttonStart);
        mButtonStep = (Button) findViewById(R.id.buttonStep);
        mToggleLockMap = (CheckBox) findViewById(R.id.toggleLockMap);
        mTextStatus = (TextView) findViewById(R.id.textStatus);
        mIntensityMap = (IntensityMapView) findViewById(R.id.intensityMapView);
        mButtonStart.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mBeaconScanner.scan(beaconScanCallback);
            }
        });
        mButtonStep.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                saveIntensityMap();
            }
        });
        mToggleLockMap.setOnCheckedChangeListener(mLockMapToggleChanged);
        mIntensityMap.setOnTouchListener(mMapTouchListener);
        mIntensityMap.setImageResource(R.mipmap.floor_map);
    }

    private void saveIntensityMap() {
        try {
            File file = new File(Environment.getExternalStorageDirectory() + INTENSITY_MAP_FILE);
            if(!file.exists()){
                file.createNewFile();
            }
            BufferedWriter writer = new BufferedWriter(new FileWriter(file, false));
            for (Sample sample : mIntensityMap.getSampleList()) {
                StringBuffer buffer = new StringBuffer();
                buffer.append(String.format("%d,%d", sample.x, sample.y));
                for (BluetoothBeacon beacon : sample.getBtBeaconList()) {
                    buffer.append(String.format(",%s,%d", beacon.getMacAddress(), beacon.getRssi()));
                }
                buffer.append("\n");
                writer.write(buffer.toString());
            }
            writer.flush();
            writer.close();
            Toast.makeText(this, "Save Complete", Toast.LENGTH_SHORT).show();
            ;
        } catch (Exception e) {
            Toast.makeText(this, "Save Failed", Toast.LENGTH_SHORT).show();
            e.printStackTrace();
        }
    }

    //    private View.OnClickListener buttonStartClick = new View.OnClickListener() {
//        @Override
//        public void onClick(View v) {
//            if(!mIsStart){
//                mMidSampleList.clear();
//                mIsStart = true;
//                mButtonStep.setEnabled(mIsStart);
//                mButtonStart.setText("End");
//            }else{
//                for(IntensitySample sample: mMidSampleList){
//                    mIntensityMap.sample(sample);
//                }
//                mIsStart = false;
//                mButtonStep.setEnabled(mIsStart);
//                mButtonStart.setText("Start");
//
//            }
//        }
//    };
    private View.OnTouchListener mMapTouchListener = new View.OnTouchListener() {
        @Override
        public boolean onTouch(View v, MotionEvent event) {
            if (!mToggleLockMap.isChecked()) {
                return false;
            }
            mIntensityMap.setPinPosition((int)event.getX(), (int)event.getY());
            return false;
        }
    };

    private BeaconScanCallback beaconScanCallback = new BeaconScanCallback() {
        @Override
        public void onStartScan() {
            mProgDialog = new ProgressDialog(ConfigActivity.this);
            mProgDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
            mProgDialog.setMessage("Now Scanning");
            mProgDialog.setCanceledOnTouchOutside(false);
            mProgDialog.setCancelable(true);
            mProgDialog.show();
        }

        @Override
        public void onScanned(BeaconList<BluetoothBeacon> btBeaconList, BeaconList<WifiBeacon> wifiBeaconList) {
            mProgDialog.dismiss();
            Toast.makeText(ConfigActivity.this, String.format("Scan Complete: BT(%d), Wifi(%d)", btBeaconList.size(), wifiBeaconList.size()), Toast.LENGTH_SHORT).show();
            mIntensityMap.sample(btBeaconList, wifiBeaconList);
            mButtonStep.setEnabled(true);
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

