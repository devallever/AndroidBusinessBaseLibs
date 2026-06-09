package app.allever.android.lib.core.permission

import android.app.Activity
import android.content.Context
import android.os.Build
import androidx.fragment.app.Fragment
import app.allever.android.lib.core.helper.ActivityHelper

object PermissionHelper {

    const val RC_CODE_JUMP_SETTING = 1000

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