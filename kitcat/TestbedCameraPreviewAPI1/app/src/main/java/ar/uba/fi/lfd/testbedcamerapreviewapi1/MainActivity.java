package ar.uba.fi.lfd.testbedcamerapreviewapi1;

import android.app.Activity;
import android.content.DialogInterface;
import android.os.Environment;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.TextureView;
import android.view.View;
import android.widget.CompoundButton;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.ToggleButton;

import java.io.File;
import java.io.IOException;
import java.util.Observer;

import ar.uba.fi.lfd.testbedcamerapreviewapi1.capturing.CameraPreview;
import ar.uba.fi.lfd.testbedcamerapreviewapi1.capturing.CameraSampler;
import ar.uba.fi.lfd.testbedcamerapreviewapi1.exceptions.CameraError;
import ar.uba.fi.lfd.testbedcamerapreviewapi1.exceptions.CameraPreviewError;


public class MainActivity extends Activity {

    private final static String TAG = MainActivity.class.getName();
    private static final String SAMPLES_FOLDER = "LFD_SAMPLES";
    private CameraPreview cameraPreview;
    private ScreenNotifier notifier;
    public CameraSampler cameraSampler;
    private TextView fpsCaption;
    private SeekBar fps;
    private ToggleButton samplingStartStop;
    private ToggleButton previewStartStop;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        this.previewStartStop = (ToggleButton) findViewById(R.id.preview_start_stop);
        this.samplingStartStop = (ToggleButton) findViewById(R.id.sampling_start_stop);
        this.fps = (SeekBar) findViewById(R.id.fps_bar);
        this.fpsCaption = (TextView) findViewById(R.id.fps_caption);
        final TextureView previewTexture = (TextureView) findViewById(R.id.camera_preview_texture);

        this.notifier = new ScreenNotifier(this);
        this.cameraPreview = new CameraPreview(this, previewTexture, this.notifier);
        this.cameraSampler = new CameraSampler(this.cameraPreview, this.notifier);

        this.fps.setEnabled(false);
        this.fps.setOnSeekBarChangeListener(this.fpsChangeListener);

        this.previewStartStop.setOnCheckedChangeListener(this.previewStartStopListener);

        this.samplingStartStop.setEnabled(false);
        this.samplingStartStop.setOnCheckedChangeListener(this.samplingStartStopListener);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
    private SeekBar.OnSeekBarChangeListener fpsChangeListener = new SeekBar.OnSeekBarChangeListener() {
        @Override
        public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
            try {
                int fps = progress + cameraPreview.getMinFPS();
                Log.i(TAG, String.format("onProgressChanged captured. Computing FPS %d for camera...", fps));
                cameraPreview.changeFPS(fps);
                fpsCaption.setText(String.format("%d fps", fps));
            } catch (CameraPreviewError cameraPreviewError) {
                notifyError(cameraPreviewError);
            }
            Log.i(TAG, "onProgressChanged captured. Finished computing FPS for camera");
        }
        @Override
        public void onStartTrackingTouch(SeekBar seekBar) {
        }
        @Override
        public void onStopTrackingTouch(SeekBar seekBar) {
        }
    };

    private CompoundButton.OnCheckedChangeListener previewStartStopListener = new CompoundButton.OnCheckedChangeListener(){
        @Override
        public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
            try {
                if (isChecked) {
                    int min = cameraPreview.getMinFPS();
                    int max = cameraPreview.getMaxFPS();
                    fps.setMax(max - min);
                    fps.setProgress(max - min);
                    fps.setEnabled(true);

                    samplingStartStop.setEnabled(true);
                    cameraPreview.startCamera();
                } else {
                    fps.setEnabled(false);
                    samplingStartStop.setEnabled(false);
                    samplingStartStop.setChecked(false);
                    cameraPreview.stopCamera();
                }
            } catch (CameraPreviewError cameraPreviewError) {
                notifyError(cameraPreviewError);
            }
        }
    };

    private CompoundButton.OnCheckedChangeListener samplingStartStopListener = new CompoundButton.OnCheckedChangeListener() {

        @Override
        public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
            try {
                if (isChecked) {
                    String targetFolder = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS), SAMPLES_FOLDER).getPath();
                    cameraSampler.startSampling(targetFolder);
                } else {
                    cameraSampler.stopSampling();
                }
            } catch (IOException e) {
                notifyError(e);
            }
        }
    };

    private void notifyError(Throwable exception) {
        notifier.showError(exception.getMessage());
        Log.e(TAG, "There was an error using the camera", exception);
    }

}
