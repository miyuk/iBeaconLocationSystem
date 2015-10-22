package jp.ac.oit.elc.mail.ibeaconlocationsystem;

import static android.os.Environment.getExternalStorageDirectory;

/**
 * Created by yuuki on 10/22/15.
 */
public class Environment {
    public static final String APP_NAME = "iBeaconLocationSystem";
    public static final String APP_DIR = getExternalStorageDirectory() + "/iBeaconLocationSystem";
    public static final String INTENSITY_MAP_PATH = APP_DIR + "/intensity_map.csv";
    public static final String WEKA_HOME = APP_DIR + "/wekafiles";
}
