package app.allever.android.sample.im.http

import com.google.gson.reflect.TypeToken
import fi.iki.elonen.NanoHTTPD
import java.nio.charset.Charset


/**
 * 解析 POST JSON 请求体，并打印请求体日志
 */
internal inline fun <reified T> NanoHTTPD.IHTTPSession.parseJsonBody(): T? {
    return try {
        val body = inputStream.bufferedReader(Charsets.UTF_8).readText()

        // 打印 POST 请求体
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