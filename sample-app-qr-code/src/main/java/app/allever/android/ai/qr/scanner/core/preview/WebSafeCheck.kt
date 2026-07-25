package app.allever.android.ai.qr.scanner.core.preview

import android.text.TextUtils
import com.google.zxing.client.android.HttpHelper
import java.io.IOException
import java.util.*

object WebSafeCheck {
    const val TYPE_CHECKING = 0
    const val TYPE_SECURE = 1
    const val TYPE_MALICIOUS = 2
    const val TYPE_UNKNOWN = 3

    var datas = WeakHashMap<String, Int>()

    fun getSafeTypeIfNeedrequestUri(uri: String, outInfo: StringBuffer?): Int {
        var type = datas[uri]
        if (type == null) {
            type = getSafeTypeRequestUri(uri, outInfo)
        }
        return type ?: TYPE_UNKNOWN
    }

    fun getSafeTypeRequestUri(uri: String, outInfo: StringBuffer?): Int {
        val requestURI = "https://transparencyreport.google.com/transparencyreport/api/v3/safebrowsing/status?site=$uri"
        var type = TYPE_UNKNOWN
        var faildInfo = ""
        try {
            var contents = HttpHelper.downloadViaHttp(requestURI, HttpHelper.ContentType.HTML, 4096)
            if (!TextUtils.isEmpty(contents)) {
                val list = Regex("sb.ssr\",").split(contents, 0)
                if (list != null && list.size > 1) {
                    val line = list[1]
                    if (!TextUtils.isEmpty(line)) {
                        val c = line[0]
                        try {
                            val code = Integer.parseInt(c.toString())
                            when (code) {
                                2 -> type = TYPE_MALICIOUS
                                1 -> type = TYPE_SECURE
                                else -> type = TYPE_SECURE
                            }
                        } catch (e: Exception) {
                            faildInfo = e.message ?: "unknown1"
                        }
                    }
                }
            }
        } catch (ioe: IOException) {
            faildInfo = ioe.message ?: "unknown2"
        }
        datas[uri] = type
        outInfo?.append(faildInfo)
        return type
    }
}