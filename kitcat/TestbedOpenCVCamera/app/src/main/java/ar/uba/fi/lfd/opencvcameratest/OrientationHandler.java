package ar.uba.fi.lfd.opencvcameratest;

import android.content.Context;
import android.view.Display;
import android.view.Surface;
import android.view.WindowManager;

import org.opencv.core.Mat;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by pablo.roca on 28/06/2015.
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

    public Mat rotate(Mat bitmap) {
        int degrees = this.getDegrees();
        if (degrees == 0)
            return bitmap;
        else {
            int with = bitmap.width();
            int height = bitmap.height();
/*
            if (rotflag == 1){
                transpose(matImage, matImage);
                flip(matImage, matImage,1); //transpose+flip(1)=CW
            } else if (rotflag == 2) {
                transpose(matImage, matImage);
                flip(matImage, matImage,0); //transpose+flip(0)=CCW
            } else if (rotflag ==3){
                flip(matImage, matImage,-1);    //flip(-1)=180
            } else if (rotflag != 0){ //if not 0,1,2,3:
                cout  << "Unknown rotation flag(" << rotflag << ")" << endl;
            }
            org.opencv.core.Core.transpose();

            Mat mtx = new Matrix();
            mtx.setRotate(degrees);

            return Bitmap.createBitmap(bitmap, 0, 0, with, height, mtx, true);
            */
            return null;
        }
    }

    public int getDegrees() {
        return this.displayRotationPreviewOrientationMap.get(this.display.getOrientation());
    }
}
