package jp.ac.oit.elc.mail.ibeaconlocationsystem.classification;

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
    private static final int DEFAULT_INSTANCES_CAPACITY = 1000;
    public LocationInstances(){
        super("IntensityMap", new ArrayList<Attribute>(), DEFAULT_INSTANCES_CAPACITY);
    }

    public boolean load(SampleList samples){
        //build attribute
        List<String> posList = new ArrayList<>(); //nominal value of position
        for (Sample sample : samples) {
            for (BluetoothBeacon beacon : sample.getBtBeaconList()) {
                if (this.attribute(beacon.getMacAddress()) == null) {
                    this.insertAttributeAt(new Attribute(beacon.getMacAddress()), this.numAttributes());
                }
            }
            String pos = String.format("%d-%d", sample.x, sample.y);
            if (!posList.contains(pos)) {
                posList.add(pos);
            }
        }
        this.insertAttributeAt(new Attribute("location", posList), this.numAttributes());

        //set instance
        for (Sample sample : samples) {
            double[] vals = new double[this.numAttributes()];
            for (BluetoothBeacon beacon : sample.getBtBeaconList()) {
                int index = this.attribute(beacon.getMacAddress()).index();
                vals[index] = beacon.getRssi();
            }
            String tag = String.format("%d-%d", sample.x, sample.y);
            Attribute attr = this.attribute("location");
            vals[attr.index()] = attr.indexOfValue(tag);
            this.add(new DenseInstance(1.0, vals));
        }
        this.setClass(attribute("location"));
        return true;
    }

}
