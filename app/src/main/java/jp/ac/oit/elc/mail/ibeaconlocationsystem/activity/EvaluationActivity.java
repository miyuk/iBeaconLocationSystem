package jp.ac.oit.elc.mail.ibeaconlocationsystem.activity;

import android.app.LoaderManager;
import android.app.ProgressDialog;
import android.content.Loader;
import android.graphics.Canvas;
import android.graphics.Paint;
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

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import jp.ac.oit.elc.mail.ibeaconlocationsystem.R;
import jp.ac.oit.elc.mail.ibeaconlocationsystem.Sample;
import jp.ac.oit.elc.mail.ibeaconlocationsystem.SampleList;
import jp.ac.oit.elc.mail.ibeaconlocationsystem.classification.BtAndWifiClassifier;
import jp.ac.oit.elc.mail.ibeaconlocationsystem.classification.BtClassifier;
import jp.ac.oit.elc.mail.ibeaconlocationsystem.classification.ClassifierLoader;
import jp.ac.oit.elc.mail.ibeaconlocationsystem.classification.FusionClassifier;
import jp.ac.oit.elc.mail.ibeaconlocationsystem.classification.WifiClassifier;
import jp.ac.oit.elc.mail.ibeaconlocationsystem.view.IntensityMapView;
import weka.classifiers.Classifier;

