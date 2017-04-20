package pt.up.fc.dcc.mousetrap.utils;

import android.content.Context;

import com.jakewharton.picasso.OkHttp3Downloader;
import com.squareup.picasso.Picasso;

import java.io.IOException;

import okhttp3.Interceptor;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import pt.up.fc.dcc.mousetrap.App;
import pt.up.fc.dcc.mousetrap.Constants;

/**
 * Provides a Picasso with auth for photo storage API
 *
 * @author José C. Paiva <up201200272@fc.up.pt>
 */
public class TrapPicasso {

    private static Picasso picasso;

    public static Picasso getPicasso(Context context) {
        if (picasso == null)
            picasso = new Picasso.Builder(context)
                    .downloader(new OkHttp3Downloader(getOkHttpClient()))
                    .build();
        return picasso;
    }

    private static Interceptor getAuthHeaderInterceptor() {

        return new Interceptor() {
            @Override
            public Response intercept(Chain chain) throws IOException {
                Request.Builder requestBuilder = chain.request().newBuilder();
                requestBuilder.addHeader(Constants.AUTHORIZATION_HEADER,
                        "Bearer " + Auth.getIdToken());
                return chain.proceed(requestBuilder.build());
            }
        };
    }

    private static OkHttpClient getOkHttpClient() {
        return new OkHttpClient.Builder()
                .addInterceptor(getAuthHeaderInterceptor())
                .build();
    }
}
