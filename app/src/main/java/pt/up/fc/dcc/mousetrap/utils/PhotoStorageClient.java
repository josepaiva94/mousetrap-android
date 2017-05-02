package pt.up.fc.dcc.mousetrap.utils;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.HttpUrl;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import pt.up.fc.dcc.mousetrap.Constants;
import pt.up.fc.dcc.mousetrap.models.TrapImage;

/**
 * Client for photo storage API
 */
public class PhotoStorageClient {

    private static final DateFormat FORMATTER;

    static {
        FORMATTER = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'");
    }

    public static void getPhotos(final String id, int n, final PhotosRunnable cb) {

        HttpUrl.Builder urlBuilder = HttpUrl.parse(Constants.getDevicePhotosUrl(id)).newBuilder();
        urlBuilder.addQueryParameter("limit", n + "");
        String url = urlBuilder.build().toString();

        Request request = new Request.Builder()
                .addHeader(Constants.AUTHORIZATION_HEADER, Auth.getTokenType() + " " + Auth.getIdToken())
                .url(url)
                .build();

        final OkHttpClient client = new OkHttpClient();
        client.newCall(request).enqueue(new Callback() {

            @Override
            public void onFailure(Call call, IOException e) {
                Alerts.showError("Failed requesting images for " + id);
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                if (!response.isSuccessful()) {
                    Alerts.showError("Failed requesting images for " + id + " with " + response.code());

                    response.close();

                    return;
                }

                try {
                    String responseData = response.body().string();
                    JSONArray json = new JSONArray(responseData);

                    TrapImage[] photos = new TrapImage[json.length()];
                    for (int i = 0; i < json.length(); i++) {

                        JSONObject jsonObject = json.getJSONObject(i);
                        String id = jsonObject.getString("_id");
                        String timestamp = jsonObject.getString("created_at");

                        try {
                            Date d = FORMATTER.parse(timestamp);
                            photos[i] = new TrapImage(id, d.getTime());
                        } catch (Exception e) {
                            photos[i] = new TrapImage(id);
                        }

                    }

                    if (cb != null) {
                        cb.setPhotos(photos);
                        cb.run();
                    }
                } catch (JSONException e) {
                    Alerts.showError(e.getMessage());
                }

                response.close();
            }
        });
    }

    public static abstract class PhotosRunnable implements Runnable {

        private TrapImage[] photos;

        public TrapImage[] getPhotos() {
            return photos;
        }

        public void setPhotos(TrapImage[] photos) {
            this.photos = photos;
        }
    }
}
