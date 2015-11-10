package jp.ac.oit.elc.mail.ibeaconlocationsystem.activity;

import android.app.LoaderManager;
import android.app.ProgressDialog;
import android.content.Loader;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Point;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import jp.ac.oit.elc.mail.ibeaconlocationsystem.R;
import jp.ac.oit.elc.mail.ibeaconlocationsystem.Sample;
import jp.ac.oit.elc.mail.ibeaconlocationsystem.SampleList;
import jp.ac.oit.elc.mail.ibeaconlocationsystem.classification.ClassifierLoader;
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
    //model
    private SampleList mEvalData;
    private LocationClassifier mClassifier;
    private Map<Point, Point> mCalcPositionMap;
    private Point mSelectedPositon;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_evaluation);
        initViews();
        Bundle bundle = new Bundle();
        bundle.putString("BT_TRAINING_CSV", BT_TRAINING_CSV);
        bundle.putString("WIFI_TRAINING_CSV", WIFI_TRAINING_CSV);
        getLoaderManager().initLoader(0, bundle, mClassifierLoadCallback);
        mEvalData = SampleList.loadFromCsv(BT_EVALUATION_CSV, WIFI_EVALUATION_CSV);
        mCalcPositionMap = new ConcurrentHashMap<>();
        for (Point pos : mEvalData.getPositions()) {
            mSpinnerAdapter.add(String.format("%d,%d", pos.x, pos.y));
        }
    }

    private void initViews() {
        mIntensityMap = (IntensityMapView) findViewById(R.id.intensityMapView);
        mSpinnerAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item);
        mPositionSpinner = (Spinner) findViewById(R.id.spinnerPositions);
        mTextError = (TextView) findViewById(R.id.textError);
        mIntensityMap.setImageResource(R.mipmap.floor_map);
        mIntensityMap.setOnDrawListener(mMapDrawListener);
        mPositionSpinner.setAdapter(mSpinnerAdapter);
        mPositionSpinner.setOnItemSelectedListener(mLocationSpinnerItemSelected);
    }

    private void evaluation() {
        for (Sample sample : mEvalData) {
            Map<Point, Double> pValues = mClassifier.recognize(sample.getWifiBeaconList(), false);
            Point calcPos = mClassifier.predictPosition(pValues);
            Map<Point, Double> pValuesBt = mClassifier.recognize(sample.getBtBeaconList(), true);
            Point calcPosBt = mClassifier.predictPosition(pValues);
            Point res = new Point((calcPos.x + calcPosBt.x) / 2, (calcPos.y + calcPosBt.y) / 2);
            mCalcPositionMap.put(sample.getPosition(), res);
            mIntensityMap.invalidate();
        }
    }

    private IntensityMapView.OnDrawListener mMapDrawListener = new IntensityMapView.OnDrawListener() {
        @Override
        public void onDraw(Canvas canvas) {
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
                if (p != null) {
                    canvas.drawCircle(p.x, p.y, 10, new Paint());
                }
                canvas.drawCircle(measurePos.x, measurePos.y, 10, paint);
                paint.setColor(Color.GREEN);
                canvas.drawCircle(calcPos.x, calcPos.y, 10, paint);
                canvas.drawLine(measurePos.x, measurePos.y, calcPos.x, calcPos.y, new Paint());
            }
        }
    };
    private AdapterView.OnItemSelectedListener mLocationSpinnerItemSelected = new AdapterView.OnItemSelectedListener() {
        @Override
        public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
            mSelectedPositon = parsePoint(mSpinnerAdapter.getItem(position));
            if (!mCalcPositionMap.containsKey(mSelectedPositon)) {
                return;
            }
            Point calculated = mCalcPositionMap.get(mSelectedPositon);
            double dist = distance(mSelectedPositon.x, mSelectedPositon.y, calculated.x, calculated.y);
            mTextError.setText(String.format("%.2fm(%.0fpx)", dist*0.048, dist));
            mIntensityMap.invalidate();
        }

        @Override
        public void onNothingSelected(AdapterView<?> parent) {
        }
    };

    private LoaderManager.LoaderCallbacks<LocationClassifier> mClassifierLoadCallback = new LoaderManager.LoaderCallbacks<LocationClassifier>() {
        ProgressDialog dialog;

        @Override
        public Loader<LocationClassifier> onCreateLoader(int id, Bundle args) {
            dialog = new ProgressDialog(EvaluationActivity.this);
            dialog.setMessage("Load Classifier");
            dialog.setCancelable(false);
            dialog.show();
            String btTrainingCsv = args.getString("BT_TRAINING_CSV");
            String wifiTrainingCsv = args.getString("WIFI_TRAINING_CSV");
            ClassifierLoader loader = new ClassifierLoader(EvaluationActivity.this, btTrainingCsv, wifiTrainingCsv);
            return loader;
        }

        @Override
        public void onLoadFinished(Loader<LocationClassifier> loader, LocationClassifier data) {
            Toast.makeText(EvaluationActivity.this, "Loaded Classifier", Toast.LENGTH_SHORT).show();
            dialog.dismiss();
            mClassifier = data;
            evaluation();
        }

        @Override
        public void onLoaderReset(Loader<LocationClassifier> loader) {
        }
    };

    private static double distance(double x1, double y1, double x2, double y2) {
        return Math.sqrt(Math.pow(x1 - x2, 2) + Math.pow(y1 - y2, 2));
    }

    private static Point parsePoint(String str) {
        String[] split = str.split(",");
        int x = Integer.parseInt(split[0]);
        int y = Integer.parseInt(split[1]);
        return new Point(x, y);
    }
}
