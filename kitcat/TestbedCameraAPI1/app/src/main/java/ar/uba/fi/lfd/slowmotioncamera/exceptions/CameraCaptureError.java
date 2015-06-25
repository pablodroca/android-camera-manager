package ar.uba.fi.lfd.slowmotioncamera.exceptions;

/**
 * Created by pablo.roca on 10/04/2015.
 */
public class CameraCaptureError extends CameraError {
    public CameraCaptureError(String message){
        super(message);
    }
    public CameraCaptureError(String message, Throwable exception){
        super(message, exception);
    }
}
