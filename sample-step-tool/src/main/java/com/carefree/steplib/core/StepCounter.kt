package com.carefree.steplib.core

import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.os.SystemClock
import com.carefree.steplib.bean.StepBean
import com.carefree.steplib.common.StepConstants
import com.carefree.steplib.utils.DateUtils
import com.carefree.steplib.utils.Mkv
import com.carefree.steplib.utils.WakeLockUtils
import com.carefree.steplib.utils.logD

/**
 * 计步传感器实现类
 */
class StepCounterImpl(
    private val context: Context,
    private val stepListener: StepChangeListener?,
    private val isSeparate: Boolean,
    private val isBoot: Boolean
) : SensorEventListener, StepCounterInterface {
    private var offsetStep = 0
    private var stepValue = 0
    private lateinit var todayDate: String
    private var shouldCleanStep = true
    private var isShutdown = false
    private var isCounterReset = true

    init {
        WakeLockUtils.getLock(context)
        // 从MMKV加载已保存的计步数据
        loadStepData()
        // 检查是否开机启动或系统重启
        checkSystemStatus()
        // 检查日期变化，必要时清零步数
        checkDateChange()
    }

    private fun loadStepData() {
        stepValue = Mkv.getInt(StepConstants.KEY_CURRENT_STEP)
        shouldCleanStep = Mkv.getBool(StepConstants.KEY_CLEAN_STEP)
        todayDate = Mkv.getString(StepConstants.KEY_TODAY_DATE)
        offsetStep = Mkv.getInt(StepConstants.KEY_STEP_OFFSET)
        isShutdown = Mkv.getBool(StepConstants.KEY_SHUTDOWN)
    }

    private fun checkSystemStatus() {
        val isSystemShutdown = isSystemShutdownByRunningTime()
        if (isBoot || isSystemShutdown) {
            isShutdown = true
            Mkv.put(StepConstants.KEY_SHUTDOWN, true)
        }
    }

    private fun checkDateChange() {
        val currentDate = DateUtils.getCurrentDate()
        if (currentDate != todayDate || isSeparate) {
            resetStepData()
        }
    }

    private fun resetStepData() {
        WakeLockUtils.getLock(context)
        shouldCleanStep = true
        Mkv.put(StepConstants.KEY_CLEAN_STEP, true)
        todayDate = DateUtils.getCurrentDate()
        Mkv.put(StepConstants.KEY_TODAY_DATE, todayDate)
        isShutdown = false
        Mkv.put(StepConstants.KEY_SHUTDOWN, false)
        stepValue = 0
        Mkv.put(StepConstants.KEY_CURRENT_STEP, stepValue)
        stepListener?.onStepClean()
    }

    override fun onSensorChanged(event: SensorEvent) {
        if (event.sensor.type == Sensor.TYPE_STEP_COUNTER) {
            val counterStep = event.values[0].toInt()
            
            // 处理步数清零逻辑
            if (shouldCleanStep) {
                cleanStep(counterStep)
            } else {
                // 处理关机重启逻辑
                if (isShutdown || isSystemShutdownByCounterStep(counterStep)) {
                    handleShutdown(counterStep)
                }
            }
            
            // 计算当前步数
            stepValue = counterStep - offsetStep
            
            // 容错处理：步数不能为负数
            if (stepValue < 0) {
                cleanStep(counterStep)
            }
            
            // 保存当前步数数据
            saveStepData(counterStep)
            
            // 更新计步器状态
            updateStepStatus()
        }
    }

    private fun cleanStep(counterStep: Int) {
        stepValue = 0
        offsetStep = counterStep
        Mkv.put(StepConstants.KEY_STEP_OFFSET, offsetStep)
        shouldCleanStep = false
        Mkv.put(StepConstants.KEY_CLEAN_STEP, false)
    }

    private fun handleShutdown(counterStep: Int) {
        val savedStep = Mkv.getInt(StepConstants.KEY_CURRENT_STEP)
        offsetStep = counterStep - savedStep
        Mkv.put(StepConstants.KEY_STEP_OFFSET, offsetStep)
        isShutdown = false
        Mkv.put(StepConstants.KEY_SHUTDOWN, false)
    }

    private fun saveStepData(counterStep: Int) {
        Mkv.put(StepConstants.KEY_CURRENT_STEP, stepValue)
        Mkv.put(StepConstants.KEY_ELAPSED_REALTIME, SystemClock.elapsedRealtime())
        Mkv.put(StepConstants.KEY_LAST_SENSOR_TIME, counterStep)
    }

    private fun updateStepStatus() {
        checkDateChange()
        stepListener?.onStepChanged(stepValue)
    }

    private fun isSystemShutdownByCounterStep(counterStep: Int): Boolean {
        if (isCounterReset) {
            isCounterReset = false
            if (counterStep < Mkv.getInt(StepConstants.KEY_LAST_SENSOR_TIME)) {
                return true
            }
        }
        return false
    }

    private fun isSystemShutdownByRunningTime(): Boolean {
        val savedTime = Mkv.getLong(StepConstants.KEY_ELAPSED_REALTIME)
        return savedTime > SystemClock.elapsedRealtime()
    }

    // 实现StepCounterInterface接口的currentStep属性
    override val currentStep: Int
        get() {
            return Mkv.getInt(StepConstants.KEY_CURRENT_STEP)
        }
        
    // 兼容旧的getCurrentStep属性
    val getCurrentStep: Int
        get() = currentStep
        
    // 实现StepCounterInterface接口的方法
    override fun setStepChangeListener(listener: StepChangeListener?) {
        // 不做实际操作，因为监听器在构造函数中已设置
    }
    
    private fun resetCounter() {
        stepValue = 0
        Mkv.put(StepConstants.KEY_CURRENT_STEP, 0)
    }
    
    override fun resetSteps() {
        resetCounter()
    }
    
    override fun stop() {
        // 停止计步的逻辑
        isShutdown = true
    }
    
    override fun start(context: Context) {
        // 开始计步的逻辑
        // 这里不需要额外操作，因为传感器注册在外部完成
    }

    override fun onAccuracyChanged(sensor: Sensor, accuracy: Int) {
        // 精度变化时不需要特殊处理
    }
}