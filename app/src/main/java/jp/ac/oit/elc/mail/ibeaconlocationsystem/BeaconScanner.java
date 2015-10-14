package jp.ac.oit.elc.mail.ibeaconlocationsystem;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.le.BluetoothLeScanner;
import android.content.Context;
import android.os.AsyncTask;

import jp.ac.oit.elc.mail.ibeaconlocationsystem.bluetooth.BluetoothBeacon;
import jp.ac.oit.elc.mail.ibeaconlocationsystem.bluetooth.BluetoothScanCallback;
import jp.ac.oit.elc.mail.ibeaconlocationsystem.wifi.WifiBeacon;
import jp.ac.oit.elc.mail.ibeaconlocationsystem.wifi.WifiReceiver;

/**
 * Created by yuuki on 10/6/15.
 */
public class BeaconScanner {
    private static final String TAG = "BeaconScanner";
    private static final long SCAN_TIMEOUT_MILLIS = 5000;
    private Context mContext;
    private BluetoothLeScanner mBtScanner;
    private BluetoothScanCallback mBtScanCallback;

    private WifiReceiver mWifiReceiver;
    private boolean mIsScanning;

    public BeaconScanner(Context context) {
        mContext = context;
        mBtScanner = BluetoothAdapter.getDefaultAdapter().getBluetoothLeScanner();

    }

    public void scan(final BeaconScanCallback callback) {
        callback.onStartScan();
        mBtScanCallback = new BluetoothScanCallback(mBtScanner);
        mWifiReceiver = new WifiReceiver(mContext);
        AsyncTask<BeaconScanCallback, BeaconList[], BeaconList[]> scanAsyncTask = new AsyncTask<BeaconScanCallback, BeaconList[], BeaconList[]>() {
            BeaconScanCallback callback;

            @Override
            protected BeaconList[] doInBackground(BeaconScanCallback... params) {
                callback = params[0];
                mBtScanCallback.startScan();
                mWifiReceiver.startScan();
                try {
                    Thread.sleep(SCAN_TIMEOUT_MILLIS);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                mBtScanCallback.stopScan();
                mWifiReceiver.stopScan();
                BeaconList[] result = new BeaconList[2];
                result[0] = mBtScanCallback.getBeaconList();
                result[1] = mWifiReceiver.getBeaconList();
                return result;
            }

            @Override
            protected void onPostExecute(BeaconList[] beaconLists) {
                super.onPostExecute(beaconLists);
                BeaconList<BluetoothBeacon> btList = beaconLists[0];
                BeaconList<WifiBeacon> wifiList = beaconLists[1];
                callback.onScanned(btList, wifiList);
            }

        };
        scanAsyncTask.execute(callback);
    }


}
