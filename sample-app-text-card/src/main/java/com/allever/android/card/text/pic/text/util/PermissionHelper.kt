package com.allever.android.card.text.pic.text.util

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.text.TextUtils
import androidx.core.app.AppOpsManagerCompat
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment

/**
 *@Description
 *@author: zq
 *@date: 2024/1/12
 */
object PermissionHelper {

    fun hasPermissionOrigin(context: Context?, permissions: List<String>): Boolean {
        context ?: return false
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) {
            return true
        }
        for (permission in permissions) {
            var result = ContextCompat.checkSelfPermission(context, permission)
            if (result == PackageManager.PERMISSION_DENIED) {
                return false
            }

            val op = AppOpsManagerCompat.permissionToOp(permission)
            if (TextUtils.isEmpty(op)) {
                continue
            }
            result = AppOpsManagerCompat.noteProxyOp(context, op!!, context.packageName)
            if (result != AppOpsManagerCompat.MODE_ALLOWED) {
                return false
            }
        }
        return true
    }

    fun gotoSettingOrigin(context: Activity) {
        openAppDetailSetting(context)
    }

    fun hasAlwaysDeniedPermissionOrigin(
        context: Context? = null,
        deniedPermissions: List<String>
    ): Boolean {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) {
            return false
        }

        if (deniedPermissions.isEmpty()) {
            return false
        }

        val activity = when (context) {
            is Activity -> {
                context
            }
            is Fragment -> {
                context.activity
            }
            else -> {
                null
            }
        }
        for (permission in deniedPermissions) {
            val rationale = activity?.shouldShowRequestPermissionRationale(permission)
            if (rationale == false) {
                return true
            }
        }
        return false
    }


    private fun getAppDetailSettingIntent(activity: Activity): Intent {
        val localIntent = Intent()
        localIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        if (Build.VERSION.SDK_INT >= 9) {
            localIntent.setAction("android.settings.APPLICATION_DETAILS_SETTINGS")
            localIntent.setData(Uri.fromParts("package", activity.packageName, null))
        } else if (Build.VERSION.SDK_INT <= 8) {
            localIntent.setAction(Intent.ACTION_VIEW)
            localIntent.setClassName(
                "com.android.settings",
                "com.android.settings.InstalledAppDetails"
            )
            localIntent.putExtra("com.android.settings.ApplicationPkgName", activity.packageName)
        }
        return localIntent
    }

    fun openAppDetailSetting(activity: Activity) {
        activity.startActivityForResult(getAppDetailSettingIntent(activity), 0x01)
    }


}