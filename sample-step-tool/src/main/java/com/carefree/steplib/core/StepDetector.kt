package com.carefree.steplib.core

import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import com.carefree.steplib.bean.StepBean
import com.carefree.steplib.common.StepConstants
import com.carefree.steplib.utils.DateUtils
import com.carefree.steplib.utils.Mkv
import com.carefree.steplib.utils.WakeLockUtils
import kotlin.math.sqrt

/**
 * 基于加速度传感器的计步算法实现
 */
class StepDetectorImpl(
    private val context: Context,
    private val stepListener: StepChangeListener?
) : SensorEventListener, StepDetectorInterface {
    // 三轴加速度数据
    private val accelerometerValues = FloatArray(3)
    private val valueCount = 4
    
    // 用于计算阈值的波峰波谷差值
    private val dynamicThresholdValues = FloatArray(valueCount)
    private var dynamicThresholdIndex = 0
    
    // 运动状态标志
    private var isDirectionUp = false
    private var continuousUpCount = 0
    private var previousUpCount = 0
    private var previousStatus = false
    
    // 波峰波谷值
    private var peakValue = 0f
    private var valleyValue = 0f
    
    // 时间记录
    private var peakTimeCurrent = 0L
    private var peakTimeLast = 0L
    private var currentTime = 0L
    
    // 传感器数据
    private var currentGravity = 0f
    private var previousGravity = 0f
    
    // 计步算法参数
    private val initialThreshold = 1.3f
    private var currentThreshold = 2.0f
    private val timeInterval = 250 // 毫秒
    
    // 计步计数器
    private var stepCountTemp = 0
    private var stepCountTotal = 0
    private var peakTimeLast1 = 0L
    private var peakTimeCurrent1 = 0L
    
    private lateinit var todayDate: String

    init {
        WakeLockUtils.getLock(context)
        // 从MMKV加载已保存的计步数据
        loadStepData()
        // 检查日期变化，必要时清零步数
        checkDateChange()
    }

    private fun loadStepData() {
        stepCountTotal = Mkv.getInt(StepConstants.KEY_CURRENT_STEP)
        todayDate = Mkv.getString(StepConstants.KEY_TODAY_DATE)
    }

    private fun checkDateChange() {
        val currentDate = DateUtils.getCurrentDate()
        if (currentDate != todayDate) {
            resetStepData()
        }
    }

    private fun resetStepData() {
        WakeLockUtils.getLock(context)
        stepCountTotal = 0
        Mkv.put(StepConstants.KEY_CURRENT_STEP, stepCountTotal)
        todayDate = DateUtils.getCurrentDate()
        Mkv.put(StepConstants.KEY_TODAY_DATE, todayDate)
        resetStepCounter()
        stepListener?.onStepClean()
    }

    private fun resetStepCounter() {
        stepCountTemp = 0
        peakTimeLast1 = 0
        peakTimeCurrent1 = 0
    }

    override fun onSensorChanged(event: SensorEvent) {
        if (event.sensor.type == Sensor.TYPE_ACCELEROMETER) {
            // 保存三轴加速度数据
            for (i in 0..2) {
                accelerometerValues[i] = event.values[i]
            }
            
            // 计算合成加速度
            currentGravity = calculateCombinedGravity(accelerometerValues)
            
            // 检测步数
            detectStep(currentGravity)
        }
    }

    private fun calculateCombinedGravity(values: FloatArray): Float {
        return sqrt(
            (values[0] * values[0] + values[1] * values[1] + values[2] * values[2]).toDouble()
        ).toFloat()
    }

    private fun detectStep(gravityValue: Float) {
        if (previousGravity == 0f) {
            previousGravity = gravityValue
        } else {
            // 检测波峰
            if (isPeak(gravityValue, previousGravity)) {
                peakTimeLast = peakTimeCurrent
                currentTime = System.currentTimeMillis()
                
                // 符合时间差和阈值条件，计为一步
                if (currentTime - peakTimeLast >= timeInterval && 
                    (peakValue - valleyValue >= currentThreshold)) {
                    peakTimeCurrent = currentTime
                    countStep()
                }
                
                // 动态更新阈值
                if (currentTime - peakTimeLast >= timeInterval && 
                    (peakValue - valleyValue >= initialThreshold)) {
                    peakTimeCurrent = currentTime
                    currentThreshold = calculateDynamicThreshold(peakValue - valleyValue)
                }
            }
        }
        previousGravity = gravityValue
    }

    private fun isPeak(newValue: Float, oldValue: Float): Boolean {
        previousStatus = isDirectionUp
        
        if (newValue >= oldValue) {
            isDirectionUp = true
            continuousUpCount++
        } else {
            previousUpCount = continuousUpCount
            continuousUpCount = 0
            isDirectionUp = false
        }
        
        // 判断是否为波峰
        if (!isDirectionUp && previousStatus && 
            (previousUpCount >= 2 || oldValue >= 20)) {
            peakValue = oldValue
            return true
        } else if (!previousStatus && isDirectionUp) {
            valleyValue = oldValue
            return false
        } else {
            return false
        }
    }

    private fun calculateDynamicThreshold(value: Float): Float {
        if (dynamicThresholdIndex < valueCount) {
            dynamicThresholdValues[dynamicThresholdIndex] = value
            dynamicThresholdIndex++
            return currentThreshold
        } else {
            // 计算均值并梯度化阈值
            val average = calculateAverage(dynamicThresholdValues)
            // 更新阈值数组
            updateThresholdArray(value)
            return average
        }
    }

    private fun calculateAverage(values: FloatArray): Float {
        var sum = 0f
        for (value in values) {
            sum += value
        }
        val average = sum / valueCount
        
        // 根据均值梯度化阈值
        return when {
            average >= 8 -> 4.3f
            average >= 7 -> 3.3f
            average >= 4 -> 2.3f
            average >= 3 -> 2.0f
            else -> 1.3f
        }
    }

    private fun updateThresholdArray(newValue: Float) {
        // 移除第一个元素，添加新元素到数组末尾
        for (i in 1 until valueCount) {
            dynamicThresholdValues[i - 1] = dynamicThresholdValues[i]
        }
        dynamicThresholdValues[valueCount - 1] = newValue
    }

    private fun countStep() {
        peakTimeLast1 = peakTimeCurrent1
        peakTimeCurrent1 = System.currentTimeMillis()
        
        // 连续走10步才开始计步，少于9步且停留超过3秒则重置计数
        if (peakTimeCurrent1 - peakTimeLast1 <= 3000L) {
            if (stepCountTemp < 9) {
                stepCountTemp++
            } else if (stepCountTemp == 9) {
                stepCountTemp++
                stepCountTotal += stepCountTemp
                saveStepCount()
            } else {
                stepCountTotal++
                saveStepCount()
            }
        } else {
            // 超时，重置临时计数
            stepCountTemp = 1
        }
    }

    private fun saveStepCount() {
        Mkv.put(StepConstants.KEY_CURRENT_STEP, stepCountTotal)
        notifyStepChange()
    }

    private fun notifyStepChange() {
        checkDateChange()
        stepListener?.onStepChanged(stepCountTotal)
    }

    fun setSteps(stepValue: Int) {
        stepCountTotal = stepValue
        resetStepCounter()
        Mkv.put(StepConstants.KEY_CURRENT_STEP, stepCountTotal)
        todayDate = DateUtils.getCurrentDate()
        Mkv.put(StepConstants.KEY_TODAY_DATE, todayDate)
        stepListener?.onStepChanged(stepCountTotal)
    }

    // 实现StepDetectorInterface接口的currentStep属性
    override val currentStep: Int
        get() = stepCountTotal
        
    // 兼容旧的getCurrentStep属性
    val getCurrentStep: Int
        get() = currentStep
        
    // 实现StepDetectorInterface接口的方法
    override fun setStepChangeListener(listener: StepChangeListener?) {
        // 不做实际操作，因为监听器在构造函数中已设置
    }
    
    override fun resetSteps() {
        resetStepData()
    }
    
    override fun stop() {
        // 停止计步的逻辑
        stepCountTemp = 0
    }
    
    override fun start(context: Context) {
        // 开始计步的逻辑
        // 这里不需要额外操作，因为传感器注册在外部完成
    }
    
    override fun setSensitivity(sensitivity: Int) {
        // 根据灵敏度调整阈值
        val clampedSensitivity = sensitivity.coerceIn(1, 10)
        // 将灵敏度转换为合适的阈值范围
        currentThreshold = when (clampedSensitivity) {
            1 -> 3.0f
            2 -> 2.8f
            3 -> 2.6f
            4 -> 2.4f
            5 -> 2.0f // 默认值
            6 -> 1.8f
            7 -> 1.6f
            8 -> 1.4f
            9 -> 1.2f
            10 -> 1.0f
            else -> 2.0f
        }
    }

    override fun onAccuracyChanged(sensor: Sensor, accuracy: Int) {
        // 精度变化时不需要特殊处理
    }
}