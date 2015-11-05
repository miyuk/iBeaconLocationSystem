package jp.ac.oit.elc.mail.ibeaconlocationsystem.wifi;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiManager;
import android.util.Log;

import java.util.Date;
import java.util.List;

import jp.ac.oit.elc.mail.ibeaconlocationsystem.BeaconList;

/**
 * Created by yuuki on 10/5/15.
 */
public class WifiBeaconScanner {
    private static final String TAG = WifiBeaconScanner.class.getSimpleName();
    private Context mContext;
    private WifiManager mWifiManager;
    private IntentFilter mWifiIntentFilter;
    private boolean mStarted;
    private Date mUpdateTime;
    private OnScanListener mOnScanListener;
    private BeaconList<WifiBeacon> mBuffer;

    public WifiBeaconScanner(Context context) {
        mContext = context.getApplicationContext();
        mWifiManager = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);
        mWifiIntentFilter = new IntentFilter(WifiManager.SCAN_RESULTS_AVAILABLE_ACTION);

    }

    public void startScan() {
        if (mStarted) {
            return;
        }
        mBuffer = new BeaconList<>();
        mContext.registerReceiver(mWifiReceiver, mWifiIntentFilter);
        requestScan();
        if (mOnScanListener != null) {
            mOnScanListener.onStartScan();
        }
        mStarted = true;
    }

    public void stopScan() {
        if (!mStarted) {
            return;
        }
        mUpdateTime = null;
        mContext.unregisterReceiver(mWifiReceiver);
        mStarted = false;
    }

    private void requestScan(){
        new Thread(new Runnable() {
            @Override
            public void run() {
                while (!mWifiManager.startScan()){
                    try{
                        Thread.sleep(100);
                    }catch(InterruptedException e){
                        e.printStackTrace();
                        break;
                    }
                }
            }
        }).start();
    }
    public void setOnScanListener(OnScanListener listener) {
        mOnScanListener = listener;
    }

    private BroadcastReceiver mWifiReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            List<ScanResult> resultList = mWifiManager.getScanResults();
            Log.d(TAG, String.format("Wifi scanned beacons: %d", resultList.size()));
            BeaconList<WifiBeacon> beaconList = new BeaconList<>();
            for (ScanResult result : resultList) {
                WifiBeacon beacon = new WifiBeacon(result.SSID, result.BSSID, result.level);
                beaconList.add(beacon);
                mBuffer.put(beacon);
            }
            mUpdateTime = new Date();
            if (mOnScanListener != null) {
                mOnScanListener.onScan(beaconList);
            }
            requestScan();
        }
    };

    public Date getUpdateTime(){
        return mUpdateTime;
    }
    public BeaconList<WifiBeacon> getBeaconBuffer(){
        return mBuffer;
    }
    public interface OnScanListener {
        void onStartScan();

        void onScan(BeaconList<WifiBeacon> beaconList);
    }
}
