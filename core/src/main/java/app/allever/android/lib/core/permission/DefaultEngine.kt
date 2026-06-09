package app.allever.android.lib.core.permission

import android.app.Activity
import android.content.Context
import android.content.pm.PackageManager
import androidx.activity.result.ActivityResultCaller
import androidx.core.content.ContextCompat
import java.lang.ref.WeakReference

/**
 * 默认权限引擎实现
 *
 * 基于自研的 [PermissionLauncher] 组件，使用 ActivityResultContract 发起权限请求。
 * 无需任何第三方依赖，作为 [PermissionEngine] 的默认引擎。
 *
 * 特性：
 * - 支持 Fragment 和 Activity
 * - 自动处理 always denied 场景（弹出 JumpPermissionSettingDialog）
 * - 支持策略模式自动版本适配
 * - 内部缓存 Launcher 实例，避免重复注册
 */
class DefaultEngine : IPermissionEngine {

    override val name: String = "Default"

    /** 缓存每个 caller 对应的 Launcher 实例 */
    private val launcherCache = mutableMapOf<Int, WeakReference<DefaultLauncher>>()

    private var nextId = 0

    @Synchronized
    private fun generateId(): Int = ++nextId

    @Synchronized
    override fun createLauncher(caller: ActivityResultCaller): IPermissionLauncher {
        // 使用 caller 的 hashCode 作为缓存 key，同一 caller 复用 Launcher
        val key = System.identityHashCode(caller)
        var ref = launcherCache[key]
        var launcher = ref?.get()
        if (launcher == null) {
            launcher = DefaultLauncher(caller)
            launcherCache[key] = WeakReference(launcher)
        }
        return launcher
    }

    override fun isGranted(context: Context, permission: String): Boolean {
        return ContextCompat.checkSelfPermission(context, permission) ==
                PackageManager.PERMISSION_GRANTED
    }

    override fun areAllGranted(context: Context, permissions: Array<out String>): Boolean {
        return permissions.all { isGranted(context, it) }
    }

    override fun destroy() {
        launcherCache.clear()
    }

    /**
     * 默认 Launcher 实现 — 内部委托给 [PermissionLauncher]
     *
     * 注意：[PermissionLauncher] 必须在 Fragment/Activity 的初始化阶段创建
     * （即 onCreate/onAttach 之前），因为其内部会调用 registerForActivityResult()。
     * 因此这里不能使用懒加载，必须在构造时立即创建。
     */
    internal class DefaultLauncher(
        private val caller: ActivityResultCaller,
    ) : IPermissionLauncher {

        /** 真正的 Launcher 实例（构造时立即创建，确保 registerForActivityResult 在生命周期之前调用） */
        private val realLauncher = PermissionLauncher(caller)

        override fun requireContext(): Context = realLauncher.requireContext()

        override fun request(permissions: Array<out String>, callback: PermissionResultCallback) {
            realLauncher.request(permissions, callback)
        }

        override fun requestByStrategy(strategy: PermissionStrategy, callback: PermissionResultCallback) {
            realLauncher.requestByStrategy(strategy, callback)
        }
    }
}
