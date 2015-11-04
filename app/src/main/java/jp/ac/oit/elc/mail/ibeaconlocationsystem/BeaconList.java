package jp.ac.oit.elc.mail.ibeaconlocationsystem;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;

/**
 * Created by yuuki on 10/6/15.
 */
public class BeaconList<T extends BeaconBase> extends ArrayList<T> {

    public T getByMacAddress(String macAddress) {
        for (T item : this) {
            if (item.getMacAddress().equals(macAddress)) {
                return item;
            }
        }
        // if no existing in List
        return null;
    }
    public T put(T beacon){
        T item = getByMacAddress(beacon.getMacAddress());
        if(item == null){
            add(beacon);
        }else{
            item.update(beacon.getRssi(), beacon.getUpdatedTime());
        }
       return item;
    }

    public BeaconList<T> getLatestBeacons(Date limit){
        if(limit == null){
            return this;
        }
        BeaconList<T> result = new BeaconList<>();
        for (T item : this){
            if(item.getUpdatedTime().after(limit)){
                result.add(item);
            }
        }
        return result;
    }
}
