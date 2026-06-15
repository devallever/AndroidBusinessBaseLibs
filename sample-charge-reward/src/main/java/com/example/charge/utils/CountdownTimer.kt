package com.example.charge.utils

import android.os.CountDownTimer

/**
 * 倒计时工具类
 * 功能特点：
 * - 默认倒计时60秒
 * - 每秒回调更新
 * - 支持追加秒数
 */
class CountdownTimer {
    
    // 默认倒计时时间（毫秒）
    private val DEFAULT_COUNTDOWN_MILLIS = 60 * 1000L
    
    // 倒计时间隔（毫秒）- 50毫秒提供更流畅的进度效果
    private val COUNTDOWN_INTERVAL = 100L
    
    // 倒计时器
    private var countDownTimer: CountDownTimer? = null
    
    // 剩余时间（毫秒）
    private var remainingMillis: Long = DEFAULT_COUNTDOWN_MILLIS
    
    // 总时间（毫秒）
    private var totalMillis: Long = DEFAULT_COUNTDOWN_MILLIS
    
    // 是否正在倒计时
    private var isRunning: Boolean = false
    
    // 倒计时回调接口
    interface OnCountdownListener {
        /**
         * 倒计时进行中回调
         * @param remainingSeconds 剩余秒数
         * @param progress 进度值（0-100）
         */
        fun onTick(remainingSeconds: Int, progress: Int)
        
        /**
         * 倒计时结束回调
         */
        fun onFinish()
    }
    
    private var listener: OnCountdownListener? = null
    
    /**
     * 开始倒计时
     * @param totalSeconds 总秒数，默认为60秒
     * @param listener 倒计时回调监听器
     */
    fun start(totalSeconds: Int = 60, listener: OnCountdownListener?) {
        // 停止之前的倒计时
        stop()
        
        // 更新参数
        this.totalMillis = totalSeconds * 1000L
        this.remainingMillis = this.totalMillis
        this.listener = listener
        
        // 创建并启动新的倒计时器
        countDownTimer = object : CountDownTimer(remainingMillis, COUNTDOWN_INTERVAL) {
            override fun onTick(millisUntilFinished: Long) {
                remainingMillis = millisUntilFinished
                val remainingSeconds = (remainingMillis / 1000).toInt()
                // 计算进度百分比 (0-100) - 使用毫秒级计算提供更精确的进度
                val progress = if (totalMillis > 0) {
                    ((totalMillis - remainingMillis) * 100 / totalMillis).toInt()
                } else {
                    0
                }
                listener?.onTick(remainingSeconds, progress)
                // 减少日志输出频率，避免日志过多
                if (remainingMillis % 1000 == 0L || remainingMillis < 1000L) {
//                    log("CountdownTimer", "剩余时间: $remainingSeconds 秒，进度: $progress%")
                }
            }
            
            override fun onFinish() {
                isRunning = false
                remainingMillis = 0
                listener?.onFinish()
//                log("CountdownTimer", "倒计时结束")
            }
        }
        
        countDownTimer?.start()
        isRunning = true
//        log("CountdownTimer", "开始倒计时: $totalSeconds 秒，总进度: 100%")
    }
    
    /**
     * 追加倒计时秒数
     * @param secondsToAdd 要追加的秒数
     */
    fun addSeconds(secondsToAdd: Int) {
        if (secondsToAdd <= 0) return
        
        val currentRemainingSeconds = (remainingMillis / 1000).toInt()
        val newTotalSeconds = currentRemainingSeconds + secondsToAdd
        val newTotalMillis = newTotalSeconds * 1000L
        
        if (isRunning) {
            // 如果正在运行，重新启动倒计时
            start(newTotalSeconds, listener)
        } else {
            // 如果未运行，更新时间
            this.totalMillis = newTotalMillis
            this.remainingMillis = newTotalMillis
        }
        
//        log("CountdownTimer", "追加时间: $secondsToAdd 秒，新的总时间: $newTotalSeconds 秒")
    }
    
    /**
     * 停止倒计时
     */
    fun stop() {
        countDownTimer?.cancel()
        countDownTimer = null
        isRunning = false
//        log("CountdownTimer", "停止倒计时")
    }
    
    /**
     * 暂停倒计时
     */
    fun pause() {
        countDownTimer?.cancel()
        countDownTimer = null
        isRunning = false
//        log("CountdownTimer", "暂停倒计时")
    }
    
    /**
     * 恢复倒计时
     */
    fun resume() {
        if (!isRunning && remainingMillis > 0) {
            // 恢复时保持原始总时间
            countDownTimer = object : CountDownTimer(remainingMillis, COUNTDOWN_INTERVAL) {
                override fun onTick(millisUntilFinished: Long) {
                    remainingMillis = millisUntilFinished
                    val remainingSeconds = (remainingMillis / 1000).toInt()
                    // 计算进度百分比 (0-100) - 使用毫秒级计算提供更精确的进度
                    val progress = if (totalMillis > 0) {
                        ((totalMillis - remainingMillis) * 100 / totalMillis).toInt()
                    } else {
                        0
                    }
                    listener?.onTick(remainingSeconds, progress)
                    // 减少日志输出频率，避免日志过多
                    if (remainingMillis % 1000 == 0L || remainingMillis < 1000L) {
//                        log("CountdownTimer", "剩余时间: $remainingSeconds 秒，进度: $progress%")
                    }
                }
                
                override fun onFinish() {
                    isRunning = false
                    remainingMillis = 0
                    listener?.onFinish()
//                    log("CountdownTimer", "倒计时结束")
                }
            }
            
            countDownTimer?.start()
            isRunning = true
//            log("CountdownTimer", "恢复倒计时，剩余时间: ${(remainingMillis / 1000).toInt()} 秒")
        }
    }
    
    /**
     * 获取剩余秒数
     * @return 剩余秒数
     */
    fun getRemainingSeconds(): Int {
        return (remainingMillis / 1000).toInt()
    }
    
    /**
     * 判断是否正在倒计时
     * @return 是否正在倒计时
     */
    fun isCountingDown(): Boolean {
        return isRunning
    }
    
    /**
     * 释放资源
     */
    fun release() {
        stop()
        listener = null
//        log("CountdownTimer", "释放资源")
    }
}