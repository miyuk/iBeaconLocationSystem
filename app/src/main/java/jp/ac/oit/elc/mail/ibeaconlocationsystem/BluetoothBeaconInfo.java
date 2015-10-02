package jp.ac.oit.elc.mail.ibeaconlocationsystem;

import android.os.Parcel;
import android.os.ParcelUuid;

/**
 * Created by yuuki on 10/2/15.
 */
public class BluetoothBeaconInfo extends BeaconInfo {
    private String mName;
    private ParcelUuid[] mUuid;

    public BluetoothBeaconInfo(String name, ParcelUuid[] uuid, String macAddress, int rssi){
        super(macAddress, rssi);
        mName = name;
        mUuid = uuid;
    }

    public String getName(){
        return mName;
    }
    public ParcelUuid[] getUuid(){
        return mUuid;
    }
}
