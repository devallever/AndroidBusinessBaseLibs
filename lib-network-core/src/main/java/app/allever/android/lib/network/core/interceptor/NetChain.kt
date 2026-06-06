package app.allever.android.lib.network.core.interceptor

import app.allever.android.lib.core.ext.log
import app.allever.android.lib.network.core.NetCore
import app.allever.android.lib.network.core.engine.NetRequest
import app.allever.android.lib.network.core.engine.NetResponse
import app.allever.android.lib.network.core.util.NetLogger

/**
 * 拦截器链
 *
 * 管理拦截器的执行顺序，支持：
 * - 依次执行每个拦截器
 * - 通过 chain.proceed() 传递到下一个拦截器或引擎执行
 * - 支持短路（直接返回，不调用 proceed）
 */
class NetChain(
    private val interceptors: List<NetInterceptor>,
    private val index: Int = 0,
    private val engineExecute: (NetRequest) -> NetResponse
) {

    /** 当前请求（可通过 interceptors 修改） */
    var request: NetRequest? = null

    /**
     * 执行请求（传递给下一个拦截器或引擎）
     * @param request 可修改后的请求
     * @return 响应
     */
    fun proceed(request: NetRequest): NetResponse {
        this.request = request
        // 还有未执行的拦截器 → 交给下一个
        if (index < interceptors.size) {
            NetLogger.log("InterceptorChain", "执行拦截器：${interceptors[index].javaClass.simpleName}")
            val nextChain = NetChain(
                interceptors = interceptors,
                index = index + 1,
                engineExecute = engineExecute
            )
            nextChain.request = request
            return interceptors[index].intercept(nextChain)
        }

        // 所有拦截器执行完毕 → 交给引擎执行
        return engineExecute(request)
    }
}

/** 旧名兼容别名（后续版本移除） */
@Deprecated("请使用 NetChain 替代", ReplaceWith("NetChain"))
typealias InterceptorChain = NetChain
