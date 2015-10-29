package jp.ac.oit.elc.mail.ibeaconlocationsystem;

import static android.os.Environment.getExternalStorageDirectory;

/**
 * Created by yuuki on 10/22/15.
 */
public class Environment {
    public static final String APP_NAME = "iBeaconLocationSystem";
    public static final String APP_DIR = getExternalStorageDirectory() + "/" + APP_NAME;
    public static final String BT_INTENSITY_MAP_PATH = APP_DIR + "/training_bt.csv";
    public static final String WIFI_INTENSITY_MAP_PATH = APP_DIR + "/training_wifi.csv";
    public static final String WEKA_HOME = APP_DIR + "/wekafiles";
}
