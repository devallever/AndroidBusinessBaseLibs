package app.allever.android.lib.network.core.engine

import app.allever.android.lib.core.ext.log
import app.allever.android.lib.network.core.util.NetLogger
import java.util.concurrent.ConcurrentHashMap

/**
 * 引擎注册表 - 注册制管理所有 HTTP 引擎
 *
 * 核心库本身不包含任何引擎实现，各引擎模块通过以下方式自动注册：
 *
 * ```kotlin
 * // 在引擎模块中，利用 companion object init {} 自动注册
 * class OkHttpEngine(config: OkHttpConfig) : HttpEngine {
 *     companion object {
 *         init {
 *             EngineRegistry.register("okhttp") { rawConfig ->
 *                 OkHttpEngine(rawConfig as? OkHttpConfig ?: OkHttpConfig())
 *             }
 *         }
 *     }
 * }
 * ```
 *
 * 使用方式：
 * ```kotlin
 * // 初始化时选择引擎
 * Network.init {
 *     engine("okhttp") { ... }
 * }
 *
 * // 获取引擎实例
 * val engine = EngineRegistry.createDefault(config)
 * ```
 */
object EngineRegistry {

    /** 已注册的引擎工厂: name → (config) → Engine */
    private val factories = ConcurrentHashMap<String, (EngineConfig) -> HttpEngine>()

    /** 当前默认使用的引擎名称 */
    @Volatile
    private var defaultName: String? = null

    /**
     * 注册引擎
     * @param name 引擎唯一标识（建议小写下划线，如 "okhttp"、"url_connection"）
     * @param factory 工厂函数，接收通用配置返回引擎实例
     * @throws IllegalStateException 如果同名引擎已存在
     */
    fun register(name: String, factory: (EngineConfig) -> HttpEngine) {
        if (factories.putIfAbsent(name, factory) != null) {
            log("引擎 '$name' 已注册，不可重复注册")
        } else {
            factories[name] = factory
            log("引擎注册成功: $name")
        }
    }

    /**
     * 设置默认引擎名称（Network.init 时调用）
     */
    fun setDefault(name: String) {
        if (!factories.containsKey(name)) {
            throw IllegalStateException("引擎 '$name' 未注册，请先确保已引入对应引擎模块")
        }
        defaultName = name
        NetLogger.log("默认引擎设置为: $name (已注册: ${factories.keys.joinToString()})")
    }

    /**
     * 创建指定名称的引擎实例
     * @param name 引擎名称
     * @param config 引擎配置
     * @return HttpEngine 实例
     * @throws IllegalStateException 引擎未注册
     */
    fun create(name: String, config: EngineConfig): HttpEngine {
        val factory = factories[name]
            ?: throw IllegalStateException(
                "引擎 '$name' 未注册。已注册引擎: ${factories.keys.joinToString()}"
            )
        return factory(config)
    }

    /**
     * 创建默认引擎实例
     */
    fun createDefault(config: EngineConfig): HttpEngine {
        val name = defaultName
            ?: throw IllegalStateException(
                "未设置默认引擎。请在 Network.init 中通过 engine(\"name\") 指定"
            )
        return create(name, config)
    }

    /** 获取当前默认引擎名称 */
    fun getDefaultName(): String? = defaultName

    /** 获取已注册的所有引擎名称（调试用） */
    fun registeredEngines(): Set<String> = factories.keys.toSet()

    /** 清除所有注册（测试用） */
    internal fun clear() {
        factories.clear()
        defaultName = null
    }
}
