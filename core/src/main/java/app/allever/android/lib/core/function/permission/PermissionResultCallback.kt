package app.allever.android.lib.core.function.permission

import android.content.Context

/**
 * 权限请求结果回调
 */
interface PermissionResultCallback {
    /** 所有权限均已授予 */
    fun onAllGranted()

    /** 部分权限被拒绝（非"总是拒绝"） */
    fun onDenied(deniedPermissions: List<String>) {}

    /** 用户选择了"不再询问"（总是拒绝），默认弹出跳转设置弹窗 */
    fun onAlwaysDenied(deniedPermissions: List<String>, context: Context) {
        if (needShowJumpSettingDialog()) {
            getCustomDialog(context)?.show()
                ?: JumpPermissionSettingDialog(
                    context,
                    title = "需要权限",
                    message = "${deniedPermissions.size}个权限被拒绝，请前往设置手动授权"
                ).show()
        }
    }

    /** 自定义的"总是拒绝"弹窗，返回 null 则使用默认弹窗 */
    fun getCustomDialog(context: Context): android.app.Dialog? = null

    /** 是否在 always denied 时显示跳转设置弹窗 */
    fun needShowJumpSettingDialog(): Boolean = true
}