package app.allever.android.lib.imageloader.core.internal.cache

import android.graphics.Bitmap
import android.util.LruCache
import java.security.MessageDigest

/**
 * 缓存 Key 生成工具
 */
object CacheKey {

    /**
     * 生成 MD5 哈希作为缓存文件名（安全、无特殊字符）
     */
    fun md5(key: String): String {
        return try {
            val digest = MessageDigest.getInstance("MD5")
            digest.update(key.toByteArray())
            val bytes = digest.digest()
            StringBuilder(bytes.size * 2).apply {
                for (b in bytes) {
                    append(String.format("%02x", b.toInt() and 0xff))
                }
            }.toString()
        } catch (e: Exception) {
            // MD5 不可用时使用简单 hash 替代
            key.hashCode().toString()
        }
    }
}
