package jp.ac.oit.elc.mail.ibeaconlocationsystem;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
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
    public static final String TAG = "IntensityMap";
    private Context mContext;
    private List<IntensitySample> mSamplingList;
    private List<Path> mPathList;
    private Paint mPaint;

    public IntensityMapView(Context context) {
        super(context, null);
        mContext = context;
        mSamplingList = new ArrayList<>();
    }

    public IntensityMapView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public IntensityMapView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        mContext = context;
        mSamplingList = new ArrayList<>();
        mPathList = new ArrayList<>();
        mPaint = new Paint();
        mPaint.setColor(Color.CYAN);
        mPaint.setStyle(Paint.Style.FILL);
        mPaint.setAlpha(50);
    }


    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        for (Path path : mPathList) {
            canvas.drawPath(path, mPaint);
        }
        Log.d(TAG, "canvas");
    }

    public void sample(int x, int y, BeaconList<BluetoothBeacon> btBeaconList, BeaconList<WifiBeacon> wifiBeaconList) {
        IntensitySample sampling = new IntensitySample(x, y, btBeaconList, wifiBeaconList);
        mSamplingList.add(sampling);
        Canvas canvas = new Canvas();
        Path path = new Path();
        path.addCircle(x, y, 100, Path.Direction.CW);
        mPathList.add(path);
        invalidate();
    }

}
