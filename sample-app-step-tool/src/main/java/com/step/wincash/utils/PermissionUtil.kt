package com.step.wincash.utils

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.provider.Settings
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.step.wincash.base.BaseApplication

/**
 * 权限管理工具类
 * 提供权限检查、请求、结果处理等功能
 */
object PermissionUtil {

    /**
     * 检查单个权限是否已授予
     * @param context 上下文
     * @param permission 要检查的权限
     * @return 是否已授予权限
     */
    fun hasPermission(context: Context, permission: String): Boolean {
        return ContextCompat.checkSelfPermission(
            context,
            permission
        ) == PackageManager.PERMISSION_GRANTED
    }

    /**
     * 检查多个权限是否已全部授予
     * @param context 上下文
     * @param permissions 要检查的权限列表
     * @return 是否已全部授予权限
     */
    fun hasAllPermissions(context: Context, permissions: Array<String>): Boolean {
        return permissions.all { hasPermission(context, it) }
    }

    /**
     * 检查哪些权限尚未授予
     * @param context 上下文
     * @param permissions 要检查的权限列表
     * @return 未授予的权限列表
     */
    fun getDeniedPermissions(context: Context, permissions: Array<String>): List<String> {
        return permissions.filter { !hasPermission(context, it) }
    }

    /**
     * 请求权限
     * @param activity Activity实例
     * @param permissions 要请求的权限列表
     * @param requestCode 请求码
     */
    fun requestPermissions(activity: Activity, permissions: Array<String>, requestCode: Int) {
        val deniedPermissions = getDeniedPermissions(activity, permissions)
        if (deniedPermissions.isNotEmpty()) {
            ActivityCompat.requestPermissions(
                activity,
                deniedPermissions.toTypedArray(),
                requestCode
            )
        }
    }

    /**
     * 处理权限请求结果
     * @param grantResults 权限授予结果
     * @return 是否所有权限都被授予
     */
    fun handlePermissionResult(grantResults: IntArray): Boolean {
        return grantResults.isNotEmpty() && grantResults.all { 
            it == PackageManager.PERMISSION_GRANTED 
        }
    }

    /**
     * 检查是否需要向用户解释为什么需要这些权限
     * @param activity Activity实例
     * @param permission 需要解释的权限
     * @return 是否需要解释
     */
    fun shouldShowRequestPermissionRationale(activity: Activity, permission: String): Boolean {
        return ActivityCompat.shouldShowRequestPermissionRationale(activity, permission)
    }

    /**
     * 检查哪些权限需要向用户解释
     * @param activity Activity实例
     * @param permissions 权限列表
     * @return 需要解释的权限列表
     */
    fun getPermissionsNeedRationale(activity: Activity, permissions: Array<String>): List<String> {
        return permissions.filter { 
            !hasPermission(activity, it) && 
            shouldShowRequestPermissionRationale(activity, it) 
        }
    }

    /**
     * 检查是否是Android 6.0+，需要动态请求权限
     * @return 是否需要动态请求权限
     */
    fun isMarshmallowOrHigher(): Boolean {
        return Build.VERSION.SDK_INT >= Build.VERSION_CODES.M
    }

    /**
     * 检查是否是Android 13+，需要请求通知权限
     * @return 是否需要请求通知权限
     */
    fun isTiramisuOrHigher(): Boolean {
        return Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU
    }

    /**
     * 检查是否拥有通知权限
     * 注意：在Android 13（API 33）及以上版本需要动态请求通知权限
     * @param context 上下文
     * @return 是否已授予通知权限
     */
    fun hasNotificationPermission(context: Context): Boolean {
        // Android 13以下版本默认拥有通知权限
        if (!isTiramisuOrHigher()) {
            return true
        }
        
        return hasPermission(context, Manifest.permission.POST_NOTIFICATIONS)
    }

    /**
     * 请求通知权限
     * 注意：仅在Android 13（API 33）及以上版本需要调用此方法
     * @param activity Activity实例
     * @param requestCode 请求码
     * @return 是否执行了权限请求（只有在Android 13+且未授予权限时才会请求）
     */
    fun requestNotificationPermission(activity: Activity, requestCode: Int): Boolean {
        // 仅在Android 13+且未授予通知权限时才请求
        if (isTiramisuOrHigher() && !hasNotificationPermission(activity)) {
            ActivityCompat.requestPermissions(
                activity,
                arrayOf(Manifest.permission.POST_NOTIFICATIONS),
                requestCode
            )
            return true
        }
        return false
    }

    /**
     * 判断权限是否被用户彻底拒绝（勾选了"不再询问"选项）
     * @param activity Activity实例
     * @param permission 要检查的权限
     * @return 是否被彻底拒绝
     */
    fun isPermissionPermanentlyDenied(activity: Activity, permission: String): Boolean {
        // 权限未授予且系统不再显示请求权限的解释说明
        return !hasPermission(activity, permission) && 
               !shouldShowRequestPermissionRationale(activity, permission)
    }

    /**
     * 获取所有被彻底拒绝的权限列表
     * @param activity Activity实例
     * @param permissions 要检查的权限列表
     * @return 被彻底拒绝的权限列表
     */
    fun getPermanentlyDeniedPermissions(activity: Activity, permissions: Array<String>): List<String> {
        return permissions.filter { isPermissionPermanentlyDenied(activity, it) }
    }
    
    /**
     * 判断所有传入的权限是否都被彻底拒绝
     * @param activity Activity实例
     * @param permissions 要检查的权限列表
     * @return 是否所有权限都被彻底拒绝
     */
    fun areAllPermissionsPermanentlyDenied(activity: Activity, permissions: Array<String>): Boolean {
        if (permissions.isEmpty()) return false
        return permissions.all { isPermissionPermanentlyDenied(activity, it) }
    }

    /**
     * 判断通知权限是否被彻底拒绝
     * @param activity Activity实例
     * @return 是否被彻底拒绝（仅在Android 13+版本有效）
     */
    fun isNotificationPermissionPermanentlyDenied(activity: Activity): Boolean {
        if (!isTiramisuOrHigher()) {
            return false
        }
        return isPermissionPermanentlyDenied(activity, Manifest.permission.POST_NOTIFICATIONS)
    }

    fun openAppSettings(activity: Activity, requestCode: Int = 100) {
        val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
        val uri = Uri.fromParts("package", BaseApplication.instance.packageName, null)
        intent.setData(uri)
        activity.startActivityForResult(intent, requestCode)
    }

}