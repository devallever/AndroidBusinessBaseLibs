package com.hd.calculator.app.util;

import android.util.Log;

import com.hd.calculator.app.BuildConfig;

public class LogUtils {
    public static void log(String msg) {
        if (BuildConfig.DEBUG) {
            Log.d("MyApp", msg);
        }
    }
}
