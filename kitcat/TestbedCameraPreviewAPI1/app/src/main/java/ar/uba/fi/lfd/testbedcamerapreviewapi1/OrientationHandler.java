package ar.uba.fi.lfd.testbedcamerapreviewapi1;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Matrix;
import android.view.Display;
import android.view.Surface;
import android.view.WindowManager;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by fluidodinamica on 27/04/15.
 */
public class OrientationHandler {
    private Display display;
    private Map<Integer, Integer> displayRotationPreviewOrientationMap;

    public OrientationHandler(Context context){
        this.display = ((WindowManager)context.getSystemService(Context.WINDOW_SERVICE)).getDefaultDisplay();
        this.displayRotationPreviewOrientationMap = new HashMap<Integer, Integer>();
        this.displayRotationPreviewOrientationMap.put(Surface.ROTATION_0, 90);
        this.displayRotationPreviewOrientationMap.put(Surface.ROTATION_90, 0);
        this.displayRotationPreviewOrientationMap.put(Surface.ROTATION_180, 0);
        this.displayRotationPreviewOrientationMap.put(Surface.ROTATION_270, 180);
    }

    public Bitmap rotate(Bitmap bitmap) {
        int degrees = this.getDegrees();
        if (degrees == 0)
            return bitmap;
        else {
            int with = bitmap.getWidth();
            int height = bitmap.getHeight();

            Matrix mtx = new Matrix();
            mtx.setRotate(degrees);

            return Bitmap.createBitmap(bitmap, 0, 0, with, height, mtx, true);
        }
    }

    public int getDegrees() {
        return this.displayRotationPreviewOrientationMap.get(this.display.getOrientation());
    }
}
