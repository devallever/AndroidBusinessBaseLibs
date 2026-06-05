package app.allever.android.lib.network.core.engine

/**
 * 响应体序列化转换器接口
 *
 * 负责将 HTTP 响应的 ByteArray 反序列化为目标类型 T
 *
 * 各序列化方案实现此接口：
 * - Gson → GsonConverter
 * - Moshi → MoshiConverter
 * - kotlinx.serialization → KotlinSerializationConverter
 */
interface ResponseConverter {

    /**
     * 将字节数组转换为目标类型
     * @param bytes 响应体字节
     * @param clazz 目标类型的 Class
     * @return 转换后的对象，失败返回 null
     */
    fun <T> convert(bytes: ByteArray, clazz: Class<T>): T?
}
