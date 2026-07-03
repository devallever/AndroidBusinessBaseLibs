package org.xm.app.virtual.call.ui.dialog

import android.app.Activity
import androidx.appcompat.app.AlertDialog
import com.allever.app.virtual.call.R
import com.allever.lib.common.util.SharedPrefUtils
import com.allever.lib.permission.PermissionUtil


object DialogHelper {
    private const val GUIDE_TIPS_NOT_SHOW = "GUIDE_TIPS_NOT_SHOW"
    fun createGuideDialog(activity: Activity) {
        if (SharedPrefUtils.getBoolean(GUIDE_TIPS_NOT_SHOW, false)) {
            return
        }

        AlertDialog.Builder(activity)
            .setNegativeButton(
                R.string.not_tips
            ) { dialog, which ->
                SharedPrefUtils.putBoolean(GUIDE_TIPS_NOT_SHOW, true)
                dialog.dismiss()
            }
            .setPositiveButton(
                R.string.go
            ) { dialog, which ->
                PermissionUtil.GoToSetting(activity)
            }
            .setCancelable(true)
            .setMessage(R.string.permission_guide)
            .create().show()
    }
}