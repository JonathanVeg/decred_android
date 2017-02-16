package altcoin.br.decred.application;

import android.app.Application;
import android.content.Context;

import com.android.volley.RequestQueue;
import com.android.volley.toolbox.Volley;
import com.crashlytics.android.Crashlytics;

import io.fabric.sdk.android.Fabric;

public class MyApplication extends Application {
    private static MyApplication mInstance;
    private static Context mAppContext;
    private RequestQueue requestQueue;

    public static MyApplication getInstance() {
        return mInstance;
    }

    @SuppressWarnings("unused")
    public static Context getAppContext() {
        return mAppContext;
    }

    private void setAppContext(Context mAppContext) {
        MyApplication.mAppContext = mAppContext;
    }

    public RequestQueue getRequestQueue() {
        if (requestQueue == null)
            requestQueue = Volley.newRequestQueue(getApplicationContext());

        return requestQueue;
    }

    @Override
    public void onCreate() {
        super.onCreate();

        Fabric.with(this, new Crashlytics());

        mInstance = this;

        this.setAppContext(getApplicationContext());
    }
}