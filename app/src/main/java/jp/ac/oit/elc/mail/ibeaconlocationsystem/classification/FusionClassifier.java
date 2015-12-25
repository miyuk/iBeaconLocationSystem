package jp.ac.oit.elc.mail.ibeaconlocationsystem.classification;

import android.graphics.Point;

import org.w3c.dom.Attr;

import java.util.ArrayList;
import java.util.List;

import jp.ac.oit.elc.mail.ibeaconlocationsystem.Sample;
import weka.classifiers.functions.MultilayerPerceptron;
import weka.core.Attribute;
import weka.core.DenseInstance;
import weka.core.Instance;
import weka.core.Instances;
import weka.core.Utils;

/**
 * Created by yuuki on 11/12/15.
 */
public class FusionClassifier extends MultilayerPerceptron {
    private Instances m_Instances;

    public void buildClassifier(List<Point[]> trainingData) throws Exception {
        ArrayList<Attribute> attrs = new ArrayList<>();
        attrs.add(new Attribute("BT_X"));
        attrs.add(new Attribute("BT_Y"));
        attrs.add(new Attribute("WIFI_X"));
        attrs.add(new Attribute("WIFI_Y"));
        ArrayList<String> clsAttrs = new ArrayList<>();
        for (Point[] points : trainingData) {
            String cat = formatPosition(points[2]);
            if (!clsAttrs.contains(cat)) {
                clsAttrs.add(cat);
            }
        }
        attrs.add(new Attribute("CLASS", clsAttrs));
        m_Instances = new Instances("FUSION", attrs, 1000);
        m_Instances.setClass(m_Instances.attribute("CLASSØ"));

        for (Point[] points : trainingData) {
            m_Instances.add(makeInstance(points[0], points[1], points[2]));
        }
        buildClassifier(m_Instances);
    }

    public Point estimatePosition(Point btPos, Point wifiPos) throws Exception {
        Instance instance = makeInstance(btPos, wifiPos, null);
        return estimatePosition(instance);

    }

    public Point estimatePosition(Instance instance) throws Exception {
        double[] pValues = distributionForInstance(instance);
        double x = 0, y = 0;
        for (int i = 0; i < pValues.length; i++) {
            Point pos = parsePosition(instance.classAttribute().value(i));
            x += pValues[i] * pos.x;
            y += pValues[i] * pos.y;
        }
        return new Point((int) Math.round(x), (int) Math.round(y));
    }

    public Instance makeInstance(Point btPos, Point wifiPos, Point truePos) {
        double[] values = new double[m_Instances.numAttributes()];
        values[m_Instances.attribute("BT_X").index()] = btPos.x;
        values[m_Instances.attribute("BT_Y").index()] = btPos.x;
        values[m_Instances.attribute("WIFI_X").index()] = wifiPos.y;
        values[m_Instances.attribute("WIFI_Y").index()] = wifiPos.y;
        Attribute clsAttr = m_Instances.classAttribute();
        if (truePos == null) {
            values[clsAttr.index()] = Utils.missingValue();
        } else {
            values[clsAttr.index()] = clsAttr.indexOfValue(formatPosition(truePos));
        }
        Instance instance = new DenseInstance(1.0, values);
        instance.setDataset(m_Instances);
        return instance;
    }

    protected static Point parsePosition(String str) {
        String[] split = str.split("-");
        int x = Integer.parseInt(split[0]);
        int y = Integer.parseInt(split[1]);
        return new Point(x, y);
    }

    protected static String formatPosition(Point position) {
        return String.format("%d-%d", position.x, position.y);
    }
}
