package com.carefree.steplib.core

import android.content.Context
import android.hardware.Sensor
import com.carefree.steplib.common.StepConstants
import com.carefree.steplib.utils.SensorUtils

/**
 * 计步管理器
 * 负责协调不同类型的计步器实现，根据设备支持情况选择最优计步方式
 */
object StepManager {
    private var stepCounter: StepCounterInterface? = null
    private var stepDetector: StepDetectorInterface? = null
    private var currentStepProvider: StepCounterInterface? = null
    private var stepChangeListener: StepChangeListener? = null
    private var context: Context? = null
    private var isRunning = false

    /**
     * 初始化计步管理器
     * @param ctx 上下文
     * @param listener 步数变化监听器
     */
    fun initialize(ctx: Context, listener: StepChangeListener? = null) {
        context = ctx.applicationContext
        stepChangeListener = listener
        
        // 优先使用硬件计步器
        if (SensorUtils.isStepCounterSensorSupported(context!!)) {
            stepCounter = StepCounterImpl(context!!, stepChangeListener, false, false)
            currentStepProvider = stepCounter
        } 
        // 其次使用加速度传感器
        else if (SensorUtils.isAccelerometerSensorSupported(context!!)) {
            stepDetector = StepDetectorImpl(context!!, stepChangeListener) as StepDetectorInterface
            currentStepProvider = stepDetector
        }
    }

    /**
     * 开始计步
     * @return 是否成功开始计步
     */
    fun startStepCounting(): Boolean {
        if (context == null || currentStepProvider == null) {
            return false
        }
        
        try {
            currentStepProvider?.start(context!!)
            isRunning = true
            return true
        } catch (e: Exception) {
            e.printStackTrace()
            return false
        }
    }

    /**
     * 停止计步
     */
    fun stopStepCounting() {
        try {
            currentStepProvider?.stop()
            isRunning = false
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    /**
     * 获取当前步数
     * @return 当前步数
     */
    val currentStep: Int
        get() = currentStepProvider?.currentStep ?: 0

    /**
     * 重置步数
     */
    fun resetSteps() {
        currentStepProvider?.resetSteps()
    }

    /**
     * 设置步数变化监听器
     * @param listener 监听器
     */
    fun setStepChangeListener(listener: StepChangeListener?) {
        stepChangeListener = listener
        stepCounter?.setStepChangeListener(listener)
        stepDetector?.setStepChangeListener(listener)
    }

    /**
     * 设置计步灵敏度（仅对基于加速度传感器的计步器有效）
     * @param sensitivity 灵敏度（1-10）
     */
    fun setSensitivity(sensitivity: Int) {
        val clampedSensitivity = sensitivity.coerceIn(StepConstants.MIN_SENSITIVITY, StepConstants.MAX_SENSITIVITY)
        if (stepDetector != null) {
            stepDetector?.setSensitivity(clampedSensitivity)
        }
    }

    /**
     * 获取当前使用的传感器类型
     * @return 传感器类型，0表示未初始化，1表示计步传感器，2表示加速度传感器
     */
    val currentSensorType: Int
        get() {
            return when {
                stepCounter != null && currentStepProvider === stepCounter -> Sensor.TYPE_STEP_COUNTER
                stepDetector != null && currentStepProvider === stepDetector -> Sensor.TYPE_ACCELEROMETER
                else -> 0
            }
        }

    /**
     * 判断计步器是否正在运行
     * @return 是否正在运行
     */
    val isCounting: Boolean
        get() = isRunning

    /**
     * 切换计步方式（调试用）
     * @param sensorType 传感器类型
     * @return 是否切换成功
     */
    fun switchStepProvider(sensorType: Int): Boolean {
        if (context == null) {
            return false
        }

        val wasRunning = isRunning
        stopStepCounting()

        try {
            when (sensorType) {
                Sensor.TYPE_STEP_COUNTER -> {
                    if (SensorUtils.isStepCounterSensorSupported(context!!)) {
                        if (stepCounter == null) {
                            stepCounter = StepCounterImpl(context!!, stepChangeListener, false, false)
                        }
                        currentStepProvider = stepCounter
                        if (wasRunning) {
                            startStepCounting()
                        }
                        return true
                    }
                }
                Sensor.TYPE_ACCELEROMETER -> {
                    if (SensorUtils.isAccelerometerSensorSupported(context!!)) {
                        if (stepDetector == null) {
                            stepDetector = StepDetectorImpl(context!!, stepChangeListener) as StepDetectorInterface
                        }
                        currentStepProvider = stepDetector
                        if (wasRunning) {
                            startStepCounting()
                        }
                        return true
                    }
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }

        return false
    }

    /**
     * 释放资源
     */
    fun release() {
        stopStepCounting()
        stepCounter = null
        stepDetector = null
        currentStepProvider = null
        stepChangeListener = null
        context = null
        isRunning = false
    }
}