package com.carefree.steplib.common

/**
 * 计步功能相关的常量定义
 */
object StepConstants {
    // 默认步数目标
    const val DEFAULT_STEP_GOAL = 8000
    
    // 计步灵敏度范围
    const val MIN_SENSITIVITY = 1
    const val MAX_SENSITIVITY = 10
    const val DEFAULT_SENSITIVITY = 5

    // MMKV存储的键名
    const val KEY_CURRENT_STEP = "step_current_step"
    const val KEY_CLEAN_STEP = "step_clean_step"
    const val KEY_TODAY_DATE = "step_today_date"
    const val KEY_STEP_OFFSET = "step_step_offset"
    const val KEY_SHUTDOWN = "step_shutdown"
    const val KEY_ELAPSED_REALTIME = "step_elapsed_realtime"
    const val KEY_LAST_SENSOR_TIME = "step_last_sensor_time"

    // LiveEventBus事件名
    const val EVENT_STEP_UPDATE = "step_event_step_update"
    const val EVENT_STEP_CHANGE = "step_event_step_change"

    // 传感器采样频率
    const val SENSOR_SAMPLING_PERIOD = 200000 // 200ms

    // 处理间隔时间
    const val DELAY_SAVE_STEP = 10000 // 10秒
    const val DELAY_REFRESH_NOTIFY = 3000 // 3秒
    const val DELAY_TIME_INTERVAL = 250 // 250毫秒

    // 通知渠道ID
    const val NOTIFICATION_CHANNEL_ID = "step_channel_id"
    const val NOTIFICATION_CHANNEL_NAME = "StepRecord"

    // 广播参数
    const val INTENT_EXTRA_SEPARATE = "intent_extra_separate"
    const val INTENT_EXTRA_BOOT = "intent_extra_boot"
    const val INTENT_EXTRA_STEP_INIT = "intent_extra_step_init"
    const val INTENT_EXTRA_STEP_GOAL = "intent_extra_step_goal"
}