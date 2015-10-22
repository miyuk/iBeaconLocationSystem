package jp.ac.oit.elc.mail.ibeaconlocationsystem.wifi;

import jp.ac.oit.elc.mail.ibeaconlocationsystem.BaseBeacon;

/**
 * Created by yuuki on 10/2/15.
 */
public class WifiBeacon extends BaseBeacon {

    private String mSsid;

    public WifiBeacon(String ssid, String macAddress, int rssi) {
        super(macAddress, rssi);
        mSsid = ssid;
    }

    public String getSsid() {
        return mSsid;
    }
}
