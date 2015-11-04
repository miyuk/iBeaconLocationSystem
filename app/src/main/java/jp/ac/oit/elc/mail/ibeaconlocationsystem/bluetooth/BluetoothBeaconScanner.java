package jp.ac.oit.elc.mail.ibeaconlocationsystem.bluetooth;

import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothManager;
import android.bluetooth.le.BluetoothLeScanner;
import android.bluetooth.le.ScanCallback;
import android.bluetooth.le.ScanFilter;
import android.bluetooth.le.ScanResult;
import android.bluetooth.le.ScanSettings;
import android.content.Context;
import android.util.Log;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by yuuki on 10/6/15.
 */
public class BluetoothBeaconScanner {
    private static final String TAG = BluetoothBeaconScanner.class.getSimpleName();
    private List<ScanFilter> mFilters;
    private BluetoothLeScanner mScanner;
    private ScanSettings mSettings;
    private boolean mStarted;
    private OnScanListener mOnScanListener;

    public BluetoothBeaconScanner(Context context) {
        BluetoothManager btManager = (BluetoothManager) context.getSystemService(Context.BLUETOOTH_SERVICE);
        mScanner = btManager.getAdapter().getBluetoothLeScanner();
        mFilters = new ArrayList<>();
        mFilters.add(new ScanFilter.Builder().build());
        mSettings = new ScanSettings.Builder().setScanMode(ScanSettings.SCAN_MODE_LOW_LATENCY).build();
    }

    public void startScan() {
        if (mStarted) {
            return;
        }
        if(mOnScanListener != null){
            mOnScanListener.onStartScan();
        }
        mScanner.startScan(mFilters, mSettings, mScanCallback);
        mStarted = true;

    }

    public void stopScan() {
        if (!mStarted) {
            return;
        }
        mScanner.stopScan(mScanCallback);
        mStarted = false;
    }

    public void setOnScanListener(OnScanListener listener) {
        mOnScanListener = listener;
    }

    private ScanCallback mScanCallback = new ScanCallback() {
        @Override
        public void onScanResult(int callbackType, ScanResult result) {
            super.onScanResult(callbackType, result);
            BluetoothDevice device = result.getDevice();
            BluetoothBeacon beacon = new BluetoothBeacon(device.getAddress(), result.getRssi());
            if (mOnScanListener != null) {
                mOnScanListener.onScan(beacon);
            }
        }

        @Override
        public void onScanFailed(int errorCode) {
            super.onScanFailed(errorCode);
            Log.d(TAG, String.format("can't scan because error code %d", errorCode));
        }
    };

    public interface OnScanListener {
        void onStartScan();
        void onScan(BluetoothBeacon beacon);
    }
}
