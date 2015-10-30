package jp.ac.oit.elc.mail.ibeaconlocationsystem.bluetooth;

import jp.ac.oit.elc.mail.ibeaconlocationsystem.BeaconBase;

/**
 * Created by yuuki on 10/2/15.
 */
public class BluetoothBeacon extends BeaconBase {

    public BluetoothBeacon(String macAddress, int rssi){
        super(macAddress, rssi);
    }

}
