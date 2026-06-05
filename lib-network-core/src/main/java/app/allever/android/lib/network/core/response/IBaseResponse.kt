package app.allever.android.lib.network.core.response

import app.allever.android.lib.network.core.NetCore
import app.allever.android.lib.network.core.NetworkConfig

/**
 * 业务响应统一抽象接口
 *
 * 不强制继承，通过 ResponseAdapter 适配任意类。
 * 建议自定义的 BaseResponse 实现此接口以获得更好的类型支持。
 */
interface IBaseResponse {

    /** 获取业务状态码 */
    fun getResponseCode(): Int

    /** 获取业务消息 */
    fun getResponseMsg(): String

    /** 是否成功（与配置的 successCode 比较） */
    fun isSuccess(): Boolean = getResponseCode() == NetCore.config.successCode

    /** 是否失败 */
    fun isFailure(): Boolean = !isSuccess()

    /**
     * 获取数据（通过 ResponseAdapter 提取）
     * @param config 网络配置（用于字段映射）
     */
    fun <T> extractData(config: NetworkConfig): T? {
        return ResponseAdapter.extractData(this, config)
    }
}
