package app.allever.android.lib.core.permission

import android.content.Context
import androidx.activity.result.ActivityResultCaller

/**
 * 权限请求引擎接口
 *
 * 所有权限实现（Default、PermissionX、AndPermission 等）均需实现此接口。
 * 通过 [PermissionCore.init] 可无缝切换底层权限方案，业务代码无需改动。
 */
interface IPermissionEngine {

    /** 引擎名称，用于日志和调试 */
    val name: String

    /**
     * 为指定的 ActivityResultCaller 创建一个 Launcher 实例
     *
     * 内部应缓存 caller 对应的 Launcher（如 PermissionLauncher），
     * 避免重复调用 registerForActivityResult。
     *
     * @param caller Fragment 或 ComponentActivity
     * @return 可用于发起权限请求的 Launcher
     */
    fun createLauncher(caller: ActivityResultCaller): IPermissionLauncher

    /**
     * 检查单个权限是否已授予
     */
    fun isGranted(context: Context, permission: String): Boolean

    /**
     * 批量检查权限是否全部已授予
     */
    fun areAllGranted(context: Context, permissions: Array<out String>): Boolean

    /**
     * 销毁引擎，释放资源
     */
    fun destroy()
}

/**
 * 权限请求 Launcher 接口
 *
 * 由 [IPermissionEngine.createLauncher] 返回，
 * 绑定到特定的 Fragment 或 Activity，用于实际发起权限请求。
 */
interface IPermissionLauncher {

    /** 获取 Context */
    fun requireContext(): Context

    /**
     * 请求权限
     *
     * @param permissions 要请求的权限数组
     * @param callback 结果回调
     */
    fun request(permissions: Array<out String>, callback: PermissionResultCallback)

    /**
     * 通过策略请求权限（自动版本适配）
     *
     * @param strategy 权限策略
     * @param callback 结果回调
     */
    fun requestByStrategy(strategy: PermissionStrategy, callback: PermissionResultCallback)
}
