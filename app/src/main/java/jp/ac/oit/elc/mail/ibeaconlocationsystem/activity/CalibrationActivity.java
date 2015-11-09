package jp.ac.oit.elc.mail.ibeaconlocationsystem.activity;

import android.bluetooth.BluetoothManager;
import android.bluetooth.le.BluetoothLeScanner;
import android.bluetooth.le.ScanCallback;
import android.bluetooth.le.ScanFilter;
import android.bluetooth.le.ScanResult;
import android.bluetooth.le.ScanSettings;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import jp.ac.oit.elc.mail.ibeaconlocationsystem.Environment;
import jp.ac.oit.elc.mail.ibeaconlocationsystem.R;

public class CalibrationActivity extends AppCompatActivity {

    private static final String TAG = CalibrationActivity.class.getSimpleName();
    private List<Map.Entry<Float, Integer>> mCalibrations;
    private BluetoothLeScanner mScanner;
    private ScanSettings mSettings;
    private List<ScanFilter> mFilters;
    private String mTargetDevice = "B4:99:4C:4F:97:11";
    private EditText mDistanceText;
    private Button mStartButton;
    private Button mSaveButton;
    private TextView mStatusText;
    private boolean mStarted;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_calibration);
        initViews();
        mCalibrations = new ArrayList<>();
        BluetoothManager manager = (BluetoothManager) getSystemService(BLUETOOTH_SERVICE);
        mScanner = manager.getAdapter().getBluetoothLeScanner();
        mSettings = new ScanSettings.Builder().setScanMode(ScanSettings.SCAN_MODE_LOW_LATENCY).build();
        mFilters = new ArrayList<>();
        mFilters.add(new ScanFilter.Builder().setDeviceAddress(mTargetDevice).build());
    }

    private void initViews() {
        mDistanceText = (EditText) findViewById(R.id.editDistance);
        mStatusText = (TextView) findViewById(R.id.textStatus);
        mStartButton = (Button) findViewById(R.id.buttonStart);
        mSaveButton = (Button) findViewById(R.id.buttonSave);
        mSaveButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    saveCsv();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        });
        mStartButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!mStarted) {
                    startScan();
                    mStartButton.setText("Stop");
                    mStarted = true;
                } else {
                    stopScan();
                    mStartButton.setText("Start");
                    mStarted = false;
                }
            }
        });
    }

    private void startScan() {
        mDistanceText.setEnabled(false);
        mSaveButton.setEnabled(false);
        mScanner.startScan(mFilters, mSettings, mCallback);
    }

    private void stopScan() {
        mDistanceText.setEnabled(true);
        mSaveButton.setEnabled(true);
        mScanner.stopScan(mCallback);
    }

    private void saveCsv() throws IOException {
        String path = Environment.APP_DIR + "/calibrations.csv";
        File file = new File(path);
        if (!file.exists()) {
            file.getParentFile().mkdirs();
            file.createNewFile();
        }
        BufferedWriter writer = new BufferedWriter(new FileWriter(file, false));
        for (Map.Entry<Float, Integer> entry : mCalibrations) {
            writer.write(String.format("%f,%d\n", entry.getKey(), entry.getValue()));
        }
        writer.flush();
        writer.close();
        Toast.makeText(this, "Save Success", Toast.LENGTH_SHORT).show();
    }

    private ScanCallback mCallback = new ScanCallback() {
        @Override
        public void onScanResult(int callbackType, ScanResult result) {
            super.onScanResult(callbackType, result);
            float distance = Float.parseFloat(mDistanceText.getText().toString());
            int rssi = result.getRssi();
            Log.d(TAG, String.format("scan: %d dBm", rssi));
            mCalibrations.add(new AbstractMap.SimpleEntry<Float, Integer>(distance, rssi));
            mStatusText.setText(String.format("%d records (%d dBm)", mCalibrations.size(), rssi));
        }
    };
}
