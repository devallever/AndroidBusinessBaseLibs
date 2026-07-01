package com.carefree.steplib.utils

import com.tencent.mmkv.MMKV
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

/**
 * @classDes:
 * @author: 稻谷
 * @create date: 2024/7/4 17:21
 */
object Mkv {
    //MMKV 默认明文存储所有 key-value，依赖 Android 系统的沙盒机制保证文件加密。如果你担心信息泄露，你可以选择加密 MMKV。
    val mkv = MMKV.mmkvWithID(
        "mkv_step",
        MMKV.SINGLE_PROCESS_MODE,
        "dhBuvYSg3coqF+Zl4nDZA!cX2neaY21/CW6ff2BibGE/HvfSZdUn"
    )


    fun put(key: String, value: Any) {
        when (value) {
            is String -> mkv.encode(key, value)
            is Float -> mkv.encode(key, value)
            is Boolean -> mkv.encode(key, value)
            is Int -> mkv.encode(key, value)
            is Long -> mkv.encode(key, value)
            is Double -> mkv.encode(key, value)
            is ByteArray -> mkv.encode(key, value)
            //is List<*> -> mkv.encode(key, Json.encodeToString(value))
            else -> mkv.encode(key, Json.encodeToString(value))
        }
    }

    fun getString(key: String, defaultValue: String = ""): String {
        return mkv.decodeString(key, defaultValue) ?: ""
    }

    fun getInt(key: String, defaultValue: Int = 0): Int {
        return mkv.decodeInt(key, defaultValue)
    }

    fun getLong(key: String, defaultValue: Long = 0L): Long {
        return mkv.decodeLong(key, defaultValue)
    }

    fun getBool(key: String, defaultValue: Boolean = false): Boolean {
        return mkv.decodeBool(key, defaultValue)
    }

    fun getDouble(key: String, defaultValue: Double = 0.0): Double {
        return mkv.decodeDouble(key, defaultValue)
    }

    fun getFloat(key: String, defaultValue: Float = 0f): Float {
        return mkv.decodeFloat(key, defaultValue)
    }

    fun getByteArray(key: String, defaultValue: ByteArray = byteArrayOf()): ByteArray {
        return mkv.decodeBytes(key, defaultValue) ?: byteArrayOf()
    }

    fun <T> getListFromString(key: String): MutableList<T> {
        return Json.decodeFromString<MutableList<T>>(mkv.decodeString(key, "") ?: "")
    }


    /*@OptIn(InternalSerializationApi::class)
    fun <T : Any> getObjectFromString(key: String, clazz: KClass<T>): T {
        return Json.decodeFromString(clazz.serializer(), mkv.decodeString(key, "") ?: "")
    }

    inline fun <reified T> getObjectFromString(key: String): T {
        return Json.decodeFromString<T>(mkv.decodeString(key, "") ?: "")
    }*/
}