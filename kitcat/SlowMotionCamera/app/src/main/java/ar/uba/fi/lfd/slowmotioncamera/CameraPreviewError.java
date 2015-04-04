package ar.uba.fi.lfd.slowmotioncamera;

import android.graphics.Camera;

/**
 * Created by pablo.roca on 03/04/2015.
 */
public class CameraPreviewError extends Throwable {
    public CameraPreviewError(String message){
        super(message);
    }
    public CameraPreviewError(String message, Throwable exception){
        super(message, exception);
    }
}
