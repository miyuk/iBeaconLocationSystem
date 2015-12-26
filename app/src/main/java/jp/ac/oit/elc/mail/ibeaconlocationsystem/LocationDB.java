package jp.ac.oit.elc.mail.ibeaconlocationsystem;

import android.graphics.Point;
import android.graphics.Rect;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by yuuki on 11/6/15.
 */
public class LocationDB {
    private static Map<String, Point> sBeaconDb;
    private static Map<String, Rect> sRoomDb;

    static {
        sBeaconDb = new HashMap<>();
        sRoomDb = new HashMap<>();
        init();
    }

    private static void init() {
        sBeaconDb.put("B4:99:4C:4F:91:FC", new Point(1174, 968));
        sBeaconDb.put("B4:99:4C:4F:9F:A9", new Point(980, 1068));
        sBeaconDb.put("B4:99:4C:4F:8F:1F", new Point(1018, 970));
        sBeaconDb.put("B4:99:4C:4F:94:26", new Point(1110, 1030));
        sBeaconDb.put("B4:99:4C:4F:4D:6A", new Point(990, 844));
        sBeaconDb.put("B4:99:4C:4F:91:9A", new Point(970, 724));
        sBeaconDb.put("B4:99:4C:4F:8F:16", new Point(1068, 748));
        sBeaconDb.put("B4:99:4C:4F:97:11", new Point(1180, 824));
        sRoomDb.put("ROOM1", new Rect(919, 926, 1147, 1094));
        sRoomDb.put("ROOM2", new Rect(1027, 695, 1225, 884));
    }

    public static String getRoomId(int x, int y) {
        for (Map.Entry<String, Rect> entry : sRoomDb.entrySet()) {
            if (entry.getValue().contains(x, y)) {
                return entry.getKey();
            }
        }
        return "OUT_OF_ROOMS";
    }

    public static String getRoomId(Point pos) {
        return getRoomId(pos.x, pos.y);
    }

    public static List<String> getRoomIds() {
        List<String> result = new ArrayList(sRoomDb.keySet());
        result.add("OUT_OF_ROOMS");
        result.add("UNKNOWN");
        return result;
    }

    public static Point get(String mac) {
        if (!sBeaconDb.containsKey(mac)) {
            return null;
        }
        return sBeaconDb.get(mac);
    }
}
