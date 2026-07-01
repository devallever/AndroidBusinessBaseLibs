package com.example.charge.utils

import android.os.Handler
import android.os.Looper
import java.util.concurrent.atomic.AtomicBoolean

/**
 * 高级定时器工具类
 * 功能特点：
 * 1. 支持设置间隔，默认1秒
 * 2. 每秒回调
 * 3. 当设置了间隔且不等于1秒时，每间隔回调的同时也要每秒回调
 */
class AdvancedTimer {
    // 默认时间间隔（毫秒）
    private val DEFAULT_INTERVAL = 1000L
    
    // 当前设置的时间间隔
    private var interval: Long = DEFAULT_INTERVAL
    
    // 定时器相关
    private val handler = Handler(Looper.getMainLooper())
    private val isRunning = AtomicBoolean(false)
    private var totalElapsedTime: Long = 0L
    private var lastSecondCallbackTime: Long = 0L
    private var lastIntervalCallbackTime: Long = 0L
    private var startTime: Long = 0L
    
    // 回调接口
    interface OnTimerTickListener {
        /**
         * 每秒回调
         * @param seconds 累计秒数
         */
        fun onSecondTick(seconds: Long)
        
        /**
         * 间隔回调
         * @param milliseconds 累计毫秒数
         * @param progress 距离下次间隔回调的进度 (0-100)
         */
        fun onIntervalTick(milliseconds: Long, progress: Int)
    }
    
    private var listener: OnTimerTickListener? = null
    
    // 每秒定时器的Runnable
    private val secondRunnable = object : Runnable {
        override fun run() {
            if (!isRunning.get()) {
                return
            }
            
            // 计算当前累计时间
            updateElapsedTime()
            val currentTimeSeconds = totalElapsedTime / 1000
            
            // 每秒回调
            listener?.onSecondTick(currentTimeSeconds)
            lastSecondCallbackTime = currentTimeSeconds * 1000
            
            // 继续每秒计时
            handler.postDelayed(this, 1000L)
        }
    }
    
    // 间隔定时器的Runnable
    private val intervalRunnable = object : Runnable {
        override fun run() {
            if (!isRunning.get()) {
                return
            }
            
            // 更新总时间
            updateElapsedTime()
            
            // 只有当间隔不等于1000毫秒时，才执行间隔回调
            if (interval != 1000L) {
                // 计算进度
                val progress = 100
                listener?.onIntervalTick(totalElapsedTime, progress)
                lastIntervalCallbackTime = totalElapsedTime
            }
            
            // 继续下一次间隔计时
            handler.postDelayed(this, interval)
        }
    }
    
    // 更新累计时间
    private fun updateElapsedTime() {
        totalElapsedTime = System.currentTimeMillis() - startTime
    }
    
    // 检查并执行每秒回调（备用方法）
    private fun checkAndPerformSecondTick() {
        val currentTimeSeconds = totalElapsedTime / 1000
        val lastTimeSeconds = lastSecondCallbackTime / 1000
        
        if (currentTimeSeconds > lastTimeSeconds) {
            // 如果当前秒数大于上次回调的秒数，执行每秒回调
            listener?.onSecondTick(currentTimeSeconds)
            lastSecondCallbackTime = currentTimeSeconds * 1000
        }
    }
    
    /**
     * 设置监听器
     */
    fun setListener(listener: OnTimerTickListener?): AdvancedTimer {
        this.listener = listener
        return this
    }
    
    /**
     * 设置时间间隔（毫秒）
     */
    fun setInterval(intervalMs: Long): AdvancedTimer {
        if (intervalMs > 0) {
            this.interval = intervalMs
        }
        return this
    }
    
    /**
     * 开始定时器
     */
    fun start(): AdvancedTimer {
        if (!isRunning.get()) {
            isRunning.set(true)
            totalElapsedTime = 0L
            lastSecondCallbackTime = 0L
            lastIntervalCallbackTime = 0L
            startTime = System.currentTimeMillis()
            
            // 立即执行初始回调
            listener?.onSecondTick(0)
            
            // 只有当间隔不等于1000毫秒时，才执行初始间隔回调
            if (interval != 1000L) {
                listener?.onIntervalTick(0, 0)
            }
            
            // 启动每秒定时器（确保每秒回调）
            handler.postDelayed(secondRunnable, 1000L)
            
            // 如果间隔不等于1000毫秒，启动间隔定时器
            if (interval != 1000L) {
                handler.postDelayed(intervalRunnable, interval)
            }
        }
        return this
    }
    
    /**
     * 停止定时器
     */
    fun stop(): AdvancedTimer {
        isRunning.set(false)
        handler.removeCallbacks(intervalRunnable)
        handler.removeCallbacks(secondRunnable)
        return this
    }
    
    /**
     * 重置定时器
     */
    fun reset(): AdvancedTimer {
        stop()
        totalElapsedTime = 0L
        lastSecondCallbackTime = 0L
        lastIntervalCallbackTime = 0L
        startTime = 0L
        return this
    }
    
    /**
     * 获取当前累计时间（毫秒）
     */
    fun getElapsedTime(): Long {
        return totalElapsedTime
    }
    
    /**
     * 判断定时器是否正在运行
     */
    fun isRunning(): Boolean {
        return isRunning.get()
    }
    
    /**
     * 释放资源
     */
    fun release() {
        stop()
        listener = null
    }
}