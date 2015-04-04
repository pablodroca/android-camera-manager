package ar.uba.fi.lfd.slowmotioncamera;

import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.SurfaceTexture;
import android.hardware.Camera;
import android.provider.MediaStore;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.TextureView;
import android.view.View;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.NumberPicker;
import android.widget.TextView;
import android.widget.Toast;

import java.io.IOException;
import java.util.Iterator;
import java.util.List;


public class MainActivity extends ActionBarActivity {
    private final static String TAG = MainActivity.class.getName();
    private CameraPreview cameraPreview;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Log.i(TAG, "Starting Application");
        Button checkFPS = (Button)findViewById(R.id.check_fps_button);
        final Button startPreview = (Button)findViewById(R.id.camera_preview_button);
        final Button stopPreview = (Button)findViewById(R.id.camera_stop_button);
        final NumberPicker minFPS = (NumberPicker)findViewById(R.id.min_fps);
        final NumberPicker maxFPS = (NumberPicker)findViewById(R.id.max_fps);
        TextureView previewTexture = (TextureView) findViewById(R.id.cameraPreviewTexture);
        final TextView fpsRanges = (TextView)findViewById(R.id.fps_ranges   );

        this.cameraPreview = new CameraPreview(this, previewTexture);

        NumberPicker.OnValueChangeListener fpsPickerListener = new NumberPicker.OnValueChangeListener() {
            @Override
            public void onValueChange(NumberPicker picker, int oldVal, int newVal) {
                Log.i(TAG, "OnValueChange captured. Computing FPS for camera...");
                if (minFPS.getValue() >= maxFPS.getValue())
                    picker.setValue(oldVal);
                else
                    try {
                        cameraPreview.changeFPS(minFPS.getValue(), maxFPS.getValue());
                    } catch (CameraPreviewError cameraPreviewError) {
                        notifyError(cameraPreviewError);
                    }
                Log.i(TAG, "OnValueChange captured. Finished computing FPS for camera");
            }
        };
        minFPS.setOnValueChangedListener(fpsPickerListener);
        maxFPS.setOnValueChangedListener(fpsPickerListener);

        checkFPS.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    fpsRanges.setText(cameraPreview.getSupportedFPSDetails());
                    int min = cameraPreview.getMinFPS();
                    int max = cameraPreview.getMaxFPS();
                    minFPS.setMinValue(min);
                    minFPS.setMaxValue(max);
                    minFPS.setValue(min);
                    maxFPS.setMinValue(min);
                    maxFPS.setMaxValue(max);
                    maxFPS.setValue(max);

                    startPreview.setVisibility(View.VISIBLE);
                } catch (CameraPreviewError cameraPreviewError) {
                    notifyError(cameraPreviewError);
                }
            }
        });
        startPreview.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    cameraPreview.startCamera();
                    startPreview.setVisibility(View.INVISIBLE);
                    stopPreview.setVisibility(View.VISIBLE);
                } catch (CameraPreviewError cameraPreviewError) {
                    notifyError(cameraPreviewError);
                }
            }
        });

        stopPreview.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    cameraPreview.stopCamera();
                    startPreview.setVisibility(View.VISIBLE);
                    stopPreview.setVisibility(View.INVISIBLE);
                } catch (CameraPreviewError cameraPreviewError) {
                    notifyError(cameraPreviewError);
                }
            }
        });
    }

    private void notifyError(CameraPreviewError cameraPreviewError) {
        Toast.makeText(MainActivity.this, cameraPreviewError.getMessage(), Toast.LENGTH_LONG).show();
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
