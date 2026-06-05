package app.allever.android.lib.network.core

import android.util.Log
import app.allever.android.lib.network.core.engine.*
import app.allever.android.lib.network.core.exception.ExceptionHandler
import app.allever.android.lib.network.core.exception.NetworkException
import app.allever.android.lib.network.core.interceptor.HeaderInterceptor
import app.allever.android.lib.network.core.interceptor.NetChain
import app.allever.android.lib.network.core.interceptor.LoggerInterceptor
import app.allever.android.lib.network.core.response.GsonConverter
import app.allever.android.lib.network.core.response.ResponseAdapter
import app.allever.android.lib.network.core.util.FailureResponseFactory
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.lang.reflect.Type

/**
 * 网络组件库统一入口
 *
 * ## 初始化
 * ```kotlin
 * class MyApp : Application() {
 *     override fun onCreate() {
 *         super.onCreate()
 *         Network.init {
 *             baseUrl("https://api.example.com")
 *             engine("url_connection") {
 *                 connectTimeout(10_000)
 *                 readTimeout(15_000)
 *             }
 *             successCode(0)
 *             header("Accept", "application/json")
 *             enableLog(true)
 *         }
 *     }
 * }
 * ```
 *
 * ## 使用
 * ```kotlin
 * // GET 请求
 * val result = Network.get<MyResponse<User>>("/user/123") {
 *     header("Authorization", "Bearer token")
 * }
 *
 * when (result) {
 *     is Result.Success -> { ... }
 *     is Result.Error -> showError(result.exception.displayMessage)
 * }
 * ```
 */
object NetCore {

    private val TAG = NetCore::class.java.simpleName

    /** 统一配置（init 后可用） */
    lateinit var config: NetworkConfig
        private set

    /** HTTP 引擎实例（懒加载） */
    private var _engine: HttpEngine? = null

    private val engine: HttpEngine
        get() {
            if (_engine == null) {
                _engine = EngineRegistry.createDefault(config.engineConfig).also {
                    Log.d(TAG, "引擎已创建: ${it.engineName}")
                }
            }
            return _engine!!
        }

    /** 是否已完成初始化 */
    val isInitialized: Boolean get() = ::config.isInitialized

    // ==================== 初始化 ====================

    /**
     * 初始化网络组件（在 Application.onCreate 中调用一次即可）
     * @param block DSL 配置块
     */
    fun init(block: NetworkConfig.Builder.() -> Unit) {
        if (isInitialized) {
            Log.w(TAG, "Network 已初始化，重复调用将被忽略")
            return
        }

        config = NetworkConfig.Builder().apply(block).build()

        // 注册默认引擎到 EngineRegistry
        EngineRegistry.setDefault(config.engineName)

        Log.i(TAG, """
            |Network 初始化完成:
            |  baseUrl   = ${config.baseUrl}
            |  engine    = ${config.engineName}
            |  successCode= ${config.successCode}
            |  converters= ${config.converter::class.simpleName}
            |  interceptors= ${config.interceptors.size} 个
        """.trimMargin())
    }

    // ==================== GET / POST / PUT / DELETE / PATCH ====================

    suspend fun <T> get(
        path: String,
        type: Type? = null,
        block: (NetRequest.Builder.() -> Unit)? = null,
    ): T = executeRequest(HttpMethod.GET, path, null, block, type)

    suspend fun <T> post(
        path: String,
        bodyData: Any? = null,
        block: (NetRequest.Builder.() -> Unit)? = null,
        type: Type? = null
    ): T = executeRequest(HttpMethod.POST, path, bodyData, block, type)

    suspend fun <T> put(
        path: String,
        bodyData: Any? = null,
        block: (NetRequest.Builder.() -> Unit)? = null,
        type: Type? = null
    ): T = executeRequest(HttpMethod.PUT, path, bodyData, block, type)

    suspend fun <T> delete(
        path: String,
        block: (NetRequest.Builder.() -> Unit)? = null,
        type: Type? = null
    ): T = executeRequest(HttpMethod.DELETE, path, null, block, type)

