package pt.up.fc.dcc.mousetrap.utils;

import android.os.Handler;
import android.os.Looper;
import android.widget.Toast;

import pt.up.fc.dcc.mousetrap.App;
import pt.up.fc.dcc.mousetrap.R;

/**
 * Alert utilities
 */
public class Alerts {


    public static void showMessage(final String message) {
        new Handler(Looper.getMainLooper()).post(new Runnable() {
            @Override
            public void run() {
                Toast.makeText(App.getContext(), message, Toast.LENGTH_SHORT).show();
            }
        });
    }

    public static void showError(final String message) {

        new Handler(Looper.getMainLooper()).post(new Runnable() {
            @Override
            public void run() {
                Toast.makeText(App.getContext(), "ERROR: " + message,
                        Toast.LENGTH_LONG).show();
            }
        });
    }

    public static void showWarning(final String message) {

        new Handler(Looper.getMainLooper()).post(new Runnable() {
            @Override
            public void run() {
                Toast.makeText(App.getContext(), "WARNING: " + message,
                        Toast.LENGTH_SHORT).show();
            }
        });
    }
}
