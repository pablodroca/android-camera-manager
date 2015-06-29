package ar.uba.fi.lfd.testbedcamerapreviewapi1.capturing;


import java.io.IOException;
import java.util.Date;
import java.util.Observable;
import java.util.Observer;

import ar.uba.fi.lfd.testbedcamerapreviewapi1.ScreenNotifier;

public class CameraSampler implements Observer {
    private final static String TAG = CameraSampler.class.getName();
    private CameraPreview cameraPreview;
    private SamplesRecorder samplesRecorder;

    private String captureFolder;
    private ScreenNotifier notifier;

    public CameraSampler(CameraPreview cameraPreview, ScreenNotifier notifier){
        this.cameraPreview = cameraPreview;
        this.notifier = notifier;
    }

    @Override
    public void update(Observable observable, Object data) {
        if (this.samplesRecorder != null) {
            byte[] frame = (byte[])data;
            Date timestamp = new Date();
            try {
                this.samplesRecorder.recordSample(timestamp, (int)frame[0], (int)frame[1]);
            } catch (IOException e) {
                this.onError(e);
            }
        }
    }

    public void startSampling(String targetFolder) throws IOException {
        this.samplesRecorder = new SamplesRecorder(targetFolder);
        this.samplesRecorder.init();
        this.cameraPreview.addObserver(this);
    }

    public void stopSampling() throws IOException {
        this.cameraPreview.deleteObserver(this);
        this.samplesRecorder.dispose();
        double secs = this.samplesRecorder.getElapsedMillisecs() / 1000.0;
        int recordsQty = this.samplesRecorder.getRecordsQty();
        double fps = recordsQty / secs;
        this.notifier.showDialog(String.format("Sampling finished. %d samples in %d s. FPS: %.1f.", (int)secs , recordsQty, fps));
    }

    private void onError(Throwable exception) {
        this.notifier.showError(exception);
    }
}
