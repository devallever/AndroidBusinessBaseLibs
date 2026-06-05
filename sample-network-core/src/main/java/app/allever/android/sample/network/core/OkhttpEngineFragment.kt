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
import app.allever.android.lib.network.core.engine.HttpMethod
import app.allever.android.lib.network.core.engine.NetCall
import app.allever.android.lib.network.core.engine.NetCallback
import app.allever.android.lib.network.core.engine.NetResponse
import app.allever.android.lib.network.core.exception.NetworkException
import app.allever.android.lib.network.engine.okhttp.OkHttpConfig
import app.allever.android.lib.network.engine.okhttp.OkHttpEngine
import app.allever.android.sample.network.core.repository.WanAndroidRepository
import com.chad.library.adapter.base.BaseQuickAdapter
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

/**
 * OkHttp 引擎使用示例
 *
 * 演示内容：
 * 1. 初始化配置（选择 OkHttp 引擎）
 * 2. OkHttp 专属配置（连接池、协议、重试等）
 * 3. GET / POST 请求（协程方式）
 * 4. 异步请求 - 回调方式 (enqueue)
 * 5. 异步请求 - 协程方式 (await)
 * 6. 异步请求 - 取消请求 (cancel)
 * 7. Repository 层封装示例
 *
 * 与 HUC 引擎对比：切换引擎只需改 engine("okhttp")，其余代码完全一致。
 */
