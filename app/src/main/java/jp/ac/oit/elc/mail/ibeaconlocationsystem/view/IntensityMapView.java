package jp.ac.oit.elc.mail.ibeaconlocationsystem.view;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Point;
import android.util.AttributeSet;

import it.sephiroth.android.library.imagezoom.ImageViewTouch;
import jp.ac.oit.elc.mail.ibeaconlocationsystem.BeaconList;
import jp.ac.oit.elc.mail.ibeaconlocationsystem.R;
import jp.ac.oit.elc.mail.ibeaconlocationsystem.Sample;
import jp.ac.oit.elc.mail.ibeaconlocationsystem.SampleList;
import jp.ac.oit.elc.mail.ibeaconlocationsystem.bluetooth.BluetoothBeacon;
import jp.ac.oit.elc.mail.ibeaconlocationsystem.util.CoordinateUtil;
import jp.ac.oit.elc.mail.ibeaconlocationsystem.wifi.WifiBeacon;

/**
 * Created by yuuki on 10/6/15.
 */
public class IntensityMapView extends ImageViewTouch {
    private static final String TAG = IntensityMapView.class.getSimpleName();
    private static final float SCAN_POINT_CENTER_RADIUS = 2.0F;
    private static final float SCAN_POINT_EXPECTED_RANGE = SCAN_POINT_CENTER_RADIUS * 4;
    private Paint mExpectedRangePaint;
    private Paint mPointCenterPaint;
    private SampleList mSampleList;
    private Point mPinPosition;
    private Point mUserPosition;
    private Point mPinOffset;
    private Bitmap mPinBmp;
    private OnDrawListener mOnDrawListener;
    private boolean mEnabledPin;

    @Override
    public void onWindowFocusChanged(boolean hasWindowFocus) {
        super.onWindowFocusChanged(hasWindowFocus);
        if (mPinPosition == null) {
            mPinPosition = new Point((int) getCenter().x, (int) getCenter().y);
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
        mSampleList = new SampleList();
        mPinBmp = BitmapFactory.decodeResource(getResources(), R.mipmap.location_pin);
        mPinPosition = null;
        //tip of pin is center of bottom
        mPinOffset = new Point(-mPinBmp.getWidth() / 2, -mPinBmp.getHeight());
        initPaint();
    }

    private void initPaint() {
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
        if (mPinBmp != null && mPinPosition != null && mEnabledPin) {
            canvas.drawBitmap(mPinBmp, mPinPosition.x + mPinOffset.x, mPinPosition.y + mPinOffset.y, new Paint());
        }
        //draw addSample list
        if (mSampleList != null) {
            for (Point position : mSampleList.getPositions()) {
                Point point = CoordinateUtil.imageToScreen(position.x, position.y, getImageViewMatrix());
                canvas.drawCircle(point.x, point.y, SCAN_POINT_EXPECTED_RANGE * getScale(), mExpectedRangePaint);
                canvas.drawCircle(point.x, point.y, SCAN_POINT_CENTER_RADIUS * getScale(), mPointCenterPaint);
            }
        }
        if (mOnDrawListener != null) {
            mOnDrawListener.onDraw(canvas);
        }
    }

    public void setPinScreenCoordPosition(int x, int y) {
        mPinPosition.set(x, y);
        invalidate();
    }

    public Point getPinScreenCoordPosition() {
        return mPinPosition;
    }

    public Point getPinImageCoordPosition() {
        return screenToImageCoord(mPinPosition);
    }

    public void setUserPosition(int x, int y) {
        mPinPosition.set(x, y);
        invalidate();
    }

    public Point getUserPositon() {
        return mUserPosition;
    }

    public void addSample(BeaconList<BluetoothBeacon> btBeaconList, BeaconList<WifiBeacon> wifiBeaconList) {
        Point point = CoordinateUtil.screenToImage(mPinPosition.x, mPinPosition.y, getImageViewMatrix());
        Sample sample = new Sample(point.x, point.y, btBeaconList, wifiBeaconList);
        addSample(sample);
        invalidate();
    }

    public void addSample(Sample sample) {
        mSampleList.add(sample);
        invalidate();
    }

    public SampleList getSampleList() {
        return mSampleList;
    }

    public void setSampleList(SampleList sampleList) {
        mSampleList = sampleList;
        invalidate();
    }

    public void setOnDrawListener(OnDrawListener onDrawListener) {
        mOnDrawListener = onDrawListener;
    }

    public Point screenToImageCoord(int screenX, int screenY) {
        return CoordinateUtil.screenToImage(screenX, screenY, getImageViewMatrix());
    }

    public Point screenToImageCoord(Point screenPoint) {
        if (screenPoint == null) {
            return null;
        }
        return screenToImageCoord(screenPoint.x, screenPoint.y);
    }

    public Point imageToScreenCoord(int x, int y) {
        return CoordinateUtil.imageToScreen(x, y, getImageViewMatrix());
    }

    public Point imageToScreenCoord(Point imagePoint) {
        return imageToScreenCoord(imagePoint.x, imagePoint.y);
    }

    public void setEnabledPin(boolean enabled){
        if(mEnabledPin != enabled){
            mEnabledPin = enabled;
        }
    }
    public interface OnDrawListener {
        void onDraw(Canvas canvas);
    }
}