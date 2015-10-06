package jp.ac.oit.elc.mail.ibeaconlocationsystem;

import android.bluetooth.BluetoothDevice;
import android.bluetooth.le.ScanCallback;
import android.bluetooth.le.ScanResult;
import android.util.Log;

import java.util.List;

/**
 * Created by yuuki on 10/6/15.
 */
public class BluetoothScanCallback extends ScanCallback{
    private static final String TAG = "BluetoothScanCallback";
    private BeaconList<BluetoothBeaconInfo> mBtBeaconList;

    public BluetoothScanCallback(BeaconList<BluetoothBeaconInfo> beaconList){
        mBtBeaconList = beaconList;
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
}
