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
import jp.ac.oit.elc.mail.ibeaconlocationsystem.R;
import jp.ac.oit.elc.mail.ibeaconlocationsystem.SampleList;
import jp.ac.oit.elc.mail.ibeaconlocationsystem.util.CoordinateUtil;

/**
 * Created by yuuki on 11/4/15.
 */
public abstract class IntensityMapViewBase extends ImageViewTouch {
    protected static final float SCAN_POINT_CENTER_RADIUS = 2.0F;
    protected static final float SCAN_POINT_EXPECTED_RANGE = SCAN_POINT_CENTER_RADIUS * 4;
    protected Paint mExpectedRangePaint;
    protected Paint mPointCenterPaint;
    protected Point mPinPosition;
    protected Point mUserPosition;
    protected Point mPinOffset;
    protected Bitmap mPinBmp;
    protected boolean mPinEnabled;
    protected SampleList mSampleList;


    public IntensityMapViewBase(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        setScaleType(ScaleType.MATRIX);
        mSampleList = new SampleList();
        mPinBmp = BitmapFactory.decodeResource(getResources(), R.mipmap.location_pin);
        mPinPosition = null;
        //tip of pin is center of bottom
        mPinOffset = new Point(-mPinBmp.getWidth() / 2, -mPinBmp.getHeight());
        initPaint();
    }

    public IntensityMapViewBase(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    @Override
    public void onWindowFocusChanged(boolean hasWindowFocus) {
        super.onWindowFocusChanged(hasWindowFocus);
        if (mPinPosition == null) {
            mPinPosition = new Point((int) getCenter().x, (int) getCenter().y);
        }
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

    public void setEnabledPin(boolean enabled){
        if(mPinEnabled != enabled){
            mPinEnabled = enabled;
        }
    }


    public SampleList getSampleList() {
        return mSampleList;
    }

    public void setSampleList(SampleList sampleList) {
        mSampleList = sampleList;
        invalidate();
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

}
