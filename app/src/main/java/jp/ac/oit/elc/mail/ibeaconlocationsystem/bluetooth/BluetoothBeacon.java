package jp.ac.oit.elc.mail.ibeaconlocationsystem.bluetooth;

import android.graphics.Point;

import jp.ac.oit.elc.mail.ibeaconlocationsystem.Beacon;

/**
 * Created by yuuki on 10/2/15.
 */
public class BluetoothBeacon extends Beacon {

    private Point mPosition;

    public BluetoothBeacon(String macAddress, int rssi){
        super(macAddress, rssi);
    }
    public void setPosition(Point position){
        mPosition = position;
    }
    public Point getPosition(){
        return mPosition;
    }

}
