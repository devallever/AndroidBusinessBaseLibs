package com.google.zxing.client.android;

public class DLog {
    public static void d(String tag, String msg) {
        android.util.Log.d(tag, msg);
    }

    public static void d(String msg) {
        android.util.Log.d("DLog", msg);
    }
}
