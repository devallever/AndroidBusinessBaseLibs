package app.allever.android.lib.player.core.engine

import android.util.Log

/**
 * 引擎注册中心（单例）
 *
 * 用于管理和创建播放引擎实例，支持动态注册新的引擎。
 * 采用工厂模式 + 注册机制，实现引擎的可扩展性。
 *
 * ## 设计原则
 * - **开闭原则**：对扩展开放，对修改关闭。新增引擎只需调用 register()，无需修改现有代码
 * - **单一职责**：只负责引擎的注册、查询和创建，不涉及播放逻辑
 * - **延迟创建**：通过工厂函数实现按需创建，避免不必要的实例化
 *
 * ## 使用方式
 * ```kotlin
 * // 1. 注册内置引擎
 * EngineRegistry.register("MediaPlayer") { MediaPlayerEngine() }
 * EngineRegistry.register("Media3") { Media3PlayerEngine() }
 *
 * // 2. 创建引擎实例
 * val engine = EngineRegistry.create("MediaPlayer")
 *
 * // 3. 获取所有可用引擎列表（用于动态生成 UI）
 * val engines = EngineRegistry.getAvailableEngines()
 * ```
 */
object EngineRegistry {

    private const val TAG = "EngineRegistry"

    /** 引擎工厂映射表 */
    private val factories = mutableMapOf<String, () -> IPlayerEngine>()

    /**
     * 注册引擎
     *
     * @param name 唯一标识符（建议使用类名或简短描述）
     * @param factory 创建实例的工厂函数（无参）
     */
    @Synchronized
    fun register(name: String, factory: () -> IPlayerEngine) {
        if (factories.containsKey(name)) {
            Log.w(TAG, "覆盖已存在的引擎: $name")
        }
        factories[name] = factory
        Log.d(TAG, "已注册引擎: $name (总数=${factories.size})")
    }

    /**
     * 创建引擎实例
     *
     * 每次调用都会创建新实例，不会缓存。
     *
     * @param name 已注册的引擎名称
     * @return 新的引擎实例，如果未注册则返回 null
     */
    fun create(name: String): IPlayerEngine? {
        val factory = factories[name]
        return if (factory != null) {
            try {
                val instance = factory.invoke()
                Log.d(TAG, "成功创建引擎: $name (${instance::class.simpleName})")
                instance
            } catch (e: Exception) {
                Log.e(TAG, "创建引擎失败: $name", e)
                null
            }
        } else {
            Log.w(TAG, "未注册的引擎: $name (可用: ${factories.keys.joinToString()})")
            null
        }
    }

    /**
     * 获取所有已注册的引擎名称列表
     *
     * @return 已注册的引擎名称列表
     */
    fun getAvailableEngines(): List<String> {
        return factories.keys.toList()
    }

    /**
     * 检查引擎是否已注册
     *
     * @param name 引擎名称
     * @return true 如果已注册
     */
    fun isRegistered(name: String): Boolean {
        return factories.containsKey(name)
    }

    /**
     * 获取已注册的引擎数量
     */
    fun getRegisteredCount(): Int {
        return factories.size
    }

    /**
     * 注销引擎
     *
     * @param name 要注销的引擎名称
     * @return true 如果成功注销
     */
    @Synchronized
    fun unregister(name: String): Boolean {
        return if (factories.remove(name) != null) {
            Log.d(TAG, "已注销引擎: $name (剩余=${factories.size})")
            true
        } else {
            Log.w(TAG, "尝试注销不存在的引擎: $name")
            false
        }
    }

    /**
     * 清空所有已注册的引擎
     */
    @Synchronized
    fun clear() {
        val count = factories.size
        factories.clear()
        Log.d(TAG, "已清空所有引擎 (原数量=$count)")
    }

    /**
     * 注册所有内置引擎
     *
     * 建议在 Application 或初始化时调用一次。
     */
    fun registerBuiltInEngines() {
        if (getRegisteredCount() > 0) {
            Log.d(TAG, "内置引擎已注册过，跳过重复注册")
            return
        }

        Log.d(TAG, "开始注册内置引擎...")

        register(MediaPlayerEngine.NAME) { MediaPlayerEngine() }

        Log.d(TAG, "内置引擎注册完成 (共 ${getRegisteredCount()} 个)")
    }
}
