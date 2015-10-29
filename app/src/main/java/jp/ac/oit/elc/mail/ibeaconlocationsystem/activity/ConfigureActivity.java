package jp.ac.oit.elc.mail.ibeaconlocationsystem.activity;

import android.app.ProgressDialog;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.DashPathEffect;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Point;
import android.media.AudioManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Pair;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;

import jp.ac.oit.elc.mail.ibeaconlocationsystem.BeaconList;
import jp.ac.oit.elc.mail.ibeaconlocationsystem.R;
import jp.ac.oit.elc.mail.ibeaconlocationsystem.Sample;
import jp.ac.oit.elc.mail.ibeaconlocationsystem.SampleList;
import jp.ac.oit.elc.mail.ibeaconlocationsystem.bluetooth.BluetoothBeacon;
import jp.ac.oit.elc.mail.ibeaconlocationsystem.bluetooth.BluetoothBeaconScanner;
import jp.ac.oit.elc.mail.ibeaconlocationsystem.view.IntensityMapView;
import jp.ac.oit.elc.mail.ibeaconlocationsystem.wifi.WifiBeacon;
import jp.ac.oit.elc.mail.ibeaconlocationsystem.wifi.WifiBeaconScanner;

import static jp.ac.oit.elc.mail.ibeaconlocationsystem.Environment.BT_INTENSITY_MAP_PATH;
import static jp.ac.oit.elc.mail.ibeaconlocationsystem.Environment.WIFI_INTENSITY_MAP_PATH;

public class ConfigureActivity extends AppCompatActivity {
    private static final String TAG = ConfigureActivity.class.getSimpleName();

    //view
    private Button mButtonStart;
    private Button mButtonStep;
    private Button mButtonSave;
    private CheckBox mToggleLockMap;
    private EditText mEditScanCount;
    private TextView mTextLocation;
    private TextView mTextStatus;
    private IntensityMapView mIntensityMap;

