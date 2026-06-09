package app.allever.android.lib.core.permission

import android.app.Activity
import android.content.Context
import androidx.fragment.app.Fragment

/**
 * 权限请求链式构建器
 *
 * 通过 [PermissionEngine.with] 获取实例，链式配置后调用 [request] 发起请求。
 * 底层实现由注入的 [IPermissionEngine] 决定，切换引擎不影响业务代码。
 *
 * 使用方式：
 * ```kotlin
 * PermissionEngine.with(this)
 *     .permissions(CAMERA, RECORD_AUDIO)
 *     .explainReason("需要权限", "用于拍照和录音") { scope ->
 *         WhyRequestPermissionDialog(scope.context(), "需要权限", "说明") {
 *             scope.proceed()
 *         }.show()
 *     }
 *     .forwardToSettings("权限被拒绝", "请前往设置开启") { context ->
 *         JumpPermissionSettingDialog(context, message = "请手动授权").show()
 *     }
 *     .onAllGranted { startCamera() }
 *     .onDenied { toast("部分权限未授予") }
 *     .request()
 *
 * // 使用策略模式（自动版本适配）
 * PermissionEngine.with(this)
 *     .strategy(BluetoothPermissionStrategy)
 *     .onAllGranted { startScan() }
 *     .request()
 * ```
 */
