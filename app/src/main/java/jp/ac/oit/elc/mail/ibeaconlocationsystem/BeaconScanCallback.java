package jp.ac.oit.elc.mail.ibeaconlocationsystem;

import jp.ac.oit.elc.mail.ibeaconlocationsystem.bluetooth.BluetoothBeacon;
import jp.ac.oit.elc.mail.ibeaconlocationsystem.wifi.WifiBeacon;

/**
 * Created by yuuki on 10/6/15.
 */
public abstract class BeaconScanCallback {

    public void onStartScan() {

    }

    public void onUpdateScanResult(BeaconList<BluetoothBeacon> btBeacons, BeaconList<WifiBeacon> wifiBeacons) {

    }

    public void onScanTimeout(BeaconList<BluetoothBeacon> btBeacons, BeaconList<WifiBeacon> wifiBeacons) {

    }

    public void onScanFailed() {

    }

    public void onScanCancelled() {

    }


}
