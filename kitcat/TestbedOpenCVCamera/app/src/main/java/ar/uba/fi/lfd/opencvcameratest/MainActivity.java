package ar.uba.fi.lfd.opencvcameratest;

import android.hardware.Camera;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.SubMenu;
import android.view.SurfaceView;
import android.widget.CompoundButton;
import android.widget.ToggleButton;

import org.opencv.android.BaseLoaderCallback;
import org.opencv.android.CameraBridgeViewBase;
import org.opencv.android.LoaderCallbackInterface;
import org.opencv.android.OpenCVLoader;
import org.opencv.core.Size;

import java.util.List;
import java.util.ListIterator;


public class MainActivity extends ActionBarActivity {
    static {
        System.loadLibrary("opencv_java3");
    }

    private static final String TAG = MainActivity.class.getName();
    private ScreenNotifier notifier;
    private CameraViewListener cameraViewListener;

    private ToggleButton grayModeButton;
    private CustomCameraView cameraView;

    private BaseLoaderCallback openCVCallback = new BaseLoaderCallback(this) {
        @Override
        public void onManagerConnected(int status) {
            if (status == LoaderCallbackInterface.SUCCESS) {
                Log.i(TAG, "OpenCV loaded successfully");
                cameraView.enableView();
            } else {
                Log.e(TAG, "Error on OpenCV loading");
                super.onManagerConnected(status);
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Log.i(TAG, "Application started.");
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        this.notifier = new ScreenNotifier(this);
        this.cameraViewListener = new CameraViewListener(this.notifier);

        this.cameraView = (CustomCameraView)this.findViewById(R.id.camera_view);
        this.grayModeButton = (ToggleButton)this.findViewById(R.id.gray_mode);

        this.cameraView.setVisibility(SurfaceView.VISIBLE);
        this.cameraView.setCvCameraViewListener(this.cameraViewListener);
        this.grayModeButton.setOnCheckedChangeListener(this.grayModeListener);
    }

    @Override
    public void onResume()
    {
        super.onResume();
        if (!OpenCVLoader.initDebug()) {
            Log.d(TAG, "Internal OpenCV library not found. Using OpenCV Manager for initialization");
            OpenCVLoader.initAsync(OpenCVLoader.OPENCV_VERSION_3_0_0, this, openCVCallback);
        } else {
            Log.d(TAG, "OpenCV library found inside package. Using it!");
            openCVCallback.onManagerConnected(LoaderCallbackInterface.SUCCESS);
        }
    }



    private List<Camera.Size> mResolutionList;
    private MenuItem[] mResolutionMenuItems;
    private SubMenu mResolutionMenu;

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);

        mResolutionMenu = menu.addSubMenu("Resolution");
        mResolutionList = cameraView.getResolutionList();
        mResolutionMenuItems = new MenuItem[mResolutionList.size()];

        ListIterator<Camera.Size> resolutionItr = mResolutionList.listIterator();
        int idx = 0;
        while(resolutionItr.hasNext()) {
            Camera.Size element = resolutionItr.next();
            mResolutionMenuItems[idx] = mResolutionMenu.add(2, idx, Menu.NONE,
                    Integer.valueOf(element.width).toString() + "x" + Integer.valueOf(element.height).toString());
            idx++;
        }
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        if (id == R.id.action_settings) {
            return true;
        }
        if (item.getGroupId() == 2)
        {
            Camera.Size resolution = mResolutionList.get(id);
            cameraView.setResolution(resolution);
            resolution = cameraView.getResolution();
            String caption = Integer.valueOf(resolution.width).toString() + "x" + Integer.valueOf(resolution.height).toString();
            notifier.showDialog(caption);
            return true;
        }
        else
            return super.onOptionsItemSelected(item);
    }

    @Override
    public void onPause()
    {
        super.onPause();
        if (cameraView != null)
            cameraView.disableView();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (cameraView != null)
            cameraView.disableView();
    }

    private CompoundButton.OnCheckedChangeListener grayModeListener = new CompoundButton.OnCheckedChangeListener(){
        @Override
        public void onCheckedChanged(CompoundButton v, boolean isChecked){
            try {
                if (isChecked) {
                    cameraViewListener.toGrayMode();
                } else {
                    cameraViewListener.toColorMode();
                }
            } catch (Exception e) {
                notifier.showError(e);
            }
        }
    };
}
