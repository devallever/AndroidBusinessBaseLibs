package com.plinkopro.wincash.utils

import android.os.Handler
import android.os.Looper
import app.allever.android.lib.core.app.App
import com.plinkopro.wincash.BuildConfig
import java.util.concurrent.atomic.AtomicBoolean

/**
 * 定时器工具类
 * 支持可配置的间隔时间（默认1秒），提供开始、暂停、恢复、停止等功能
 */
class TimerUtil {
    // 默认间隔时间（毫秒）
    companion object {
        const val DEFAULT_INTERVAL_MS = 60 * 1000L // 1分钟 60 * 1000
    }
    
    private val handler = Handler(Looper.getMainLooper())
    private val isRunning = AtomicBoolean(false)
    private val isPaused = AtomicBoolean(false)
    private var intervalMs: Long = DEFAULT_INTERVAL_MS
    private var totalElapsedTime: Long = 0L // 累计运行时间
    private var lastPauseTime: Long = 0L // 上次暂停的时间
    private var tickCallback: ((elapsedTime: Long) -> Unit)? = null
    private var finishCallback: (() -> Unit)? = null
    private var maxRunningTime: Long? = null // 最大运行时间（毫秒）
    
    private val timerRunnable = object : Runnable {
        override fun run() {
            if (!isRunning.get() || isPaused.get()) {
                return
            }
            
            val currentTime = System.currentTimeMillis()
            totalElapsedTime = currentTime - startTime
            
            // 检查是否达到最大运行时间
            if (maxRunningTime != null && totalElapsedTime >= maxRunningTime!!) {
                stop()
                finishCallback?.invoke()
                return
            }
            
            // 执行回调
            tickCallback?.invoke(totalElapsedTime)
            if (App.DEBUG) {
                LogUtil.local("TimerUtil: $totalElapsedTime")
            }
            
            // 继续下一次计时
            handler.postDelayed(this, intervalMs)
        }
    }
    
    private var startTime: Long = 0L
    
    /**
     * 设置定时器间隔时间
     * @param intervalMs 间隔时间（毫秒），默认为1000毫秒
     */
    fun setInterval(intervalMs: Long): TimerUtil {
        if (intervalMs <= 0) {
            throw IllegalArgumentException("Interval must be greater than 0")
        }
        this.intervalMs = intervalMs
        return this
    }
    
    /**
     * 设置最大运行时间
     * @param maxTimeMs 最大运行时间（毫秒），null表示无限运行
     */
    fun setMaxRunningTime(maxTimeMs: Long?): TimerUtil {
        if (maxTimeMs != null && maxTimeMs <= 0) {
            throw IllegalArgumentException("Max time must be greater than 0")
        }
        this.maxRunningTime = maxTimeMs
        return this
    }
    
    /**
     * 设置计时回调函数
     * @param callback 每次计时触发的回调函数，参数为累计运行时间（毫秒）
     */
    fun setTickCallback(callback: (elapsedTime: Long) -> Unit): TimerUtil {
        this.tickCallback = callback
        return this
    }
    
    /**
     * 设置完成回调函数
     * @param callback 定时器完成时触发的回调函数
     */
    fun setFinishCallback(callback: () -> Unit): TimerUtil {
        this.finishCallback = callback
        return this
    }
    
    /**
     * 开始定时器
     */
    fun start(): TimerUtil {
        if (isRunning.get()) {
            return this
        }
        
        isRunning.set(true)
        isPaused.set(false)
        startTime = System.currentTimeMillis() - totalElapsedTime
        handler.post(timerRunnable)
        return this
    }
    
    /**
     * 暂停定时器
     */
    fun pause(): TimerUtil {
        if (!isRunning.get() || isPaused.get()) {
            return this
        }
        
        isPaused.set(true)
        lastPauseTime = System.currentTimeMillis()
        handler.removeCallbacks(timerRunnable)
        return this
    }
    
    /**
     * 恢复定时器
     */
    fun resume(): TimerUtil {
        if (!isRunning.get() || !isPaused.get()) {
            return this
        }
        
        val pauseDuration = System.currentTimeMillis() - lastPauseTime
        startTime += pauseDuration // 调整开始时间，补偿暂停的时间
        isPaused.set(false)
        handler.post(timerRunnable)
        return this
    }
    
    /**
     * 停止定时器
     */
    fun stop(): TimerUtil {
        isRunning.set(false)
        isPaused.set(false)
        handler.removeCallbacks(timerRunnable)
        return this
    }
    
    /**
     * 重置定时器
     */
    fun reset(): TimerUtil {
        stop()
        totalElapsedTime = 0L
        lastPauseTime = 0L
        return this
    }
    
    /**
     * 获取当前是否正在运行
     */
    fun isRunning(): Boolean {
        return isRunning.get() && !isPaused.get()
    }
    
    /**
     * 获取当前是否已暂停
     */
    fun isPaused(): Boolean {
        return isPaused.get()
    }
    
    /**
     * 获取累计运行时间（毫秒）
     */
    fun getElapsedTime(): Long {
        return totalElapsedTime
    }
    
    /**
     * 取消所有回调并释放资源
     */
    fun release() {
        stop()
        tickCallback = null
        finishCallback = null
    }
}