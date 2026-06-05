package app.allever.android.lib.network.core.response

import app.allever.android.lib.network.core.NetworkConfig
import app.allever.android.lib.network.core.response.ResponseCode
import app.allever.android.lib.network.core.response.ResponseData
import app.allever.android.lib.network.core.response.ResponseMsg

/**
 * 响应适配器 - 从任意类型的响应对象中提取 code / msg / data
 *
 * 三层提取策略，按优先级依次尝试：
 *
 * 1. **用户自定义提取器** (codeExtractor / msgExtractor / dataExtractor) — 最高优先级
 * 2. **注解标记** (@ResponseCode / @ResponseMsg / @ResponseData) — 推荐方式
 * 3. **默认字段名反射查找** — 兜底机制
 *
 * 所有方式都失败时使用默认值：
 * - code → defaultErrorCode (-1)
 * - msg → ""
 * - data → null
 */
object ResponseAdapter {

    /**
     * 从响应对象中提取业务状态码
     */
    fun extractCode(response: Any, config: NetworkConfig): Int {
        // 优先级1：用户自定义 code 提取器
        config.codeExtractor?.let { extractor ->
            return runCatching { extractor(response) }.getOrDefault(config.defaultErrorCode)
        }

        // 优先级2：注解
        findAnnotatedField<Int>(response, ResponseCode::class.java)?.let { return it }

        // 优先级3：按字段名列表反射查找
        findFieldByNames(response, config.codeFieldNames)?.let { value ->
            return when (value) {
                is Number -> value.toInt()
                is String -> value.toIntOrNull() ?: config.defaultErrorCode
                else -> config.defaultErrorCode
            }
        }

        return config.defaultErrorCode
    }

    /**
     * 从响应对象中提取业务消息
     */
    fun extractMsg(response: Any, config: NetworkConfig): String {
        config.msgExtractor?.let { extractor ->
            return runCatching { extractor(response) }.getOrElse { "" }
        }

        findAnnotatedField<String>(response, ResponseMsg::class.java)?.let { return it }

        return findFieldByNames(response, config.msgFieldNames)?.toString().orEmpty()
    }

    /**
     * 从响应对象中提取业务数据
     */
    fun <T> extractData(response: Any, config: NetworkConfig): T? {
        @Suppress("UNCHECKED_CAST")
        config.dataExtractor?.let { extractor ->
            return runCatching { extractor(response) as T? }.getOrDefault(null)
        }

        findAnnotatedField<Any>(response, ResponseData::class.java)?.let {
            @Suppress("UNCHECKED_CAST") return it as T?
        }

        @Suppress("UNCHECKED_CAST")
        return findFieldByNames(response, config.dataFieldNames) as? T?
    }

    // ==================== 内部反射工具 ====================

    /**
     * 通过注解查找字段值
     */
    private inline fun <reified T> findAnnotatedField(
        obj: Any,
        annotationClass: Class<out Annotation>
    ): T? {
        return try {
            var clazz: Class<*> = obj::class.java
            while (clazz != Any::class.java) {
                for (field in clazz.declaredFields) {
                    if (field.isAnnotationPresent(annotationClass)) {
                        field.isAccessible = true
                        @Suppress("UNCHECKED_CAST")
                        return field.get(obj) as? T
                    }
                }
                clazz = clazz.superclass ?: break
            }
            null
        } catch (_: Exception) {
            null
        }
    }

    /**
     * 按候选字段名列表依次尝试反射读取
     */
    private fun findFieldByNames(obj: Any, names: List<String>): Any? {
        if (names.isEmpty()) return null
        return try {
            var clazz: Class<*> = obj::class.java
            while (clazz != Any::class.java) {
                for (name in names) {
                    for (field in clazz.declaredFields) {
                        if (field.name == name) {
                            field.isAccessible = true
                            return field.get(obj)
                        }
                    }
                }
                clazz = clazz.superclass ?: break
            }
            null
        } catch (_: Exception) {
            null
        }
    }
}
