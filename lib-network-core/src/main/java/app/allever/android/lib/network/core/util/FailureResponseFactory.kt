package app.allever.android.lib.network.core.util

import app.allever.android.lib.network.core.exception.ExceptionHandler
import app.allever.android.lib.network.core.exception.NetworkException
import java.lang.reflect.Field

/**
 * 反射工具 — 失败响应实例创建
 *
 * 统一 NetCore 和 BaseRepository 中反射创建失败响应的逻辑，
 * 避免代码重复。
 *
 * 使用方式：
 * ```kotlin
 * // 通过 code + message 创建
 * val resp = FailureResponseFactory.create(BaseResponse::class.java, code = 404, message = "Not Found")
 *
 * // 从异常创建（自动提取 displayMessage 和错误码）
 * val resp = FailureResponseFactory.create(BaseResponse::class.java, exception)
 * ```
 */
object FailureResponseFactory {

    /**
     * 通过 code + message 创建失败响应实例
     *
     * @param clazz 响应类（需有无参构造函数）
     * @param code 错误码
     * @param message 错误信息
     * @return 填充了错误信息的响应实例
     */
    fun <T> create(clazz: Class<*>, code: Int, message: String): T {
        return try {
            val instance = createInstance(clazz)

            setFieldValue(instance, "code", code)
                || setFieldValue(instance, "errorCode", code)
                || setFieldValue(instance, "errCode", code)

            setFieldValue(instance, "msg", message)
                || setFieldValue(instance, "message", message)
                || setFieldValue(instance, "errorMsg", message)
                || setFieldValue(instance, "errorMessage", message)

            NetLogger.log("失败响应创建成功: ${clazz.simpleName}(code=$code, msg=$message)")

            @Suppress("UNCHECKED_CAST")
            instance as T
        } catch (e: Exception) {
            NetLogger.logE("反射创建失败响应异常: ${clazz.simpleName} → ${e.message}")
            throw RuntimeException("反射创建失败响应失败: ${e.message}", e)
        }
    }

    /**
     * 从异常创建失败响应实例
     * 自动从 NetworkException 提取错误码和 displayMessage
     */
    fun <T> create(clazz: Class<*>, exception: Exception): T {
        val networkException = ExceptionHandler.handle(exception)
        val displayMsg = if (networkException is NetworkException) {
            networkException.displayMessage
        } else {
            exception.message ?: "未知错误"
        }

        val errorCode = when (networkException) {
            is NetworkException.HttpError -> networkException.code
            is NetworkException.BizError -> networkException.code
            is NetworkException.ParseError -> -2
            is NetworkException.EmptyBodyError -> -1
            else -> -1
        }

        return create(clazz, errorCode, displayMsg)
    }

    // ==================== 反射工具方法 ====================

    /**
     * 通过无参构造函数创建实例
     */
    internal fun createInstance(clazz: Class<*>): Any {
        return try {
            val instance = clazz.getDeclaredConstructor().apply { isAccessible = true }.newInstance()
            NetLogger.log("反射实例化成功: ${clazz.simpleName}")
            instance
        } catch (e: NoSuchMethodException) {
            NetLogger.logE("${clazz.simpleName} 无参构造函数不存在，尝试其他构造函数")
            val constructors = clazz.declaredConstructors
            if (constructors.isNotEmpty()) {
                constructors[0].apply { isAccessible = true }.newInstance(*emptyArray())
            } else {
                throw IllegalArgumentException("${clazz.name} 无可用构造函数")
            }
        }
    }

    /**
     * 通过反射设置字段值（支持自身和父类）
     */
    internal fun setFieldValue(instance: Any, fieldName: String, value: Any?): Boolean {
        var c: Class<*>? = instance::class.java
        while (c != null) {
            try {
                val field: Field = c.getDeclaredField(fieldName)
                field.isAccessible = true

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
                c = c.superclass
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
        var c: Class<*>? = instance::class.java
        while (c != null) {
            try {
                val field: Field = c.getDeclaredField(fieldName)
                field.isAccessible = true
                return field.get(instance) as? T
            } catch (_: NoSuchFieldException) {
                c = c.superclass
            } catch (_: Exception) {
                return null
            }
        }
        return null
    }
}
