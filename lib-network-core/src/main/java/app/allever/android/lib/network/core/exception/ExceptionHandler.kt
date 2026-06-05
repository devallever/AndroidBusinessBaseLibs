package app.allever.android.lib.network.core.exception

import android.net.Uri
import androidx.annotation.StringRes
import app.allever.android.lib.core.R
import app.allever.android.lib.core.app.App
import org.apache.http.conn.ConnectTimeoutException
import java.net.ConnectException
import java.net.SocketTimeoutException
import java.net.UnknownHostException
import java.util.concurrent.CancellationException
import javax.net.ssl.SSLException

/**
 * 异常处理器 - 将任意 Throwable 转换为 NetworkException
 *
 * 统一所有异常类型，让上层只需处理 NetworkException 即可
 */
object ExceptionHandler {

    /**
     * 处理异常并转换为 NetworkException
     * @param e 原始异常
     * @return 标准化的 NetworkException
     */
    fun handle(e: Throwable): NetworkException {
        // 已经是 NetworkException，直接返回
        if (e is NetworkException) return e

        // 协程取消
        if (e is CancellationException || e is java.io.InterruptedIOException) {
            return NetworkException.CanceledError(e)
        }

        // 超时
        if (e is SocketTimeoutException || e is ConnectTimeoutException) {
            return NetworkException.TimeoutError(e)
        }

        // SSL 错误
        if (e is SSLException) {
            return NetworkException.SslError(e)
        }

        // 连接失败（无网络 / DNS 解析失败）
        if (e is ConnectException || e is UnknownHostException || e is java.net.PortUnreachableException) {
            // 进一步判断是否是无网络
            if (!isNetworkAvailable()) {
                return NetworkException.NoNetworkError(e)
            }
            return NetworkException.ConnectError(e)
        }

        // JSON 解析错误
        if (isParseException(e)) {
            return NetworkException.ParseError(e.message.orEmpty(), e)
        }

        // 默认未知错误
        return NetworkException.UnknownError(e)
    }

    /**
     * 判断是否为解析类异常
     */
    private fun isParseException(e: Throwable): Boolean {
        return when (e) {
            is com.google.gson.JsonParseException,
            is com.google.gson.stream.MalformedJsonException,
            is org.json.JSONException,
            is android.net.ParseException,
            is IllegalStateException -> true   // Gson 类型不匹配也会抛这个
            else -> false
        }
    }

    /**
     * 检查网络是否可用
     */
    private fun isNetworkAvailable(): Boolean {
        return try {
            val connectivityManager = App.context
                .getSystemService(android.content.Context.CONNECTIVITY_SERVICE)
                    as? android.net.ConnectivityManager
                ?: return false
            val network = connectivityManager.activeNetworkInfo
            network?.isConnected == true
        } catch (_: Exception) {
            false
        }
    }

    /**
     * 获取字符串资源
     */
    internal fun getStringRes(@StringRes stringId: Int): String {
        return try {
            App.context.resources.getString(stringId)
        } catch (_: Exception) {
            ""
        }
    }
}
