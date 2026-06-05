package app.allever.android.lib.network.core

import app.allever.android.lib.network.core.engine.EngineConfig
import app.allever.android.lib.network.core.engine.ResponseConverter
import app.allever.android.lib.network.core.exception.NetworkException
import app.allever.android.lib.network.core.interceptor.NetInterceptor
import app.allever.android.lib.network.core.response.GsonConverter

/**
 * 网络统一配置（DSL Builder 模式）
 *
 * 在 Application.onCreate() 中一次性完成所有配置：
 *
 * ```kotlin
 * Network.init {
 *     // 基础配置
 *     baseUrl("https://api.example.com")
 *     engine("okhttp") { connectTimeout(10_000) }
 *     successCode(0)
 *
 *     // 公共请求头
 *     header("Accept", "application/json")
 *
 *     // 应用层拦截器
 *     interceptor(AuthNetInterceptor(...))
 *     interceptor(RetryNetInterceptor(3))
 *
 *     // 响应体配置（支持任意字段名）
 *     responseClass(MyResponse::class.java)
 *     codeField("errorCode", "errCode", "code")
 *     msgField("errorMsg", "message")
 *     dataField("result", "data")
 *
 *     // 全局错误处理
 *     onError { exception, _ ->
 *         Log.e("Network", "请求失败: ${exception.displayMessage}")
 *     }
 * }
 * ```
 */
class NetworkConfig internal constructor(builder: Builder) {

    // ==================== 基础配置 ====================

    /** 基础 URL */
    val baseUrl: String = builder.baseUrl

    /** 引擎名称 */
    val engineName: String = builder.engineName

    /** 业务成功码 */
    val successCode: Int = builder.successCode

    /** 引擎配置 */
    val engineConfig: EngineConfig = builder.engineConfig

    /** 序列化转换器 */
    val converter: ResponseConverter = builder.converter ?: GsonConverter()

    // ==================== 请求头 ====================

    /** 全局公共请求头 */
    val headers: Map<String, String> = builder.headers.toMap()

    // ==================== 拦截器 ====================

    /** 应用层拦截器列表（按添加顺序执行） */
    val interceptors: List<NetInterceptor> = builder.interceptors.toList()

    /** 是否启用日志拦截器 */
    val logEnabled: Boolean = builder.logEnabled

    // ==================== 响应体字段映射 ====================

    /**
     * 自定义 code 提取器（函数式，最高优先级）
     * 接收反序列化后的响应对象，返回 Int 类型的业务码
     */
    val codeExtractor: ((Any) -> Int)? = builder.codeExtractor

    /** 自定义 msg 提取器 */
    val msgExtractor: ((Any) -> String)? = builder.msgExtractor

    /** 自定义 data 提取器 */
    val dataExtractor: ((Any) -> Any?)? = builder.dataExtractor

    /**
     * code 字段名候选列表（按优先级匹配第一个找到的）
     * 默认: ["code", "errorCode", "errCode", "status", "error_code"]
     */
    val codeFieldNames: List<String> =
        builder.codeFieldNames.ifEmpty {
            listOf("code", "errorCode", "errCode", "status", "error_code")
        }

    /** msg 字段名候选列表 */
    val msgFieldNames: List<String> =
        builder.msgFieldNames.ifEmpty {
            listOf("msg", "message", "errorMsg", "errorMessage", "error_msg")
        }

    /** data 字段名候选列表 */
    val dataFieldNames: List<String> =
        builder.dataFieldNames.ifEmpty {
            listOf("data", "result", "body", "content")
        }

    /** 默认错误码（当找不到 code 字段时使用） */
    val defaultErrorCode: Int = builder.defaultErrorCode

    /**
     * 统一业务响应类（用于反序列化 JSON）
     * 如 ApiResponse::class.java, MyResponse::class.java 等
     */
    val responseClass: Class<*>? = builder.responseClass

    /**
     * 基础响应类（用于 Repository 层反射实例化失败响应）
     * 必须有无参构造函数，且可通过字段赋值设置 code/msg/data
     * 如不设置，BaseRepository 将返回 null
     */
    val baseResponseClass: Class<*>? = builder.responseClass

    // ==================== 错误处理 ====================

    /** 全局错误回调 */
    val globalErrorHandler: ErrorHandler? = builder.globalErrorHandler

    // ==================== DSL Builder ====================

