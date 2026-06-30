package com.plinkopro.wincash.utils

import android.content.Context
import android.content.SharedPreferences
import androidx.core.content.edit
import com.google.gson.Gson
import java.lang.reflect.Type
import kotlin.jvm.java
import kotlin.text.isNullOrEmpty

class SpUtil private constructor() {

    companion object {
        private const val DEFAULT_SP_NAME = "default_sp"
        private lateinit var sharedPreferences: SharedPreferences
        private val gson = Gson()

        /**
         * 初始化，建议在Application中调用
         */
        fun init(context: Context, spName: String = DEFAULT_SP_NAME) {
            sharedPreferences = context.getSharedPreferences(spName, Context.MODE_PRIVATE)
        }

        /**
         * 保存数据
         */
        fun <T> put(key: String, value: T) {
            sharedPreferences.edit {
                when (value) {
                    is Int -> putInt(key, value)
                    is Long -> putLong(key, value)
                    is Float -> putFloat(key, value)
                    is Boolean -> putBoolean(key, value)
                    is String -> putString(key, value)
                    else -> putString(key, gson.toJson(value))
                }
            }
        }

        /**
         * 获取数据
         */
        @Suppress("UNCHECKED_CAST")
        fun <T> get(key: String, defaultValue: T, type: Type? = null): T {
            return when (defaultValue) {
                is Int -> sharedPreferences.getInt(key, defaultValue) as T
                is Long -> sharedPreferences.getLong(key, defaultValue) as T
                is Float -> sharedPreferences.getFloat(key, defaultValue) as T
                is Boolean -> sharedPreferences.getBoolean(key, defaultValue) as T
                is String -> sharedPreferences.getString(key, defaultValue) as T
                else -> {
                    val json = sharedPreferences.getString(key, "")
                    if (json.isNullOrEmpty()) defaultValue
                    else gson.fromJson(json, type ?: defaultValue!!::class.java) as T
                }
            }
        }

        /**
         * 删除指定key的数据
         */
        fun remove(key: String) {
            sharedPreferences.edit {
                remove(key)
            }
        }

        /**
         * 清空所有数据
         */
        fun clear() {
            sharedPreferences.edit {
                clear()
            }
        }

        /**
         * 检查是否包含某个key
         */
        fun contains(key: String): Boolean {
            return sharedPreferences.contains(key)
        }

        /**
         * 获取所有键值对
         */
        fun getAll(): Map<String, *> {
            return sharedPreferences.all
        }
    }
}