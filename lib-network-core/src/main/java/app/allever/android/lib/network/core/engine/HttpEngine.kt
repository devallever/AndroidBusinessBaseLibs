package app.allever.android.lib.network.core.engine

/**
 * HTTP 引擎接口 - 所有网络引擎必须实现的契约
 *
 * 库本身不包含任何具体引擎实现，通过 EngineRegistry 注册机制按需引入：
 * - OkHttp → lib-network-okhttp 模块
 * - HttpURLConnection → lib-network-engine-huc 模块
 * - Ktor → （未来）lib-network-engine-ktor 模块
 *
 * 注册方式：在引擎模块的 companion object init {} 中自动注册
 */
interface HttpEngine {

    /** 引擎唯一标识名称（如 "okhttp"、"url_connection"） */
    val engineName: String

    /**
     * 同步执行请求
     * @param request 引擎无关的请求对象
     * @return 引擎无关的响应对象
     */
    fun execute(request: HttpRequest): HttpResponse

    /**
     * 创建异步调用（支持取消）
     * @param request 引擎无关的请求对象
     * @return 可取消的 Call 对象
     */
    fun newCall(request: HttpRequest): Call

    /**
     * 释放引擎资源
     * 应在应用退出时调用
     */
    fun shutdown()
}
