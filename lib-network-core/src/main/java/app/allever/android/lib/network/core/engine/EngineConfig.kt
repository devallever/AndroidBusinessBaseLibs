package app.allever.android.lib.network.core.engine

/**
 * 引擎基础配置 - 所有引擎共享的通用配置项
 *
 * 各引擎可继承此类扩展专属配置（如 OkHttpConfig、UrlConnectionConfig）
 */
open class EngineConfig {
    /** 连接超时（毫秒） */
    var connectTimeoutMs: Long = 10_000L

    /** 读取超时（毫秒） */
    var readTimeoutMs: Long = 15_000L

    /** 写入超时（毫秒） */
    var writeTimeoutMs: Long = 30_000L

    fun connectTimeout(ms: Long) { connectTimeoutMs = ms }
    fun readTimeout(ms: Long) { readTimeoutMs = ms }
    fun writeTimeout(ms: Long) { writeTimeoutMs = ms }
}
