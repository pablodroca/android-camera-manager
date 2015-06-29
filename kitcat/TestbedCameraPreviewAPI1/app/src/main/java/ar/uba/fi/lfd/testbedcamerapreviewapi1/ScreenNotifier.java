package ar.uba.fi.lfd.testbedcamerapreviewapi1;

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

    public void showDialog(String message) {
        Toast.makeText(this.activity, message, Toast.LENGTH_SHORT).show();
    }

    public void showError(String message) {
        Toast.makeText(this.activity, message, Toast.LENGTH_SHORT).show();
    }

    public void showError(Throwable exception) {
        this.showError("An unexpected error has occurred. " + exception.getMessage());
    }
}
