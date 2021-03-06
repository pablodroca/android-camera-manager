package ar.uba.fi.lfd.testbedcamerapreviewapi1.capturing;

import android.content.Context;
import android.content.pm.PackageManager;
import android.graphics.ImageFormat;
import android.hardware.Camera;
import android.util.Log;
import android.view.TextureView;

import java.util.ArrayList;
import java.util.List;
import java.util.Observable;

import ar.uba.fi.lfd.testbedcamerapreviewapi1.OrientationHandler;
import ar.uba.fi.lfd.testbedcamerapreviewapi1.ScreenNotifier;
import ar.uba.fi.lfd.testbedcamerapreviewapi1.exceptions.CameraPreviewError;


/**
 * Created by pablo.roca on 25/03/2015.
 */
public class CameraPreview extends Observable implements Camera.PreviewCallback {
    private final static String TAG = CameraPreview.class.getName();

    private Context context;
    private TextureView previewTexture;
    private Camera camera;
    private List<int[]> supportedPreviewFps;
    private int minPreviewFPS;
    private int maxPreviewFPS;
    private boolean onPreview;
    private OrientationHandler orientationHandler;
    private ScreenNotifier notifier;
    private boolean cameraMilliFPSEnabled;
    private byte[] buffer;
    private byte[] receivedFrame;

    public CameraPreview(Context context, TextureView previewTexture, ScreenNotifier notifier){
        this.context = context;
        this.previewTexture = previewTexture;
        this.notifier = notifier;
        this.orientationHandler = new OrientationHandler(context);
    }


    public void changeFPS(int fps) throws CameraPreviewError {
        if (this.onPreview)
            this.pauseCamera();

        this.checkFPSInRange(fps);
        Camera.Parameters params = this.getCamera().getParameters();
        this.getCamera().enableShutterSound(false);
        int cameraFPS = ( this.cameraMilliFPSEnabled ) ? fps * 1000 : fps;
        params.setPreviewFpsRange(cameraFPS, cameraFPS);
        this.getCamera().setParameters(params);
        if (this.onPreview)
            this.resumeCamera();
    }

    private void checkFPSInRange(int fps) throws CameraPreviewError {
        int min = this.getMinFPS();
        int max = this.getMaxFPS();
        if (fps < min|| max < fps)
            throw new CameraPreviewError(String.format("The given FPS (%d) should be in the camera range: [%d-%d]", fps, min, max));
    }

    public void resumeCamera() throws CameraPreviewError {
        try {
            this.getCamera().startPreview();
        } catch (Exception e) {
            throw new CameraPreviewError("There was an error during the camera start preview phase", e);
        }
    }

    public void pauseCamera() throws CameraPreviewError {
        try {
            this.getCamera().stopPreview();
        } catch (Exception e) {
            throw new CameraPreviewError("There was an error during the camera start preview phase", e);
        }
    }

    public void startCamera() throws CameraPreviewError {
        try {
            this.getCamera().setDisplayOrientation(this.orientationHandler.getDegrees());
            this.getCamera().setPreviewTexture(previewTexture.getSurfaceTexture());

            this.initBuffers();

            this.getCamera().addCallbackBuffer(this.buffer);
            this.getCamera().setPreviewCallbackWithBuffer(this);
            this.getCamera().startPreview();
            this.onPreview = true;
        } catch (Exception e) {
            throw new CameraPreviewError("There was an error during the camera start preview phase", e);
        }
    }

    private void initBuffers() throws CameraPreviewError {
        Camera.Parameters params = this.getCamera().getParameters();
        Camera.Size previewSize = params.getPreviewSize();
        int bufferSize = previewSize.width * previewSize.height * ImageFormat.getBitsPerPixel(params.getPreviewFormat()) / 8;
        this.buffer = new byte[bufferSize];
        this.receivedFrame = new byte[bufferSize];
    }

    public void stopCamera() throws CameraPreviewError {
        this.onPreview = false;
        this.getCamera().stopPreview();
        this.releaseCamera();
    }

