package jp.ac.oit.elc.mail.ibeaconlocationsystem.activity;

import android.app.ProgressDialog;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Point;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;

import jp.ac.oit.elc.mail.ibeaconlocationsystem.BeaconList;
import jp.ac.oit.elc.mail.ibeaconlocationsystem.BeaconScanCallback;
import jp.ac.oit.elc.mail.ibeaconlocationsystem.BeaconScanner;
import jp.ac.oit.elc.mail.ibeaconlocationsystem.R;
import jp.ac.oit.elc.mail.ibeaconlocationsystem.Sample;
import jp.ac.oit.elc.mail.ibeaconlocationsystem.SampleList;
import jp.ac.oit.elc.mail.ibeaconlocationsystem.bluetooth.BluetoothBeacon;
import jp.ac.oit.elc.mail.ibeaconlocationsystem.view.IntensityMapView;
import jp.ac.oit.elc.mail.ibeaconlocationsystem.view.OnDrawListener;
import jp.ac.oit.elc.mail.ibeaconlocationsystem.wifi.WifiBeacon;

import static jp.ac.oit.elc.mail.ibeaconlocationsystem.Environment.INTENSITY_MAP_PATH;

public class ConfigActivity extends AppCompatActivity {
    private static final String TAG = ConfigActivity.class.getSimpleName();
    private BeaconScanner mBeaconScanner;
    private Button mButtonStart;
    private Button mButtonSet;
    private Button mButtonStep;
    private CheckBox mToggleLockMap;
    private TextView mTextStatus;
    private IntensityMapView mIntensityMap;
    private ProgressDialog mProgDialog;
    private List<List<Point>> mSetPoints;
    private SampleList mSampleBuffer;
    private boolean isStart;
    private int lastStartIndex;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_config);
        initViews();
        mBeaconScanner = new BeaconScanner(this);
        mSetPoints = new ArrayList<>();
        mSampleBuffer = new SampleList();
    }

    private void initViews() {
        mButtonStart = (Button) findViewById(R.id.buttonStart);
        mButtonSet = (Button) findViewById(R.id.buttonSet);
        mButtonStep = (Button) findViewById(R.id.buttonStep);
        mToggleLockMap = (CheckBox) findViewById(R.id.toggleLockMap);
        mTextStatus = (TextView) findViewById(R.id.textStatus);
        mIntensityMap = (IntensityMapView) findViewById(R.id.intensityMapView);
        mButtonStart.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!isStart) {
                    mButtonStart.setEnabled(false);
                    mButtonSet.setEnabled(true);
                    mButtonStep.setEnabled(false);
                    mSetPoints.add(new ArrayList<Point>());
                    isStart = true;
                    lastStartIndex = 0;
                    mButtonStart.setText("Stop");
                } else {
                    saveIntensityMap();
                    mButtonStart.setText("Start");
                    mButtonStart.setEnabled(true);
                    mButtonSet.setEnabled(false);
                    mButtonStep.setEnabled(false);
                    isStart = false;
                }
            }
        });
        mButtonSet.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                List<Point> latestSet = mSetPoints.get(mSetPoints.size() - 1);
                Point currentPoint = mIntensityMap.getPinImageCoordPosition();
                latestSet.add(currentPoint);
                if(latestSet.size() == 1){
                    mButtonStart.setEnabled(false);
                    mButtonSet.setEnabled(false);
                    mButtonStep.setEnabled(true);
                    return;
                }
                Point lineSPoint = latestSet.get(latestSet.size() - lastStartIndex - 1);
                Point offset = new Point(currentPoint.x - lineSPoint.x, currentPoint.y - lineSPoint.y);
                int numPoints = mSampleBuffer.size() - 1;
                for (int i = 0; i < mSampleBuffer.size(); i++){
                    int x = lineSPoint.x + (offset.x * i / numPoints);
                    int y = lineSPoint.y + (offset.y * i / numPoints);
                    Sample sample = mSampleBuffer.get(i);
                    sample.x = x;
                    sample.y = y;
                    Log.d(TAG, "add sample");
                    mIntensityMap.addSample(sample);
                }
                lastStartIndex = latestSet.size();
                mSampleBuffer.clear();
                mButtonStart.setEnabled(true);
                mButtonSet.setEnabled(false);
                mButtonStep.setEnabled(true);
            }
        });
        mButtonStep.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mBeaconScanner.scan(beaconScanCallback);
                mButtonStart.setEnabled(false);
                mButtonSet.setEnabled(true);
                mButtonStep.setEnabled(true);
            }
        });
        mToggleLockMap.setOnCheckedChangeListener(locksMapCheckedChangeListner);
        mIntensityMap.setOnTouchListener(mapTouchListener);
        mIntensityMap.setImageResource(R.mipmap.floor_map);
        mIntensityMap.setOnDrawListener(mapDrawListener);
    }

    private void saveIntensityMap() {
        mIntensityMap.getSampleList().save(INTENSITY_MAP_PATH);
    }

    //    private View.OnClickListener buttonStartClick = new View.OnClickListener() {
