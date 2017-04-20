package pt.up.fc.dcc.mousetrap.utils;

import android.content.SharedPreferences;
import android.preference.PreferenceManager;

import com.auth0.android.result.Credentials;

import pt.up.fc.dcc.mousetrap.App;
import pt.up.fc.dcc.mousetrap.Constants;

/**
 * Auth utilities
 *
 * @author José C. Paiva <up201200272@fc.up.pt>
 */
public class Auth {

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
        editor.apply();
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
     * Get token type from SharedPreferences
     * @return token type from SharedPreferences
     */
    public static String getTokenType() {
        SharedPreferences settings = PreferenceManager
                .getDefaultSharedPreferences(App.getContext());
        return settings.getString(Constants.TOKEN_TYPE_FIELD, null);
    }
}
