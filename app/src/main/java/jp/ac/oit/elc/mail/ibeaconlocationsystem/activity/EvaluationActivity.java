package jp.ac.oit.elc.mail.ibeaconlocationsystem.activity;

import android.graphics.Canvas;
import android.graphics.Point;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import java.util.List;

import jp.ac.oit.elc.mail.ibeaconlocationsystem.R;
import jp.ac.oit.elc.mail.ibeaconlocationsystem.SampleList;
import jp.ac.oit.elc.mail.ibeaconlocationsystem.view.IntensityMapView;

import static jp.ac.oit.elc.mail.ibeaconlocationsystem.Environment.BT_EVALUATION_CSV;
import static jp.ac.oit.elc.mail.ibeaconlocationsystem.Environment.BT_TRAINING_CSV;
import static jp.ac.oit.elc.mail.ibeaconlocationsystem.Environment.WIFI_EVALUATION_CSV;
import static jp.ac.oit.elc.mail.ibeaconlocationsystem.Environment.WIFI_TRAINING_CSV;

public class EvaluationActivity extends AppCompatActivity {
    private static final String TAG = EvaluationActivity.class.getSimpleName();
    private IntensityMapView mIntensityMap;
    private Spinner mPositionSpinner;
    private TextView mTextError;

    //controller
    private SampleList mTrainingData;
    private SampleList mEvaluationData;
    private List<Point> mPositionList;
    private ArrayAdapter<String> mSpinnerAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_evaluation);
        loadClassifier();
        initViews();
    }

    private void initViews() {
        mIntensityMap = (IntensityMapView) findViewById(R.id.intensityMapView);
        mPositionSpinner = (Spinner) findViewById(R.id.spinnerPositions);
        mTextError = (TextView) findViewById(R.id.textError);

        mIntensityMap.setImageResource(R.mipmap.floor_map);
        mIntensityMap.setOnDrawListener(mOnMapDrawListener);
        mIntensityMap.setSampleList(mEvaluationData);
        mPositionSpinner.setAdapter(mSpinnerAdapter);
        mPositionSpinner.setOnItemSelectedListener(mOnSpinnerItemSelectedListener);
    }

    private void loadClassifier() {
        mTrainingData = SampleList.loadFromCsv(BT_TRAINING_CSV, WIFI_TRAINING_CSV);
        mEvaluationData = SampleList.loadFromCsv(BT_EVALUATION_CSV, WIFI_EVALUATION_CSV);
        if (mEvaluationData == null) {
            Log.e(TAG, "can't load Csv");
            Toast.makeText(this, "Can't Load CSV file", Toast.LENGTH_SHORT).show();
            return;
        }
        mPositionList = mEvaluationData.getPositionList();

        mSpinnerAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item);
        for (Point pos : mPositionList) {
            mSpinnerAdapter.add(String.format("(%d,%d)", pos.x, pos.y));
        }
    }

    private IntensityMapView.OnDrawListener mOnMapDrawListener = new IntensityMapView.OnDrawListener() {
        @Override
        public void onDraw(Canvas canvas) {

        }
    };
    private AdapterView.OnItemSelectedListener mOnSpinnerItemSelectedListener = new AdapterView.OnItemSelectedListener() {
        @Override
        public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
            //TODO
            Point selectPos = mPositionList.get(position);
            Point calcPos = new Point(100, 100);
            double dist = Math.sqrt(Math.pow((double) (selectPos.x - calcPos.x), 2) + (double) (selectPos.y - calcPos.y));
            mTextError.setText(String.format("%.3fpx", dist));
        }

        @Override
        public void onNothingSelected(AdapterView<?> parent) {

        }
    };
}
