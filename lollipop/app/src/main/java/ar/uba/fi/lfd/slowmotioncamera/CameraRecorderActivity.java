package ar.uba.fi.lfd.slowmotioncamera;

import ar.uba.fi.lfd.slowmotioncamera.recording.CameraRecorder;

import android.annotation.TargetApi;
import android.app.Activity;
import android.content.Context;
import android.hardware.camera2.*;
import android.media.MediaRecorder;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.view.MotionEvent;
import android.view.TextureView;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import java.util.List;


public class CameraRecorderActivity extends Activity {
    /**
     * Whether or not the system UI should be auto-hidden after
     * {@link #AUTO_HIDE_DELAY_MILLIS} milliseconds.
     */
    private static final boolean AUTO_HIDE = true;

    /**
     * If {@link #AUTO_HIDE} is set, the number of milliseconds to wait after
     * user interaction before hiding the system UI.
     */
    private static final int AUTO_HIDE_DELAY_MILLIS = 3000;

    private CameraRecorder recorder;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_camera_recorder);

        final View checkFps = findViewById(R.id.check_fps_button);


        // Set up the user interaction to manually show or hide the system UI.
        checkFps.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View view) {
                final TextView fps = (TextView)findViewById(R.id.fps);
                final TextureView previewTexture = (TextureView)findViewById(R.id.cameraPreviewTexture);
                recorder = new CameraRecorder();
                recorder.startPreview(getBaseContext(), previewTexture,fps);
            }
        });
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (recorder != null)
            recorder.stopPreview();
    }
}
