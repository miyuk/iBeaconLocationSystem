package jp.ac.oit.elc.mail.ibeaconlocationsystem.activity;

import android.app.ProgressDialog;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.DashPathEffect;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Point;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
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
    private Button mButtonStep;
    private Button mButtonSave;
    private CheckBox mToggleLockMap;
    private TextView mTextStatus;
    private IntensityMapView mIntensityMap;
    private ProgressDialog mProgDialog;
    private List<Point[]> mSetPoints;
    private SampleList mSampleBuffer;
    private boolean isStart;
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
        mButtonStep = (Button) findViewById(R.id.buttonStep);
        mButtonSave = (Button)findViewById(R.id.buttonSave);
        mToggleLockMap = (CheckBox) findViewById(R.id.toggleLockMap);
        mTextStatus = (TextView) findViewById(R.id.textStatus);
        mIntensityMap = (IntensityMapView) findViewById(R.id.intensityMapView);
        mButtonStart.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!isStart) {
                    mButtonStart.setEnabled(false);
                    mButtonStep.setEnabled(true);
                    mButtonSave.setEnabled(false);
                    mSetPoints.add(new Point[2]);
                    mSetPoints.get(mSetPoints.size() - 1)[0] = mIntensityMap.getPinImageCoordPosition();
                    mBeaconScanner.startScan(beaconScanCallback);
                    isStart = true;

                    mButtonStart.setText("Stop");
                } else {
                    mSetPoints.get(mSetPoints.size() - 1)[1] = mIntensityMap.getPinImageCoordPosition();
                    flushBufferedSample();
                    mButtonStart.setText("Start");
                    mButtonStart.setEnabled(true);
                    mButtonStep.setEnabled(false);
                    mButtonSave.setEnabled(true);
                    isStart = false;
                }
            }
        });
        mButtonStep.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mBeaconScanner.startScan(beaconScanCallback);
                mButtonStart.setEnabled(true);
                mButtonStep.setEnabled(true);
                mButtonSave.setEnabled(false);
            }
        });
        mButtonSave.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(saveIntensityMap()){
                    Toast.makeText(ConfigActivity.this, "Save Success", Toast.LENGTH_SHORT).show();
                }else{
                    Toast.makeText(ConfigActivity.this, "Save Failed", Toast.LENGTH_SHORT).show();

                }
            }
        });
        mToggleLockMap.setOnCheckedChangeListener(locksMapCheckedChangeListner);
        mIntensityMap.setOnTouchListener(mapTouchListener);
        mIntensityMap.setImageResource(R.mipmap.floor_map);
        mIntensityMap.setOnDrawListener(mapDrawListener);
    }

    private void flushBufferedSample() {
        Point[] lastSet = mSetPoints.get(mSetPoints.size() - 1);
        Point sp = lastSet[0];
        Point ep = lastSet[1];
        Point offset = new Point(ep.x - sp.x, ep.y - sp.y);
        int numPoints = mSampleBuffer.size() - 1;
        for (int i = 0; i <= numPoints; i++) {
            int x = sp.x + (offset.x * i / numPoints);
            int y = sp.y + (offset.y * i / numPoints);
            Sample sample = mSampleBuffer.get(i);
            sample.x = x;
            sample.y = y;
            mIntensityMap.addSample(sample);
        }
        mSampleBuffer.clear();

    }

    private boolean saveIntensityMap() {
        return mIntensityMap.getSampleList().save(INTENSITY_MAP_PATH);
    }

    private View.OnTouchListener mapTouchListener = new View.OnTouchListener() {
        @Override
        public boolean onTouch(View v, MotionEvent event) {
            if (mToggleLockMap.isChecked()) {
                mIntensityMap.setPinScreenCoordPosition((int) event.getX(), (int) event.getY());
            }

            return false;
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
            for (Point[] points : mSetPoints) {
                Paint paint = new Paint();
                paint.setStyle(Paint.Style.STROKE);
                paint.setColor(Color.BLUE);
                paint.setStrokeWidth(3);
                Point p1 = mIntensityMap.imageToScreenCoord(points[0]);
                Point p2;
                if (points[1] == null) {
                    p2 = mIntensityMap.getPinScreenCoordPosition();
                    float[] interval = new float[]{10, 10};
                    DashPathEffect effect = new DashPathEffect(interval, 0);
                    paint.setPathEffect(effect);
                    paint.setColor(Color.RED);
                } else {
                    p2 = mIntensityMap.imageToScreenCoord(points[1]);
                }
                Path path = new Path();
                path.moveTo(p1.x, p1.y);
                path.lineTo(p2.x, p2.y);
                canvas.drawPath(path, paint);
            }
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
        public void onScanTimeout(BeaconList<BluetoothBeacon> btBeacons, BeaconList<WifiBeacon> wifiBeacons) {
            mProgDialog.dismiss();
            Toast.makeText(ConfigActivity.this, String.format("Scan Complete: BT(%d), Wifi(%d)", btBeacons.size(), wifiBeacons.size()), Toast.LENGTH_SHORT).show();
            Point point = mIntensityMap.getPinImageCoordPosition();
            Sample sample = new Sample(point.x, point.y, btBeacons, wifiBeacons);
            mSampleBuffer.add(sample);
        }

        @Override
        public void onScanFailed() {
        }
    };

}

