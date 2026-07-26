package com.hd.calculator.app.util;

import android.widget.Toast;

import com.hd.calculator.app.MyApp;

public class ToastUtils {
    public static void show(String msg) {
        ThreadUtils.runOnUiThread(() -> {
            Toast.makeText(MyApp.context, msg, Toast.LENGTH_SHORT).show();
        });
    }

    public static void showLong(String msg) {
        ThreadUtils.runOnUiThread(() -> {
            Toast.makeText(MyApp.context, msg, Toast.LENGTH_LONG).show();
        });
    }
}
