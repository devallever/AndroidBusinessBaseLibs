package app.allever.android.lib.network.engine.huc

import app.allever.android.lib.network.core.engine.EngineConfig

/**
 * HttpURLConnection 引擎专属配置
 *
 * 继承 EngineConfig 基础配置，扩展 HUC 特有选项：
 */
open class UrlConnectionConfig : EngineConfig() {

    /** 是否跟随重定向（默认 true） */
    var followRedirects: Boolean = true

    /** 是否使用缓存（默认 false） */
    var useCaches: Boolean = false

    /** 请求方法（用于某些需要预设置的 HUC 行为） */
    var requestMethod: String? = null

    /** 连接是否可复用（默认 true） */
    var keepAlive: Boolean = true

    fun followRedirects(enable: Boolean) { followRedirects = enable }
    fun useCaches(use: Boolean) { useCaches = use }
    fun keepAlive(enable: Boolean) { keepAlive = enable }
}
