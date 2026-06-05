package app.allever.android.sample.network.core

import android.util.Log
import android.widget.Toast
import app.allever.android.lib.common.ListFragment
import app.allever.android.lib.common.ListViewModel
import app.allever.android.lib.common.adapter.TextClickAdapter
import app.allever.android.lib.common.adapter.bean.TextClickItem
import app.allever.android.lib.common.databinding.FragmentListBinding
import app.allever.android.lib.core.ext.log
import app.allever.android.lib.core.ext.logE
import app.allever.android.lib.core.ext.toJson
import app.allever.android.lib.core.ext.toast
import app.allever.android.lib.network.core.Network
import app.allever.android.lib.network.core.engine.EngineRegistry
import app.allever.android.lib.network.core.exception.NetworkException
import app.allever.android.lib.network.core.response.ResponseCode
import app.allever.android.lib.network.core.response.ResponseData
import app.allever.android.lib.network.core.response.ResponseMsg
import app.allever.android.lib.network.engine.huc.UrlConnectionConfig
import app.allever.android.lib.network.engine.huc.UrlConnectionEngine
import app.allever.android.lib.network.engine.huc.UrlConnectionEngine.Companion.ENGINE_NAME
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
        TextClickItem("2. GET 请求 - 获取用户信息") {
            requestGetUser()
        },
//        TextClickItem("3. POST 请求 - 登录") {
//            requestLogin()
//        },
//        TextClickItem("4. PUT 请求 - 更新用户") {
//            requestUpdateUser()
//        },
//        TextClickItem("5. DELETE 请求 - 删除资源") {
//            requestDelete()
//        },

        // ==================== 错误处理示例 ====================
//        TextClickItem("6. 模拟网络异常处理") {
//            simulateNetworkError()
//        },
//        TextClickItem("7. 模拟业务错误处理") {
//            simulateBizError()
//        },

        // ==================== 高级功能示例 ====================
        TextClickItem("8. 查看当前引擎信息") {
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

    // ==================== 自定义响应体（演示不同字段名）====================

    /**
     * 示例 A：标准格式 { code, msg, data }
     */
    data class BaseResponse<T>(
        @ResponseCode val errorCode: Int = -1,
        @ResponseMsg val errorMsg: String = "",
        @ResponseData val data: T? = null
    )

    data class BannerData(
        val desc: String,
        val id: Int,
        val imagePath: String,
        val isVisible: Int,
        val order: Int,
        val title: String,
        val type: Int,
        val url: String
    )


    // ==================== 示例方法 ====================

    /**
     * 1. 初始化 Network（使用 HttpURLConnection 引擎）
     */
    private fun initNetwork() {
        if (Network.isInitialized) {
            toast("Network 已初始化，当前引擎: ${Network.currentEngine()}")
            return
        }

        Network.init {
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

        toast("初始化完成！引擎: ${Network.currentEngine()}")
        Log.i("HUC-Sample", "Network 已初始化，引擎: ${Network.currentEngine()}")
    }

    /**
     * 2. GET 请求示例
     */
    private fun requestGetUser() {
        checkAndInit()

        CoroutineScope(Dispatchers.Main).launch {
            try {
                toast("正在发起 GET 请求...")

                val result = Network.get<BaseResponse<List<BannerData>>>("banner/json")
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

    /**
     * 8. 显示当前引擎信息
     */
    private fun showEngineInfo() {
        val info = buildString {
            appendLine("=== Network 引擎信息 ===")
            appendLine("引擎名称: ${Network.currentEngine()}")
            appendLine("已初始化: ${Network.isInitialized}")
            if (Network.isInitialized) {
                appendLine("baseUrl: ${Network.config.baseUrl}")
                appendLine("successCode: ${Network.config.successCode}")
                appendLine("responseClass: ${Network.config.responseClass?.simpleName}")
                appendLine("拦截器数: ${Network.config.interceptors.size}")
                appendLine("公共头数: ${Network.config.headers.size}")
                appendLine("日志开关: ${Network.config.logEnabled}")
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
        if (!Network.isInitialized) {
            initNetwork()
        }
    }
}
