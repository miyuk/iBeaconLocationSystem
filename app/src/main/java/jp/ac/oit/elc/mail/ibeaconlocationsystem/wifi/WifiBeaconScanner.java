package jp.ac.oit.elc.mail.ibeaconlocationsystem.wifi;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiManager;

import java.util.List;

import jp.ac.oit.elc.mail.ibeaconlocationsystem.BeaconList;

/**
 * Created by yuuki on 10/5/15.
 */
public class WifiBeaconScanner {
    private Context mContext;
    private WifiManager mWifiManager;
    private IntentFilter mWifiIntentFilter;
    private boolean mStarted;
    private OnScanListener mOnScanListener;

    public WifiBeaconScanner(Context context) {
        mContext = context.getApplicationContext();
        mWifiManager = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);
        mWifiIntentFilter = new IntentFilter(WifiManager.SCAN_RESULTS_AVAILABLE_ACTION);

    }

    public void startScan() {
        if (mStarted) {
            return;
        }
        mContext.registerReceiver(mWifiReceiver, mWifiIntentFilter);
        while (!mWifiManager.startScan()) ;
        if (mOnScanListener != null) {
            mOnScanListener.onStartScan();
        }
        mStarted = true;
    }

    public void stopScan() {
        if (!mStarted) {
            return;
        }
        mContext.unregisterReceiver(mWifiReceiver);
        mStarted = false;
    }

    public void setOnScanListener(OnScanListener listener) {
        mOnScanListener = listener;
    }

    private BroadcastReceiver mWifiReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            BeaconList<WifiBeacon> beaconList = new BeaconList<>();
            List<ScanResult> resultList = mWifiManager.getScanResults();
            for (ScanResult result : resultList) {
                WifiBeacon beacon = new WifiBeacon(result.SSID, result.BSSID, result.level);
                beaconList.add(beacon);
            }
            if (mOnScanListener != null) {
                mOnScanListener.onScan(beaconList);
            }
        }
    };

    public interface OnScanListener {
        void onStartScan();

        void onScan(BeaconList<WifiBeacon> beaconList);
    }
}
