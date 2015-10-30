package jp.ac.oit.elc.mail.ibeaconlocationsystem.classification;

import android.graphics.Point;

import java.util.ArrayList;
import java.util.List;

import jp.ac.oit.elc.mail.ibeaconlocationsystem.Sample;
import jp.ac.oit.elc.mail.ibeaconlocationsystem.SampleList;
import jp.ac.oit.elc.mail.ibeaconlocationsystem.bluetooth.BluetoothBeacon;
import weka.core.Attribute;
import weka.core.DenseInstance;
import weka.core.Instances;

/**
 * Created by yuuki on 10/21/15.
 */
public class LocationInstances extends Instances {
    private static final int INSTANCES_CAPACITY = 1000;
    private static final int OUT_OF_RANGE_RSSI = -70;
    private static final int LOWER_RSSI = -90;
    private static final int UPPER_RSSI = -20;

    public LocationInstances() {
        super("IntensityMap", new ArrayList<Attribute>(), INSTANCES_CAPACITY);
    }

    public boolean setDataSet(SampleList samples) {
        // config attribute
        List<String> posList = new ArrayList<>(); //nominal value of position
        for (Sample sample : samples) {
            for (BluetoothBeacon beacon : sample.getBtBeaconList()) {
                if (this.attribute(beacon.getMacAddress()) == null) {
                    this.insertAttributeAt(new Attribute(beacon.getMacAddress()), this.numAttributes());
                }
            }
            Point position = sample.getPosition();
            String pos = String.format("%d-%d", position.x, position.y);
            if (!posList.contains(pos)) {
                posList.add(pos);
            }
        }
        this.insertAttributeAt(new Attribute("location", posList), this.numAttributes());

        //set instance
        for (Sample sample : samples) {
            double[] vals = new double[this.numAttributes()];
            for (double val : vals) {
                val = -1.0;
            }
            for (BluetoothBeacon beacon : sample.getBtBeaconList()) {
                int index = this.attribute(beacon.getMacAddress()).index();
                vals[index] = convertRssiValue(beacon.getRssi());
            }
            Point position = sample.getPosition();
            String tag = String.format("%d-%d", position.x, position.y);
            Attribute attr = this.attribute("location");
            vals[attr.index()] = attr.indexOfValue(tag);
            this.add(new DenseInstance(1.0, vals));
        }
        this.setClass(attribute("location"));
        return true;
    }

    private static double convertRssiValue(int rssi) {
        if (rssi < OUT_OF_RANGE_RSSI) {
            return -1.0;
        }
        return (2 * rssi - (UPPER_RSSI + LOWER_RSSI)) / (UPPER_RSSI - LOWER_RSSI);
    }
}
