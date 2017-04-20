package pt.up.fc.dcc.mousetrap;

import android.app.Application;
import android.content.Context;

/**
 * Application class
 */
public class App extends Application {

    protected static Context context;

    @Override
    public void onCreate() {
        super.onCreate();
        context = getApplicationContext();
    }

    public static Context getContext() {
        return context;
    }
}
