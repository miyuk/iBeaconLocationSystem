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

import jp.ac.oit.elc.mail.ibeaconlocationsystem.R;
import jp.ac.oit.elc.mail.ibeaconlocationsystem.Sample;
import jp.ac.oit.elc.mail.ibeaconlocationsystem.SampleList;
import jp.ac.oit.elc.mail.ibeaconlocationsystem.classification.LocationClassifier;
import jp.ac.oit.elc.mail.ibeaconlocationsystem.view.IntensityMapView;

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
        mCalcPositionMap = new HashMap<>();
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
                Toast.makeText(EvaluationActivity.this, "Built Classifier", Toast.LENGTH_SHORT).show();
            }

            @Override
            protected Void doInBackground(Void... params) {
                mClassifier.build();
                for (Sample sample : mEvalData) {
                    Map<Point, Double> probs = mClassifier.recognize(sample);
                    Point calcPos = calcPositions(probs);
                    mCalcPositionMap.put(sample.getPosition(), calcPos);
                    publishProgress();
                }
                return null;
            }
        };
        task.execute();
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
            for (Map.Entry<Point, Point> entry : mCalcPositionMap.entrySet()) {
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
            double dist = Math.sqrt(Math.pow((double) (mSelectedPositon.x - calculated.x), 2) + (double) (mSelectedPositon.y - calculated.y));
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