    suspend fun <T> patch(
        path: String,
        bodyData: Any? = null,
        block: (NetRequest.Builder.() -> Unit)? = null,
        type: Type? = null
    ): T = executeRequest(HttpMethod.PATCH, path, bodyData, block, type)

    // ==================== 核心请求执行（非 inline，内部使用）====================

    @Suppress("UNCHECKED_CAST")
    private suspend fun <T> executeRequest(
        method: HttpMethod,
        path: String,
        bodyData: Any?,
        customBlock: (NetRequest.Builder.() -> Unit)?,
        explicitType: Type?
    ): T {
        checkInitialized()

        return withContext(Dispatchers.IO) {
            try {
                // 1. 构建请求
                val request = buildRequest(method, path, bodyData, customBlock)

                // 2. 构建拦截器链并执行
                val allInterceptors = buildInterceptors()
                val chain = NetChain(
                    interceptors = allInterceptors,
                    engineExecute = { req ->
                        val startTime = System.currentTimeMillis()
                        val response = engine.execute(req)
                        response.copy(elapsedMs = System.currentTimeMillis() - startTime)
                    }
                )
                chain.request = request
                val httpResponse = chain.proceed(request)

                // 3. 检查 HTTP 层状态码
                if (!httpResponse.isSuccessful) {
                    return@withContext createFailureResponse(
                        code = httpResponse.code,
                        message = httpResponse.message,
                        explicitType
                    )
                }

                // 4. 反序列化为业务响应
                val responseBody = httpResponse.body
                    ?: return@withContext createFailureResponse(
                        code = -1,
                        message = "响应体为空",
                        explicitType
                    )

                // 使用显式传入的 Type 或 responseClass 或 T 的 Class
                val parsedResponse: Any? = when {
                    explicitType != null -> {
                        convertWithType(responseBody, explicitType)
                            ?: return@withContext createFailureResponse(
                                code = -2,
                                message = "反序列化结果为空",
                                explicitType
                            )
                    }
                    config.responseClass != null -> {
                        config.converter.convert(responseBody, config.responseClass!!)
                            ?: return@withContext createFailureResponse(
                                code = -2,
                                message = "反序列化结果为空",
                                explicitType
                            )
                    }
                    else -> {
                        @Suppress("UNCHECKED_CAST")
                        config.converter.convert(responseBody, Any::class.java) as? T
                            ?: return@withContext createFailureResponse(
                                code = -3,
                                message = "反序列化失败，请设置 responseClass",
                                explicitType
                            )
                    }
                }

                parsedResponse as T

            } catch (e: Exception) {
                handleGlobalError(ExceptionHandler.handle(e), null)
                createFailureResponse(e, explicitType)
            }
        }
    }

    /**
     * 获取原始 NetResponse（不进行业务反序列化）
     */
    suspend fun rawGet(
        path: String,
        block: (NetRequest.Builder.() -> Unit)? = null
    ): Result<NetResponse> {
        checkInitialized()

        return try {
            val request = buildRequest(HttpMethod.GET, path, null, block)
            val startTime = System.currentTimeMillis()
            val response = engine.execute(request)
            Result.success(response.copy(elapsedMs = System.currentTimeMillis() - startTime))
        } catch (e: Exception) {
            val networkException = ExceptionHandler.handle(e)
            handleGlobalError(networkException, null)
            Result.failure(networkException)
        }
    }

    // ==================== 内部工具方法 ====================

    /**
     * 使用 GsonConverter 的 Type 方式转换（支持泛型）
     */
    private fun <T> convertWithType(bytes: ByteArray, type: Type): T? {
        val gsonConverter = config.converter as? GsonConverter
        return gsonConverter?.convert(bytes, type)
    }

    // ==================== 失败响应创建（委托给 FailureResponseFactory）====================

    /**
     * 通过 code + message 创建失败响应实例
     */
    @Suppress("UNCHECKED_CAST")
    private fun <T> createFailureResponse(code: Int, message: String, explicitType: Type?): T {
        val clazz = resolveFailureClass(explicitType)
            ?: throw IllegalStateException("未配置 responseClass 或 baseResponseClass，无法创建失败响应")
        return FailureResponseFactory.create(clazz, code, message)
    }

