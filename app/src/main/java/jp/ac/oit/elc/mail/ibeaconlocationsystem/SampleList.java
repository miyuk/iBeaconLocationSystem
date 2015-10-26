package jp.ac.oit.elc.mail.ibeaconlocationsystem;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.util.ArrayList;
import java.util.StringTokenizer;

import jp.ac.oit.elc.mail.ibeaconlocationsystem.bluetooth.BluetoothBeacon;

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

    public boolean save(String path) {
        File csv = new File(path);
        csv.getParentFile().mkdirs();
        if (!csv.exists()) {
            try {
                csv.createNewFile();
            } catch (Exception e) {
                e.printStackTrace();
                return false;
            }
        }
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(csv, false))) {
            for (Sample sample : this) {
                StringBuffer buffer = new StringBuffer();
                buffer.append(String.format("%d,%d", sample.x, sample.y));
                for (BluetoothBeacon beacon : sample.getBtBeaconList()) {
                    buffer.append(String.format(",%s,%d", beacon.getMacAddress(), beacon.getRssi()));
                }
                buffer.append("\n");
                writer.write(buffer.toString());
            }
            writer.flush();
            writer.close();
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }
}
