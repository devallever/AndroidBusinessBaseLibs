package app.allever.android.sample.permission

import app.allever.android.lib.common.ListFragment
import app.allever.android.lib.common.ListViewModel
import app.allever.android.lib.common.adapter.TextClickAdapter
import app.allever.android.lib.common.adapter.bean.TextClickItem
import app.allever.android.lib.common.databinding.FragmentListBinding
import app.allever.android.lib.core.ext.toast
import app.allever.android.lib.core.permission.BluetoothPermissionStrategy
import app.allever.android.lib.core.permission.internal.DefaultEngine
import app.allever.android.lib.core.permission.dialog.JumpPermissionSettingDialog
import app.allever.android.lib.core.permission.PermissionCore
import app.allever.android.lib.core.permission.dialog.WhyRequestPermissionDialog
import app.allever.android.lib.permission.engine.permissionx.PermissionXEngine
import com.chad.library.adapter.base.BaseQuickAdapter

class PermissionEngineFragment: ListFragment<FragmentListBinding, ListViewModel, TextClickItem>() {
    override fun getAdapter(): BaseQuickAdapter<TextClickItem, *> = TextClickAdapter()

    /** 必须在初始化阶段创建 Launcher（不能在点击回调中懒创建，否则 registerForActivityResult 会闪退） */
    private val permissionLauncher = PermissionCore.with(this)

    override fun getList(): MutableList<TextClickItem> = mutableListOf(
        // ==================== PermissionEngine 引擎模式 ====================

        TextClickItem("【引擎】初始化引擎 (DefaultEngine)") {
            // 切换引擎只需改这一行（通常在 Application.onCreate 中调用一次）
            PermissionCore.init { DefaultEngine() }
            toast("引擎已切换为: ${PermissionCore.currentEngineName}")
        },

        TextClickItem("【引擎】初始化引擎 (PermissionXEngine)") {
            // 切换引擎只需改这一行（通常在 Application.onCreate 中调用一次）
            PermissionCore.init { PermissionXEngine() }
            toast("引擎已切换为: ${PermissionCore.currentEngineName}")
        },

        TextClickItem("【引擎】申请相机权限 (链式 API)") {
            permissionLauncher
                .permissions(android.Manifest.permission.CAMERA)
                .onAllGranted { toast("[引擎] 相机权限已授予") }
                .onDenied { denied -> toast("[引擎] 相机权限被拒绝: $denied") }
                .request()
        },

        TextClickItem("【引擎】多权限 + explainReason + forwardToSettings") {
            permissionLauncher
                .permissions(
                    android.Manifest.permission.CAMERA,
                    android.Manifest.permission.RECORD_AUDIO,
                )
                .explainReason { scope ->
                    WhyRequestPermissionDialog(
                        scope.context(),
                        "需要权限",
                        "相机用于拍照识别，录音用于语音输入"
                    ) {
                        scope.proceed()
                    }.show()
                }
                .forwardToSettings { context ->
                    JumpPermissionSettingDialog(context, message = "请手动开启所需权限啦啦").show()
                }
                .onAllGranted { toast("[引擎] 所有权限已授予") }
                .onDenied { denied -> toast("[引擎] 部分权限被拒绝: $denied") }
                .request()
        },

        TextClickItem("【引擎】策略模式 (蓝牙权限自动适配)") {
            permissionLauncher
                .strategy(BluetoothPermissionStrategy)
                .onAllGranted { toast("[引擎] 蓝牙权限已授予（版本自动适配）") }
                .onDenied { denied -> toast("[引擎] 蓝牙权限被拒绝: $denied") }
                .request()
        },

        TextClickItem("【引擎】简化回调版 request(callback)") {
            permissionLauncher
                .permissions(
                    android.Manifest.permission.CAMERA,
                    android.Manifest.permission.ACCESS_FINE_LOCATION,
                )
                .request { allGranted, deniedList ->
                    if (allGranted) {
                        toast("[引擎] 全部授权成功")
                    } else {
                        toast("[引擎] 被拒绝: $deniedList")
                    }
                }
        },

    )
}