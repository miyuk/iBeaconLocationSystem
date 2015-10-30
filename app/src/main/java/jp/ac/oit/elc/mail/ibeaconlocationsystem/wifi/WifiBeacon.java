package jp.ac.oit.elc.mail.ibeaconlocationsystem.wifi;

import jp.ac.oit.elc.mail.ibeaconlocationsystem.BeaconBase;

/**
 * Created by yuuki on 10/2/15.
 */
public class WifiBeacon extends BeaconBase {

    private String mSsid;

    public WifiBeacon(String ssid, String macAddress, int rssi) {
        super(macAddress, rssi);
        mSsid = ssid;
    }

    public String getSsid() {
        return mSsid;
    }
}
