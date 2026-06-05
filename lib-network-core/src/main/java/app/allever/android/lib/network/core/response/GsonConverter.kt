package app.allever.android.lib.network.core.response

import app.allever.android.lib.network.core.engine.ResponseConverter
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import java.lang.reflect.Type

/**
 * Gson 序列化转换器实现
 *
 * 基于 Gson 库将 ByteArray 反序列化为目标对象
 *
 * @param gson Gson 实例，允许自定义配置
 */
class GsonConverter(private val gson: Gson = GsonBuilder().create()) : ResponseConverter {

    override fun <T> convert(bytes: ByteArray, clazz: Class<T>): T? {
        return try {
            val json = bytes.toString(Charsets.UTF_8).trim()
            if (json.isEmpty() || json == "null") return null
            gson.fromJson(json, clazz)
        } catch (e: Exception) {
            null
        }
    }

    /**
     * 使用 Type 进行转换（支持泛型如 List<User>）
     */
    fun <T> convert(bytes: ByteArray, type: Type): T? {
        return try {
            val json = bytes.toString(Charsets.UTF_8).trim()
            if (json.isEmpty() || json == "null") return null
            gson.fromJson<T>(json, type)
        } catch (e: Exception) {
            null
        }
    }

    /** 将对象序列化为 JSON 字符串 */
    fun toJson(any: Any?): String = gson.toJson(any)
}
