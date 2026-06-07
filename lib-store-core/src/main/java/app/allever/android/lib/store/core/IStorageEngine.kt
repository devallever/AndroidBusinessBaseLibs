package app.allever.android.lib.store.core

/**
 * 存储引擎接口
 *
 * 所有存储实现（SP、MMKV、DataStore 等）均需实现此接口。
 * 通过 [Storage.init] 可无缝切换底层存储方案，业务代码无需改动。
 */
interface IStorageEngine {

    // ========== 基础读写 ==========

    fun putString(key: String, value: String?)
    fun getString(key: String, defaultValue: String? = null): String?

    fun putInt(key: String, value: Int)
    fun getInt(key: String, defaultValue: Int = 0): Int

    fun putLong(key: String, value: Long)
    fun getLong(key: String, defaultValue: Long = 0L): Long

    fun putFloat(key: String, value: Float)
    fun getFloat(key: String, defaultValue: Float = 0f): Float

    fun putBoolean(key: String, value: Boolean)
    fun getBoolean(key: String, defaultValue: Boolean = false): Boolean

    fun putStringSet(key: String, value: Set<String>?)
    fun getStringSet(key: String, defaultValue: Set<String>? = null): Set<String>?

    // ========== 对象读写（JSON 序列化） ==========

    /**
     * 存储任意对象，内部通过 JSON 序列化为字符串存储
     * @param key 键
     * @param value 对象值，传 null 等价于删除该键
     */
    fun putObject(key: String, value: Any?)

    /**
     * 读取并反序列化为指定类型的对象
     * @param key 键
     * @param clazz 目标类型
     * @return 反序列化后的对象，不存在或反序列化失败返回 null
     */
    fun <T : Any> getObject(key: String, clazz: Class<T>): T?

    // ========== 批量操作 ==========

    /** 批量写入，value 支持 String / Int / Long / Float / Boolean / Set\<String\> */
    fun putAll(map: Map<String, Any?>)

    // ========== 删除 & 清空 ==========

    fun remove(vararg keys: String)

    fun clear()

    // ========== 查询 ==========

    fun contains(key: String): Boolean

    val allKeys: Set<String>

    // ========== 生命周期 ==========

    /**
     * 初始化引擎
     * @param context Application Context
     * @param name 存储名称（对应 SP 文件名 / MMKV ID / DataStore name 等）
     */
    fun init(context: android.content.Context, name: String)

    /** 销毁引擎，释放资源 */
    fun destroy()
}
