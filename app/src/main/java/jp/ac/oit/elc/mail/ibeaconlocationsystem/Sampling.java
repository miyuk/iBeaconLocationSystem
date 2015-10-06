package jp.ac.oit.elc.mail.ibeaconlocationsystem;

/**
 * Created by yuuki on 10/6/15.
 */
public class Sampling {
    public int x;
    public int y;
    private BeaconList<BluetoothBeacon> mBtBeaconList;
    private BeaconList<WifiBeacon> mWifiBeaconList;

    public Sampling(int x, int y, BeaconList<BluetoothBeacon> btBeaconList, BeaconList<WifiBeacon> wifiBeaconList) {
        this.x = x;
        this.y = y;
        mBtBeaconList = btBeaconList;
        mWifiBeaconList = wifiBeaconList;
    }

    public BeaconList<BluetoothBeacon> getBtBeaconList(){
        return mBtBeaconList;
    }
    public BeaconList<WifiBeacon> getWifiBeaconList(){
        return mWifiBeaconList;
    }
}
