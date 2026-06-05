package app.allever.android.lib.network.core.repository

import app.allever.android.lib.network.core.Network
import app.allever.android.lib.network.core.engine.HttpMethod
import app.allever.android.lib.network.core.engine.NetRequest
import app.allever.android.lib.network.core.exception.ExceptionHandler
import app.allever.android.lib.network.core.exception.NetworkException
import app.allever.android.lib.network.core.response.IBaseResponse
import java.lang.reflect.Field

/**
 * Repository 基类 — 统一异常封装，永不抛异常
 *
 * 核心能力：
 * 1. **统一异常处理**：所有网络错误自动转换为 BaseResponse 失败实例，调用方无需 try-catch
 * 2. **反射实例化**：失败时通过反射创建 baseResponseClass 实例，填充 errorCode/errorMsg
 * 3. **便捷方法**：提供 get/post/put/delete 快捷方法，内部委托给 [Network]
 *
 * 使用方式：
 * ```kotlin
 * object WanAndroidRepository : BaseRepository() {
 *     suspend fun getBanner(): BaseResponse<List<BannerData>> =
 *         get("/banner/json")
 *
 *     suspend fun login(username: String, password: String): BaseResponse<TokenData> =
 *         post("/user/login", mapOf("username" to username, "password" to password))
 * }
 *
 * // 调用 — 永远安全，无需 try-catch
 * val resp = WanAndroidRepository.getBanner()
 * if (resp.errorCode == 0) { showBanners(resp.data) }
 * else { toast(resp.errorMsg) }
 * ```
 */
abstract class BaseRepository {

    // ==================== 核心请求方法 ====================

    /**
     * 统一请求方法 — 永不抛异常
     *
     * 执行 [block] 获取 Result<T>：
     * - **成功**：直接返回解析后的响应对象（T 必须实现 [IBaseResponse]）
     * - **失败**：通过反射创建失败响应实例（code=-1, msg=错误信息），返回非 null
     *
     * @param T 响应类型，必须实现 [IBaseResponse]
     * @param block 实际的请求逻辑，通常调用 [Network.get] / [Network.post] 等
     * @return 永不返回 null（失败时返回填充了错误信息的响应实例）
     */
    protected suspend fun <T : IBaseResponse> request(block: suspend () -> Result<T>): T {
        return try {
            val result = block()
            result.getOrNull() ?: createFailureResponse("响应数据为空")
        } catch (e: Exception) {
            createFailureResponse(e)
        }
    }

    /**
     * 统一请求方法（带自定义请求构建）
     *
     * @param method HTTP 方法
     * @param path 接口路径
     * @param body 请求体（POST/PUT 时使用）
     * @param requestBlock 请求构建（header、params 等）
     * @return 永不返回 null 的响应实例
     */
    protected suspend fun <T : IBaseResponse> request(
        method: HttpMethod,
        path: String,
        body: Any? = null,
        requestBlock: (NetRequest.Builder.() -> Unit)? = null
    ): T = request {
        @Suppress("UNCHECKED_CAST")
        when (method) {
            HttpMethod.GET -> Network.get<T>(path, block = requestBlock)
            HttpMethod.POST -> Network.post<T>(path, body, block = requestBlock)
            HttpMethod.PUT -> Network.put<T>(path, body, block = requestBlock)
            HttpMethod.DELETE -> Network.delete<T>(path, block = requestBlock)
            else -> Network.get<T>(path, block = requestBlock)
        }
    }

    // ==================== 快捷方法 ====================

    /** GET 请求 */
    protected suspend fun <T : IBaseResponse> get(
        path: String,
        block: (NetRequest.Builder.() -> Unit)? = null
    ): T = request(HttpMethod.GET, path, requestBlock = block)

    /** POST 请求 */
    protected suspend fun <T : IBaseResponse> post(
        path: String,
        body: Any? = null,
        block: (NetRequest.Builder.() -> Unit)? = null
    ): T = request(HttpMethod.POST, path, body, block)

    /** PUT 请求 */
    protected suspend fun <T : IBaseResponse> put(
        path: String,
        body: Any? = null,
        block: (NetRequest.Builder.() -> Unit)? = null
    ): T = request(HttpMethod.PUT, path, body, block)

    /** DELETE 请求 */
    protected suspend fun <T : IBaseResponse> delete(
        path: String,
        block: (NetRequest.Builder.() -> Unit)? = null
    ): T = request(HttpMethod.DELETE, path, requestBlock = block)

    // ==================== 失败响应创建 ====================

