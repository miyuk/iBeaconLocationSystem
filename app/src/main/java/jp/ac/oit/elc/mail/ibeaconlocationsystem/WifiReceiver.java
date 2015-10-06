package jp.ac.oit.elc.mail.ibeaconlocationsystem;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiManager;
import android.util.Log;

import java.util.List;

/**
 * Created by yuuki on 10/5/15.
 */
public class WifiReceiver extends BroadcastReceiver {
    private static final String TAG = "WifiReceiver";
    private WifiManager mWifiManager;
    private BeaconList<WifiBeaconInfo> mWifiBeaconList;

    public WifiReceiver(WifiManager wifiManager, BeaconList<WifiBeaconInfo> wifiBeaconList) {
        super();
        mWifiManager = wifiManager;
        mWifiBeaconList = wifiBeaconList;
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        Log.d(TAG, "onReceive");
        List<ScanResult> resultList = mWifiManager.getScanResults();
        for (ScanResult result : resultList) {
            WifiBeaconInfo beacon;
            if ((beacon = mWifiBeaconList.getByMacAddress(result.BSSID)) != null) {
                beacon.update(result.level);
                Log.i(TAG, "update Wifi beacon: " + result.BSSID + "(" + result.level + ")");
                if (!result.SSID.equals(beacon.getSsid())) {
                    Log.w(TAG, String.format("different SSID about %s", result.BSSID));
                    Log.w(TAG, String.format("%s <--> %s", result.SSID, beacon.getSsid()));
                }
            } else {
                // if not existing in List
                beacon = new WifiBeaconInfo(result.SSID, result.BSSID, result.level);
                mWifiBeaconList.add(beacon);
                Log.d(TAG, "add WiFi beacon: " + result.BSSID);
            }
        }
    }

}
