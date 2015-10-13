package jp.ac.oit.elc.mail.ibeaconlocationsystem;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PointF;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.inputmethod.CorrectionInfo;

import java.util.ArrayList;
import java.util.List;

import it.sephiroth.android.library.imagezoom.ImageViewTouch;
import jp.ac.oit.elc.mail.ibeaconlocationsystem.bluetooth.BluetoothBeacon;
import jp.ac.oit.elc.mail.ibeaconlocationsystem.utils.CoordinateUtils;
import jp.ac.oit.elc.mail.ibeaconlocationsystem.wifi.WifiBeacon;

/**
 * Created by yuuki on 10/6/15.
 */
public class IntensityMapView extends ImageViewTouch {
    private static final String TAG = "IntensityMap";
    private static final float SCAN_POINT_EXPECTED_RANGE = 100.0F;
    private static final float SCAN_POINT_CENTER_RADIUS = 10.0F;
    private Paint mExpectedRangePaint;
    private Paint mPointCenterPaint;
    private Context mContext;
    private List<IntensitySample> mSampleList;
    private PointF mPinPosition;
    private PointF mUserPosition;
    private PointF mPinOffset;
    private Bitmap mPinBmp;
    private Bitmap mMapBmp;
    private boolean mLocksMap;


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
        mPinPosition = new PointF();
        //tip of pin is center of bottom
        mPinOffset = new PointF(-mPinBmp.getWidth() / 2.0F, -mPinBmp.getHeight());
        mUserPosition = new PointF();
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
            PointF point = CoordinateUtils.clientToScreenPoint(sample.x, sample.y, getImageViewMatrix());
            canvas.drawCircle(point.x, point.y, SCAN_POINT_EXPECTED_RANGE * getScale(), mExpectedRangePaint);
            canvas.drawCircle(point.x, point.y, SCAN_POINT_CENTER_RADIUS * getScale(), mPointCenterPaint);
        }
    }

    public void setPinPosition(float x, float y) {
        mPinPosition.set(x, y);
        invalidate();
    }

    public void sample(BeaconList<BluetoothBeacon> btBeaconList, BeaconList<WifiBeacon> wifiBeaconList) {
        PointF point = CoordinateUtils.screenToClientPoint(mPinPosition.x, mPinPosition.y, getImageViewMatrix());
        IntensitySample sample = new IntensitySample((int) point.x, (int) point.y, btBeaconList, wifiBeaconList);
        mSampleList.add(sample);
        invalidate();
    }

}


