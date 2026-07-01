package com.clean.wood.data

import android.content.Context
import com.clean.wood.WoodApp

class KvManager private constructor() {
    companion object {
        val ins by lazy { KvManager() }
    }

    private val kvCache =
        WoodApp.context.getSharedPreferences("wood_global_kv", Context.MODE_PRIVATE)

    fun getBoolean(key: String, default: Boolean = false): Boolean {
        return kvCache.getBoolean(key, default)
    }

    fun putBoolean(key: String, value: Boolean) {
        kvCache.edit().putBoolean(key, value).apply()
    }

    fun getInt(key: String, default: Int = 0): Int {
        return kvCache.getInt(key, default)
    }

    fun putInt(key: String, value: Int) {
        kvCache.edit().putInt(key, value).apply()
    }

    fun getString(key: String, default: String = ""): String {
        return kvCache.getString(key, default) ?: default
    }

    fun putString(key: String, value: String) {
        kvCache.edit().putString(key, value).apply()
    }

}