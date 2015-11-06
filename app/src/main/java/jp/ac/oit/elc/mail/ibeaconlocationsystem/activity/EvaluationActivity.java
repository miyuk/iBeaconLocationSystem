package jp.ac.oit.elc.mail.ibeaconlocationsystem.activity;

import android.app.ProgressDialog;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Point;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import jp.ac.oit.elc.mail.ibeaconlocationsystem.BeaconList;
import jp.ac.oit.elc.mail.ibeaconlocationsystem.LocationDB;
import jp.ac.oit.elc.mail.ibeaconlocationsystem.R;
import jp.ac.oit.elc.mail.ibeaconlocationsystem.Sample;
import jp.ac.oit.elc.mail.ibeaconlocationsystem.SampleList;
import jp.ac.oit.elc.mail.ibeaconlocationsystem.bluetooth.BluetoothBeacon;
import jp.ac.oit.elc.mail.ibeaconlocationsystem.classification.LocationClassifier;
import jp.ac.oit.elc.mail.ibeaconlocationsystem.view.IntensityMapView;
import weka.core.matrix.Matrix;

import static jp.ac.oit.elc.mail.ibeaconlocationsystem.Environment.BT_EVALUATION_CSV;
import static jp.ac.oit.elc.mail.ibeaconlocationsystem.Environment.BT_TRAINING_CSV;
import static jp.ac.oit.elc.mail.ibeaconlocationsystem.Environment.WIFI_EVALUATION_CSV;
import static jp.ac.oit.elc.mail.ibeaconlocationsystem.Environment.WIFI_TRAINING_CSV;

