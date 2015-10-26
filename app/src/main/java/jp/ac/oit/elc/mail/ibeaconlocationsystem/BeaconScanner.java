package jp.ac.oit.elc.mail.ibeaconlocationsystem;

import android.content.AsyncTaskLoader;
import android.content.Context;
import android.os.AsyncTask;

import jp.ac.oit.elc.mail.ibeaconlocationsystem.bluetooth.BluetoothBeacon;
import jp.ac.oit.elc.mail.ibeaconlocationsystem.bluetooth.BluetoothScanManager;
import jp.ac.oit.elc.mail.ibeaconlocationsystem.wifi.WifiScanManager;

/**
 * Created by yuuki on 10/6/15.
 */
public class BeaconScanner{
    private static final long SCAN_TIMEOUT_MILLIS = 2000;
    private BluetoothScanManager mBtScanManager;
    private WifiScanManager mWifiScanManager;
    private boolean mStarted = false;
    private BeaconScanCallback mCallback;

    public BeaconScanner(Context context) {
        mWifiScanManager = new WifiScanManager(context);
        mBtScanManager = new BluetoothScanManager(context);
    }

    public void startScan(BeaconScanCallback callback) {
        if (mStarted) {
            return;
        }
        BeaconScanAsyncTask scanTask = new BeaconScanAsyncTask(callback);
        callback.onStartScan();
        scanTask.execute();
    }
    private class BeaconScanAsyncTask extends AsyncTask<Void, BeaconList[], BeaconList[]>{
        private BeaconScanCallback mCallback;
        public BeaconScanAsyncTask(BeaconScanCallback callback){
            super();
            mCallback = callback;
        }

        @Override
        protected void onPostExecute(BeaconList[] beaconLists) {
            super.onPostExecute(beaconLists);
            mCallback.onScanned(beaconLists[0], beaconLists[1]);
        }

        @Override
        protected void onProgressUpdate(BeaconList[]... values) {
            super.onProgressUpdate(values);
        }

        @Override
        protected BeaconList[] doInBackground(Void... params) {
            mBtScanManager.startScan();
            mWifiScanManager.startScan();
            try {
                Thread.sleep(SCAN_TIMEOUT_MILLIS);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            mBtScanManager.stopScan();
            mWifiScanManager.stopScan();
            BeaconList[] result = new BeaconList[2];
            result[0] = mBtScanManager.getBeaconList();
            result[1] = mWifiScanManager.getBeaconList();
            return result;
        }
    }
}
