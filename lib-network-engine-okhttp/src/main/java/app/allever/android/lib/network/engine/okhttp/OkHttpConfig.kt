package app.allever.android.lib.network.engine.okhttp

import app.allever.android.lib.network.core.engine.EngineConfig
import okhttp3.Authenticator
import okhttp3.ConnectionPool
import okhttp3.Interceptor
import okhttp3.Protocol
import java.io.File
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
 *         // OkHttp 专属配置（无需强转）
 *         addInterceptor(HttpLoggingInterceptor().setLevel(BODY))
 *         addNetworkInterceptor(StaleIfErrorInterceptor())
 *         authenticator(TokenAuthenticator())
 *         cache(cacheDir, 10 * 1024 * 1024)
 *         connectionPool(5, 5, TimeUnit.MINUTES)
 *     }
 * }
 * ```
 */
open class OkHttpConfig : EngineConfig() {

    /** 应用层拦截器（在请求发出前执行） */
    val interceptors: MutableList<okhttp3.Interceptor> = mutableListOf()

    /** 网络层拦截器（在网络层之后执行，可处理 gzip 等） */
    val networkInterceptors: MutableList<okhttp3.Interceptor> = mutableListOf()

    /** 认证器（401 自动刷新 Token） */
    var authenticator: Authenticator? = null

    /** 缓存目录 */
    var cacheDir: File? = null

    /** 缓存大小（字节） */
    var cacheSize: Long = 0L

    /** 连接池（默认 null，使用 OkHttp 默认连接池） */
    var connectionPool: ConnectionPool? = null

    /** 协议列表（默认 null，使用 OkHttp 默认协议） */
    var protocols: List<Protocol>? = null

    /** 连接失败是否自动重试（默认 true） */
    var retryOnConnectionFailure: Boolean = true

    // ==================== DSL 方法 ====================

    /**
     * 添加应用层拦截器
     *
     * 执行时机：在 NetCore 的 NetInterceptor 链之后、网络请求之前。
     * 典型用途：日志打印、Header 补充等。
     */
    fun addInterceptor(interceptor: Any) {
        if (interceptor is Interceptor) {
            interceptors.add(interceptor)
        }
    }

    /**
     * 添加网络层拦截器
     *
     * 执行时机：在 TCP 连接建立后、服务器响应返回前。
     * 典型用途：gzip 解压处理、缓存策略控制。
     */
    fun addNetworkInterceptor(interceptor: Any) {
        if (interceptor is Interceptor) {
            networkInterceptors.add(interceptor)
        }
    }

    /**
     * 设置认证器
     *
     * 当服务端返回 401 时自动触发，用于刷新 Token 后重试请求。
     * 与普通拦截器的区别：认证器只对 401 响应生效，且会自动重试原请求。
     */
    fun authenticator(authenticator: Any) {
        if (authenticator is Authenticator) {
            this.authenticator = authenticator
        }
    }

    /**
     * 配置 HTTP 缓存
     * @param dir 缓存文件目录
     * @ maxSize 最大缓存大小（字节），如 10 * 1024 * 1024 表示 10MB
     */
    fun cache(dir: java.io.File, maxSize: Long) {
        this.cacheDir = dir
        this.cacheSize = maxSize
    }

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
