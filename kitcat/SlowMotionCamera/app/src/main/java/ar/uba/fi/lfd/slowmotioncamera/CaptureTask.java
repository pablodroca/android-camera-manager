package ar.uba.fi.lfd.slowmotioncamera;


import android.hardware.Camera;
import android.os.AsyncTask;
import android.util.Log;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

import ar.uba.fi.lfd.slowmotioncamera.exceptions.CameraPreviewError;

public class CaptureTask extends AsyncTask<Void, Void, Void> {
    private final static String TAG = CaptureTask.class.getName();
    private final CameraPreview preview;
    private final Camera.PictureCallback jpgCallback;
    private final Camera.PictureCallback rawCallback;
    private final Camera.ShutterCallback shutterCallback;
    private boolean waitingCapture;
    private boolean continueCapturing;
    private String captureFolder;
    private int fps;
    private ScreenNotifier notifier;

    public CaptureTask(CameraPreview preview, ScreenNotifier notifier){
        this.preview = preview;
        this.notifier = notifier;
        this.shutterCallback = new Camera.ShutterCallback() {
            @Override
            public void onShutter() {
                Log.d(TAG, "Camera Shutter detected");
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
                    File imagesFolder = new File(CaptureTask.this.captureFolder);
                    if (!imagesFolder.exists())
                        imagesFolder.mkdirs();
                    String timestamp = new SimpleDateFormat("yyyyMMdd-HHmmss.SSS").format(new Date());
                    String filename = timestamp + "capture.jpg";
                    File output = new File(imagesFolder, filename);
                    try {
                        FileOutputStream fos = new FileOutputStream(output);
                        fos.write(data);
                        fos.close();
                        Log.d(TAG, String.format("Picture captured at folder: %s. Filename: %s", captureFolder, filename));
                    } catch (IOException e) {
                        Log.e(TAG, String.format("There was an error writing the captured picture to the device storage. Folder: %s. Filename: %s", captureFolder, filename), e);
                    }
                }
                waitingCapture = false;
            }
        };
    }

    @Override
    protected void onPreExecute() {
        this.continueCapturing = true;
    }


    @Override
    protected Void doInBackground(Void... params) {
        try {
            while(continueCapturing) {
                if (waitingCapture)
                    Thread.sleep((long)((1000 / fps)*0.25), 0);
                else
                {
                    this.waitingCapture = true;
                    preview.getCamera().takePicture(shutterCallback, rawCallback, jpgCallback);
                }
            }
        } catch (CameraPreviewError cameraPreviewError) {
            this.onError(cameraPreviewError);
        } catch (InterruptedException e) {
            this.onError(e);
        }
        return null;
    }

    private void onError(Throwable exception) {
        this.notifier.showError(exception.getMessage());
        Log.e(TAG, "There was an error trying to capture images.", exception);
    }

    @Override
    protected void onProgressUpdate(Void... update) {
        //setProgressPercent(progress[0]);
    }

    @Override
    protected void onPostExecute(Void result) {
        notifier.showDialog("Capturing finished");
    }

    public void stopCapturing() {
        this.continueCapturing = false;
    }

    public void startCapturing(String captureFolder, int fps) {
        this.captureFolder = captureFolder;
        this.fps = fps;
        this.execute();
    }
}
