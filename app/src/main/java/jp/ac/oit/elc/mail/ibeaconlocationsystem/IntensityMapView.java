package jp.ac.oit.elc.mail.ibeaconlocationsystem;

import android.content.Context;
import android.content.IntentFilter;
import android.graphics.Bitmap;
import android.media.Image;
import android.util.AttributeSet;
import android.view.View;
import android.widget.ImageView;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by yuuki on 10/6/15.
 */
public class IntensityMapView extends ImageView{
    public static final String TAG = "IntensityMap";
    private Context mContext;
    private List<IntensitySample> mSamplingList;

    public IntensityMapView(Context context) {
        super(context);
        mContext = context;
        mSamplingList = new ArrayList<>();
    }

    public IntensityMapView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public IntensityMapView(Context context, AttributeSet attrs, int defStyleAttr) {
        this(context, attrs, defStyleAttr, 0);
    }

    public IntensityMapView(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        mContext = context;
        mSamplingList = new ArrayList<>();
    }


    public void sample(int x, int y, BeaconList<BluetoothBeacon> btBeaconList, BeaconList<WifiBeacon> wifiBeaconList) {
        IntensitySample sampling = new IntensitySample(x, y, btBeaconList, wifiBeaconList);
        mSamplingList.add(sampling);
    }

}
