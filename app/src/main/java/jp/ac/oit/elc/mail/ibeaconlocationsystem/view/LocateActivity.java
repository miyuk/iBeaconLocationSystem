package jp.ac.oit.elc.mail.ibeaconlocationsystem.view;

import android.os.Environment;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.TextView;
import android.widget.Toast;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.List;
import java.util.StringTokenizer;

import jp.ac.oit.elc.mail.ibeaconlocationsystem.BeaconList;
import jp.ac.oit.elc.mail.ibeaconlocationsystem.IntensitySample;
import jp.ac.oit.elc.mail.ibeaconlocationsystem.R;
import jp.ac.oit.elc.mail.ibeaconlocationsystem.bluetooth.BluetoothBeacon;

public class LocateActivity extends AppCompatActivity {
    private static final String TAG = LocateActivity.class.getSimpleName();
    private static final String INTENSITY_MAP_FILE = "/local/intensity_map.csv";
    private IntensityMapView mIntensityMapView;
    private TextView mTextStatus;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_locate);
        initViews();
        loadIntensityMap();
    }
    private void initViews(){
        mIntensityMapView = (IntensityMapView)findViewById(R.id.intensityMapView);
        mTextStatus = (TextView)findViewById(R.id.textStatus);
        mIntensityMapView.setImageResource(R.mipmap.floor_map);
    }
    private void loadIntensityMap(){
        File file = new File(Environment.getExternalStorageDirectory() + INTENSITY_MAP_FILE);
        if(!file.exists()){
            Toast.makeText(this, "File not found", Toast.LENGTH_SHORT).show();
            return;
        }
        try {
            BufferedReader reader = new BufferedReader(new FileReader(file));
            String line;
            while((line = reader.readLine()) != null){
                StringTokenizer token = new StringTokenizer(line, ",");
                int x = Integer.parseInt(token.nextToken());
                int y = Integer.parseInt(token.nextToken());
                BeaconList<BluetoothBeacon> beaconList = new BeaconList<>();
                while(token.countTokens() >= 2){
                    String mac = token.nextToken();
                    int rssi = Integer.parseInt(token.nextToken());
                    beaconList.add(new BluetoothBeacon(mac, rssi));
                }
                mIntensityMapView.setPinPosition(x, y);
                mIntensityMapView.sample(beaconList, null);
            }
            Toast.makeText(this, "Load Complete", Toast.LENGTH_SHORT).show();
        }catch (Exception e){
            e.printStackTrace();
        }
    }
}
