package com.dgk.myaudiodemo;

import android.app.Application;
import android.content.Context;

/**
 * Created by Kevin on 2016/7/27.
 *
 */
public class MyApplication extends Application{

    private static Context ctx;

    @Override
    public void onCreate() {
        super.onCreate();
        ctx = getApplicationContext();
    }

    public static Context getContext() {
        return ctx;
    }
}
