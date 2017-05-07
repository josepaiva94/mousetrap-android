package pt.up.fc.dcc.mousetrap.models;

import android.content.Context;
import android.content.SharedPreferences;

import com.google.gson.Gson;

/**
 * Class to be extended by object that can be stored in preferences
 *
 * @author josepaiva
 */
public class ModelStore {
    private static final String PREFS_NAME = ModelStore.class.getName();

    private static SharedPreferences settings;
    private static SharedPreferences.Editor editor;

    private static ModelStore modelStore;
    private Gson gson;

    private ModelStore(Context context) {

        if(settings == null){
            settings = context.getSharedPreferences(PREFS_NAME,
                    Context.MODE_PRIVATE);
        }

        editor = settings.edit();

        gson = new Gson();
    }

    public static ModelStore getInstance(Context context) {
        if (modelStore == null)
            modelStore = new ModelStore(context);
        return modelStore;
    }

    public void store(Storable s) {
        String id = s.getStoreId();
        String json = s.getJson();
        editor.putString(id, json);
        editor.commit();
    }

    public <T extends Storable> T retrieve(String id, T dflt) {
        String json = settings.getString(id, null);

        if (json == null)
            return dflt;

        return (T) gson.fromJson(json, dflt.getClass());
    }
}
