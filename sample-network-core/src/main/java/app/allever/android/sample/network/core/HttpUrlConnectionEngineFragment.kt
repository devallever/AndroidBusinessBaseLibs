package app.allever.android.sample.network.core

import android.util.Log
import app.allever.android.lib.common.ListFragment
import app.allever.android.lib.common.ListViewModel
import app.allever.android.lib.common.adapter.TextClickAdapter
import app.allever.android.lib.common.adapter.bean.TextClickItem
import app.allever.android.lib.common.databinding.FragmentListBinding
import app.allever.android.lib.core.ext.log
import app.allever.android.lib.core.ext.logE
import app.allever.android.lib.core.ext.toJson
import app.allever.android.lib.core.ext.toast
import app.allever.android.lib.network.core.NetCore
import app.allever.android.lib.network.core.engine.NetCall
import app.allever.android.lib.network.core.engine.NetCallback
import app.allever.android.lib.network.core.engine.HttpMethod
import app.allever.android.lib.network.core.engine.NetResponse
import app.allever.android.lib.network.core.exception.NetworkException
import app.allever.android.lib.network.engine.huc.UrlConnectionConfig
import app.allever.android.lib.network.engine.huc.UrlConnectionEngine
import app.allever.android.sample.network.core.repository.WanAndroidRepository
import com.chad.library.adapter.base.BaseQuickAdapter
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

/**
 * HttpURLConnection 引擎使用示例
 *
 * 演示内容：
 * 1. 初始化配置（选择 HUC 引擎）
 * 2. 自定义响应体（注解方式适配字段名）
 * 3. GET / POST 请求
 * 4. 错误处理（网络异常 + 业务错误）
 * 5. 字段名映射配置
 */
