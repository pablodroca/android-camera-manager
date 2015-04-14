package ar.uba.fi.lfd.slowmotioncamera.exceptions;

import android.graphics.Camera;

/**
 * Created by pablo.roca on 03/04/2015.
 */
public class CameraPreviewError extends CameraError {
    public CameraPreviewError(String message){
        super(message);
    }
    public CameraPreviewError(String message, Throwable exception){
        super(message, exception);
    }
}
