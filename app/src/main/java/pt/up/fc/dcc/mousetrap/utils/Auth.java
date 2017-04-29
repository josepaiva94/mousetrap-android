package pt.up.fc.dcc.mousetrap.utils;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

import com.auth0.android.Auth0;
import com.auth0.android.authentication.AuthenticationAPIClient;
import com.auth0.android.authentication.AuthenticationException;
import com.auth0.android.callback.BaseCallback;
import com.auth0.android.management.ManagementException;
import com.auth0.android.management.UsersAPIClient;
import com.auth0.android.result.Credentials;
import com.auth0.android.result.Delegation;
import com.auth0.android.result.UserProfile;

import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;

import pt.up.fc.dcc.mousetrap.App;
import pt.up.fc.dcc.mousetrap.Constants;
import pt.up.fc.dcc.mousetrap.mqtt.MqttClient;
import pt.up.fc.dcc.mousetrap.mqtt.Topic;

/**
 * Auth utilities
 *
 * @author José C. Paiva <up201200272@fc.up.pt>
 */
public class Auth {

    private static Auth auth = null;

    private Auth0 auth0;
    private AuthenticationAPIClient authentication;
    private UsersAPIClient management;

    private UserProfile userProfile;

    private Auth(Context context) {
        auth0 = new Auth0(context);
        auth0.setOIDCConformant(true);
        authentication = new AuthenticationAPIClient(auth0);
    }

    public static Auth getInstance() {
        if (auth == null)
            auth = new Auth(App.getContext());
        return auth;
    }

    /**
     * Store credentials in SharedPreferences
     * @param credentials Credentials object
     */
    public static void store(Credentials credentials) {
        SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(App.getContext());
        SharedPreferences.Editor editor = settings.edit();
        editor.putString(Constants.ID_TOKEN_FIELD, credentials.getIdToken());
        editor.putString(Constants.ACCESS_TOKEN_FIELD, credentials.getAccessToken());
        editor.putString(Constants.TOKEN_TYPE_FIELD, credentials.getType());
        editor.putString(Constants.REFRESH_TOKEN_FIELD, credentials.getRefreshToken());
        editor.apply();

        // grab profile
        Auth.getInstance().grabUserProfileFromAuth0(null);
    }

    /**
     * Get id token from SharedPreferences
     * @return id token from SharedPreferences
     */
    public static String getIdToken() {
        SharedPreferences settings = PreferenceManager
                .getDefaultSharedPreferences(App.getContext());
        return settings.getString(Constants.ID_TOKEN_FIELD, null);
    }

    /**
     * Get access token from SharedPreferences
     * @return access token from SharedPreferences
     */
    public static String getAccessToken() {
        SharedPreferences settings = PreferenceManager
                .getDefaultSharedPreferences(App.getContext());
        return settings.getString(Constants.ACCESS_TOKEN_FIELD, null);
    }

    /**
     * Get refresh token from SharedPreferences
     * @return refresh token from SharedPreferences
     */
    public static String getRefreshToken() {
        SharedPreferences settings = PreferenceManager
                .getDefaultSharedPreferences(App.getContext());
        return settings.getString(Constants.REFRESH_TOKEN_FIELD, null);
    }

    /**
     * Get token type from SharedPreferences
     * @return token type from SharedPreferences
     */
    public static String getTokenType() {
        SharedPreferences settings = PreferenceManager
                .getDefaultSharedPreferences(App.getContext());
        return settings.getString(Constants.TOKEN_TYPE_FIELD, null);
    }

    /**
     * Get user profile
     * @return user profile
     */
    public UserProfile getUserProfile() {
        if (userProfile == null)
            grabUserProfileFromAuth0(null);
        return userProfile;
    }

    /**
     * Get all topics in user metadata
     */
    public String[] getRegisteredTopics() {

        if (userProfile == null)
            return null;

        Map<String, Object> userMetadata = userProfile.getUserMetadata();
        Object topicsObj = userMetadata.get(Constants.USER_METADATA_TOPICS_FIELD);

        if (topicsObj == null)
            return new String[] {};

        return String.valueOf(topicsObj).split(":");
    }

