package app.allever.android.lib.core.permission

import android.content.Context
import androidx.activity.result.ActivityResultCaller
import app.allever.android.lib.core.permission.internal.DefaultEngine
import app.allever.android.lib.core.permission.internal.RequestBuilder

/**
 * 全局权限引擎门面（单例）
 *
 * 使用方式：
 * ```
 * // Application 中初始化（只做一次，不调用则默认使用 DefaultEngine）
 * class MyApp : Application() {
 *     override fun onCreate() {
 *         super.onCreate()
 *         // 三选一，换引擎只需改这一行
 *         PermissionEngine.init { DefaultEngine() }           // 自研（默认）
 *         // PermissionEngine.init { PermissionXEngine() }     // PermissionX
 *         // PermissionEngine.init { AndPermissionEngine() }  // AndPermission
 *     }
 * }
 *
 * // 业务代码中 — 不关心底层是谁
 * PermissionEngine.with(this)
 *     .permissions(CAMERA, STORAGE)
 *     .onAllGranted { ... }
 *     .request()
 *
 * // 策略模式（自动版本适配）
 * PermissionEngine.with(this)
 *     .strategy(BluetoothPermissionStrategy)
 *     .onAllGranted { ... }
 *     .request()
 * ```
 */
object PermissionCore {

    internal var engine: IPermissionEngine = DefaultEngine()
    private val lock = Any()

    /**
     * 初始化权限引擎
     *
     * 必须在 Application.onCreate 中尽早调用。
     * 不调用则默认使用 [DefaultEngine]。
     *
     * @param engineFactory 引擎工厂，返回一个 [IPermissionEngine] 实例
     */
    fun init(engineFactory: () -> IPermissionEngine) {
        synchronized(lock) {
            engine.destroy()
            engine = engineFactory()
        }
    }

    /** 获取或创建引擎实例 */
    internal fun getOrCreateEngine(): IPermissionEngine {
        return engine
    }

    // ==================== 链式 API 入口 ====================

    /**
     * 获取请求构建器
     *
     * ⚠️ **重要：此方法必须在 Fragment/Activity 的初始化阶段调用**（如属性声明、init 块、
     * onCreate/onAttach 之前），不能在点击回调、异步回调等运行时场景中调用。
     * 因为底层会调用 registerForActivityResult()，该方法要求在 STARTED 之前注册。
     *
     * 正确用法：
     * ```kotlin
     * class MyFragment : Fragment() {
     *     // ✅ 正确：在属性声明时创建，此时 Fragment 尚未完成创建
     *     private val permissionLauncher = PermissionEngine.with(this)
     *
     *     override fun onViewCreated(...) {
     *         btn.setOnClickListener {
     *             // ✅ 正确：使用已创建好的 launcher
     *             permissionLauncher.permissions(CAMERA).onAllGranted { ... }.request()
     *         }
     *     }
     * }
     *
     * // ❌ 错误：在点击回调中创建会导致闪退
     * btn.setOnClickListener {
     *     PermissionEngine.with(this)  // IllegalStateException!
     *         .permissions(CAMERA).request()
     * }
     * ```
     *
     * @param caller Fragment 或 ComponentActivity
     * @return [app.allever.android.lib.core.permission.internal.RequestBuilder] 用于链式配置和发起请求
     */
    fun with(caller: ActivityResultCaller): RequestBuilder {
        return RequestBuilder(getOrCreateEngine(), caller)
    }

    // ==================== 便捷方法 ====================

    /** 检查单个权限是否已授予 */
    fun isGranted(context: Context, permission: String): Boolean =
        getOrCreateEngine().isGranted(context, permission)

    /** 批量检查权限是否全部已授予 */
    fun areAllGranted(context: Context, permissions: Array<out String>): Boolean =
        getOrCreateEngine().areAllGranted(context, permissions)

    /** 获取当前引擎名称（用于调试） */
    val currentEngineName: String
        get() = getOrCreateEngine().name
}
