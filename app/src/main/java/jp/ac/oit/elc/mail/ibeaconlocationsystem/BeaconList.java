package jp.ac.oit.elc.mail.ibeaconlocationsystem;

import java.util.ArrayList;

/**
 * Created by yuuki on 10/6/15.
 */
public class BeaconList<T extends BaseBeacon> extends ArrayList<T> {
    public T getByMacAddress(String macAddress) {
        for (T item : this) {
            if (item.getMacAddress().equals(macAddress)) {
                return item;
            }
        }
        // if no existing in List
        return null;
    }
}
