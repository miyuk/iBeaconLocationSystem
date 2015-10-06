package jp.ac.oit.elc.mail.ibeaconlocationsystem;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by yuuki on 10/6/15.
 */
public class IntensityMap {
    public static final String TAG = "IntensityMap";
    private List<Sampling> mSamplingList;

    public IntensityMap(){
        mSamplingList = new ArrayList<>();
    }

    public void sample(int x, int y, BeaconList<BluetoothBeacon> btBeaconList, BeaconList<WifiBeacon> wifiBeaconList){

    }
}
