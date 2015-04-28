package ar.uba.fi.lfd.slowmotioncamera.capturing;


import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.hardware.Camera;
import android.os.AsyncTask;
import android.util.Log;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import ar.uba.fi.lfd.slowmotioncamera.OrientationHandler;
import ar.uba.fi.lfd.slowmotioncamera.SamplesRecorder;
import ar.uba.fi.lfd.slowmotioncamera.ScreenNotifier;
import ar.uba.fi.lfd.slowmotioncamera.exceptions.CameraPreviewError;

public class CaptureTask extends AsyncTask<Void, Void, CaptureTaskResult> {
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
    private OrientationHandler orientationHandler;
    private List<CapturedImage> captures = new ArrayList<CapturedImage>();

    public CaptureTask(CameraPreview preview, ScreenNotifier notifier, OrientationHandler orientationHandler){
        this.preview = preview;
        this.notifier = notifier;
        this.orientationHandler = orientationHandler;
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
                    captures.add(new CapturedImage(data));
                }
                synchronized(captures) {
                    captures.notify();
                }
            }
        };
    }

    @Override
    protected void onPreExecute() {
        this.continueCapturing = true;
    }


    @Override
    protected CaptureTaskResult doInBackground(Void... params) {
        SamplesRecorder samplesRecorder = new SamplesRecorder(CaptureTask.this.captureFolder);
        int capturesQty = 0;
        Date startingTime = new Date();
        try {
            samplesRecorder.init();
            while(continueCapturing) {
                Thread.sleep(1000);
                synchronized(captures) {
                    preview.getCamera().takePicture(shutterCallback, rawCallback, jpgCallback);
                    captures.wait();
                    Log.d(TAG, String.format("Picture %d captured.", ++capturesQty));
                    if (captures.size() > 0) {
                        CapturedImage capture = captures.get(0);
                        byte[] data = capture.getData();
                        Date timestamp = capture.getTimestamp();
                        captures.remove(0);
                        Bitmap picture = BitmapFactory.decodeByteArray(data, 0, data.length);
                        Bitmap pictureRotated = orientationHandler.rotate(picture);
                        int data1 = pictureRotated.getPixel(100, 100);
                        int data2 = pictureRotated.getPixel(200, 200);
                        samplesRecorder.recordSample(timestamp, data1, data2);
                        /*
                        Log.d(TAG, String.format("Camera JPG image detected. Size: %d KB\n", data.length / 1024));
                        File imagesFolder = new File(CaptureTask.this.captureFolder);
                        if (!imagesFolder.exists())
                            imagesFolder.mkdirs();
                        String label = new SimpleDateFormat("yyyyMMdd-HHmmss.SSS").format(timestamp);
                        String filename = label + "capture.jpg";
                        File output = new File(imagesFolder, filename);
                        try {
                            FileOutputStream fos = new FileOutputStream(output);
                            fos.write(data);
                            fos.close();
                            Log.d(TAG, String.format("Picture captured at folder: %s. Filename: %s", captureFolder, filename));
                        } catch (IOException e) {
                            Log.e(TAG, String.format("There was an error writing the captured picture to the device storage. Folder: %s. Filename: %s", captureFolder, filename), e);
                        }*/
                    }
                }
            }
            Date endingTime = new Date();

            long millisecs = endingTime.getTime() - startingTime.getTime();
            return new CaptureTaskResult(capturesQty, millisecs);
        } catch (CameraPreviewError cameraPreviewError) {
            this.onError(cameraPreviewError);
        } catch (InterruptedException e) {
            this.onError(e);
        } catch (FileNotFoundException e) {
            this.onError(e);
        } catch (UnsupportedEncodingException e) {
            this.onError(e);
        } catch (IOException e) {
            this.onError(e);
        }
        finally {
            try {
                samplesRecorder.dispose();
            } catch (IOException e) {
                Log.e(TAG, String.format("There was an error writing the samples to the file. Folder: %s", captureFolder), e);
            }
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
    protected void onPostExecute(CaptureTaskResult result) {
        long millisecs = result.getMilliseconds();
        int capturesQty = result.getCapturesQty();
        int fps = result.getFPS();
        this.notifier.showDialog(String.format("Capture finished. Elapsed time: %d ms. Pictures: %d. FPS: %d.", millisecs, capturesQty, fps));
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
