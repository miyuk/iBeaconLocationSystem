package jp.ac.oit.elc.mail.ibeaconlocationsystem;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.StringTokenizer;

import jp.ac.oit.elc.mail.ibeaconlocationsystem.bluetooth.BluetoothBeacon;
import jp.ac.oit.elc.mail.ibeaconlocationsystem.wifi.WifiBeacon;

/**
 * Created by yuuki on 10/21/15.
 */
public class SampleList extends ArrayList<Sample> {
    public SampleList() {
        super();
    }

    public static SampleList load(String path) {
        File csv = new File(path);
        if (!csv.exists()) {
            return null;
        }
        SampleList result = new SampleList();
        try (BufferedReader reader = new BufferedReader(new FileReader(csv))) {
            String line;
            while ((line = reader.readLine()) != null) {
                StringTokenizer token = new StringTokenizer(line, ",");
                int x = Integer.parseInt(token.nextToken());
                int y = Integer.parseInt(token.nextToken());
                BeaconList<BluetoothBeacon> beaconList = new BeaconList<>();
                while (token.countTokens() >= 2) {
                    String mac = token.nextToken();
                    int rssi = Integer.parseInt(token.nextToken());
                    beaconList.add(new BluetoothBeacon(mac, rssi));
                }
                Sample sample = new Sample(x, y, beaconList, null);
                result.add(sample);
            }
            return result;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public boolean saveToCsv(String btPath, String wifiPath) {
        StringBuffer btBuffer = new StringBuffer();
        StringBuffer wifiBuffer = new StringBuffer();
        for (Sample sample : this) {
            btBuffer.append(String.format("%d,%d", sample.x, sample.y));
            for (BluetoothBeacon beacon : sample.getBtBeaconList()) {
                btBuffer.append(String.format(",%s,%d", beacon.getMacAddress(), beacon.getRssi()));
            }
            btBuffer.append("\n");

            wifiBuffer.append(String.format("%d,%d", sample.x, sample.y));
            for (WifiBeacon beacon : sample.getWifiBeaconList()) {
                wifiBuffer.append(String.format(",%s,%d", beacon.getMacAddress(), beacon.getRssi()));
            }
            wifiBuffer.append("\n");
        }
        return save(btPath, btBuffer.toString(), false) && save(wifiPath, wifiBuffer.toString(), false);
    }

    private static boolean save(String path, String text, boolean append) {
        File file = new File(path);
        if (!file.exists()) {
            try {
                file.getParentFile().mkdirs();
                file.createNewFile();
            } catch (IOException e) {
                e.printStackTrace();
                return false;
            }
        }
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(file, append))) {
            writer.write(text);
            writer.flush();
            writer.close();
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
        return true;
    }
}
