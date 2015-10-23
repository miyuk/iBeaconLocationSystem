package jp.ac.oit.elc.mail.ibeaconlocationsystem;

import java.util.Date;

/**
 * Created by yuuki on 10/2/15.
 */
public abstract class BeaconBase {
    protected String mMacAddress;
    protected int mRssi;
    protected Date mLastUpdateTime;

    public BeaconBase(String macAddress, int rssi) {
        mMacAddress = macAddress;
        mRssi = rssi;
        mLastUpdateTime = new Date();
    }

    public String getMacAddress() {
        return mMacAddress;
    }

    public int getRssi() {
        return mRssi;
    }

    public Date getLastUpdateTime() {
        return mLastUpdateTime;
    }

    public void update(int rssi) {
        mRssi = rssi;
        mLastUpdateTime = new Date();
    }
}