    /**
     * 创建失败响应实例（通过反射）
     *
     * 使用 [Network.config.baseResponseClass] 配置的类进行反射实例化，
     * 设置 errorCode 和 errorMsg 字段。
     *
     * 反射策略（按优先级）：
     * 1. 尝试无参构造函数创建实例
     * 2. 通过字段赋值设置 errorCode、errorMsg
     * 3. 如果反射失败，打印警告并尝试返回默认值
     *
     * @param message 错误信息
     * @return 填充了错误信息的响应实例
     */
    @Suppress("UNCHECKED_CAST")
    private fun <T : IBaseResponse> createFailureResponse(message: String): T {
        val clazz = Network.config.baseResponseClass
        if (clazz == null) {
            throw IllegalStateException(
                "未配置 baseResponseClass，无法创建失败响应。" +
                "请在 Network.init { baseResponseClass(BaseResponse::class.java) } 中配置"
            )
        }

        return try {
            // 1. 创建实例（优先找无参构造）
            val instance = createInstance(clazz)

            // 2. 设置 errorCode 字段
            setFieldValue(instance, "code", -1)
                || setFieldValue(instance, "errorCode", -1)
                || setFieldValue(instance, "errCode", -1)

            // 3. 设置 errorMsg 字段
            setFieldValue(instance, "msg", message)
                || setFieldValue(instance, "message", message)
                || setFieldValue(instance, "errorMsg", message)
                || setFieldValue(instance, "errorMessage", message)

            instance as T
        } catch (e: Exception) {
            throw RuntimeException("反射创建失败响应失败: ${e.message}", e)
        }
    }

    /**
     * 从 Exception 创建失败响应
     * 提取 NetworkException 的 displayMessage 作为错误信息
     */
    private fun <T : IBaseResponse> createFailureResponse(exception: Exception): T {
        val networkException = ExceptionHandler.handle(exception)
        val message = if (networkException is NetworkException) {
            networkException.displayMessage
        } else {
            exception.message ?: "未知错误"
        }
        return createFailureResponse(message)
    }

    // ==================== 反射工具方法 ====================

    companion object {
        /**
         * 通过反射创建实例
         * 策略：优先使用无参构造函数
         */
        internal fun createInstance(clazz: Class<*>): Any {
            // 尝试无参构造
            return try {
                clazz.getDeclaredConstructor().apply { isAccessible = true }.newInstance()
            } catch (e: NoSuchMethodException) {
                // 尝试查找参数全有默认值的合成构造函数
                val constructors = clazz.declaredConstructors
                if (constructors.isNotEmpty()) {
                    constructors[0].apply { isAccessible = true }.newInstance(*emptyArray())
                } else {
                    throw IllegalArgumentException("${clazz.name} 无可用构造函数")
                }
            }
        }

        /**
         * 通过反射设置字段值
         * 支持自身声明的字段和父类字段
         *
         * @param instance 目标实例
         * @param fieldName 字段名
         * @param value 要设置的值
         * @return 是否设置成功
         */
        internal fun setFieldValue(instance: Any, fieldName: String, value: Any?): Boolean {
            var clazz: Class<*>? = instance::class.java
            while (clazz != null) {
                try {
                    val field: Field = clazz.getDeclaredField(fieldName)
                    field.isAccessible = true

                    // 处理基础类型转换
                    val finalValue = when {
                        field.type == Int::class.javaPrimitiveType && value is Number -> value.toInt()
                        field.type == Long::class.javaPrimitiveType && value is Number -> value.toLong()
                        field.type == Double::class.javaPrimitiveType && value is Number -> value.toDouble()
                        field.type == Float::class.javaPrimitiveType && value is Number -> value.toFloat()
                        else -> value
                    }

                    field.set(instance, finalValue)
                    return true
                } catch (_: NoSuchFieldException) {
                    clazz = clazz.superclass
                } catch (_: Exception) {
                    return false
                }
            }
            return false
        }

        /**
         * 通过反射获取字段值
         */
        @Suppress("UNCHECKED_CAST")
        internal fun <T> getFieldValue(instance: Any, fieldName: String): T? {
            var clazz: Class<*>? = instance::class.java
            while (clazz != null) {
                try {
                    val field: Field = clazz.getDeclaredField(fieldName)
                    field.isAccessible = true
                    return field.get(instance) as? T
                } catch (_: NoSuchFieldException) {
                    clazz = clazz.superclass
                } catch (_: Exception) {
                    return null
                }
            }
            return null
        }
    }
}
