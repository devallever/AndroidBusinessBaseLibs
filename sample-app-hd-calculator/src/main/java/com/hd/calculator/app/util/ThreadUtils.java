package com.hd.calculator.app.util;

import android.os.Handler;
import android.os.Looper;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class ThreadUtils {

    //CacheThreadPool
    private static final ExecutorService sCacheThreadPool = Executors.newCachedThreadPool();

    private static final ExecutorService sSingleThreadPool = Executors.newSingleThreadExecutor();

    public static void runOnUiThread(Runnable runnable) {
        if (Thread.currentThread().getId() == Looper.getMainLooper().getThread().getId()) {
            runnable.run();
        } else {
            new Handler(Looper.getMainLooper()).post(runnable);
        }
    }

    public static void runOnIoThreadDelayed(Runnable runnable) {
        sCacheThreadPool.execute(runnable);
    }

    public static void runSingleThread(Runnable runnable) {
        sSingleThreadPool.execute(runnable);
    }
}
