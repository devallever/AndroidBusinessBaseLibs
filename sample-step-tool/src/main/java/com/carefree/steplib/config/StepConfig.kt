package com.carefree.steplib.config

import android.content.Context
import android.content.SharedPreferences
import com.carefree.steplib.common.StepConstants
import com.carefree.steplib.utils.StepTracker

/**
 * 计步配置管理类
 */
object StepConfig {
    private const val PREFS_NAME = "step_counter_prefs"
    private const val KEY_STEP_GOAL = "step_goal"
    private const val KEY_SENSITIVITY = "sensitivity"
    private const val KEY_AUTO_START = "auto_start"
    private const val KEY_USE_HARDWARE_SENSOR = "use_hardware_sensor"
    private const val KEY_NOTIFICATION_ENABLED = "notification_enabled"
    private const val KEY_LAST_RESET_TIME = "last_reset_time"

    private lateinit var sharedPreferences: SharedPreferences

    /**
     * 初始化配置
     */
    fun initialize(context: Context) {
        sharedPreferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    }

    /**
     * 获取步数目标
     */
    var stepGoal: Int
        get() = sharedPreferences.getInt(KEY_STEP_GOAL, StepConstants.DEFAULT_STEP_GOAL)
        set(goal) {
            sharedPreferences.edit().putInt(KEY_STEP_GOAL, goal).apply()
        }

    /**
     * 获取计步灵敏度
     */
    var sensitivity: Int
        get() = sharedPreferences.getInt(KEY_SENSITIVITY, StepConstants.DEFAULT_SENSITIVITY)
        set(sensitivityValue) {
            val clampedSensitivity = 
                sensitivityValue.coerceIn(StepConstants.MIN_SENSITIVITY, StepConstants.MAX_SENSITIVITY)
            sharedPreferences.edit().putInt(KEY_SENSITIVITY, clampedSensitivity).apply()
        }

    /**
     * 获取是否自动启动计步服务
     */
    var isAutoStartEnabled: Boolean
        get() = sharedPreferences.getBoolean(KEY_AUTO_START, true)
        set(enabled) {
            sharedPreferences.edit().putBoolean(KEY_AUTO_START, enabled).apply()
        }

    /**
     * 获取是否优先使用硬件计步传感器
     */
    var isHardwareSensorPreferred: Boolean
        get() = sharedPreferences.getBoolean(KEY_USE_HARDWARE_SENSOR, true)
        set(preferred) {
            sharedPreferences.edit().putBoolean(KEY_USE_HARDWARE_SENSOR, preferred).apply()
        }

    /**
     * 获取是否启用通知栏显示
     */
    var isNotificationEnabled: Boolean
        get() = sharedPreferences.getBoolean(KEY_NOTIFICATION_ENABLED, true)
        set(enabled) {
            sharedPreferences.edit().putBoolean(KEY_NOTIFICATION_ENABLED, enabled).apply()
        }

    /**
     * 获取上次重置时间
     */
    var lastResetTime: Long
        get() = sharedPreferences.getLong(KEY_LAST_RESET_TIME, 0)
        set(time) {
            sharedPreferences.edit().putLong(KEY_LAST_RESET_TIME, time).apply()
        }

    /**
     * 重置配置为默认值
     */
    fun resetToDefaults() {
        sharedPreferences.edit()
            .putInt(KEY_STEP_GOAL, StepConstants.DEFAULT_STEP_GOAL)
            .putInt(KEY_SENSITIVITY, StepConstants.DEFAULT_SENSITIVITY)
            .putBoolean(KEY_AUTO_START, true)
            .putBoolean(KEY_USE_HARDWARE_SENSOR, true)
            .putBoolean(KEY_NOTIFICATION_ENABLED, true)
            .apply()
    }

    /**
     * 获取配置信息的字符串表示
     */
    fun getConfigSummary(): String {
        return "计步配置：\n" +
               "- 步数目标: $stepGoal\n" +
               "- 计步灵敏度: $sensitivity\n" +
               "- 自动启动: $isAutoStartEnabled\n" +
               "- 优先使用硬件传感器: $isHardwareSensorPreferred\n" +
               "- 通知栏显示: $isNotificationEnabled\n" +
               "- 上次重置时间: $lastResetTime"
    }
}