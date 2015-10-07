package jp.ac.oit.elc.mail.ibeaconlocationsystem;

/**
 * Created by yuuki on 10/6/15.
 */
public interface BeaconScanCallback {

    public void onStartScan();
    public void onScanned(BeaconList<BluetoothBeacon> btBeaconList, BeaconList<WifiBeacon> wifiBeaconList);
    public void onScanFailed();
}
