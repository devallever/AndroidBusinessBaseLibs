package app.allever.android.lib.store.engine.datastore

import android.content.Context
import android.content.ContextWrapper
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.floatPreferencesKey
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.core.stringSetPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import app.allever.android.lib.core.helper.GsonHelper
import app.allever.android.lib.store.core.IStorageEngine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.runBlocking
import java.util.concurrent.ConcurrentHashMap

/**
 * DataStore (Preferences) 引擎实现
 *
 * 基于 Jetpack DataStore Preferences，提供异步、一致性、事务性的存储能力。
 * 内部通过 runBlocking 将协程操作转为同步调用，对外保持与 [IStorageEngine] 一致的同步接口。
 *
 * 使用方式：
 * ```
 * Storage.init { DataStoreEngine() }
 * // 或多实例
 * val store = StorageKit.get("cache") { DataStoreEngine() }
 * ```
 */
class DataStoreEngine : IStorageEngine {

    private var dataStoreName: String? = null

    override fun init(context: Context, name: String) {
        dataStoreName = name.ifEmpty { "default_datastore" }
        // 确保全局缓存中已创建该名称对应的 DataStore 单例
        DataStoreCache.getOrCreate(context.applicationContext, dataStoreName!!)
    }

    private fun requireDataStore(): DataStore<Preferences> {
        val name = checkNotNull(dataStoreName) { "DataStoreEngine 未初始化，请先调用 init()" }
        return DataStoreCache.get(name)
            ?: throw IllegalStateException("DataStore '$name' 不存在，请先调用 init()")
    }

    private inline fun <T> blockingGet(crossinline block: suspend () -> T): T =
        runBlocking { block() }

    // ==================== 写入 ====================

    override fun putString(key: String, value: String?) {
        if (value != null) {
            blockingGet { requireDataStore().edit { it[stringPreferencesKey(key)] = value } }
        } else {
            remove(key)
        }
    }

    override fun putInt(key: String, value: Int) {
        blockingGet { requireDataStore().edit { it[intPreferencesKey(key)] = value } }
    }

    override fun putLong(key: String, value: Long) {
        blockingGet { requireDataStore().edit { it[longPreferencesKey(key)] = value } }
    }

    override fun putFloat(key: String, value: Float) {
        blockingGet { requireDataStore().edit { it[floatPreferencesKey(key)] = value } }
    }

    override fun putBoolean(key: String, value: Boolean) {
        blockingGet { requireDataStore().edit { it[booleanPreferencesKey(key)] = value } }
    }

    override fun putStringSet(key: String, value: Set<String>?) {
        if (value != null) {
            blockingGet { requireDataStore().edit { it[stringSetPreferencesKey(key)] = value } }
        } else {
            remove(key)
        }
    }

    override fun putAll(map: Map<String, Any?>) {
        blockingGet {
            requireDataStore().edit { prefs ->
                map.forEach { (key, value) ->
                    when (value) {
                        is String -> prefs[stringPreferencesKey(key)] = value
                        is Int -> prefs[intPreferencesKey(key)] = value
                        is Long -> prefs[longPreferencesKey(key)] = value
                        is Float -> prefs[floatPreferencesKey(key)] = value
                        is Boolean -> prefs[booleanPreferencesKey(key)] = value
                        is Set<*> -> {
                            @Suppress("UNCHECKED_CAST")
                            prefs[stringSetPreferencesKey(key)] = value as Set<String>
                        }

                        null -> prefs.remove(stringPreferencesKey(key))
                    }
                }
            }
        }
    }

    // ==================== 读取 ====================

    override fun getString(key: String, defaultValue: String?): String? =
        blockingGet {
            requireDataStore().data.map { it[stringPreferencesKey(key)] ?: defaultValue }.first()
        }

    override fun getInt(key: String, defaultValue: Int): Int =
        blockingGet {
            requireDataStore().data.map { it[intPreferencesKey(key)] ?: defaultValue }.first()
        }

    override fun getLong(key: String, defaultValue: Long): Long =
        blockingGet {
            requireDataStore().data.map { it[longPreferencesKey(key)] ?: defaultValue }.first()
        }

    override fun getFloat(key: String, defaultValue: Float): Float =
        blockingGet {
            requireDataStore().data.map { it[floatPreferencesKey(key)] ?: defaultValue }.first()
        }

    override fun getBoolean(key: String, defaultValue: Boolean): Boolean =
        blockingGet {
            requireDataStore().data.map { it[booleanPreferencesKey(key)] ?: defaultValue }.first()
        }

    override fun getStringSet(key: String, defaultValue: Set<String>?): Set<String>? =
        blockingGet {
            requireDataStore().data.map { it[stringSetPreferencesKey(key)] ?: defaultValue }.first()
        }

    // ==================== 对象读写 ====================

    override fun putObject(key: String, value: Any?) {
        val json = if (value != null) GsonHelper.toJson(value) else null
        putString(key, json)
    }

    override fun <T : Any> getObject(key: String, clazz: Class<T>): T? {
        val json = getString(key) ?: return null
        return runCatching { GsonHelper.fromJson(json, clazz) }.getOrNull()
    }

    // ==================== 删除 & 清空 ====================

    override fun remove(vararg keys: String) {
        blockingGet {
            requireDataStore().edit { prefs ->
                keys.forEach { key ->
                    prefs.remove(stringPreferencesKey(key))
                }
            }
        }
    }

    override fun clear() {
        blockingGet { requireDataStore().edit { it.clear() } }
    }

    // ==================== 查询 ====================

    override fun contains(key: String): Boolean =
        blockingGet {
            requireDataStore().data.map { it.contains(stringPreferencesKey(key)) }.first()
        }

    override val allKeys: Set<String>
        get() = blockingGet {
            requireDataStore().data.map { preferences ->
                preferences.asMap().keys.mapNotNull { key ->
                    key.name
                }.toSet()
            }.first()
        }

    // ==================== 生命周期 ====================

    override fun destroy() {
        // DataStore 由全局缓存管理，不在此处销毁
        dataStoreName = null
    }

    /**
     * 全局 DataStore 单例缓存
     *
     * 确保同一 name 只创建一个 DataStore 实例，
     * 避免 "multiple DataStores active for the same file" 崩溃。
     */
    private object DataStoreCache {

        private val cache = ConcurrentHashMap<String, DataStore<Preferences>>()
        private val lock = Any()

        fun getOrCreate(context: Context, name: String): DataStore<Preferences> {
            return cache.getOrPut(name) {
                synchronized(lock) {
                    cache.getOrPut(name) {
                        Holder(context, name).dataStore
                    }
                }
            }
        }

        fun get(name: String): DataStore<Preferences>? = cache[name]

        /** 内部 ContextWrapper 用于挂载 preferencesDataStore 委托 */
        private class Holder(context: Context, name: String) : ContextWrapper(context.applicationContext) {
            val dataStore: DataStore<Preferences> by preferencesDataStore(name = name)
        }
    }
}
