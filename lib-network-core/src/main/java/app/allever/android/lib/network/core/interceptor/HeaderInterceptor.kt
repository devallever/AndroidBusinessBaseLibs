package app.allever.android.lib.network.core.interceptor

import android.util.Log
import app.allever.android.lib.network.core.engine.HttpRequest
import app.allever.android.lib.network.core.engine.HttpResponse

/**
 * 公共请求头拦截器
 *
 * 自动添加公共 Header 到每个请求中。
 * 在 NetworkConfig 中配置的全局 headers 会通过此拦截器注入。
 *
 * 默认添加：
 * - Accept-Encoding: gzip
 * - Accept: application/json
 * - Content-Type: application/json; charset=utf-8
 */
class HeaderInterceptor(private val globalHeaders: Map<String, String>) : Interceptor {

    companion object {
        private const val TAG = "HeaderInterceptor"
    }

    override fun intercept(chain: InterceptorChain): HttpResponse {
        val originalRequest = chain.request ?: return chain.proceed(chain.request!!)

        // 构建新请求，合并全局 header
        val builder = HttpRequest.Builder()
            .url(originalRequest.url)
            .method(originalRequest.method)
            .connectTimeout(originalRequest.connectTimeoutMs)
            .readTimeout(originalRequest.readTimeoutMs)
            .writeTimeout(originalRequest.writeTimeoutMs)

        // 1. 先加默认公共头
        builder.header("Accept-Encoding", "gzip")
        builder.header("Accept", "application/json")
        builder.header("Content-Type", "application/json; charset=utf-8")

        // 2. 再加用户配置的全局 headers
        for ((key, value) in globalHeaders) {
            builder.header(key, value)
        }

        // 3. 最后保留原始请求的 headers（优先级最高，可覆盖全局配置）
        builder.headers(originalRequest.headers)

        // 4. 保留 body 和其他属性
        builder.body(originalRequest.body)
        builder.tag(originalRequest.tag)

        val newRequest = builder.build()
        return chain.proceed(newRequest)
    }
}
