package app.allever.android.lib.network.core.engine.body

import java.io.File
import java.io.OutputStream

/**
 * 抽象请求体
 *
 * 各引擎需将此抽象转换为自身实现：
 * - OkHttp → okhttp3.RequestBody
 * - HttpURLConnection → OutputStream 写入
 */
abstract class NetBody {

    /** Content-Type (如 "application/json; charset=utf-8") */
    abstract val contentType: String?

    /** 请求体内容长度，未知返回 -1 */
    abstract fun contentLength(): Long

    /** 将请求体写入输出流 */
    abstract fun writeTo(output: OutputStream)

    // ==================== 工厂方法 ====================

    companion object {
        /**
         * 从字符串创建请求体
         * @param content 字符串内容
         * @param contentType MIME 类型，默认 application/json
         */
        fun create(content: String, contentType: String = "application/json; charset=utf-8"): NetBody {
            return StringNetBody(content, contentType)
        }

        /**
         * 从字节数组创建请求体
         */
        fun create(bytes: ByteArray, contentType: String = "application/octet-stream"): NetBody {
            return BytesNetBody(bytes, contentType)
        }

        /**
         * 从 File 创建请求体（用于上传文件）
         * @param file 文件
         * @param contentType MIME 类型
         */
        fun create(file: File, contentType: String? = null): NetBody {
            return FileNetBody(file, contentType)
        }

        /**
         * 空请求体
         */
        fun empty(): NetBody = EmptyNetBody()
    }
}

/** 旧名兼容别名（后续版本移除） */
@Deprecated("请使用 NetBody 替代", ReplaceWith("NetBody"))
typealias RequestBody = NetBody
