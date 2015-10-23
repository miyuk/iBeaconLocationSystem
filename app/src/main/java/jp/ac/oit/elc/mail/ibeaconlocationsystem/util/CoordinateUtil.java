package jp.ac.oit.elc.mail.ibeaconlocationsystem.util;

import android.graphics.Bitmap;
import android.graphics.Matrix;
import android.graphics.Point;
import android.graphics.PointF;
import android.graphics.RectF;

/**
 * Created by yuuki on 10/13/15.
 */
public class CoordinateUtil {
    public static PointF screenToImage(float screenX, float screenY, Matrix screenMatrix) {
        float[] scrElems = new float[9];
        screenMatrix.getValues(scrElems);
        float left = scrElems[Matrix.MTRANS_X];
        float top = scrElems[Matrix.MTRANS_Y];
        float scaleX = scrElems[Matrix.MSCALE_X];
        float scaleY = scrElems[Matrix.MSCALE_Y];
        return new PointF((screenX - left) / scaleX, (screenY - top) / scaleY);
    }

    public static PointF imageToScreen(float imageX, float imageY, Matrix screenMatrix) {
        float[] scrElems = new float[9];
        screenMatrix.getValues(scrElems);
        float left = scrElems[Matrix.MTRANS_X];
        float top = scrElems[Matrix.MTRANS_Y];
        float scaleX = scrElems[Matrix.MSCALE_X];
        float scaleY = scrElems[Matrix.MSCALE_Y];
        return new PointF((scaleX * imageX) + left, (scaleY * imageY) + top);
    }

    public static Point screenToImage(int screenX, int screenY, Matrix screenMatrix) {
        float[] scrElems = new float[9];
        screenMatrix.getValues(scrElems);
        float left = scrElems[Matrix.MTRANS_X];
        float top = scrElems[Matrix.MTRANS_Y];
        float scaleX = scrElems[Matrix.MSCALE_X];
        float scaleY = scrElems[Matrix.MSCALE_Y];
        return new Point((int) ((screenX - left) / scaleX), (int) ((screenY - top) / scaleY));
    }

    public static Point imageToScreen(int imageX, int imageY, Matrix screenMatrix) {
        float[] scrElems = new float[9];
        screenMatrix.getValues(scrElems);
        float left = scrElems[Matrix.MTRANS_X];
        float top = scrElems[Matrix.MTRANS_Y];
        float scaleX = scrElems[Matrix.MSCALE_X];
        float scaleY = scrElems[Matrix.MSCALE_Y];
        return new Point((int) ((scaleX * imageX) + left), (int) ((scaleY * imageY) + top));
    }

    public static RectF screenToImage(RectF scrRect, Matrix scrMatrix) {
        PointF topLeft = screenToImage(scrRect.left, scrRect.top, scrMatrix);
        PointF bottomRight = screenToImage(scrRect.bottom, scrRect.right, scrMatrix);
        return new RectF(topLeft.x, topLeft.y, bottomRight.x, bottomRight.y);
    }

    public static RectF imageToScreen(RectF imageRect, Matrix scrMatrix) {
        PointF topLeft = imageToScreen(imageRect.left, imageRect.top, scrMatrix);
        PointF bottomRight = imageToScreen(imageRect.bottom, imageRect.right, scrMatrix);
        return new RectF(topLeft.x, topLeft.y, bottomRight.x, bottomRight.y);
    }

    public static RectF calcBmpRect(Bitmap bmp) {
        return new RectF(0, 0, bmp.getWidth(), bmp.getHeight());
    }

}
