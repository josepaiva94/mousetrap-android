package pt.up.fc.dcc.mousetrap;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;

import com.auth0.android.Auth0;
import com.auth0.android.lock.AuthenticationCallback;
import com.auth0.android.lock.Lock;
import com.auth0.android.lock.LockCallback;
import com.auth0.android.lock.utils.LockException;
import com.auth0.android.result.Credentials;
import com.auth0.android.result.UserProfile;

import java.util.Arrays;

import pt.up.fc.dcc.mousetrap.mqtt.MqttClient;
import pt.up.fc.dcc.mousetrap.utils.Alerts;
import pt.up.fc.dcc.mousetrap.utils.Auth;

public class LoginActivity extends Activity {

    private Lock mLock;

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Auth0 auth0 = new Auth0(getString(R.string.com_auth0_client_id), getString(R.string.com_auth0_domain));
        mLock = Lock.newBuilder(auth0, mCallback)
                .withScope(Constants.AUTH_SCOPE)
                .allowedConnections(Arrays.asList(Constants.AUTH_CONNECTION,
                        Constants.GOOGLE_AUTH_CONNECTION))
                //Add parameters to the builder
                .build(this);
        startActivity(mLock.newIntent(this));
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        // Your own Activity code
        mLock.onDestroy(this);
        mLock = null;
    }

    private final LockCallback mCallback = new AuthenticationCallback() {
        @Override
        public void onAuthentication(Credentials credentials) {

            // store credentials
            Auth.store(credentials);

            // update user profile
            Auth.getInstance().grabUserProfileFromAuth0(new Runnable() {
                @Override
                public void run() {

                    final UserProfile profile = Auth.getInstance().getUserProfile();

                    // redirect
                    Intent multiTrapsIntent = new Intent(getApplicationContext(), MultiTrapsActivity.class);
                    multiTrapsIntent.putExtra("traps", Auth.getInstance().getAllRegisteredDevices());
                    startActivity(multiTrapsIntent);

                    // initialize client & subscribe to registered topics
                    MqttClient.getInstance().initClient();

                    Alerts.showMessage("Welcome to MouseTrap, " + profile.getName() + "!");

                    finish();
                }
            });
        }

        @Override
        public void onCanceled() {
            Alerts.showWarning("Login canceled");
        }

        @Override
        public void onError(LockException error) {
            Alerts.showError(error.getMessage());
        }
    };

}