    //controller
    private List<Point[]> mStartStopPoints;
    private Point mStartPosition;
    private List<List<BeaconList[]>> mSampleBuffer;
    private BluetoothBeaconScanner mBtScanner;
    private WifiBeaconScanner mWifiScanner;
    private SampleList mSampleList;
    private boolean mStarted;
    private BeaconList<BluetoothBeacon> mBtBuffer;
    private BeaconList<WifiBeacon> mWifiBuffer;
    private boolean mBtScanned;
    private boolean mWifiScanned;
    private AudioManager mAudioManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_configure);
        mBtScanner = new BluetoothBeaconScanner(this);
        mWifiScanner = new WifiBeaconScanner(this);
        mStartStopPoints = new ArrayList<>();
        mSampleList = new SampleList();
        mSampleBuffer = new ArrayList<>();
        mBtScanner.setOnScanListener(mBtOnScanListener);
        mWifiScanner.setOnScanListener(mWifiOnScanListener);
        mAudioManager = (AudioManager) getSystemService(AUDIO_SERVICE);
        initViews();
    }

    private void initViews() {
        mButtonStart = (Button) findViewById(R.id.buttonStart);
        mButtonStep = (Button) findViewById(R.id.buttonStep);
        mButtonSave = (Button) findViewById(R.id.buttonSave);
        mToggleLockMap = (CheckBox) findViewById(R.id.toggleLockMap);
        mEditScanCount = (EditText) findViewById(R.id.editScanCount);
        mTextStatus = (TextView) findViewById(R.id.textStatus);
        mTextLocation = (TextView) findViewById(R.id.textLocation);
        mIntensityMap = (IntensityMapView) findViewById(R.id.intensityMapView);
        mIntensityMap.setSampleList(mSampleList);
        mButtonStart.setOnClickListener(onStartButtonClickListener);
        mButtonStep.setOnClickListener(onStepButtonClickListener);
        mButtonSave.setOnClickListener(onSaveButtonClickListener);
        mToggleLockMap.setOnCheckedChangeListener(locksMapCheckedChangeListner);
        mIntensityMap.setOnTouchListener(mapTouchListener);
        mIntensityMap.setImageResource(R.mipmap.floor_map);
        mIntensityMap.setOnDrawListener(mapDrawListener);
        mTextStatus.setText("Press Start");
    }

    //view listener
    private IntensityMapView.OnDrawListener mapDrawListener = new IntensityMapView.OnDrawListener() {
        @Override
        public void onDraw(Canvas canvas) {
            Point pinPos = mIntensityMap.getPinImageCoordPosition();
            if (pinPos == null) {
                return;
            }
            mTextLocation.setText(String.format("(%d,%d)", pinPos.x, pinPos.y));
            // draw flushed buffer line
            Path path = new Path();
            for (Point[] line : mStartStopPoints) {
                Point sPos = mIntensityMap.imageToScreenCoord(line[0]);
                Point ePos = mIntensityMap.imageToScreenCoord(line[1]);
                path.moveTo(sPos.x, sPos.y);
                path.lineTo(ePos.x, ePos.y);
            }
            Paint paint = new Paint();
            paint.setStyle(Paint.Style.STROKE);
            paint.setColor(Color.BLUE);
            paint.setStrokeWidth(3);
            canvas.drawPath(path, paint);
            path.rewind();

            // draw current line
            if (mStartPosition != null) {
                Point sPos = mIntensityMap.imageToScreenCoord(mStartPosition);
                Point ePos = mIntensityMap.getPinScreenCoordPosition();
                DashPathEffect dash = new DashPathEffect(new float[]{10, 10}, 0);
                paint.setColor(Color.RED);
                paint.setPathEffect(dash);
                path.moveTo(sPos.x, sPos.y);
                path.lineTo(ePos.x, ePos.y);
                canvas.drawPath(path, paint);
            }
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

    private View.OnTouchListener mapTouchListener = new View.OnTouchListener() {
        @Override
        public boolean onTouch(View v, MotionEvent event) {
            if (mToggleLockMap.isChecked()) {
                mIntensityMap.setPinScreenCoordPosition((int) event.getX(), (int) event.getY());
            }
            return false;
        }
    };

    private View.OnClickListener onStartButtonClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            if (!mStarted) {
                setViewEnabled(false, true, false, false);
                mStartPosition = mIntensityMap.getPinImageCoordPosition();
                mButtonStart.setText("Stop");
                mTextStatus.setText(String.format("Start Position (%d,%d)", mStartPosition.x, mStartPosition.y));
                startScan();
                mStarted = true;
            } else {
                setViewEnabled(true, false, true, true);
                Point currentPos = mIntensityMap.getPinImageCoordPosition();
                Point[] line = new Point[2];
                line[0] = mStartPosition;
                line[1] = mIntensityMap.getPinImageCoordPosition();
                mStartStopPoints.add(line);
                flushSampleBuffer(line[0], line[1]);
                mStartPosition = null;
                mButtonStart.setText("Start");
                mTextStatus.setText(String.format("Total Scan Count: %d", mSampleList.size()));
                mStarted = false;
            }
        }
    };
    private View.OnClickListener onStepButtonClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            setViewEnabled(true, true, false, false);
            startScan();
            mTextStatus.setText(String.format("%d steps", mSampleBuffer.size()));

        }
    };
    private View.OnClickListener onSaveButtonClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            if (mIntensityMap.getSampleList().saveToCsv(BT_INTENSITY_MAP_PATH, WIFI_INTENSITY_MAP_PATH)) {
                Toast.makeText(ConfigureActivity.this, "Save Success", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(ConfigureActivity.this, "Save Failed", Toast.LENGTH_SHORT).show();
            }
        }
    };

    //beacon scanner listener
    private BluetoothBeaconScanner.OnScanListener mBtOnScanListener = new BluetoothBeaconScanner.OnScanListener() {

        @Override
        public void onStartScan() {
            mBtScanned = false;
            mBtBuffer = new BeaconList<>();
        }

        @Override
        public void onScan(BluetoothBeacon beacon) {
            if (!mBtScanned) {
                mBtScanned = true;

            }
            BluetoothBeacon item = mBtBuffer.getByMacAddress(beacon.getMacAddress());
            if (item != null) {
                item.update(beacon.getRssi());
            } else {
                mBtBuffer.add(beacon);
            }
        }
    };

    private WifiBeaconScanner.OnScanListener mWifiOnScanListener = new WifiBeaconScanner.OnScanListener() {
        @Override
        public void onStartScan() {
            mWifiScanned = false;
            mWifiBuffer = new BeaconList<>();
        }

        @Override
        public void onScan(BeaconList<WifiBeacon> beaconList) {
            if (!mWifiScanned) {
                mWifiScanned = true;
            }
            for (WifiBeacon beacon : beaconList) {
                WifiBeacon item = mWifiBuffer.getByMacAddress(beacon.getMacAddress());
                if (item != null) {
                    item.update(beacon.getRssi());
                } else {
                    mWifiBuffer.add(beacon);
                }
            }
        }
    };

    //controller
    private void setViewEnabled(boolean startButton, boolean stepButton, boolean saveButton, boolean scanCount) {
        mButtonStart.setEnabled(startButton);
        mButtonStep.setEnabled(stepButton);
        mButtonSave.setEnabled(saveButton);
        mEditScanCount.setEnabled(scanCount);
    }

    private void startScan() {
        int count = Integer.parseInt(mEditScanCount.getText().toString());
        if (count < 1) {
            return;
        }
        BeaconScanAsyncTask task = new BeaconScanAsyncTask(count);
        task.execute();
    }

    private void flushSampleBuffer(Point sPos, Point ePos) {
        Point diff = new Point(ePos.x - sPos.x, ePos.y - sPos.y);
        int numPoints = mSampleBuffer.size();
        for (int i = 0; i < numPoints; i++) {
            int x = sPos.x + (diff.x * i / (numPoints - 1));
            int y = sPos.y + (diff.y * i / (numPoints - 1));
            for (BeaconList[] scan : mSampleBuffer.get(i)) {
                Sample sample = new Sample(x, y, scan[0], scan[1]);
                mSampleList.add(sample);
            }
        }
        mIntensityMap.invalidate();
        mSampleBuffer.clear();
    }

    private class BeaconScanAsyncTask extends AsyncTask<Void, Pair<Integer, BeaconList[]>, Void> {
        private static final long BT_SCAN_WAIT = 2000;
        private ProgressDialog progDialog;
        private int scanCount;

        public BeaconScanAsyncTask(int scanCount) {
            super();
            this.scanCount = scanCount;
            progDialog = new ProgressDialog(ConfigureActivity.this);
            progDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
            progDialog.setMax(scanCount);
            progDialog.setProgress(0);
            progDialog.setMessage("Now Scanning");
            progDialog.setCanceledOnTouchOutside(false);
            progDialog.setCancelable(true);
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            progDialog.show();
        }

        @Override
        protected void onProgressUpdate(Pair<Integer, BeaconList[]>... values) {
            super.onProgressUpdate(values);
            mAudioManager.playSoundEffect(AudioManager.FX_KEY_CLICK);
            int count = values[0].first;
            int numBt = values[0].second[0].size();
            int numWifi = values[0].second[1].size();
            Toast.makeText(ConfigureActivity.this,
                    String.format("%d times scan complete: BT(%d), WiFi(%d)", count, numBt, numWifi),
                    Toast.LENGTH_SHORT).show();
            progDialog.setProgress(count);
        }

        @Override
        protected void onPostExecute(Void value) {
            super.onPostExecute(value);
            progDialog.dismiss();
        }

        @Override
        protected void onCancelled(Void aVoid) {
            super.onCancelled(aVoid);
            Toast.makeText(ConfigureActivity.this, "cancelled", Toast.LENGTH_SHORT).show();
            progDialog.dismiss();
        }

        @Override
        protected Void doInBackground(Void... params) {
            List<BeaconList[]> pointBuffer = new ArrayList<>();
            for (int i = 1; i <= scanCount; i++) {
                mBtScanner.startScan();
                mWifiScanner.startScan();
                BeaconList[] scanBeacons = new BeaconList[2];
                try {
                    Thread.sleep(BT_SCAN_WAIT);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                while (!(mBtScanned && mWifiScanned)) ;
                mBtScanner.stopScan();
                mWifiScanner.stopScan();
                scanBeacons[0] = mBtBuffer;
                scanBeacons[1] = mWifiBuffer;
                pointBuffer.add(scanBeacons);
                publishProgress(new Pair<Integer, BeaconList[]>(i, scanBeacons));
            }
            mSampleBuffer.add(pointBuffer);
            return null;
        }
    }
}