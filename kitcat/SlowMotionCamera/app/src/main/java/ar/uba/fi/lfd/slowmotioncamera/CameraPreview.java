package ar.uba.fi.lfd.slowmotioncamera;

import android.content.Context;
import android.content.pm.PackageManager;
import android.hardware.Camera;
import android.util.Log;
import android.view.TextureView;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import ar.uba.fi.lfd.slowmotioncamera.exceptions.CameraCaptureError;
import ar.uba.fi.lfd.slowmotioncamera.exceptions.CameraError;
import ar.uba.fi.lfd.slowmotioncamera.exceptions.CameraPreviewError;

/**
 * Created by pablo.roca on 25/03/2015.
 */
public class CameraPreview {
    private final static String TAG = CameraPreview.class.getName();

    private Context context;
    private TextureView previewTexture;
    private Camera camera;
    private List<int[]> supportedPreviewFps;
    private int minPreviewFPS;
    private int maxPreviewFPS;
    private boolean onPreview;
    private boolean onCapture;
    private String captureFolder;
    private Camera.PictureCallback jpgCallback;
    private Camera.PictureCallback rawCallback;
    private Camera.ShutterCallback shutterCallback;
    private boolean orientationLandscape;

    public CameraPreview(Context context, TextureView previewTexture){
        this.context = context;
        this.previewTexture = previewTexture;
        this.shutterCallback = new Camera.ShutterCallback() {
            @Override
            public void onShutter() {
                Log.d(TAG, "Camera Shutter detected");
                if (onCapture) {
                    try {
                        Log.d(TAG, "New Capture scheduled");
                        CameraPreview.this.getCamera().takePicture(shutterCallback, rawCallback,jpgCallback);
                    } catch (CameraError e) {
                        Log.e(TAG, "There was an error taking pictures with the camera", e);
                    }
                }
                else
                    Log.d(TAG, "No new capture required. Capture stopped.");
            }
        };

        this.rawCallback = new Camera.PictureCallback() {
            @Override
            public void onPictureTaken(byte[] data, Camera camera) {
                if (data == null)
                    Log.d(TAG, "Empty RAW image detected");
                else
                    Log.d(TAG, String.format("Camera RAW image detected. Size: %d KB", data.length / 1024));

            }
        };
        this.jpgCallback = new Camera.PictureCallback() {
            @Override
            public void onPictureTaken(byte[] data, Camera camera) {
                if (data == null)
                    Log.d(TAG, "Empty JPG image detected");
                else {
                    Log.d(TAG, String.format("Camera JPG image detected. Size: %d KB\n", data.length / 1024));
                    File imagesFolder = new File(CameraPreview.this.captureFolder);
                    if (!imagesFolder.exists())
                        imagesFolder.mkdirs();
                    String timestamp = new SimpleDateFormat("yyyyMMdd-HHmmss").format(new Date());
                    String filename = timestamp + "capture.jpg";
                    File output = new File(imagesFolder, filename);
                    try {
                        FileOutputStream fos = new FileOutputStream(output);
                        fos.write(data);
                        fos.close();
                    } catch (IOException e) {
                        Log.e(TAG, String.format("There was an error writing the captured picture to the device storage. Folder: %s. Filename: %s", CameraPreview.this.captureFolder, filename), e);
                    }
                }
            }
        };
    }


    public void changeFPS(int fps) throws CameraPreviewError {
        if (this.onPreview)
            this.pauseCamera();

        this.checkFPSInRange(fps);
        Camera.Parameters params = this.getCamera().getParameters();
        this.getCamera().enableShutterSound(false);
        if (this.orientationLandscape)
            params.set("orientation", "landscape");
        else
            params.set("orientation", "portrait");
        params.setPreviewFpsRange(fps * 1000, fps * 1000);
        this.getCamera().setParameters(params);
        if (this.onPreview)
            this.resumeCamera();
    }

    public void startCapture(String folder) throws CameraCaptureError, CameraPreviewError {
        if (!this.onPreview)
            throw new CameraCaptureError("It is not possible to capture images since preview was not activated");
        Log.d(TAG, "Starting Capture...");
        this.onCapture = true;
        this.captureFolder = folder;
        this.getCamera().takePicture(shutterCallback, rawCallback, jpgCallback);
    }

    public void stopCapture() {
        Log.d(TAG, "Stopping Capture...");
        this.onCapture = false;
    }

    private void checkFPSInRange(int fps) throws CameraPreviewError {
        int min = this.getMinFPS();
        int max = this.getMaxFPS();
        if (fps < min|| max < fps)
            throw new CameraPreviewError(String.format("The given FPS (%d) should be in the camera range: [%d-%d]", fps, min, max));
    }

    private void resumeCamera() throws CameraPreviewError {
        try {
            this.getCamera().startPreview();
        } catch (Exception e) {
            throw new CameraPreviewError("There was an error during the camera start preview phase", e);
        }
    }

    private void pauseCamera() throws CameraPreviewError {
        try {
            this.getCamera().stopPreview();
        } catch (Exception e) {
            throw new CameraPreviewError("There was an error during the camera start preview phase", e);
        }
    }

    public void startCamera() throws CameraPreviewError {
        try {
            this.getCamera().setPreviewTexture(previewTexture.getSurfaceTexture());
            this.getCamera().startPreview();
            this.onPreview = true;
        } catch (Exception e) {
            throw new CameraPreviewError("There was an error during the camera start preview phase", e);
        }
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
        this.supportedPreviewFps = parameters.getSupportedPreviewFpsRange();
        this.minPreviewFPS = Integer.MAX_VALUE;
        this.maxPreviewFPS = Integer.MIN_VALUE;
        for(int[] fpsRange: supportedPreviewFps){
            fpsRange[0] = this.sanitizeFPS(fpsRange[0]);
            fpsRange[1] = this.sanitizeFPS(fpsRange[1]);
            if (fpsRange[0] < this.minPreviewFPS )
                this.minPreviewFPS  = fpsRange[0];
            if (fpsRange[1] > this.maxPreviewFPS)
                this.maxPreviewFPS = fpsRange[1];
        }
    }

    private int sanitizeFPS(int fpsToSanitize) {
        return (fpsToSanitize >= 1000)? fpsToSanitize / 1000 : fpsToSanitize;
    }

    private Camera getCamera() throws CameraPreviewError {
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

    public void setPortrait() {
        this.orientationLandscape = false;
    }

    public void setLandscape() {
        this.orientationLandscape = true;
    }
}
