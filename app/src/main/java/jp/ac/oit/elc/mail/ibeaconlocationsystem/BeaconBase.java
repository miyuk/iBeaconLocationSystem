package jp.ac.oit.elc.mail.ibeaconlocationsystem;

import java.util.Date;

/**
 * Created by yuuki on 10/2/15.
 */
public abstract class BeaconBase {
    protected String mMacAddress;
    protected int mRssi;
    protected Date mUpdatedTime;

    public BeaconBase(String macAddress, int rssi) {
        mMacAddress = macAddress;
        mRssi = rssi;
        mUpdatedTime = new Date();
    }

    public String getMacAddress() {
        return mMacAddress;
    }

    public int getRssi() {
        return mRssi;
    }

    public Date getUpdatedTime() {
        return mUpdatedTime;
    }

    public void update(int rssi) {
        mRssi = rssi;
        mUpdatedTime = new Date();
    }

    @Override
    public boolean equals(Object o) {
        return mMacAddress.equals(o);
    }
}
