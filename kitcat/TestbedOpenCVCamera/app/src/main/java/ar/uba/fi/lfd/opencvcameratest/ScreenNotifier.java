package ar.uba.fi.lfd.opencvcameratest;

import android.app.Activity;
import android.widget.Toast;

/**
 * Created by fluidodinamica on 23/04/15.
 */
public class ScreenNotifier {
    private Activity activity;

    public ScreenNotifier(Activity activity) {
        this.activity = activity;
    }

    public void showDialog(final String message) {
        this.activity.runOnUiThread(new Runnable(){
            @Override
            public void run() {
                Toast.makeText(activity, message, Toast.LENGTH_SHORT).show();
            }
        });
    }

    public void showError(final String message) {
        this.activity.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Toast.makeText(activity, message, Toast.LENGTH_SHORT).show();
            }
        });
    }

    public void showError(Throwable throwable) {
        this.showError("Unexpected error. " + throwable.getMessage());
    }
}
