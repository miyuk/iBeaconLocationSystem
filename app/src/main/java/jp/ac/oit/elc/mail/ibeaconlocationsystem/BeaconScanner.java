package jp.ac.oit.elc.mail.ibeaconlocationsystem;

import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;

import java.util.Date;

import jp.ac.oit.elc.mail.ibeaconlocationsystem.bluetooth.BluetoothScanManager;
import jp.ac.oit.elc.mail.ibeaconlocationsystem.wifi.WifiScanManager;

/**
 * Created by yuuki on 10/6/15.
 */
public class BeaconScanner{
    private static final long DEFAULT_SCAN_TIMEOUT_MILLIS = 3000;
    private BluetoothScanManager mBtScanManager;
    private WifiScanManager mWifiScanManager;
    private boolean mStarted = false;

    public BeaconScanner(Context context) {
        mWifiScanManager = new WifiScanManager(context);
        mBtScanManager = new BluetoothScanManager(context);
    }

    public void startScan(BeaconScanCallback callback) {
        if (mStarted) {
            return;
        }

        BeaconScanAsyncTask scanTask = new BeaconScanAsyncTask(DEFAULT_SCAN_TIMEOUT_MILLIS, callback);
        scanTask.execute();
    }


    private class BeaconScanAsyncTask extends AsyncTask<Void, BeaconList[], BeaconList[]>{
        private long mScanTimeoutMills;
        private BeaconScanCallback mCallback;
        private Date mLastCheckTime;
        public BeaconScanAsyncTask(long scanTimeoutMills, BeaconScanCallback callback){
            super();
            mScanTimeoutMills = scanTimeoutMills;
            mCallback = callback;
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            mCallback.onStartScan();
        }

        @Override
        protected void onPostExecute(BeaconList[] values) {
            super.onPostExecute(values);
            mCallback.onScanTimeout(values[0], values[1]);
        }

        @Override
        protected void onProgressUpdate(BeaconList[]... values) {
            super.onProgressUpdate(values);
            mCallback.onUpdateScanResult(values[0][0], values[0][1]);
        }

        @Override
        protected BeaconList[] doInBackground(Void... params) {
            mBtScanManager.startScan();
            mWifiScanManager.startScan();
            mLastCheckTime = new Date();
            if (mScanTimeoutMills == 0){
                while(!isCancelled()){
                    if(mLastCheckTime.before(mBtScanManager.getUpdatedTime()) || mLastCheckTime.before(mWifiScanManager.getUpdatedTime())){
                        BeaconList[] beacons = new BeaconList[2];
                        beacons[0] = mBtScanManager.getBeaconList();
                        beacons[1] = mWifiScanManager.getBeaconList();
                        mLastCheckTime = new Date();
                        publishProgress(beacons);
                    }
                }
            }else {
                try {
                    Thread.sleep(DEFAULT_SCAN_TIMEOUT_MILLIS);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                while(true){
                    if(mLastCheckTime.before(mBtScanManager.getUpdatedTime()) && mLastCheckTime.before(mWifiScanManager.getUpdatedTime())){
                        break;
                    }
                }
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
