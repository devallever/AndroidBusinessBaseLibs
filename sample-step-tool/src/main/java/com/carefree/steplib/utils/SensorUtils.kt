package com.carefree.steplib.utils

import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorManager

/**
 * 传感器工具类
 */
object SensorUtils {
    /**
     * 检查设备是否支持计步传感器
     * @param context 上下文
     * @return true 支持，false 不支持
     */
    fun isStepCounterSensorSupported(context: Context): Boolean {
        val sensorManager = context.getSystemService(Context.SENSOR_SERVICE) as? SensorManager
        val stepCounterSensor = sensorManager?.getDefaultSensor(Sensor.TYPE_STEP_COUNTER)
        return stepCounterSensor != null
    }

    /**
     * 检查设备是否支持加速度传感器
     * @param context 上下文
     * @return true 支持，false 不支持
     */
    fun isAccelerometerSensorSupported(context: Context): Boolean {
        val sensorManager = context.getSystemService(Context.SENSOR_SERVICE) as? SensorManager
        val accelerometerSensor = sensorManager?.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)
        return accelerometerSensor != null
    }

    /**
     * 检查设备是否支持陀螺仪传感器
     * @param context 上下文
     * @return true 支持，false 不支持
     */
    fun isGyroscopeSensorSupported(context: Context): Boolean {
        val sensorManager = context.getSystemService(Context.SENSOR_SERVICE) as? SensorManager
        val gyroscopeSensor = sensorManager?.getDefaultSensor(Sensor.TYPE_GYROSCOPE)
        return gyroscopeSensor != null
    }

    /**
     * 获取计步传感器
     * @param context 上下文
     * @return 计步传感器实例，如果不支持则返回null
     */
    fun getStepCounterSensor(context: Context): Sensor? {
        val sensorManager = context.getSystemService(Context.SENSOR_SERVICE) as? SensorManager
        return sensorManager?.getDefaultSensor(Sensor.TYPE_STEP_COUNTER)
    }

    /**
     * 获取加速度传感器
     * @param context 上下文
     * @return 加速度传感器实例，如果不支持则返回null
     */
    fun getAccelerometerSensor(context: Context): Sensor? {
        val sensorManager = context.getSystemService(Context.SENSOR_SERVICE) as? SensorManager
        return sensorManager?.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)
    }

    /**
     * 获取设备支持的传感器列表
     * @param context 上下文
     * @return 传感器列表
     */
    fun getAllSupportedSensors(context: Context): List<Sensor> {
        val sensorManager = context.getSystemService(Context.SENSOR_SERVICE) as? SensorManager
        return sensorManager?.getSensorList(Sensor.TYPE_ALL) ?: emptyList()
    }

    /**
     * 计算三轴加速度的合成值
     * @param x X轴加速度
     * @param y Y轴加速度
     * @param z Z轴加速度
     * @return 合成加速度值
     */
    fun calculateAcceleration(x: Float, y: Float, z: Float): Double {
        return Math.sqrt((x * x + y * y + z * z).toDouble())
    }

    /**
     * 计算两帧加速度数据的差值
     * @param prevValue 上一帧加速度值
     * @param currentValue 当前帧加速度值
     * @return 差值绝对值
     */
    fun calculateAccelerationDifference(prevValue: Double, currentValue: Double): Double {
        return Math.abs(currentValue - prevValue)
    }

    /**
     * 判断传感器类型是否为计步相关传感器
     * @param sensorType 传感器类型
     * @return true 是计步相关传感器，false 否
     */
    fun isStepRelatedSensor(sensorType: Int): Boolean {
        return sensorType == Sensor.TYPE_STEP_COUNTER || 
               sensorType == Sensor.TYPE_STEP_DETECTOR || 
               sensorType == Sensor.TYPE_ACCELEROMETER
    }

    /**
     * 获取传感器类型的可读名称
     * @param sensorType 传感器类型
     * @return 传感器名称
     */
    fun getSensorTypeName(sensorType: Int): String {
        return when (sensorType) {
            Sensor.TYPE_STEP_COUNTER -> "计步器"
            Sensor.TYPE_STEP_DETECTOR -> "计步检测器"
            Sensor.TYPE_ACCELEROMETER -> "加速度传感器"
            Sensor.TYPE_GYROSCOPE -> "陀螺仪"
            Sensor.TYPE_MAGNETIC_FIELD -> "磁场传感器"
            Sensor.TYPE_GRAVITY -> "重力传感器"
            Sensor.TYPE_LINEAR_ACCELERATION -> "线性加速度传感器"
            else -> "未知传感器"
        }
    }

    /**
     * 检查设备是否支持任何计步方式
     * @param context 上下文
     * @return true 支持至少一种计步方式，false 不支持任何计步方式
     */
    fun isAnyStepDetectionSupported(context: Context): Boolean {
        // 优先检查硬件计步器
        if (isStepCounterSensorSupported(context)) {
            return true
        }
        // 其次检查加速度传感器（用于软件计步）
        return isAccelerometerSensorSupported(context)
    }

    /**
     * 获取推荐的计步方式
     * @param context 上下文
     * @return 推荐的计步传感器类型
     */
    fun getRecommendedStepSensorType(context: Context): Int {
        if (isStepCounterSensorSupported(context)) {
            return Sensor.TYPE_STEP_COUNTER
        }
        return Sensor.TYPE_ACCELEROMETER
    }
}