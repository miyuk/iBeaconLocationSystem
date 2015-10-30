package jp.ac.oit.elc.mail.ibeaconlocationsystem;

import static android.os.Environment.getExternalStorageDirectory;

/**
 * Created by yuuki on 10/22/15.
 */
public class Environment {
    public static final String APP_NAME = "iBeaconLocationSystem";
    public static final String APP_DIR = getExternalStorageDirectory() + "/" + APP_NAME;
    public static final String BT_TRAINING_CSV = APP_DIR + "/training_bt.csv";
    public static final String WIFI_TRAINING_CSV = APP_DIR + "/training_wifi.csv";
    public static final String BT_EVALUATION_CSV = APP_DIR + "/evaluation_bt.csv";
    public static final String WIFI_EVALUATION_CSV = APP_DIR + "/evaluation_wifi.csv";
    public static final String WEKA_HOME = APP_DIR + "/wekafiles";
}