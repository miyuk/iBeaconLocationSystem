package jp.ac.oit.elc.mail.ibeaconlocationsystem.wifi;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiManager;
import android.util.Log;

import java.util.List;

import jp.ac.oit.elc.mail.ibeaconlocationsystem.BeaconList;

/**
 * Created by yuuki on 10/5/15.
 */
public class WifiScanManager extends BroadcastReceiver {
    private static final String TAG = WifiScanManager.class.getSimpleName();
    private WifiManager mWifiManager;
    private BeaconList<WifiBeacon> mWifiBeaconList;
    private boolean mIsScanning;
    private Context mContext;
    private IntentFilter mWifiFilter;

    public WifiScanManager(Context context) {
        super();
        mContext = context;
        mWifiManager = (WifiManager)context.getSystemService(Context.WIFI_SERVICE);
        mIsScanning = false;
        mWifiFilter = new IntentFilter(WifiManager.SCAN_RESULTS_AVAILABLE_ACTION);

    }

    @Override
    public void onReceive(Context context, Intent intent) {
        if (!mIsScanning) return;
        Log.d(TAG, "onReceive");
        List<ScanResult> resultList = mWifiManager.getScanResults();
        for (ScanResult result : resultList) {
            WifiBeacon beacon;
            if ((beacon = mWifiBeaconList.getByMacAddress(result.BSSID)) != null) {
                beacon.update(result.level);
                Log.i(TAG, "update Wifi beacon: " + result.BSSID + "(" + result.level + ")");
                if (!result.SSID.equals(beacon.getSsid())) {
                    Log.w(TAG, String.format("different SSID about %s", result.BSSID));
                    Log.w(TAG, String.format("%s <--> %s", result.SSID, beacon.getSsid()));
                }
            } else {
                // if not existing in List
                beacon = new WifiBeacon(result.SSID, result.BSSID, result.level);
                mWifiBeaconList.add(beacon);
                Log.d(TAG, "add WiFi beacon: " + result.BSSID);
            }
        }
        //mWifiManager.startScan();
    }

    public void startScan() {
        mIsScanning = true;
        mWifiBeaconList = new BeaconList<>();
        mContext.registerReceiver(this, mWifiFilter);
        mWifiManager.startScan();
    }

    public void stopScan() {
        mContext.unregisterReceiver(this);
        mIsScanning = false;
    }

    public BeaconList<WifiBeacon> getBeaconList() {
        return mWifiBeaconList;
    }

    public boolean isScanning() {
        return mIsScanning;
    }
}