    /**
     * Get all registered devices in user metadata
     */
    public String[] getAllRegisteredDevices() {

        if (userProfile == null)
            return null;

        Map<String, Object> userMetadata = userProfile.getUserMetadata();
        Object topicsObj = userMetadata.get(Constants.USER_METADATA_TOPICS_FIELD);

        if (topicsObj == null)
            return new String[] {};

        String[] topics = String.valueOf(topicsObj).split(":");

        HashSet<String> devices = new HashSet<>();
        for (String topic: topics) {
            if (topic.length() == 0)
                continue;
            devices.add(MqttClient.getTopicDevice(topic));
        }

        return devices.toArray(new String[devices.size()]);
    }

    /**
     * Register for topics of a new device
     *
     * @param id id of the device
     */
    public void registerDeviceTopics(String id, final Runnable callback) {

        Map<String, Object> userMetadata = userProfile.getUserMetadata();
        Object topicsObj = userMetadata.get(Constants.USER_METADATA_TOPICS_FIELD);

        String topicsStr = "";
        if (topicsObj != null)
            topicsStr = String.valueOf(topicsObj);

        topicsStr += !topicsStr.isEmpty() ? ":" : "";
        topicsStr += Topic.DOOR_STATE.toString(id);
        topicsStr += ":" + Topic.PICTURE.toString(id);
        topicsStr += ":" + Topic.ALERT.toString(id);
        topicsStr += ":" + Topic.TIMEOUT_ACK.toString(id);
        topicsStr += ":" + Topic.PICTURE_REQUEST.toString(id);
        topicsStr += ":" + Topic.TIMEOUT.toString(id);
        topicsStr += ":" + Topic.DOOR.toString(id);

        userMetadata = new HashMap<>(userMetadata);
        userMetadata.put(Constants.USER_METADATA_TOPICS_FIELD, topicsStr);

        if (management == null)
            management = new UsersAPIClient(auth0, getIdToken());

        System.out.println("FFFFGGGGG");

        management.updateMetadata(userProfile.getId(), userMetadata)
                .start(new BaseCallback<UserProfile, ManagementException>() {

            @Override
            public void onSuccess(UserProfile payload) {

                renewIdToken(new Runnable() {

                    @Override
                    public void run() {
                        Alerts.showMessage("SUCCESS");

                        grabUserProfileFromAuth0(new Runnable() {
                            @Override
                            public void run() {

                                if (callback != null)
                                    callback.run();
                            }
                        });
                    }
                });
            }

            @Override
            public void onFailure(ManagementException error) {
                Alerts.showError(error.getMessage());
            }
        });
    }

    /**
     * Grab user profile from Auth0
     * @return user profile
     */
    public void grabUserProfileFromAuth0(final Runnable callback) {

        authentication.userInfo(getAccessToken()).start(new BaseCallback<UserProfile,
                AuthenticationException>() {
            @Override
            public void onSuccess(UserProfile payload) {
                userProfile = payload;

                if (callback != null)
                    callback.run();

            }

            @Override
            public void onFailure(final AuthenticationException error) {
                Alerts.showError(error.getMessage());
            }
        });
    }

    /**
     * Renew ID token
     */
    public void renewIdToken(final Runnable callback) {

        authentication.delegationWithIdToken(getIdToken())
                .setScope(Constants.AUTH_SCOPE)
                .start(new BaseCallback<Delegation, AuthenticationException>() {
                    @Override
                    public void onSuccess(Delegation payload) {

                        Alerts.showMessage(payload.getIdToken());
                        store(new Credentials(payload.getIdToken(), getAccessToken(),
                                getTokenType(), getRefreshToken(), payload.getExpiresIn()));

                        if (callback != null)
                            callback.run();

                    }

                    @Override
                    public void onFailure(AuthenticationException error) {
                        Alerts.showError(error.getMessage());
                    }
                });
        /*authentication.renewAuth(getRefreshToken())
                .addParameter(Constants.SCOPE_FIELD, Constants.AUTH_SCOPE)
                .start(new BaseCallback<Credentials, AuthenticationException>() {

            @Override
            public void onSuccess(Credentials payload) {

                Alerts.showMessage(payload.getIdToken());
                store(payload);

                if (callback != null)
                    callback.run();
            }

            @Override
            public void onFailure(AuthenticationException error) {
                Alerts.showError(error.getMessage());
            }
        });*/
    }
}
