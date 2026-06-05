package app.allever.android.lib.network.core.response

/**
 * 标记业务响应中的状态码字段
 *
 * 使用示例：
 * ```kotlin
 * data class MyResponse<T>(
 *     @ResponseCode val code: Int = -1,
 *     @ResponseMsg  val msg: String = "",
 *     @ResponseData val data: T? = null
 * )
 * ```
 */
@Target(AnnotationTarget.FIELD)
@Retention(AnnotationRetention.RUNTIME)
annotation class ResponseCode

/**
 * 标记业务响应中的消息字段
 */
@Target(AnnotationTarget.FIELD)
@Retention(AnnotationRetention.RUNTIME)
annotation class ResponseMsg

/**
 * 标记业务响应中的数据字段
 */
@Target(AnnotationTarget.FIELD)
@Retention(AnnotationRetention.RUNTIME)
annotation class ResponseData
