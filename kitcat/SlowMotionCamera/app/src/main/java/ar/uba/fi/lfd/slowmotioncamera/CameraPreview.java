package ar.uba.fi.lfd.slowmotioncamera;

import android.content.Context;
import android.content.pm.PackageManager;
import android.hardware.Camera;
import android.util.Log;
import android.view.Surface;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.TextureView;
import android.widget.Toast;

import java.io.IOException;
import java.util.List;

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

    public CameraPreview(Context context, TextureView previewTexture){
        this.context = context;
        this.previewTexture = previewTexture;
    }


    public void changeFPS(int minFPS, int maxFPS) throws CameraPreviewError {
        if (this.onPreview)
            this.pauseCamera();
        Camera.Parameters params = this.getCamera().getParameters();
        params.setPreviewFpsRange(minFPS * 1000, maxFPS * 1000);
        this.getCamera().setParameters(params);
        if (this.onPreview)
            this.resumeCamera();
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
        builder.append("Available FPS: ");
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
            if (fpsRange[0] < this.minPreviewFPS )
                this.minPreviewFPS  = fpsRange[0];
            if (fpsRange[1] > this.maxPreviewFPS)
                this.maxPreviewFPS = fpsRange[1];
        }
        if (this.minPreviewFPS >= 1000)
            this.minPreviewFPS = this.minPreviewFPS / 1000;
        if (this.maxPreviewFPS >= 1000)
            this.maxPreviewFPS = this.maxPreviewFPS / 1000;
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
}
