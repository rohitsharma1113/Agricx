package com.agricx.app.agricximagecapture;

import android.app.Application;

import com.crashlytics.android.Crashlytics;

import io.fabric.sdk.android.Fabric;

/**
 * Created by rohit on 20/4/17.
 */

public class AgricxImageCaptureApplication extends Application {
    @Override
    public void onCreate() {
        super.onCreate();
        if (!BuildConfig.DEBUG){
            Fabric.with(this, new Crashlytics());
        }
    }
}
