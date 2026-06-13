package app.allever.android.lib.core.permission.internal

import android.app.Activity
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import android.text.TextUtils
import androidx.core.app.AppOpsManagerCompat
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import app.allever.android.lib.core.helper.ActivityHelper

object PermissionHelper {

    const val RC_CODE_JUMP_SETTING = 1000

    fun hasPermissions(context: Context?, permission: String): Boolean {
        context ?: return false
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) {
            return true
        }

        var result = ContextCompat.checkSelfPermission(context, permission)
        if (result == PackageManager.PERMISSION_DENIED) {
            return false
        }

        val op = AppOpsManagerCompat.permissionToOp(permission)
        if (TextUtils.isEmpty(op)) {
            return false
        }

        result = AppOpsManagerCompat.noteProxyOp(context, op!!, context.packageName)
        if (result != AppOpsManagerCompat.MODE_ALLOWED) {

            return false
        }

        return true
    }

    fun hasPermissions(context: Context?, permissions: List<String>): Boolean {
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

    fun jumpSetting(context: Context, requestCode: Int) {
        gotoSettingOrigin()
    }

    fun gotoSettingOrigin(context: Context? = null) {
        PermissionUtil.GoToSetting(context ?: ActivityHelper.getTopActivity())
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
                context.requireActivity()
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
}