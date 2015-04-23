package ar.uba.fi.lfd.slowmotioncamera;

import android.content.res.Configuration;
import android.os.Environment;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.TextureView;
import android.view.View;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ToggleButton;

import java.io.File;

import ar.uba.fi.lfd.slowmotioncamera.exceptions.CameraCaptureError;
import ar.uba.fi.lfd.slowmotioncamera.exceptions.CameraError;
import ar.uba.fi.lfd.slowmotioncamera.exceptions.CameraPreviewError;


public class MainActivity extends ActionBarActivity {
    private final static String TAG = MainActivity.class.getName();
    private CameraPreview cameraPreview;
    private ScreenNotifier notifier;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Log.i(TAG, "Starting Application");
        final LinearLayout fpsControlPanel = (LinearLayout) findViewById(R.id.fps_control_panel);
        final Button checkFPS = (Button) findViewById(R.id.check_fps_button);
        final ToggleButton previewStartStop = (ToggleButton) findViewById(R.id.preview_start_stop);
        final ToggleButton captureStartStop = (ToggleButton) findViewById(R.id.capture_start_stop);
        final SeekBar fps = (SeekBar) findViewById(R.id.fps_bar);
        final TextView fpsLabel = (TextView) findViewById(R.id.fps_label);
        final TextureView previewTexture = (TextureView) findViewById(R.id.camera_preview_texture);
        final TextView fpsRanges = (TextView) findViewById(R.id.fps_ranges);
        final ImageView capturedImage = (ImageView) findViewById(R.id.captured_image);
        fpsControlPanel.setVisibility(View.INVISIBLE);
        previewStartStop.setVisibility(View.INVISIBLE);
        captureStartStop.setVisibility(View.INVISIBLE);

        this.notifier = new ScreenNotifier(this);
        this.cameraPreview = new CameraPreview(this, previewTexture, this.notifier);

        if (getResources().getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT)
            cameraPreview.setPortrait();
        else
            cameraPreview.setLandscape();


        fps.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                try {
                    int fps = progress + cameraPreview.getMinFPS();
                    Log.i(TAG, String.format("onProgressChanged captured. Computing FPS %d for camera...", fps));
                    cameraPreview.changeFPS(fps);
                    fpsLabel.setText(String.format("%d fps", fps));
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
        });

        checkFPS.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    fpsRanges.setText(cameraPreview.getSupportedFPSDetails());
                    int min = cameraPreview.getMinFPS();
                    int max = cameraPreview.getMaxFPS();
                    fps.setMax(max - min);
                    fps.setProgress(max - min);
                    checkFPS.setVisibility(View.INVISIBLE);
                    fpsControlPanel.setVisibility(View.VISIBLE);
                    previewStartStop.setVisibility(View.VISIBLE);
                } catch (CameraPreviewError cameraPreviewError) {
                    notifyError(cameraPreviewError);
                }
            }
        });
        previewStartStop.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener(){
            @Override
            public void onCheckedChanged(CompoundButton v, boolean isChecked){
                try {
                    if (isChecked) {
                        cameraPreview.startCamera();
                        captureStartStop.setVisibility(View.VISIBLE);
                    } else {
                        cameraPreview.stopCamera();
                        captureStartStop.setVisibility(View.INVISIBLE);
                    }
                } catch (CameraPreviewError cameraPreviewError) {
                    notifyError(cameraPreviewError);
                }
            }
        });
        captureStartStop.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener(){
            @Override
            public void onCheckedChanged(CompoundButton v, boolean isChecked){
                try {
                    if (isChecked) {
                        String targetFolder = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS), "LFD").getPath();
                        cameraPreview.startCapture(targetFolder);
                    }
                    else {
                        cameraPreview.stopCapture();
                    }
                } catch (CameraError e) {
                    notifyError(e);
                }
            }
        });
    }

    private void notifyError(CameraError cameraPreviewError) {
        notifier.showError(cameraPreviewError.getMessage());
        Log.e(TAG, "There was an error using the camera", cameraPreviewError);
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
}