public class EvaluationActivity extends AppCompatActivity {
    private static final String TAG = EvaluationActivity.class.getSimpleName();
    //view
    private IntensityMapView mIntensityMap;
    private Spinner mPositionSpinner;
    private ArrayAdapter<String> mSpinnerAdapter;
    private TextView mTextError;
    //controller
    private SampleList mEvalData;
    private LocationClassifier mClassifier;
    private Map<Point, Point> mCalcPositionMap;
    private Point mSelectedPositon;
    private boolean mLoadedEvaluation = false;
    private Map<Point, Point> mTriCalcMap;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_evaluation);
        initViews();
        SampleList trainingData = SampleList.loadFromCsv(BT_TRAINING_CSV, WIFI_TRAINING_CSV);
        mClassifier = new LocationClassifier("IntensityMap", trainingData);
        mEvalData = SampleList.loadFromCsv(BT_EVALUATION_CSV, WIFI_EVALUATION_CSV);
        if (mEvalData == null) {
            Log.e(TAG, "can't Load Csv file");
            Toast.makeText(this, "Can't Load CSV file", Toast.LENGTH_SHORT).show();
        }
        mCalcPositionMap = new ConcurrentHashMap<>();
        for (Point pos : mEvalData.getPositions()) {
            mSpinnerAdapter.add(String.format("%d,%d", pos.x, pos.y));
        }
        loadClassifier();
    }

    private void initViews() {
        mIntensityMap = (IntensityMapView) findViewById(R.id.intensityMapView);
        mSpinnerAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item);
        mPositionSpinner = (Spinner) findViewById(R.id.spinnerPositions);
        mTextError = (TextView) findViewById(R.id.textError);
        mIntensityMap.setImageResource(R.mipmap.floor_map);
        mIntensityMap.setOnDrawListener(mOnMapDrawListener);
        mPositionSpinner.setAdapter(mSpinnerAdapter);
        mPositionSpinner.setOnItemSelectedListener(mOnSpinnerItemSelectedListener);
    }

    private void loadClassifier() {
        AsyncTask<Void, Void, Void> task = new AsyncTask<Void, Void, Void>() {
            ProgressDialog dialog;

            @Override
            protected void onPreExecute() {
                super.onPreExecute();
                dialog = new ProgressDialog(EvaluationActivity.this);
                dialog.setMessage("Building Classifier");
                dialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
                dialog.setCancelable(false);
            }

            @Override
            protected void onProgressUpdate(Void... values) {
                super.onProgressUpdate(values);
                mIntensityMap.invalidate();
            }

            @Override
            protected void onPostExecute(Void aVoid) {
                super.onPostExecute(aVoid);
                dialog.dismiss();
                mLoadedEvaluation = true;
                Toast.makeText(EvaluationActivity.this, "Built Classifier", Toast.LENGTH_SHORT).show();
            }

            @Override
            protected Void doInBackground(Void... params) {
                mClassifier.build();
                for (Sample sample : mEvalData) {
                    Map<Point, Double> probs = mClassifier.recognize(sample);
                    Point calcPos = mClassifier.predictPosition(probs);
                    mCalcPositionMap.put(sample.getPosition(), calcPos);
                    publishProgress();
                }
                mTriCalcMap = new HashMap<>();
                for (Sample sample : mEvalData){
                    Point p =  triangle(sample.getBtBeaconList());
                    if (p == null){
                        continue;
                    }
                    mTriCalcMap.put(sample.getPosition(), p);
                }
                return null;
            }
        };
        task.execute();
    }

    private Point triangle(BeaconList<BluetoothBeacon> beacons){
        Map<BluetoothBeacon, Point> map = new HashMap<>();
        for (BluetoothBeacon beacon : beacons){
            Point loc = LocationDB.get(beacon.getMacAddress());
            if (loc != null){
                map.put(beacon, loc);
            }
        }
        Log.d(TAG, "points: " + String.valueOf(map.size()));
        if(map.size() < 3){
            return null;
        }
        while(map.size() > 3){
            BluetoothBeacon min = new BluetoothBeacon("", 0);
            for (BluetoothBeacon beacon : map.keySet()){
                if(beacon.getRssi() <= min.getRssi()){
                    min = beacon;
                }
            }
            map.remove(min);
        }
        double[] rssis = new double[3];
        Point[] points = new Point[3];
        int i = 0;
        for (Map.Entry<BluetoothBeacon, Point> entry : map.entrySet()){
            rssis[i] = (double)mClassifier.convertRssiValue(entry.getKey().getRssi());
            points[i] = entry.getValue();
            i++;
        }
        double[][] a = new double[2][2];
        double[][] b = new double[2][1];
        a[0][0] = points[1].x - points[0].x;
        a[0][1] = points[1].y - points[0].y;
        a[1][0] = points[2].x - points[0].x;
        a[1][1] = points[2].y - points[0].y;
        b[0][0] = Math.pow(points[1].x, 2) - Math.pow(points[0].x, 2) + Math.pow(points[1].y, 2) - Math.pow(points[0].y, 2) - Math.pow(rssis[1], 2) + Math.pow(rssis[0], 2);
        b[1][0] = Math.pow(points[2].x, 2) - Math.pow(points[0].x, 2) + Math.pow(points[2].y, 2) - Math.pow(points[0].y, 2) - Math.pow(rssis[2], 2) + Math.pow(rssis[0], 2);
        Matrix mA = new Matrix(a);
        Matrix mB = new Matrix(b);
        mA = mA.times(2);
        mA = mA.inverse();
        Matrix mRes = mA.times(mB);
        int x = (int)mRes.get(0, 0);
        int y = (int)mRes.get(0, 1);
        return new Point(x, y);
    }
    private Point calcPositions(Map<Point, Double> prob) {
        double x = 0, y = 0;
        for (Map.Entry<Point, Double> entry : prob.entrySet()) {
            x += entry.getValue() * entry.getKey().x;
            y += entry.getValue() * entry.getKey().y;
        }
        return new Point((int) Math.round(x), (int) Math.round(y));
    }


    private IntensityMapView.OnDrawListener mOnMapDrawListener = new IntensityMapView.OnDrawListener() {
        @Override
        public void onDraw(Canvas canvas) {
            if (!mLoadedEvaluation) {
                return;
            }
            for (Map.Entry<Point, Point> entry : mCalcPositionMap.entrySet()) {
                Point p = mCalcPositionMap.get(entry.getKey());
                boolean isSelected = entry.getKey().equals(mSelectedPositon);
                Point measurePos = mIntensityMap.imageToScreenCoord(entry.getKey());
                Point calcPos = mIntensityMap.imageToScreenCoord(entry.getValue());
                Paint paint = new Paint();
                paint.setStyle(Paint.Style.FILL);
                if (isSelected) {
                    paint.setColor(Color.RED);
                } else {
                    paint.setColor(Color.BLUE);
                }
                if(p != null) {
                    canvas.drawCircle(p.x, p.y, 10, new Paint());
                }
                canvas.drawCircle(measurePos.x, measurePos.y, 10, paint);
                paint.setColor(Color.GREEN);
                canvas.drawCircle(calcPos.x, calcPos.y, 10, paint);
                canvas.drawLine(measurePos.x, measurePos.y, calcPos.x, calcPos.y, new Paint());
            }
        }
    };
    private AdapterView.OnItemSelectedListener mOnSpinnerItemSelectedListener = new AdapterView.OnItemSelectedListener() {
        @Override
        public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
            mSelectedPositon = parsePoint(mSpinnerAdapter.getItem(position));
            if (!mCalcPositionMap.containsKey(mSelectedPositon)) {
                return;
            }
            Point calculated = mCalcPositionMap.get(mSelectedPositon);
            if (calculated == null) {
                return;
            }
            double dist = Math.sqrt(Math.pow((double) (mSelectedPositon.x - calculated.x), 2) + Math.pow((double) (mSelectedPositon.y - calculated.y), 2));
            mTextError.setText(String.format("%.1fpx", dist));
            mIntensityMap.invalidate();
        }

        @Override
        public void onNothingSelected(AdapterView<?> parent) {
        }
    };

    private Point parsePoint(String str) {
        String[] split = str.split(",");
        int x = Integer.parseInt(split[0]);
        int y = Integer.parseInt(split[1]);
        return new Point(x, y);
    }
}
