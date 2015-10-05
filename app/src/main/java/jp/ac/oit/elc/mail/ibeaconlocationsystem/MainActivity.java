package jp.ac.oit.elc.mail.ibeaconlocationsystem;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.le.BluetoothLeScanner;
import android.bluetooth.le.ScanCallback;
import android.bluetooth.le.ScanFilter;
import android.bluetooth.le.ScanResult;
import android.bluetooth.le.ScanSettings;
import android.net.wifi.WifiManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = "MainActivity";
    private BluetoothAdapter mBtAdapter;
    private BluetoothLeScanner mBtScanner;
    private WifiManager mWifiManager;
    private List<BluetoothBeaconInfo> mBtBeaconList;
    private List<WifiBeaconInfo> mWifiBeaconList;

    private List<ScanFilter> mBtFilterList;
    private ScanSettings mBtSettings;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mBtAdapter = BluetoothAdapter.getDefaultAdapter();
        mBtScanner = mBtAdapter.getBluetoothLeScanner();
        mWifiManager = (WifiManager) getSystemService(WIFI_SERVICE);
        mBtBeaconList = new ArrayList<>();
        mWifiBeaconList = new ArrayList<>();

        mBtFilterList = new ArrayList<>();
        mBtFilterList.add(new ScanFilter.Builder().build());
        mBtSettings = new ScanSettings.Builder().build();
        mBtScanner.startScan(mBtFilterList, mBtSettings, scanBtDevice);
    }

    private ScanCallback scanBtDevice = new ScanCallback() {
        @Override
        public void onScanResult(int callbackType, ScanResult result) {
            super.onScanResult(callbackType, result);
            BluetoothDevice device = result.getDevice();
            for (BluetoothBeaconInfo item : mBtBeaconList) {
                if (device.getAddress().equals(item.getMacAddress())) {
                    Log.d(TAG, "update BT beacon: " + device.getAddress() + "(RSSI: " + result.getRssi() + ")");
                    item.update(result.getRssi());
                    return;
                }
            }
            //if not existing in List
            Log.d(TAG, "add BT beacon: " + device.getAddress());
            BluetoothBeaconInfo beacon = new BluetoothBeaconInfo(
                    device.getName(), device.getUuids(),
                    device.getAddress(), result.getRssi());
            mBtBeaconList.add(beacon);
        }
    };

}
