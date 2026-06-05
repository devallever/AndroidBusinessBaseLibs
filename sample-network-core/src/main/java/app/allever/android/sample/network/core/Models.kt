package app.allever.android.sample.network.core

import app.allever.android.lib.network.core.response.IBaseResponse
import app.allever.android.lib.network.core.response.ResponseCode
import app.allever.android.lib.network.core.response.ResponseData
import app.allever.android.lib.network.core.response.ResponseMsg

/**
 * 统一业务响应体（演示非标准字段名：errorCode / errorMsg）
 *
 * 通过 @ResponseCode / @ResponseMsg / @ResponseData 注解适配不同服务端字段名
 */
data class BaseResponse<T>(
    @ResponseCode val errorCode: Int = -1,
    @ResponseMsg val errorMsg: String = "",
    @ResponseData val data: T? = null
) : IBaseResponse {
    override fun getResponseCode(): Int = errorCode
    override fun getResponseMsg(): String = errorMsg
}

/**
 * Banner 数据
 */
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