class OkhttpEngineFragment :
    ListFragment<FragmentListBinding, ListViewModel, TextClickItem>() {

    override fun getAdapter(): BaseQuickAdapter<TextClickItem, *> = TextClickAdapter()

    override fun getList(): MutableList<TextClickItem> = mutableListOf(
        // ==================== 初始化示例 ====================
        TextClickItem("1. 初始化 Network (OkHttp引擎)") {
            initNetwork()
        },

        // ==================== 基础请求示例 ====================
        TextClickItem("2. GET 请求 - 获取Banner") {
            requestGetBanner()
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

    /** 保存当前 Call 引用，用于取消演示 */
    private var currentCall: NetCall? = null

    // ==================== 示例方法 ====================

    /**
     * 1. 初始化 Network（使用 OkHttp 引擎）
     */
    private fun initNetwork() {
        if (NetCore.isInitialized) {
            toast("Network 已初始化，当前引擎: ${NetCore.currentEngine()}")
            return
        }

        NetCore.init {
            // 使用公开测试 API
            baseUrl("https://www.wanandroid.com")

            // 选择 OkHttp 引擎
            engine(OkHttpEngine.ENGINE_NAME) {
                connectTimeout(10_000)
                readTimeout(15_000)

                // OkHttp 专属配置
                (this as? OkHttpConfig)?.apply {
                    connectionPool(5, 5, java.util.concurrent.TimeUnit.MINUTES)
                    retryOnConnectionFailure(true)
//                    addInterceptor("LoggingInterceptor")
//                    addNetworkInterceptor("LoggingInterceptor")
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
        }

        toast("初始化完成！引擎: ${NetCore.currentEngine()}")
        Log.i("OkHttp-Sample", "Network 已初始化，引擎: ${NetCore.currentEngine()}")
    }

    /**
     * 2. GET 请求示例（协程方式 - 高层封装）
     */
    private fun requestGetBanner() {
        checkAndInit()

        CoroutineScope(Dispatchers.Main).launch {
            try {
                toast("正在发起 GET 请求...")

                val resp = NetCore.get<BaseResponse<List<BannerData>>>("banner/json")
                if (resp.isSuccess()) {
                    val data = resp.data
                    if (data != null) {
                        log("OkHttp-Sample", "response = ${data.toJson()}")
                        toast("GET 成功！response = ${data.toJson()}")
                    } else {
                        toast("GET 成功但 data 为空")
                    }
                } else {
                    logE("业务失败: code=${resp.errorCode}, msg=${resp.errorMsg}")
                    toast("业务失败: code=${resp.errorCode}, msg=${resp.errorMsg}")
                }
            } catch (e: Exception) {
                handleNetworkError(e)
            }
        }
    }

    // ==================== 异步请求示例 ====================

    /**
     * 3. 异步请求 - 回调方式 (enqueue)
     *
     * 底层直接使用 OkHttp 的异步机制，回调在 IO 线程触发
     */
    private fun requestAsyncCallback() {
        checkAndInit()

        toast("正在发起异步请求 (enqueue)...")

        currentCall = NetCore.newCall(HttpMethod.GET, "banner/json") {
            header("X-Request-Type", "okhttp-callback")
        }

        currentCall!!.enqueue(object : NetCallback {
            override fun onSuccess(response: NetResponse) {
                log("OkHttp-Sample", "enqueue 成功! HTTP ${response.code}, 耗时 ${response.elapsedMs}ms")
                log("OkHttp-Sample", "响应体: ${String(response.body ?: ByteArray(0))}")

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
                        log("OkHttp-Sample", "Banner 数据: ${resp.data?.toJson()}")
                    }
                }
            }

            override fun onFailure(exception: Exception) {
                logE("OkHttp-Sample", "enqueue 失败: ${exception.message}")

                CoroutineScope(Dispatchers.Main).launch {
                    handleNetworkError(exception)
                }
            }
        })
    }

    /**
     * 4. 异步请求 - 协程方式 (await)
     *
     * 利用 OkHttp 原生异步 + 协程挂起，协程取消时自动 cancel
     */
    private fun requestAsyncAwait() {
        checkAndInit()

        CoroutineScope(Dispatchers.Main).launch {
            try {
                toast("正在发起异步请求 (await)...")

                val call = NetCore.newCall(HttpMethod.GET, "banner/json") {
                    header("X-Request-Type", "okhttp-await")
                }

                val response = call.await()

                log("OkHttp-Sample", "await 成功! HTTP ${response.code}, 耗时 ${response.elapsedMs}ms")

                val parsed = response.toObject(NetCore.config.converter, BaseResponse::class.java)
                    as? BaseResponse<*>

                if (parsed != null && parsed.errorCode == 0) {
                    log("OkHttp-Sample", "Banner 数据: ${parsed.data?.toJson()}")
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
     * 取消操作直接委托给 OkHttp 原生的 Call.cancel()，
     * 比 HUC 的标志位方式更可靠（底层会关闭 Socket）
     */
    private fun requestAsyncCancel() {
        checkAndInit()

        toast("发起请求后 500ms 自动取消...")

        currentCall = NetCore.newCall(HttpMethod.GET, "banner/json") {
            connectTimeout(30_000)
            readTimeout(30_000)
        }

        currentCall!!.enqueue(object : NetCallback {
            override fun onSuccess(response: NetResponse) {
                log("OkHttp-Sample", "请求未被取消，正常完成: HTTP ${response.code}")
                CoroutineScope(Dispatchers.Main).launch {
                    toast("请求正常完成（未取消）")
                }
            }

            override fun onFailure(exception: Exception) {
                logE("OkHttp-Sample", "请求被取消或失败: ${exception.javaClass.simpleName} - ${exception.message}")
                CoroutineScope(Dispatchers.Main).launch {
                    when (exception) {
                        is java.io.IOException -> toast("请求已取消！（IOException）")
                        else -> toast("请求失败: ${exception.message}")
                    }
                }
            }
        })

        android.os.Handler(android.os.Looper.getMainLooper()).postDelayed({
            currentCall?.let { call ->
                if (!call.isCanceled && !call.isExecuted) {
                    call.cancel()
                    log("OkHttp-Sample", "已调用 call.cancel(), isCanceled=${call.isCanceled}")
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
     * 切换引擎后 Repository 代码无需任何修改 — 这就是引擎抽象的价值
     */
    private fun requestRepoGetBanner() {
        checkAndInit()

        CoroutineScope(Dispatchers.Main).launch {
            val resp = WanAndroidRepository.getBanner()

            if (resp.isSuccess()) {
                log("OkHttp-Sample", "Repo-GET 成功! data = ${resp.data?.toJson()}")
                toast("Repo-GET 成功! 共 ${resp.data?.size} 条 Banner")
            } else {
                logE("OkHttp-Sample", "Repo-GET 失败: code=${resp.errorCode}, msg=${resp.errorMsg}")
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
        Log.i("OkHttp-Sample", info)
        toast(info.lines().first())
    }

    // ==================== 工具方法 ====================

    private fun handleNetworkError(exception: Throwable) {
        val networkException = exception as? NetworkException ?: exception

        when (networkException) {
            is NetworkException.TimeoutError -> toast("连接超时，请检查网络后重试")
            is NetworkException.ConnectError -> toast("无法连接到服务器")
            is NetworkException.NoNetworkError -> toast("当前无网络连接")
            is NetworkException.SslError -> toast("安全证书验证失败")
            is NetworkException.ParseError -> toast("数据解析异常: ${networkException.detail}")
            is NetworkException.HttpError -> toast("HTTP 错误 [${networkException.code}]: ${networkException.displayMessage}")
            is NetworkException.BizError -> toast("业务错误 [${networkException.code}]: ${networkException.bizMsg}")
            is NetworkException.CanceledError -> Log.d("OkHttp-Sample", "请求被取消")
            is NetworkException.UnknownError -> toast("未知错误: ${networkException.message}")
            else -> toast("请求失败: ${networkException.message}")
        }
    }

    private fun checkAndInit() {
        if (!NetCore.isInitialized) {
            initNetwork()
        }
    }
}
