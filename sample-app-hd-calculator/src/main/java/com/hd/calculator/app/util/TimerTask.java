package com.hd.calculator.app.util;

import android.os.Handler;
import android.os.Looper;

import androidx.annotation.NonNull;
import androidx.lifecycle.Lifecycle;
import androidx.lifecycle.LifecycleObserver;
import androidx.lifecycle.LifecycleOwner;
import androidx.lifecycle.DefaultLifecycleObserver;

import java.lang.ref.WeakReference;

/**
 * 定时器任务类
 * 可以设置每隔n秒执行任务，支持取消执行以防止内存泄漏
 * 通过回调方式通知外部执行结果
 * 支持Lifecycle，可自动结束执行
 */
public class TimerTask implements LifecycleObserver {

    private Handler handler;
    private Runnable runnable;
    private boolean isRunning = false;
    private long intervalMillis;
    private Lifecycle lifecycleRef;

    /**
     * 定时器回调接口
     */
    public interface TimerCallback {
        /**
         * 定时任务执行时回调
         */
        void onTick();
    }

    /**
     * 构造函数
     *
     * @param intervalMillis 间隔毫秒数
     * @param callback       定时器回调
     */
    public TimerTask(long intervalMillis, @NonNull TimerCallback callback) {
        init(intervalMillis, callback, null);
    }

    /**
     * 构造函数
     *
     * @param intervalMillis 间隔毫秒数
     * @param callback       定时器回调
     * @param lifecycle      生命周期对象
     */
    public TimerTask(long intervalMillis, Lifecycle lifecycle,  @NonNull TimerCallback callback) {
        init(intervalMillis, callback, lifecycle);
    }

    public TimerTask(long intervalMillis, @NonNull TimerCallback callback, LifecycleOwner lifecycleOwner) {
        init(intervalMillis, callback, lifecycleOwner.getLifecycle());
    }

    /**
     * 初始化方法
     *
     * @param intervalMillis 间隔毫秒数
     * @param callback       定时器回调
     * @param lifecycle      生命周期对象
     */
    private void init(long intervalMillis, @NonNull TimerCallback callback, Lifecycle lifecycle) {
        this.intervalMillis = intervalMillis;
        // 使用WeakReference防止内存泄漏
        final WeakReference<TimerCallback> callbackRef = new WeakReference<>(callback);
        handler = new Handler(Looper.getMainLooper());
        runnable = new Runnable() {
            @Override
            public void run() {
                TimerCallback callback = callbackRef.get();
                if (callback != null) {
                    callback.onTick();
                    if (isRunning) {
                        handler.postDelayed(this, intervalMillis);
                    }
                } else {
                    // 如果回调已被回收，则停止定时器
                    stop();
                }
            }
        };



        // 设置Lifecycle
        if (lifecycle != null) {
            this.lifecycleRef = lifecycle;
            lifecycle.addObserver(new DefaultLifecycleObserver() {
                @Override
                public void onDestroy(@NonNull LifecycleOwner owner) {
                    stop();
                }
            });
        }
    }

    /**
     * 开始定时执行
     */
    public void start(Lifecycle lifecycle) {
        if (!isRunning) {
            isRunning = true;
            handler.post(runnable);
        }

        // 设置Lifecycle
        if (lifecycle != null) {
            this.lifecycleRef = lifecycle;
            lifecycle.addObserver(new DefaultLifecycleObserver() {
                @Override
                public void onDestroy(@NonNull LifecycleOwner owner) {
                    stop();
                }
            });
        }
    }

    /**
     * 停止定时执行
     */
    public void stop() {
        isRunning = false;
        handler.removeCallbacks(runnable);
        
        // 移除Lifecycle观察者
        Lifecycle lifecycle = lifecycleRef != null ? lifecycleRef: null;
        if (lifecycle != null && lifecycle.getCurrentState().isAtLeast(Lifecycle.State.STARTED)) {
            lifecycle.removeObserver(this);
        }
    }

    /**
     * 重新开始定时执行（先停止再开始）
     */
    public void restart(Lifecycle lifecycle) {
        stop();
        start(lifecycle);
    }

    /**
     * 检查定时器是否正在运行
     *
     * @return 是否正在运行
     */
    public boolean isRunning() {
        return isRunning;
    }

    /**
     * 设置新的间隔时间
     *
     * @param intervalSeconds 新的间隔秒数
     */
    public void setInterval(long intervalSeconds) {
        setIntervalMillis(intervalSeconds * 1000);
    }

    /**
     * 设置新的间隔时间（毫秒）
     *
     * @param intervalMillis 新的间隔毫秒数
     */
    public void setIntervalMillis(long intervalMillis) {
        boolean wasRunning = isRunning;
        if (wasRunning) {
            stop();
        }
        this.intervalMillis = intervalMillis;
        if (wasRunning) {
            start(null);
        }
    }

    /**
     * 获取当前间隔时间（毫秒）
     *
     * @return 间隔时间（毫秒）
     */
    public long getIntervalMillis() {
        return intervalMillis;
    }

}