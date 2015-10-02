package jp.ac.oit.elc.mail.ibeaconlocationsystem;

/**
 * Created by yuuki on 10/2/15.
 */
public class WifiBeaconInfo extends BeaconInfo{

    private String mSsid;
    public WifiBeaconInfo(String ssid, String macAddress, int rssi){
        super(macAddress, rssi);
        mSsid = ssid;
    }

    public String getSsid(){
        return mSsid;
    }
}
