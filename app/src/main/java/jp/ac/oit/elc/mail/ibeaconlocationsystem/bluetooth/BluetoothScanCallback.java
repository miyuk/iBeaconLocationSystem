package jp.ac.oit.elc.mail.ibeaconlocationsystem.bluetooth;

import android.bluetooth.BluetoothDevice;
import android.bluetooth.le.BluetoothLeScanner;
import android.bluetooth.le.ScanCallback;
import android.bluetooth.le.ScanFilter;
import android.bluetooth.le.ScanResult;
import android.bluetooth.le.ScanSettings;
import android.util.Log;

import java.util.ArrayList;
import java.util.List;

import jp.ac.oit.elc.mail.ibeaconlocationsystem.BeaconList;
import jp.ac.oit.elc.mail.ibeaconlocationsystem.bluetooth.BluetoothBeacon;

/**
 * Created by yuuki on 10/6/15.
 */
public class BluetoothScanCallback extends ScanCallback{
    private static final String TAG = BluetoothScanCallback.class.getSimpleName();
    private BeaconList<BluetoothBeacon> mBtBeaconList;
    private BluetoothLeScanner mBtScanner;
    private List<ScanFilter> mBtFilterList;
    private ScanSettings mBtSettings;
    private boolean mIsScannning;
    public BluetoothScanCallback(BluetoothLeScanner btScanner){
        mBtScanner = btScanner;
        mIsScannning = false;
        mBtFilterList = new ArrayList<>();
        mBtFilterList.add(new ScanFilter.Builder().build());
        mBtSettings = new ScanSettings.Builder().build();
    }

    public void startScan(){
        mIsScannning = true;
        mBtBeaconList = new BeaconList<>();
        mBtScanner.startScan(mBtFilterList, mBtSettings, this);
    }

    public void stopScan(){
        mBtScanner.stopScan(this);
        mIsScannning = false;
    }
    public BeaconList<BluetoothBeacon> getBeaconList(){
        return mBtBeaconList;
    }
    @Override
    public void onScanFailed(int errorCode) {
        super.onScanFailed(errorCode);
        Log.e(TAG, "scanFailed: " + String.valueOf(errorCode));
    }

    @Override
    public void onBatchScanResults(List<ScanResult> results) {
        super.onBatchScanResults(results);
        Log.i(TAG, "scanned" + String.valueOf(results.size()));
    }

    @Override
    public void onScanResult(int callbackType, ScanResult result) {
        super.onScanResult(callbackType, result);
        BluetoothDevice device = result.getDevice();
        for (BluetoothBeacon item : mBtBeaconList) {
            if (device.getAddress().equals(item.getMacAddress())) {
                Log.i(TAG, "update BT beacon: " + device.getAddress() + "(RSSI: " + result.getRssi() + ")");
                item.update(result.getRssi());
                return;
            }
        }
        //if not existing in List
        Log.i(TAG, "add BT beacon: " + device.getAddress());
        BluetoothBeacon beacon = new BluetoothBeacon(device.getAddress(), result.getRssi());
        mBtBeaconList.add(beacon);
    }
}
