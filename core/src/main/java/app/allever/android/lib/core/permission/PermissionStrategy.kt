package app.allever.android.lib.core.permission

import android.Manifest
import android.content.Context
import android.content.Intent
import android.os.Build
import android.provider.Settings

/**
 * 权限策略接口
 *
 * 每种权限类型实现此接口，负责根据当前系统版本决定请求哪些权限。
 * 配合 [PermissionLauncher.requestByStrategy] 使用，实现版本适配逻辑与业务代码解耦。
 */
interface PermissionStrategy {
    /** 策略名称，用于日志标识 */
    val name: String

    /**
     * 根据当前系统版本返回需要请求的权限数组
     * 返回空数组表示当前版本无需申请任何权限
     */
    fun getPermissions(): Array<String>

    /**
     * 是否跳过本次请求（例如提示用户后直接返回）
     * 默认 false
     */
    fun shouldSkipRequest(): Boolean = false
}

// ==================== 内置策略实现 ====================

/** 相机权限策略 */
object CameraPermissionStrategy : PermissionStrategy {
    override val name = "Camera"
    override fun getPermissions() = arrayOf(Manifest.permission.CAMERA)
}

/** 录音权限策略 */
object RecordAudioPermissionStrategy : PermissionStrategy {
    override val name = "RecordAudio"
    override fun getPermissions() = arrayOf(Manifest.permission.RECORD_AUDIO)
}

/**
 * 媒体资源权限策略
 * - Android 13+: READ_MEDIA_IMAGES + READ_MEDIA_VIDEO + READ_MEDIA_AUDIO
 * - Android 12 及以下: READ_EXTERNAL_STORAGE
 */
object MediaPermissionStrategy : PermissionStrategy {
    override val name = "Media"

    override fun getPermissions() = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
        arrayOf(
            Manifest.permission.READ_MEDIA_IMAGES,
            Manifest.permission.READ_MEDIA_VIDEO,
            Manifest.permission.READ_MEDIA_AUDIO
        )
    } else {
        arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE)
    }
}

/**
 * 存储权限策略
 * - Android 13+: 细粒度媒体权限（非媒体文件无需权限）
 * - Android 10~12: READ_EXTERNAL_STORAGE（分区存储）
 * - Android 9 及以下: READ_EXTERNAL_STORAGE + WRITE_EXTERNAL_STORAGE
 */
object StoragePermissionStrategy : PermissionStrategy {
    override val name = "Storage"

    override fun getPermissions() = when {
        Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU -> arrayOf(
            Manifest.permission.READ_MEDIA_IMAGES,
            Manifest.permission.READ_MEDIA_VIDEO,
            Manifest.permission.READ_MEDIA_AUDIO
        )
        Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q -> arrayOf(
            Manifest.permission.READ_EXTERNAL_STORAGE
        )
        else -> arrayOf(
            Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.WRITE_EXTERNAL_STORAGE
        )
    }
}

/**
 * 通知权限策略
 * - Android 13+: POST_NOTIFICATIONS（运行时权限）
 * - Android 8.0~12: 跳转通知设置页（非运行时权限，此处返回空数组由调用方处理）
 * - Android 7.1 及以下: 无需权限
 */
object NotificationPermissionStrategy : PermissionStrategy {
    override val name = "Notification"

    override fun getPermissions(): Array<String> = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
        arrayOf(Manifest.permission.POST_NOTIFICATIONS)
    } else {
        emptyArray()
    }

    /** Android 12 及以下需要通过 Settings 页面处理，返回 true 让调用方跳过运行时请求 */
    override fun shouldSkipRequest(): Boolean =
        Build.VERSION.SDK_INT in Build.VERSION_CODES.O until Build.VERSION_CODES.TIRAMISU

    /**
     * 获取通知设置页 Intent（Android 8.0~12 调用方使用）
     */
    fun getNotificationSettingsIntent(context: Context) =
        Intent(Settings.ACTION_APP_NOTIFICATION_SETTINGS).apply {
            putExtra(Settings.EXTRA_APP_PACKAGE, context.packageName)
        }
}

/**
 * 前台位置权限策略
 * - 所有版本一致：ACCESS_FINE_LOCATION + ACCESS_COARSE_LOCATION
 */
object ForegroundLocationPermissionStrategy : PermissionStrategy {
    override val name = "ForegroundLocation"

    override fun getPermissions() = arrayOf(
        Manifest.permission.ACCESS_FINE_LOCATION,
        Manifest.permission.ACCESS_COARSE_LOCATION
    )
}

