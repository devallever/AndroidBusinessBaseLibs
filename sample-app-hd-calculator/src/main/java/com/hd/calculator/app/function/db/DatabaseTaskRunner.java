package com.hd.calculator.app.function.db;

import android.os.Handler;
import android.os.Looper;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class DatabaseTaskRunner {

    private static final ExecutorService executor = Executors.newSingleThreadExecutor();

    private static final Handler handler = new Handler(Looper.getMainLooper());

    public static <T> void executeTask(DatabaseTask<T> task, DatabaseCallback<T> callback) {

        executor.execute(() -> {
            T result = task.execute();
            handler.post(() -> callback.onComplete(result));
        });
    }

    public interface DatabaseTask<T> {
        T execute(); // 在后台线程执行
    }

    public interface DatabaseCallback<T> {
        void onComplete(T result); // 在主线程回调
    }
}