    /**
     * 从异常创建失败响应
     */
    private fun <T> createFailureResponse(exception: Exception, explicitType: Type?): T {
        val clazz = resolveFailureClass(explicitType)
            ?: throw IllegalStateException("未配置 responseClass 或 baseResponseClass，无法创建失败响应")
        return FailureResponseFactory.create(clazz, exception)
    }

    /**
     * 确定用于创建失败响应的 Class
     */
    private fun resolveFailureClass(explicitType: Type?): Class<*>? = when {
        explicitType != null && explicitType is Class<*> -> explicitType
        config.responseClass != null -> config.responseClass
        config.baseResponseClass != null -> config.baseResponseClass
        else -> null
    }

    private fun buildRequest(
        method: HttpMethod,
        path: String,
        bodyData: Any?,
        customBlock: (NetRequest.Builder.() -> Unit)?
    ): NetRequest {
        val fullUrl = resolveUrl(path)

        return NetRequest.Builder().apply {
            url(fullUrl)
            method(method)
            connectTimeout(config.engineConfig.connectTimeoutMs)
            readTimeout(config.engineConfig.readTimeoutMs)
            writeTimeout(config.engineConfig.writeTimeoutMs)

            // 请求体：将对象转为 JSON 字符串
            if (bodyData != null && method in listOf(HttpMethod.POST, HttpMethod.PUT, HttpMethod.PATCH)) {
                val json = when (bodyData) {
                    is String -> bodyData
                    else -> (config.converter as? GsonConverter)
                        ?.toJson(bodyData) ?: bodyData.toString()
                }
                body(NetBody.create(json))
            }

            // 用户自定义配置
            customBlock?.invoke(this)
        }.build()
    }

    private fun resolveUrl(path: String): String {
        val base = config.baseUrl.trimEnd('/')
        val p = path.trimStart('/')
        return "$base/$p"
    }

    private fun buildInterceptors(): List<app.allever.android.lib.network.core.interceptor.NetInterceptor> {
        val list = mutableListOf<app.allever.android.lib.network.core.interceptor.NetInterceptor>()

        if (config.logEnabled) {
            list.add(LoggerInterceptor())
        }

        list.add(HeaderInterceptor(config.headers))
        list.addAll(config.interceptors)

        return list
    }

    private fun handleGlobalError(exception: NetworkException, response: Any?) {
        try {
            config.globalErrorHandler?.onError(exception, response)
        } catch (_: Exception) {
            // 全局错误回调本身不应影响主流程
        }
    }

    private fun checkInitialized() {
        require(isInitialized) {
            "Network 未初始化！请先在 Application.onCreate() 中调用 Network.init { ... }"
        }
    }

    // ==================== 公开便捷方法 ====================

    /**
     * 从业务响应中安全提取 data
     */
    fun <T> extractData(response: Any): T? {
        return ResponseAdapter.extractData<T>(response, config)
    }

    /**
     * 获取当前使用的引擎名称
     */
    fun currentEngine(): String = EngineRegistry.getDefaultName() ?: "未设置"

    /**
     * 创建异步 Call（支持取消、回调、协程 await）
     *
     * 使用示例：
     * ```kotlin
     * // 回调方式
     * val call = Network.newCall(HttpMethod.GET, "/user/123") { header("token", "xxx") }
     * call.enqueue(object : NetCallback {
     *     override fun onSuccess(response: NetResponse) { ... }
     *     override fun onFailure(e: Exception) { ... }
     * })
     *
     * // 协程方式
     * val response = call.await()
     * ```
     *
     * @param method 请求方法
     * @param path 接口路径
     * @param block 请求构建
     * @return NetCall 对象，可随时 cancel()
     */
    fun newCall(
        method: HttpMethod,
        path: String,
        block: (NetRequest.Builder.() -> Unit)? = null
    ): NetCall {
        checkInitialized()
        val request = buildRequest(method, path, null, block)
        return engine.newCall(request)
    }

    /**
     * 释放资源（应用退出时调用）
     */
    fun shutdown() {
        _engine?.shutdown()
        _engine = null
    }
}
