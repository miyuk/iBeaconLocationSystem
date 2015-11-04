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
public class IntensityMapView extends IntensityMapViewBase{
    private static final String TAG = IntensityMapView.class.getSimpleName();
    private OnDrawListener mOnDrawListener;

    public IntensityMapView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public IntensityMapView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        // draw pin
        if (mPinBmp != null && mPinPosition != null && mPinEnabled) {
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

    public void setPinImageCoordPosition(int x, int y){
        Point p = imageToScreenCoord(x, y);
        mPinPosition.set(p.x, p.y);
        invalidate();
    }

    public Point getPinScreenCoordPosition() {
        return mPinPosition;
    }

    public Point getPinImageCoordPosition() {
        return screenToImageCoord(mPinPosition);
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

    public void setOnDrawListener(OnDrawListener onDrawListener) {
        mOnDrawListener = onDrawListener;
    }

    public void addSample(Sample sample) {
        mSampleList.add(sample);
        invalidate();
    }
    public interface OnDrawListener {
        void onDraw(Canvas canvas);
    }
}