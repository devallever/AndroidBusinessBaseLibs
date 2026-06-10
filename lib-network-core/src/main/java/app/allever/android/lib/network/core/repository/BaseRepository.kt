//package app.allever.android.lib.network.core.repository
//
//import app.allever.android.lib.network.core.NetCore
//import app.allever.android.lib.network.core.engine.HttpMethod
//import app.allever.android.lib.network.core.engine.NetRequest
//import app.allever.android.lib.network.core.response.IBaseResponse
//import app.allever.android.lib.network.core.util.FailureResponseFactory
//import app.allever.android.lib.network.core.util.NetLogger
//
///**
// * Repository 基类 — 统一异常封装，永不抛异常
// *
// * 核心能力：
// * 1. **统一异常处理**：所有网络错误自动转换为 BaseResponse 失败实例，调用方无需 try-catch
// * 2. **反射实例化**：失败时通过反射创建 baseResponseClass 实例，填充 errorCode/errorMsg
// * 3. **便捷方法**：提供 get/post/put/delete 快捷方法，内部委托给 [NetCore]
// *
// * 使用方式：
// * ```kotlin
// * object WanAndroidRepository : BaseRepository() {
// *     suspend fun getBanner(): BaseResponse<List<BannerData>> =
// *         get("/banner/json")
// *
// *     suspend fun login(username: String, password: String): BaseResponse<TokenData> =
// *         post("/user/login", mapOf("username" to username, "password" to password))
// * }
// *
// * // 调用 — 永远安全，无需 try-catch
// * val resp = WanAndroidRepository.getBanner()
// * if (resp.errorCode == 0) { showBanners(resp.data) }
// * else { toast(resp.errorMsg) }
// * ```
// */
//@Deprecated("请使用 NetCore.get/post/put/delete")
//abstract class BaseRepository {
//
//    // ==================== 核心请求方法 ====================
//
//    /**
//     * 统一请求方法 — 永不抛异常
//     *
//     * 执行 [block] 获取业务响应：
//     * - **成功**：直接返回解析后的响应对象（T 必须实现 [IBaseResponse]）
//     * - **失败**：通过反射创建失败响应实例（code=-1, msg=错误信息），返回非 null
//     *
//     * @param T 响应类型，必须实现 [IBaseResponse]
//     * @param block 实际的请求逻辑，通常调用 [NetCore.get] / [NetCore.post] 等（内部抛异常）
//     * @return 永不返回 null（失败时返回填充了错误信息的响应实例）
//     */
//    protected suspend fun <T : IBaseResponse> request(block: suspend () -> T): T {
//        return try {
//            block()
//        } catch (e: Exception) {
//            NetLogger.logE("${this::class.java.simpleName} 请求异常: ${e.message}")
//            createFailureResponse(e)
//        }
//    }
//
//    /**
//     * 统一请求方法（带自定义请求构建）
//     *
//     * @param method HTTP 方法
//     * @param path 接口路径
//     * @param body 请求体（POST/PUT 时使用）
//     * @param requestBlock 请求构建（header、params 等）
//     * @return 永不返回 null 的响应实例
//     */
//    protected suspend fun <T : IBaseResponse> request(
//        method: HttpMethod,
//        path: String,
//        body: Any? = null,
//        requestBlock: (NetRequest.Builder.() -> Unit)? = null
//    ): T = request {
//        @Suppress("UNCHECKED_CAST")
//        when (method) {
//            HttpMethod.GET -> NetCore.get<T>(path, block = requestBlock)
//            HttpMethod.POST -> NetCore.post<T>(path, body, block = requestBlock)
//            HttpMethod.PUT -> NetCore.put<T>(path, body, block = requestBlock)
//            HttpMethod.DELETE -> NetCore.delete<T>(path, block = requestBlock)
//            else -> NetCore.get<T>(path, block = requestBlock)
//        }
//    }
//
//    // ==================== 快捷方法 ====================
//
//    /** GET 请求 */
//    protected suspend fun <T : IBaseResponse> get(
//        path: String,
//        block: (NetRequest.Builder.() -> Unit)? = null
//    ): T = request(HttpMethod.GET, path, requestBlock = block)
//
//    /** POST 请求 */
//    protected suspend fun <T : IBaseResponse> post(
//        path: String,
//        body: Any? = null,
//        block: (NetRequest.Builder.() -> Unit)? = null
//    ): T = request(HttpMethod.POST, path, body, block)
//
//    /** PUT 请求 */
//    protected suspend fun <T : IBaseResponse> put(
//        path: String,
//        body: Any? = null,
//        block: (NetRequest.Builder.() -> Unit)? = null
//    ): T = request(HttpMethod.PUT, path, body, block)
//
//    /** DELETE 请求 */
//    protected suspend fun <T : IBaseResponse> delete(
//        path: String,
//        block: (NetRequest.Builder.() -> Unit)? = null
//    ): T = request(HttpMethod.DELETE, path, requestBlock = block)
//
//    // ==================== 失败响应创建（委托给 FailureResponseFactory）====================
//
//    /**
//     * 创建失败响应实例（通过反射）
//     */
//    @Suppress("UNCHECKED_CAST")
//    private fun <T : IBaseResponse> createFailureResponse(message: String): T {
//        val clazz = NetCore.config.baseResponseClass
//            ?: throw IllegalStateException(
//                "未配置 baseResponseClass，无法创建失败响应。" +
//                "请在 Network.init { baseResponseClass(BaseResponse::class.java) } 中配置"
//            )
//        return FailureResponseFactory.create(clazz, -1, message)
//    }
//
//    /**
//     * 从 Exception 创建失败响应
//     */
//    private fun <T : IBaseResponse> createFailureResponse(exception: Exception): T {
//        val clazz = NetCore.config.baseResponseClass
//            ?: throw IllegalStateException(
//                "未配置 baseResponseClass，无法创建失败响应。" +
//                "请在 Network.init { baseResponseClass(BaseResponse::class.java) } 中配置"
//            )
//        return FailureResponseFactory.create(clazz, exception)
//    }
//}
