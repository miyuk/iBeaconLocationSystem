package jp.ac.oit.elc.mail.ibeaconlocationsystem.view;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Point;
import android.util.AttributeSet;

import java.util.ArrayList;
import java.util.List;

import it.sephiroth.android.library.imagezoom.ImageViewTouch;
import jp.ac.oit.elc.mail.ibeaconlocationsystem.BeaconList;
import jp.ac.oit.elc.mail.ibeaconlocationsystem.IntensitySample;
import jp.ac.oit.elc.mail.ibeaconlocationsystem.R;
import jp.ac.oit.elc.mail.ibeaconlocationsystem.bluetooth.BluetoothBeacon;
import jp.ac.oit.elc.mail.ibeaconlocationsystem.utils.CoordinateUtils;
import jp.ac.oit.elc.mail.ibeaconlocationsystem.wifi.WifiBeacon;

/**
 * Created by yuuki on 10/6/15.
 */
public class IntensityMapView extends ImageViewTouch {
    private static final String TAG = IntensityMapView.class.getSimpleName();
    private static final float SCAN_POINT_CENTER_RADIUS = 1.0F;
    private static final float SCAN_POINT_EXPECTED_RANGE = SCAN_POINT_CENTER_RADIUS * 8;
    private Paint mExpectedRangePaint;
    private Paint mPointCenterPaint;
    private Context mContext;
    private List<IntensitySample> mSampleList;
    private Point mPinPosition;
    private Point mUserPosition;
    private Point mPinOffset;
    private Bitmap mPinBmp;
    private Bitmap mMapBmp;

    @Override
    public void onWindowFocusChanged(boolean hasWindowFocus) {
        super.onWindowFocusChanged(hasWindowFocus);
        if (mPinPosition.equals(0, 0)) {
            mPinPosition.set((int) getCenter().x, (int) getCenter().y);
        }
    }

    public IntensityMapView(Context context) {
        this(context, null);
    }

    public IntensityMapView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public IntensityMapView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        setScaleType(ScaleType.MATRIX);
        mContext = context;
        mSampleList = new ArrayList<>();
        mPinBmp = BitmapFactory.decodeResource(getResources(), R.mipmap.location_pin);
        mMapBmp = BitmapFactory.decodeResource(getResources(), R.mipmap.floor_map);
        mPinPosition = new Point();
        //tip of pin is center of bottom
        mPinOffset = new Point(-mPinBmp.getWidth() / 2, -mPinBmp.getHeight());
        mUserPosition = new Point();
        mExpectedRangePaint = new Paint();
        mExpectedRangePaint.setARGB(50, 0, 255, 255);
        mExpectedRangePaint.setStyle(Paint.Style.FILL);
        mPointCenterPaint = new Paint();
        mPointCenterPaint.setColor(Color.RED);
        mPointCenterPaint.setStyle(Paint.Style.FILL);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        // draw pin
        canvas.drawBitmap(mPinBmp, mPinPosition.x + mPinOffset.x, mPinPosition.y + mPinOffset.y, null);
        //draw sample list
        for (IntensitySample sample : mSampleList) {
            Point point = CoordinateUtils.clientToScreenPoint(sample.x, sample.y, getImageViewMatrix());
            canvas.drawCircle(point.x, point.y, SCAN_POINT_EXPECTED_RANGE * getScale(), mExpectedRangePaint);
            canvas.drawCircle(point.x, point.y, SCAN_POINT_CENTER_RADIUS * getScale(), mPointCenterPaint);
        }
    }

    public void setPinPosition(int x, int y) {
        mPinPosition.set(x, y);
        invalidate();
    }

    public void setUserPosition(int x, int y) {
        mPinPosition.set(x, y);
        invalidate();
    }

    public void sample(BeaconList<BluetoothBeacon> btBeaconList, BeaconList<WifiBeacon> wifiBeaconList) {
        Point point = CoordinateUtils.screenToClientPoint(mPinPosition.x, mPinPosition.y, getImageViewMatrix());
        IntensitySample sample = new IntensitySample(point.x, point.y, btBeaconList, wifiBeaconList);
        sample(sample);
        invalidate();
    }

    public void sample(IntensitySample sample) {
        mSampleList.add(sample);
        invalidate();
    }

    public List<IntensitySample> getSampleList() {
        return mSampleList;
    }
}


