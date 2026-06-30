package com.plinkopro.wincash.business.step

import android.Manifest
import android.app.Activity
import android.os.Build
import com.carefree.steplib.utils.Mkv
import com.plinkopro.wincash.BuildConfig
import com.plinkopro.wincash.utils.PermissionUtil
import com.plinkopro.wincash.utils.SpKey
import com.plinkopro.wincash.utils.SpUtil
import com.plinkopro.wincash.utils.formThousand
import com.plinkopro.wincash.utils.log

object StepBusiness {
    private const val RC_HEALTH_PERMISSION_REQUEST = 100
    const val RC_SETTING = 101

    fun hasRequirePermission(activity: Activity): Boolean {

        // 构建需要请求的权限列表
        val permissionsList = getRequirePermission(activity)
        val result = permissionsList.isEmpty()
        if (BuildConfig.LOG_OUTPUT) {
            log("hasRequirePermission: $result")
        }
        return result
    }

    fun requestPermission(activity: Activity) {
        PermissionUtil.requestPermissions(activity,
            getRequirePermission(activity).toTypedArray(), RC_HEALTH_PERMISSION_REQUEST)
    }

    fun getRequirePermission(activity: Activity): List<String> {

        // 构建需要请求的权限列表
        val permissionsList = mutableListOf<String>()

        // Android 10及以上需要活动识别权限
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q &&
            !PermissionUtil.hasPermission(activity, Manifest.permission.ACTIVITY_RECOGNITION)) {
            permissionsList.add(Manifest.permission.ACTIVITY_RECOGNITION)
        }

        // Android 13及以上需要通知权限
//        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU &&
//            !PermissionUtil.hasPermission(activity, Manifest.permission.POST_NOTIFICATIONS)
//        ) {
//            permissionsList.add(Manifest.permission.POST_NOTIFICATIONS)
//        }

        if (BuildConfig.LOG_OUTPUT) {
            log("requirePermission: ${permissionsList.size}")
        }
        return permissionsList
    }

    fun handlePermissionResult(activity: Activity, requestCode: Int, grantResults: IntArray, finish:() -> Unit) {
        if (requestCode == RC_HEALTH_PERMISSION_REQUEST) {
            finish.invoke()
            try {
                if (!hasRequirePermission(activity)) {
                    Mkv.put(SpKey.KEY_PERMISSION_TIME, System.currentTimeMillis() + 48 * 60 * 60 * 1000L)
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    fun getStepGoal(): Int {
        return SpUtil.get(SpKey.GOAL_STEP_COUNT, 6000)
    }

    fun updateStepGoal(goal: Int) {
        SpUtil.Companion.put(SpKey.GOAL_STEP_COUNT, goal)
    }

    /**
     * 将步数转换为卡路里消耗值
     * @param steps 步数
     * @param weight 体重(kg)，默认60kg
     * @return 消耗的卡路里值(kcal)，保留两位小数
     */
    fun stepsToCalories(steps: Int): String {
        return  (steps *0.042).formThousand()
    }

    /**
     * 将步数转换为运动时间（HH:MM格式）
     * @param steps 步数
     * @param stepsPerMinute 每分钟步数，默认100步/分钟
     * @return 运动时间，格式为00:00
     */
    fun stepsToMinutes(steps: Int, stepsPerMinute: Int = 60): String {
        // 计算总分钟数
        val totalMinutes = (steps.toDouble() / stepsPerMinute.toDouble()).toInt()

        // 计算小时和分钟
        val hours = totalMinutes / 60
        val minutes = totalMinutes % 60

        // 格式化输出为00:00格式
        return String.format("%02d:%02d", hours, minutes)
    }

    /**
     * 将步数转换为公里数
     * @param steps 步数
     * @param stepLength 步长(米)，默认0.7米
     * @return 距离（公里），保留两位小数
     */
    fun stepsToKilometers(steps: Int, stepLength: Double = 0.7): String {
        return  (steps*0.0005).formThousand()
    }

}