package app.allever.android.lib.core.permission.dialog

import android.content.Context
import app.allever.android.lib.core.permission.internal.PermissionHelper

class JumpPermissionSettingDialog(
    context: Context,
    title: String? = "手动授权",
    message: String? = "前往应用详情->权限手动授权"
) :
    PermissionDialog(context, title, message) {
    override fun onClickConfirm() {
        PermissionHelper.jumpSetting(context, PermissionHelper.RC_CODE_JUMP_SETTING)
    }
}