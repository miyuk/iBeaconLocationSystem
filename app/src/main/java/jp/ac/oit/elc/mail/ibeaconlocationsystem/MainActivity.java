package jp.ac.oit.elc.mail.ibeaconlocationsystem;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.le.BluetoothLeScanner;
import android.net.wifi.WifiManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = "MainActivity";
    private BluetoothAdapter mBtAdapter;
    private BluetoothLeScanner mBtScanner;
    private WifiManager mWifiManager;
    private List<BluetoothBeaconInfo> mBtBeaconList;
    private List<WifiBeaconInfo> mWifiBeaconList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mBtAdapter = BluetoothAdapter.getDefaultAdapter();
        mBtScanner = mBtAdapter.getBluetoothLeScanner();
        mWifiManager = (WifiManager)getSystemService(WIFI_SERVICE);
        mBtBeaconList = new ArrayList<>();
        mWifiBeaconList = new ArrayList<>();
    }
}
