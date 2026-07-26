package com.hd.calculator.app.util;

import android.os.Handler;
import android.os.Looper;

public class IntervalTimer {
    private final Handler handler = new Handler(Looper.getMainLooper());
    private Runnable taskRunnable;
    private long intervalMillis; // 间隔时间（毫秒）
    private boolean isRunning = false;

    public void start(long intervalSeconds, final Runnable task) {
        if (isRunning) return; // 避免重复启动

        intervalMillis = intervalSeconds * 1000;
        isRunning = true;

        taskRunnable = new Runnable() {
            @Override
            public void run() {
                if (!isRunning) return; // 确保取消后不再执行
                task.run(); // 执行用户任务
                handler.postDelayed(this, intervalMillis); // 安排下次执行
            }
        };

        handler.postDelayed(taskRunnable, intervalMillis);
    }

    public void stop() {
        isRunning = false;
        if (taskRunnable != null) {
            handler.removeCallbacks(taskRunnable); // 移除未执行的任务
        }
    }

    public boolean isRunning() {
        return isRunning;
    }
}