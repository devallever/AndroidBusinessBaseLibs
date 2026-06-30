package com.carefree.steplib.lib

/**
 * @classDes:
 * @author: 稻谷
 * @create date: 2024/7/4 17:51
 */
object ConstStep {
    const val APP_SHARD: String = "today_step_share_prefs"

    // 上一次计步器的步数
    const val LAST_SENSOR_TIME: String = "last_sensor_time"
    // 步数补偿数值，每次传感器返回的步数-offset=当前步数
    const val STEP_OFFSET: String = "step_offset"
    // 当天，用来判断是否跨天
    const val STEP_TODAY: String = "step_today"
    // 清除步数
    const val CLEAN_STEP: String = "clean_step"
    // 当前步数
    const val CURR_STEP: String = "curr_step"
    //手机关机监听
    const val SHUTDOWN: String = "shutdown"
    //系统运行时间
    const val ELAPSED_REALTIMEl: String = "elapsed_realtime"

    // 步数更新event
    const val STEP_EVENT: String = "step_event"
    const val STEP_EVENT_CHANGE: String = "step_event_change"

    const val STEP_GOAL_DEF = 10000
}