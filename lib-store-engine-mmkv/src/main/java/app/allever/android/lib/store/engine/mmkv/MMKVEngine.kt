package app.allever.android.lib.store.engine.mmkv

import android.content.Context
import android.util.Log
import app.allever.android.lib.core.helper.GsonHelper
import app.allever.android.lib.core.store.IStoreEngine
import com.tencent.mmkv.MMKV

/**
 * MMKV 引擎实现
 *
 * 基于 Tencent MMKV，提供高性能的键值存储能力。
 * 支持：
 * - 高性能读写（mmap + 内存缓存）
 * - 多进程支持（可选）
 * - 数据加密（可选）
 *
 * 使用方式：
 * ```
 * Storage.init { MMKVEngine() }
 * // 或多实例
 * val store = StorageKit.get("cache") { MMKVEngine() }
 * ```
 */
class MMKVEngine : IStoreEngine {

    companion object {
        private const val DEFAULT_ID = "default_mmkv"
        private var initialized = false

        /** 全局初始化 MMKV（只需调用一次） */
        fun globalInit(context: Context) {
            if (!initialized) {
                val rootDir = MMKV.initialize(context)
                Log.d("MMKVEngine", "MMKV root dir: $rootDir")
                initialized = true
            }
        }
    }

    private var mmkv: MMKV? = null

    override fun init(context: Context, name: String) {
        // 确保 MMKV 已全局初始化
        globalInit(context)

        if (mmkv == null) {
            mmkv = MMKV.mmkvWithID(name.ifEmpty { DEFAULT_ID }, MMKV.MULTI_PROCESS_MODE)
        }
    }

    private fun requireMmkv(): MMKV {
        checkNotNull(mmkv) { "MMKVEngine 未初始化，请先调用 init()" }
        return mmkv!!
    }

    // ==================== 写入 ====================

    override fun putString(key: String, value: String?) {
        requireMmkv().putString(key, value)
    }

    override fun putInt(key: String, value: Int) {
        requireMmkv().putInt(key, value)
    }

    override fun putLong(key: String, value: Long) {
        requireMmkv().putLong(key, value)
    }

    override fun putFloat(key: String, value: Float) {
        requireMmkv().putFloat(key, value)
    }

    override fun putBoolean(key: String, value: Boolean) {
        requireMmkv().putBoolean(key, value)
    }

    override fun putStringSet(key: String, value: Set<String>?) {
        requireMmkv().putStringSet(key, value)
    }

    override fun putAll(map: Map<String, Any?>) {
        val kv = requireMmkv()
        map.forEach { (key, value) ->
            when (value) {
                is String -> kv.putString(key, value)
                is Int -> kv.putInt(key, value)
                is Long -> kv.putLong(key, value)
                is Float -> kv.putFloat(key, value)
                is Boolean -> kv.putBoolean(key, value)
                is Set<*> -> @Suppress("UNCHECKED_CAST") kv.putStringSet(
                    key,
                    value as Set<String>
                )

                null -> kv.removeValueForKey(key)
            }
        }
    }

    // ==================== 读取 ====================

    override fun getString(key: String, defaultValue: String?): String? =
        requireMmkv().getString(key, defaultValue)

    override fun getInt(key: String, defaultValue: Int): Int =
        requireMmkv().getInt(key, defaultValue)

    override fun getLong(key: String, defaultValue: Long): Long =
        requireMmkv().getLong(key, defaultValue)

    override fun getFloat(key: String, defaultValue: Float): Float =
        requireMmkv().getFloat(key, defaultValue)

    override fun getBoolean(key: String, defaultValue: Boolean): Boolean =
        requireMmkv().getBoolean(key, defaultValue)

    override fun getStringSet(key: String, defaultValue: Set<String>?): Set<String>? =
        requireMmkv().getStringSet(key, defaultValue)

    // ==================== 对象读写 ====================

    override fun putObject(key: String, value: Any?) {
        val json = if (value != null) GsonHelper.toJson(value) else null
        requireMmkv().putString(key, json)
    }

    override fun <T : Any> getObject(key: String, clazz: Class<T>): T? {
        val json = requireMmkv().getString(key, null) ?: return null
        return runCatching { GsonHelper.fromJson(json, clazz) }.getOrNull()
    }

    // ==================== 删除 & 清空 ====================

    override fun remove(vararg keys: String) {
        requireMmkv().removeValuesForKeys(keys)
    }

    override fun clear() {
        requireMmkv().clearAll()
    }

    // ==================== 查询 ====================

    override fun contains(key: String): Boolean = requireMmkv().containsKey(key)

    override val allKeys: Set<String>
        get() = requireMmkv().allKeys()?.toSet() ?: emptySet()

    // ==================== 生命周期 ====================

    override fun destroy() {
        mmkv?.close()
        mmkv = null
    }
}
