package app.allever.android.lib.core.function.permission

import android.app.Activity
import android.content.Context
import androidx.activity.result.ActivityResultCaller
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.Fragment
import app.allever.android.lib.core.ext.log



/**
 * 简化的回调接口，仅处理授予和拒绝两种情况
 * （always denied 时自动使用默认行为：弹出 JumpPermissionSettingDialog）
 */
typealias SimplePermissionCallback = (allGranted: Boolean, deniedList: List<String>) -> Unit

/**
 * 基于 ActivityResultContract 的通用权限请求组件
 *
 * 支持 [Fragment] 和 [Activity] 中使用：
 * ```kotlin
 * // Fragment 中
 * private val permissionLauncher = PermissionLauncherComponent(this)
 *
 * // Activity 中（this 为 AppCompatActivity / ComponentActivity）
 * private val permissionLauncher = PermissionLauncherComponent(this)
 *
 * // 请求权限
 * permissionLauncher.request(
 *     permissions = arrayOf(Manifest.permission.CAMERA),
 *     onAllGranted = { toast("已授权") },
 *     onDenied = { toast("被拒绝") },
 * )
 *
 * // 或使用策略模式（自动版本适配）
 * permissionLauncher.requestByStrategy(
 *     strategy = CameraPermissionStrategy,
 *     onAllGranted = { toast("相机权限已授予") },
 * )
 * ```
 */
class PermissionLauncherComponent(owner: ActivityResultCaller) {
    private val mOwner = owner
    fun requireContext(): Context {
        return when (mOwner) {
            is Fragment -> mOwner.requireContext()
            is Activity -> mOwner
            else -> throw IllegalArgumentException("owner must be Fragment or Activity")
        }
    }

    private fun requireActivity(): Activity {
        return when (mOwner) {
            is Fragment -> mOwner.requireActivity()
            is Activity -> mOwner
            else -> throw IllegalArgumentException("owner must be Fragment or Activity")
        }
    }


    private var currentCallback: PermissionResultCallback? = null

    /** 多权限 Launcher */
    private val multiPermissionLauncher =
        owner.registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { permissions ->
            handleResult(permissions)
        }

    /** 单权限 Launcher */
    private val singlePermissionLauncher =
        owner.registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted ->
            // 转换为统一格式
            val permissions = if (isGranted) {
                mapOf(lastRequestedSinglePermission to true)
            } else {
                mapOf(lastRequestedSinglePermission to false)
            }
            handleResult(permissions)
        }

    /** 记录最近一次单权限请求的权限名（用于回调中识别被拒绝的权限） */
    private var lastRequestedSinglePermission: String = ""

    /**
     * 请求权限（通用方法）
     *
     * @param permissions 要请求的权限数组
     * @param callback 结果回调
     */
    fun request(permissions: Array<String>, callback: PermissionResultCallback) {
        currentCallback = callback
        when (permissions.size) {
            0 -> {
                log("没有需要请求的权限")
                callback.onAllGranted()
            }
            1 -> {
                lastRequestedSinglePermission = permissions[0]
                singlePermissionLauncher.launch(permissions[0])
            }
            else -> {
                multiPermissionLauncher.launch(permissions)
            }
        }
    }

    /**
     * 请求权限（简化回调）
     */
    fun request(
        permissions: Array<String>,
        onAllGranted: () -> Unit,
        onDenied: (deniedList: List<String>) -> Unit = {},
        onAlwaysDenied: ((deniedList: List<String>, context: Context) -> Unit)? = null,
    ) {
        request(permissions, object : PermissionResultCallback {
            override fun onAllGranted() = onAllGranted()
            override fun onDenied(deniedPermissions: List<String>) = onDenied(deniedPermissions)
            override fun onAlwaysDenied(deniedPermissions: List<String>, context: Context) {
                onAlwaysDenied?.invoke(deniedPermissions, context)
                    ?: super.onAlwaysDenied(deniedPermissions, context)
            }
        })
    }

    /**
     * 通过策略请求权限（自动版本适配）
     *
     * @param strategy 权限策略，决定当前系统版本应请求哪些权限
     * @param callback 结果回调
     */
    fun requestByStrategy(strategy: PermissionStrategy, callback: PermissionResultCallback) {
        val permissions = strategy.getPermissions()
        if (permissions.isEmpty() && !strategy.shouldSkipRequest()) {
            log("[${strategy.name}] 当前版本无需申请权限")
            callback.onAllGranted()
            return
        }
        if (strategy.shouldSkipRequest()) {
            log("[${strategy.name}] 策略要求跳过请求")
            return
        }
        request(permissions, callback)
    }

    /**
     * 通过策略请求权限（简化回调）
     */
    fun requestByStrategy(
        strategy: PermissionStrategy,
        onAllGranted: () -> Unit,
        onDenied: (deniedList: List<String>) -> Unit = {},
        onAlwaysDenied: ((deniedList: List<String>, context: Context) -> Unit)? = null,
    ) {
        requestByStrategy(strategy, object : PermissionResultCallback {
            override fun onAllGranted() = onAllGranted()
            override fun onDenied(deniedPermissions: List<String>) = onDenied(deniedPermissions)
            override fun onAlwaysDenied(deniedPermissions: List<String>, context: Context) {
                onAlwaysDenied?.invoke(deniedPermissions, context)
                    ?: super.onAlwaysDenied(deniedPermissions, context)
            }
        })
    }

    /**
     * 处理权限请求结果
     */
    private fun handleResult(permissions: Map<String, Boolean>) {
        val grantedList = mutableListOf<String>()
        val deniedList = mutableListOf<String>()

        permissions.entries.forEach { (permission, granted) ->
            if (granted) {
                grantedList.add(permission)
            } else {
                deniedList.add(permission)
            }
        }

        val callback = currentCallback ?: return
        val ctx = requireContext()

        when {
            deniedList.isEmpty() -> {
                log("所有权限已授予: $grantedList")
                callback.onAllGranted()
            }
            isAlwaysDenied(requireActivity(), deniedList) -> {
                log("权限总是被拒绝: $deniedList")
                callback.onAlwaysDenied(deniedList, ctx)
            }
            else -> {
                log("权限被拒绝: $deniedList")
                callback.onDenied(deniedList)
            }
        }
    }

    /**
     * 判断是否为"总是拒绝"
     */
    private fun isAlwaysDenied(
        activity: Activity?,
        deniedPermissions: List<String>
    ): Boolean {
        return try {
            PermissionHelper.hasAlwaysDeniedPermissionOrigin(activity, deniedPermissions)
        } catch (e: Exception) {
            log("判断 always denied 异常: ${e.message}", e.message)
            false
        }
    }
}
