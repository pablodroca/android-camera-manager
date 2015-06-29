package ar.uba.fi.lfd.opencvcameratest;

import android.util.Log;

import org.opencv.android.CameraBridgeViewBase;
import org.opencv.core.Mat;

import java.util.Date;

/**
 * Created by pablo.roca on 27/06/2015.
 */
public class CameraViewListener implements CameraBridgeViewBase.CvCameraViewListener2 {
    private static final String TAG = CameraViewListener.class.getName();
    private boolean grayMode;
    private int counter;
    private Date fpsMeterStartingTime;
    private ScreenNotifier notifier;

    public CameraViewListener(ScreenNotifier notifier) {
        this.notifier = notifier;
    }

    @Override
    public void onCameraViewStarted(int width, int height) {
        this.fpsMeterStartingTime = new Date();
    }

    @Override
    public void onCameraViewStopped() {

    }

    @Override
    public Mat onCameraFrame(CameraBridgeViewBase.CvCameraViewFrame inputFrame) {
        ++this.counter;
        Date currentTime = new Date();
        long millisecs = currentTime.getTime() - this.fpsMeterStartingTime.getTime();
        if (millisecs > 5000) {
            float fps = this.counter / (millisecs / 1000f);
            String msg = String.format("Frames Captured at: %d x %d. Elapsed ms: %d. Qty: %d. FPS: %d" , inputFrame.rgba().width(), inputFrame.rgba().height(),
                    millisecs, this.counter, (int)fps);
            this.notifier.showDialog(msg);
            Log.i(TAG,msg);
            this.fpsMeterStartingTime = currentTime;
            this.counter = 0;
        }
        if (grayMode)
            return inputFrame.gray();
        else
            return inputFrame.rgba();
    }

    public void toGrayMode() {
        this.grayMode = true;
    }

    public void toColorMode() {
        this.grayMode = false;
    }
}
