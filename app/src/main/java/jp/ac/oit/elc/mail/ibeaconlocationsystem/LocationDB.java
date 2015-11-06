package jp.ac.oit.elc.mail.ibeaconlocationsystem;

import android.graphics.Point;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by yuuki on 11/6/15.
 */
public class LocationDB{
    private static Map<String, Point> mDb;
    static{
        mDb = new HashMap<>();
        init();
    }
    private static void init(){
        mDb.put("B4:99:4C:4F:91:FC", new Point(1174, 968));
        mDb.put("B4:99:4C:4F:9F:A9", new Point(980, 1068));
        mDb.put("B4:99:4C:4F:8F:1F", new Point(1018, 970));
        mDb.put("B4:99:4C:4F:94:26", new Point(1110, 1030));
        mDb.put("B4:99:4C:4F:4D:6A", new Point(990, 844));
        mDb.put("B4:99:4C:4F:91:9A", new Point(970, 724));
        mDb.put("B4:99:4C:4F:8F:16", new Point(1068, 748));
        mDb.put("B4:99:4C:4F:97:11", new Point(1180, 824));
    }
    public static Point get(String mac){
        if(!mDb.containsKey(mac)){
            return null;
        }
        return mDb.get(mac);
    }
}
