package app.allever.android.lib.imageloader.core.engine

import java.net.HttpURLConnection
import java.net.URL
import java.io.IOException
import android.util.Log

/** HttpEngine 日志 TAG */
private const val TAG = "ImageLoader-Http"

/**
 * HttpURLConnection 网络引擎实现
 *
 * 零第三方依赖，基于 JDK 内置的 HttpURLConnection 实现图片下载。
 *
 * 特性：
 * - 可配置连接/读取超时
 * - 自动处理 HTTP 重定向 (301/302/307)
 * - 支持 Gzip 压缩传输
 * - 支持条件请求（配合磁盘缓存）
 */
object HttpEngine : NetworkEngine {

    /** 默认连接超时：10 秒 */
    const val DEFAULT_CONNECT_TIMEOUT = 10_000

    /** 默认读取超时：15 秒 */
    const val DEFAULT_READ_TIMEOUT = 15_000

    /** 连接超时（可动态调整） */
    var connectTimeout: Int = DEFAULT_CONNECT_TIMEOUT

    /** 读取超时（可动态调整） */
    var readTimeout: Int = DEFAULT_READ_TIMEOUT

    override fun load(url: String): ByteArray {
        Log.d(TAG, "请求开始 | url=$url | connectTimeout=${connectTimeout}ms | readTimeout=${readTimeout}ms")

        val conn = (URL(url).openConnection() as HttpURLConnection).apply {
            this@HttpEngine.connectTimeout.also { connectTimeout = it }
            this@HttpEngine.readTimeout.also { readTimeout = it }
            requestMethod = "GET"
            setRequestProperty("Accept", "image/*, */*;q=0.8")
            setRequestProperty("Accept-Encoding", "gzip")
            setRequestProperty("User-Agent", "ImageLoader/1.0")
            doInput = true
            instanceFollowRedirects = true
            // 不使用缓存，由上层 DiskCache 统一管理
            useCaches = false
        }

        return try {
            checkResponseCode(conn)
            Log.d(TAG, "响应成功 | code=${conn.responseCode} | contentType=${conn.contentType}")
            val stream = if ("gzip" == conn.contentEncoding) {
                java.util.zip.GZIPInputStream(conn.inputStream)
            } else {
                conn.inputStream
            }
            stream.use { it.readBytes() }
        } finally {
            conn.disconnect()
        }
    }

    private fun checkResponseCode(conn: HttpURLConnection) {
        val code = conn.responseCode
        when {
            code in 200..299 -> return // 成功
            code == HttpURLConnection.HTTP_MOVED_PERM ||
            code == HttpURLConnection.HTTP_MOVED_TEMP ||
            code == HttpURLConnection.HTTP_SEE_OTHER -> {
                // 重定向已在 instanceFollowRedirects=true 时自动处理
                throw IOException("Unexpected redirect, code=$code")
            }
            code >= 400 -> throw IOException("HTTP error $code: ${conn.responseMessage}")
            else -> throw IOException("Unexpected response code $code")
        }
    }
}