class HttpUrlConnectionEngineFragment :
    ListFragment<FragmentListBinding, ListViewModel, TextClickItem>() {

    override fun getAdapter(): BaseQuickAdapter<TextClickItem, *> = TextClickAdapter()

    override fun getList(): MutableList<TextClickItem> = mutableListOf(
        // ==================== 初始化示例 ====================
        TextClickItem("1. 初始化 Network (HUC引擎)") {
            initNetwork()
        },

        // ==================== 基础请求示例 ====================
        TextClickItem("2. GET 请求 - 获取Banner") {
            requestGetUser()
        },
        TextClickItem("3. 异步请求 - 回调方式 (enqueue)") {
            requestAsyncCallback()
        },
        TextClickItem("4. 异步请求 - 协程方式 (await)") {
            requestAsyncAwait()
        },
        TextClickItem("5. 异步请求 - 取消请求 (cancel)") {
            requestAsyncCancel()
        },

        // ==================== Repository 层封装示例 ====================
        TextClickItem("6. Repository - GET 获取Banner (永不抛异常)") {
            requestRepoGetBanner()
        },

        // ==================== 高级功能示例 ====================
        TextClickItem("9. 查看当前引擎信息") {
            showEngineInfo()
        }
    )

    // ==================== 数据模型 ====================

    /**
     * 用户数据
     */
    data class User(val id: String, val name: String, val email: String)

    /**
     * 登录请求体
     */
    data class LoginReq(val username: String, val password: String)

    /**
     * Token 数据
     */
    data class TokenData(val accessToken: String, val refreshToken: String)

    // BaseResponse<T> / BannerData 已提取到顶层 Models.kt


    // ==================== 示例方法 ====================

    /**
     * 1. 初始化 Network（使用 HttpURLConnection 引擎）
     */
    private fun initNetwork() {
        if (NetCore.isInitialized) {
            toast("Network 已初始化，当前引擎: ${NetCore.currentEngine()}")
            return
        }

        NetCore.init {
            // 使用公开测试 API
            baseUrl("https://www.wanandroid.com")

            // 选择 HttpURLConnection 引擎
            engine(UrlConnectionEngine.ENGINE_NAME) {
                // HUC 专属配置
                connectTimeout(10_000)
                readTimeout(15_000)
                (this as? UrlConnectionConfig)?.apply {
                    followRedirects(true)
                    keepAlive(true)
                }
            }

            successCode(0)

            // 公共请求头
            header("Accept", "application/json")
            header("App-Version", "1.0.0")

            // 启用日志
            enableLog(true)

            // 设置统一业务响应类型
            responseClass(BaseResponse::class.java)

            // 全局错误处理
//            onError { exception, _ ->
//                Log.e("HUC-Sample", "全局错误: [${exception.code}] ${exception.displayMessage}")
//            }
        }

        toast("初始化完成！引擎: ${NetCore.currentEngine()}")
        Log.i("HUC-Sample", "Network 已初始化，引擎: ${NetCore.currentEngine()}")
    }

    /**
     * 2. GET 请求示例（协程方式 - 高层封装）
     */
    private fun requestGetUser() {
        checkAndInit()

        CoroutineScope(Dispatchers.Main).launch {
            try {
                toast("正在发起 GET 请求...")

                val result = NetCore.get<BaseResponse<List<BannerData>>>("banner/json")
                if (result.isSuccess) {
                    val resp = result.getOrNull()
                    if (resp != null && resp.errorCode == 0) {
                        val data = resp.data
                        if (data != null) {
                            log("HUC-Sample", "response = ${data.toJson()}")
                            toast("GET 成功！response = ${data.toJson()}")
                        } else {
                            toast("GET 成功但 data 为空")
                        }
                    } else {
                        logE("业务失败: code=${resp?.errorCode}, msg=${resp?.errorMsg}")
                        toast("业务失败: code=${resp?.errorCode}, msg=${resp?.errorMsg}")
                    }
                } else {
                    handleNetworkError(result.exceptionOrNull()!!)
                }
            } catch (e: Exception) {
                toast("请求异常: ${e.message}")
            }
        }
    }

    // ==================== 异步请求示例 ====================

    /** 保存当前 Call 引用，用于取消演示 */
    private var currentCall: NetCall? = null

    /**
     * 3. 异步请求 - 回调方式 (enqueue)
     *
     * 使用 Network.newCall() 创建 NetCall，通过 enqueue 回调获取结果
     * 适用于：不需要协程的场景、传统回调模式
     */
    private fun requestAsyncCallback() {
        checkAndInit()

        toast("正在发起异步请求 (enqueue)...")

        // 1. 创建 NetCall（不立即执行）
        currentCall = NetCore.newCall(HttpMethod.GET, "banner/json") {
            header("X-Request-Type", "async-callback")
        }

        // 2. 通过 enqueue 异步执行，结果在回调中返回
        currentCall!!.enqueue(object : NetCallback {
            override fun onSuccess(response: NetResponse) {
                log("HUC-Sample", "enqueue 成功! HTTP ${response.code}, 耗时 ${response.elapsedMs}ms")
                log("HUC-Sample", "响应体: ${String(response.body ?: ByteArray(0))}")

                // 切回主线程更新 UI
                CoroutineScope(Dispatchers.Main).launch {
                    toast("enqueue 回调成功！HTTP ${response.code}, 耗时 ${response.elapsedMs}ms")
                }

                // 手动反序列化业务数据
                response.body?.let { bytes ->
                    @Suppress("UNCHECKED_CAST")
                    val resp = NetCore.config.converter.convert(
                        bytes,
                        BaseResponse::class.java
                    ) as? BaseResponse<*>
                    if (resp != null && resp.errorCode == 0) {
                        log("HUC-Sample", "Banner 数据: ${resp.data?.toJson()}")
                    }
                }
            }

            override fun onFailure(exception: Exception) {
                logE("HUC-Sample", "enqueue 失败: ${exception.message}")

                CoroutineScope(Dispatchers.Main).launch {
                    handleNetworkError(exception)
                }
            }
        })
    }

    /**
     * 4. 异步请求 - 协程方式 (await)
     *
     * 使用 NetCall.await() 挂起函数，结合协程使用
     * 优点：自动取消（协程取消时）、代码更简洁
     */
    private fun requestAsyncAwait() {
        checkAndInit()

        CoroutineScope(Dispatchers.Main).launch {
            try {
                toast("正在发起异步请求 (await)...")

                // 1. 创建 NetCall
                val call = NetCore.newCall(HttpMethod.GET, "banner/json") {
                    header("X-Request-Type", "async-await")
                }

                // 2. await 挂起等待结果（协程取消时自动 cancel）
                val response = call.await()

                log("HUC-Sample", "await 成功! HTTP ${response.code}, 耗时 ${response.elapsedMs}ms")

                // 3. 反序列化业务数据
                val parsed = response.toObject(NetCore.config.converter, BaseResponse::class.java)
                    as? BaseResponse<*>

                if (parsed != null && parsed.errorCode == 0) {
                    log("HUC-Sample", "Banner 数据: ${parsed.data?.toJson()}")
                    toast("await 成功！HTTP ${response.code}, 耗时 ${response.elapsedMs}ms")
                } else {
                    toast("业务失败: code=${parsed?.errorCode}, msg=${parsed?.errorMsg}")
                }
            } catch (e: Exception) {
                handleNetworkError(e)
            }
        }
    }

    /**
     * 5. 异步请求 - 取消演示 (cancel)
     *
     * 展示如何主动取消一个正在执行的请求
     */
    private fun requestAsyncCancel() {
        checkAndInit()

        toast("发起请求后 500ms 自动取消...")

        // 1. 发起一个较慢的请求
        currentCall = NetCore.newCall(HttpMethod.GET, "banner/json") {
            connectTimeout(30_000)   // 设置较长超时，确保请求还在进行中
            readTimeout(30_000)
        }

        // 2. 通过 enqueue 执行
        currentCall!!.enqueue(object : NetCallback {
            override fun onSuccess(response: NetResponse) {
                log("HUC-Sample", "请求未被取消，正常完成: HTTP ${response.code}")
                CoroutineScope(Dispatchers.Main).launch {
                    toast("请求正常完成（未取消）")
                }
            }

            override fun onFailure(exception: Exception) {
                logE("HUC-Sample", "请求被取消或失败: ${exception.javaClass.simpleName} - ${exception.message}")
                CoroutineScope(Dispatchers.Main).launch {
                    when (exception) {
                        is java.io.IOException -> toast("请求已取消！（IOException）")
                        else -> toast("请求失败: ${exception.message}")
                    }
                }
            }
        })

        // 3. 延迟 500ms 后取消请求
        android.os.Handler(android.os.Looper.getMainLooper()).postDelayed({
            currentCall?.let { call ->
                if (!call.isCanceled && !call.isExecuted) {
                    call.cancel()
                    log("HUC-Sample", "已调用 call.cancel(), isCanceled=${call.isCanceled}")
                } else {
                    toast("请求已完成，无法取消")
                }
            }
        }, 500)
    }

    // ==================== Repository 层封装示例 ====================

    /**
     * 6. Repository - GET 获取Banner
     *
     * 核心优势：**永不抛异常，无需 try-catch**
     *
     * 对比 Network.get()：
     * - Network.get() 返回 Result<T>，需要 isSuccess / getOrNull() / exceptionOrNull()
     * - Repository.get() 直接返回 BaseResponse<T>，只需判断 errorCode
     */
    private fun requestRepoGetBanner() {
        checkAndInit()

        CoroutineScope(Dispatchers.Main).launch {
            // ✅ 无需 try-catch，直接用！
            val resp = WanAndroidRepository.getBanner()

            if (resp.isSuccess()) {
                log("HUC-Sample", "Repo-GET 成功! data = ${resp.data?.toJson()}")
                toast("Repo-GET 成功! 共 ${resp.data?.size} 条 Banner")
            } else {
                logE("HUC-Sample", "Repo-GET 失败: code=${resp.errorCode}, msg=${resp.errorMsg}")
                toast("Repo-GET 失败: ${resp.errorMsg}")
            }
        }
    }

    /**
     * 9. 显示当前引擎信息
     */
    private fun showEngineInfo() {
        val info = buildString {
            appendLine("=== Network 引擎信息 ===")
            appendLine("引擎名称: ${NetCore.currentEngine()}")
            appendLine("已初始化: ${NetCore.isInitialized}")
            if (NetCore.isInitialized) {
                appendLine("baseUrl: ${NetCore.config.baseUrl}")
                appendLine("successCode: ${NetCore.config.successCode}")
                appendLine("responseClass: ${NetCore.config.responseClass?.simpleName}")
                appendLine("baseResponseClass: ${NetCore.config.baseResponseClass?.simpleName}")
                appendLine("拦截器数: ${NetCore.config.interceptors.size}")
                appendLine("公共头数: ${NetCore.config.headers.size}")
                appendLine("日志开关: ${NetCore.config.logEnabled}")
            }
        }
        Log.i("HUC-Sample", info)
        toast(info.lines().first())
    }

    // ==================== 工具方法 ====================

    /**
     * 统一的网络异常处理
     */
    private fun handleNetworkError(exception: Throwable) {
        val networkException = exception as? NetworkException ?: exception

        when (networkException) {
            is NetworkException.TimeoutError -> {
                toast("连接超时，请检查网络后重试")
            }
            is NetworkException.ConnectError -> {
                toast("无法连接到服务器")
            }
            is NetworkException.NoNetworkError -> {
                toast("当前无网络连接")
            }
            is NetworkException.SslError -> {
                toast("安全证书验证失败")
            }
            is NetworkException.ParseError -> {
                toast("数据解析异常: ${networkException.detail}")
            }
            is NetworkException.HttpError -> {
                toast("HTTP 错误 [${networkException.code}]: ${networkException.displayMessage}")
            }
            is NetworkException.BizError -> {
                toast("业务错误 [${networkException.code}]: ${networkException.bizMsg}")
            }
            is NetworkException.CanceledError -> {
                // 取消不提示
                Log.d("HUC-Sample", "请求被取消")
            }
            is NetworkException.UnknownError -> {
                toast("未知错误: ${networkException.message}")
            }
            else -> {
                toast("请求失败: ${networkException.message}")
            }
        }
    }

    /**
     * 确保已初始化
     */
    private fun checkAndInit() {
        if (!NetCore.isInitialized) {
            initNetwork()
        }
    }
}
