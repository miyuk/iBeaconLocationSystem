package jp.ac.oit.elc.mail.ibeaconlocationsystem;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.le.BluetoothLeScanner;
import android.bluetooth.le.ScanCallback;
import android.bluetooth.le.ScanFilter;
import android.bluetooth.le.ScanResult;
import android.bluetooth.le.ScanSettings;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.wifi.WifiManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = "MainActivity";
    private BluetoothAdapter mBtAdapter;
    private BluetoothLeScanner mBtScanner;
    private WifiManager mWifiManager;
    private BeaconList<BluetoothBeaconInfo> mBtBeaconList;
    private BeaconList<WifiBeaconInfo> mWifiBeaconList;

    private List<ScanFilter> mBtFilterList;
    private ScanSettings mBtSettings;
    private WifiReceiver mWifiReceiver;
    private IntentFilter mWifiFilter;

    private Button mButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mBtAdapter = BluetoothAdapter.getDefaultAdapter();
        mBtScanner = mBtAdapter.getBluetoothLeScanner();
        mWifiManager = (WifiManager) getSystemService(WIFI_SERVICE);
        mBtBeaconList = new BeaconList<>();
        mWifiBeaconList = new BeaconList<>();

        mBtFilterList = new ArrayList<>();
        mBtFilterList.add(new ScanFilter.Builder().build());
        mBtSettings = new ScanSettings.Builder().build();
        //mBtScanner.startScan(mBtFilterList, mBtSettings, scanedBtDevices);
        mWifiReceiver = new WifiReceiver(mWifiManager, mWifiBeaconList);
        mWifiFilter = new IntentFilter(WifiManager.SCAN_RESULTS_AVAILABLE_ACTION);
        registerReceiver(mWifiReceiver, mWifiFilter);
        //mWifiManager.startScan();
        mButton = (Button)findViewById(R.id.button);
        mButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mWifiManager.startScan();
                mBtScanner.startScan(mBtFilterList, mBtSettings, scanedBtDevices);
            }
        });
    }

    private ScanCallback scanedBtDevices = new ScanCallback() {
        @Override
        public void onScanResult(int callbackType, ScanResult result) {
            super.onScanResult(callbackType, result);
            BluetoothDevice device = result.getDevice();
            for (BluetoothBeaconInfo item : mBtBeaconList) {
                if (device.getAddress().equals(item.getMacAddress())) {
                    Log.i(TAG, "update BT beacon: " + device.getAddress() + "(RSSI: " + result.getRssi() + ")");
                    item.update(result.getRssi());
                    return;
                }
            }
            //if not existing in List
            Log.i(TAG, "add BT beacon: " + device.getAddress());
            BluetoothBeaconInfo beacon = new BluetoothBeaconInfo(device.getAddress(), result.getRssi());
            mBtBeaconList.add(beacon);
        }
    };


}
