package jp.ac.oit.elc.mail.ibeaconlocationsystem;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Point;
import android.graphics.PointF;
import android.util.AttributeSet;
import android.util.Log;

import java.util.ArrayList;
import java.util.List;

import it.sephiroth.android.library.imagezoom.ImageViewTouch;
import jp.ac.oit.elc.mail.ibeaconlocationsystem.bluetooth.BluetoothBeacon;
import jp.ac.oit.elc.mail.ibeaconlocationsystem.wifi.WifiBeacon;

/**
 * Created by yuuki on 10/6/15.
 */
public class IntensityMapView extends ImageViewTouch {
    private static final String TAG = "IntensityMap";
    private static final float SCAN_POINT_EXPECTED_RANGE = 100.0f;
    private static final float SCAN_POINT_CENTER_RADIUS = 10.0f;
    private Paint mExpectedRangePaint;
    private Paint mPointCenterPaint;
    private Context mContext;
    private List<IntensitySample> mSampleList;
    private PointF mPinPoint;
    private PointF mPinOffset;
    private Bitmap mPinBmp;


    public IntensityMapView(Context context) {
        this(context, null);
    }

    public IntensityMapView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public IntensityMapView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        mContext = context;
        mSampleList = new ArrayList<>();
        mPinBmp = BitmapFactory.decodeResource(getResources(), R.mipmap.location_pin);
        mPinPoint = new PointF();
        //tip of pin is center of bottom
        mPinOffset = new PointF(-mPinBmp.getWidth() / 2, -mPinBmp.getHeight());
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
        PointF pinOffset = calcCanvasPoint(mPinPoint.x, mPinPoint.y);
        pinOffset.offset(mPinOffset.x, mPinOffset.y);
        Matrix pinMtx = new Matrix();
        pinMtx.postTranslate(pinOffset.x, pinOffset.y);
        canvas.drawBitmap(mPinBmp, pinMtx, null);
        canvas.drawCircle(pinOffset.x - mPinOffset.x, pinOffset.y - mPinOffset.y, 20, mExpectedRangePaint);
        for (IntensitySample sample : mSampleList) {
            PointF offset = calcCanvasPoint(sample.x, sample.y);
            canvas.drawCircle(offset.x, offset.y, SCAN_POINT_EXPECTED_RANGE, mExpectedRangePaint);
            canvas.drawCircle(offset.x, offset.y, SCAN_POINT_CENTER_RADIUS, mPointCenterPaint);
        }
    }

    public void movePin(float x, float y) {
        mPinPoint.set(x, y);
        invalidate();
    }
    private PointF calcCanvasPoint(float x, float y){
        float[] imageMatrixElems = new float[9];
        getImageMatrix().getValues(imageMatrixElems);
        float left = imageMatrixElems[Matrix.MTRANS_X];
        float top = imageMatrixElems[Matrix.MTRANS_Y];
        float scaleX = imageMatrixElems[Matrix.MSCALE_X];
        float scaleY = imageMatrixElems[Matrix.MSCALE_Y];
        return new PointF(left + (x * scaleX), top + (y * scaleY));
    }

    public void sample(int x, int y, BeaconList<BluetoothBeacon> btBeaconList, BeaconList<WifiBeacon> wifiBeaconList) {
        IntensitySample sampling = new IntensitySample(x, y, btBeaconList, wifiBeaconList);
        mSampleList.add(sampling);
        Canvas canvas = new Canvas();
        Path path = new Path();
        path.addCircle(x, y, 100, Path.Direction.CW);
//        mPathList.add(path);
        invalidate();
    }

}
