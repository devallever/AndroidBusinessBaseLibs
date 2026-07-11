package app.allever.android.sample.im.http

import com.google.gson.reflect.TypeToken
import fi.iki.elonen.NanoHTTPD
import java.nio.charset.Charset

/**
 * 统一解析 POST JSON 请求体
 */
internal inline fun <reified T> NanoHTTPD.IHTTPSession.parseJsonBody(): T? {
    return try {
        val body = inputStream.bufferedReader(Charsets.UTF_8).readText()
        if (body.isBlank()) null
        else LocalHttpServer.gson.fromJson(body, object : TypeToken<T>() {}.type)
    } catch (e: Exception) {
        null
    }
}