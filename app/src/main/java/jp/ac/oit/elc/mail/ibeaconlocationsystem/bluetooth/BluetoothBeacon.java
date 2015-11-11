package jp.ac.oit.elc.mail.ibeaconlocationsystem.bluetooth;

import jp.ac.oit.elc.mail.ibeaconlocationsystem.Beacon;

/**
 * Created by yuuki on 10/2/15.
 */
public class BluetoothBeacon extends Beacon {

    public BluetoothBeacon(String macAddress, int rssi){
        super(macAddress, rssi);
    }

}
