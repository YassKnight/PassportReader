package com.kernal.passportreader.sdk.base;

import android.app.Application;

/**
 * @author A@H
 */
public class IdCardApplication extends Application {
    @Override
    public void onCreate() {
        super.onCreate();
       // LeakCanary.install(this);
     //   CrashReport.initCrashReport(getApplicationContext(), "0265ffc578", true);

    }

}
