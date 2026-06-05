package app.allever.android.lib.network.core.interceptor

import app.allever.android.lib.network.core.engine.NetRequest
import app.allever.android.lib.network.core.engine.NetResponse

/**
 * 应用层拦截器接口（引擎无关）
 *
 * 与 OkHttp Interceptor 类似，但运行在引擎抽象层之上，
 * 所有引擎共享同一套拦截器链。
 *
 * 使用场景：
 * - AuthInterceptor: Token 注入 + 自动刷新
 * - HeaderInterceptor: 公共请求头
 * - RetryInterceptor: 失败重试
 * - LoggerInterceptor: 请求/响应日志
 */
interface NetInterceptor {

    /**
     * 拦截处理
     * @param chain 拦截器链，调用 chain.proceed() 将请求传递给下一个环节
     * @return 响应
     */
    @Throws(Exception::class)
    fun intercept(chain: NetChain): NetResponse
}

/** 旧名兼容别名（后续版本移除） */
@Deprecated("请使用 NetInterceptor 替代", ReplaceWith("NetInterceptor"))
typealias Interceptor = NetInterceptor
