package jp.ac.oit.elc.mail.ibeaconlocationsystem;

import java.util.Date;
import java.util.HashSet;

/**
 * Created by yuuki on 10/6/15.
 */
public class BeaconList<T extends BeaconBase> extends HashSet<T> {

    public T getByMacAddress(String macAddress) {
        for (T item : this) {
            if (item.getMacAddress().equals(macAddress)) {
                return item;
            }
        }
        // if no existing in List
        return null;
    }


    public Date getUpdatedTime() {
        Date result = new Date(0);
        for (T beacon : this) {
            Date time = beacon.getUpdatedTime();
            if (time.after(result)) {
                result = time;
            }
        }
        return result;
    }


}
