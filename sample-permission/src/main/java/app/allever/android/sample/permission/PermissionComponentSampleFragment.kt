package app.allever.android.sample.permission

import android.content.Context
import android.os.Build
import app.allever.android.lib.common.ListFragment
import app.allever.android.lib.common.ListViewModel
import app.allever.android.lib.common.adapter.TextClickAdapter
import app.allever.android.lib.common.adapter.bean.TextClickItem
import app.allever.android.lib.common.databinding.FragmentListBinding
import app.allever.android.lib.core.ext.log
import app.allever.android.lib.core.ext.toast
import app.allever.android.lib.core.function.permission.*
import com.chad.library.adapter.base.BaseQuickAdapter

/**
 * PermissionLauncherComponent 组件使用示例
 *
 * 展示如何使用 [PermissionLauncherComponent] 配合 [PermissionStrategy] 进行权限请求，
 * 相比 [PermissionBaseSampleFragment] 中的原始写法，代码量大幅减少，版本适配逻辑完全解耦。
 */
class PermissionComponentSampleFragment :
    ListFragment<FragmentListBinding, ListViewModel, TextClickItem>() {

    /** 权限请求组件（核心：一行初始化） */
    private val permissionLauncher = PermissionLauncherComponent(this)

    override fun getAdapter(): BaseQuickAdapter<TextClickItem, *> {
        return TextClickAdapter()
    }

    override fun getList(): MutableList<TextClickItem> {
        return mutableListOf(
            // ==================== 基础用法（直接传权限数组）====================

            TextClickItem("【基础】申请相机权限") {
                permissionLauncher.request(
                    permissions = arrayOf(android.Manifest.permission.CAMERA),
                    onAllGranted = { toast("相机权限已授予") },
                    onDenied = { toast("相机权限被拒绝") },
                )
            },

            TextClickItem("【基础】同时申请多个权限（相机+录音+位置）") {
                permissionLauncher.request(
                    permissions = arrayOf(
                        android.Manifest.permission.CAMERA,
                        android.Manifest.permission.RECORD_AUDIO,
                        android.Manifest.permission.ACCESS_FINE_LOCATION,
                        android.Manifest.permission.ACCESS_COARSE_LOCATION,
                    ),
                    onAllGranted = { toast("所有权限已授予") },
                    onDenied = { deniedList ->
                        toast("${deniedList.size}个权限被拒绝: $deniedList")
                    },
                )
            },

            // ==================== 策略模式用法（自动版本适配）====================

            TextClickItem("【策略】相机权限 (CameraPermissionStrategy)") {
                permissionLauncher.requestByStrategy(
                    strategy = CameraPermissionStrategy,
                    onAllGranted = { toast("相机权限已授予") },
                    onDenied = { toast("相机权限被拒绝") },
                )
            },

            TextClickItem("【策略】媒体资源权限 (MediaPermissionStrategy)") {
                permissionLauncher.requestByStrategy(
                    strategy = MediaPermissionStrategy,
                    onAllGranted = { toast("媒体资源权限已授予") },
                    onDenied = { deniedList -> toast("媒体资源权限被拒绝: $deniedList") },
                )
            },

            TextClickItem("【策略】存储权限 (StoragePermissionStrategy)") {
                permissionLauncher.requestByStrategy(
                    strategy = StoragePermissionStrategy,
                    onAllGranted = { toast("存储权限已授予") },
                    onDenied = { deniedList -> toast("存储权限被拒绝: $deniedList") },
                )
            },

            TextClickItem("【策略】通知权限 (NotificationPermissionStrategy)") {
                requestNotificationByComponent()
            },

            TextClickItem("【策略】前台位置权限 (ForegroundLocationPermissionStrategy)") {
                permissionLauncher.requestByStrategy(
                    strategy = ForegroundLocationPermissionStrategy,
                    onAllGranted = { toast("前台位置权限已授予") },
                    onDenied = { deniedList -> toast("前台位置权限被拒绝: $deniedList") },
                )
            },

            TextClickItem("【策略】蓝牙权限 (BluetoothPermissionStrategy)") {
                permissionLauncher.requestByStrategy(
                    strategy = BluetoothPermissionStrategy,
                    onAllGranted = { toast("蓝牙权限已授予") },
                    onDenied = { deniedList -> toast("蓝牙权限被拒绝: $deniedList") },
                )
            },

            TextClickItem("【策略】邻近设备权限 (NearbyDevicesPermissionStrategy)") {
                if (Build.VERSION.SDK_INT < Build.VERSION_CODES.S) {
                    toast("当前系统版本不支持独立邻近设备权限，请使用蓝牙权限")
                    return@TextClickItem
                }
                permissionLauncher.requestByStrategy(
                    strategy = NearbyDevicesPermissionStrategy,
                    onAllGranted = { toast("邻近设备权限已授予") },
                    onDenied = { deniedList -> toast("邻近设备权限被拒绝: $deniedList") },
                )
            },

            TextClickItem("【策略】联系人权限 (ContactsPermissionStrategy)") {
                permissionLauncher.requestByStrategy(
                    strategy = ContactsPermissionStrategy,
                    onAllGranted = { toast("联系人权限已授予") },
                    onDenied = { deniedList -> toast("联系人权限被拒绝: $deniedList") },
                )
            },

            TextClickItem("【策略】电话权限 (PhonePermissionStrategy)") {
                permissionLauncher.requestByStrategy(
                    strategy = PhonePermissionStrategy,
                    onAllGranted = { toast("电话权限已授予") },
                    onDenied = { deniedList -> toast("电话权限被拒绝: $deniedList") },
                )
            },

            TextClickItem("【策略】短信权限 (SmsPermissionStrategy)") {
                permissionLauncher.requestByStrategy(
                    strategy = SmsPermissionStrategy,
                    onAllGranted = { toast("短信权限已授予") },
                    onDenied = { deniedList -> toast("短信权限被拒绝: $deniedList") },
                )
            },

            TextClickItem("【策略】日历权限 (CalendarPermissionStrategy)") {
                permissionLauncher.requestByStrategy(
                    strategy = CalendarPermissionStrategy,
                    onAllGranted = { toast("日历权限已授予") },
                    onDenied = { deniedList -> toast("日历权限被拒绝: $deniedList") },
                )
            },

            TextClickItem("【策略】传感器权限 (SensorPermissionStrategy)") {
                permissionLauncher.requestByStrategy(
                    strategy = SensorPermissionStrategy,
                    onAllGranted = { toast("传感器权限已授予") },
                    onDenied = { deniedList -> toast("传感器权限被拒绝: $deniedList") },
                )
            },

            // ==================== 高级用法 ====================

            TextClickItem("【高级】自定义 always denied 弹窗") {
                permissionLauncher.request(
                    permissions = arrayOf(android.Manifest.permission.CAMERA),
                    onAllGranted = { toast("相机权限已授予") },
                    onDenied = { toast("相机权限被拒绝") },
                    onAlwaysDenied = { _, context ->
                        // 自定义弹窗样式
                        JumpPermissionSettingDialog(
                            context,
                            title = "需要相机权限",
                            message = "您拒绝了相机权限且选择了'不再询问'，请在设置中手动开启"
                        ).show()
                    }
                )
            },

            TextClickItem("【高级】自定义 PermissionResultCallback") {
                permissionLauncher.request(
                    permissions = arrayOf(
                        android.Manifest.permission.CAMERA,
                        android.Manifest.permission.RECORD_AUDIO,
                    ),
                    callback = object : PermissionResultCallback {
                        override fun onAllGranted() {
                            log("所有权限均已授予")
                            toast("所有权限均已授予")
                        }

                        override fun onDenied(deniedPermissions: List<String>) {
                            log("部分权限被拒绝: $deniedPermissions")
                            toast("部分权限被拒绝")
                        }

                        override fun onAlwaysDenied(deniedPermissions: List<String>, context: Context) {
                            log("总是拒绝的权限: $deniedPermissions")
                            // 使用默认弹窗
                            super.onAlwaysDenied(deniedPermissions, context)
                        }
                    }
                )
            },

            TextClickItem("【高级】组合策略 - 蓝牙 + 位置 + 存储") {
                // 可以一次性请求多个策略的权限合并
                val combinedPermissions = buildList {
                    addAll(BluetoothPermissionStrategy.getPermissions())
                    addAll(ForegroundLocationPermissionStrategy.getPermissions())
                    addAll(StoragePermissionStrategy.getPermissions())
                }.toTypedArray()

                permissionLauncher.request(
                    permissions = combinedPermissions,
                    onAllGranted = { toast("蓝牙 + 位置 + 存储权限全部授予") },
                    onDenied = { deniedList -> toast("${deniedList.size}个权限被拒绝") },
                )
            },
        )
    }

    /**
     * 通知权限特殊处理：
     * - Android 13+: 使用 POST_NOTIFICATIONS 运行时权限
     * - Android 8.0~12: 需跳转设置页（非运行时权限）
     * - Android 7.1 及以下: 无需权限
     */
    private fun requestNotificationByComponent() {
        when {
            Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU -> {
                // Android 13+: 运行时权限
                permissionLauncher.requestByStrategy(
                    strategy = NotificationPermissionStrategy,
                    onAllGranted = { toast("通知权限已授予") },
                    onDenied = { toast("通知权限被拒绝") },
                )
            }
            Build.VERSION.SDK_INT >= Build.VERSION_CODES.O -> {
                // Android 8.0~12: 跳转通知设置页
                val intent = NotificationPermissionStrategy.getNotificationSettingsIntent(requireContext())
                startActivity(intent)
                toast("已跳转到通知设置页面")
            }
            else -> {
                toast("当前系统版本无需申请通知权限")
            }
        }
    }
}
