package app.allever.android.sample.permission

import android.Manifest
import android.content.Intent
import android.os.Build
import android.os.Environment
import android.provider.Settings
import androidx.activity.result.contract.ActivityResultContracts
import app.allever.android.lib.common.ListFragment
import app.allever.android.lib.common.ListViewModel
import app.allever.android.lib.common.adapter.TextClickAdapter
import app.allever.android.lib.common.adapter.bean.TextClickItem
import app.allever.android.lib.common.databinding.FragmentListBinding
import app.allever.android.lib.core.ext.log
import app.allever.android.lib.core.ext.toast
import app.allever.android.lib.core.permission.dialog.JumpPermissionSettingDialog
import app.allever.android.lib.core.permission.internal.PermissionHelper
import com.chad.library.adapter.base.BaseQuickAdapter
import androidx.core.net.toUri

class PermissionBaseSampleFragment: ListFragment<FragmentListBinding, ListViewModel, TextClickItem>() {

    // ==================== 相机权限 ====================

    private val requestCameraPermissionLauncher =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted: Boolean ->
            if (isGranted) {
                log("相机权限已授予，可以使用相机功能")
                toast("相机权限已授予，可以使用相机功能")
            } else {
                if (PermissionHelper.hasAlwaysDeniedPermissionOrigin(requireActivity(), listOf(Manifest.permission.CAMERA))) {
                    JumpPermissionSettingDialog(requireActivity()).show()
                } else {
                    log("相机权限被拒绝")
                    toast("相机权限被拒绝")
                }
            }
        }

    // ==================== 多权限 ====================

    private val requestMultiPermissionLauncher =
        registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { permissions ->
            var isAllGranted = true
            val deniedList = mutableListOf<String>()
            permissions.entries.forEach {
                if (!it.value) {
                    isAllGranted = false
                    deniedList.add(it.key)
                }
            }
            if (isAllGranted) {
                log("权限已授予，可以使用相机、录音功能")
                toast("权限已授予，可以使用相机、录音功能")
            } else {
                deniedList.forEach {
                    log("权限被拒绝：$it")
                }
                toast("权限被拒绝")
                if (PermissionHelper.hasAlwaysDeniedPermissionOrigin(requireActivity(), deniedList)) {
                    JumpPermissionSettingDialog(requireActivity(), message = "${deniedList.size}个权限总是被拒绝，手动授权").show()
                }
            }
        }

    // ==================== 媒体资源权限 ====================

    /**
     * 媒体资源权限 Launcher
     * Android 13+ 使用细粒度权限：READ_MEDIA_IMAGES / READ_MEDIA_VIDEO / READ_MEDIA_AUDIO
     * Android 12 及以下使用：READ_EXTERNAL_STORAGE
     */
    private val requestMediaPermissionLauncher =
        registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { permissions ->
            var isAllGranted = true
            val deniedList = mutableListOf<String>()
            permissions.entries.forEach {
                if (!it.value) {
                    isAllGranted = false
                    deniedList.add(it.key)
                }
            }
            if (isAllGranted) {
                log("媒体资源权限已授予")
                toast("媒体资源权限已授予")
            } else {
                deniedList.forEach { log("媒体权限被拒绝：$it") }
                toast("媒体资源权限被拒绝")
                if (PermissionHelper.hasAlwaysDeniedPermissionOrigin(requireActivity(), deniedList)) {
                    JumpPermissionSettingDialog(requireActivity(), message = "媒体权限总是被拒绝，请手动授权").show()
                }
            }
        }

    // ==================== 存储权限 ====================

    private val requestStoragePermissionLauncher =
        registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { permissions ->
            var isAllGranted = true
            val deniedList = mutableListOf<String>()
            permissions.entries.forEach {
                if (!it.value) {
                    isAllGranted = false
                    deniedList.add(it.key)
                }
            }
            if (isAllGranted) {
                log("存储权限已授予")
                toast("存储权限已授予")
            } else {
                deniedList.forEach { log("存储权限被拒绝：$it") }
                toast("存储权限被拒绝")
                if (PermissionHelper.hasAlwaysDeniedPermissionOrigin(requireActivity(), deniedList)) {
                    JumpPermissionSettingDialog(requireActivity(), message = "存储权限总是被拒绝，请手动授权").show()
                }
            }
        }

    // ==================== 完整存储访问权限 (MANAGE_EXTERNAL_STORAGE) ====================

    /**
     * MANAGE_EXTERNAL_STORAGE 是特殊权限（Special Permission），
     * 无法通过常规运行时权限对话框申请，必须跳转到系统设置页由用户手动授权。
     * 使用 StartActivityForResult 跳转，通过返回结果判断是否授权成功。
     */
    private val manageStorageLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { _ ->
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                if (Environment.isExternalStorageManager()) {
                    log("完整存储访问权限已授予")
                    toast("完整存储访问权限已授予")
                } else {
                    log("用户未授予完整存储访问权限")
                    toast("未授予完整存储访问权限")
                }
            }
        }

    // ==================== 通知权限 ====================

    /**
     * Android 13+ (API >= 33) 使用运行时权限 POST_NOTIFICATIONS
     * Android 12 及以下无需运行时权限，跳转到通知设置页
     */
    private val requestNotificationPermissionLauncher =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted: Boolean ->
            if (isGranted) {
                log("通知权限已授予")
                toast("通知权限已授予")
            } else {
                log("通知权限被拒绝")
                toast("通知权限被拒绝")
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU &&
                    PermissionHelper.hasAlwaysDeniedPermissionOrigin(
                        requireActivity(),
                        listOf(Manifest.permission.POST_NOTIFICATIONS)
                    )
                ) {
                    JumpPermissionSettingDialog(
                        requireContext(),
                        title = "需要通知权限",
                        message = "通知权限被拒绝，请前往应用详情 -> 通知中手动开启"
                    ).show()
                }
            }
        }

    /** 跳转通知设置页的 Launcher（用于 Android 12 及以下） */
    private val notificationSettingsLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { _ ->
            log("用户已从通知设置页返回")
        }

    override fun getAdapter(): BaseQuickAdapter<TextClickItem, *> = TextClickAdapter()

    override fun getList(): MutableList<TextClickItem> = mutableListOf(
        TextClickItem("申请相机权限 (ActivityResultContract)") {
            requestCameraPermission()
        },
        TextClickItem("申请多个权限 (ActivityResultContract)") {
            requestMultiPermission()
        },
        TextClickItem("申请媒体资源权限 (版本适配)") {
            requestMediaPermission()
        },
        TextClickItem("申请存储权限 (版本适配)") {
            requestStoragePermission()
        },
        TextClickItem("申请完整存储访问权限 (MANAGE_EXTERNAL_STORAGE)") {
            requestManageExternalStorage()
        },
        TextClickItem("申请通知权限 (版本适配)") {
            requestNotificationPermission()
        },
        TextClickItem("申请前台位置权限") {
            requestForegroundLocationPermission()
        },
        TextClickItem("申请后台位置权限 (版本适配)") {
            requestBackgroundLocationPermission()
        },
        TextClickItem("申请蓝牙权限 (版本适配)") {
            requestBluetoothPermission()
        },
        TextClickItem("申请联系人权限") {
            requestContactsPermission()
        },
        TextClickItem("申请电话权限") {
            requestPhonePermission()
        },
        TextClickItem("申请短信权限") {
            requestSmsPermission()
        },
        TextClickItem("申请日历权限") {
            requestCalendarPermission()
        },
        TextClickItem("申请传感器权限 (版本适配)") {
            requestSensorPermission()
        },
        TextClickItem("申请邻近设备权限 (版本适配)") {
            requestNearbyDevicesPermission()
        },
    )

    private fun requestCameraPermission() {
        requestCameraPermissionLauncher.launch(Manifest.permission.CAMERA)
    }

    private fun requestMultiPermission() {
        requestMultiPermissionLauncher.launch(arrayOf(Manifest.permission.CAMERA, Manifest.permission.RECORD_AUDIO))
    }

    /**
     * 根据当前系统版本请求对应的媒体资源权限
     * - Android 13+ (API >= 33): READ_MEDIA_IMAGES + READ_MEDIA_VIDEO + READ_MEDIA_AUDIO（细粒度）
     * - Android 12 及以下 (API < 33): READ_EXTERNAL_STORAGE（统一存储权限）
     */
    private fun requestMediaPermission() {
        val permissions = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            // Android 13+ 细粒度媒体权限
            arrayOf(
                Manifest.permission.READ_MEDIA_IMAGES,
                Manifest.permission.READ_MEDIA_VIDEO,
                Manifest.permission.READ_MEDIA_AUDIO
            )
        } else {
            // Android 12 及以下统一存储权限
            arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE)
        }
        requestMediaPermissionLauncher.launch(permissions)
    }

    /**
     * 根据当前系统版本请求存储权限
     *
     * Android 版本适配说明：
     * - Android 9 及以下 (API <= 28):
     *   请求 READ_EXTERNAL_STORAGE + WRITE_EXTERNAL_STORAGE，可读写所有文件
     *
     * - Android 10 ~ 12 (API 29 ~ 32):
     *   分区存储（Scoped Storage）逐步强制生效
     *   WRITE_EXTERNAL_STORAGE 仅对应用专属目录有效，访问公共目录需通过 MediaStore / SAF
     *   仍需请求 READ_EXTERNAL_STORAGE 读取其他应用的媒体文件
     *
     * - Android 13+ (API >= 33):
     *   READ_EXTERNAL_STORAGE 已废弃移除
     *   媒体文件按类型拆分为细粒度权限（READ_MEDIA_IMAGES / VIDEO / AUDIO）
     *   应用专属目录无需任何权限即可读写
     *   非媒体文件使用 Storage Access Framework (SAF) 无需预申请权限
     *   如需完整存储访问权限，需申请特殊权限 MANAGE_EXTERNAL_STORAGE（跳转设置页授权）
     */
    private fun requestStoragePermission() {
        val permissions = when {
            Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU -> {
                // Android 13+: 细粒度媒体权限（非媒体文件无需权限）
                toast("Android 13+: 应用专属目录无需权限，此处请求媒体文件读取权限")
                arrayOf(
                    Manifest.permission.READ_MEDIA_IMAGES,
                    Manifest.permission.READ_MEDIA_VIDEO,
                    Manifest.permission.READ_MEDIA_AUDIO
                )
            }
            Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q -> {
                // Android 10~12: 分区存储，WRITE 外部公共目录受限
                arrayOf(
                    Manifest.permission.READ_EXTERNAL_STORAGE
                )
            }
            else -> {
                // Android 9 及以下: 传统存储权限
                arrayOf(
                    Manifest.permission.READ_EXTERNAL_STORAGE,
                    Manifest.permission.WRITE_EXTERNAL_STORAGE
                )
            }
        }
        requestStoragePermissionLauncher.launch(permissions)
    }

    /**
     * 请求 MANAGE_EXTERNAL_STORAGE（所有文件访问权限）
     *
     * 适用场景：文件管理器、清理工具等需要遍历整个存储空间的 App
     *
     * 注意事项：
     * - 仅 Android 11+ (API 30+) 可用
     * - 需在 AndroidManifest 中声明 android.permission.MANAGE_EXTERNAL_STORAGE
     * - Google Play 审核严格，需提供正当理由才可使用
     * - 授权后可通过 Environment.getExternalStorageDirectory() 访问全部文件
     */
    private fun requestManageExternalStorage() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.R) {
            toast("MANAGE_EXTERNAL_STORAGE 仅支持 Android 11 及以上版本")
            return
        }

        if (Environment.isExternalStorageManager()) {
            toast("已有完整存储访问权限")
            return
        }

        try {
            // 方式一：直接跳转到"所有文件访问权限"设置页
            val intent = Intent(Settings.ACTION_MANAGE_APP_ALL_FILES_ACCESS_PERMISSION).apply {
                data = "package:${requireContext().packageName}".toUri()
            }
            manageStorageLauncher.launch(intent)
        } catch (e: Exception) {
            // 方式一失败时回退到应用详情页
            log("跳转所有文件访问权限页失败: ${e.message}", e.message)
            try {
                val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
                    data = "package:${requireContext().packageName}".toUri()
                }
                manageStorageLauncher.launch(intent)
            } catch (e2: Exception) {
                log("跳转应用详情页也失败: ${e2.message}", e2.message)
                JumpPermissionSettingDialog(
                    requireContext(),
                    title = "需要完整存储访问权限",
                    message = "请前往 设置 -> 应用 -> 权限 -> 管理所有文件 中开启"
                ).show()
            }
        }
    }

    /**
     * 根据当前系统版本请求通知权限
     *
     * Android 版本适配说明：
     * - Android 13+ (API >= 33):
     *   需要运行时请求 POST_NOTIFICATIONS 权限，否则无法发送通知（前台服务通知除外）
     *
     * - Android 8.0 ~ 12 (API 26 ~ 32):
     *   无需运行时权限，但可跳转到应用通知设置页让用户管理通知渠道和开关
     *   可通过 NotificationManager.areNotificationsEnabled() 检查是否开启
     *
     * - Android 7.1 及以下 (API < 26):
     *   无需任何运行时权限即可发送通知
     */
    private fun requestNotificationPermission() {
        when {
            Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU -> {
                // Android 13+: 运行时请求 POST_NOTIFICATIONS 权限
                requestNotificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
            }
            Build.VERSION.SDK_INT >= Build.VERSION_CODES.O -> {
                // Android 8.0 ~ 12: 跳转到应用通知设置页
                val intent = Intent(Settings.ACTION_APP_NOTIFICATION_SETTINGS).apply {
                    putExtra(Settings.EXTRA_APP_PACKAGE, requireContext().packageName)
                }
                notificationSettingsLauncher.launch(intent)
            }
            else -> {
                // Android 7.1 及以下：无需权限即可发送通知
                toast("当前系统版本无需申请通知权限")
            }
        }
    }

    // ==================== 前台位置权限 ====================

    private val requestForegroundLocationLauncher =
        registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { permissions ->
            var isAllGranted = true
            val deniedList = mutableListOf<String>()
            permissions.entries.forEach {
                if (!it.value) {
                    isAllGranted = false
                    deniedList.add(it.key)
                }
            }
            if (isAllGranted) {
                log("前台位置权限已授予")
                toast("前台位置权限已授予")
            } else {
                deniedList.forEach { log("前台位置权限被拒绝：$it") }
                toast("前台位置权限被拒绝")
                if (PermissionHelper.hasAlwaysDeniedPermissionOrigin(requireActivity(), deniedList)) {
                    JumpPermissionSettingDialog(
                        requireActivity(),
                        message = "位置权限总是被拒绝，请手动授权"
                    ).show()
                }
            }
        }

    // ==================== 后台位置权限 ====================

    /** Android 10+ 后台位置权限 Launcher（必须单独请求） */
    private val requestBackgroundLocationLauncher =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted: Boolean ->
            if (isGranted) {
                log("后台位置权限已授予")
                toast("后台位置权限已授予")
            } else {
                log("后台位置权限被拒绝")
                toast("后台位置权限被拒绝")
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q &&
                    PermissionHelper.hasAlwaysDeniedPermissionOrigin(
                        requireActivity(),
                        listOf(Manifest.permission.ACCESS_BACKGROUND_LOCATION)
                    )
                ) {
                    JumpPermissionSettingDialog(
                        requireContext(),
                        title = "需要后台位置权限",
                        message = "后台位置权限被拒绝，请前往应用详情 -> 权限中手动开启"
                    ).show()
                }
            }
        }

    /**
     * 请求前台位置权限（精确 + 大概）
     * ACCESS_FINE_LOCATION: 精确位置（GPS）
     * ACCESS_COARSE_LOCATION: 大概位置（网络/WiFi 定位）
     *
     * 适用场景：地图导航、附近搜索、打卡签到等使用 App 时获取位置
     */
    private fun requestForegroundLocationPermission() {
        requestForegroundLocationLauncher.launch(
            arrayOf(
                Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.ACCESS_COARSE_LOCATION
            )
        )
    }

    /**
     * 根据当前系统版本请求后台位置权限
     *
     * Android 版本适配说明：
     * - Android 9 及以下 (API <= 28):
     *   无单独的后台定位概念，只要授予了前台位置权限即可在后台获取位置
     *
     * - Android 10 (API 29):
     *   新增 ACCESS_BACKGROUND_LOCATION 权限
     *   可与前台位置权限一起请求，也可单独请求
     *
     * - Android 11+ (API >= 30):
     *   ACCESS_BACKGROUND_LOCATION 不能与前台位置权限一起请求
     *   必须在获得前台位置权限后单独请求
     *   用户会看到"始终允许" / "仅在使用中允许" 的选择对话框
     *
     * - Android 14 (API 34):
     *   用户可以选择"仅授予大概位置"而非精确位置
     *   即使拒绝了精确位置，也可能授予大概位置
     */
    private fun requestBackgroundLocationPermission() {
        when {
            Build.VERSION.SDK_INT < Build.VERSION_CODES.Q -> {
                // Android 9 及以下：无后台位置权限概念
                toast("当前系统版本无独立的后台位置权限，请先申请前台位置权限")
            }
            else -> {
                // Android 10+: 单独请求 ACCESS_BACKGROUND_LOCATION
                // 注意：Android 11+ 必须在已有前台位置权限后才可请求后台位置权限
                requestBackgroundLocationLauncher.launch(Manifest.permission.ACCESS_BACKGROUND_LOCATION)
            }
        }
    }

    // ==================== 蓝牙权限 ====================

    /** 蓝牙权限 Launcher（Android 12+ 使用新蓝牙权限体系） */
    private val requestBluetoothLauncher =
        registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { permissions ->
            var isAllGranted = true
            val deniedList = mutableListOf<String>()
            permissions.entries.forEach {
                if (!it.value) {
                    isAllGranted = false
                    deniedList.add(it.key)
                }
            }
            if (isAllGranted) {
                log("蓝牙权限已授予")
                toast("蓝牙权限已授予")
            } else {
                deniedList.forEach { log("蓝牙权限被拒绝：$it") }
                toast("蓝牙权限被拒绝")
                if (PermissionHelper.hasAlwaysDeniedPermissionOrigin(requireActivity(), deniedList)) {
                    JumpPermissionSettingDialog(
                        requireActivity(),
                        message = "蓝牙权限总是被拒绝，请手动授权"
                    ).show()
                }
            }
        }

    /**
     * 根据当前系统版本请求蓝牙权限
     *
     * Android 版本适配说明：
     * - Android 11 及以下 (API <= 30):
     *   使用旧版蓝牙权限：BLUETOOTH + BLUETOOTH_ADMIN
     *   扫描蓝牙设备还需要 ACCESS_FINE_LOCATION（因为可通过蓝牙信号估算距离）
     *
     * - Android 12 (API 31) 及以上:
     *   引入新的细粒度蓝牙权限，替代旧权限：
     *   BLUETOOTH_SCAN: 扫描附近的蓝牙设备（替代 BLUETOOTH_ADMIN + ACCESS_FINE_LOCATION）
     *   BLUETOOTH_CONNECT: 与已配对设备通信（替代 BLUETOOTH）
     *   BLUETOOTH_ADVERTISE: 使设备可被其他设备发现
     *   旧的 BLUETOOTH / BLUETOOTH_ADMIN 在 Android 12+ 已废弃但仍有效
     */
    private fun requestBluetoothPermission() {
        val permissions = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            // Android 12+: 新蓝牙权限体系
            arrayOf(
                Manifest.permission.BLUETOOTH_SCAN,
                Manifest.permission.BLUETOOTH_CONNECT,
                Manifest.permission.BLUETOOTH_ADVERTISE
            )
        } else {
            // Android 11 及以下：旧蓝牙权限 + 位置权限
            arrayOf(
                Manifest.permission.BLUETOOTH,
                Manifest.permission.BLUETOOTH_ADMIN,
                Manifest.permission.ACCESS_FINE_LOCATION
            )
        }
        requestBluetoothLauncher.launch(permissions)
    }

    // ==================== 联系人权限 ====================

    private val requestContactsLauncher =
        registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { permissions ->
            var isAllGranted = true
            val deniedList = mutableListOf<String>()
            permissions.entries.forEach {
                if (!it.value) {
                    isAllGranted = false
                    deniedList.add(it.key)
                }
            }
            if (isAllGranted) {
                log("联系人权限已授予")
                toast("联系人权限已授予")
            } else {
                deniedList.forEach { log("联系人权限被拒绝：$it") }
                toast("联系人权限被拒绝")
                if (PermissionHelper.hasAlwaysDeniedPermissionOrigin(requireActivity(), deniedList)) {
                    JumpPermissionSettingDialog(
                        requireActivity(),
                        message = "联系人权限总是被拒绝，请手动授权"
                    ).show()
                }
            }
        }

    /**
     * 请求联系人权限
     * READ_CONTACTS: 读取通讯录，用于联系人选择器、通讯录同步等
     * WRITE_CONTACTS: 写入通讯录，用于新建/编辑联系人
     * 无特殊版本适配，所有版本行为一致
     */
    private fun requestContactsPermission() {
        requestContactsLauncher.launch(
            arrayOf(
                Manifest.permission.READ_CONTACTS,
                Manifest.permission.WRITE_CONTACTS
            )
        )
    }

    // ==================== 电话权限 ====================

    private val requestPhoneLauncher =
        registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { permissions ->
            var isAllGranted = true
            val deniedList = mutableListOf<String>()
            permissions.entries.forEach {
                if (!it.value) {
                    isAllGranted = false
                    deniedList.add(it.key)
                }
            }
            if (isAllGranted) {
                log("电话权限已授予")
                toast("电话权限已授予")
            } else {
                deniedList.forEach { log("电话权限被拒绝：$it") }
                toast("电话权限被拒绝")
                if (PermissionHelper.hasAlwaysDeniedPermissionOrigin(requireActivity(), deniedList)) {
                    JumpPermissionSettingDialog(
                        requireActivity(),
                        message = "电话权限总是被拒绝，请手动授权"
                    ).show()
                }
            }
        }

    /**
     * 请求电话相关权限
     * READ_PHONE_STATE: 读取手机状态（IMEI、手机号等），设备标识、来电拦截等场景
     * CALL_PHONE: 拨打电话
     * ANSWER_PHONE_CALLS (Android 8.0+): 接听电话
     * READ_PHONE_NUMBERS (Android 8.0+): 读取手机号
     */
    private fun requestPhonePermission() {
        val permissions = mutableListOf<String>().apply {
            add(Manifest.permission.READ_PHONE_STATE)
            add(Manifest.permission.CALL_PHONE)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                add(Manifest.permission.ANSWER_PHONE_CALLS)
                add(Manifest.permission.READ_PHONE_NUMBERS)
            }
        }.toTypedArray()
        requestPhoneLauncher.launch(permissions)
    }

    // ==================== 短信权限 ====================

    private val requestSmsLauncher =
        registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { permissions ->
            var isAllGranted = true
            val deniedList = mutableListOf<String>()
            permissions.entries.forEach {
                if (!it.value) {
                    isAllGranted = false
                    deniedList.add(it.key)
                }
            }
            if (isAllGranted) {
                log("短信权限已授予")
                toast("短信权限已授予")
            } else {
                deniedList.forEach { log("短信权限被拒绝：$it") }
                toast("短信权限被拒绝")
                if (PermissionHelper.hasAlwaysDeniedPermissionOrigin(requireActivity(), deniedList)) {
                    JumpPermissionSettingDialog(
                        requireActivity(),
                        message = "短信权限总是被拒绝，请手动授权"
                    ).show()
                }
            }
        }

    /**
     * 请求短信权限
     * SEND_SMS: 发送短信
     * RECEIVE_SMS: 接收短信
     * READ_SMS: 读取短信内容
     *
     * 注意：Google Play 对短信权限审核严格，需提供正当理由才可使用
     */
    private fun requestSmsPermission() {
        requestSmsLauncher.launch(
            arrayOf(
                Manifest.permission.SEND_SMS,
                Manifest.permission.RECEIVE_SMS,
                Manifest.permission.READ_SMS
            )
        )
    }

    // ==================== 日历权限 ====================

    private val requestCalendarLauncher =
        registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { permissions ->
            var isAllGranted = true
            val deniedList = mutableListOf<String>()
            permissions.entries.forEach {
                if (!it.value) {
                    isAllGranted = false
                    deniedList.add(it.key)
                }
            }
            if (isAllGranted) {
                log("日历权限已授予")
                toast("日历权限已授予")
            } else {
                deniedList.forEach { log("日历权限被拒绝：$it") }
                toast("日历权限被拒绝")
                if (PermissionHelper.hasAlwaysDeniedPermissionOrigin(requireActivity(), deniedList)) {
                    JumpPermissionSettingDialog(
                        requireActivity(),
                        message = "日历权限总是被拒绝，请手动授权"
                    ).show()
                }
            }
        }

    /**
     * 请求日历权限
     * READ_CALENDAR: 读取日程安排，用于日程展示、冲突检测等
     * WRITE_CALENDAR: 写入日程，用于创建/编辑日程事件
     * 无特殊版本适配，所有版本行为一致
     */
    private fun requestCalendarPermission() {
        requestCalendarLauncher.launch(
            arrayOf(
                Manifest.permission.READ_CALENDAR,
                Manifest.permission.WRITE_CALENDAR
            )
        )
    }

    // ==================== 传感器权限 ====================

    /** 传感器权限 Launcher（Android 13+ 细粒度拆分） */
    private val requestSensorLauncher =
        registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { permissions ->
            var isAllGranted = true
            val deniedList = mutableListOf<String>()
            permissions.entries.forEach {
                if (!it.value) {
                    isAllGranted = false
                    deniedList.add(it.key)
                }
            }
            if (isAllGranted) {
                log("传感器权限已授予")
                toast("传感器权限已授予")
            } else {
                deniedList.forEach { log("传感器权限被拒绝：$it") }
                toast("传感器权限被拒绝")
                if (PermissionHelper.hasAlwaysDeniedPermissionOrigin(requireActivity(), deniedList)) {
                    JumpPermissionSettingDialog(
                        requireActivity(),
                        message = "传感器权限总是被拒绝，请手动授权"
                    ).show()
                }
            }
        }

    /**
     * 根据当前系统版本请求传感器权限
     *
     * Android 版本适配说明：
     * - Android 12 及以下 (API <= 32):
     *   使用 BODY_SENSORS 权限，涵盖心率、步数等所有身体传感器数据
     *
     * - Android 13+ (API >= 33):
     *   BODY_SENSORS 拆分为前台和后台两个权限：
     *   BODY_SENSORS: 前台访问传感器数据（App 在前台时）
     *   BODY_SENSORS_BACKGROUND: 后台访问传感器数据（App 在后台时）
     *   如需后台持续采集传感器数据（如计步），需同时申请两者
     *
     * - ACTIVITY_RECOGNITION (API 29+):
     *   运动识别权限，检测用户运动状态（步行、骑车、静止等）
     *   独立于 BODY_SENSORS，需单独申请
     */
    private fun requestSensorPermission() {
        val permissions = mutableListOf<String>().apply {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                // Android 13+: 前台传感器 + 后台传感器
                add(Manifest.permission.BODY_SENSORS)
                add(Manifest.permission.BODY_SENSORS_BACKGROUND)
            } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                // Android 10~12: 统一的 BODY_SENSORS
                add(Manifest.permission.BODY_SENSORS)
            }
            // 运动识别权限（Android 10+）
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                add(Manifest.permission.ACTIVITY_RECOGNITION)
            }
        }.toTypedArray()

        if (permissions.isEmpty()) {
            toast("当前系统版本无需申请传感器权限")
            return
        }
        requestSensorLauncher.launch(permissions)
    }

    // ==================== 邻近设备权限 ====================

    /** 邻近设备权限 Launcher */
    private val requestNearbyDevicesLauncher =
        registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { permissions ->
            var isAllGranted = true
            val deniedList = mutableListOf<String>()
            permissions.entries.forEach {
                if (!it.value) {
                    isAllGranted = false
                    deniedList.add(it.key)
                }
            }
            if (isAllGranted) {
                log("邻近设备权限已授予")
                toast("邻近设备权限已授予")
            } else {
                deniedList.forEach { log("邻近设备权限被拒绝：$it") }
                toast("邻近设备权限被拒绝")
                if (PermissionHelper.hasAlwaysDeniedPermissionOrigin(requireActivity(), deniedList)) {
                    JumpPermissionSettingDialog(
                        requireActivity(),
                        message = "邻近设备权限总是被拒绝，请手动授权"
                    ).show()
                }
            }
        }

    /**
     * 根据当前系统版本请求邻近设备权限
     *
     * Android 版本适配说明：
     * - Android 11 及以下 (API <= 30):
     *   无独立的邻近设备权限，通过蓝牙权限（BLUETOOTH + ACCESS_FINE_LOCATION）覆盖
     *
     * - Android 12 (API 31):
     *   引入 NEARBY_DEVICES 权限，统一管理近距离通信：
     *   覆盖蓝牙 BLE、UWB（超宽带）、WiFi-Aware、NFC 等场景
     *   替代了部分 BLUETOOTH_ADMIN + ACCESS_FINE_LOCATION 的用途
     *
     * - Android 13+ (API >= 33):
     *   NEARBY_DEVICES 仍有效，但 WiFi 相关功能拆分为 NEARBY_WIFI_DEVICES
     *   蓝牙相关已由 BLUETOOTH_SCAN / CONNECT / ADVERTISE 替代
     *   此处同时请求两者以覆盖完整场景
     */
    private fun requestNearbyDevicesPermission() {
        val permissions = when {
            Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU -> {
                // Android 13+: NEARBY_WIFI_DEVICES（WiFi 直连/扫描）
                // 注意：蓝牙部分已由 BLUETOOTH_SCAN/CONNECT/ADVERTISE 覆盖
                arrayOf("android.permission.NEARBY_WIFI_DEVICES")
            }
            Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
                // Android 12: 统一的邻近设备权限
                arrayOf("android.permission.NEARBY_DEVICES")
            }
            else -> {
                // Android 11 及以下：无独立邻近设备权限
                toast("当前系统版本使用蓝牙权限替代，请申请蓝牙权限")
                return
            }
        }
        requestNearbyDevicesLauncher.launch(permissions)
    }

}
