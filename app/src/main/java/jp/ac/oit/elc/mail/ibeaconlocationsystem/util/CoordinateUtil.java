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
    public static PointF screenToClientPoint(float screenX, float screenY, Matrix screenMatrix) {
        float[] scrElems = new float[9];
        screenMatrix.getValues(scrElems);
        float left = scrElems[Matrix.MTRANS_X];
        float top = scrElems[Matrix.MTRANS_Y];
        float scaleX = scrElems[Matrix.MSCALE_X];
        float scaleY = scrElems[Matrix.MSCALE_Y];
        return new PointF((screenX - left) / scaleX, (screenY - top) / scaleY);
    }

    public static PointF clientToScreenPoint(float clientX, float clientY, Matrix screenMatrix) {
        float[] scrElems = new float[9];
        screenMatrix.getValues(scrElems);
        float left = scrElems[Matrix.MTRANS_X];
        float top = scrElems[Matrix.MTRANS_Y];
        float scaleX = scrElems[Matrix.MSCALE_X];
        float scaleY = scrElems[Matrix.MSCALE_Y];
        return new PointF((scaleX * clientX) + left, (scaleY * clientY) + top);
    }

    public static Point screenToClientPoint(int screenX, int screenY, Matrix screenMatrix) {
        float[] scrElems = new float[9];
        screenMatrix.getValues(scrElems);
        float left = scrElems[Matrix.MTRANS_X];
        float top = scrElems[Matrix.MTRANS_Y];
        float scaleX = scrElems[Matrix.MSCALE_X];
        float scaleY = scrElems[Matrix.MSCALE_Y];
        return new Point((int) ((screenX - left) / scaleX), (int) ((screenY - top) / scaleY));
    }

    public static Point clientToScreenPoint(int clientX, int clientY, Matrix screenMatrix) {
        float[] scrElems = new float[9];
        screenMatrix.getValues(scrElems);
        float left = scrElems[Matrix.MTRANS_X];
        float top = scrElems[Matrix.MTRANS_Y];
        float scaleX = scrElems[Matrix.MSCALE_X];
        float scaleY = scrElems[Matrix.MSCALE_Y];
        return new Point((int) ((scaleX * clientX) + left), (int) ((scaleY * clientY) + top));
    }

    public static RectF screenToClientRect(RectF scrRect, Matrix scrMatrix) {
        PointF topLeft = screenToClientPoint(scrRect.left, scrRect.top, scrMatrix);
        PointF bottomRight = screenToClientPoint(scrRect.bottom, scrRect.right, scrMatrix);
        return new RectF(topLeft.x, topLeft.y, bottomRight.x, bottomRight.y);
    }

    public static RectF clientToScreenRect(RectF clientRect, Matrix scrMatrix) {
        PointF topLeft = clientToScreenPoint(clientRect.left, clientRect.top, scrMatrix);
        PointF bottomRight = clientToScreenPoint(clientRect.bottom, clientRect.right, scrMatrix);
        return new RectF(topLeft.x, topLeft.y, bottomRight.x, bottomRight.y);
    }

    public static RectF calcBmpRect(Bitmap bmp) {
        return new RectF(0, 0, bmp.getWidth(), bmp.getHeight());
    }

}
