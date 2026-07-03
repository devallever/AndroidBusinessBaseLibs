package org.xm.app.virtual.call.ui.dialog

import android.app.Activity
import androidx.appcompat.app.AlertDialog
import app.allever.android.lib.core.permission.internal.PermissionUtil
import app.allever.android.lib.core.store.StoreCore
import com.allever.app.virtual.call.R


object DialogHelper {
    private const val GUIDE_TIPS_NOT_SHOW = "GUIDE_TIPS_NOT_SHOW"
    fun createGuideDialog(activity: Activity) {
        if (StoreCore.getBoolean(GUIDE_TIPS_NOT_SHOW, false)) {
            return
        }

        AlertDialog.Builder(activity)
            .setNegativeButton(
                R.string.not_tips
            ) { dialog, which ->
                StoreCore.putBoolean(GUIDE_TIPS_NOT_SHOW, true)
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