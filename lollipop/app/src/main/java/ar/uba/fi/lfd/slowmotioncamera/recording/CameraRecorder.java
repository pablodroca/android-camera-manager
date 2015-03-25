package ar.uba.fi.lfd.slowmotioncamera.recording;

import android.content.Context;
import android.graphics.SurfaceTexture;
import android.hardware.camera2.CameraAccessException;
import android.hardware.camera2.CameraCaptureSession;
import android.hardware.camera2.CameraCharacteristics;
import android.hardware.camera2.CameraDevice;
import android.hardware.camera2.CameraManager;
import android.hardware.camera2.CameraMetadata;
import android.hardware.camera2.CaptureRequest;
import android.hardware.camera2.params.StreamConfigurationMap;
import android.os.Handler;
import android.os.HandlerThread;
import android.util.Log;
import android.util.Size;
import android.view.Surface;
import android.view.TextureView;
import android.widget.TextView;

import java.util.Arrays;

/**
 * Created by pablo.roca on 24/03/2015.
 */
public class CameraRecorder {
    private final static String TAG = CameraRecorder.class.getCanonicalName();
    private RecordingCallback callback;

    private class ConfigurationCallback extends CameraCaptureSession.StateCallback {
        private final CaptureRequest.Builder builder;
        private HandlerThread backgroundThread;

        public ConfigurationCallback(CaptureRequest.Builder builder) {
            this.builder = builder;
            this.backgroundThread = new HandlerThread("CameraPreview");
        }

        @Override
        public void onConfigured(CameraCaptureSession session) {
            Log.i(TAG, "onConfigured");
            builder.set(CaptureRequest.CONTROL_MODE, CameraMetadata.CONTROL_MODE_AUTO);

            backgroundThread.start();
            Handler backgroundHandler = new Handler(backgroundThread.getLooper());

            try {
                session.setRepeatingRequest(builder.build(), null, backgroundHandler);
            } catch (CameraAccessException e) {
                e.printStackTrace();
            }

        }

        @Override
        public void onConfigureFailed(CameraCaptureSession session) {
            // TODO Auto-generated method stub
            Log.e(TAG, "CameraCaptureSession Configure failed");
        }

        public void stop() {
            this.backgroundThread.quit();
        }
    };

    private class RecordingCallback extends CameraDevice.StateCallback {
        private final TextureView previewTexture;
        private final TextView fpsText;
        private ConfigurationCallback configurationCallback;
        private int width;
        private int height;
        private CameraDevice camera;

        public RecordingCallback(Size previewSize, final TextureView previewTexture, final TextView fpsText) {
            this.previewTexture = previewTexture;
            this.fpsText = fpsText;
            this.width = previewSize.getWidth();
            this.height = previewSize.getHeight();
        }

        @Override
        public void onOpened(CameraDevice camera) {
            Log.i(TAG, "Camera opened");
            SurfaceTexture texture = this.previewTexture.getSurfaceTexture();
            if (texture == null) {
                notifyError("Texture is null");
            }
            else {
                texture.setDefaultBufferSize(this.width, this.height);
                Surface surface = new Surface(texture);
                try {
                    CaptureRequest.Builder builder = camera.createCaptureRequest(CameraDevice.TEMPLATE_PREVIEW);
                    builder.addTarget(surface);
                    this.configurationCallback = new ConfigurationCallback(builder);
                    this.camera = camera;
                    camera.createCaptureSession(Arrays.asList(surface), this.configurationCallback, null);
                } catch (CameraAccessException e) {
                    notifyError("It is not possible to create a request in the camera.", e);
                }
            }
        }

        @Override
        public void onError(CameraDevice camera, int error) {
            notifyError(String.format("Camera error. Error code detected: {0}", error));
        }

        @Override
        public void onDisconnected(CameraDevice camera) {
            notifyError("Camera disconnected");
        }

        public void stop() {
            if (this.configurationCallback != null)
                this.configurationCallback.stop();
            if (this.camera != null)
                camera.close();
            this.configurationCallback = null;
            this.camera = null;
        }

        private void notifyError(String message) {
            Log.e(TAG, message);
        }

        private void notifyError(String message, Throwable e) {
            Log.e(TAG, message, e);
        }

    };

    public void startPreview(final Context context, final TextureView previewTexture, final TextView fpsText) {
        CameraManager manager = (CameraManager) context.getSystemService(context.CAMERA_SERVICE);
        try{
            String cameraId = manager.getCameraIdList()[1];
            CameraCharacteristics characteristics = manager.getCameraCharacteristics(cameraId);
            StreamConfigurationMap map = characteristics.get(CameraCharacteristics.SCALER_STREAM_CONFIGURATION_MAP);
            Size surfaceSize = map.getOutputSizes(SurfaceTexture.class)[0];

            RecordingCallback callback = new RecordingCallback(surfaceSize, previewTexture, fpsText);
            this.registerRecordingCallback(callback);

            manager.openCamera(cameraId, callback, null);
        }
        catch(CameraAccessException e)
        {
            e.printStackTrace();
        }
    }

    private void registerRecordingCallback(RecordingCallback callback) {
        if (this.callback != null){
            this.callback.stop();
        }
        this.callback = callback;
    }

    public void stopPreview(){
        if (this.callback != null)
            this.callback.stop();
    }
}
