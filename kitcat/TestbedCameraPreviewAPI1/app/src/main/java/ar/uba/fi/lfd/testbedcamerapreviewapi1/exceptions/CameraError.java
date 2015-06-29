package ar.uba.fi.lfd.testbedcamerapreviewapi1.exceptions;

/**
 * Created by pablo.roca on 10/04/2015.
 */
public class CameraError extends Throwable {
    public CameraError(String message){
        super(message);
    }
    public CameraError(String message, Throwable exception){
        super(message, exception);
    }
}