import static jp.ac.oit.elc.mail.ibeaconlocationsystem.Environment.APP_DIR;
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
    private BtClassifier mBtClassifier;
    private WifiClassifier mWifiClassifier;
    private BtAndWifiClassifier mBtAndWifiClassifier;
    private FusionClassifier mFusionClassifier;
    private Map<Point, Point[]> mEstimatedPositionMap;
    private Point mSelectedPosition;
    private boolean mEvaluated;

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
        mEstimatedPositionMap = new ConcurrentHashMap<>();
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

    private void evaluate() {
        StringBuffer buffer = new StringBuffer();
        buffer.append("TRUE_X,TRUE_Y,BT_X,BT_Y,WIFI_X,WIFI_Y,BT+WIFI_X,BT+WIFI_Y\n");
        for (Sample sample : mEvalData) {
            Point btPos;
            Point wifiPos;
            Point btAndWifiPos;
            Point fusionPos;
            try {
                btPos = mBtClassifier.calcExpectedPosition(sample.getBtBeaconList(), sample.getWifiBeaconList());
                wifiPos = mWifiClassifier.calcExpectedPosition(sample.getBtBeaconList(), sample.getWifiBeaconList());
                btAndWifiPos = mBtAndWifiClassifier.calcExpectedPosition(sample.getBtBeaconList(), sample.getWifiBeaconList());
//                fusionPos = mFusionClassifier.calcExpectedPosition(sample.getBtBeaconList(), sample.getWifiBeaconList());
//                btPos = mBtClassifier.classifyPosition(sample.getBtBeaconList(), sample.getWifiBeaconList());
//                wifiPos = mWifiClassifier.classifyPosition(sample.getBtBeaconList(), sample.getWifiBeaconList());
//                btAndWifiPos = mBtAndWifiClassifier.classifyPosition(sample.getBtBeaconList(), sample.getWifiBeaconList());
                fusionPos = mFusionClassifier.classifyPosition(sample.getBtBeaconList(), sample.getWifiBeaconList());

                buffer.append(String.format("%d,%d,%d,%d,%d,%d,%d,%d,%d,%d\n",
                        sample.getPosition().x, sample.getPosition().y,
                        btPos.x, btPos.y,
                        wifiPos.x, wifiPos.y,
                        btAndWifiPos.x, btAndWifiPos.y,
                        fusionPos.x, fusionPos.y));
            } catch (Exception e) {
                e.printStackTrace();
                Toast.makeText(this, "can't evaluate", Toast.LENGTH_SHORT).show();
                return;
            }
//            Log.d(TAG, String.format("True:%s BT:%s Wifi:%s BT+Wifi:%s FUSION:%s", sample.getPosition(), btPos, wifiPos, btAndWifiPos, fusionPos));
            mEstimatedPositionMap.put(sample.getPosition(), new Point[]{btPos, wifiPos, btAndWifiPos, fusionPos});
        }
        mEvaluated = true;
        mIntensityMap.invalidate();
        try {
            saveEvalResult(buffer.toString());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void saveEvalResult(String text) throws IOException {
        File file = new File(APP_DIR + "/eval_result.csv");
        if (!file.exists()) {
            file.createNewFile();
        }
        BufferedWriter writer = new BufferedWriter(new FileWriter(file, false));
        writer.write(text);
        writer.flush();
        writer.close();
    }

    private IntensityMapView.OnDrawListener mMapDrawListener = new IntensityMapView.OnDrawListener() {
        @Override
        public void onDraw(Canvas canvas) {
            if (!mEvaluated) {
                return;
            }
            for (Map.Entry<Point, Point[]> entry : mEstimatedPositionMap.entrySet()) {
                boolean isSelected = entry.getKey().equals(mSelectedPosition);
                Point truePos = mIntensityMap.imageToScreenCoord(entry.getKey());
                Point btPos = mIntensityMap.imageToScreenCoord(entry.getValue()[0]);
                Point wifiPos = mIntensityMap.imageToScreenCoord(entry.getValue()[1]);
                Point btAndWifiPos = mIntensityMap.imageToScreenCoord(entry.getValue()[2]);
                Point fusionPos = mIntensityMap.imageToScreenCoord(entry    .getValue()[3]);
                Paint strokePaint = new Paint();
                Paint fillPaint = new Paint();
                strokePaint.setStyle(Paint.Style.STROKE);
                fillPaint.setStyle(Paint.Style.FILL);
                int width = 2;
                int alpha = 64;
                if (isSelected) {
                    width = 5;
                    alpha = 255;
                }
                strokePaint.setStrokeWidth(width);
                strokePaint.setARGB(alpha, 255, 0, 0);
                fillPaint.setARGB(alpha, 255, 0, 0);
                canvas.drawLine(btPos.x, btPos.y, truePos.x, truePos.y, strokePaint);
                canvas.drawCircle(btPos.x, btPos.y, width * 2, fillPaint);
                strokePaint.setARGB(alpha, 0, 255, 0);
                fillPaint.setARGB(alpha, 0, 255, 0);
                canvas.drawLine(wifiPos.x, wifiPos.y, truePos.x, truePos.y, strokePaint);
                canvas.drawCircle(wifiPos.x, wifiPos.y, width * 2, fillPaint);
                strokePaint.setARGB(alpha, 0, 0, 255);
                fillPaint.setARGB(alpha, 0, 0, 255);
                canvas.drawLine(truePos.x, truePos.y, btAndWifiPos.x, btAndWifiPos.y, strokePaint);
                canvas.drawCircle(btAndWifiPos.x, btAndWifiPos.y, width * 2, fillPaint);
                strokePaint.setARGB(alpha, 255, 255, 0);
                fillPaint.setARGB(alpha, 255, 255, 0);
                canvas.drawLine(truePos.x, truePos.y, fusionPos.x, fusionPos.y, strokePaint);
                canvas.drawCircle(fusionPos.x, fusionPos.y, width * 2, fillPaint);
                strokePaint.setARGB(alpha, 0, 0, 0);
                fillPaint.setARGB(alpha, 0, 0, 0);
                canvas.drawCircle(truePos.x, truePos.y, width * 2, fillPaint);
            }
        }
    };
    private AdapterView.OnItemSelectedListener mLocationSpinnerItemSelected = new AdapterView.OnItemSelectedListener() {
        @Override
        public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
            mSelectedPosition = parsePoint(mSpinnerAdapter.getItem(position));
            if (!mEstimatedPositionMap.containsKey(mSelectedPosition)) {
                return;
            }
            Point[] calculated = mEstimatedPositionMap.get(mSelectedPosition);
            Log.d(TAG, String.format("True:%s BT:%s Wifi:%s BT+Wifi:%s FUSION:%s", mSelectedPosition, calculated[0], calculated[1], calculated[2], calculated[3]));

            double dist = distance(mSelectedPosition.x, mSelectedPosition.y, calculated[3].x, calculated[3].y);
            mTextError.setText(String.format("%.2fm(%.0fpx)", dist * 0.048, dist));
            mIntensityMap.invalidate();
        }

        @Override
        public void onNothingSelected(AdapterView<?> parent) {
        }
    };

    private LoaderManager.LoaderCallbacks<Classifier[]> mClassifierLoadCallback = new LoaderManager.LoaderCallbacks<Classifier[]>() {
        ProgressDialog dialog;

        @Override
        public Loader<Classifier[]> onCreateLoader(int id, Bundle args) {
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
        public void onLoadFinished(Loader<Classifier[]> loader, Classifier[] data) {
            dialog.dismiss();
            if (data == null) {
                Toast.makeText(EvaluationActivity.this, "can't load Classifier", Toast.LENGTH_SHORT).show();
                return;
            }
            Toast.makeText(EvaluationActivity.this, "Loaded Classifier", Toast.LENGTH_SHORT).show();
            mBtClassifier = (BtClassifier) data[0];
            mWifiClassifier = (WifiClassifier) data[1];
            mBtAndWifiClassifier = (BtAndWifiClassifier) data[2];
            mFusionClassifier = (FusionClassifier)data[3];
            evaluate();
        }

        @Override
        public void onLoaderReset(Loader<Classifier[]> loader) {
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