//        @Override
//        public void onClick(View v) {
//            if(!mIsStart){
//                mSetPoints.clear();
//                mIsStart = true;
//                mButtonStep.setEnabled(mIsStart);
//                mButtonSet.setText("End");
//            }else{
//                for(IntensitySample addSample: mSetPoints){
//                    mIntensityMap.addSample(addSample);
//                }
//                mIsStart = false;
//                mButtonStep.setEnabled(mIsStart);
//                mButtonSet.setText("Start");
//
//            }
//        }
//    };
    private View.OnTouchListener mapTouchListener = new View.OnTouchListener() {
        @Override
        public boolean onTouch(View v, MotionEvent event) {
            if (mToggleLockMap.isChecked()) {
                mIntensityMap.setPinScreenCoordPosition((int) event.getX(), (int) event.getY());
            }

            return false;
        }
    };


    private BeaconScanCallback beaconScanCallback = new BeaconScanCallback() {
        @Override
        public void onStartScan() {
            mProgDialog = new ProgressDialog(ConfigActivity.this);
            mProgDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
            mProgDialog.setMessage("Now Scanning");
            mProgDialog.setCanceledOnTouchOutside(false);
            mProgDialog.setCancelable(true);
            mProgDialog.show();
        }


        @Override
        public void onScanned(BeaconList<BluetoothBeacon> btBeaconList, BeaconList<WifiBeacon> wifiBeaconList) {
            mProgDialog.dismiss();
            Toast.makeText(ConfigActivity.this, String.format("Scan Complete: BT(%d), Wifi(%d)", btBeaconList.size(), wifiBeaconList.size()), Toast.LENGTH_SHORT).show();
            Point point = mIntensityMap.getPinImageCoordPosition();
            Sample sample = new Sample(point.x, point.y, btBeaconList, wifiBeaconList);
            mSampleBuffer.add(sample);
        }


        @Override
        public void onScanFailed() {
            Log.e(TAG, "error scan");
        }
    };

    private CompoundButton.OnCheckedChangeListener locksMapCheckedChangeListner = new CompoundButton.OnCheckedChangeListener() {
        @Override
        public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
            mIntensityMap.setScaleEnabled(!isChecked);
            mIntensityMap.setScrollEnabled(!isChecked);
            mIntensityMap.setDoubleTapEnabled(!isChecked);
        }
    };

    private OnDrawListener mapDrawListener = new OnDrawListener() {
        @Override
        public void onDraw(Canvas canvas) {
            Point point = mIntensityMap.getPinScreenCoordPosition();
            if (point == null) {
                return;
            }
            point = mIntensityMap.screenToImageCoord(point.x, point.y);
            mTextStatus.setText(String.format("(%d,%d)", point.x, point.y));
            for (List<Point> pointList : mSetPoints){
                for (int i = 1; i < pointList.size(); i++){
                    Point prevPoint = pointList.get(i - 1);
                    Point stepPoint = pointList.get(i);
                    Paint paint = new Paint();
                    paint.setStyle(Paint.Style.STROKE);
                    paint.setColor(Color.BLUE);
                    paint.setStrokeWidth(100);
                    Point p1 = mIntensityMap.imageToScreenCoord(prevPoint);
                    Point p2 = mIntensityMap.imageToScreenCoord(stepPoint);
                    canvas.drawLine(p1.x, p1.y, p2.x, p2.y, new Paint());
                }
            }
        }
    };
}

