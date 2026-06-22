package app.allever.android.sample.audiovideo.core.render

import android.util.Log
import app.allever.android.lib.core.ext.log

/**
 * 渲染器注册中心（单例）
 *
 * 用于管理和创建渲染器实例，支持动态注册新的渲染器。
 * 采用工厂模式 + 注册机制，实现渲染器的可扩展性。
 *
 * ## 设计原则
 * - **开闭原则**：对扩展开放，对修改关闭。新增渲染器只需调用 register()，无需修改现有代码
 * - **单一职责**：只负责渲染器的注册、查询和创建，不涉及播放逻辑
 * - **延迟创建**：通过工厂函数实现按需创建，避免不必要的实例化
 *
 * ## 使用方式
 * ```kotlin
 * // 1. 注册内置渲染器
 * RenderRegistry.register("SurfaceView") { SurfaceViewRender() }
 * RenderRegistry.register("TextureView") { TextureViewRender() }
 *
 * // 2. 注册自定义渲染器（将来）
 * RenderRegistry.register("VulkanRender") { VulkanRender() }
 *
 * // 3. 创建渲染器实例
 * val render = RenderRegistry.create("SurfaceView")
 *
 * // 4. 获取所有可用渲染器列表（用于动态生成 UI）
 * val renders = RenderRegistry.getAvailableRenders()
 *
 * // 5. 检查是否已注册
 * if (RenderRegistry.isRegistered("PlayerView")) {
 *     // ...
 * }
 * ```
 */
object RenderRegistry {

    private val TAG = RenderRegistry::class.java.simpleName

    /** 渲染器工厂映射表 */
    private val factories = mutableMapOf<String, () -> IVideoRender>()

    /**
     * 注册渲染器
     *
     * @param name 唯一标识符（建议使用类名或简短描述）
     * @param factory 创建实例的工厂函数（无参）
     *
     * ## 示例
     * ```kotlin
     * RenderRegistry.register("SurfaceView") { SurfaceViewRender() }
     * ```
     */
    @Synchronized
    fun register(name: String, factory: () -> IVideoRender) {
        if (factories.containsKey(name)) {
            Log.w(TAG, "覆盖已存在的渲染器: $name")
        }
        factories[name] = factory
        Log.d(TAG, "已注册渲染器: $name (总数=${factories.size})")
    }

    /**
     * 创建渲染器实例
     *
     * 每次调用都会创建新实例，不会缓存。
     *
     * @param name 已注册的渲染器名称
     * @return 新的渲染器实例，如果未注册则返回 null
     *
     * ## 示例
     * ```kotlin
     * val render = RenderRegistry.create("SurfaceView")
     * if (render != null) {
     *     player.safeSwitchToRender(render)
     * } else {
     *     Log.e(TAG, "未找到渲染器: SurfaceView")
     * }
     * ```
     */
    fun create(name: String): IVideoRender? {
        val factory = factories[name]
        return if (factory != null) {
            try {
                val instance = factory.invoke()
                Log.d(TAG, "成功创建渲染器: $name (${instance::class.simpleName})")
                instance
            } catch (e: Exception) {
                Log.e(TAG, "创建渲染器失败: $name", e)
                null
            }
        } else {
            Log.w(TAG, "未注册的渲染器: $name (可用: ${factories.keys.joinToString()})")
            null
        }
    }

    /**
     * 获取所有已注册的渲染器名称列表
     *
     * 返回列表的顺序与注册顺序一致。
     * 可用于动态生成 UI 按钮/下拉选择等。
     *
     * @return 已注册的渲染器名称列表
     */
    fun getAvailableRenders(): List<String> {
        return factories.keys.toList()
    }

    /**
     * 获取所有已注册的渲染器显示名称映射
     *
     * @return Map<renderName, displayName>
     */
    fun getRenderNames(): Map<String, String> {
        return factories.map { (name, factory) ->
            // 创建临时实例获取 displayName（注意：这会创建一个临时对象）
            try {
                name to factory.invoke().renderName
            } catch (e: Exception) {
                name to name
            }
        }.toMap()
    }

    /**
     * 检查渲染器是否已注册
     *
     * @param name 渲染器名称
     * @return true 如果已注册
     */
    fun isRegistered(name: String): Boolean {
        return factories.containsKey(name)
    }

    /**
     * 获取已注册的渲染器数量
     */
    fun getRegisteredCount(): Int {
        return factories.size
    }

    /**
     * 注销渲染器
     *
     * @param name 要注销的渲染器名称
     * @return true 如果成功注销
     */
    @Synchronized
    fun unregister(name: String): Boolean {
        return if (factories.remove(name) != null) {
            Log.d(TAG, "已注销渲染器: $name (剩余=${factories.size})")
            true
        } else {
            Log.w(TAG, "尝试注销不存在的渲染器: $name")
            false
        }
    }

    /**
     * 清空所有已注册的渲染器
     *
     * 通常在测试或重置时使用。
     */
    @Synchronized
    fun clear() {
        val count = factories.size
        factories.clear()
        Log.d(TAG, "已清空所有渲染器 (原数量=$count)")
    }

    /**
     * 注册所有内置渲染器
     *
     * 建议在 Application 或初始化时调用一次。
     */
    fun registerBuiltInRenders() {
        if (getRegisteredCount() > 0) {
            Log.d(TAG, "内置渲染器已注册过，跳过重复注册")
            return
        }

        Log.d(TAG, "开始注册内置渲染器...")

        register(SurfaceViewRender.NAME) { SurfaceViewRender() }
        register(TextureViewRender.NAME) { TextureViewRender() }
        register(VideoViewRender.NAME) { VideoViewRender() }
        register(ExoPlayerViewRender.NAME) { ExoPlayerViewRender() }

        Log.d(TAG, "内置渲染器注册完成 (共 ${getRegisteredCount()} 个)")
    }
}