class RequestBuilder internal constructor(
    private val engine: IPermissionEngine,
    private val caller: androidx.activity.result.ActivityResultCaller,
) {

    /** 在构造时立即创建 Launcher（确保 registerForActivityResult 在生命周期之前调用） */
    private val launcher: IPermissionLauncher = engine.createLauncher(caller)

    /** 要请求的权限数组 */
    private var permissions: Array<out String> = emptyArray()

    /** 权限策略（与 permissions 二选一） */
    private var strategy: PermissionStrategy? = null

    /** 请求前说明理由配置 */
    private var explainReasonConfig: ExplainReasonConfig? = null

    /** always denied 后跳转设置配置 */
    private var forwardSettingsConfig: ForwardSettingsConfig? = null

    /** 全部授予回调 */
    private var onAllGrantedCallback: (() -> Unit)? = null

    /** 部分拒绝回调 */
    private var onDeniedCallback: ((deniedList: List<String>) -> Unit)? = null

    /**
     * 设置要请求的权限
     */
    fun permissions(vararg perms: String): RequestBuilder {
        this.permissions = perms
        forwardSettingsConfig = null
        explainReasonConfig = null
        onDeniedCallback = null
        onAllGrantedCallback = null
        strategy = null
        return this
    }

    /**
     * 使用策略模式请求权限（自动版本适配）
     * 与 [permissions] 二选一，strategy 优先级更高
     */
    fun strategy(s: PermissionStrategy): RequestBuilder {
        this.strategy = s
        return this
    }

    /**
     * 配置"请求前说明理由"弹窗
     *
     * 当用户之前拒绝过权限时，在再次请求前先弹出说明弹窗，
     * 用户确认后才真正发起权限请求。
     *
     * @param title 弹窗标题
     * @param message 弹窗内容
     * @param callback 回调，通过 [ExplainReasonScope.proceed] 继续请求，
     *                  或 [ExplainReasonScope.cancel] 取消请求
     */
    fun explainReason(
        callback: (ExplainReasonScope) -> Unit
    ): RequestBuilder {
        this.explainReasonConfig = ExplainReasonConfig( callback)
        return this
    }

    /**
     * 配置"总是被拒绝"后的处理逻辑
     *
     * 当用户选择了"不再询问"时触发，默认会弹出 JumpPermissionSettingDialog。
     * 可在此自定义弹窗或直接跳转设置页。
     *
     * @param title 自定义弹窗标题（可选）
     * @param message 自定义弹窗消息或提示信息
     * @param callback 回调，参数为 Context
     */
    fun forwardToSettings(
        callback: (Context) -> Unit
    ): RequestBuilder {
        this.forwardSettingsConfig = ForwardSettingsConfig(callback)
        return this
    }

    /**
     * 所有权限均已授予时的回调
     */
    fun onAllGranted(callback: () -> Unit): RequestBuilder {
        this.onAllGrantedCallback = callback
        return this
    }

    /**
     * 部分权限被拒绝时的回调（非"总是拒绝"）
     */
    fun onDenied(callback: (deniedList: List<String>) -> Unit): RequestBuilder {
        this.onDeniedCallback = callback
        return this
    }

    /**
     * 发起权限请求
     *
     * 执行流程：
     * 1. 如果配置了 explainReason 且存在已拒绝的权限 → 先弹说明弹窗 → 用户确认后继续
     * 2. 调用引擎发起实际的权限请求
     * 3. 根据结果分发到 onAllGranted / onDenied / onAlwaysDenied
     */
    fun request() {
        // 使用构造时已创建好的 launcher（不能在运行时新建，否则 registerForActivityResult 会闪退）

        // 构建回调
        val resultCallback = object : PermissionResultCallback {
            override fun onAllGranted() {
                onAllGrantedCallback?.invoke()
            }

            override fun onDenied(deniedPermissions: List<String>) {
                onDeniedCallback?.invoke(deniedPermissions)
            }

            override fun onAlwaysDenied(deniedPermissions: List<String>, context: Context) {
                if (forwardSettingsConfig != null) {
                    forwardSettingsConfig!!.callback.invoke(context)
                } else {
                    // 未配置 forwardToSettings 时静默，不弹默认弹窗, 回调失败
                    onDeniedCallback?.invoke(deniedPermissions)
                }
            }

            override fun getCustomDialog(context: Context): android.app.Dialog? = null

            override fun needShowJumpSettingDialog(): Boolean = false
        }

        // 实际发起请求的方法
        fun doRequest() {
            val actualStrategy = strategy
            if (actualStrategy != null) {
                launcher.requestByStrategy(actualStrategy, resultCallback)
            } else {
                launcher.request(permissions, resultCallback)
            }
        }

        // ===== explainReason 前置检查 =====
        val explainConfig = explainReasonConfig
        if (explainConfig != null) {
            val ctx = when (caller) {
                is Fragment -> caller.context
                is Activity -> caller
                else -> null
            }
            if (ctx != null) {
                val permsToCheck = if (strategy != null) strategy!!.getPermissions() else permissions
                // 检查是否有未授予的权限（之前可能被拒绝过）
                val notGranted = permsToCheck.filter { !PermissionEngine.isGranted(ctx, it) }
                if (notGranted.isNotEmpty()) {
                    val scope = object : ExplainReasonScope {
                        override fun context(): Context = ctx
                        override fun proceed() { doRequest() }
                        override fun cancel() {}
                    }
                    explainConfig.callback.invoke(scope)
                    return
                }
            }
        }

        doRequest()
    }

    /**
     * 发起权限请求（简化回调版）
     *
     * @param callback (allGranted, deniedList) → Unit
     */
    fun request(callback: (allGranted: Boolean, deniedList: List<String>) -> Unit) {
        onAllGranted { callback(true, emptyList()) }
        onDenied { denied -> callback(false, denied) }
        request()
    }

    // ==================== 内部数据类 ====================

    data class ExplainReasonConfig(
        val callback: (ExplainReasonScope) -> Unit
    )

    data class ForwardSettingsConfig(
        val callback: (Context) -> Unit
    )
}

/**
 * explainReason 回调的作用域
 *
 * 提供对当前 Context 的访问，以及控制是否继续请求的方法。
 */
interface ExplainReasonScope {

    /** 获取当前 Context */
    fun context(): android.content.Context

    /** 继续发起权限请求 */
    fun proceed()

    /** 取消本次请求 */
    fun cancel()
}
