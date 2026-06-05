package app.allever.android.lib.network.engine.okhttp

import app.allever.android.lib.network.core.engine.EngineConfig
import okhttp3.ConnectionPool
import okhttp3.Protocol
import java.util.concurrent.TimeUnit

/**
 * OkHttp 引擎专属配置
 *
 * 继承 EngineConfig 基础配置，扩展 OkHttp 特有选项：
 *
 * ```kotlin
 * Network.init {
 *     engine("okhttp") {
 *         connectTimeout(10_000)
 *         readTimeout(15_000)
 *         // OkHttp 专属配置
 *         (this as? OkHttpConfig)?.apply {
 *             connectionPool(5, 5, TimeUnit.MINUTES)
 *             protocols(listOf(Protocol.H2_PRIOR_KNOWLEDGE, Protocol.HTTP_1_1))
 *             retryOnConnectionFailure(true)
 *         }
 *     }
 * }
 * ```
 */
open class OkHttpConfig : EngineConfig() {

    /** 连接池（默认 null，使用 OkHttp 默认连接池） */
    var connectionPool: ConnectionPool? = null

    /** 协议列表（默认 null，使用 OkHttp 默认协议） */
    var protocols: List<Protocol>? = null

    /** 连接失败是否自动重试（默认 true） */
    var retryOnConnectionFailure: Boolean = true

    // ==================== DSL 方法 ====================

    /**
     * 配置连接池
     * @param maxIdleConnections 最大空闲连接数
     * @param keepAliveDuration 空闲连接存活时间
     * @param unit 时间单位
     */
    fun connectionPool(maxIdleConnections: Int, keepAliveDuration: Long, unit: TimeUnit) {
        connectionPool = ConnectionPool(maxIdleConnections, keepAliveDuration, unit)
    }

    fun protocols(protocols: List<Protocol>) { this.protocols = protocols }
    fun retryOnConnectionFailure(retry: Boolean) { retryOnConnectionFailure = retry }
}
