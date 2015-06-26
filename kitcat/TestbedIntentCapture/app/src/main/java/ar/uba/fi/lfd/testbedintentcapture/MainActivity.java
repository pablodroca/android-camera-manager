package ar.uba.fi.lfd.testbedintentcapture;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ToggleButton;


public class MainActivity extends Activity {
    private final int CAMERA_CAPTURE_CODE = 1;
    private final static String TAG = MainActivity.class.getName();

    private ScreenNotifier notifier;
    private ImageView capturedImage;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        this.notifier = new ScreenNotifier(this);
        Log.i(TAG, "Starting Application");
        final LinearLayout fpsControlPanel = (LinearLayout) findViewById(R.id.fps_control_panel);
        final Button capture = (Button) findViewById(R.id.capture);
        final ToggleButton previewStartStop = (ToggleButton) findViewById(R.id.preview_start_stop);
        capturedImage = (ImageView) findViewById(R.id.captured_image);

        capture.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    //use standard intent to capture an image
                    Intent captureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                    //we will handle the returned data in onActivityResult
                    startActivityForResult(captureIntent, CAMERA_CAPTURE_CODE);

                } catch (Exception cameraPreviewError) {
                    notifyError(cameraPreviewError);
                }
            }
        });

    }
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode == RESULT_OK) {
            if(requestCode == CAMERA_CAPTURE_CODE){
                Bundle extras = data.getExtras();
                Bitmap thePic = extras.getParcelable("data");

                Log.i(TAG, String.format("Picture captured. Size: %d x %d", thePic.getWidth(), thePic.getHeight()));
                capturedImage.setImageBitmap(thePic);
            }
        }
    }

    private void notifyError(Exception exception) {
        this.notifier.showError(exception.getMessage());
        Log.e(TAG, "There was an error using the camera", exception);
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