    class Builder {
        // ---- 基础 ----
        var baseUrl: String = ""
        var engineName: String = ""
        var successCode: Int = 0
        var engineConfig: EngineConfig = EngineConfig()
        var converter: ResponseConverter? = null

        // ---- 请求头 ----
        private val _headers = mutableMapOf<String, String>()
        val headers: Map<String, String> get() = _headers

        // ---- 拦截器 ----
        private val _interceptors = mutableListOf<NetInterceptor>()
        val interceptors: List<NetInterceptor> get() = _interceptors
        var logEnabled: Boolean = true

        // ---- 响应体映射 ----
        var codeExtractor: ((Any) -> Int)? = null
        var msgExtractor: ((Any) -> String)? = null
        var dataExtractor: ((Any) -> Any?)? = null
        var codeFieldNames: List<String> = emptyList()
        var msgFieldNames: List<String> = emptyList()
        var dataFieldNames: List<String> = emptyList()
        var defaultErrorCode: Int = -1
        var responseClass: Class<*>? = null

        // ---- 错误处理 ----
        var globalErrorHandler: ErrorHandler? = null

        // ==================== 配置方法 ====================

        fun baseUrl(url: String) = apply { this.baseUrl = url }

        /**
         * 选择 HTTP 引擎
         * @param name 引擎名称（如 "okhttp"、"url_connection"），对应引擎模块注册时的名称
         * @param block 引擎专属配置
         */
        fun engine(name: String, block: EngineConfig.() -> Unit = {}) = apply {
            this.engineName = name
            engineConfig.apply(block)
        }

        fun successCode(code: Int) = apply { this.successCode = code }

        /** 设置自定义序列化转换器（默认 Gson） */
        fun converter(converter: ResponseConverter) = apply { this.converter = converter }

        // ---- 请求头 ----

        fun header(key: String, value: String) = apply {
            _headers[key] = value
        }

        fun headers(map: Map<String, String>) = apply {
            _headers.putAll(map)
        }

        // ---- 拦截器 ----

        fun interceptor(interceptor: NetInterceptor) = apply {
            _interceptors.add(interceptor)
        }

        fun enableLog(enabled: Boolean = true) = apply { logEnabled = enabled }

        // ---- 响应体字段映射 ----

        /**
         * 手动指定 code 提取逻辑（最高优先级，覆盖注解和字段名）
         */
        fun codeExtractor(extractor: (Any) -> Int) = apply { codeExtractor = extractor }

        fun msgExtractor(extractor: (Any) -> String) = apply { msgExtractor = extractor }

        fun dataExtractor(extractor: (Any) -> Any?) = apply { dataExtractor = extractor }

        /**
         * 指定 code 字段名候选列表（按顺序匹配第一个找到的）
         * @param names 字段名列表，如 ["errorCode", "errCode"]
         */
        fun codeField(vararg names: String) = apply { codeFieldNames = names.toList() }

        fun msgField(vararg names: String) = apply { msgFieldNames = names.toList() }

        fun dataField(vararg names: String) = apply { dataFieldNames = names.toList() }

        fun defaultErrorCode(code: Int) = apply { defaultErrorCode = code }

        /**
         * 设置统一业务响应类
         * @param clazz 响应类的 Class，如 MyResponse::class.java
         */
        fun responseClass(clazz: Class<*>) = apply { responseClass = clazz }

        /**
         * 设置基础响应类（用于 Repository 层反射实例化失败响应）
         * 该类必须有无参构造函数
         * @param clazz 基础响应类的 Class
         */
        fun baseResponseClass(clazz: Class<*>) = apply { responseClass = clazz }

        // ---- 错误处理 ----

        /**
         * 全局错误处理回调
         * @param handler 收到异常时触发
         */
        fun onError(handler: ErrorHandler) = apply { globalErrorHandler = handler }

        // ==================== 构建 ====================

        internal fun build(): NetworkConfig {
            require(baseUrl.isNotBlank()) { "baseUrl 不能为空" }
            require(engineName.isNotBlank()) { "必须通过 engine(\"name\") 选择一个 HTTP 引擎" }
            return NetworkConfig(this)
        }
    }
}

/**
 * 全局错误处理器接口
 */
interface ErrorHandler {
    /**
     * 当网络请求发生错误时调用
     * @param exception 网络异常
     * @param response 原始响应（可能为 null，如网络层就失败了）
     */
    fun onError(exception: NetworkException, response: Any?)
}