    public int getMinFPS() throws CameraPreviewError {
        if (this.supportedPreviewFps == null)
            this.collectCameraParameters();
        return this.minPreviewFPS;
    }

    public int getMaxFPS() throws CameraPreviewError {
        if (this.supportedPreviewFps == null)
            this.collectCameraParameters();
        return this.maxPreviewFPS;
    }

    public String getSupportedFPSDetails() throws CameraPreviewError {
        if (this.supportedPreviewFps == null)
            this.collectCameraParameters();
        StringBuilder builder = new StringBuilder();
        for (int[] fpsRange: this.supportedPreviewFps) {
            builder.append('[').append(fpsRange[0]).append('-').append(fpsRange[1]).append(']');
            builder.append(", ");
        }
        return builder.toString();
    }

    private void releaseCamera() {
        if (this.camera != null)
            this.camera.release();
        this.camera = null;
    }

    private void collectCameraParameters() throws CameraPreviewError {
        Camera.Parameters parameters = this.getCamera().getParameters();
        List<int[]> previewFpsRanges = parameters.getSupportedPreviewFpsRange();
        this.supportedPreviewFps = new ArrayList<int[]>();
        this.minPreviewFPS = Integer.MAX_VALUE;
        this.maxPreviewFPS = Integer.MIN_VALUE;
        for(int[] fpsRange: previewFpsRanges){
            int[] sanitizedFpsRange = this.sanitizeFpsRange(fpsRange);
            this.supportedPreviewFps.add(sanitizedFpsRange);
            if (sanitizedFpsRange[0] < this.minPreviewFPS )
                this.minPreviewFPS  = sanitizedFpsRange[0];
            if (sanitizedFpsRange[1] > this.maxPreviewFPS)
                this.maxPreviewFPS = sanitizedFpsRange[1];
        }
    }

    private int[] sanitizeFpsRange(int[] fpsToSanitize) {
        if (fpsToSanitize[0] >= 1000) {
            this.cameraMilliFPSEnabled = true;
            int[] fpsSanitized = new int[2];
            fpsSanitized[0] = (int) Math.ceil(fpsToSanitize[0] / 1000.0);
            fpsSanitized[1] = (int) Math.floor(fpsToSanitize[1] / 1000.0);
            return fpsSanitized;
        }
        else {
            this.cameraMilliFPSEnabled = false;
            return fpsToSanitize;
        }
    }


    public Camera getCamera() throws CameraPreviewError {
        if (this.camera == null) {
            Log.i(TAG, "Opening Camera...");
            this.camera = this.openCamera();
            Log.i(TAG, "Camera ready");
        }
        return this.camera;
    }

    private Camera openCamera() throws CameraPreviewError {
        if (!this.context.getPackageManager().hasSystemFeature(PackageManager.FEATURE_CAMERA)) {
            throw new CameraPreviewError("No camera on this device");
        } else {
            int cameraId = findCameraId();
            try {
                return Camera.open(cameraId);
            }
            catch(Throwable e) {
                throw new CameraPreviewError("There was an error on the camera startup. Please, try again.", e);
            }
        }
    }

    private int findCameraId() throws CameraPreviewError {
        int numberOfCameras = Camera.getNumberOfCameras();
        for (int i = 0; i < numberOfCameras; i++) {
            Camera.CameraInfo info = new Camera.CameraInfo();
            Camera.getCameraInfo(i, info);
            if (info.facing == Camera.CameraInfo.CAMERA_FACING_BACK) {
                Log.d(TAG, String.format("Camera found for ID: {0}", i));
                return i;
            }
        }
        throw new CameraPreviewError("No back facing camera found");
    }

    @Override
    public void onPreviewFrame(byte[] frame, Camera camera) {
        Log.d(TAG, "Preview Frame received. Frame size: " + frame.length + ". Buffer size: " + this.buffer.length);
        System.arraycopy(frame, 0, this.receivedFrame, 0, frame.length);

        camera.addCallbackBuffer(this.buffer);

        this.setChanged();
        this.notifyObservers(this.receivedFrame);
    }
}
