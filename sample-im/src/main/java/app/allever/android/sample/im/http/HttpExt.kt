package app.allever.android.sample.im.http

import com.google.gson.reflect.TypeToken
import fi.iki.elonen.NanoHTTPD
import java.nio.charset.Charset


/**
 * 解析 POST JSON 请求体，并打印请求体日志
 * 正确读取 POST JSON 请求体（NanoHTTPD 标准写法）
 */
internal inline fun <reified T> NanoHTTPD.IHTTPSession.parseJsonBody(): T? {
    return try {
        // 1. 先触发 NanoHTTPD 解析请求体
        val params = mutableMapOf<String, String>()
        this.parseBody(params)

        // 2. 从解析结果中取出原始 JSON 字符串
        val body = params["postData"] ?: ""

        // 3. 打印请求体日志
        if (body.isNotBlank()) {
            LocalHttpServer.log("[请求体] ${this.uri} -> $body")
        }

        if (body.isBlank()) null
        else LocalHttpServer.gson.fromJson(body, object : TypeToken<T>() {}.type)
    } catch (e: Exception) {
        LocalHttpServer.logE("解析请求体失败: ${e.message}")
        null
    }
}