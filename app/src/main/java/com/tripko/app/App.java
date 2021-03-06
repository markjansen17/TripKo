package com.tripko.app;

import android.app.Application;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.tripko.model.data.User;

import java.util.concurrent.TimeUnit;

import io.realm.Realm;
import io.realm.RealmConfiguration;
import okhttp3.OkHttpClient;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;


public class App extends Application {

    private static App sInstance;
    private OkHttpClient.Builder httpClient;
    private Retrofit retrofit;
    private ApiInterface apiInterface;
    private Retrofit retrofitMedia;

    @Override
    public void onCreate() {
        super.onCreate();
        sInstance = this;
        Realm.init(this);
        RealmConfiguration realmConfig = new RealmConfiguration.Builder()
                .deleteRealmIfMigrationNeeded()
                .build();
        Realm.setDefaultConfiguration(realmConfig);


    }

    public synchronized static App getInstance() {
        return sInstance;
    }


    private OkHttpClient.Builder getOkHttpClient() {
        if (httpClient == null) {
            // setup logs for debugging of http request
            HttpLoggingInterceptor logging = new HttpLoggingInterceptor();
            // set your desired log level
            logging.setLevel(HttpLoggingInterceptor.Level.BODY);

            httpClient = new OkHttpClient.Builder();
            httpClient.connectTimeout(120, TimeUnit.SECONDS);
            httpClient.readTimeout(120, TimeUnit.SECONDS);
            httpClient.writeTimeout(120, TimeUnit.SECONDS);

            // add logging as last interceptor
            httpClient.addNetworkInterceptor(logging);
            httpClient.addInterceptor(logging);// <-- this is the important line!
        }
        return httpClient;


    }

    private Retrofit getClient() {
        if (retrofit == null) {

            Gson gson = new GsonBuilder()
                    .setDateFormat("yyyy-MM-dd HH:mm:ss")
                    .create();
            String url = Endpoints.API_URL;
            retrofit = new Retrofit.Builder()
                    .baseUrl(url)
                    .addConverterFactory(GsonConverterFactory.create(gson))
                    .client(getOkHttpClient().build())
                    .build();
        }
        return retrofit;
    }

    public ApiInterface getApiInterface() {
        if (apiInterface == null) {
            apiInterface = getClient().create(ApiInterface.class);
        }
        return apiInterface;
    }

    public static User getUser() {
        Realm realm = Realm.getDefaultInstance();
        User user = realm.where(User.class).findFirst();
        return user;
    }

    public static boolean isPassenger() {
        User user = getUser();
        return user.getRole().equals("Passenger");
    }


}
