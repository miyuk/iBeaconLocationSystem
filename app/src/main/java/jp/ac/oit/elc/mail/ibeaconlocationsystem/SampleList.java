package jp.ac.oit.elc.mail.ibeaconlocationsystem;

import android.graphics.Point;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import jp.ac.oit.elc.mail.ibeaconlocationsystem.bluetooth.BluetoothBeacon;
import jp.ac.oit.elc.mail.ibeaconlocationsystem.wifi.WifiBeacon;

/**
 * Created by yuuki on 10/21/15.
 */
public class SampleList extends ArrayList<Sample> {
    public SampleList() {
        super();
    }

    public static SampleList loadFromCsv(String btPath, String wifiPath) {
        SampleList result = new SampleList();
        String[][] btCsv = loadCsv(btPath);
        String[][] wifiCsv = loadCsv(wifiPath);
        if(btCsv == null || wifiCsv == null){
            return null;
        }
        if(btCsv.length != wifiCsv.length){
            return null;
        }
        for(int i = 0; i < btCsv.length; i++){
            String[] btLine = btCsv[i];
            String[] wifiLine = wifiCsv[i];
            if(!btLine[0].equals(wifiLine[0]) || !btLine[1].equals(wifiLine[1])){
                continue;
            }
            int x = Integer.parseInt(btLine[0]);
            int y = Integer.parseInt(btLine[1]);
            BeaconList<BluetoothBeacon> btList = new BeaconList<>();
            BeaconList<WifiBeacon> wifiList = new BeaconList<>();
            for(int j = 2; j < btLine.length; j += 2){
                String btMac = btLine[j];
                int btRssi = Integer.parseInt(btLine[j + 1]);
                btList.add(new BluetoothBeacon(btMac, btRssi));
            }
            for(int j = 2; j < wifiLine.length; j += 2){
                String wifiMac = wifiLine[j];
                int wifiRssi = Integer.parseInt(wifiLine[j + 1]);
                wifiList.add(new WifiBeacon(null, wifiMac, wifiRssi));
            }
            Sample sample = new Sample(x, y, btList, wifiList);
            result.add(sample);
        }
        return result;
    }

    private static String[][] loadCsv(String path){
        File file = new File(path);
        if (!file.exists()) {
            return null;
        }
        List<String[]> result = new ArrayList<>();
        try(BufferedReader reader = new BufferedReader(new FileReader(file))){
            String line;
            while((line = reader.readLine()) != null){
                result.add(line.split(","));
            }
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
        return result.toArray(new String[][]{});
    }
    public List<Point> getPositions(){
        List<Point> result = new ArrayList<>();
        for (Sample sample : this){
            Point position = sample.getPosition();
            if (!result.contains(position)){
                result.add(position);
            }
        }
        return result;
    }
    public static List<String> getMacAddresses(BeaconList<Beacon> list){
        List<String> result = new ArrayList<>();
        for (Beacon beacon : list){
            String mac = beacon.getMacAddress();
            if(!result.contains(mac)){
                result.add(mac);
            }
        }
        return result;
    }
    public boolean saveToCsv(String btPath, String wifiPath) {
        StringBuffer btBuffer = new StringBuffer();
        StringBuffer wifiBuffer = new StringBuffer();
        for (Sample sample : this) {
            Point position = sample.getPosition();
            btBuffer.append(String.format("%d,%d", position.x, position.y));
            for (BluetoothBeacon beacon : sample.getBtBeaconList()) {
                btBuffer.append(String.format(",%s,%d", beacon.getMacAddress(), beacon.getRssi()));
            }
            btBuffer.append("\n");

            wifiBuffer.append(String.format("%d,%d", position.x, position.y));
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