/**
 * 后台位置权限策略
 * - Android 10+: ACCESS_BACKGROUND_LOCATION（必须单独请求）
 * - Android 9 及以下: 无独立后台位置权限
 */
object BackgroundLocationPermissionStrategy : PermissionStrategy {
    override val name = "BackgroundLocation"

    override fun getPermissions(): Array<String> = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
        arrayOf(Manifest.permission.ACCESS_BACKGROUND_LOCATION)
    } else {
        emptyArray()
    }

    override fun shouldSkipRequest() = Build.VERSION.SDK_INT < Build.VERSION_CODES.Q
}

/**
 * 蓝牙权限策略
 * - Android 12+: BLUETOOTH_SCAN + BLUETOOTH_CONNECT + BLUETOOTH_ADVERTISE
 * - Android 11 及以下: BLUETOOTH + BLUETOOTH_ADMIN + ACCESS_FINE_LOCATION
 */
object BluetoothPermissionStrategy : PermissionStrategy {
    override val name = "Bluetooth"

    override fun getPermissions() = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
        arrayOf(
            Manifest.permission.BLUETOOTH_SCAN,
            Manifest.permission.BLUETOOTH_CONNECT,
            Manifest.permission.BLUETOOTH_ADVERTISE
        )
    } else {
        arrayOf(
            Manifest.permission.BLUETOOTH,
            Manifest.permission.BLUETOOTH_ADMIN,
            Manifest.permission.ACCESS_FINE_LOCATION
        )
    }
}

/**
 * 邻近设备权限策略
 * - Android 13+: NEARBY_WIFI_DEVICES
 * - Android 12: NEARBY_DEVICES
 * - Android 11 及以下: 无独立邻近设备权限
 */
object NearbyDevicesPermissionStrategy : PermissionStrategy {
    override val name = "NearbyDevices"

    @Suppress("NewApi")
    override fun getPermissions(): Array<String> = when {
        Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU -> {
            arrayOf("android.permission.NEARBY_WIFI_DEVICES")
        }
        Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            arrayOf("android.permission.NEARBY_DEVICES")
        }
        else -> emptyArray()
    }

    override fun shouldSkipRequest() = Build.VERSION.SDK_INT < Build.VERSION_CODES.S
}

/** 联系人权限策略 */
object ContactsPermissionStrategy : PermissionStrategy {
    override val name = "Contacts"
    override fun getPermissions() = arrayOf(
        Manifest.permission.READ_CONTACTS,
        Manifest.permission.WRITE_CONTACTS
    )
}

/**
 * 电话权限策略
 * - 基础: READ_PHONE_STATE + CALL_PHONE
 * - Android 8.0+: 额外添加 ANSWER_PHONE_CALLS + READ_PHONE_NUMBERS
 */
object PhonePermissionStrategy : PermissionStrategy {
    override val name = "Phone"

    override fun getPermissions() = buildList {
        add(Manifest.permission.READ_PHONE_STATE)
        add(Manifest.permission.CALL_PHONE)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            add(Manifest.permission.ANSWER_PHONE_CALLS)
            add(Manifest.permission.READ_PHONE_NUMBERS)
        }
    }.toTypedArray()
}

/** 短信权限策略 */
object SmsPermissionStrategy : PermissionStrategy {
    override val name = "SMS"
    override fun getPermissions() = arrayOf(
        Manifest.permission.SEND_SMS,
        Manifest.permission.RECEIVE_SMS,
        Manifest.permission.READ_SMS
    )
}

/** 日历权限策略 */
object CalendarPermissionStrategy : PermissionStrategy {
    override val name = "Calendar"
    override fun getPermissions() = arrayOf(
        Manifest.permission.READ_CALENDAR,
        Manifest.permission.WRITE_CALENDAR
    )
}

/**
 * 传感器权限策略
 * - Android 13+: BODY_SENSORS + BODY_SENSORS_BACKGROUND
 * - Android 10~12: BODY_SENSORS (+ ACTIVITY_RECOGNITION)
 * - Android 9 及以下: 仅 ACTIVITY_RECOGNITION（如有）
 */
object SensorPermissionStrategy : PermissionStrategy {
    override val name = "Sensor"

    override fun getPermissions() = buildList {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            add(Manifest.permission.BODY_SENSORS)
            add(Manifest.permission.BODY_SENSORS_BACKGROUND)
        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            add(Manifest.permission.BODY_SENSORS)
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            add(Manifest.permission.ACTIVITY_RECOGNITION)
        }
    }.toTypedArray()

    override fun shouldSkipRequest() = getPermissions().isEmpty()
}
