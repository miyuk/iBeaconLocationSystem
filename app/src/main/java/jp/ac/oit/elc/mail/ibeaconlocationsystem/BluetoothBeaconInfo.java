package jp.ac.oit.elc.mail.ibeaconlocationsystem;

import android.os.Parcel;
import android.os.ParcelUuid;

/**
 * Created by yuuki on 10/2/15.
 */
public class BluetoothBeaconInfo extends BeaconInfo {

    public BluetoothBeaconInfo(String macAddress, int rssi){
        super(macAddress, rssi);
    }

}
