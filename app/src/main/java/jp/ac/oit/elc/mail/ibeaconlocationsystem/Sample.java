package jp.ac.oit.elc.mail.ibeaconlocationsystem;

import android.graphics.Point;

import jp.ac.oit.elc.mail.ibeaconlocationsystem.bluetooth.BluetoothBeacon;
import jp.ac.oit.elc.mail.ibeaconlocationsystem.wifi.WifiBeacon;

/**
 * Created by yuuki on 10/6/15.
 */
public class Sample {
    private Point mPosition;
    private BeaconList<BluetoothBeacon> mBtBeaconList;
    private BeaconList<WifiBeacon> mWifiBeaconList;

    public Sample(int x, int y, BeaconList<BluetoothBeacon> btBeaconList, BeaconList<WifiBeacon> wifiBeaconList) {
        mPosition = new Point(x, y);
        mBtBeaconList = btBeaconList;
        mWifiBeaconList = wifiBeaconList;
    }

    public Point getPosition() {
        return mPosition;
    }

    public BeaconList<BluetoothBeacon> getBtBeaconList() {
        return mBtBeaconList;
    }

    public BeaconList<WifiBeacon> getWifiBeaconList() {
        return mWifiBeaconList;
    }
}
