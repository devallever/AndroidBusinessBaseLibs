package app.allever.android.lib.permission.engine.permissionx

import android.app.Activity
import android.content.Context
import android.content.pm.PackageManager
import androidx.activity.result.ActivityResultCaller
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import app.allever.android.lib.core.permission.IPermissionEngine
import app.allever.android.lib.core.permission.IPermissionLauncher
import app.allever.android.lib.core.permission.PermissionResultCallback
import app.allever.android.lib.core.permission.PermissionStrategy
import com.permissionx.guolindev.PermissionX

/**
 * PermissionX 引擎实现
 *
 * 基于 [com.guolindev.permissionx.PermissionX] 库，
 * 将 PermissionX 的 API 适配到 [IPermissionEngine] 接口。
 *
 * 使用方式：
 * ```
 * // Application 中初始化
 * PermissionEngine.init { PermissionXEngine() }
 *
 * // 业务代码不变
 * PermissionEngine.with(this)
 *     .permissions(CAMERA)
 *     .onAllGranted { ... }
 *     .request()
 * ```
 */
class PermissionXEngine : IPermissionEngine {

    override val name: String = "PermissionX"

    @Synchronized
    override fun createLauncher(caller: ActivityResultCaller): IPermissionLauncher {
        return PermissionXLauncher(caller)
    }

    override fun isGranted(context: Context, permission: String): Boolean {
        return ContextCompat.checkSelfPermission(context, permission) ==
                PackageManager.PERMISSION_GRANTED
    }

    override fun areAllGranted(context: Context, permissions: Array<out String>): Boolean {
        return permissions.all { isGranted(context, it) }
    }

    override fun destroy() {
        // PermissionX 无需显式销毁
    }

    /**
     * PermissionX Launcher 实现 — 内部委托给 PermissionX.init()
     *
     * 注意：PermissionX 内部通过隐藏 Fragment 管理 ActivityResultContract 注册，
     * 因此不需要像 DefaultEngine 那样缓存 Launcher 实例。
     */
    internal class PermissionXLauncher(
        private val caller: ActivityResultCaller,
    ) : IPermissionLauncher {

        override fun requireContext(): Context = when (caller) {
            is Fragment -> caller.requireContext()
            is Activity -> caller
            else -> throw IllegalArgumentException("caller must be Fragment or Activity")
        }

        override fun request(permissions: Array<out String>, callback: PermissionResultCallback) {
            if (permissions.isEmpty()) {
                callback.onAllGranted()
                return
            }

            val permArray = permissions.toList().toTypedArray()

            // 将 caller 转换为 PermissionX 支持的类型并请求权限
            when (caller) {
                is Fragment -> {
                    com.permissionx.guolindev.PermissionX.init(caller)
                        .permissions(*permArray)
                        .request { allGranted: Boolean, grantedList: List<String?>, deniedList: List<String?> ->
                            when {
                                allGranted -> callback.onAllGranted()
                                else -> callback.onDenied(deniedList.filterNotNull())
                            }
                        }
                }
                is FragmentActivity -> {
                    com.permissionx.guolindev.PermissionX.init(caller)
                        .permissions(*permArray)
                        .request { allGranted: Boolean, grantedList: List<String?>, deniedList: List<String?> ->
                            when {
                                allGranted -> callback.onAllGranted()
                                else -> callback.onDenied(deniedList.filterNotNull())
                            }
                        }
                }
                else -> throw IllegalArgumentException("不支持的 caller 类型")
            }
        }

        override fun requestByStrategy(strategy: PermissionStrategy, callback: PermissionResultCallback) {
            val permissions = strategy.getPermissions()
            if (permissions.isEmpty() && !strategy.shouldSkipRequest()) {
                callback.onAllGranted()
                return
            }
            if (strategy.shouldSkipRequest()) {
                return
            }
            request(permissions, callback)
        }
    }
}
