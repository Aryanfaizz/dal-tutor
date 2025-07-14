package com.example.daltutor.core;

import android.app.Application;
import android.content.Context;

import org.checkerframework.checker.units.qual.C;

public class GlobalContext extends Application {
    public static Context context;

    @Override
    public void onCreate() {
        super.onCreate();
        context = getApplicationContext();
    }
}
